/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.resources.*;
import rife.resources.exceptions.ResourceWriterErrorException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestTemplateFactory {
    @Test
    void testUniqueFactoryHtml() {
        assertSame(TemplateFactory.HTML, TemplateFactory.HTML);
    }

    @Test
    void testUniqueFactoryTxt() {
        assertSame(TemplateFactory.TXT, TemplateFactory.TXT);
    }

    @Test
    void testUniqueParserHtml() {
        assertSame(TemplateFactory.HTML.getParser(), TemplateFactory.HTML.getParser());
    }

    @Test
    void testUniqueParserTxt() {
        assertSame(TemplateFactory.TXT.getParser(), TemplateFactory.TXT.getParser());
    }

    @Test
    void testDefaultContentTypeHtml() {
        assertEquals(TemplateFactory.HTML.get("testhtml_in").getDefaultContentType(), "text/html");
    }

    @Test
    void testDefaultContentTypeTxt() {
        assertEquals(TemplateFactory.TXT.get("testtext_in").getDefaultContentType(), "text/plain");
    }

    @Test
    void testTemplateHtml() {
        var template = TemplateFactory.HTML.get("templates.testhtml_in");
        assertEquals(template.getFactoryIdentifier(), TemplateFactory.HTML.getIdentifier());
        assertEquals(template.getFullName(), "templates.testhtml_in");
        assertNull(template.getEncoding());

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
    void testTemplateTxt() {
        var template = TemplateFactory.TXT.get("templates.testtext_in");
        assertEquals(template.getFactoryIdentifier(), TemplateFactory.TXT.getIdentifier());
        assertEquals(template.getFullName(), "templates.testtext_in");
        assertNull(template.getEncoding());

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
    void testTemplateHtmlCreateNewInstance() {
        var template = TemplateFactory.HTML.get("templates.testhtml_in");
        var instance = template.createNewInstance();

        assertEquals(template.getFactoryIdentifier(), instance.getFactoryIdentifier());
        assertEquals(template.getFullName(), instance.getFullName());
        assertEquals(template.getEncoding(), instance.getEncoding());
    }

    @Test
    void testTemplateTxtCreateNewInstance() {
        var template = TemplateFactory.TXT.get("templates.testtext_in");
        var instance = template.createNewInstance();

        assertEquals(template.getFactoryIdentifier(), instance.getFactoryIdentifier());
        assertEquals(template.getFullName(), instance.getFullName());
        assertEquals(template.getEncoding(), instance.getEncoding());
    }

    @Test
    void testTemplatePathHtml() {
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
    void testTemplatePathTxt() {
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
    void testTemplateAmbiguousHtml() {
        try {
            TemplateFactory.HTML.get("html.html");
            fail("exception not thrown");
        } catch (AmbiguousTemplateNameException e) {
            assertEquals(e.getName(), "html.html");
        }
    }

    @Test
    void testTemplateAmbiguousTxt() {
        try {
            TemplateFactory.TXT.get("txt.txt");
            fail("exception not thrown");
        } catch (AmbiguousTemplateNameException e) {
            assertEquals(e.getName(), "txt.txt");
        }
    }

    @Test
    void testTemplateInitializerHtml() {
        var factory = TemplateFactory.HTML;
        var template = factory.get("testhtml_in", null);
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
        template = factory.get("testhtml_in", null);
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
    void testTemplateInitializerTxt() {
        var factory = TemplateFactory.TXT;
        var template = factory.get("testtext_in", null);
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
        template = factory.get("testtext_in", null);
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
    void testFilteredTagsRenderHtml() {
        RendererImpl.sCount = 0;
        var t = TemplateFactory.HTML.get("filtered_tags_render");
        assertEquals("This is the render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
                     "This is another render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
                     "This is the render value with a differentiator 'RENDER:RIFE.TEMPLATE.RENDERERIMPL:DIFFERENT:different:2'.\n", t.getContent());
    }

    @Test
    void testFilteredTagsRenderTxt() {
        RendererImpl.sCount = 0;
        var t = TemplateFactory.TXT.get("filtered_tags_render");
        assertEquals("This is the render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
                     "This is another render value 'RENDER:RIFE.TEMPLATE.RENDERERIMPLnull:1'.\n" +
                     "This is the render value with a differentiator 'RENDER:RIFE.TEMPLATE.RENDERERIMPL:DIFFERENT:different:2'.\n", t.getContent());
    }

    @Test
    void testFilteredTagsL10nHtml() {
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
    void testFilteredTagsL10nTxt() {
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
    void testFilteredTagsL10nHtmlResourceBundleNotFound() {
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
    void testFilteredTagsL10nTxtResourceBundleNotFound() {
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
    void testFilteredTagsL10nHtmlDefaultResourceBundles() {
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
    void testFilteredTagsL10nTxtDefaultResourceBundles() {
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
    void testFilteredTagsL10nHtmlSeveralResourceBundles() {
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
    void testFilteredTagsL10nTxtSeveralResourceBundles() {
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
    void testFilteredTagsLangHtml() {
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
    void testFilteredTagsLangTxt() {
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
    void testEncodingHtml() {
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
    void testEncodingTxt() {
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
    void testCachingHtml() {
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
    void testCachingTxt() {
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
    void testPackageFullNameHtml() {
        var template = TemplateFactory.HTML.get("test_package.noblocks_in");
        assertNotNull(template);
        assertEquals("test_package.noblocks_in", template.getFullName());
    }

    @Test
    void testPackageFullNameTxt() {
        var template = TemplateFactory.TXT.get("test_package.noblocks_in");
        assertNotNull(template);
        assertEquals("test_package.noblocks_in", template.getFullName());
    }

    @Test
    void testTemplatesInPackageCachingHtml() {
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
    void testTemplatesInPackageCachingTxt() {
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
    void testTemplatesInPackagePathHtml() {
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
    void testTemplatesInPackagePathTxt() {
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
    void testReloadBasicHtml() {
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

            var template1_resource = TemplateFactory.HTML.getParser().resolve("defaultvalues_in");
            var template1_name = "reload_basic";
            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.HTML.getParser().getExtension());
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
            var template2_resource = TemplateFactory.HTML.getParser().resolve("noblocks_in");
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
    public void testFilteredBlocks() {
        var filter1 = "^FILTER1:(\\w+):CONST:(\\w+)$";
        var filter2 = "^FILTER2:(\\w+)$";
        var filter3 = "^CONST-FILTER3:(\\w+)$";
        var filter4 = "(\\w+)";

        TemplateFactory factory = null;
        Template template = null;

        try {
            factory = new TemplateFactory(TemplateConfig.XML, ResourceFinderClasspath.instance(), "html", "text/html", ".html", new String[]{filter1, filter2, filter3, filter4}, null, null, null, null);

            template = factory.get("blocks_filtered_in");

            assertTrue(template.hasFilteredBlocks(filter1));
            assertTrue(template.hasFilteredBlocks(filter2));
            assertTrue(template.hasFilteredBlocks(filter3));
            assertFalse(template.hasFilteredBlocks(filter4));

            List<String[]> filtered_blocks = null;

            filtered_blocks = template.getFilteredBlocks(filter1);
            assertEquals(3, filtered_blocks.size());

            var filter1_got_block1 = false;
            var filter1_got_block2 = false;
            var filter1_got_block3 = false;
            for (var block_groups : filtered_blocks) {
                assertEquals(3, block_groups.length);
                if (block_groups[0].equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[1].equals("BLOCK1a") &&
                    block_groups[2].equals("BLOCK1b")) {
                    filter1_got_block1 = true;
                } else if (block_groups[0].equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                           block_groups[1].equals("BLOCK2a") &&
                           block_groups[2].equals("BLOCK2b")) {
                    filter1_got_block2 = true;
                } else if (block_groups[0].equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                           block_groups[1].equals("BLOCK3a") &&
                           block_groups[2].equals("BLOCK3b")) {
                    filter1_got_block3 = true;
                }
            }
            assertTrue(filter1_got_block1 && filter1_got_block2 && filter1_got_block3);

            filtered_blocks = template.getFilteredBlocks(filter2);
            assertEquals(2, filtered_blocks.size());

            var filter2_got_block1 = false;
            var filter2_got_block2 = false;
            for (var block_groups : filtered_blocks) {
                assertEquals(2, block_groups.length);
                if (block_groups[0].equals("FILTER2:BLOCK1") &&
                    block_groups[1].equals("BLOCK1")) {
                    filter2_got_block1 = true;
                } else if (block_groups[0].equals("FILTER2:BLOCK2") &&
                           block_groups[1].equals("BLOCK2")) {
                    filter2_got_block2 = true;
                }
            }
            assertTrue(filter2_got_block1 && filter2_got_block2);

            filtered_blocks = template.getFilteredBlocks(filter3);
            assertEquals(2, filtered_blocks.size());

            var filter3_got_block1 = false;
            var filter3_got_block2 = false;
            for (var block_groups : filtered_blocks) {
                assertEquals(2, block_groups.length);
                if (block_groups[0].equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[1].equals("BLOCK1")) {
                    filter3_got_block1 = true;
                } else if (block_groups[0].equals("CONST-FILTER3:BLOCK2") &&
                           block_groups[1].equals("BLOCK2")) {
                    filter3_got_block2 = true;
                }
            }
            assertTrue(filter3_got_block1 && filter3_got_block2);

            factory = new TemplateFactory(TemplateConfig.XML, ResourceFinderClasspath.instance(), "html", "text/html", ".html", new String[]{filter4, filter1, filter2, filter3}, null, null, null, null);

            template = factory.get("blocks_filtered_in");

            assertFalse(template.hasFilteredBlocks(filter1));
            assertFalse(template.hasFilteredBlocks(filter2));
            assertFalse(template.hasFilteredBlocks(filter3));
            assertTrue(template.hasFilteredBlocks(filter4));

            filtered_blocks = template.getFilteredBlocks(filter4);
            assertEquals(7, filtered_blocks.size());

            var filter4_got_block1 = false;
            var filter4_got_block2 = false;
            var filter4_got_block3 = false;
            var filter4_got_block4 = false;
            var filter4_got_block5 = false;
            var filter4_got_block6 = false;
            var filter4_got_block7 = false;
            for (var block_groups : filtered_blocks) {
                if (block_groups[0].equals("FILTER1:BLOCK1a:CONST:BLOCK1b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK1a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK1b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block1 = true;
                    continue;
                }
                if (block_groups[0].equals("FILTER1:BLOCK2a:CONST:BLOCK2b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK2a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK2b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block2 = true;
                    continue;
                }
                if (block_groups[0].equals("FILTER1:BLOCK3a:CONST:BLOCK3b") &&
                    block_groups[1].equals("FILTER1") &&
                    block_groups[2].equals("BLOCK3a") &&
                    block_groups[3].equals("CONST") &&
                    block_groups[4].equals("BLOCK3b")) {
                    assertEquals(5, block_groups.length);
                    filter4_got_block3 = true;
                    continue;
                }
                if (block_groups[0].equals("FILTER2:BLOCK1") &&
                    block_groups[1].equals("FILTER2") &&
                    block_groups[2].equals("BLOCK1")) {
                    assertEquals(3, block_groups.length);
                    filter4_got_block4 = true;
                    continue;
                }
                if (block_groups[0].equals("FILTER2:BLOCK2") &&
                    block_groups[1].equals("FILTER2") &&
                    block_groups[2].equals("BLOCK2")) {
                    assertEquals(3, block_groups.length);
                    filter4_got_block5 = true;
                    continue;
                }
                if (block_groups[0].equals("CONST-FILTER3:BLOCK1") &&
                    block_groups[1].equals("CONST") &&
                    block_groups[2].equals("FILTER3") &&
                    block_groups[3].equals("BLOCK1")) {
                    assertEquals(4, block_groups.length);
                    filter4_got_block6 = true;
                    continue;
                }
                if (block_groups[0].equals("CONST-FILTER3:BLOCK2") &&
                    block_groups[1].equals("CONST") &&
                    block_groups[2].equals("FILTER3") &&
                    block_groups[3].equals("BLOCK2")) {
                    assertEquals(4, block_groups.length);
                    filter4_got_block7 = true;
                    continue;
                }
            }
            assertTrue(filter4_got_block1 && filter4_got_block2 && filter4_got_block3 &&
                       filter4_got_block4 && filter4_got_block5 && filter4_got_block6 &&
                       filter4_got_block7);
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    public void testFilteredValues() {
        var filter1 = "^FILTER1:(\\w+):CONST:(\\w+)$";
        var filter2 = "^FILTER2:(\\w+)$";
        var filter3 = "^CONST-FILTER3:(\\w+)$";
        var filter4 = "(\\w+)";

        TemplateFactory factory = null;
        Template template = null;

        try {
            factory = new TemplateFactory(TemplateConfig.XML, ResourceFinderClasspath.instance(), "html", "text/html", ".html", null, new String[]{filter1, filter2, filter3, filter4}, null, null, null);

            template = factory.get("values_filtered_in");

            assertTrue(template.hasFilteredValues(filter1));
            assertTrue(template.hasFilteredValues(filter2));
            assertTrue(template.hasFilteredValues(filter3));
            assertFalse(template.hasFilteredValues(filter4));

            List<String[]> filtered_values = null;

            filtered_values = template.getFilteredValues(filter1);
            assertEquals(3, filtered_values.size());

            var filter1_got_value1 = false;
            var filter1_got_value2 = false;
            var filter1_got_value3 = false;
            for (var value_groups : filtered_values) {
                assertEquals(3, value_groups.length);
                if (value_groups[0].equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[1].equals("VALUE1a") &&
                    value_groups[2].equals("VALUE1b")) {
                    filter1_got_value1 = true;
                } else if (value_groups[0].equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                           value_groups[1].equals("VALUE2a") &&
                           value_groups[2].equals("VALUE2b")) {
                    filter1_got_value2 = true;
                } else if (value_groups[0].equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                           value_groups[1].equals("VALUE3a") &&
                           value_groups[2].equals("VALUE3b")) {
                    filter1_got_value3 = true;
                }
            }
            assertTrue(filter1_got_value1 && filter1_got_value2 && filter1_got_value3);

            filtered_values = template.getFilteredValues(filter2);
            assertEquals(2, filtered_values.size());

            var filter2_got_value1 = false;
            var filter2_got_value2 = false;
            for (var value_groups : filtered_values) {
                assertEquals(2, value_groups.length);
                if (value_groups[0].equals("FILTER2:VALUE1") &&
                    value_groups[1].equals("VALUE1")) {
                    filter2_got_value1 = true;
                } else if (value_groups[0].equals("FILTER2:VALUE2") &&
                           value_groups[1].equals("VALUE2")) {
                    filter2_got_value2 = true;
                }
            }
            assertTrue(filter2_got_value1 && filter2_got_value2);

            filtered_values = template.getFilteredValues(filter3);
            assertEquals(2, filtered_values.size());

            var filter3_got_value1 = false;
            var filter3_got_value2 = false;
            for (var value_groups : filtered_values) {
                assertEquals(2, value_groups.length);
                if (value_groups[0].equals("CONST-FILTER3:VALUE1") &&
                    value_groups[1].equals("VALUE1")) {
                    filter3_got_value1 = true;
                } else if (value_groups[0].equals("CONST-FILTER3:VALUE2") &&
                           value_groups[1].equals("VALUE2")) {
                    filter3_got_value2 = true;
                }
            }
            assertTrue(filter3_got_value1 && filter3_got_value2);

            factory = new TemplateFactory(TemplateConfig.XML, ResourceFinderClasspath.instance(), "html", "text/html", ".html", null, new String[]{filter4, filter1, filter2, filter3}, null, null, null);

            template = factory.get("values_filtered_in");

            assertFalse(template.hasFilteredValues(filter1));
            assertFalse(template.hasFilteredValues(filter2));
            assertFalse(template.hasFilteredValues(filter3));
            assertTrue(template.hasFilteredValues(filter4));

            filtered_values = template.getFilteredValues(filter4);
            assertEquals(7, filtered_values.size());

            var filter4_got_value1 = false;
            var filter4_got_value2 = false;
            var filter4_got_value3 = false;
            var filter4_got_value4 = false;
            var filter4_got_value5 = false;
            var filter4_got_value6 = false;
            var filter4_got_value7 = false;
            for (var value_groups : filtered_values) {
                if (value_groups[0].equals("FILTER1:VALUE1a:CONST:VALUE1b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE1a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE1b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value1 = true;
                    continue;
                }
                if (value_groups[0].equals("FILTER1:VALUE2a:CONST:VALUE2b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE2a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE2b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value2 = true;
                    continue;
                }
                if (value_groups[0].equals("FILTER1:VALUE3a:CONST:VALUE3b") &&
                    value_groups[1].equals("FILTER1") &&
                    value_groups[2].equals("VALUE3a") &&
                    value_groups[3].equals("CONST") &&
                    value_groups[4].equals("VALUE3b")) {
                    assertEquals(5, value_groups.length);
                    filter4_got_value3 = true;
                    continue;
                }
                if (value_groups[0].equals("FILTER2:VALUE1") &&
                    value_groups[1].equals("FILTER2") &&
                    value_groups[2].equals("VALUE1")) {
                    assertEquals(3, value_groups.length);
                    filter4_got_value4 = true;
                    continue;
                }
                if (value_groups[0].equals("FILTER2:VALUE2") &&
                    value_groups[1].equals("FILTER2") &&
                    value_groups[2].equals("VALUE2")) {
                    assertEquals(3, value_groups.length);
                    filter4_got_value5 = true;
                    continue;
                }
                if (value_groups[0].equals("CONST-FILTER3:VALUE1") &&
                    value_groups[1].equals("CONST") &&
                    value_groups[2].equals("FILTER3") &&
                    value_groups[3].equals("VALUE1")) {
                    assertEquals(4, value_groups.length);
                    filter4_got_value6 = true;
                    continue;
                }
                if (value_groups[0].equals("CONST-FILTER3:VALUE2") &&
                    value_groups[1].equals("CONST") &&
                    value_groups[2].equals("FILTER3") &&
                    value_groups[3].equals("VALUE2")) {
                    assertEquals(4, value_groups.length);
                    filter4_got_value7 = true;
                    continue;
                }
            }
            assertTrue(filter4_got_value1 && filter4_got_value2 && filter4_got_value3 &&
                       filter4_got_value4 && filter4_got_value5 && filter4_got_value6 &&
                       filter4_got_value7);
        } catch (TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testReloadBasicTxt() {
        var resource_finder = TemplateFactory.TXT.getResourceFinder();

        try {
            // set up the temporary directory
            var template_dir = RifeConfig.global().getTempPath();
            var template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
            var group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.TXT.setResourceFinder(group);

            var template1_resource = TemplateFactory.TXT.getParser().resolve("defaultvalues_in");
            var template1_name = "reload_basic";
            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.TXT.getParser().getExtension());
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
            var template2_resource = TemplateFactory.TXT.getParser().resolve("noblocks_in");
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
    void testReloadIncludesHtml() {
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

    @Test
    void testReloadIncludesTxt() {
        var resource_finder = TemplateFactory.TXT.getResourceFinder();

        try {
            // set up the temporary directory
            var template_dir = RifeConfig.global().getTempPath();
            var template_dir_file = new File(template_dir);
            template_dir_file.mkdirs();

            // set up the first template file
            var group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{template_dir_file}))
                .add(ResourceFinderClasspath.instance());
            TemplateFactory.TXT.setResourceFinder(group);

            // set up the first template file with its included file
            var template1_resource = TemplateFactory.TXT.getParser().resolve("includes_reload_master_in");
            var template1_name = "includes_reload_master";
            var template1_file = new File(template_dir + File.separator + template1_name + TemplateFactory.TXT.getParser().getExtension());
            template1_file.delete();
            var template1_included_resource = TemplateFactory.TXT.getParser().resolve("defaultvalues_in");
            var template1_included_name = "includes_reload_included_in";
            var template1_included_file = new File(template_dir + File.separator + template1_included_name + TemplateFactory.TXT.getParser().getExtension());
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

            // modify the contents of the included file
            var template1_included_resource2 = TemplateFactory.TXT.getParser().resolve("noblocks_in");
            try {
                FileUtils.copy(template1_included_resource2.openStream(), template1_included_file);
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
            TemplateFactory.TXT.setResourceFinder(resource_finder);
        }
    }

    @Test
    void testReloadMultiLevelIncludes()
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

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testOtherResourceFinder(Datasource datasource) {
        var resources = DatabaseResourcesFactory.instance(datasource);
        try {
            resources.install();
            resources.addResource("db_template_name.txt", "{{b block1}}a block with value {{v value1/}}{{/b}}{{v value2/}}");

            var factory = new TemplateFactory(TemplateConfig.TXT, resources, "databasetext", "text/txt", ".txt", new String[]{TemplateFactoryFilters.TAG_PROPERTY, TemplateFactoryFilters.TAG_L10N}, null, BeanHandlerPlainSingleton.INSTANCE, null, null);
            Template template = null;

            template = factory.get("db_template_name");
            assertEquals("{{v value2/}}", template.getContent());
            template.setValue("value1", 1);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 2);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 3);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 4);
            template.appendBlock("value2", "block1");
            assertEquals("a block with value 1" +
                         "a block with value 2" +
                         "a block with value 3" +
                         "a block with value 4", template.getContent());

            // wait a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }

            resources.updateResource("db_template_name.txt", "{{b block1}}another block with value {{v value1/}}{{/b}}{{v value3/}}");

            template = factory.get("db_template_name");
            assertEquals("{{v value3/}}", template.getContent());
            template.setValue("value1", 1);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 2);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 3);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 4);
            template.appendBlock("value3", "block1");
            assertEquals("another block with value 1" +
                         "another block with value 2" +
                         "another block with value 3" +
                         "another block with value 4", template.getContent());
        } catch (ResourceWriterErrorException | TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                resources.remove();
            } catch (ResourceWriterErrorException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testOtherResourceFinderCommonFactory(Datasource datasource) throws Exception {
        var resources = DatabaseResourcesFactory.instance(datasource);
        var previous = TemplateFactory.HTML.getResourceFinder();
        TemplateFactory.HTML.setResourceFinder(resources);
        try {
            resources.install();
            resources.addResource("db_template_name.html", "<!--b block1-->a block with value <!--v value1/--><!--/b--><!--v value2/-->");

            Template template = null;

            template = TemplateFactory.HTML.get("db_template_name");
            assertEquals("<!--v value2/-->", template.getContent());
            template.setValue("value1", 1);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 2);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 3);
            template.appendBlock("value2", "block1");
            template.setValue("value1", 4);
            template.appendBlock("value2", "block1");
            assertEquals("a block with value 1" +
                         "a block with value 2" +
                         "a block with value 3" +
                         "a block with value 4", template.getContent());

            // wait a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }

            resources.updateResource("db_template_name.html", "<!--b block1-->another block with value <!--v value1/--><!--/b--><!--v value3/-->");

            template = TemplateFactory.HTML.get("db_template_name");
            assertEquals("<!--v value3/-->", template.getContent());
            template.setValue("value1", 1);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 2);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 3);
            template.appendBlock("value3", "block1");
            template.setValue("value1", 4);
            template.appendBlock("value3", "block1");
            assertEquals("another block with value 1" +
                         "another block with value 2" +
                         "another block with value 3" +
                         "another block with value 4", template.getContent());
        } catch (ResourceWriterErrorException | TemplateException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            TemplateFactory.HTML.setResourceFinder(previous);
            try {
                resources.remove();
            } catch (ResourceWriterErrorException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}