/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import org.junit.jupiter.api.Test;
import rife.ioc.exceptions.TemplateFactoryUnknownException;
import rife.template.Template;
import rife.template.exceptions.TemplateNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class TestPropertyValueTemplate {
    @Test
    void testInstantiation() {
        PropertyValueTemplate object = new PropertyValueTemplate("html", "values");
        assertNotNull(object);
        assertFalse(object.isStatic());
    }

    @Test
    void testGetValue() {
        PropertyValueTemplate object = new PropertyValueTemplate("html", "values");
        assertNotNull(object.getValue());
        assertTrue(object.getValue() instanceof Template);
    }

    @Test
    void testGetValueUnknownFactory() {
        PropertyValueTemplate object = new PropertyValueTemplate("blah", "values");
        try {
            object.getValue();
            fail("TemplateFactoryUnknownException wasn't thrown");
        } catch (TemplateFactoryUnknownException e) {
            assertEquals("blah", e.getType());
        }
    }

    @Test
    void testGetValueUnknownTemplate() {
        PropertyValueTemplate object = new PropertyValueTemplate("html", "blahblihbloh");
        try {
            object.getValue();
            fail("template 'blahblihbloh' shouldn't have been found");
        } catch (TemplateNotFoundException e) {
            assertEquals("blahblihbloh", e.getName());
        }
    }

    @Test
    void testGetValueString() {
        PropertyValueTemplate object = new PropertyValueTemplate("html", "values");
        assertEquals("{{v VALUE1/}}<!--v VALUE2/-->{{v VALUE3/}}\n", object.getValueString());
    }

    @Test
    void testToString() {
        PropertyValueTemplate object = new PropertyValueTemplate("html", "values");
        assertEquals("{{v VALUE1/}}<!--v VALUE2/-->{{v VALUE3/}}\n", object.toString());
    }

    @Test
    void testisNegligible() {
        assertFalse(new PropertyValueTemplate("html", "values").isNegligible());
    }
}
