/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.xml;

import org.junit.jupiter.api.Test;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestXml2Data {
    @Test
    void testInstantiation()
    throws ResourceFinderErrorException {
        var xml = new Xml2DataTest();
        assertNotNull(xml);
    }

    @Test
    void testProcess()
    throws ResourceFinderErrorException {
        var xml = new Xml2DataTest();
        var content = ResourceFinderClasspath.instance().getContent("xml/jetty-server-11.0.14.pom.xml");
        assertTrue(xml.processXml(content));
        assertEquals(55, xml.getElements().size());
        assertEquals("""
            project-parent-groupId : org.eclipse.jetty
            project-parent-artifactId : jetty-project
            project-parent-version : 11.0.14
            project-parent :\s
            project-modelVersion : 4.0.0
            project-artifactId : jetty-server
            project-name : Jetty :: Server Core
            project-description : The core jetty server artifact.
            project-properties-bundle-symbolic-name : ${project.groupId}.server
            project-properties-spotbugs.onlyAnalyze : org.eclipse.jetty.server.*
            project-properties :\s
            project-build-plugins-plugin-artifactId : maven-surefire-plugin
            project-build-plugins-plugin-configuration-argLine : @{argLine} ${jetty.surefire.argLine}
                        --add-opens org.eclipse.jetty.server/org.eclipse.jetty.server=ALL-UNNAMED
                        --add-reads org.eclipse.jetty.server=org.eclipse.jetty.logging
            project-build-plugins-plugin-configuration :\s
            project-build-plugins-plugin :\s
            project-build-plugins :\s
            project-build :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty.toolchain
            project-dependencies-dependency-artifactId : jetty-jakarta-servlet-api
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-http
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-io
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-jmx
            project-dependencies-dependency-optional : true
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.slf4j
            project-dependencies-dependency-artifactId : slf4j-api
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-xml
            project-dependencies-dependency-scope : test
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty.toolchain
            project-dependencies-dependency-artifactId : jetty-test-helper
            project-dependencies-dependency-scope : test
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty.tests
            project-dependencies-dependency-artifactId : jetty-http-tools
            project-dependencies-dependency-scope : test
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-util-ajax
            project-dependencies-dependency-scope : test
            project-dependencies-dependency :\s
            project-dependencies-dependency-groupId : org.eclipse.jetty
            project-dependencies-dependency-artifactId : jetty-slf4j-impl
            project-dependencies-dependency-scope : test
            project-dependencies-dependency :\s
            project-dependencies :\s
            project :\s""", StringUtils.join(xml.getElements(), "\n"));
    }


    @Test
    void testProcessValidated()
    throws ResourceFinderErrorException {
        var xml = new Xml2DataTest();
        xml.enableValidation(true);
        var content = ResourceFinderClasspath.instance().getContent("xml/jetty-server-11.0.14.pom.xml");
        assertFalse(xml.processXml(content));
        assertFalse(xml.getErrors().isEmpty());
    }
}
