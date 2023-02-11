/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.session.*;
import org.eclipse.jetty.servlet.*;
import rife.config.RifeConfig;
import rife.ioc.HierarchicalProperties;
import rife.servlet.RifeFilter;

import java.util.EnumSet;

/**
 * Embedded Jetty server that can directly start from a RIFE2 site.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Server {
    private final HierarchicalProperties properties_;
    private final org.eclipse.jetty.server.Server server_ = new org.eclipse.jetty.server.Server();
    private final SessionIdManager sessions_ = new DefaultSessionIdManager(server_);
    private final ServletContextHandler handler_ = new ServletContextHandler();

    /**
     * Instantiates a new embedded Jetty server.
     * @since 1.0
     */
    public Server() {
        var system_properties = new HierarchicalProperties().putAll(System.getProperties());
        properties_ = new HierarchicalProperties().parent(system_properties);
    }

    /**
     * Configures the HTTP port the server will be listening to.
     *
     * @param port a port number (defaults to 8080)
     * @return the instance of the server that's being configured
     * @since 1.0
     */
    public Server port(int port) {
        RifeConfig.server().setPort(port);
        return this;
    }

    /**
     * Configures the base directory where static resources will be looked up.
     *
     * @param base a port number (defaults to 8080)
     * @return the instance of the server that's being configured
     * @since 1.0
     */
    public Server staticResourceBase(String base) {
        RifeConfig.server().setStaticResourceBase(base);
        return this;
    }

    /**
     * Retrieves the hierarchical properties for this server instance.
     *
     * @return this server's collection of hierarchical properties
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Start the embedded server for a particular site.
     *
     * @param site the {@code Site} instance that the server will be set up for
     * @return the instance of the server that's being started
     * @see Site
     * @since 1.0
     */
    public Server start(Site site) {
        try (var connector = new ServerConnector(server_)) {
            connector.setPort(RifeConfig.server().getPort());
            server_.setConnectors(new Connector[]{connector});
        }

        server_.setHandler(handler_);

        var session_handler = new SessionHandler();
        session_handler.getSessionCookieConfig().setHttpOnly(true);
        session_handler.setSessionIdManager(sessions_);
        handler_.setSessionHandler(session_handler);

        var rife_filter = new RifeFilter();
        rife_filter.init(properties_, site);
        var filter_holder = new FilterHolder(rife_filter);

        var ctx = new ServletContextHandler();
        ctx.setContextPath("/");

        var default_servlet = new DefaultServlet();
        var servlet_holder = new ServletHolder(default_servlet);
        if (RifeConfig.server().getStaticResourceBase() != null) {
            handler_.setResourceBase(RifeConfig.server().getStaticResourceBase());
        }

        handler_.addFilter(filter_holder, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler_.addServlet(servlet_holder, "/*");

        try {
            server_.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Stop running the embedded server.
     *
     * @since 1.0
     */
    public void stop() {
        try {
            server_.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
