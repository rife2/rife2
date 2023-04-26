/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.Version;
import rife.config.RifeConfig;
import rife.engine.exceptions.DeferException;
import rife.engine.exceptions.RedirectException;
import rife.ioc.HierarchicalProperties;
import rife.template.TemplateFactory;
import rife.tools.ExceptionFormattingUtils;
import rife.tools.ExceptionUtils;

import java.util.logging.Logger;

/**
 * Main entry class into the RIFE2 web engine, used both by the servlet engine and
 * the out of container testing API.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Gate {
    private Site site_ = null;
    private Throwable initException_ = null;

    /**
     * Set up the gate with the provided {@code Site}.
     *
     * @param properties the parent hierarchical properties
     * @param site the site that will handle the requests
     * @since 1.0
     */
    public void setup(HierarchicalProperties properties, Site site) {
        site.properties_.setParent(properties);
        site_ = site;

        if (!site.deployed_) {
            try {
                site_.setup();
                site_.deploy();
            } catch (Throwable e) {
                handleSiteInitException(e);
            }
        }
    }

    /**
     * Tears down the gate and the associated {@code Site}.
     *
     * @since 1.6.1
     */
    public void destroy() {
        site_.destroy();
    }

    /**
     * Retrieves the {@code Site} of this gate.
     * @return this gate's site
     * @since 1.6.1
     */
    public Site getSite() {
        return site_;
    }

    /**
     * Handle the web request with the provided arguments.
     *
     * @param gateUrl    the part of the URL that corresponds to the root of the gate, typically the webapp context URL
     * @param elementUrl the part of the URL after the gateUrl that will be resolved to find the execution element
     * @param request    the request instance of this web request
     * @param response   the response instance of this web request
     * @return {@code true} if the request was successfully handled; or
     * {@code false} otherwise
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
            context.process();
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
        var message = "Error on host " + c.request().getServerName() + ":" + c.request().getServerPort() + c.request().getContextPath();
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
        response.clearBuffer();

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

        response.print(template.getContent());
    }
}

