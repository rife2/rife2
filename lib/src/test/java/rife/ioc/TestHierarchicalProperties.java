/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import org.junit.jupiter.api.Test;
import rife.ioc.exceptions.IncompatiblePropertyValueTypeException;
import rife.ioc.exceptions.PropertyValueException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestHierarchicalProperties {
    @Test
    void testInstantiation() {
        var properties = new HierarchicalProperties();
        assertNotNull(properties);
        assertEquals(0, properties.size());
        assertNotNull(properties.getNames());
        assertEquals(0, properties.getNames().size());
        assertNotNull(properties.getInjectableNames());
        assertEquals(0, properties.getInjectableNames().size());
        assertNotNull(properties.getLocalMap());
        assertEquals(0, properties.getLocalMap().size());
    }

    @Test
    void testSingleInstance() {
        Iterator<String> names_it;

        PropertyValue property1 = new PropertyValueObject("value1");
        PropertyValue property2 = new PropertyValueObject("value2");
        PropertyValue property3 = new PropertyValueObject("value3");
        PropertyValue property4 = new PropertyValueObject("value4");
        PropertyValue property5 = new PropertyValueObject("value5");

        var properties = new HierarchicalProperties();
        assertSame(properties, properties.put("name1", property1));
        assertSame(properties, properties.put("name2", property2));
        assertSame(properties, properties.put("non.identifier.name3", property3));
        assertEquals(3, properties.size());
        assertNotNull(properties.getLocalMap());
        assertEquals(3, properties.getLocalMap().size());

        assertTrue(properties.contains("name1"));
        assertTrue(properties.contains("name2"));
        assertTrue(properties.contains("non.identifier.name3"));

        assertSame(property1, properties.get("name1"));
        assertSame(property2, properties.get("name2"));
        assertSame(property3, properties.get("non.identifier.name3"));
        assertEquals("value1", properties.getValue("name1"));
        assertEquals("value2", properties.getValue("name2"));
        assertEquals("value3", properties.getValue("non.identifier.name3"));
        assertEquals("value1", properties.getValueString("name1"));
        assertEquals("value2", properties.getValueString("name2"));
        assertEquals("value3", properties.getValueString("non.identifier.name3"));

        names_it = properties.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());

        assertSame(property2, properties.remove("name2"));
        assertEquals(2, properties.size());

        assertTrue(properties.contains("name1"));
        assertFalse(properties.contains("name2"));
        assertTrue(properties.contains("non.identifier.name3"));

        names_it = properties.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertFalse(names_it.hasNext());

        var properties_alternative = new HierarchicalProperties();
        assertSame(properties_alternative, properties_alternative.put("name4", property4));
        assertSame(properties_alternative, properties_alternative.put("non.identifier.name5", property5));
        assertEquals(2, properties_alternative.size());

        assertSame(properties, properties.putAll(properties_alternative));

        assertEquals(4, properties.size());

        assertTrue(properties.contains("name1"));
        assertTrue(properties.contains("non.identifier.name3"));
        assertTrue(properties.contains("name4"));
        assertTrue(properties.contains("non.identifier.name5"));

        assertSame(property1, properties.get("name1"));
        assertSame(property3, properties.get("non.identifier.name3"));
        assertSame(property4, properties.get("name4"));
        assertSame(property5, properties.get("non.identifier.name5"));
        assertEquals("value1", properties.getValue("name1"));
        assertNull(properties.getValue("name2"));
        var default_value = 34;
        assertSame(default_value, properties.getValue("name2", default_value));
        assertEquals("value3", properties.getValue("non.identifier.name3"));
        assertEquals("value4", properties.getValue("name4"));
        assertEquals("value5", properties.getValue("non.identifier.name5"));
        assertEquals("value1", properties.getValueString("name1"));
        assertNull(properties.getValueString("name2"));
        assertEquals("somevalue", properties.getValueString("name2", "somevalue"));
        assertEquals("value3", properties.getValueString("non.identifier.name3"));
        assertEquals("value4", properties.getValueString("name4"));
        assertEquals("value5", properties.getValueString("non.identifier.name5"));

        names_it = properties.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name4", names_it.next());
        assertEquals("non.identifier.name5", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name4", names_it.next());
        assertFalse(names_it.hasNext());
    }

    @Test
    void testHierarchy() {
        /*
         *  This is the hierarchy that's being built.
         *
         *  	                          grandparent
         *  	                          -----------
         *  	                          name1 (property1d)
         *  	                          name2 (property2d)
         *  	                          name2_grandparent (property2d)
         *  	                          non.identifier.name3 (property3d)
         *  	                                 |
         *  	                   ______________|___________________________
         *  	                  /                                          \
         *  	                 /                                            \
         *  	            parent1                                           parent2
         *  	            -------                                           -------
         *  	            name1 (property1c)                                name2 (property2c)
         *  	            name2_parent (property2c)                         non.identifier.name3_parent (property3c)
         *  	                 |                                              |
         *  	      ___________|___________________                           |
         *  	     /                               \                          |
         *  	    /                                 \                         |
         *  	child1                                child2                  child3
         *  	------                                ------                  ------
         *  	name1 (property1)                     name1 (property1b)      non.identifier.name3 (property3b)
         *  	name2 (property2)                     name2 (property2b)
         *  	non.identifier.name3 (property3)
         */
        Iterator<String> names_it;

        PropertyValue property1 = new PropertyValueObject("value1");
        PropertyValue property2 = new PropertyValueObject("value2");
        PropertyValue property3 = new PropertyValueObject("value3");
        PropertyValue property1b = new PropertyValueObject("value1b");
        PropertyValue property2b = new PropertyValueObject("value2b");
        PropertyValue property3b = new PropertyValueObject("value3b");
        PropertyValue property1c = new PropertyValueObject("value1c");
        PropertyValue property2c = new PropertyValueObject("value2c");
        PropertyValue property3c = new PropertyValueObject("value3c");
        PropertyValue property1d = new PropertyValueObject("value1d");
        PropertyValue property2d = new PropertyValueObject("value2d");
        PropertyValue property3d = new PropertyValueObject("value3d");

        var properties_child1 = new HierarchicalProperties();
        assertSame(properties_child1, properties_child1.put("name1", property1));
        assertSame(properties_child1, properties_child1.put("name2", property2));
        assertSame(properties_child1, properties_child1.put("non.identifier.name3", property3));
        assertEquals(3, properties_child1.size());
        assertSame(property1, properties_child1.get("name1"));
        assertSame(property2, properties_child1.get("name2"));
        assertSame(property3, properties_child1.get("non.identifier.name3"));
        names_it = properties_child1.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child1.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());

        var properties_child2 = new HierarchicalProperties();
        assertSame(properties_child2, properties_child2.put("name1", property1b));
        assertSame(properties_child2, properties_child2.put("name2", property2b));
        assertEquals(2, properties_child2.size());
        assertSame(property1b, properties_child2.get("name1"));
        assertSame(property2b, properties_child2.get("name2"));
        names_it = properties_child2.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child2.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());

        var properties_child3 = new HierarchicalProperties();
        assertSame(properties_child3, properties_child3.put("non.identifier.name3", property3b));
        assertEquals(1, properties_child3.size());
        assertSame(property3b, properties_child3.get("non.identifier.name3"));
        names_it = properties_child3.getNames().iterator();
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child3.getInjectableNames().iterator();
        assertFalse(names_it.hasNext());

        var properties_parent1 = new HierarchicalProperties();
        assertSame(properties_parent1, properties_parent1.put("name1", property1c));
        assertSame(properties_parent1, properties_parent1.put("name2_parent", property2c));
        assertEquals(2, properties_parent1.size());
        assertSame(property1c, properties_parent1.get("name1"));
        assertSame(property2c, properties_parent1.get("name2_parent"));
        names_it = properties_parent1.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_parent1.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());

        var properties_parent2 = new HierarchicalProperties();
        assertSame(properties_parent2, properties_parent2.put("name2", property2c));
        assertSame(properties_parent2, properties_parent2.put("non.identifier.name3_parent", property3c));
        assertEquals(2, properties_parent2.size());
        assertSame(property2c, properties_parent2.get("name2"));
        assertSame(property3c, properties_parent2.get("non.identifier.name3_parent"));
        names_it = properties_parent2.getNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3_parent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_parent2.getInjectableNames().iterator();
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());

        var properties_grandparent = new HierarchicalProperties();
        assertSame(properties_grandparent, properties_grandparent.put("name1", property1d));
        assertSame(properties_grandparent, properties_grandparent.put("name2", property2d));
        assertSame(properties_grandparent, properties_grandparent.put("name2_grandparent", property2d));
        assertSame(properties_grandparent, properties_grandparent.put("non.identifier.name3", property3d));
        assertEquals(4, properties_grandparent.size());
        assertSame(property1d, properties_grandparent.get("name1"));
        assertSame(property2d, properties_grandparent.get("name2"));
        assertSame(property2d, properties_grandparent.get("name2_grandparent"));
        assertSame(property3d, properties_grandparent.get("non.identifier.name3"));
        names_it = properties_grandparent.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_grandparent.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        // set the first level parents
        properties_child1.parent(properties_parent1);
        assertSame(properties_parent1, properties_child1.getRoot());
        assertEquals(4, properties_child1.size());
        assertSame(property1, properties_child1.get("name1"));
        assertSame(property2, properties_child1.get("name2"));
        assertSame(property2c, properties_child1.get("name2_parent"));
        assertSame(property3, properties_child1.get("non.identifier.name3"));
        names_it = properties_child1.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child1.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());

        properties_child2.parent(properties_parent1);
        assertSame(properties_parent1, properties_child2.getRoot());
        assertEquals(3, properties_child2.size());
        assertSame(property1b, properties_child2.get("name1"));
        assertSame(property2b, properties_child2.get("name2"));
        names_it = properties_child2.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child2.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertFalse(names_it.hasNext());

        properties_child3.parent(properties_parent2);
        assertSame(properties_parent2, properties_child3.getRoot());
        assertEquals(3, properties_child3.size());
        assertSame(property3b, properties_child3.get("non.identifier.name3"));
        assertSame(property2c, properties_child3.get("name2"));
        assertSame(property3c, properties_child3.get("non.identifier.name3_parent"));
        names_it = properties_child3.getNames().iterator();
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3_parent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child3.getInjectableNames().iterator();
        assertEquals("name2", names_it.next());
        assertFalse(names_it.hasNext());

        // set the second level parents
        properties_parent1.parent(properties_grandparent);
        assertEquals(5, properties_child1.size());
        assertSame(property1, properties_child1.get("name1"));
        assertSame(property2, properties_child1.get("name2"));
        assertSame(property2c, properties_child1.get("name2_parent"));
        assertSame(property3, properties_child1.get("non.identifier.name3"));
        assertSame(property2d, properties_child1.get("name2_grandparent"));
        names_it = properties_child1.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child1.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_parent", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        properties_parent2.parent(properties_grandparent);
        assertEquals(5, properties_child3.size());
        assertSame(property3b, properties_child3.get("non.identifier.name3"));
        assertSame(property2c, properties_child3.get("name2"));
        assertSame(property3c, properties_child3.get("non.identifier.name3_parent"));
        assertSame(property1d, properties_child3.get("name1"));
        assertSame(property2d, properties_child3.get("name2_grandparent"));
        names_it = properties_child3.getNames().iterator();
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3_parent", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child3.getInjectableNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        assertSame(properties_grandparent, properties_child1.getRoot());
        assertSame(properties_grandparent, properties_child2.getRoot());
        assertSame(properties_grandparent, properties_child3.getRoot());

        // manipulate the hierarchy
        assertSame(property1, properties_child1.remove("name1"));
        assertNull(properties_child1.remove("name1"));
        assertSame(property1c, properties_child1.get("name1"));

        assertSame(property2c, properties_parent1.remove("name2_parent"));
        assertNull(properties_parent1.remove("name2_parent"));
        assertNull(properties_parent1.get("name2_parent"));

        assertSame(property1c, properties_parent1.remove("name1"));
        assertNull(properties_child1.remove("name1"));
        assertSame(property1d, properties_child1.get("name1"));
        assertSame(property1d, properties_parent1.get("name1"));

        assertSame(property3b, properties_child3.remove("non.identifier.name3"));
        assertNull(properties_child3.remove("non.identifier.name3"));
        assertSame(property3d, properties_child3.get("non.identifier.name3"));

        assertSame(property2c, properties_parent2.remove("name2"));
        assertNull(properties_parent2.remove("name2"));
        assertSame(property2d, properties_child3.get("name2"));

        assertSame(property2d, properties_grandparent.remove("name2"));
        assertNull(properties_grandparent.remove("name2"));
        assertNull(properties_child3.get("name2"));

        names_it = properties_child1.getNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child1.getInjectableNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties_child2.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child2.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties_child3.getNames().iterator();
        assertEquals("non.identifier.name3_parent", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child3.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        PropertyValue property2e = new PropertyValueObject("value1e");
        assertSame(properties_parent1, properties_parent1.put("new_name2", property2e));
        PropertyValue property3e = new PropertyValueObject("value3e");
        assertSame(properties_grandparent, properties_grandparent.put("new_non.identifier.name3", property3e));

        names_it = properties_child1.getNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("new_name2", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("new_non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child1.getInjectableNames().iterator();
        assertEquals("name2", names_it.next());
        assertEquals("new_name2", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties_child2.getNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("new_name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("new_non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child2.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2", names_it.next());
        assertEquals("new_name2", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());

        names_it = properties_child3.getNames().iterator();
        assertEquals("non.identifier.name3_parent", names_it.next());
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertEquals("non.identifier.name3", names_it.next());
        assertEquals("new_non.identifier.name3", names_it.next());
        assertFalse(names_it.hasNext());
        names_it = properties_child3.getInjectableNames().iterator();
        assertEquals("name1", names_it.next());
        assertEquals("name2_grandparent", names_it.next());
        assertFalse(names_it.hasNext());
    }

    @Test
    void testPutAll() {
        var properties1 = new HierarchicalProperties();
        assertEquals(0, properties1.size());
        var properties2 = new HierarchicalProperties();
        assertEquals(0, properties2.size());

        Object value1 = new StringBuffer("test");
        Object value2 = new Date();
        properties2.put("24", value1);
        properties2.put("test", value2);
        properties1.putAll(properties2);

        assertEquals(2, properties1.size());
        Iterator<String> names_it = properties1.getNames().iterator();
        assertEquals("24", names_it.next());
        assertEquals("test", names_it.next());
        assertSame(value1, properties1.get("24").getValue());
        assertSame(value2, properties1.get("test").getValue());
    }

    @Test
    void testPutAllMap() {
        var properties = new HierarchicalProperties();
        assertEquals(0, properties.size());

        Map<Object, Object> map = new LinkedHashMap<>();
        Object value1 = new StringBuffer("test");
        Object value2 = new Date();
        map.put(24, value1);
        map.put("test", value2);
        properties.putAll(map);

        assertEquals(2, properties.size());
        Iterator<String> names_it = properties.getNames().iterator();
        assertEquals("24", names_it.next());
        assertEquals("test", names_it.next());
        assertSame(value1, properties.get("24").getValue());
        assertSame(value2, properties.get("test").getValue());
    }

    @Test
    void testPutAllWithoutReplacing() {
        var properties1 = new HierarchicalProperties();
        assertEquals(0, properties1.size());
        var properties2 = new HierarchicalProperties();
        assertEquals(0, properties2.size());

        Object value1 = new StringBuffer("test");
        Object value2 = new Date();
        Object value3 = 38746387L;
        properties1.put("test", value2);
        properties2.put("24", value1);
        properties2.put("test", value3);
        properties1.putAllWithoutReplacing(properties2);

        assertEquals(2, properties1.size());
        Iterator<String> names_it = properties1.getNames().iterator();
        assertEquals("test", names_it.next());
        assertEquals("24", names_it.next());
        assertSame(value1, properties1.get("24").getValue());
        assertSame(value2, properties1.get("test").getValue());
    }

    @Test
    void testGetValueString() {
        var properties = new HierarchicalProperties();
        Object value1 = new StringBuffer("test");
        Object value2 = new BigDecimal("12682861E+10");
        properties.put("value1", value1);
        properties.put("value2", value2);
        properties.put("value3", null);
        properties.put("value4", "");

        assertEquals("test", properties.getValueString("value1"));
        assertEquals("1.2682861E+17", properties.getValueString("value2"));
        assertNull(properties.getValueString("value3"));
        assertNull(properties.getValueString("value4"));
        assertNull(properties.getValueString("inexistent"));

        assertEquals("test", properties.getValueString("value1", "default1"));
        assertEquals("1.2682861E+17", properties.getValueString("value2", "default2"));
        assertEquals("default3", properties.getValueString("value3", "default3"));
        assertEquals("default4", properties.getValueString("value4", "default4"));
        assertEquals("default5", properties.getValueString("inexistent", "default5"));
    }

    @Test
    void testGetValueTyped() {
        var properties = new HierarchicalProperties();
        Object value1 = new StringBuffer("test");
        Object value2 = new BigDecimal("12682861E+10");
        Object value4 = "";
        properties.put("value1", value1);
        properties.put("value2", value2);
        properties.put("value3", null);
        properties.put("value4", value4);

        assertSame(value1, properties.getValueTyped("value1", StringBuffer.class));
        assertSame(value2, properties.getValueTyped("value2", BigDecimal.class));
        assertNull(properties.getValueTyped("value3", String.class));
        assertSame(value4, properties.getValueTyped("value4", String.class));
        assertNull(properties.getValueTyped("inexistent", String.class));

        var default3 = new BigDecimal("5718620E+6");
        Integer default5 = 97586;
        assertSame(value1, properties.getValueTyped("value1", StringBuffer.class, new StringBuffer("default1")));
        assertSame(value2, properties.getValueTyped("value2", BigDecimal.class, new BigDecimal(1268)));
        assertSame(default3, properties.getValueTyped("value3", BigDecimal.class, default3));
        assertSame(value4, properties.getValueTyped("value4", String.class, "default4"));
        assertSame(default5, properties.getValueTyped("inexistent", Integer.class, default5));

        try {
            properties.getValueTyped("value2", Date.class);
            fail("Expected exception");
        } catch (PropertyValueException e) {
            assertTrue(e instanceof IncompatiblePropertyValueTypeException);
            assertEquals("value2", ((IncompatiblePropertyValueTypeException) e).getPropertyName());
            assertSame(Date.class, ((IncompatiblePropertyValueTypeException) e).getExpectedType());
            assertSame(BigDecimal.class, ((IncompatiblePropertyValueTypeException) e).getActualType());
        }
    }
}
