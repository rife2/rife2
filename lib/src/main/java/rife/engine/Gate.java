/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

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
    public final static String REQUEST_ATTRIBUTE_RIFE_ENGINE_EXCEPTION = "rife.engine.exception";

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
            printExceptionDetails(response, initException_);
            return true;
        }

        // Set up the element request and process it.
        Site.RouteMatch match = site_.findRouteForRequest(request, elementUrl);

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
            handleRequestException(request, response, e);
        }

        return true;
    }

    private void handleSiteInitException(Throwable e) {
        // ensure the later init exceptions don't overwrite earlier ones
        if (null == initException_) {
            if (RifeConfig.engine().getPrettyEngineExceptions()) {
                initException_ = e;
            } else {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleRequestException(Request request, Response response, Throwable e) {
        String message = "Error on host " + request.getServerName() + ":" + request.getServerPort() + "/" + request.getContextPath();
        if (RifeConfig.engine().getLogEngineExceptions()) {
            Logger.getLogger("rife.engine").severe(message + "\n" + ExceptionUtils.getExceptionStackTrace(e));
        }

        if (!RifeConfig.engine().getPrettyEngineExceptions()) {
            request.setAttribute(REQUEST_ATTRIBUTE_RIFE_ENGINE_EXCEPTION, e);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(message, e);
            }
        }

        printExceptionDetails(response, e);
    }

    private void printExceptionDetails(Response response, Throwable exception) {
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
        Template template = null;

        Throwable cause = exception;
        while (cause != null && cause.getCause() != cause) {

            if (cause instanceof SyntaxErrorException) {
                template = template_factory.get("errors.rife.engine_error_compilation");
                break;
            }

            cause = cause.getCause();
        }

        if (null == template) {
            template = template_factory.get("errors.rife.engine_error_default");
        }


        template.setValue("exceptions", ExceptionFormattingUtils.formatExceptionStackTrace(exception, template));
        template.setValue("RIFE_VERSION", template.getEncoder().encode(Version.getVersion()));

        try {
            response.getWriter().print(template.getContent());
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }
}

