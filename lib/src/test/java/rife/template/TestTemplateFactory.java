/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.junit.jupiter.api.Test;
import rife.config.Config;
import rife.config.RifeConfig;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.exceptions.AmbiguousTemplateNameException;
import rife.template.exceptions.ResourceBundleNotFoundException;
import rife.template.exceptions.TemplateException;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.Localization;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

public class TestTemplateFactory {
    @Test
    public void testUniqueFactoryHtml() {
        assertSame(TemplateFactory.HTML, TemplateFactory.HTML);
    }

    @Test
    public void testUniqueFactoryTxt() {
        assertSame(TemplateFactory.TXT, TemplateFactory.TXT);
    }

    @Test
    public void testUniqueParserHtml() {
        assertSame(TemplateFactory.HTML.getParser(), TemplateFactory.HTML.getParser());
    }

    @Test
    public void testUniqueParserTxt() {
        assertSame(TemplateFactory.TXT.getParser(), TemplateFactory.TXT.getParser());
    }

    @Test
    public void testDefaultContentTypeHtml() {
        assertEquals(TemplateFactory.HTML.get("testhtml_in").getDefaultContentType(), "text/html");
    }

    @Test
    public void testDefaultContentTypeTxt() {
        assertEquals(TemplateFactory.TXT.get("testtext_in").getDefaultContentType(), "text/plain");
    }

    @Test
    public void testTemplateHtml() {
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
    public void testTemplateTxt() {
        var template = TemplateFactory.TXT.get("templates.testtext_in");
        template.setValue("first", "first1");
        template.setValue("second", "second1");
        template.appendBlock("lines", "line");
        template.setValue("first", "first2");
        template.setValue("second", "second2");
        template.appendBlock("lines", "line");
        template.setValue("first", "first3");
        template.setValue("second", "second3");
        template.appendBlock("lines", "line");
        assertEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("templates.testtext_out"));
    }

    @Test
    public void testTemplatePathHtml() {
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
    public void testTemplatePathTxt() {
        var template = TemplateFactory.TXT.get("testtext_in");
        template.setValue("first", "first1");
        template.setValue("second", "second1");
        template.appendBlock("lines", "line");
        template.setValue("first", "first2");
        template.setValue("second", "second2");
        template.appendBlock("lines", "line");
        template.setValue("first", "first3");
        template.setValue("second", "second3");
        template.appendBlock("lines", "line");
        assertEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("testtext_out"));
    }

    @Test
    public void testTemplateAmbiguousHtml() {
        try {
            TemplateFactory.HTML.get("html.html");
            fail("exception not thrown");
        } catch (AmbiguousTemplateNameException e) {
            assertEquals(e.getName(), "html.html");
        }
    }

    @Test
    public void testTemplateAmbiguousTxt() {
        try {
            TemplateFactory.TXT.get("txt.txt");
            fail("exception not thrown");
        } catch (AmbiguousTemplateNameException e) {
            assertEquals(e.getName(), "txt.txt");
        }
    }

    @Test
    public void testTemplateInitializerHtml() {
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
    public void testTemplateInitializerTxt() {
        var factory = TemplateFactory.TXT;
        var template = factory.get("testtext_in", null, null);
        assertNotEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("testtext_out"));

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
        template = factory.get("testtext_in", null, null);
        assertEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("testtext_out"));
        template.setValue("first", "otherfirst1");
        template.setValue("second", "othersecond1");
        template.appendBlock("lines", "line");
        assertNotEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("testtext_out"));
        template.clear();
        assertEquals(template.getContent(), TemplateFactory.TXT.getParser().getTemplateContent("testtext_out"));

        factory.setInitializer(null);
    }

    @Test
    public void testFilteredTagsRenderHtml()
    throws Exception {
        RendererImpl.sCount = 0;
        Template t = TemplateFactory.HTML.get("filtered_tags_render");
        assertEquals("This is the render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
            "This is another render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
            "This is the render value with a differentiator 'RENDER:RIFE.TEMPLATE.RENDERERIMPL:DIFFERENT:different:2'.\n", t.getContent());
    }

    @Test
    public void testFilteredTagsRenderTxt()
    throws Exception {
        RendererImpl.sCount = 0;
        Template t = TemplateFactory.TXT.get("filtered_tags_render");
        assertEquals("This is the render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
            "This is another render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
            "This is the render value with a differentiator 'RENDER:RIFE.TEMPLATE.RENDERERIMPL:DIFFERENT:different:2'.\n", t.getContent());
    }

    @Test
    public void testFilteredTagsConfigHtml()
    throws Exception
    {
        Config.instance().setParameter("TEMPLATE_CONFIG_VALUE", "the config value");
        var template = TemplateFactory.HTML.get("filtered_tags_config");
        assertEquals("This is the config value 'the config value'.\nThis is an unknown config value '{{v config:TEMPLATE_CONFIG_VALUE_UNKNOWN/}}'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsConfigTxt()
    throws Exception
    {
        Config.instance().setParameter("TEMPLATE_CONFIG_VALUE", "the config value");
        var template = TemplateFactory.TXT.get("filtered_tags_config");
        assertEquals("This is the config value 'the config value'.\nThis is an unknown config value '{{v config:TEMPLATE_CONFIG_VALUE_UNKNOWN/}}'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsL10nHtml()
    throws Exception {
        var template = TemplateFactory.HTML.get("filtered_tags_l10n");
        assertEquals("This is the localized key 'default value'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "nl"));
        assertEquals("This is the localized key 'De Nederlandse tekst'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "fr"));
        assertEquals("This is the localized key 'Le texte Francais'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsL10nTxt()
    throws Exception {
        var template = TemplateFactory.TXT.get("filtered_tags_l10n");
        assertEquals("This is the localized key 'default value'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "nl"));
        assertEquals("This is the localized key 'De Nederlandse tekst'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "fr"));
        assertEquals("This is the localized key 'Le texte Francais'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsL10nHtmlResourceBundleNotFound()
    throws Exception {
        try {
            TemplateFactory.HTML.get("filtered_tags_l10n_bundlenotfound");
            fail();
        } catch (ResourceBundleNotFoundException e) {
            assertEquals(e.getTemplateName(), "filtered_tags_l10n_bundlenotfound");
            assertEquals(e.getValueTag(), "l10n:loc/bundlenotpresent-l10n:THE_KEY");
            assertEquals(e.getBundleName(), "loc/bundlenotpresent-l10n");
        }
    }

    @Test
    public void testFilteredTagsL10nTxtResourceBundleNotFound()
    throws Exception {
        try {
            TemplateFactory.TXT.get("filtered_tags_l10n_bundlenotfound");
            fail();
        } catch (ResourceBundleNotFoundException e) {
            assertEquals(e.getTemplateName(), "filtered_tags_l10n_bundlenotfound");
            assertEquals(e.getValueTag(), "l10n:loc/bundlenotpresent-l10n:THE_KEY");
            assertEquals(e.getBundleName(), "loc/bundlenotpresent-l10n");
        }
    }

    @Test
    public void testFilteredTagsL10nHtmlDefaultResourceBundles()
    throws Exception {
        Template template;

        var bundles = new ArrayList<String>();
        bundles.add("localization/filtered-tags-l10n");
        bundles.add("rife.template.TestResourceBundleClass");
        RifeConfig.template().setDefaultResourceBundles(TemplateFactory.HTML, bundles);

        try {
            RifeConfig.tools().setDefaultLanguage("en");
            template = TemplateFactory.HTML.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

            RifeConfig.tools().setDefaultLanguage("nl");
            template = TemplateFactory.HTML.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'De Nederlandse tekst'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'De Nederlandse tekst'.\n", template.getContent());

            RifeConfig.tools().setDefaultLanguage("fr");
            template = TemplateFactory.HTML.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'Le texte Francais'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'Le texte Francais'.\n", template.getContent());
        } finally {
            RifeConfig.template().setDefaultResourceBundles(TemplateFactory.HTML, null);
            RifeConfig.tools().setDefaultLanguage("en");
        }
    }

    @Test
    public void testFilteredTagsL10nTxtDefaultResourceBundles()
    throws Exception {
        Template template;

        var bundles = new ArrayList<String>();
        bundles.add("localization/filtered-tags-l10n");
        bundles.add("rife.template.TestResourceBundleClass");
        RifeConfig.template().setDefaultResourceBundles(TemplateFactory.TXT, bundles);

        try {
            RifeConfig.tools().setDefaultLanguage("en");
            template = TemplateFactory.TXT.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

            RifeConfig.tools().setDefaultLanguage("nl");
            template = TemplateFactory.TXT.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'De Nederlandse tekst'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'De Nederlandse tekst'.\n", template.getContent());

            RifeConfig.tools().setDefaultLanguage("fr");
            template = TemplateFactory.TXT.get("filtered_tags_l10n");
            assertEquals("This is the localized key 'Le texte Francais'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key 'list key class'.\nThis is a key with a bundle 'Le texte Francais'.\n", template.getContent());
        } finally {
            RifeConfig.template().setDefaultResourceBundles(TemplateFactory.TXT, null);
            RifeConfig.tools().setDefaultLanguage("en");
        }
    }

    @Test
    public void testFilteredTagsL10nHtmlSeveralResourceBundles()
    throws Exception {
        var template = TemplateFactory.HTML.get("filtered_tags_l10n");
        assertEquals("This is the localized key 'default value'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_KEY", "list key"}
                };
            }
        });
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'list key'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_KEY", "list key"}
                };
            }
        });
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_OTHER_KEY", "list key"}
                };
            }
        });
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsL10nTxtSeveralResourceBundles()
    throws Exception {
        var template = TemplateFactory.TXT.get("filtered_tags_l10n");
        assertEquals("This is the localized key 'default value'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_KEY", "list key"}
                };
            }
        });
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'list key'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_KEY", "list key"}
                };
            }
        });
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());

        template.clear();
        template.addResourceBundle(new ListResourceBundle() {
            public Object[][] getContents() {
                return new Object[][]{
                    {"THE_OTHER_KEY", "list key"}
                };
            }
        });
        template.addResourceBundle(Localization.getResourceBundle("localization/filtered-tags-l10n", "en"));
        assertEquals("This is the localized key 'The English text'.\nThis is an unknown key '{{v l10n:UNKNOWN_KEY/}}'.\nThis is a class key '{{v l10n:THE_CLASS_KEY/}}'.\nThis is a key with a bundle 'The English text'.\n", template.getContent());
    }

    @Test
    public void testFilteredTagsLangHtml()
    throws Exception {
        var template = TemplateFactory.HTML.get("filtered_tags_lang");
        assertEquals("This expression is Dutch '<!--v lang:value1/-->'.\nThis expression is French or English 'yes yes'.\n\n\n\n", template.getContent());

        template = TemplateFactory.HTML.get("filtered_tags_lang");
        RifeConfig.tools().setDefaultLanguage("nl");
        assertEquals("This expression is Dutch 'ja ja'.\nThis expression is French or English '{{v lang:value2/}}'.\n\n\n\n", template.getContent());

        template = TemplateFactory.HTML.get("filtered_tags_lang");
        RifeConfig.tools().setDefaultLanguage("fr");
        assertEquals("This expression is Dutch '<!--v lang:value1/-->'.\nThis expression is French or English 'oui oui'.\n\n\n\n", template.getContent());

        RifeConfig.tools().setDefaultLanguage(null);

        template = TemplateFactory.HTML.get("filtered_tags_lang");
        template.setLanguage("en");
        assertEquals("This expression is Dutch '<!--v lang:value1/-->'.\nThis expression is French or English 'yes yes'.\n\n\n\n", template.getContent());

        template = TemplateFactory.HTML.get("filtered_tags_lang");
        template.setLanguage("nl");
        assertEquals("This expression is Dutch 'ja ja'.\nThis expression is French or English '{{v lang:value2/}}'.\n\n\n\n", template.getContent());

        template = TemplateFactory.HTML.get("filtered_tags_lang");
        template.setLanguage("fr");
        assertEquals("This expression is Dutch '<!--v lang:value1/-->'.\nThis expression is French or English 'oui oui'.\n\n\n\n", template.getContent());
    }

    @Test
    public void testFilteredTagsLangTxt()
    throws Exception {
        var template = TemplateFactory.TXT.get("filtered_tags_lang");
        assertEquals("This expression is Dutch '<!v lang:value1/>'.\nThis expression is French or English 'yes yes'.\n\n\n\n", template.getContent());

        template = TemplateFactory.TXT.get("filtered_tags_lang");
        RifeConfig.tools().setDefaultLanguage("nl");
        assertEquals("This expression is Dutch 'ja ja'.\nThis expression is French or English '{{v lang:value2/}}'.\n\n\n\n", template.getContent());

        template = TemplateFactory.TXT.get("filtered_tags_lang");
        RifeConfig.tools().setDefaultLanguage("fr");
        assertEquals("This expression is Dutch '<!v lang:value1/>'.\nThis expression is French or English 'oui oui'.\n\n\n\n", template.getContent());

        RifeConfig.tools().setDefaultLanguage(null);

        template = TemplateFactory.TXT.get("filtered_tags_lang");
        template.setLanguage("en");
        assertEquals("This expression is Dutch '<!v lang:value1/>'.\nThis expression is French or English 'yes yes'.\n\n\n\n", template.getContent());

        template = TemplateFactory.TXT.get("filtered_tags_lang");
        template.setLanguage("nl");
        assertEquals("This expression is Dutch 'ja ja'.\nThis expression is French or English '{{v lang:value2/}}'.\n\n\n\n", template.getContent());

        template = TemplateFactory.TXT.get("filtered_tags_lang");
        template.setLanguage("fr");
        assertEquals("This expression is Dutch '<!v lang:value1/>'.\nThis expression is French or English 'oui oui'.\n\n\n\n", template.getContent());
    }

    @Test
    public void testEncodingHtml() {
        Template template_iso8859_1;
        Template template_utf_8;
        try {
            template_iso8859_1 = TemplateFactory.HTML.get("encoding_latin1_iso88591", "ISO8859-1");
            assertNotNull(template_iso8859_1);
            template_utf_8 = TemplateFactory.HTML.get("encoding_latin1_utf8", "UTF-8");
            assertNotNull(template_utf_8);
            assertEquals(template_iso8859_1.getContent(), template_utf_8.getContent());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testEncodingTxt() {
        Template template_iso8859_1;
        Template template_utf_8;
        try {
            template_iso8859_1 = TemplateFactory.TXT.get("encoding_latin1_iso88591", "ISO8859-1");
            assertNotNull(template_iso8859_1);
            template_utf_8 = TemplateFactory.TXT.get("encoding_latin1_utf8", "UTF-8");
            assertNotNull(template_utf_8);
            assertEquals(template_iso8859_1.getContent(), template_utf_8.getContent());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testCachingHtml() {
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
    public void testCachingTxt() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.TXT.get("defaultvalues_in");
            assertNotNull(template1);
            template2 = TemplateFactory.TXT.get("defaultvalues_in");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testPackageFullNameHtml() {
        var template = TemplateFactory.HTML.get("test_package.noblocks_in");
        assertNotNull(template);
        assertEquals("test_package.noblocks_in", template.getFullName());
    }

    @Test
    public void testPackageFullNameTxt() {
        var template = TemplateFactory.TXT.get("test_package.noblocks_in");
        assertNotNull(template);
        assertEquals("test_package.noblocks_in", template.getFullName());
    }

    @Test
    public void testTemplatesInPackageCachingHtml() {
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
    public void testTemplatesInPackageCachingTxt() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.TXT.get("test_package.noblocks_in");
            assertNotNull(template1);
            template2 = TemplateFactory.TXT.get("test_package.noblocks_in");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testTemplatesInPackagePathHtml() {
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
    public void testTemplatesInPackagePathTxt() {
        Template template1;
        Template template2;
        try {
            template1 = TemplateFactory.TXT.get("test_package/noblocks_in.txt");
            assertNotNull(template1);
            template2 = TemplateFactory.TXT.get("test_package/noblocks_in.txt");
            assertNotNull(template2);
            assertNotSame(template1, template2);
            assertSame(template1.getClass(), template2.getClass());
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testReloadBasicHtml() {
        var resource_finder = TemplateFactory.HTML.getResourceFinder();

        try {
            // set up the temporary directory
            String template_dir = RifeConfig.global().getTempPath();
            File template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
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

    @Test
    public void testReloadBasicTxt() {
        var resource_finder = TemplateFactory.TXT.getResourceFinder();

        try {
            // set up the temporary directory
            String template_dir = RifeConfig.global().getTempPath();
            File template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
            ResourceFinderGroup group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.TXT.setResourceFinder(group);

            URL template1_resource = TemplateFactory.TXT.getParser().resolve("defaultvalues_in");
            String template1_name = "reload_basic";
            File template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.TXT.getParser().getExtension());
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
                template1 = TemplateFactory.TXT.get(template1_name);
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
            URL template2_resource = TemplateFactory.TXT.getParser().resolve("noblocks_in");
            try {
                FileUtils.copy(template2_resource.openStream(), template1_file);
            } catch (FileUtilsErrorException | IOException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
                return;
            }
            // obtain the modified template
            Template template2 = null;
            try {
                template2 = TemplateFactory.TXT.get(template1_name);
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
            TemplateFactory.TXT.setResourceFinder(resource_finder);
        }
    }

    @Test
    public void testReloadIncludesHtml() {
        var resource_finder = TemplateFactory.HTML.getResourceFinder();

        try {
            // set up the temporary directory
            var template_dir = RifeConfig.global().getTempPath();
            var template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
            var group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.HTML.setResourceFinder(group);

            // set up the first template file with its included file
            var template1_resource = TemplateFactory.HTML.getParser().resolve("includes_reload_master_in");
            var template1_name = "includes_reload_master";
            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.HTML.getParser().getExtension());
            template1_file.delete();
            var template1_included_resource = TemplateFactory.HTML.getParser().resolve("defaultvalues_in");
            var template1_included_name = "includes_reload_included_in";
            var template1_included_file = new File(template_dir + File.separator + template1_included_name + TemplateFactory.HTML.getParser().getExtension());
            template1_included_file.delete();
            try {
                FileUtils.copy(template1_resource.openStream(), template1_file);
                FileUtils.copy(template1_included_resource.openStream(), template1_included_file);
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

            // modify the contents of the included file
            var template1_included_resource2 = TemplateFactory.HTML.getParser().resolve("noblocks_in");
            try {
                FileUtils.copy(template1_included_resource2.openStream(), template1_included_file);
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

            // check if the template was correctly updated
            assertNotSame(template1, template2);
            assertNotSame(template1.getClass(), template2.getClass());
            assertNotEquals(template1.getContent(), template2.getContent());

            // clean up the copied files and the created dir
            template1_file.delete();
            template1_included_file.delete();

            // clean up the copied files
            template1_file.delete();
        } finally {
            TemplateFactory.HTML.setResourceFinder(resource_finder);
        }
    }

    // TODO
//    @Test
//    public void testReloadIncludesTxt() {
//        var resource_finder = TemplateFactory.TXT.getResourceFinder();
//
//        try {
//            // set up the temporary directory
//            var template_dir = RifeConfig.global().getTempPath();
//            var template_dir_file = new File(template_dir);
//            template_dir_file.mkdirs();
//
//            // set up the first template file
//            var group = new ResourceFinderGroup()
//                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
//                .add(ResourceFinderClasspath.instance());
//            TemplateFactory.TXT.setResourceFinder(group);
//
//            // set up the first template file with its included file
//            var template1_resource = TemplateFactory.TXT.getParser().resolve("includes_reload_master_in");
//            var template1_name = "includes_reload_master";
//            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.TXT.getParser().getExtension());
//            template1_file.delete();
//            var template1_included_resource = TemplateFactory.TXT.getParser().resolve("defaultvalues_in");
//            var template1_included_name = "includes_reload_included_in";
//            var template1_included_file = new File(template_dir + File.separator + template1_included_name + TemplateFactory.TXT.getParser().getExtension());
//            template1_included_file.delete();
//            try {
//                FileUtils.copy(template1_resource.openStream(), template1_file);
//                FileUtils.copy(template1_included_resource.openStream(), template1_included_file);
//            } catch (FileUtilsErrorException | IOException e) {
//                fail(ExceptionUtils.getExceptionStackTrace(e));
//                return;
//            }
//            // obtain the original template
//            Template template1 = null;
//            try {
//                template1 = TemplateFactory.TXT.get(template1_name);
//                assertNotNull(template1);
//            } catch (TemplateException e) {
//                fail(ExceptionUtils.getExceptionStackTrace(e));
//            }
//
//            // wait a second
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                fail(ExceptionUtils.getExceptionStackTrace(e));
//            }
//
//            // modify the contents of the included file
//            var template1_included_resource2 = TemplateFactory.TXT.getParser().resolve("noblocks_in");
//            try {
//                FileUtils.copy(template1_included_resource2.openStream(), template1_included_file);
//            } catch (FileUtilsErrorException | IOException e) {
//                fail(ExceptionUtils.getExceptionStackTrace(e));
//                return;
//            }
//            // obtain the modified template
//            Template template2 = null;
//            try {
//                template2 = TemplateFactory.TXT.get(template1_name);
//                assertNotNull(template2);
//            } catch (TemplateException e) {
//                fail(ExceptionUtils.getExceptionStackTrace(e));
//            }
//
//            // check if the template was correctly updated
//            assertNotSame(template1, template2);
//            assertNotSame(template1.getClass(), template2.getClass());
//            assertNotEquals(template1.getContent(), template2.getContent());
//
//            // clean up the copied files and the created dir
//            template1_file.delete();
//            template1_included_file.delete();
//
//            // clean up the copied files
//            template1_file.delete();
//        } finally {
//            TemplateFactory.TXT.setResourceFinder(resource_finder);
//        }
//    }

    @Test
    public void testReloadMultiLevelIncludes()
    throws IOException {
        var resource_finder = TemplateFactory.HTML.getResourceFinder();

        try {
            // set up the temporary directory
            var template_dir = RifeConfig.global().getTempPath();
            var template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
            var group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.HTML.setResourceFinder(group);

            // set up the first template file with its included file
            var template1_resource = TemplateFactory.HTML.getParser().resolve("includes_reload_multi_master_in");
            var template1_name = "includes_reload_multi_master";
            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.HTML.getParser().getExtension());
            template1_file.delete();

            var template1_included_resource = TemplateFactory.HTML.getParser().resolve("defaultvalues_in");
            var template1_included_name = "includes_reload_multi_included2_in";
            var template1_included_file = new File(template_dir + File.separator + template1_included_name + TemplateFactory.HTML.getParser().getExtension());
            template1_included_file.delete();
            try {
                FileUtils.copy(template1_resource.openStream(), template1_file);
                FileUtils.copy(template1_included_resource.openStream(), template1_included_file);
            } catch (FileUtilsErrorException e) {
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

            // modify the contents of the included file
            var template1_included_resource2 = TemplateFactory.HTML.getParser().resolve("noblocks_in");
            try {
                FileUtils.copy(template1_included_resource2.openStream(), template1_included_file);
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

            // check if the template was correctly updated
            assertNotSame(template1, template2);
            assertNotSame(template1.getClass(), template2.getClass());
            assertNotEquals(template1.getContent(), template2.getContent());

            // clean up the copied files and the created dir
            template1_file.delete();
            template1_included_file.delete();
        } finally {
            TemplateFactory.HTML.setResourceFinder(resource_finder);
        }
    }
}