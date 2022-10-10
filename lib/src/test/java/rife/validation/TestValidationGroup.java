/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;
import rife.validation.exceptions.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidationGroup {
    @Test
    public void testInstantiation() {
        Validation validation = new Validation();
        ValidationGroup group1 = validation.addGroup("group1");
        assertNotNull(group1);
        ValidationGroup group2 = validation.addGroup("group2");
        assertNotNull(group2);
        ValidationGroup group3 = validation.addGroup("group1");
        assertNotNull(group3);

        assertNotSame(group1, validation.getGroup("group1"));
        assertSame(group3, validation.getGroup("group1"));
        assertSame(group2, validation.getGroup("group2"));
        assertSame(group3, validation.getGroup("group1"));
    }

    @Test
    public void testInitialstate() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");
        assertSame(validation, group.getValidation());
        assertEquals("mygroup", group.getName());
        assertEquals(0, group.getSubjects().size());
    }

    @Test
    public void testAddSubject() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");
        assertSame(group, group.addSubject("subject1"));
        assertSame(group, group.addSubject("subject2"));
        assertSame(group, group.addSubject("subject3"));
        assertSame(group, group.addSubject("subject1"));
        assertSame(group, group.addSubject("subject4"));

        List<String> subjects = group.getSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));
    }

    @Test
    public void testReinitializeProperties() {
        InitializedBeanImpl bean = new InitializedBeanImpl();
        bean.setString("one");
        bean.setInt(9);
        bean.setInteger(12);
        bean.setCharacter('Z');

        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");

        group.reinitializeProperties(bean);

        assertEquals("one", bean.getString());
        assertNull(bean.getStringbuffer());
        assertEquals(9, bean.getInt());
        assertEquals(12, bean.getInteger().intValue());
        assertEquals('i', bean.getChar());
        assertEquals('Z', bean.getCharacter().charValue());

        group
            .addConstraint(new ConstrainedProperty("string"))
            .addSubject("int")
            .addSubject("integer")
            .addSubject("char")
            .addSubject("character");

        group.reinitializeProperties(null);

        assertEquals("one", bean.getString());
        assertNull(bean.getStringbuffer());
        assertEquals(9, bean.getInt());
        assertEquals(12, bean.getInteger().intValue());
        assertEquals('i', bean.getChar());
        assertEquals('Z', bean.getCharacter().charValue());

        group.reinitializeProperties(bean);

        assertEquals("default", bean.getString());
        assertNull(bean.getStringbuffer());
        assertEquals(-1, bean.getInt());
        assertNull(bean.getInteger());
        assertEquals('i', bean.getChar());
        assertEquals('k', bean.getCharacter().charValue());
    }

    @Test
    public void testReinitializePropertiesError() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");
        group
            .addSubject("string");

        NonStaticBean bean = new NonStaticBean();
        try {
            group.reinitializeProperties(bean);
            fail();
        } catch (ValidationException e) {
            assertTrue(e.getCause() instanceof InstantiationException);
        }
    }

    class NonStaticBean {
    }

    @Test
    public void testAddRule() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");
        assertSame(group, group.addRule(new ValidationRuleNotNull("subject1")));
        assertSame(group, group.addRule(new ValidationRuleNotEmpty("subject2")));
        assertSame(group, group.addRule(new ValidationRuleUrl("subject3")));
        assertSame(group, group.addRule(new ValidationRuleEmail("subject1")));
        assertSame(group, group.addRule(new ValidationRuleNotEqual("subject4", "test")));

        List<String> subjects = group.getSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));

        subjects = validation.getValidatedSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));

        List<ValidationRule> rules = validation.getRules();
        assertEquals(5, rules.size());
        assertTrue(rules.get(0) instanceof ValidationRuleNotNull);
        assertEquals("subject1", rules.get(0).getSubject());
        assertTrue(rules.get(1) instanceof ValidationRuleNotEmpty);
        assertEquals("subject2", rules.get(1).getSubject());
        assertTrue(rules.get(2) instanceof ValidationRuleUrl);
        assertEquals("subject3", rules.get(2).getSubject());
        assertTrue(rules.get(3) instanceof ValidationRuleEmail);
        assertEquals("subject1", rules.get(3).getSubject());
        assertTrue(rules.get(4) instanceof ValidationRuleNotEqual);
        assertEquals("subject4", rules.get(4).getSubject());
    }

    @Test
    public void testAddConstraint() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");

        ConstrainedProperty property1 = new ConstrainedProperty("subject1").notNull(true).notEmpty(true);
        ConstrainedProperty property2 = new ConstrainedProperty("subject2").url(true);
        ConstrainedProperty property3 = new ConstrainedProperty("subject3").email(true);
        ConstrainedProperty property4 = new ConstrainedProperty("subject4").notEqual("test");
        ConstrainedProperty property5 = new ConstrainedProperty("subject5");

        assertSame(group, group.addConstraint(property1));
        assertSame(group, group.addConstraint(property2));
        assertSame(group, group.addConstraint(property3));
        assertSame(group, group.addConstraint(property4));
        assertSame(group, group.addConstraint(property5));

        List<String> subjects = group.getSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));

        subjects = validation.getValidatedSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));

        List<ValidationRule> rules = validation.getRules();
        assertEquals(5, rules.size());
        assertTrue(rules.get(0) instanceof ValidationRuleNotNull);
        assertEquals("subject1", rules.get(0).getSubject());
        assertTrue(rules.get(1) instanceof ValidationRuleNotEmpty);
        assertEquals("subject1", rules.get(1).getSubject());
        assertTrue(rules.get(2) instanceof ValidationRuleUrl);
        assertEquals("subject2", rules.get(2).getSubject());
        assertTrue(rules.get(3) instanceof ValidationRuleEmail);
        assertEquals("subject3", rules.get(3).getSubject());
        assertTrue(rules.get(4) instanceof ValidationRuleNotEqual);
        assertEquals("subject4", rules.get(4).getSubject());

        assertEquals(5, validation.getConstrainedProperties().size());
        assertSame(property1, validation.getConstrainedProperty("subject1"));
        assertSame(property2, validation.getConstrainedProperty("subject2"));
        assertSame(property3, validation.getConstrainedProperty("subject3"));
        assertSame(property4, validation.getConstrainedProperty("subject4"));
        assertSame(property5, validation.getConstrainedProperty("subject5"));
    }

    @Test
    public void testAddGroup() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("mygroup");
        ValidationGroup group2 = group.addGroup("group2");

        assertSame(group2, validation.getGroup("group2"));

        assertSame(group, group.addSubject("subject1"));
        assertSame(group, group.addSubject("subject2"));
        assertSame(group2, group2.addSubject("subject3"));
        assertSame(group2, group2.addSubject("subject1"));
        assertSame(group2, group2.addSubject("subject4"));

        List<String> subjects = null;
        subjects = group.getSubjects();
        assertEquals(4, subjects.size());
        assertEquals("subject1", subjects.get(0));
        assertEquals("subject2", subjects.get(1));
        assertEquals("subject3", subjects.get(2));
        assertEquals("subject4", subjects.get(3));

        subjects = group2.getSubjects();
        assertEquals(3, subjects.size());
        assertEquals("subject3", subjects.get(0));
        assertEquals("subject1", subjects.get(1));
        assertEquals("subject4", subjects.get(2));
    }

    @Test
    public void testAddGroupAddRule() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("group").addGroup("mygroup");
        assertSame(group, group.addRule(new ValidationRuleNotNull("subject1")));

        List<String> subjects = group.getSubjects();
        assertEquals(1, subjects.size());
        assertEquals("subject1", subjects.get(0));

        subjects = validation.getValidatedSubjects();
        assertEquals(1, subjects.size());
        assertEquals("subject1", subjects.get(0));

        List<ValidationRule> rules = validation.getRules();
        assertEquals(1, rules.size());
        assertTrue(rules.get(0) instanceof ValidationRuleNotNull);
        assertEquals("subject1", rules.get(0).getSubject());
    }

    @Test
    public void testAddGroupAddConstraint() {
        Validation validation = new Validation();
        ValidationGroup group = validation.addGroup("group").addGroup("mygroup");

        ConstrainedProperty property1 = new ConstrainedProperty("subject1").notNull(true).notEmpty(true);

        assertSame(group, group.addConstraint(property1));

        List<String> subjects = group.getSubjects();
        assertEquals(1, subjects.size());
        assertEquals("subject1", subjects.get(0));

        subjects = validation.getValidatedSubjects();
        assertEquals(1, subjects.size());
        assertEquals("subject1", subjects.get(0));

        List<ValidationRule> rules = validation.getRules();
        assertEquals(2, rules.size());
        assertTrue(rules.get(0) instanceof ValidationRuleNotNull);
        assertEquals("subject1", rules.get(0).getSubject());
        assertTrue(rules.get(1) instanceof ValidationRuleNotEmpty);
        assertEquals("subject1", rules.get(1).getSubject());

        assertEquals(1, validation.getConstrainedProperties().size());
        assertSame(property1, validation.getConstrainedProperty("subject1"));
    }

    @Test
    public void testClone() {
        Validation validation = new Validation();
        ValidationGroup group1 = validation.addGroup("mygroup");
        group1.addSubject("subject1");
        group1.addSubject("subject2");
        group1.addSubject("subject3");
        group1.addSubject("subject4");

        ValidationGroup group2 = (ValidationGroup) group1.clone();
        assertNotSame(group2, group1);
        assertSame(group2.getValidation(), group1.getValidation());
        assertEquals(group2.getName(), group1.getName());

        List<String> subjects1 = group1.getSubjects();
        List<String> subjects2 = group2.getSubjects();
        assertEquals(subjects2.size(), subjects1.size());
        assertEquals(subjects2.get(0), subjects1.get(0));
        assertEquals(subjects2.get(1), subjects1.get(1));
        assertEquals(subjects2.get(2), subjects1.get(2));
        assertEquals(subjects2.get(3), subjects1.get(3));
    }
}
