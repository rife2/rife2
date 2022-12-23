/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.*;
import rife.database.queries.Select;
import rife.database.querymanagers.generic.beans.MMFirstBean;
import rife.database.querymanagers.generic.beans.MMSecondBean;
import rife.validation.ValidationError;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerManyToMany {
    private GenericQueryManager<MMFirstBean> firstManager_ = null;
    private GenericQueryManager<MMSecondBean> secondManager_ = null;

    protected void setUp(Datasource datasource) {
        firstManager_ = GenericQueryManagerFactory.getInstance(datasource, MMFirstBean.class);
        secondManager_ = GenericQueryManagerFactory.getInstance(datasource, MMSecondBean.class);
        secondManager_.install();
        firstManager_.install();
    }

    protected void tearDown() {
        firstManager_.remove();
        secondManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClass(Datasource datasource) {
        setUp(datasource);
        try {
            assertSame(MMFirstBean.class, firstManager_.getBaseClass());
            assertSame(MMSecondBean.class, secondManager_.getBaseClass());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQuery(Datasource datasource) {
        setUp(datasource);
        try {
            firstManager_.remove();
            secondManager_.remove();

            secondManager_.install(secondManager_.getInstallTableQuery());
            firstManager_.install(firstManager_.getInstallTableQuery());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveRestoreConstrained(Datasource datasource) {
        setUp(datasource);
        try {
            var bean = new MMFirstBean();
            MMFirstBean new_bean = null;

            bean.setFirstString("This is my test string");

            // add the many-to-many relations to the bean instance
            var bean2a = new MMSecondBean();
            bean2a.setSecondString("MMSecondBeanA");

            var bean2b = new MMSecondBean();
            bean2b.setSecondString("MMSecondBeanB");

            var bean2c = new MMSecondBean();
            bean2c.setSecondString("MMSecondBeanC");

            Collection<MMSecondBean> second_beans = new ArrayList<>();
            second_beans.add(bean2a);
            second_beans.add(bean2b);
            second_beans.add(bean2c);
            bean.setSecondBeans(second_beans);

            // assert that the many-to-many relations have not been saved too
            assertNull(bean2a.getIdentifier());
            assertNull(bean2b.getIdentifier());
            assertNull(bean2c.getIdentifier());

            // save the bean instance
            Integer id = firstManager_.save(bean);

            // assert that the many-to-many relations have been saved too
            assertNotNull(bean2a.getIdentifier());
            assertNotNull(bean2b.getIdentifier());
            assertNotNull(bean2c.getIdentifier());

            // restore the bean instance
            new_bean = firstManager_.restore(id);

            // assert that the bean has correctly been restored
            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);
            assertEquals(new_bean.getFirstString(), bean.getFirstString());
            assertEquals(new_bean.getIdentifier(), id);

            // assert that the many-to-many relationships have correctly been restored
            var second_beans_restored = new_bean.getSecondBeans();
            var bean2a_found = false;
            var bean2b_found = false;
            var bean2c_found = false;
            for (var second_bean : second_beans_restored) {
                if ("MMSecondBeanA".equals(second_bean.getSecondString())) {
                    assertFalse(bean2a_found);
                    assertEquals(bean2a.getIdentifier(), second_bean.getIdentifier());
                    assertEquals(bean2a.getSecondString(), second_bean.getSecondString());
                    bean2a_found = true;
                } else if ("MMSecondBeanB".equals(second_bean.getSecondString())) {
                    assertFalse(bean2b_found);
                    assertEquals(bean2b.getIdentifier(), second_bean.getIdentifier());
                    assertEquals(bean2b.getSecondString(), second_bean.getSecondString());
                    bean2b_found = true;
                } else if ("MMSecondBeanC".equals(second_bean.getSecondString())) {
                    assertFalse(bean2c_found);
                    assertEquals(bean2c.getIdentifier(), second_bean.getIdentifier());
                    assertEquals(bean2c.getSecondString(), second_bean.getSecondString());
                    bean2c_found = true;
                }

                assertNotNull(second_bean.getFirstBeans());
                assertEquals(1, second_bean.getFirstBeans().size());

                var first_bean = second_bean.getFirstBeans().iterator().next();
                assertEquals(new_bean.getIdentifier(), first_bean.getIdentifier());
                assertEquals(new_bean.getFirstString(), first_bean.getFirstString());
            }
            assertTrue(bean2a_found);
            assertTrue(bean2b_found);
            assertTrue(bean2c_found);

            // perform update with changed many-to-many relationships
            // only the data of those that haven't been saved before will
            // be stored
            bean.setIdentifier(id);
            bean.setFirstString("This is a new test string");

            bean2a.setSecondString("MMSecondBeanAUpdated");

            var bean2d = new MMSecondBean();
            bean2d.setSecondString("MMSecondBeanD");
            second_beans = new ArrayList<>();
            second_beans.add(bean2a);
            second_beans.add(bean2c);
            second_beans.add(bean2d);
            bean.setSecondBeans(second_beans);

            assertEquals(firstManager_.save(bean), id.intValue());
            assertEquals(bean.getIdentifier(), id);

            // restore the updated bean
            new_bean = firstManager_.restore(id);

            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);

            // assert that the updated bean has been stored correctly
            assertEquals(new_bean.getFirstString(), "This is a new test string");

            // assert that the many-to-many relationships have correctly been stored and restored
            second_beans_restored = new_bean.getSecondBeans();
            bean2a_found = false;
            bean2b_found = false;
            bean2c_found = false;
            var bean2d_found = false;
            for (var second_bean : second_beans_restored) {
                if ("MMSecondBeanA".equals(second_bean.getSecondString())) {
                    assertFalse(bean2a_found);
                    assertEquals(bean2a.getIdentifier(), second_bean.getIdentifier());
                    // the data of this many-to-many association hasn't been updated since the entity already was saved before
                    assertNotEquals(bean2a.getSecondString(), second_bean.getSecondString());
                    bean2a_found = true;
                } else if ("MMSecondBeanB".equals(second_bean.getSecondString())) {
                    bean2b_found = true;
                } else if ("MMSecondBeanC".equals(second_bean.getSecondString())) {
                    assertFalse(bean2c_found);
                    assertEquals(bean2c.getIdentifier(), second_bean.getIdentifier());
                    assertEquals(bean2c.getSecondString(), second_bean.getSecondString());
                    bean2c_found = true;
                } else if ("MMSecondBeanD".equals(second_bean.getSecondString())) {
                    assertFalse(bean2d_found);
                    assertEquals(bean2d.getIdentifier(), second_bean.getIdentifier());
                    assertEquals(bean2d.getSecondString(), second_bean.getSecondString());
                    bean2d_found = true;
                }

                assertNotNull(second_bean.getFirstBeans());
                assertEquals(1, second_bean.getFirstBeans().size());

                var first_bean = second_bean.getFirstBeans().iterator().next();
                assertEquals(new_bean.getIdentifier(), first_bean.getIdentifier());
                assertEquals(new_bean.getFirstString(), first_bean.getFirstString());
            }
            assertTrue(bean2a_found);
            assertFalse(bean2b_found);
            assertTrue(bean2c_found);
            assertTrue(bean2d_found);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDelete(Datasource datasource) {
        setUp(datasource);
        try {
            var bean = new MMFirstBean();

            bean.setFirstString("This is my test string");

            // add the many-to-many relations to the bean instance
            var bean2a = new MMSecondBean();
            bean2a.setSecondString("MMSecondBeanA");

            var bean2b = new MMSecondBean();
            bean2b.setSecondString("MMSecondBeanB");

            Collection<MMSecondBean> second_beans = new ArrayList<>();
            second_beans.add(bean2a);
            second_beans.add(bean2b);
            bean.setSecondBeans(second_beans);

            // save the bean instance
            var id1 = firstManager_.save(bean);

            // ensure that everything was saved correctly
            assertNotNull(firstManager_.restore(id1));
            assertEquals(2, new DbQueryManager(datasource)
                .executeGetFirstInt(new Select(datasource)
                    .field("count(*)")
                    .from("mmfirstbean_mmsecondbean")
                    .where("mmfirstbean_identifier", "=", id1)));
            assertEquals(2, secondManager_.count());

            // delete the first bean
            firstManager_.delete(id1);

            // ensure that everything was deleted correctly
            assertNull(firstManager_.restore(id1));
            assertEquals(0, new DbQueryManager(datasource)
                .executeGetFirstInt(new Select(datasource)
                    .field("count(*)")
                    .from("mmfirstbean_mmsecondbean")
                    .where("mmfirstbean_identifier", "=", id1)));
            assertEquals(2, secondManager_.count());

            // add another many-to-many relationship
            var bean2c = new MMSecondBean();
            bean2b.setSecondString("MMSecondBeanC");
            second_beans.add(bean2c);

            // save the bean instance again
            var id2 = firstManager_.save(bean);

            // ensure that everything was saved correctly
            assertNotNull(firstManager_.restore(id2));
            assertEquals(3, new DbQueryManager(datasource)
                .executeGetFirstInt(new Select(datasource)
                    .field("count(*)")
                    .from("mmfirstbean_mmsecondbean")
                    .where("mmfirstbean_identifier", "=", id2)));
            assertEquals(3, secondManager_.count());

            // delete the second bean
            firstManager_.delete(id2);

            // ensure that everything was deleted correctly
            assertNull(firstManager_.restore(id2));
            assertEquals(0, new DbQueryManager(datasource)
                .executeGetFirstInt(new Select(datasource)
                    .field("count(*)")
                    .from("mmfirstbean_mmsecondbean")
                    .where("mmfirstbean_identifier", "=", id2)));
            assertEquals(3, secondManager_.count());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidationContextManyToMany(Datasource datasource) {
        setUp(datasource);
        try {
            var bean = new MMFirstBean();

            bean.setFirstString("This is my test string");

            // add the many-to-many relations to the bean instance
            var bean2a = new MMSecondBean();
            bean2a.setIdentifier(23);
            bean2a.setSecondString("MMSecondBeanA");

            var bean2b = new MMSecondBean();
            bean2b.setIdentifier(24);
            bean2b.setSecondString("MMSecondBeanB");

            Collection<MMSecondBean> second_beans = new ArrayList<>();
            second_beans.add(bean2a);
            second_beans.add(bean2b);
            bean.setSecondBeans(second_beans);

            // validate the bean instance
            ValidationError error;
            assertFalse(bean.validate(firstManager_));
            error = bean.getValidationErrors().iterator().next();
            assertEquals(error.getSubject(), "secondBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);

            bean.resetValidation();

            // store the first associated bean
            secondManager_.save(bean2a);

            // validate the bean instance again
            assertFalse(bean.validate(firstManager_));
            error = bean.getValidationErrors().iterator().next();
            assertEquals(error.getSubject(), "secondBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);

            // store the second associated bean
            secondManager_.save(bean2b);

            bean.resetValidation();

            // validate the bean instance a last time
            assertTrue(bean.validate(firstManager_));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidationContextManyToManyAssociation(Datasource datasource) {
        setUp(datasource);
        try {
            var bean2 = new MMSecondBean();
            bean2.setSecondString("This is my test string");

            // add the many-to-many association relations to the bean instance
            var bean1a = new MMFirstBean();
            bean1a.setIdentifier(23);
            bean1a.setFirstString("MMFirstBeanA");

            var bean1b = new MMFirstBean();
            bean1b.setIdentifier(24);
            bean1b.setFirstString("MMFirstBeanB");

            Collection<MMFirstBean> first_beans = new ArrayList<>();
            first_beans.add(bean1a);
            first_beans.add(bean1b);
            bean2.setFirstBeans(first_beans);

            // validate the bean instance
            ValidationError error;
            assertFalse(bean2.validate(secondManager_));
            error = bean2.getValidationErrors().iterator().next();
            assertEquals(error.getSubject(), "firstBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);

            bean2.resetValidation();

            // store the first associated bean
            firstManager_.save(bean1a);

            // validate the bean instance again
            assertFalse(bean2.validate(secondManager_));
            error = bean2.getValidationErrors().iterator().next();
            assertEquals(error.getSubject(), "firstBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);

            // store the second associated bean
            firstManager_.save(bean1b);

            bean2.resetValidation();

            // validate the bean instance a last time
            assertTrue(bean2.validate(secondManager_));
        } finally {
            tearDown();
        }
    }
}
