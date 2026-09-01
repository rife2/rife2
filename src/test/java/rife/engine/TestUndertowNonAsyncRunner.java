/*
 * Copyright 2026 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.handlers.DefaultServlet;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.DispatcherType;
import rife.ioc.HierarchicalProperties;
import rife.servlet.RifeFilter;

import java.nio.file.Path;

import static io.undertow.servlet.Servlets.deployment;

/**
 * Embedded Undertow server whose filter and servlet declare no async
 * support, reproducing a plain deployment where async hasn't been
 * enabled — unlike {@link TestUndertowRunner}, which goes through
 * {@link UndertowServer} and always enables it.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.10
 */
public class TestUndertowNonAsyncRunner implements AutoCloseable {
    private final Undertow undertow_;
    private final DeploymentManager manager_;

    public TestUndertowNonAsyncRunner(Site site, int port) {
        try {
            var properties =
                new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance());
            var rifeFilter = new RifeFilter();
            rifeFilter.init(properties, site);

            var rifeFilterInfo = new FilterInfo("RIFE2", RifeFilter.class, new ImmediateInstanceFactory<>(rifeFilter));
            var defaultServlet = Servlets.servlet("default-servlet", DefaultServlet.class).addMapping("/");

            DeploymentInfo deploymentInfo = deployment()
                .setClassLoader(TestUndertowNonAsyncRunner.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("rife2-nonasync-" + port)
                .setResourceManager(new PathResourceManager(Path.of(".").toAbsolutePath().normalize(), 1024))
                .addFilter(rifeFilterInfo)
                .addFilterUrlMapping("RIFE2", "/*", DispatcherType.REQUEST)
                .addServlet(defaultServlet);

            manager_ = Servlets.defaultContainer().addDeployment(deploymentInfo);
            manager_.deploy();
            var servletHandler = manager_.start();

            undertow_ = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(servletHandler)
                .build();
            undertow_.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start non-async Undertow server", e);
        }
    }

    public void close() {
        try {
            manager_.stop();
        } catch (Exception e) {
            System.err.println("[Undertow] Failed to stop deployment manager: " + e.getLocalizedMessage());
        }
        try {
            var deploymentInfo = manager_.getDeployment().getDeploymentInfo();
            manager_.undeploy();
            Servlets.defaultContainer().removeDeployment(deploymentInfo);
        } catch (Exception e) {
            System.err.println("[Undertow] Failed to undeploy: " + e.getLocalizedMessage());
        }
        undertow_.stop();
    }
}