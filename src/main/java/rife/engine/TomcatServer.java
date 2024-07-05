/*
 * Copyright 2023 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import rife.config.RifeConfig;
import rife.ioc.HierarchicalProperties;
import rife.servlet.RifeFilter;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

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
    private final HierarchicalProperties properties_;
    private final Map<String, String> roles_ = new HashMap<>();
    private final Map<String, String> users_ = new HashMap<>();
    private String baseDir_ = null;
    private String docBase_ = ".";
    private String hostname_ = null;
    private Tomcat tomcat_;
    private int port_ = 8080;

    /**
     * Instantiates a new embedded Tomcat server.
     *
     * @since 1.7.1
     */
    public TomcatServer() {
        properties_ = new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance());
    }

    /**
     * Add a role for a user.
     *
     * @param role the role name
     * @param user the user name
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer addRole(String role, String user) {
        roles_.put(role, user);
        return this;
    }

    /**
     * Add a user for the in-memory realm.
     *
     * @param user the user name
     * @param pass the password
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer addUser(String user, String pass) {
        users_.put(user, pass);
        return this;
    }

    /**
     * Add the location of the webapp directory or WAR for the root context.
     *
     * @param docBase the base directory for the context, for static file.
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer addWebapp(String docBase) {
        docBase_ = docBase;
        return this;
    }

    /**
     * Configures the Tomcat base directory on which all others, such as the {@code work} directory, will be derived.
     *
     * @param dir the base directory
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer baseDir(String dir) {
        baseDir_ = dir;
        return this;
    }

    /**
     * Configures the host name the server will be listening on.
     *
     * @param host the default host name
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer hostname(String host) {
        hostname_ = host;
        return this;
    }

    /**
     * Configures the port the server will be listening to.
     *
     * @param port the port number
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer port(int port) {
        port_ = port;
        return this;
    }

    /**
     * Retrieves the hierarchical properties for this server instance.
     *
     * @return this server's collection of hierarchical properties
     * @since 1.7.1
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Starts the embedded server.
     *
     * @param site the {@code Site} instance that the server will be set up for
     * @return the instance of the server that's being configured
     * @since 1.7.1
     */
    public TomcatServer start(Site site) {
        tomcat_ = new Tomcat();

        if (baseDir_ == null) {
            var tmpDir = new File(RifeConfig.global().getTempPath(), "rife2.tomcat." + port_);
            tmpDir.deleteOnExit();
            tomcat_.setBaseDir(tmpDir.getAbsolutePath());
        } else {
            tomcat_.setBaseDir(new File(baseDir_).getAbsolutePath());
        }

        var ctx = tomcat_.addWebapp("", new File(docBase_).getAbsolutePath());

        var servletName = "default-servlet";
        var defaultServlet = new DefaultServlet();
        Tomcat.addServlet(ctx, servletName, defaultServlet);
        ctx.addServletMappingDecoded("/*", servletName);

        var filterName = "RIFE2";
        var rifeFilter = new RifeFilter();
        rifeFilter.init(properties_, site);

        var filerDef = new FilterDef();
        filerDef.setFilter(rifeFilter);
        filerDef.setFilterName(filterName);
        ctx.addFilterDef(filerDef);

        var filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Stops the embedded server.
     *
     * @since 1.7.1
     */
    public void stop() {
        try {
            tomcat_.stop();
            tomcat_.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}