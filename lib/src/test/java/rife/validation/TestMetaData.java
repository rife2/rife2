/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.tools.ObjectUtils;
import rifetestmodels.Person;
import rifetestmodels.PersonCallbacks;
import rifetestmodels.PersonCloneable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestMetaData {
    @Test
    public void testConstraintsValidation() {
        Person person = new Person();

        Constrained constrained = (Constrained) person;
        assertNotNull(constrained);

        Set<ValidationError> errors;

        Validated validated = (Validated) person;
        assertFalse(validated.validate());
        assertFalse(validated.isSubjectValid("firstname"));
        assertTrue(validated.isSubjectValid("lastname"));
        errors = validated.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(ValidationError.IDENTIFIER_MANDATORY, errors.iterator().next().getIdentifier());

        validated.resetValidation();
        person.setFirstname("John");
        person.setLastname("Smith");
        assertTrue(validated.validate());

        validated.resetValidation();
        person.setFirstname("John");
        person.setLastname("Wayne");
        assertFalse(validated.validate());
        assertTrue(validated.isSubjectValid("firstname"));
        assertFalse(validated.isSubjectValid("lastname"));
        errors = validated.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(ValidationError.IDENTIFIER_INVALID, errors.iterator().next().getIdentifier());

        validated.resetValidation();
        person.setFirstname("Dean Marie Alson");
        person.setLastname("Jones");
        assertFalse(validated.validate());
        assertFalse(validated.isSubjectValid("firstname"));
        assertTrue(validated.isSubjectValid("lastname"));
        errors = validated.getValidationErrors();
        assertEquals(1, errors.size());
        assertEquals(ValidationError.IDENTIFIER_WRONGLENGTH, errors.iterator().next().getIdentifier());
    }

    @Test
    public void testCloningNoMethod() {
        Person person = new Person();

        person.setFirstname("John");
        person.setLastname("Smith");
        assertTrue(((Validated) person).validate());

        Person person_clone = ObjectUtils.genericClone(person);
        assertTrue(((Validated) person_clone).validate());

        person_clone.setFirstname("Jeremy Jackson James");
        person_clone.setLastname("Dillinger");
        assertFalse(((Validated) person_clone).validate());
        assertEquals(2, ((Validated) person_clone).countValidationErrors());

        assertEquals(0, ((Validated) person).countValidationErrors());
        assertTrue(((Validated) person).validate());
        ((Validated) person).resetValidation();

        assertEquals(2, ((Validated) person_clone).countValidationErrors());
    }

    @Test
    public void testCloningExistingMethod() {
        PersonCloneable person = new PersonCloneable();
        assertNull(person.getFirstname());
        assertNull(person.getLastname());

        PersonCloneable person_clone;

        // check if the original clone methods it still working
        person_clone = ObjectUtils.genericClone(person);
        assertNotNull(person_clone);
        assertEquals("autofirst", person_clone.getFirstname());
        assertNull(person_clone.getLastname());

        // check that the instance properties as really seperated
        assertNull(person.getFirstname());
        assertNull(person.getLastname());

        // check that the validation features are really seperated
        assertFalse(((Validated) person).validate());
        assertEquals(1, ((Validated) person).countValidationErrors());

        assertEquals(0, ((Validated) person_clone).countValidationErrors());
        assertTrue(((Validated) person_clone).validate());
        assertEquals(0, ((Validated) person_clone).countValidationErrors());

        // try another path in the default clone method
        // also test that the original validation errors are cloned correctly
        ((Validated) person).resetValidation();
        assertEquals(0, ((Validated) person).countValidationErrors());
        person.setFirstname("this name is too long");
        assertFalse(((Validated) person).validate());
        assertEquals(1, ((Validated) person).countValidationErrors());

        person_clone = ObjectUtils.genericClone(person);
        assertNotNull(person_clone);
        assertEquals(1, ((Validated) person_clone).countValidationErrors());
        assertEquals("this name is too long", person_clone.getFirstname());
        assertEquals("autolast", person_clone.getLastname());

        // check that the error resetting is really seperated
        ((Validated) person).resetValidation();
        assertEquals(0, ((Validated) person).countValidationErrors());
        assertEquals(1, ((Validated) person_clone).countValidationErrors());

        ((Validated) person_clone).resetValidation();
        assertEquals(0, ((Validated) person_clone).countValidationErrors());

        // check that the constrained properties are seperated
        ConstrainedProperty prop = ((Constrained) person_clone).getConstrainedProperty("lastname");
        assertNotNull(prop);
        String[] list_array = prop.getInList();
        List<String> list_list = new ArrayList<String>(Arrays.asList(list_array));
        list_list.add("Chronno");
        prop.inList(list_list);
        ((Constrained) person_clone).addConstraint(prop);
        person_clone.setFirstname("Jeremy");
        person_clone.setLastname("Chronno");
        assertTrue(((Validated) person_clone).validate());
        assertEquals(0, ((Validated) person_clone).countValidationErrors());

        person.setFirstname("Jeremy");
        person.setLastname("Chronno");
        assertFalse(((Validated) person).validate());
        assertEquals(1, ((Validated) person).countValidationErrors());
    }

    @Test
    public void testCallbacks() {
        PersonCallbacks person = new PersonCallbacks();

        person.setFirstname("John");
        person.setLastname("Smith");

        GenericQueryManager<PersonCallbacks> manager = GenericQueryManagerFactory.getInstance(TestDatasources.DERBY, PersonCallbacks.class);
        manager.install();
        try {
            int id = manager.save(person);
            assertTrue(id >= 0);

            PersonCallbacks restored_person = manager.restore(id);
            assertNotNull(restored_person);

            assertEquals("beforeSave", restored_person.getFirstname());
            assertEquals("Smith", restored_person.getLastname());
        } finally {
            manager.remove();
        }
    }
}
