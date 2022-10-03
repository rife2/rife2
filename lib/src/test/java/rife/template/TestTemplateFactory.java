/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.junit.jupiter.api.Test;
import rife.config.RifeConfig;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.exceptions.AmbiguousTemplateNameException;
import rife.template.exceptions.TemplateException;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class TestTemplateFactory {
    @Test
    public void testUniqueFactory() {
        assertSame(TemplateFactory.HTML, TemplateFactory.HTML);
    }

    @Test
    public void testUniqueParser() {
        assertSame(TemplateFactory.HTML.getParser(), TemplateFactory.HTML.getParser());
    }

    @Test
    public void testDefaultContentType() {
        assertEquals(TemplateFactory.HTML.get("testhtml_in").getDefaultContentType(), "text/html");
    }

    @Test
    public void testHtmlTemplate() {
        var template = TemplateFactory.HTML.get("templates.testhtml_in");
        template.setValue("first", "first1");
        template.setValue("second", "second1");
        template.appendBlock("lines", "line");
        template.setValue("first", "first2");
        template.setValue("second", "second2");
        template.appendBlock("lines", "line");
        template.setValue("first", "first3");
        template.setValue("second", "second3");
        template.appendBlock("lines", "line");
        assertEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("templates.testhtml_out"));
    }

    @Test
    public void testHtmlTemplatePath() {
        var template = TemplateFactory.HTML.get("testhtml_in");
        template.setValue("first", "first1");
        template.setValue("second", "second1");
        template.appendBlock("lines", "line");
        template.setValue("first", "first2");
        template.setValue("second", "second2");
        template.appendBlock("lines", "line");
        template.setValue("first", "first3");
        template.setValue("second", "second3");
        template.appendBlock("lines", "line");
        assertEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("testhtml_out"));
    }

    @Test
    public void testHtmlTemplateAmbiguous() {
        try {
            TemplateFactory.HTML.get("html.html");
            fail("exception not thrown");
        } catch (AmbiguousTemplateNameException e) {
            assertEquals(e.getName(), "html.html");
        }
    }

    @Test
    public void testTemplateInitializer() {
        var factory = TemplateFactory.HTML;
        var template = factory.get("testhtml_in", null, null);
        assertNotEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("testhtml_out"));

        factory.setInitializer(template1 -> {
            template1.setValue("first", "first1");
            template1.setValue("second", "second1");
            template1.appendBlock("lines", "line");
            template1.setValue("first", "first2");
            template1.setValue("second", "second2");
            template1.appendBlock("lines", "line");
            template1.setValue("first", "first3");
            template1.setValue("second", "second3");
            template1.appendBlock("lines", "line");
        });
        template = factory.get("testhtml_in", null, null);
        assertEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("testhtml_out"));
        template.setValue("first", "otherfirst1");
        template.setValue("second", "othersecond1");
        template.appendBlock("lines", "line");
        assertNotEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("testhtml_out"));
        template.clear();
        assertEquals(template.getContent(), TemplateFactory.HTML.getParser().getTemplateContent("testhtml_out"));

        factory.setInitializer(null);
    }

    @Test
    public void testCaching() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.HTML.get("defaultvalues_in");
            assertNotNull(template1);
            template2 = TemplateFactory.HTML.get("defaultvalues_in");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPackageFullName() {
        var template = TemplateFactory.HTML.get("test_package.noblocks_in");
        assertNotNull(template);
        assertEquals("test_package.noblocks_in", template.getFullName());
    }

    @Test
    public void testTemplatesInPackageCaching() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.HTML.get("test_package.noblocks_in");
            assertNotNull(template1);
            template2 = TemplateFactory.HTML.get("test_package.noblocks_in");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testTemplatesInPackagePath() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.HTML.get("test_package/noblocks_in.html");
            assertNotNull(template1);
            template2 = TemplateFactory.HTML.get("test_package/noblocks_in.html");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReloadBasic() {
        var resource_finder = TemplateFactory.HTML.getResourceFinder();

        try {
            // setup the temporary directory
            String template_dir = RifeConfig.instance().global.tempPath();
            File template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // setup the first template file
            ResourceFinderGroup group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.HTML.setResourceFinder(group);
            URL template1_resource = TemplateFactory.HTML.getParser().resolve("defaultvalues_in");
            String template1_name = "reload_basic";
            File template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.HTML.getParser().getExtension());
            template1_file.delete();
            try {
                FileUtils.copy(template1_resource.openStream(), template1_file);
            } catch (FileUtilsErrorException | IOException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
                return;
            }
            // obtain the original template
            Template template1 = null;
            try {
                template1 = TemplateFactory.HTML.get(template1_name);
                assertNotNull(template1);
            } catch (TemplateException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }

            // wait a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }

            // overwrite the template file with new content
            URL template2_resource = TemplateFactory.HTML.getParser().resolve("noblocks_in");
            try {
                FileUtils.copy(template2_resource.openStream(), template1_file);
            } catch (FileUtilsErrorException | IOException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
                return;
            }
            // obtain the modified template
            Template template2 = null;
            try {
                template2 = TemplateFactory.HTML.get(template1_name);
                assertNotNull(template2);
            } catch (TemplateException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }

            // verify if the template was correctly reloaded
            assertNotSame(template1, template2);
            assertNotSame(template1.getClass(), template2.getClass());
            assertTrue(template1.getModificationTime() < template2.getModificationTime());
            assertNotEquals(template1.getContent(), template2.getContent());

            // clean up the copied files
            template1_file.delete();
        } finally {
            TemplateFactory.HTML.setResourceFinder(resource_finder);
        }
    }
}