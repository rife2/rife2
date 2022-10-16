/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.eclipse.jetty.util.VirtualThreads;
import rife.Version;
import rife.config.RifeConfig;
import rife.engine.exceptions.DeferException;
import rife.engine.exceptions.RedirectException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.template.exceptions.SyntaxErrorException;
import rife.tools.ExceptionFormattingUtils;
import rife.tools.ExceptionUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class Gate {
    private Site site_;
    private Throwable initException_ = null;

    public void setup(Site site)
    {
        site_ = site;

        try {
            site_.setup();
        }
        catch (Throwable e)
        {
            handleSiteInitException(e);
        }
    }

    public boolean handleRequest(String gateUrl, String elementUrl, Request request, Response response) {
        // ensure a valid element url
        if (null == elementUrl ||
            0 == elementUrl.length()) {
            elementUrl = "/";
        }

        // strip away the optional path parameters
        int path_parameters_index = elementUrl.indexOf(";");
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
        RouteMatch match = site_.findRouteForRequest(request, elementUrl);
        // If no element was found, don't continue executing the gate logic.
        // This could allow a next filter in the chain to be executed.
        if (null == match) {
            return false;
        }

        var context = new Context(gateUrl, site_, request, response, match);
        try {
            // TODO : handle before and after routes
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
        String message = "Error on host " + c.request().getServerName() + ":" + c.request().getServerPort() + "/" + c.request().getContextPath();
        if (RifeConfig.engine().getLogEngineExceptions()) {
            Logger.getLogger("rife.engine").severe(message + "\n" + ExceptionUtils.getExceptionStackTrace(exception));
        }

        c.setEngineException(exception);

        Route exception_route = site_.getExceptionRoute();
        if (exception_route != null) {
            try {
                exception_route.getElementInstance(c).process(c);
                return;
            } catch (Exception ignored) {
            }
        }

        if (!RifeConfig.engine().getPrettyEngineExceptions()) {
            c.setEngineException(exception);

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
            String content_type = response.getContentType();
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
        Template template = template_factory.get("errors.rife.engine_error");
        template.setValue("exceptions", ExceptionFormattingUtils.formatExceptionStackTrace(exception, template));
        template.setValue("RIFE_VERSION", template.getEncoder().encode(Version.getVersion()));

        try {
            response.getWriter().print(template.getContent());
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }
}

