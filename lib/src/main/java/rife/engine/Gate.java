/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.Version;
import rife.config.RifeConfig;
import rife.continuations.*;
import rife.continuations.exceptions.PauseException;
import rife.engine.exceptions.DeferException;
import rife.engine.exceptions.RedirectException;
import rife.template.TemplateFactory;
import rife.tools.ExceptionFormattingUtils;
import rife.tools.ExceptionUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main entry class into the RIFE2 web engine, used both by the servlet engine and
 * the out of container testing API.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Gate {
    final String CONTINUATION_COOKIE_ID = "continuationId";


    private Site site_ = null;
    private Throwable initException_ = null;

    final ContinuationManager continuationManager_ = new ContinuationManager(new EngineContinuationConfigRuntime(this));

    /**
     * Set up the gate with the provided <code>Site</code>.
     *
     * @param site the site that will handle the requests
     * @since 1.0
     */
    public void setup(Site site) {
        site_ = site;

        try {
            site_.setup();
            site_.deploy();
        } catch (Throwable e) {
            handleSiteInitException(e);
        }
    }

    /**
     * Handle the web request with the provided arguments.
     *
     * @param gateUrl    the part of the URL that corresponds to the root of the gate, typically the webapp context URL
     * @param elementUrl the part of the URL after the gateUrl that will be resolved to find the execution element
     * @param request    the request instance of this web request
     * @param response   the response instance of this web request
     * @return <code>true</code> if the request was successfully handled; or
     * <code>false</code> otherwise
     * @since 1.0
     */
    public boolean handleRequest(String gateUrl, String elementUrl, Request request, Response response) {
        // check if the gateUrl hasn't been overridden by a webapp context path configuration parameter
        final var webapp_context_path = RifeConfig.engine().getWebappContextPath();
        if (webapp_context_path != null) {
            gateUrl = webapp_context_path;
        }

        // ensure a valid element url
        if (null == elementUrl ||
            0 == elementUrl.length()) {
            elementUrl = "/";
        }

        // strip away the optional path parameters
        var path_parameters_index = elementUrl.indexOf(";");
        if (path_parameters_index != -1) {
            elementUrl = elementUrl.substring(0, path_parameters_index);
        }

        // Handle the request
        // check if an exception occurred during the initialization
        if (initException_ != null) {
            handleRequestException(initException_, new Context(gateUrl, site_, request, response, null));
            return true;
        }

        // Set up the element request and process it.
        var match = site_.findRouteForRequest(request, elementUrl);
        // If no element was found, don't continue executing the gate logic.
        // This could allow a next filter in the chain to be executed.
        if (null == match) {
            return false;
        }

        var context = new Context(gateUrl, site_, request, response, match);
        try {
            setupContinuationContext(context);

            context.process();
            response.close();
        } catch (PauseException e) {
            handlePause(e, context);
            response.close();
        } catch (RedirectException e) {
            response.sendRedirect(e.getUrl());
        } catch (DeferException e) {
            return false;
        } catch (Throwable e) {
            handleRequestException(e, context);
            response.close();
        }

        return true;
    }

    private void setupContinuationContext(Context context)
    throws CloneNotSupportedException {
        ContinuationContext continuation_context = null;
        if (context.hasCookie(CONTINUATION_COOKIE_ID)) {
            continuation_context = continuationManager_.resumeContext(context.cookieValue(CONTINUATION_COOKIE_ID));
        }
        if (continuation_context != null) {
            ContinuationContext.setActiveContext(continuation_context);
        }
        ContinuationConfigRuntime.setActiveConfigRuntime(continuationManager_.getConfigRuntime());
    }

    private void handlePause(PauseException e, Context context) {
        // register context
        var continuation_context = e.getContext();
        continuationManager_.addContext(continuation_context);

        // obtain continuation ID
        var continuation_id = continuation_context.getId();
        context.addCookie(new CookieBuilder(CONTINUATION_COOKIE_ID, continuation_id)
            .path("/")
            .maxAge((int) continuationManager_.getConfigRuntime().getContinuationDuration() / 1000));
    }

    private void handleSiteInitException(Throwable exception) {
        // ensure the later init exceptions don't overwrite earlier ones
        if (null == initException_) {
            if (RifeConfig.engine().getPrettyEngineExceptions()) {
                initException_ = exception;
                if (RifeConfig.engine().getLogEngineExceptions()) {
                    Logger.getLogger("rife.engine").severe("Error while initializing site\n" + ExceptionUtils.getExceptionStackTrace(exception));
                }
            } else {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    private void handleRequestException(Throwable exception, Context c) {
        var message = "Error on host " + c.request().getServerName() + ":" + c.request().getServerPort() + "/" + c.request().getContextPath();
        if (RifeConfig.engine().getLogEngineExceptions()) {
            Logger.getLogger("rife.engine").severe(message + "\n" + ExceptionUtils.getExceptionStackTrace(exception));
        }

        c.engineException(exception);

        Router router = site_;
        if (c.route() != null && c.route().router() != null) {
            router = c.route().router();
        }

        var exception_route = router.getExceptionRoute();
        if (exception_route != null) {
            try {
                c.processElement(exception_route);
                return;
            } catch (Exception ignored) {
            }
        }

        if (!RifeConfig.engine().getPrettyEngineExceptions()) {
            c.engineException(exception);

            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else {
                throw new RuntimeException(message, exception);
            }
        }

        printExceptionDetails(exception, c.response());
    }

    private void printExceptionDetails(Throwable exception, Response response) {
        TemplateFactory template_factory = null;
        if (response.isContentTypeSet()) {
            var content_type = response.getContentType();
            if (content_type.startsWith("text/xml") ||
                content_type.startsWith("application/xhtml+xml")) {
                template_factory = TemplateFactory.XML;
                response.setContentType("text/xml");
            }
        }
        if (null == template_factory) {
            template_factory = TemplateFactory.HTML;
            response.setContentType("text/html");
        }

        // pretty exception formatting and outputting instead of the default servlet
        // engine's formatting
        var template = template_factory.get("errors.rife.engine_error");
        template.setValue("exceptions", ExceptionFormattingUtils.formatExceptionStackTrace(exception, template));
        template.setValue("RIFE_VERSION", template.getEncoder().encode(Version.getVersion()));

        try {
            response.getWriter().print(template.getContent());
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }
}

