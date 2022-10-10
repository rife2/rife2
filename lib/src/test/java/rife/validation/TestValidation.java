/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;
import rife.tools.ExceptionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestValidation {
    @Test
    public void testInstantiation() {
        Bean bean = new Bean("value");
        assertNotNull(bean);
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        assertEquals(0, bean.getConstrainedProperties().size());
        assertEquals(0, bean.getRules().size());
        assertEquals(0, bean.getGroups().size());
        assertNull(bean.getConstrainedBean());
        assertTrue(bean.isSubjectValid("property"));
        assertTrue(bean.validate());
    }

    @Test
    public void testValidRule() {
        Bean bean = new Bean("value");
        bean.addRule(null);
        ValidationRule rule1 = new ValidationRuleNotEmpty("property").setBean(bean);
        ValidationRule rule2 = new ValidationRuleNotNull("property");
        bean.addRule(rule1);
        bean.addRule(rule2);

        List<ValidationRule> rules = bean.getRules();
        assertEquals(2, rules.size());
        assertSame(rule1, rules.get(0));
        assertSame(rule2, rules.get(1));

        assertTrue(bean.validate());
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        assertTrue(bean.isSubjectValid("property"));
    }

    @Test
    public void testInvalidRule() {
        Bean bean = new Bean(null);
        bean.addRule(null);
        ValidationRule rule1 = new ValidationRuleNotEmpty("property");
        ValidationRule rule2 = new ValidationRuleNotNull("property");
        bean.addRule(rule1);
        bean.addRule(rule2);
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        assertFalse(bean.isSubjectValid("property"));
        assertTrue(bean.isSubjectValid("property_unknown"));
        Iterator<ValidationError> it = bean.getValidationErrors().iterator();
        assertTrue(it.hasNext());
        ValidationError error = it.next();
        assertFalse(it.hasNext());
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("property", error.getSubject());
    }

    @Test
    public void testGetGroupIllegalArgument() {
        Bean bean = new Bean("value");
        assertNull(bean.getGroup(null));
        assertNull(bean.getGroup(""));
        assertNull(bean.getGroup("group2"));
    }

    @Test
    public void testAddGroup() {
        Bean bean = new Bean("value");
        ValidationGroup group1 = bean.addGroup("group1")
            .addRule(new ValidationRuleNotEmpty("property"))
            .addRule(new ValidationRuleNotNull("property"));
        ValidationGroup group2 = bean.addGroup("group2")
            .addRule(new ValidationRuleNotNull("theDate"));

        assertEquals(2, bean.getGroups().size());
        assertSame(group1, bean.getGroup("group1"));
        assertSame(group2, bean.getGroup("group2"));
    }

    @Test
    public void testAddValidationError() {
        Bean bean = new Bean(null);
        bean.addValidationError(null);
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        bean.addValidationError(new ValidationError.MANDATORY("subject"));
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
    }

    @Test
    public void testReplaceValidationError() {
        Bean bean = new Bean(null);

        Set<ValidationError> errors = new LinkedHashSet<ValidationError>();
        bean.replaceValidationErrors(errors);
        assertSame(errors, bean.getValidationErrors());
    }

    @Test
    public void testValidateWithoutReset() {
        Bean bean = new Bean("");
        ValidationRule rule = new ValidationRuleNotEmpty("property");
        bean.addRule(rule);
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        bean.setProperty("value");
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
    }

    @Test
    public void testResetValidation() {
        Bean bean = new Bean("");
        ValidationRule rule = new ValidationRuleNotEmpty("property");
        bean.addRule(rule);
        assertFalse(bean.validate());
        bean.resetValidation();
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        assertTrue(bean.isSubjectValid("property"));
    }

    @Test
    public void testFocusGroupIllegalArguments() {
        Bean bean = new Bean("");
        bean.focusGroup(null);
        assertEquals(0, bean.countValidationErrors());

        bean.addGroup("group1")
            .addRule(new ValidationRuleLimitedLength("property", 1, -1))
            .addRule(new ValidationRuleNotEmpty("property"));
        bean.validate();
        assertEquals(1, bean.countValidationErrors());
        bean.focusGroup("unknown");
        assertEquals(1, bean.countValidationErrors());
    }

    @Test
    public void testFocusGroup() {
        Bean bean = new Bean("");
        bean.addGroup("group1")
            .addRule(new ValidationRuleLimitedLength("property", 1, -1))
            .addRule(new ValidationRuleNotEmpty("property"));
        bean.addGroup("group2")
            .addRule(new ValidationRuleNotNull("theDate"));

        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));

        bean.focusGroup("group1");
        assertEquals(1, bean.countValidationErrors());
        Set<ValidationError> set = bean.getValidationErrors();
        assertFalse(bean.isSubjectValid("property"));
        assertTrue(bean.isSubjectValid("theDate"));
        Iterator<ValidationError> set_it = set.iterator();
        ValidationError error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_WRONGLENGTH, error.getIdentifier());
        assertEquals("property", error.getSubject());

        bean.resetValidation();

        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));

        bean.focusGroup("group2");
        assertEquals(1, bean.countValidationErrors());
        set = bean.getValidationErrors();
        assertTrue(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));
        set_it = set.iterator();
        error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("theDate", error.getSubject());
    }

    @Test
    public void testResetGroupIllegalArguments() {
        Bean bean = new Bean("");
        bean.resetGroup(null);
        assertEquals(0, bean.countValidationErrors());

        bean.addGroup("group1")
            .addRule(new ValidationRuleLimitedLength("property", 1, -1))
            .addRule(new ValidationRuleNotEmpty("property"));
        bean.validate();
        assertEquals(1, bean.countValidationErrors());
        bean.resetGroup("unknown");
        assertEquals(1, bean.countValidationErrors());
    }

    @Test
    public void testResetGroup() {
        Bean bean = new Bean("");
        bean.addGroup("group1")
            .addRule(new ValidationRuleLimitedLength("property", 1, -1))
            .addRule(new ValidationRuleNotEmpty("property"));
        bean.addGroup("group2")
            .addRule(new ValidationRuleNotNull("theDate"));

        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));

        bean.resetGroup("group1");
        assertEquals(1, bean.countValidationErrors());
        Set<ValidationError> set = bean.getValidationErrors();
        assertTrue(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));
        Iterator<ValidationError> set_it = set.iterator();
        ValidationError error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("theDate", error.getSubject());

        bean.resetValidation();

        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));

        bean.resetGroup("group2");
        assertEquals(1, bean.countValidationErrors());
        set = bean.getValidationErrors();
        assertFalse(bean.isSubjectValid("property"));
        assertTrue(bean.isSubjectValid("theDate"));
        set_it = set.iterator();
        error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_WRONGLENGTH, error.getIdentifier());
        assertEquals("property", error.getSubject());
    }

    @Test
    public void testValidateGroupIllegalArguments() {
        Bean bean = new Bean("value");
        bean.addGroup("group1")
            .addRule(new ValidationRuleNotEmpty("property"))
            .addRule(new ValidationRuleNotNull("property"));

        assertTrue(bean.validateGroup(null));
        assertTrue(bean.validateGroup("unknown"));
    }

    @Test
    public void testValidateGroup() {
        Bean bean = new Bean("");
        bean.setProperty("12");
        bean.addGroup("group1")
            .addRule(new ValidationRuleLimitedLength("property", 3, -1));
        bean.addGroup("group2")
            .addRule(new ValidationRuleNotNull("theDate"));

        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));

        bean.resetValidation();

        assertFalse(bean.validateGroup("group1"));
        assertEquals(1, bean.countValidationErrors());
        assertFalse(bean.isSubjectValid("property"));
        assertTrue(bean.isSubjectValid("theDate"));
        Set<ValidationError> set = bean.getValidationErrors();
        Iterator<ValidationError> set_it = set.iterator();
        ValidationError error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_WRONGLENGTH, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertFalse(set_it.hasNext());

        bean.getGroup("group1")
            .addRule(new ValidationRuleNotEqual("property", "12"));
        assertFalse(bean.validateGroup("group1"));
        assertEquals(2, bean.countValidationErrors());
        set = bean.getValidationErrors();
        set_it = set.iterator();
        error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_WRONGLENGTH, error.getIdentifier());
        assertEquals("property", error.getSubject());
        error = set_it.next();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertFalse(set_it.hasNext());

        bean.resetValidation();

        assertFalse(bean.validateGroup("group2"));
        assertEquals(1, bean.countValidationErrors());
        set = bean.getValidationErrors();
        set_it = set.iterator();
        error = set_it.next();
        assertTrue(bean.isSubjectValid("property"));
        assertFalse(bean.isSubjectValid("theDate"));
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, error.getIdentifier());
        assertEquals("theDate", error.getSubject());

        assertFalse(bean.validateGroup("group2"));
        assertEquals(1, bean.countValidationErrors());
    }

    @Test
    public void testUniqueSubjectErrors() {
        Bean bean = new Bean("");
        bean.setProperty("://wrong");
        bean.addRule(new ValidationRuleEmail("property"));
        bean.addRule(new ValidationRuleUrl("property"));
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
    }

    @Test
    public void testLimitSubjectErrors() {
        Bean bean = new Bean("");
        bean.setProperty("://wrong");
        bean.limitSubjectErrors(null);
        bean.addRule(new ValidationRuleEmail("property"));
        bean.addRule(new ValidationRuleLimitedLength("property", 2, 4));
        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertEquals(2, bean.getValidationErrors().size());
        bean.limitSubjectErrors("property");
        bean.limitSubjectErrors("property");
        assertEquals(2, bean.countValidationErrors());
        assertEquals(2, bean.getValidationErrors().size());
        bean.resetValidation();
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        bean.unlimitSubjectErrors("property");
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
    }

    @Test
    public void testUnlimitSubjectErrors() {
        Bean bean = new Bean("");
        bean.setProperty("://wrong");
        bean.unlimitSubjectErrors(null);
        bean.unlimitSubjectErrors("property");
        bean.limitSubjectErrors("property");
        bean.addRule(new ValidationRuleEmail("property"));
        bean.addRule(new ValidationRuleLimitedLength("property", 2, 4));
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        bean.unlimitSubjectErrors("property");
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        bean.resetValidation();
        assertFalse(bean.validate());
        assertEquals(2, bean.countValidationErrors());
        assertEquals(2, bean.getValidationErrors().size());
    }

    @Test
    public void testMakeSubjectValid() {
        Bean bean = new Bean("");
        bean.makeSubjectValid(null);
        assertTrue(bean.isSubjectValid(null));
        bean.makeSubjectValid("blurp");
        assertTrue(bean.isSubjectValid("blurp"));
        ValidationRule rule = new ValidationRuleNotEmpty("property");
        bean.addRule(rule);
        assertFalse(bean.validate());
        bean.makeSubjectValid("property_blah");
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        assertFalse(bean.isSubjectValid("property"));
        bean.makeSubjectValid("property");
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        assertTrue(bean.isSubjectValid("property"));
    }

    @Test
    public void testMakeErrorValid() {
        Bean bean = new Bean("");
        bean.makeErrorValid(null, null);
        assertTrue(bean.isSubjectValid(null));
        bean.makeErrorValid(null, "blurp");
        assertTrue(bean.isSubjectValid("blurp"));
        bean.makeErrorValid("INVALID", "blurp");
        assertTrue(bean.isSubjectValid("blurp"));
        ValidationRule rule1 = new ValidationRuleNotEqual("property", "");
        ValidationRule rule2 = new ValidationRuleLimitedLength("property", 2, 4);
        bean.addRule(rule1);
        bean.addRule(rule2);
        assertFalse(bean.validate());

        bean.makeErrorValid("INVALID", "property_blah");
        assertEquals(2, bean.countValidationErrors());
        assertEquals(2, bean.getValidationErrors().size());
        assertFalse(bean.isSubjectValid("property"));

        bean.makeErrorValid(null, "property");
        assertEquals(2, bean.countValidationErrors());
        assertEquals(2, bean.getValidationErrors().size());
        assertFalse(bean.isSubjectValid("property"));

        bean.makeErrorValid("INVALID", "property");
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        assertFalse(bean.isSubjectValid("property"));

        ValidationError error = (ValidationError) bean.getValidationErrors().iterator().next();
        assertEquals("property", error.getSubject());
        assertEquals("WRONGLENGTH", error.getIdentifier());

        bean.makeErrorValid("WRONGLENGTH", "property");
        assertEquals(0, bean.countValidationErrors());
        assertEquals(0, bean.getValidationErrors().size());
        assertTrue(bean.isSubjectValid("property"));
    }

    @Test
    public void testGetErrorIndication() {
        Bean bean = new Bean("test");
        ValidationRule rule = new ValidationRuleNotEmpty("property");
        bean.addRule(rule);
        assertTrue(bean.validate());
        assertEquals("valid", Validation.getErrorIndication(bean, "property", "valid", "error"));
        bean.setProperty("");
        assertFalse(bean.validate());
        assertEquals("error", Validation.getErrorIndication(bean, "property", "valid", "error"));
    }

    @Test
    public void testConstrainedBean() {
        Bean bean = null;

        ConstrainedBean constraint = new ConstrainedBean().unique("property", "other");

        bean = new Bean("value");
        bean.addConstraint(constraint);
        assertSame(constraint, bean.getConstrainedBean());
    }

    @Test
    public void testSeveralConstrainedBean() {
        Bean bean = null;

        ConstrainedBean constraint1 = new ConstrainedBean().unique("property", "other");
        ConstrainedBean constraint2 = new ConstrainedBean().unique("property3", "other2", "other3").defaultOrder("ordered");

        bean = new Bean("value");
        bean.addConstraint(constraint1);
        assertEquals(1, bean.getConstrainedBean().getConstraints().size());
        bean.addConstraint((ConstrainedBean) null);
        bean.addConstraint(constraint2);
        assertSame(constraint2, bean.getConstrainedBean());
        assertEquals(2, bean.getConstrainedBean().getConstraints().size());
        assertEquals(1, bean.getConstrainedBean().getUniques().size());
        assertEquals("property3", ((String[]) bean.getConstrainedBean().getUniques().get(0))[0]);
        assertEquals("other2", ((String[]) bean.getConstrainedBean().getUniques().get(0))[1]);
        assertEquals("other3", ((String[]) bean.getConstrainedBean().getUniques().get(0))[2]);
        assertEquals("ordered", ((ConstrainedBean.Order) bean.getConstrainedBean().getDefaultOrdering().get(0)).getPropertyName());
        assertSame(ConstrainedBean.ASC, ((ConstrainedBean.Order) bean.getConstrainedBean().getDefaultOrdering().get(0)).getDirection());
    }

    @Test
    public void testConstraintSubject() {
        Bean bean = null;

        bean = new Bean("value");
        bean.addConstraint(new ConstrainedProperty("property").notNull(true));
        bean.setProperty(null);
        assertFalse(bean.validate());
        Set<ValidationError> set = bean.getValidationErrors();
        assertEquals("property", set.iterator().next().getSubject());

        bean = new Bean("value");
        bean.addConstraint(new ConstrainedProperty("property").subjectName("subject").notNull(true));
        bean.setProperty(null);
        assertFalse(bean.validate());
        set = bean.getValidationErrors();
        assertEquals("subject", set.iterator().next().getSubject());
    }

    @Test
    public void testConstraintNotNull() {
        Bean bean = new Bean("value");
        bean.addConstraint(new ConstrainedProperty("property").notNull(true));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isNotNull());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty(null);
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintNotEmpty() {
        Bean bean = new Bean("value");
        bean.addConstraint(new ConstrainedProperty("property").notEmpty(true));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isNotEmpty());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintNotEqual() {
        Bean bean = new Bean("other");
        bean.addConstraint(new ConstrainedProperty("property").notEqual("value"));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isNotEqual());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("value");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintLimitedLength() {
        Bean bean = new Bean("value");
        bean.addConstraint(new ConstrainedProperty("property").minLength(3));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().hasLimitedLength());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("df");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintEmail() {
        Bean bean = new Bean("test@domain.com");
        bean.addConstraint(new ConstrainedProperty("property").email(true));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isEmail());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("dfdf@");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintUrl() {
        Bean bean = new Bean("http://test.some.com");
        bean.addConstraint(new ConstrainedProperty("property").url(true));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isUrl());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("htt:www/.test.com");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintRegexp() {
        Bean bean = new Bean("two words");
        bean.addConstraint(new ConstrainedProperty("property").regexp("\\w+ \\w+"));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().matchesRegexp());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("aword");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintFormat() {
        Bean bean = new Bean("12032003");
        bean.addConstraint(new ConstrainedProperty("property").format(new SimpleDateFormat("ddmmyyyy")));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isFormatted());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("12");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintLimitedDate() {
        Bean bean = new Bean(null);
        bean.setTheDate(new Date(2003, 12, 11));
        bean.addConstraint(new ConstrainedProperty("theDate").maxDate(new Date(2004, 3, 1)));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isLimitedDate());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setTheDate(new Date(2004, 12, 11));
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintInList() {
        Bean bean = new Bean("entry");
        bean.addConstraint(new ConstrainedProperty("property").inList(new String[]{"one", "two", "entry", "three"}));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isInList());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("notinlist");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintRange() {
        Bean bean = new Bean("bbbb");
        bean.addConstraint(new ConstrainedProperty("property").rangeBegin("aaab").rangeEnd("ccca"));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isRange());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setProperty("dddd");
        assertFalse(bean.validate());
    }

    @Test
    public void testConstraintSameAs() {
        Bean bean = new Bean("first value");
        bean.setOther("first value");
        bean.addConstraint(new ConstrainedProperty("other").sameAs("property"));
        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isSameAs());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setOther("second value");
        assertFalse(bean.validate());
    }

    @Test
    public void testSeveralConstraints() {
        Bean bean = new Bean("value");
        bean.setTheDate(new Date());

        assertNull(bean.getConstrainedProperty(null));
        assertNull(bean.getConstrainedProperty(""));
        assertNull(bean.getConstrainedProperty("property"));

        bean.addConstraint((ConstrainedProperty) null);
        bean.addConstraint(new ConstrainedProperty("theDate").notNull(true));
        bean.addConstraint(new ConstrainedProperty("property").notNull(true).notEmpty(true));

        assertNull(bean.getConstrainedProperty("property_unknown"));
        assertTrue(bean.getConstrainedProperty("theDate").isNotNull());
        assertTrue(bean.getConstrainedProperty("property").isNotNull());
        assertTrue(bean.getConstrainedProperty("property").isNotEmpty());

        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isNotNull());
        assertTrue(it.hasNext());
        ConstrainedProperty property = it.next();
        assertTrue(property.isNotNull());
        assertTrue(property.isNotEmpty());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setTheDate(null);
        assertFalse(bean.validate());

        bean.setTheDate(new Date());
        bean.resetValidation();
        assertTrue(bean.validate());

        bean.setProperty("");
        bean.resetValidation();
        assertFalse(bean.validate());

        bean.setProperty(null);
        bean.resetValidation();
        assertFalse(bean.validate());
    }

    @Test
    public void testSameConstraintProperties() {
        Bean bean = new Bean("value");
        bean.setTheDate(new Date());

        bean.addConstraint(new ConstrainedProperty("theDate").notNull(true));
        bean.addConstraint(new ConstrainedProperty("property").notNull(true).notEmpty(false));
        bean.addConstraint(new ConstrainedProperty("property").notEmpty(true));

        assertNull(bean.getConstrainedProperty("property_unknown"));
        assertTrue(bean.getConstrainedProperty("theDate").isNotNull());
        assertTrue(bean.getConstrainedProperty("property").isNotNull());
        assertTrue(bean.getConstrainedProperty("property").isNotEmpty());

        Iterator<ConstrainedProperty> it = bean.getConstrainedProperties().iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next().isNotNull());
        assertTrue(it.hasNext());
        ConstrainedProperty property = it.next();
        assertTrue(property.isNotNull());
        assertTrue(property.isNotEmpty());
        assertFalse(it.hasNext());

        assertTrue(bean.validate());

        bean.setTheDate(null);
        assertFalse(bean.validate());

        bean.setTheDate(new Date());
        bean.resetValidation();
        assertTrue(bean.validate());

        bean.setProperty("");
        bean.resetValidation();
        assertFalse(bean.validate());

        bean.setProperty(null);
        bean.resetValidation();
        assertFalse(bean.validate());
    }

    @Test
    public void testCloneEmpty() {
        Bean bean = new Bean("value");
        Bean other = null;
        try {
            other = (Bean) bean.clone();
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        assertNotNull(other);
        assertEquals(0, other.getRules().size());
        assertEquals(0, other.countValidationErrors());
        assertEquals(0, other.getValidationErrors().size());
        assertEquals(0, other.getConstrainedProperties().size());
        assertEquals(0, other.getGroups().size());
        assertNull(other.getConstrainedBean());
    }

    @Test
    public void testCloneFilled() {
        Iterator<ValidationGroup> it_groups = null;
        Iterator<String> it_subjects = null;
        Iterator<ConstrainedProperty> it_constraints = null;
        Iterator<ValidationError> it_errors = null;
        ValidationError error = null;

        Bean bean = new Bean("value");

        bean.addGroup("group1");
        it_groups = bean.getGroups().iterator();
        assertTrue(it_groups.hasNext());
        assertEquals(it_groups.next().getName(), "group1");
        assertSame(bean, bean.getGroup("group1").getValidation());
        assertFalse(it_groups.hasNext());

        bean.addConstraint(new ConstrainedProperty("property").notEqual("value"));
        it_constraints = bean.getConstrainedProperties().iterator();
        assertTrue(it_constraints.hasNext());
        assertTrue(it_constraints.next().isNotEqual());
        assertFalse(it_constraints.hasNext());

        it_subjects = bean.getValidatedSubjects().iterator();
        assertTrue(it_subjects.hasNext());
        assertEquals("property", it_subjects.next());
        assertFalse(it_subjects.hasNext());

        bean.limitSubjectErrors("property");

        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());
        assertFalse(bean.validate());
        assertEquals(1, bean.countValidationErrors());
        assertEquals(1, bean.getValidationErrors().size());

        it_errors = bean.getValidationErrors().iterator();
        assertTrue(it_errors.hasNext());
        error = it_errors.next();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertFalse(it_errors.hasNext());

        bean.setProperty("test");
        bean.resetValidation();
        assertTrue(bean.validate());
        bean.setProperty("value");
        bean.resetValidation();
        assertFalse(bean.validate());

        Bean other = null;
        try {
            other = (Bean) bean.clone();
        } catch (CloneNotSupportedException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        it_groups = other.getGroups().iterator();
        assertTrue(it_groups.hasNext());
        assertEquals(it_groups.next().getName(), "group1");
        assertSame(other, other.getGroup("group1").getValidation());
        assertFalse(it_groups.hasNext());

        it_constraints = other.getConstrainedProperties().iterator();
        assertTrue(it_constraints.hasNext());
        assertTrue(it_constraints.next().isNotEqual());
        assertFalse(it_constraints.hasNext());
        assertEquals(1, other.countValidationErrors());
        assertEquals(1, other.getValidationErrors().size());
        assertFalse(bean.validate());
        assertEquals(1, other.countValidationErrors());
        assertEquals(1, other.getValidationErrors().size());

        it_subjects = other.getValidatedSubjects().iterator();
        assertTrue(it_subjects.hasNext());
        assertEquals("property", it_subjects.next());
        assertFalse(it_subjects.hasNext());

        it_errors = other.getValidationErrors().iterator();
        assertTrue(it_errors.hasNext());
        error = it_errors.next();
        assertEquals(ValidationError.IDENTIFIER_INVALID, error.getIdentifier());
        assertEquals("property", error.getSubject());
        assertFalse(it_errors.hasNext());

        bean.setProperty("test");
        bean.resetValidation();
        assertTrue(bean.validate());
        other.resetValidation();
        assertFalse(other.validate());

        other.setProperty("test2");
        other.resetValidation();
        assertTrue(other.validate());
        other.resetValidation();
        assertTrue(other.validate());

        bean.setProperty("value");
        bean.resetValidation();
        assertFalse(bean.validate());
        other.resetValidation();
        assertTrue(other.validate());
    }

    public class Bean extends Validation {
        private String property_ = null;
        private String other_ = null;
        private Date theDate_ = null;

        public Bean(String property) {
            property_ = property;
        }

        public void setProperty(String property) {
            property_ = property;
        }

        public String getProperty() {
            return property_;
        }

        public void setOther(String other) {
            other_ = other;
        }

        public String getOther() {
            return other_;
        }

        public void setTheDate(Date date) {
            theDate_ = date;
        }

        public Date getTheDate() {
            return theDate_;
        }
    }
}
