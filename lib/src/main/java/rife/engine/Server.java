/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.EnumSet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import jakarta.servlet.DispatcherType;
import rife.ioc.HierarchicalProperties;
import rife.resources.ResourceFinderClasspath;
import rife.servlet.RifeFilter;

/**
 * Embedded Jetty server that can directly start from a RIFE2 site.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Server {
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_HOST = null;
    public static final String DEFAULT_STATIC_RESOURCE_BASE = null;
    public static final int DEFAULT_MIN_THREADS = 8;
    public static final int DEFAULT_MAX_THREADS = 200;
    public static final int DEFAULT_IDLE_TIMEOUT_MS = 60000;

    private int port_ = DEFAULT_PORT;
    private String host_ = DEFAULT_HOST;
    private String staticResourceBase_ = DEFAULT_STATIC_RESOURCE_BASE;
    private int minThreads_ = DEFAULT_MIN_THREADS;
    private int maxThreads_ = DEFAULT_MAX_THREADS;
    private int idleTimeout_ = DEFAULT_IDLE_TIMEOUT_MS;

    protected String sslKeyStorePath_ = null;
    protected String sslKeyStorePassword_ = null;
    protected String sslCertAlias_ = null;
    protected String sslTrustStorePath_ = null;
    protected String sslTrustStorePassword_ = null;
    protected boolean sslNeedClientAuth_ = false;
    protected boolean sslWantClientAuth_ = false;
    protected boolean useLoom = true;

    private final HierarchicalProperties properties_;
    private org.eclipse.jetty.server.Server server_;

    /**
     * Instantiates a new embedded Jetty server.
     *
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
        port_ = port;
        return this;
    }

    /**
     * Configures the host the server will be listening on.
     *
     * @param host the hostname to listen on
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server host(String host) {
        host_ = host;
        return this;
    }

    /**
     * Configures the base path where static resources will be looked up.
     *
     * @param base the base path for static resources
     * @return the instance of the server that's being configured
     * @since 1.0
     */
    public Server staticResourceBase(String base) {
        staticResourceBase_ = base;
        return this;
    }

    /**
     * Configures the base directory where static resources will be looked up from.
     * <p>
     * This will be relative to the classpath root of an Uber Jar that contains your
     * web application as well as the RIFE2 framework classes and everything else
     * required to run your web application.
     *
     * @param base the base directory for static resources
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server staticUberJarResourceBase(String base) {
        var version = "RIFE_VERSION";
        var resource = ResourceFinderClasspath.instance().getResource(version);
        var path = resource.toString();
        staticResourceBase_ = path.substring(0, path.length() - version.length()) + base;
        return this;
    }

    /**
     * Configures the minimum number of threads to use.
     * <p>
     * Defaults to {@code 8}.
     *
     * @param minThreads the minimum number of threads to use
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server minThreads(int minThreads) {
        minThreads_ = minThreads;
        return this;
    }

    /**
     * Configures the maximum number of threads to use.
     * <p>
     * Defaults to {@code 200}.
     *
     * @param maxThreads the maximum number of threads to use
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server maxThreads(int maxThreads) {
        maxThreads_ = maxThreads;
        return this;
    }

    /**
     * Configures the maximum thread idle time in ms.
     * <p>
     * Defaults to {@code 60000}.
     *
     * @param idleTimeout the maximum thread idle time in ms
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server idleTimeout(int idleTimeout) {
        idleTimeout_ = idleTimeout;
        return this;
    }

    /**
     * Sets the file system path to the SSL key store.
     * <p>
     * When this is provided, the embedded server will switch from http to
     * https. Other SSL parameters have no effect unless the key store path
     * is configured.
     *
     * @param path the SSL key store path
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslKeyStorePath(String path) {
        sslKeyStorePath_ = path;
        return this;
    }

    /**
     * Sets the password for the SSL key store.
     *
     * @param password the SSL key store password
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslKeyStorePassword(String password) {
        sslKeyStorePassword_ = password;
        return this;
    }

    /**
     * Sets the SSL certificate alias.
     *
     * @param alias the SSL certificate alias
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslCertAlias(String alias) {
        sslCertAlias_ = alias;
        return this;
    }

    /**
     * Sets the path to the SSL trust store.
     *
     * @param path the SSL trust store path
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslTrustStorePath(String path) {
        sslTrustStorePath_ = path;
        return this;
    }

    /**
     * Sets the password for the SSL trust store.
     *
     * @param password the SSL trust store password
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslTrustStorePassword(String password) {
        sslTrustStorePassword_ = password;
        return this;
    }

    /**
     * Sets whether the SSL client certificate needs to be authenticated.
     *
     * @param auth {@code true} if the client certificate needs to be authenticated; or
     *             {@code false} otherwise
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslNeedClientAuth(boolean auth) {
        sslNeedClientAuth_ = auth;
        return this;
    }

    /**
     * Sets whether the server wants SSL client certificate to be authenticated.
     *
     * @param auth {@code true} of server wants the client certificate to be authenticated; or
     *             {@code false} otherwise
     * @return the instance of the server that's being configured
     * @since 1.1
     */
    public Server sslWantClientAuth(boolean auth) {
        sslWantClientAuth_ = auth;
        return this;
    }

    /**
     * By default, Rife will attempt to use virtual threads if available. This method explicitly disables virtual threads to use a standard QueuedThreadPool.
     *
     * @param auth {@code true} of server wants the client certificate to be authenticated; or
     *             {@code false} otherwise
     * @return the instance of the server that's being configured
     */
    public Server disableLoom(boolean loom) {
        this.useLoom = !loom;
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
        ThreadPool thread_pool;

        if (useLoom) {
        	try {
        		thread_pool = new LoomThreadPool();
        	} catch (IllegalStateException e) {
        		thread_pool  = new QueuedThreadPool(maxThreads_, minThreads_, idleTimeout_);
        	}
        }

        else {
        	thread_pool  = new QueuedThreadPool(maxThreads_, minThreads_, idleTimeout_);
        }
        server_ = new org.eclipse.jetty.server.Server(thread_pool);
        SessionIdManager sessions_ = new DefaultSessionIdManager(server_);
        ServletContextHandler handler_ = new ServletContextHandler();

        SslContextFactory.Server sslContextFactory = null;
        if (sslKeyStorePath_ != null) {
            sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(sslKeyStorePath_);

            if (sslKeyStorePassword_ != null) {
                sslContextFactory.setKeyStorePassword(sslKeyStorePassword_);
            }
            if (sslCertAlias_ != null) {
                sslContextFactory.setCertAlias(sslCertAlias_);
            }
            if (sslTrustStorePath_ != null) {
                sslContextFactory.setTrustStorePath(sslTrustStorePath_);
            }
            if (sslTrustStorePassword_ != null) {
                sslContextFactory.setTrustStorePassword(sslTrustStorePassword_);
            }
            if (sslNeedClientAuth_) {
                sslContextFactory.setNeedClientAuth(true);
            }
            if (sslWantClientAuth_) {
                sslContextFactory.setWantClientAuth(true);
            }
        }

        try (var connector = new ServerConnector(server_, sslContextFactory)) {
            connector.setPort(port_);
            if (host_ != null) {
                connector.setHost(host_);
            }
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
        if (staticResourceBase_ != null) {
            handler_.setResourceBase(staticResourceBase_);
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
