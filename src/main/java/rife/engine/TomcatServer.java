/*
 * Copyright 2023 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.apache.catalina.startup.Tomcat;
import rife.config.RifeConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Embedded Tomcat server that can directly start from a RIFE2 site.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.7.1
 */
public class TomcatServer {
    private final Map<String, String> roles_ = new HashMap<>();
    private final Map<String, String> users_ = new HashMap<>();
    private final Map<String, String> webapps_ = new HashMap<>();
    private String baseDir_ = null;
    private String hostname_ = null;
    private Tomcat tomcat_;
    private int port_ = 8080;

    /**
     * Add a role for a user.
     */
    public TomcatServer addRole(String role, String user) {
        roles_.put(role, user);
        return this;
    }

    /**
     * Add a user for the in-memory realm.
     */
    public TomcatServer addUser(String user, String pass) {
        users_.put(user, pass);
        return this;
    }

    /**
     * Add the location of the webapp directory or war.
     *
     * @param contextPath The context mapping to use, {@code ""} for root context.
     * @param docBase     Base directory for the context, for static files. Must exist.
     * @see #addWebapp(String)
     */
    public TomcatServer addWebapp(String contextPath, String docBase) {
        webapps_.put(contextPath, docBase);
        return this;
    }

    /**
     * Add the location of the webapp directory or war for the root context.
     *
     * @param docBase Base directory for the context, for static files. Must exist.
     * @see #addWebapp(String, String)
     */
    public TomcatServer addWebapp(String docBase) {
        webapps_.put("", docBase);
        return this;
    }

    /**
     * Configures the host name the server will be listening on.
     */
    public TomcatServer hostname(String host) {
        hostname_ = host;
        return this;
    }

    /**
     * Configures the port the server will be listening to.
     */
    public TomcatServer port(int port) {
        port_ = port;
        return this;
    }

    /**
     * Configures the base directory location.
     */
    public TomcatServer baseDir(String dir) {
        baseDir_ = dir;
        return this;
    }

    /**
     * Starts the embedded server.
     */
    public TomcatServer start() {
        tomcat_ = new Tomcat();

        if (baseDir_ == null) {
            var tmpDir = new File(RifeConfig.global().getTempPath(), "rife2.tomcat." + port_);
            tmpDir.deleteOnExit();
            baseDir_ = tmpDir.getAbsolutePath();
        }
        tomcat_.setBaseDir(baseDir_);

        webapps_.forEach((c, d) -> tomcat_.addWebapp(c, new File(d).getAbsolutePath()));

        tomcat_.setPort(port_);

        if (hostname_ != null) {
            tomcat_.setHostname(hostname_);
        }

        users_.forEach((u, p) -> tomcat_.addUser(u, p));

        roles_.forEach((u, r) -> tomcat_.addRole(u, r));

        // Tomcat opens the port only if called at least once
        tomcat_.getConnector();

        try {
            tomcat_.start();
            tomcat_.getServer().await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Stops the embedded server.
     */
    public void stop() {
        try {
            tomcat_.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}