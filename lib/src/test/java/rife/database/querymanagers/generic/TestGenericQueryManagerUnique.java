/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.beans.UniqueBean;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerUnique {
    private GenericQueryManager<UniqueBean> uniqueManager_ = null;

    private UniqueBean createNewUniqueBean() {
        return new UniqueBean();
    }

    protected void setup(Datasource datasource) {
        uniqueManager_ = GenericQueryManagerFactory.instance(datasource, UniqueBean.class);
        uniqueManager_.install();
    }

    protected void tearDown() {
        uniqueManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetBaseClass(Datasource datasource) {
        setup(datasource);
        try {
            assertSame(UniqueBean.class, uniqueManager_.getBaseClass());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstallCustomQuery(Datasource datasource) {
        setup(datasource);
        try {
            uniqueManager_.remove();
            uniqueManager_.install(uniqueManager_.getInstallTableQuery());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidationContextUnique(Datasource datasource) {
        setup(datasource);
        try {
            // uniqueness of individual properties
            UniqueBean bean1 = createNewUniqueBean();
            bean1.setTestString("test_string");
            bean1.setAnotherString("another_string_one");
            bean1.setThirdString("third_string_one");
            assertTrue(bean1.validate(uniqueManager_));
            int id1 = uniqueManager_.save(bean1);

            bean1 = uniqueManager_.restore(id1);
            bean1.setTestString("test_string_one");
            assertTrue(bean1.validate(uniqueManager_));
            assertEquals(id1, uniqueManager_.save(bean1));

            UniqueBean bean2 = createNewUniqueBean();
            bean2.setTestString("test_string_one");
            bean2.setAnotherString("another_string_two");
            bean2.setThirdString("third_string_two");
            assertFalse(bean2.validate(uniqueManager_));
            try {
                uniqueManager_.save(bean2);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            bean2.resetValidation();
            bean2.setTestString("test_string_two");
            bean2.setAnotherString("another_string_two");
            assertTrue(bean2.validate(uniqueManager_));
            int id2 = uniqueManager_.save(bean2);
            assertTrue(id1 != id2);

            bean1.resetValidation();
            bean1.setTestString("test_string_two");
            assertFalse(bean1.validate(uniqueManager_));
            try {
                uniqueManager_.save(bean1);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            // uniqueness of multiple properties
            UniqueBean bean3 = createNewUniqueBean();
            bean3.setTestString("test_string_three");
            bean3.setAnotherString("another_string");
            bean3.setThirdString("third_string");
            assertTrue(bean3.validate(uniqueManager_));
            int id3 = uniqueManager_.save(bean3);

            bean3 = uniqueManager_.restore(id3);
            bean3.setAnotherString("another_string_three");
            bean3.setThirdString("third_string_three");
            assertTrue(bean3.validate(uniqueManager_));
            assertEquals(id3, uniqueManager_.save(bean3));

            UniqueBean bean4 = createNewUniqueBean();
            bean4.setTestString("test_string_four");
            bean4.setAnotherString("another_string_three");
            bean4.setThirdString("third_string_three");
            assertFalse(bean4.validate(uniqueManager_));
            try {
                uniqueManager_.save(bean4);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            bean4.resetValidation();
            bean4.setAnotherString("another_string_four");
            bean4.setThirdString("third_string_four");
            assertTrue(bean4.validate(uniqueManager_));
            int id4 = uniqueManager_.save(bean4);
            assertTrue(id3 != id4);

            bean3.resetValidation();
            bean3.setAnotherString("another_string_four");
            bean3.setThirdString("third_string_four");
            assertFalse(bean3.validate(uniqueManager_));
            try {
                uniqueManager_.save(bean3);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            UniqueBean bean5 = createNewUniqueBean();
            bean5.setTestString("test_string_five");
            bean5.setAnotherString("another_string_five");
            assertTrue(bean5.validate(uniqueManager_));
            uniqueManager_.save(bean5);

            UniqueBean bean6 = createNewUniqueBean();
            bean6.setTestString("test_string_six");
            bean6.setAnotherString("another_string_five");
            assertTrue(bean6.validate(uniqueManager_));
            // this is DB-specific
//			try
//			{
//				uniqueManager_.save(bean6);
//				fail();
//			}
//			catch (DatabaseException e)
//			{
//				assertTrue(e.getCause() instanceof SQLException);
//			}
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGroupValidationContextUnique(Datasource datasource) {
        setup(datasource);
        try {
            // uniqueness of individual properties
            UniqueBean bean1 = createNewUniqueBean();
            bean1.setTestString("test_string");
            bean1.setAnotherString("another_string_one");
            bean1.setThirdString("third_string_one");
            assertTrue(bean1.validateGroup("group1", uniqueManager_));
            bean1.resetValidation();
            assertTrue(bean1.validateGroup("group2", uniqueManager_));
            int id1 = uniqueManager_.save(bean1);

            bean1 = uniqueManager_.restore(id1);
            bean1.setTestString("test_string_one");
            assertTrue(bean1.validateGroup("group1", uniqueManager_));
            bean1.resetValidation();
            assertTrue(bean1.validateGroup("group2", uniqueManager_));
            assertEquals(id1, uniqueManager_.save(bean1));

            UniqueBean bean2 = createNewUniqueBean();
            bean2.setTestString("test_string_one");
            bean2.setAnotherString("another_string_two");
            bean2.setThirdString("third_string_two");
            assertFalse(bean2.validateGroup("group1", uniqueManager_));
            bean2.resetValidation();
            assertFalse(bean2.validateGroup("group2", uniqueManager_));
            try {
                uniqueManager_.save(bean2);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            bean2.resetValidation();
            bean2.setTestString("test_string_two");
            bean2.setAnotherString("another_string_two");
            assertTrue(bean2.validateGroup("group1", uniqueManager_));
            bean2.resetValidation();
            assertTrue(bean2.validateGroup("group2", uniqueManager_));
            int id2 = uniqueManager_.save(bean2);
            assertTrue(id1 != id2);

            bean1.resetValidation();
            bean1.setTestString("test_string_two");
            assertFalse(bean1.validateGroup("group1", uniqueManager_));
            bean1.resetValidation();
            assertFalse(bean1.validateGroup("group2", uniqueManager_));
            try {
                uniqueManager_.save(bean1);
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }
        } finally {
            tearDown();
        }
    }
}
