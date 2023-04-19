/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import org.junit.jupiter.api.Test;
import rife.config.exceptions.ConfigErrorException;
import rife.ioc.HierarchicalProperties;
import rife.resources.ResourceFinderClasspath;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestXml2Config {
    @Test
    void testParse()
    throws ConfigErrorException {
        var properties = new HierarchicalProperties().put("config.property.test", "property_test_value");
        var config = Config.fromXmlResource("xml/test_xml2config.xml", ResourceFinderClasspath.instance(), properties);

        assertTrue(config.hasParameter("paramstring"));
        assertTrue(config.hasParameter("parambool"));
        assertTrue(config.hasParameter("paramchar"));
        assertTrue(config.hasParameter("paramint"));
        assertTrue(config.hasParameter("paramlong"));
        assertTrue(config.hasParameter("paramfloat"));
        assertTrue(config.hasParameter("paramdouble"));
        assertTrue(config.hasParameter("paramproperty"));
        assertTrue(config.hasParameter("paramfinal"));
        assertEquals(config.countParameters(), 9);

        assertEquals(config.getString("paramstring"), "astring");
        assertTrue(config.getBool("parambool"));
        assertEquals(config.getChar("paramchar"), 'C');
        assertEquals(config.getInt("paramint"), 5133);
        assertEquals(config.getLong("paramlong"), 8736478L);
        assertEquals(config.getFloat("paramfloat"), 545.2546f, 0);
        assertEquals(config.getDouble("paramdouble"), 7863.3434353d, 0);
        assertEquals(config.getString("paramproperty"), "begin:property_test_value:end");
        assertEquals(config.getString("paramfinal"), "initial value");

        assertFalse(config.isFinalParameter("paramstring"));
        assertFalse(config.isFinalParameter("parambool"));
        assertFalse(config.isFinalParameter("paramchar"));
        assertFalse(config.isFinalParameter("paramint"));
        assertFalse(config.isFinalParameter("paramlong"));
        assertFalse(config.isFinalParameter("paramfloat"));
        assertFalse(config.isFinalParameter("paramdouble"));
        assertFalse(config.isFinalParameter("paramproperty"));
        assertTrue(config.isFinalParameter("paramfinal"));

        assertTrue(config.hasList("list1"));
        assertTrue(config.hasList("list2"));
        assertTrue(config.hasList("listfinal"));
        assertEquals(config.countLists(), 3);

        assertFalse(config.isFinalList("list1"));
        assertFalse(config.isFinalList("list2"));
        assertTrue(config.isFinalList("listfinal"));

        Collection<String> items = null;
        Iterator<String> item_it = null;

        items = config.getStringItems("list1");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertFalse(item_it.hasNext());

        items = config.getStringItems("list2");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item4");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item5");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "start:property_test_value:finish");
        assertFalse(item_it.hasNext());

        items = config.getStringItems("listfinal");
        assertEquals(items.size(), 2);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item6");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item7");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testIncluding()
    throws ConfigErrorException {
        var properties = new HierarchicalProperties().put("config.property.test", "property_test_value");
        var config = Config.fromXmlResource("xml/test_xml2config_including.xml", ResourceFinderClasspath.instance(), properties);

        assertTrue(config.hasParameter("includingstring"));
        assertTrue(config.hasParameter("paramstring"));
        assertTrue(config.hasParameter("parambool"));
        assertTrue(config.hasParameter("paramchar"));
        assertTrue(config.hasParameter("paramint"));
        assertTrue(config.hasParameter("paramlong"));
        assertTrue(config.hasParameter("paramfloat"));
        assertTrue(config.hasParameter("paramdouble"));
        assertTrue(config.hasParameter("paramincluding"));
        assertTrue(config.hasParameter("paramproperty"));
        assertTrue(config.hasParameter("paramfinal"));
        assertEquals(config.countParameters(), 11);

        assertEquals(config.getString("includingstring"), "the including value goes to the included file : ");
        assertEquals(config.getString("paramstring"), "the including value goes to the included file : astring");
        assertTrue(config.getBool("parambool"));
        assertEquals(config.getChar("paramchar"), 'C');
        assertEquals(config.getInt("paramint"), 9999);
        assertEquals(config.getLong("paramlong"), 8736478L);
        assertEquals(config.getFloat("paramfloat"), 545.2546f, 0);
        assertEquals(config.getDouble("paramdouble"), 7863.3434353d, 0);
        assertEquals(config.getString("paramincluding"), "it's including : C");
        assertEquals(config.getString("paramproperty"), "begin:property_test_value:end");
        assertEquals(config.getString("paramfinal"), "initial value");

        assertFalse(config.isFinalParameter("paramstring"));
        assertFalse(config.isFinalParameter("parambool"));
        assertFalse(config.isFinalParameter("paramchar"));
        assertTrue(config.isFinalParameter("paramint"));
        assertFalse(config.isFinalParameter("paramlong"));
        assertFalse(config.isFinalParameter("paramfloat"));
        assertFalse(config.isFinalParameter("paramdouble"));
        assertFalse(config.isFinalParameter("paramproperty"));
        assertTrue(config.isFinalParameter("paramfinal"));

        assertTrue(config.hasList("list1"));
        assertTrue(config.hasList("list2"));
        assertTrue(config.hasList("listfinal"));
        assertEquals(config.countLists(), 3);

        Collection<String> items = null;
        Iterator<String> item_it = null;

        items = config.getStringItems("list1");
        assertEquals(items.size(), 1);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item10");
        assertFalse(item_it.hasNext());

        items = config.getStringItems("list2");
        assertEquals(items.size(), 4);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item13");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item14");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item15");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item16");
        assertFalse(item_it.hasNext());

        items = config.getStringItems("listfinal");
        assertEquals(items.size(), 2);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item6");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item7");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testSelectedShortClassname()
    throws ConfigErrorException {
        var config = Config.fromXmlResource("xml/test_xml2config_selector1.xml", ResourceFinderClasspath.instance());
        assertTrue(config.hasParameter("selectedparamstring"));
    }

    @Test
    void testSelectedFullClassname()
    throws ConfigErrorException {
        var config = Config.fromXmlResource("xml/test_xml2config_selector2.xml", ResourceFinderClasspath.instance());
        assertTrue(config.hasParameter("selectedparamstring"));
    }

    @Test
    void testUnavailableXmlFile() {
        Config config = null;

        try {
            config = Config.fromXmlResource("xml/this_file_is_not_there.xml", ResourceFinderClasspath.instance());
            fail();
            assertNotNull(config);
        } catch (IllegalArgumentException | ConfigErrorException e) {
            assertTrue(true);
        }
    }
}
