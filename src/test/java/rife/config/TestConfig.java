/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import org.junit.jupiter.api.Test;
import rife.config.exceptions.ConfigErrorException;
import rife.resources.ResourceFinderDirectories;
import rife.tools.SerializationUtils;
import rife.tools.exceptions.SerializationUtilsErrorException;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfig {
    @Test
    void testInstantiation() {
        var config = new Config();

        assertNotNull(config);
    }

    @Test
    void testValidParameters() {
        var config = new Config();

        assertEquals(config.countParameters(), 0);
        config
            .put("paramstring", "astring")
            .put("parambool1", "0")
            .put("parambool2", "1")
            .put("parambool3", "false")
            .put("parambool4", "true")
            .put("parambool5", "f")
            .put("parambool6", "t")
            .put("paramchar", "C")
            .put("paramint", "5133")
            .put("paramlong", "8736478")
            .put("paramfloat", "545.2546")
            .put("paramdouble", "7863.3434353");

        assertTrue(config.hasParameter("paramstring"));
        assertTrue(config.hasParameter("parambool1"));
        assertTrue(config.hasParameter("parambool2"));
        assertTrue(config.hasParameter("parambool3"));
        assertTrue(config.hasParameter("parambool4"));
        assertTrue(config.hasParameter("parambool5"));
        assertTrue(config.hasParameter("parambool6"));
        assertTrue(config.hasParameter("paramchar"));
        assertTrue(config.hasParameter("paramint"));
        assertTrue(config.hasParameter("paramlong"));
        assertTrue(config.hasParameter("paramfloat"));
        assertTrue(config.hasParameter("paramdouble"));
        assertEquals(config.countParameters(), 12);

        assertFalse(config.isFinalParameter("paramstring"));
        assertFalse(config.isFinalParameter("parambool1"));
        assertFalse(config.isFinalParameter("parambool2"));
        assertFalse(config.isFinalParameter("parambool3"));
        assertFalse(config.isFinalParameter("parambool4"));
        assertFalse(config.isFinalParameter("parambool5"));
        assertFalse(config.isFinalParameter("parambool6"));
        assertFalse(config.isFinalParameter("paramchar"));
        assertFalse(config.isFinalParameter("paramint"));
        assertFalse(config.isFinalParameter("paramlong"));
        assertFalse(config.isFinalParameter("paramfloat"));
        assertFalse(config.isFinalParameter("paramdouble"));

        assertEquals(config.getString("paramstring"), "astring");
        assertFalse(config.getBool("parambool1"));
        assertTrue(config.getBool("parambool2"));
        assertFalse(config.getBool("parambool3"));
        assertTrue(config.getBool("parambool4"));
        assertFalse(config.getBool("parambool5"));
        assertTrue(config.getBool("parambool6"));
        assertEquals(config.getChar("paramchar"), 'C');
        assertEquals(config.getInt("paramint"), 5133);
        assertEquals(config.getLong("paramlong"), 8736478L);
        assertEquals(config.getFloat("paramfloat"), 545.2546f, 0);
        assertEquals(config.getDouble("paramdouble"), 7863.3434353d, 0);

        config.remove("paramstring");
        assertFalse(config.hasParameter("paramstring"));
        assertNull(config.getString("paramstring"));
    }

    @Test
    void testInvalidParameters() {
        var config = new Config();

        assertEquals(config.countParameters(), 0);
        assertFalse(config.hasParameter("paramstring"));
        assertFalse(config.hasParameter("parambool"));
        assertFalse(config.hasParameter("paramchar"));
        assertFalse(config.hasParameter("paramint"));
        assertFalse(config.hasParameter("paramlong"));
        assertFalse(config.hasParameter("paramfloat"));
        assertFalse(config.hasParameter("paramdouble"));

        assertNull(config.getString("paramstring"));
        assertFalse(config.getBool("parambool"));
        assertEquals(config.getChar("paramchar"), 0);
        assertEquals(config.getInt("paramint"), 0);
        assertEquals(config.getLong("paramlong"), 0);
        assertEquals(config.getFloat("paramfloat"), 0, 0);
        assertEquals(config.getDouble("paramdouble"), 0, 0);
    }

    @Test
    void testDefaultParameters() {
        var config = new Config();

        config
            .put("paramstring", "astring")
            .put("parambool", true)
            .put("paramchar", 'C')
            .put("paramint", 5133)
            .put("paramlong", 8736478L)
            .put("paramfloat", 545.2546f)
            .put("paramdouble", 7863.3434353d);

        assertEquals(config.getString("paramstring", "defaultstring"), "astring");
        assertTrue(config.getBool("parambool", false));
        assertEquals(config.getChar("paramchar", 'H'), 'C');
        assertEquals(config.getInt("paramint", 834), 5133);
        assertEquals(config.getLong("paramlong", 349875L), 8736478L);
        assertEquals(config.getFloat("paramfloat", 354.9457f), 545.2546f, 0);
        assertEquals(config.getDouble("paramdouble", 9347.784578d), 7863.3434353d, 0);

        assertEquals(config.getString("paramstring2", "defaultstring"), "defaultstring");
        assertFalse(config.getBool("parambool2", false));
        assertEquals(config.getChar("paramchar2", 'H'), 'H');
        assertEquals(config.getInt("paramint2", 834), 834);
        assertEquals(config.getLong("paramlong2", 349875L), 349875L);
        assertEquals(config.getFloat("paramfloat2", 354.9457f), 354.9457f, 0);
        assertEquals(config.getDouble("paramdouble2", 9347.784578d), 9347.784578d, 0);
    }

    @Test
    void testFinalParameters() {
        var config = new Config();

        config.put("final", "first");
        assertFalse(config.isFinalParameter("final"));
        assertEquals(config.getString("final"), "first");

        config.finalParameter("final", true);
        assertTrue(config.isFinalParameter("final"));
        config.put("final", "second");
        assertEquals(config.getString("final"), "first");

        config.remove("final");
        assertTrue(config.isFinalParameter("final"));
        assertEquals(config.getString("final"), "first");

        config.finalParameter("final", false);
        assertFalse(config.isFinalParameter("final"));
        config.put("final", "second");
        assertEquals(config.getString("final"), "second");

        config.remove("final");
        assertNull(config.getString("final"));
    }

    @Test
    void testSerialization()
    throws ConfigErrorException {
        var config = new Config();

        var serializable1 = new SerializableClass(459, "thestring");
        var serializable2 = new SerializableClass(680824, "thesecondstring");
        config.put("paramserializable", serializable1);
        SerializableClass serializable3 = config.getSerializable("paramserializable");
        assertEquals(serializable1, serializable3);
        assertNotSame(serializable1, serializable3);

        config.put("paramstring", "astring");
        serializable3 = config.getSerializable("paramstring");
        assertNull(serializable3);
        serializable3 = config.getSerializable("paramstring", serializable2);
        assertEquals(serializable3, serializable2);
    }

    @Test
    void testLists() {
        var config = new Config();

        assertEquals(config.countLists(), 0);
        config
            .putItem("list1", "item1")
            .putItem("list1", "item2")
            .putItem("list1", "item3")
            .putItem("list2", "item4")
            .putItem("list2", "item5");

        assertTrue(config.hasList("list1"));
        assertTrue(config.hasList("list2"));
        assertEquals(config.countLists(), 2);

        assertFalse(config.isFinalList("list1"));
        assertFalse(config.isFinalList("list2"));

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

        config.putItem("list1", "item3");
        assertEquals(items.size(), 4);
        config.putItem("list1", "item6");
        assertEquals(items.size(), 5);

        items = config.getStringItems("list2");
        assertEquals(items.size(), 2);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item4");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item5");
        assertFalse(item_it.hasNext());

        config.clearList("list1");
        assertEquals(config.getStringItems("list1").size(), 0);

        config.removeList("list2");
        assertTrue(config.hasList("list1"));
        assertFalse(config.hasList("list2"));
        assertEquals(config.countLists(), 1);
    }

    @Test
    void testListBool() {
        var config = new Config();

        Collection<Boolean> booleans = null;
        config
            .putItem("booleanlist", true)
            .putItem("booleanlist", false)
            .putItem("booleanlist", true);
        booleans = config.getBoolItems("booleanlist");

        assertEquals(3, booleans.size());
        var booleans_it = booleans.iterator();
        assertTrue(booleans_it.hasNext());
        assertTrue(booleans_it.next());
        assertTrue(booleans_it.hasNext());
        assertFalse(booleans_it.next());
        assertTrue(booleans_it.hasNext());
        assertTrue(booleans_it.next());
        assertFalse(booleans_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", true)
            .putItem("stringlist", "athirdstring");
        booleans = config.getBoolItems("stringlist");
        assertEquals(4, booleans.size());
        booleans_it = booleans.iterator();
        assertTrue(booleans_it.hasNext());
        assertFalse(booleans_it.next());
        assertTrue(booleans_it.hasNext());
        assertFalse(booleans_it.next());
        assertTrue(booleans_it.hasNext());
        assertTrue(booleans_it.next());
        assertTrue(booleans_it.hasNext());
        assertFalse(booleans_it.next());
        assertFalse(booleans_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "true");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListChar() {
        var config = new Config();

        Collection<Character> chars = null;
        config
            .putItem("charlist", 'G')
            .putItem("charlist", 'i')
            .putItem("charlist", 'M');
        chars = config.getCharItems("charlist");

        assertEquals(3, chars.size());
        var chars_it = chars.iterator();
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'G');
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'i');
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'M');
        assertFalse(chars_it.hasNext());

        config
            .putItem("stringlist", "t")
            .putItem("stringlist", "a")
            .putItem("stringlist", 'O')
            .putItem("stringlist", "a");
        chars = config.getCharItems("stringlist");
        assertEquals(4, chars.size());
        chars_it = chars.iterator();
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 't');
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'a');
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'O');
        assertTrue(chars_it.hasNext());
        assertEquals(chars_it.next().charValue(), 'a');
        assertFalse(chars_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "t");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "a");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "O");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "a");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListInt() {
        var config = new Config();

        Collection<Integer> ints = null;
        config
            .putItem("intlist", 96285)
            .putItem("intlist", 1596)
            .putItem("intlist", 4174);
        ints = config.getIntItems("intlist");

        assertEquals(3, ints.size());
        var ints_it = ints.iterator();
        assertTrue(ints_it.hasNext());
        assertEquals(ints_it.next().intValue(), 96285);
        assertTrue(ints_it.hasNext());
        assertEquals(ints_it.next().intValue(), 1596);
        assertTrue(ints_it.hasNext());
        assertEquals(ints_it.next().intValue(), 4174);
        assertFalse(ints_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", 88646)
            .putItem("stringlist", "athirdstring");
        ints = config.getIntItems("stringlist");
        assertEquals(1, ints.size());
        ints_it = ints.iterator();
        assertTrue(ints_it.hasNext());
        assertEquals(ints_it.next().intValue(), 88646);
        assertFalse(ints_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "88646");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListLong() {
        var config = new Config();

        Collection<Long> longs = null;
        config
            .putItem("longlist", 69778249L)
            .putItem("longlist", 6687792094L)
            .putItem("longlist", 24425829L);
        longs = config.getLongItems("longlist");

        assertEquals(3, longs.size());
        var longs_it = longs.iterator();
        assertTrue(longs_it.hasNext());
        assertEquals(longs_it.next().longValue(), 69778249L);
        assertTrue(longs_it.hasNext());
        assertEquals(longs_it.next().longValue(), 6687792094L);
        assertTrue(longs_it.hasNext());
        assertEquals(longs_it.next().longValue(), 24425829L);
        assertFalse(longs_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", 7098634812L)
            .putItem("stringlist", "athirdstring");
        longs = config.getLongItems("stringlist");
        assertEquals(1, longs.size());
        longs_it = longs.iterator();
        assertTrue(longs_it.hasNext());
        assertEquals(longs_it.next().longValue(), 7098634812L);
        assertFalse(longs_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "7098634812");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListFloat() {
        var config = new Config();

        Collection<Float> floats = null;
        config
            .putItem("floatlist", 425.68f)
            .putItem("floatlist", 9682.54f)
            .putItem("floatlist", 134.98f);
        floats = config.getFloatItems("floatlist");

        assertEquals(3, floats.size());
        var floats_it = floats.iterator();
        assertTrue(floats_it.hasNext());
        assertEquals(floats_it.next(), 425.68f);
        assertTrue(floats_it.hasNext());
        assertEquals(floats_it.next(), 9682.54f);
        assertTrue(floats_it.hasNext());
        assertEquals(floats_it.next(), 134.98f);
        assertFalse(floats_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", 4512.78f)
            .putItem("stringlist", "athirdstring");
        floats = config.getFloatItems("stringlist");
        assertEquals(1, floats.size());
        floats_it = floats.iterator();
        assertTrue(floats_it.hasNext());
        assertEquals(floats_it.next(), 4512.78f);
        assertFalse(floats_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "4512.78");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListDouble() {
        var config = new Config();

        Collection<Double> doubles = null;
        config
            .putItem("doublelist", 69978.23524d)
            .putItem("doublelist", 413387.23451d)
            .putItem("doublelist", 441534.79798d);
        doubles = config.getDoubleItems("doublelist");

        assertEquals(3, doubles.size());
        var doubles_it = doubles.iterator();
        assertTrue(doubles_it.hasNext());
        assertEquals(doubles_it.next(), 69978.23524d);
        assertTrue(doubles_it.hasNext());
        assertEquals(doubles_it.next(), 413387.23451d);
        assertTrue(doubles_it.hasNext());
        assertEquals(doubles_it.next(), 441534.79798d);
        assertFalse(doubles_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", 551348.7986d)
            .putItem("stringlist", "athirdstring");
        doubles = config.getDoubleItems("stringlist");
        assertEquals(1, doubles.size());
        doubles_it = doubles.iterator();
        assertTrue(doubles_it.hasNext());
        assertEquals(doubles_it.next(), 551348.7986d);
        assertFalse(doubles_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "551348.7986");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testListSerialization()
    throws SerializationUtilsErrorException, ConfigErrorException {
        var config = new Config();

        var serializable1 = new SerializableClass(459, "thestring");
        var serializable2 = new SerializableClass(69823, "anotherstring");
        var serializable3 = new SerializableClass(499417, "athirdstring");
        config
            .putItem("serializablelist", serializable1)
            .putItem("serializablelist", serializable2)
            .putItem("serializablelist", serializable3);
        Collection<SerializableClass> serializables = config.getSerializableItems("serializablelist");

        assertEquals(3, serializables.size());
        var serializables_it = serializables.iterator();
        assertTrue(serializables_it.hasNext());
        assertEquals(serializables_it.next(), serializable1);
        assertTrue(serializables_it.hasNext());
        assertEquals(serializables_it.next(), serializable2);
        assertTrue(serializables_it.hasNext());
        assertEquals(serializables_it.next(), serializable3);
        assertFalse(serializables_it.hasNext());

        config
            .putItem("stringlist", "thestring")
            .putItem("stringlist", "anotherstring")
            .putItem("stringlist", serializable2)
            .putItem("stringlist", "athirdstring");
        serializables = config.getSerializableItems("stringlist");
        assertEquals(1, serializables.size());
        serializables_it = serializables.iterator();
        assertTrue(serializables_it.hasNext());
        assertEquals(serializables_it.next(), serializable2);
        assertFalse(serializables_it.hasNext());

        var items = config.getStringItems("stringlist");
        assertEquals(items.size(), 4);
        var item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "thestring");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "anotherstring");
        assertTrue(item_it.hasNext());
        var serialized = item_it.next();
        assertEquals(SerializationUtils.serializeToString(serializable2), serialized);
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "athirdstring");
        assertFalse(item_it.hasNext());
    }

    @Test
    void testFinalLists() {
        var config = new Config();

        Collection<String> items = null;
        Iterator<String> item_it = null;

        config
            .putItem("final", "item1")
            .putItem("final", "item2")
            .putItem("final", "item3");
        assertFalse(config.isFinalList("final"));

        items = config.getStringItems("final");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertFalse(item_it.hasNext());

        config.finalList("final", true);
        assertTrue(config.isFinalList("final"));
        config.putItem("final", "item4");
        items = config.getStringItems("final");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertFalse(item_it.hasNext());

        config.clearList("final");
        assertEquals(config.getStringItems("final").size(), 3);

        config.removeList("final");
        assertTrue(config.hasList("final"));
        assertEquals(config.getStringItems("final").size(), 3);

        config.finalList("final", false);
        assertFalse(config.isFinalList("final"));
        config.putItem("final", "item4");
        items = config.getStringItems("final");
        assertEquals(items.size(), 4);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item4");
        assertFalse(item_it.hasNext());

        config.clearList("final");
        assertEquals(config.getStringItems("final").size(), 0);

        config.removeList("final");
        assertFalse(config.hasList("final"));
    }

    @Test
    void testXmlOutput() throws SerializationUtilsErrorException, ConfigErrorException {
        var config = new Config();

        var serializable1 = new SerializableClass(69823, "anotherstring");
        var serializable2 = new SerializableClass(459, "thestring");
        var serializable3 = new SerializableClass(499417, "athirdstring");
        config
            .put("paramstring", "astring")
            .finalParameter("paramstring", true)
            .put("parambool", true)
            .put("paramchar", 'C')
            .put("paramint", 5133)
            .put("paramlong", 8736478L)
            .put("paramfloat", 545.2546f)
            .put("paramdouble", 7863.3434353d)
            .putItem("list1", "item1")
            .putItem("list1", "item2")
            .putItem("list1", "item3")
            .putItem("list2", "item4")
            .putItem("list2", "item5")
            .finalList("list2", true)
            .put("paramserializable", serializable1)
            .putItem("serializablelist", serializable2)
            .putItem("serializablelist", serializable3);

        var xml = config.toXml();
        assertEquals(xml, "<config>\n" +
            "\t<list name=\"list1\">\n" +
            "\t\t<item>item1</item>\n" +
            "\t\t<item>item2</item>\n" +
            "\t\t<item>item3</item>\n" +
            "\t</list>\n" +
            "\t<list name=\"list2\" final=\"true\">\n" +
            "\t\t<item>item4</item>\n" +
            "\t\t<item>item5</item>\n" +
            "\t</list>\n" +
            "\t<list name=\"serializablelist\">\n" +
            "\t\t<item>" + SerializationUtils.serializeToString(serializable2) + "</item>\n" +
            "\t\t<item>" + SerializationUtils.serializeToString(serializable3) + "</item>\n" +
            "\t</list>\n" +
            "\t<param name=\"parambool\">true</param>\n" +
            "\t<param name=\"paramchar\">C</param>\n" +
            "\t<param name=\"paramdouble\">7863.3434353</param>\n" +
            "\t<param name=\"paramfloat\">545.2546</param>\n" +
            "\t<param name=\"paramint\">5133</param>\n" +
            "\t<param name=\"paramlong\">8736478</param>\n" +
            "\t<param name=\"paramserializable\">" + SerializationUtils.serializeToString(serializable1) + "</param>\n" +
            "\t<param name=\"paramstring\" final=\"true\">astring</param>\n" +
            "</config>\n");
    }

    @Test
    void testStoreXml() throws ConfigErrorException {
        var config = new Config();

        config
            .put("paramstring", "astring")
            .finalParameter("paramstring", true)
            .put("parambool", true)
            .put("paramchar", 'C')
            .put("paramint", 5133)
            .put("paramlong", 8736478L)
            .put("paramfloat", 545.2546f)
            .put("paramdouble", 7863.3434353d)
            .putItem("list1", "item1")
            .putItem("list1", "item2")
            .putItem("list1", "item3")
            .putItem("list2", "item4")
            .putItem("list2", "item5")
            .finalList("list2", true);

        var xml_filename = "config_storexml_test.xml";
        var xml_dir = new File(RifeConfig.global().getTempPath());
        var xml_file = new File(xml_dir, xml_filename);
        try {
            config.storeToXml(xml_file);

            var config_stored = Config.fromXmlFile(xml_file);

            assertEquals(config_stored.countParameters(), 7);

            assertTrue(config_stored.hasParameter("paramstring"));
            assertTrue(config_stored.hasParameter("parambool"));
            assertTrue(config_stored.hasParameter("paramchar"));
            assertTrue(config_stored.hasParameter("paramint"));
            assertTrue(config_stored.hasParameter("paramlong"));
            assertTrue(config_stored.hasParameter("paramfloat"));
            assertTrue(config_stored.hasParameter("paramdouble"));
            assertEquals(config_stored.getString("paramstring"), "astring");
            assertTrue(config_stored.getBool("parambool"));
            assertEquals(config_stored.getChar("paramchar"), 'C');
            assertEquals(config_stored.getInt("paramint"), 5133);
            assertEquals(config_stored.getLong("paramlong"), 8736478L);
            assertEquals(config_stored.getFloat("paramfloat"), 545.2546f, 0);
            assertEquals(config_stored.getDouble("paramdouble"), 7863.3434353d, 0);
            assertTrue(config_stored.isFinalParameter("paramstring"));
            assertFalse(config_stored.isFinalParameter("parambool"));
            assertFalse(config_stored.isFinalParameter("paramchar"));
            assertFalse(config_stored.isFinalParameter("paramint"));
            assertFalse(config_stored.isFinalParameter("paramlong"));
            assertFalse(config_stored.isFinalParameter("paramfloat"));
            assertFalse(config_stored.isFinalParameter("paramdouble"));

            assertEquals(config_stored.countLists(), 2);

            assertTrue(config_stored.hasList("list1"));
            assertTrue(config_stored.hasList("list2"));
            assertFalse(config_stored.isFinalList("list1"));
            assertTrue(config_stored.isFinalList("list2"));

            Collection<String> items = null;
            Iterator<String> item_it = null;

            items = config_stored.getStringItems("list1");
            assertEquals(items.size(), 3);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item1");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item2");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item3");
            assertFalse(item_it.hasNext());

            items = config_stored.getStringItems("list2");
            assertEquals(items.size(), 2);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item4");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item5");
            assertFalse(item_it.hasNext());

            config_stored.put("paramstring2", "anotherstring");
            config_stored.putItem("list3", "item6");
            config_stored.putItem("list3", "item7");

            assertTrue(config_stored.hasList("list3"));

            config_stored.storeToXml();

            var config_stored2 = Config.fromXmlResource(xml_filename, new ResourceFinderDirectories(xml_dir));
            assertEquals(config_stored2.countParameters(), 8);
            assertEquals(config_stored2.getString("paramstring2"), "anotherstring");
            assertEquals(config_stored2.countLists(), 3);
            assertTrue(config_stored2.hasList("list1"));
            assertTrue(config_stored2.hasList("list2"));
            assertTrue(config_stored2.hasList("list3"));
            items = config_stored.getStringItems("list3");
            assertEquals(items.size(), 2);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item6");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item7");
            assertFalse(item_it.hasNext());
        } finally {
            xml_file.delete();
        }
    }

    @Test
    void testStorePreferences()
    throws ConfigErrorException, BackingStoreException {
        var preferences_path = "test_rifeconfig_preferences_integration";
        var preferences = Preferences.userRoot().node(preferences_path);

        try {
            var config = new Config();

            config
                .put("paramstring", "astring")
                .put("paramstring2", "astring2")
                .put("parambool", true)
                .put("paramchar", 'C')
                .put("paramint", 5133)
                .put("paramlong", 8736478L)
                .put("paramfloat", 545.2546f)
                .put("paramdouble", 7863.3434353d)
                .putItem("list1", "item1")
                .putItem("list1", "item2")
                .putItem("list1", "item3")
                .putItem("list2", "item4")
                .putItem("list2", "item5")
                .putItem("list3", "item6")
                .putItem("list3", "item7")
                .putItem("list3", "item8");

            config.storeToPreferences(preferences);

            var config_other = new Config();

            assertEquals(config_other.countParameters(), 0);

            config_other.put("paramstring", "afirststring");
            config_other.put("paramstring2", "afirststring2");
            config_other.finalParameter("paramstring2", true);
            config_other.put("paramstring3", "afirststring3");
            config_other.put("parambool", false);
            config_other.put("paramchar", 'D');
            config_other.put("paramint", 698);
            config_other.put("paramlong", 985835L);
            config_other.put("paramfloat", 978.14898f);
            config_other.put("paramdouble", 6098.1439724d);
            config_other.putItem("list1", "item1a");
            config_other.putItem("list1", "item2a");
            config_other.putItem("list2", "item3a");
            config_other.finalList("list2", true);

            assertEquals(config_other.countParameters(), 9);

            assertTrue(config_other.hasList("list1"));
            assertTrue(config_other.hasList("list2"));

            assertTrue(config_other.hasParameter("paramstring"));
            assertTrue(config_other.hasParameter("paramstring2"));
            assertTrue(config_other.hasParameter("paramstring3"));
            assertTrue(config_other.hasParameter("parambool"));
            assertTrue(config_other.hasParameter("paramchar"));
            assertTrue(config_other.hasParameter("paramint"));
            assertTrue(config_other.hasParameter("paramlong"));
            assertTrue(config_other.hasParameter("paramfloat"));
            assertTrue(config_other.hasParameter("paramdouble"));
            assertEquals(config_other.getString("paramstring"), "afirststring");
            assertEquals(config_other.getString("paramstring2"), "afirststring2");
            assertEquals(config_other.getString("paramstring3"), "afirststring3");
            assertFalse(config_other.getBool("parambool"));
            assertEquals(config_other.getChar("paramchar"), 'D');
            assertEquals(config_other.getInt("paramint"), 698);
            assertEquals(config_other.getLong("paramlong"), 985835L);
            assertEquals(config_other.getFloat("paramfloat"), 978.14898f, 0);
            assertEquals(config_other.getDouble("paramdouble"), 6098.1439724d, 0);

            assertEquals(config_other.countLists(), 2);

            Collection<String> items = null;
            Iterator<String> item_it = null;

            items = config_other.getStringItems("list1");
            assertEquals(items.size(), 2);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item1a");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item2a");
            assertFalse(item_it.hasNext());

            items = config_other.getStringItems("list2");
            assertEquals(items.size(), 1);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item3a");
            assertFalse(item_it.hasNext());

            config_other.preferencesNode(preferences);

            assertEquals(config_other.countParameters(), 10);

            assertTrue(config_other.hasParameter("paramstring"));
            assertTrue(config_other.hasParameter("paramstring2"));
            assertTrue(config_other.hasParameter("paramstring3"));
            assertTrue(config_other.hasParameter("parambool"));
            assertTrue(config_other.hasParameter("paramchar"));
            assertTrue(config_other.hasParameter("paramint"));
            assertTrue(config_other.hasParameter("paramlong"));
            assertTrue(config_other.hasParameter("paramfloat"));
            assertTrue(config_other.hasParameter("paramdouble"));
            assertEquals(config_other.getString("paramstring"), "astring");
            assertEquals(config_other.getString("paramstring2"), "afirststring2");
            assertEquals(config_other.getString("paramstring3"), "afirststring3");
            assertTrue(config_other.getBool("parambool"));
            assertEquals(config_other.getChar("paramchar"), 'C');
            assertEquals(config_other.getInt("paramint"), 5133);
            assertEquals(config_other.getLong("paramlong"), 8736478L);
            assertEquals(config_other.getFloat("paramfloat"), 545.2546f, 0);
            assertEquals(config_other.getDouble("paramdouble"), 7863.3434353d, 0);

            assertEquals(config_other.countLists(), 3);

            assertTrue(config_other.hasList("list1"));
            assertTrue(config_other.hasList("list2"));
            assertTrue(config_other.hasList("list3"));

            items = config_other.getStringItems("list1");
            assertEquals(items.size(), 3);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item1");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item2");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item3");
            assertFalse(item_it.hasNext());

            items = config_other.getStringItems("list2");
            assertEquals(items.size(), 1);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item3a");
            assertFalse(item_it.hasNext());

            items = config_other.getStringItems("list3");
            assertEquals(items.size(), 3);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item6");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item7");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item8");
            assertFalse(item_it.hasNext());

            config_other.remove("paramint");
            config_other.put("paramstring3", "anotherstring");
            config_other.putItem("list1", "item9");
            config_other.putItem("list4", "item10");
            config_other.putItem("list4", "item11");

            assertTrue(config_other.hasList("list4"));

            config_other.storeToPreferences();

            var config_other2 = new Config();
            config_other2.put("paramstring2", "oncemoreastring");
            config_other2.preferencesNode(preferences);

            assertEquals(config_other2.countParameters(), 9);
            assertFalse(config_other.hasParameter("paramint"));
            assertNull(config_other.getString("paramint"));
            assertTrue(config_other.hasParameter("paramstring2"));
            assertEquals(config_other2.getString("paramstring2"), "astring2");
            config_other2.finalParameter("paramstring2", true);
            assertEquals(config_other2.getString("paramstring2"), "oncemoreastring");
            assertTrue(config_other.hasParameter("paramstring3"));
            assertEquals(config_other2.getString("paramstring3"), "anotherstring");

            assertEquals(config_other2.countLists(), 4);
            assertTrue(config_other2.hasList("list1"));
            assertTrue(config_other2.hasList("list2"));
            assertTrue(config_other2.hasList("list3"));
            assertTrue(config_other2.hasList("list4"));

            items = config_other2.getStringItems("list1");
            assertEquals(items.size(), 4);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item1");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item2");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item3");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item9");
            assertFalse(item_it.hasNext());

            items = config_other2.getStringItems("list2");
            assertEquals(items.size(), 2);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item4");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item5");
            assertFalse(item_it.hasNext());

            items = config_other.getStringItems("list3");
            assertEquals(items.size(), 3);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item6");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item7");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item8");
            assertFalse(item_it.hasNext());

            items = config_other2.getStringItems("list4");
            assertEquals(items.size(), 2);
            item_it = items.iterator();
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item10");
            assertTrue(item_it.hasNext());
            assertEquals(item_it.next(), "item11");
            assertFalse(item_it.hasNext());

            var config_other3 = new Config();
            config_other3.preferencesNode(preferences);

            assertEquals(config_other3.countLists(), 4);
            assertTrue(config_other3.hasList("list1"));
            assertTrue(config_other3.hasList("list2"));
            assertTrue(config_other3.hasList("list3"));
            assertTrue(config_other3.hasList("list4"));

            items = config_other3.getStringItems("list1");
            assertEquals(items.size(), 4);
            items = config_other3.getStringItems("list2");
            assertEquals(items.size(), 2);
            items = config_other3.getStringItems("list3");
            assertEquals(items.size(), 3);
            items = config_other3.getStringItems("list4");
            assertEquals(items.size(), 2);

            config_other3.clearList("list2");
            config_other3.removeList("list3");

            assertEquals(config_other3.countLists(), 3);
            assertTrue(config_other3.hasList("list1"));
            assertTrue(config_other3.hasList("list2"));
            assertFalse(config_other3.hasList("list3"));
            assertTrue(config_other3.hasList("list4"));
            items = config_other3.getStringItems("list1");
            assertEquals(items.size(), 4);
            items = config_other3.getStringItems("list2");
            assertNull(items);
            items = config_other3.getStringItems("list4");
            assertEquals(items.size(), 2);

            var config_other4 = new Config();
            config_other4.preferencesNode(preferences);

            assertEquals(config_other4.countLists(), 3);
            assertTrue(config_other4.hasList("list1"));
            assertTrue(config_other4.hasList("list2"));
            assertFalse(config_other4.hasList("list3"));
            assertTrue(config_other4.hasList("list4"));

            items = config_other4.getStringItems("list1");
            assertEquals(items.size(), 4);
            items = config_other4.getStringItems("list2");
            assertNull(items);
            items = config_other4.getStringItems("list4");
            assertEquals(items.size(), 2);
        } finally {
            preferences.removeNode();
        }
    }

    @Test
    void testClone() {
        var config1 = new Config();

        config1
            .put("paramstring", "astring")
            .finalParameter("paramstring", true)
            .put("parambool", true)
            .put("paramchar", 'C')
            .put("paramint", 5133)
            .put("paramlong", 8736478L)
            .put("paramfloat", 545.2546f)
            .put("paramdouble", 7863.3434353d)
            .putItem("list1", "item1")
            .putItem("list1", "item2")
            .putItem("list1", "item3")
            .putItem("list2", "item4")
            .putItem("list2", "item5")
            .finalList("list2", true);

        var config2 = config1.clone();

        assertNotSame(config1, config2);

        assertEquals(config1.toXml(), config2.toXml());

        config2
            .finalParameter("paramstring", false)
            .put("paramstring", "astring2")
            .put("parambool", false)
            .finalParameter("parambool", true)
            .put("paramchar", 'D')
            .put("paramint", 442)
            .put("paramlong", 9121L)
            .put("paramfloat", 112.87f)
            .put("paramdouble", 1313.2232887d)
            .put("paramnew", 2223)
            .putItem("list1", "item6")
            .finalList("list1", true)
            .finalList("list2", false)
            .putItem("list2", "item7")
            .putItem("list3", "item8");

        assertNotEquals(config1.toXml(), config2.toXml());

        assertEquals(config1.countParameters(), 7);

        assertTrue(config1.hasParameter("paramstring"));
        assertTrue(config1.hasParameter("parambool"));
        assertTrue(config1.hasParameter("paramchar"));
        assertTrue(config1.hasParameter("paramint"));
        assertTrue(config1.hasParameter("paramlong"));
        assertTrue(config1.hasParameter("paramfloat"));
        assertTrue(config1.hasParameter("paramdouble"));
        assertEquals(config1.getString("paramstring"), "astring");
        assertTrue(config1.getBool("parambool"));
        assertEquals(config1.getChar("paramchar"), 'C');
        assertEquals(config1.getInt("paramint"), 5133);
        assertEquals(config1.getLong("paramlong"), 8736478L);
        assertEquals(config1.getFloat("paramfloat"), 545.2546f, 0);
        assertEquals(config1.getDouble("paramdouble"), 7863.3434353d, 0);
        assertTrue(config1.isFinalParameter("paramstring"));
        assertFalse(config1.isFinalParameter("parambool"));
        assertFalse(config1.isFinalParameter("paramchar"));
        assertFalse(config1.isFinalParameter("paramint"));
        assertFalse(config1.isFinalParameter("paramlong"));
        assertFalse(config1.isFinalParameter("paramfloat"));
        assertFalse(config1.isFinalParameter("paramdouble"));

        assertEquals(config1.countLists(), 2);

        assertTrue(config1.hasList("list1"));
        assertTrue(config1.hasList("list2"));
        assertFalse(config1.isFinalList("list1"));
        assertTrue(config1.isFinalList("list2"));

        Collection<String> items = null;
        Iterator<String> item_it = null;

        items = config1.getStringItems("list1");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertFalse(item_it.hasNext());

        items = config1.getStringItems("list2");
        assertEquals(items.size(), 2);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item4");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item5");
        assertFalse(item_it.hasNext());

        assertEquals(config2.countParameters(), 8);

        assertTrue(config2.hasParameter("paramstring"));
        assertTrue(config2.hasParameter("parambool"));
        assertTrue(config2.hasParameter("paramchar"));
        assertTrue(config2.hasParameter("paramint"));
        assertTrue(config2.hasParameter("paramlong"));
        assertTrue(config2.hasParameter("paramfloat"));
        assertTrue(config2.hasParameter("paramdouble"));
        assertEquals(config2.getString("paramstring"), "astring2");
        assertFalse(config2.getBool("parambool"));
        assertEquals(config2.getChar("paramchar"), 'D');
        assertEquals(config2.getInt("paramint"), 442);
        assertEquals(config2.getLong("paramlong"), 9121L);
        assertEquals(config2.getFloat("paramfloat"), 112.87f, 0);
        assertEquals(config2.getDouble("paramdouble"), 1313.2232887d, 0);
        assertEquals(config2.getInt("paramnew"), 2223);
        assertFalse(config2.isFinalParameter("paramstring"));
        assertTrue(config2.isFinalParameter("parambool"));
        assertFalse(config2.isFinalParameter("paramchar"));
        assertFalse(config2.isFinalParameter("paramint"));
        assertFalse(config2.isFinalParameter("paramlong"));
        assertFalse(config2.isFinalParameter("paramfloat"));
        assertFalse(config2.isFinalParameter("paramdouble"));
        assertFalse(config2.isFinalParameter("paramnew"));

        assertEquals(config2.countLists(), 3);

        assertTrue(config2.hasList("list1"));
        assertTrue(config2.hasList("list2"));
        assertTrue(config2.hasList("list3"));
        assertTrue(config2.isFinalList("list1"));
        assertFalse(config2.isFinalList("list2"));
        assertFalse(config2.isFinalList("list3"));

        items = config2.getStringItems("list1");
        assertEquals(items.size(), 4);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item1");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item2");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item3");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item6");
        assertFalse(item_it.hasNext());

        items = config2.getStringItems("list2");
        assertEquals(items.size(), 3);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item4");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item5");
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item7");
        assertFalse(item_it.hasNext());

        items = config2.getStringItems("list3");
        assertEquals(items.size(), 1);
        item_it = items.iterator();
        assertTrue(item_it.hasNext());
        assertEquals(item_it.next(), "item8");
        assertFalse(item_it.hasNext());
    }
}

class SerializableClass implements Serializable {
    private int number_ = -1;
    private String string_ = null;

    public SerializableClass(int number, String string) {
        number_ = number;
        string_ = string;
    }

    public void setNumber(int number) {
        number_ = number;
    }

    public int getNumber() {
        return number_;
    }

    public void setString(String string) {
        string_ = string;
    }

    public String getString() {
        return string_;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (null == other) {
            return false;
        }

        if (!(other instanceof SerializableClass)) {
            return false;
        }

        var other_datalink = (SerializableClass) other;
        if (!other_datalink.getString().equals(getString())) {
            return false;
        }
        if (other_datalink.getNumber() != getNumber()) {
            return false;
        }

        return true;
    }
}

