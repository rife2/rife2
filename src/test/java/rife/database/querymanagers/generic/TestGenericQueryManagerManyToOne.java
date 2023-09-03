/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.validation.ValidationError;
import rifetestmodels.MOFirstBean;
import rifetestmodels.MOSecondBean;
import rifetestmodels.MOThirdBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerManyToOne {
    private GenericQueryManager<MOFirstBean> firstManager_ = null;
    private GenericQueryManager<MOSecondBean> secondManager_ = null;
    private GenericQueryManager<MOThirdBean> thirdManager_ = null;

    protected void setup(Datasource datasource) {
        firstManager_ = GenericQueryManagerFactory.instance(datasource, MOFirstBean.class);
        secondManager_ = GenericQueryManagerFactory.instance(datasource, MOSecondBean.class);
        thirdManager_ = GenericQueryManagerFactory.instance(datasource, MOThirdBean.class);
        thirdManager_.install();
        secondManager_.install();
        firstManager_.install();
    }

    protected void tearDown() {
        firstManager_.remove();
        secondManager_.remove();
        thirdManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetBaseClass(Datasource datasource) {
        setup(datasource);
        try {
            assertSame(MOFirstBean.class, firstManager_.getBaseClass());
            assertSame(MOSecondBean.class, secondManager_.getBaseClass());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstallCustomQuery(Datasource datasource) {
        setup(datasource);
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
    void testSaveRestoreConstrained(Datasource datasource) {
        setup(datasource);
        try {
            var bean_a = new MOFirstBean();
            var bean_b = new MOFirstBean();
            MOFirstBean new_bean_a = null;
            MOFirstBean new_bean_b = null;

            bean_a.setFirstString("This is my test string");
            bean_b.setFirstString("This is my test string B");

            // add the many-to-one relations to the bean instance
            var bean_2a = new MOSecondBean();
            bean_2a.setSecondString("MOSecondBeanA");

            var bean_2b = new MOSecondBean();
            bean_2b.setSecondString("MOSecondBeanB");

            var bean_2c = new MOSecondBean();
            bean_2c.setSecondString("MOSecondBeanC");

            var bean_3a = new MOThirdBean();
            bean_3a.setThirdString("MOThirdBeanA");

            var bean_3b = new MOThirdBean();
            bean_3a.setThirdString("MOThirdBeanB");

            bean_a.setSecondBean(bean_2a);
            bean_a.setSecondBean2(bean_2b);
            bean_a.setThirdBean(bean_3a);

            bean_b.setSecondBean(bean_2b);
            bean_b.setSecondBean2(bean_2c);
            bean_b.setThirdBean(bean_3b);

            // assert that the many-to-one relations have not been saved too
            assertNull(bean_2a.getIdentifier());
            assertNull(bean_2b.getIdentifier());
            assertNull(bean_2c.getIdentifier());
            assertNull(bean_3a.getId());
            assertNull(bean_3b.getId());

            // save the bean instances
            Integer ida = firstManager_.save(bean_a);
            Integer idb = firstManager_.save(bean_b);

            // assert that the many-to-one relations have been saved too
            assertNotNull(bean_2a.getIdentifier());
            assertNotNull(bean_2b.getIdentifier());
            assertNotNull(bean_2c.getIdentifier());
            assertNotNull(bean_3a.getId());
            assertNotNull(bean_3b.getId());

            // restore the bean instances
            new_bean_a = firstManager_.restore(ida);
            new_bean_b = firstManager_.restore(idb);

            // assert that the first bean has correctly been restored
            assertNotNull(new_bean_a);
            assertNotSame(new_bean_a, bean_a);
            assertEquals(new_bean_a.getFirstString(), bean_a.getFirstString());
            assertEquals(new_bean_a.getIdentifier(), ida);

            // assert that the first bean's many-to-one relationships have correctly been restored
            var second_bean_a = new_bean_a.getSecondBean();
            assertNotNull(second_bean_a);
            assertEquals(bean_2a.getIdentifier(), second_bean_a.getIdentifier());
            assertEquals(bean_2a.getSecondString(), second_bean_a.getSecondString());

            // assert that the second bean has correctly been restored
            assertNotNull(new_bean_b);
            assertNotSame(new_bean_b, bean_b);
            assertEquals(new_bean_b.getFirstString(), bean_b.getFirstString());
            assertEquals(new_bean_b.getIdentifier(), idb);

            // assert that the second bean's many-to-one relationships have correctly been restored
            var second_bean_b = new_bean_b.getSecondBean();
            assertNotNull(second_bean_b);
            assertEquals(bean_2b.getIdentifier(), second_bean_b.getIdentifier());
            assertEquals(bean_2b.getSecondString(), second_bean_b.getSecondString());

            // assert that exactly the same instance is returned the next time the property is retrieved
            assertSame(second_bean_a, new_bean_a.getSecondBean());
            assertSame(second_bean_a, new_bean_a.getSecondBean());
            assertSame(second_bean_b, new_bean_b.getSecondBean());
            assertSame(second_bean_b, new_bean_b.getSecondBean());

            // set the property to null to cause a new instance to be fetched
            new_bean_a.setSecondBean(null);
            var second_bean_c = new_bean_a.getSecondBean();
            assertNotNull(second_bean_c);
            assertEquals(second_bean_c.getIdentifier(), second_bean_a.getIdentifier());
            assertEquals(second_bean_c.getSecondString(), second_bean_a.getSecondString());
            assertNotSame(second_bean_c, second_bean_a);

            new_bean_b.setSecondBean(null);
            var second_bean_d = new_bean_b.getSecondBean();
            assertNotNull(second_bean_d);
            assertEquals(second_bean_d.getIdentifier(), second_bean_b.getIdentifier());
            assertEquals(second_bean_d.getSecondString(), second_bean_b.getSecondString());
            assertNotSame(second_bean_d, second_bean_b);

            // assert that the other many-to-one relationships have correctly been restored
            var second_bean_2a = new_bean_a.getSecondBean2();
            assertNotNull(second_bean_2a);
            assertEquals(bean_2b.getIdentifier(), second_bean_2a.getIdentifier());
            assertEquals(bean_2b.getSecondString(), second_bean_2a.getSecondString());

            var third_bean_a = new_bean_a.getThirdBean();
            assertNotNull(third_bean_a);
            assertEquals(bean_3a.getId(), third_bean_a.getId());
            assertEquals(bean_3a.getThirdString(), third_bean_a.getThirdString());

            var second_bean_2b = new_bean_b.getSecondBean2();
            assertNotNull(second_bean_2b);
            assertEquals(bean_2c.getIdentifier(), second_bean_2b.getIdentifier());
            assertEquals(bean_2c.getSecondString(), second_bean_2b.getSecondString());

            var third_bean_b = new_bean_b.getThirdBean();
            assertNotNull(third_bean_b);
            assertEquals(bean_3b.getId(), third_bean_b.getId());
            assertEquals(bean_3b.getThirdString(), third_bean_b.getThirdString());

            // perform update with changed many-to-one relationships
            // only the data of those that haven't been saved before will
            // be stored
            bean_a.setIdentifier(ida);
            bean_a.setFirstString("This is a new test string");

            bean_2a.setSecondString("MOSecondBeanAUpdated");

            var bean2d = new MOSecondBean();
            bean2d.setSecondString("MOSecondBeanD");
            bean_a.setSecondBean2(bean2d);

            assertEquals(firstManager_.save(bean_a), ida.intValue());
            assertEquals(bean_a.getIdentifier(), ida);

            // restore the updated bean
            new_bean_a = firstManager_.restore(ida);

            assertNotNull(new_bean_a);
            assertNotSame(new_bean_a, bean_a);

            // assert that the updated bean has been stored correctly
            assertEquals(new_bean_a.getFirstString(), "This is a new test string");

            // assert that the many-to-one relationships have correctly been stored and restored
            second_bean_a = new_bean_a.getSecondBean();
            assertNotNull(second_bean_a);
            assertEquals(bean_2a.getIdentifier(), second_bean_a.getIdentifier());
            // the data of this many-to-one association hasn't been updated since the entity already was saved before
            assertNotEquals(bean_2a.getSecondString(), second_bean_a.getSecondString());

            var second_bean3 = new_bean_a.getSecondBean2();
            assertNotNull(second_bean3);
            assertEquals(bean2d.getIdentifier(), second_bean3.getIdentifier());
            assertEquals(bean2d.getSecondString(), second_bean3.getSecondString());

            third_bean_a = new_bean_a.getThirdBean();
            assertNotNull(third_bean_a);
            assertEquals(bean_3a.getId(), third_bean_a.getId());
            assertEquals(bean_3a.getThirdString(), third_bean_a.getThirdString());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveRestoreConstrainedAssociation(Datasource datasource) {
        setup(datasource);
        try {
            var bean1a = new MOFirstBean();
            bean1a.setFirstString("This is my test string");
            var bean2 = new MOSecondBean();
            bean2.setSecondString("MOSecondBeanA");
            bean1a.setSecondBean(bean2);

            // save the bean instance
            Integer id = firstManager_.save(bean1a);

            // save a second instance of the first bean type
            var bean1b = new MOFirstBean();
            bean1b.setFirstString("This is my test string B");
            bean1b.setSecondBean(bean2);
            firstManager_.save(bean1b);

            // restore the second bean
            var second_bean = secondManager_.restore(bean2.getIdentifier());

            // assert that the second bean association links are correct
            var first_beans = second_bean.getFirstBeans();
            assertNotNull(first_beans);
            assertEquals(2, first_beans.size());
            for (var bean_assoc_restored : first_beans) {
                if (bean_assoc_restored.getIdentifier().equals(bean1a.getIdentifier())) {
                    assertEquals(bean_assoc_restored.getFirstString(), bean1a.getFirstString());
                } else if (bean_assoc_restored.getIdentifier().equals(bean1b.getIdentifier())) {
                    assertEquals(bean_assoc_restored.getFirstString(), bean1b.getFirstString());
                } else {
                    fail();
                }
                assertEquals(bean2.getIdentifier(), bean_assoc_restored.getSecondBean().getIdentifier());
                assertEquals(bean2.getSecondString(), bean_assoc_restored.getSecondBean().getSecondString());
            }

            // store the second bean with updated links
            first_beans.remove(first_beans.iterator().next());
            secondManager_.save(second_bean);
            second_bean = secondManager_.restore(bean2.getIdentifier());
            first_beans = second_bean.getFirstBeans();
            assertNotNull(first_beans);
            assertEquals(1, first_beans.size());

            // save a third instance of the first bean type and an updated
            // version of the first instance, which will not be saved
            var bean1c = new MOFirstBean();
            bean1c.setFirstString("This is my test string C");
            assertNull(bean1c.getIdentifier());
            List<MOFirstBean> firstbeans2 = new ArrayList<>();
            firstbeans2.add(bean1a);
            bean1a.setFirstString("This is my test string updated");
            firstbeans2.add(bean1c);
            second_bean.setFirstBeans(firstbeans2);
            secondManager_.save(second_bean);
            assertNotNull(bean1c.getIdentifier());

            second_bean = secondManager_.restore(bean2.getIdentifier());
            first_beans = second_bean.getFirstBeans();
            assertNotNull(first_beans);
            assertEquals(2, first_beans.size());
            for (var bean_assoc_restored : first_beans) {
                if (bean_assoc_restored.getIdentifier().equals(bean1a.getIdentifier())) {
                    assertEquals(bean_assoc_restored.getFirstString(), "This is my test string");
                    assertNotEquals(bean_assoc_restored.getFirstString(), bean1a.getFirstString());
                } else if (bean_assoc_restored.getIdentifier().equals(bean1c.getIdentifier())) {
                    assertEquals(bean_assoc_restored.getFirstString(), bean1c.getFirstString());
                } else {
                    fail();
                }
                assertEquals(bean2.getIdentifier(), bean_assoc_restored.getSecondBean().getIdentifier());
                assertEquals(bean2.getSecondString(), bean_assoc_restored.getSecondBean().getSecondString());
            }
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDelete(Datasource datasource) {
        setup(datasource);
        try {
            assertEquals(0, secondManager_.count());
            assertEquals(0, thirdManager_.count());

            var bean = new MOFirstBean();

            bean.setFirstString("This is my test string");

            // add the many-to-one relations to the bean instance
            var bean_2a = new MOSecondBean();
            bean_2a.setSecondString("MOSecondBeanA");

            var bean_3 = new MOThirdBean();
            bean_3.setThirdString("MOThirdBean");

            bean.setSecondBean(bean_2a);
            bean.setThirdBean(bean_3);

            // save the bean instance
            var id1 = firstManager_.save(bean);

            // ensure that everything was saved correctly
            assertNotNull(firstManager_.restore(id1));
            assertEquals(1, secondManager_.count());
            assertEquals(1, thirdManager_.count());

            // delete the first bean
            firstManager_.delete(id1);

            // ensure that everything was deleted correctly
            assertNull(firstManager_.restore(id1));
            assertEquals(1, secondManager_.count());
            assertEquals(1, thirdManager_.count());

            // add another many-to-one relationship
            var bean_2c = new MOSecondBean();
            bean_2c.setSecondString("MOSecondBeanC");

            bean.setSecondBean2(bean_2c);

            // save the bean instance again
            var id2 = firstManager_.save(bean);

            // ensure that everything was saved correctly
            assertNotNull(firstManager_.restore(id2));
            assertEquals(2, secondManager_.count());
            assertEquals(1, thirdManager_.count());

            // delete the second bean
            firstManager_.delete(id2);

            // ensure that everything was deleted correctly
            assertNull(firstManager_.restore(id2));
            assertEquals(2, secondManager_.count());
            assertEquals(1, thirdManager_.count());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDeleteAssociation(Datasource datasource) {
        setup(datasource);
        try {
            final var bean_1a = new MOFirstBean();
            bean_1a.setFirstString("This is my test string");

            final var bean_1b = new MOFirstBean();
            bean_1b.setFirstString("This is my test string B");

            var bean_2 = new MOSecondBean();
            bean_2.setSecondString("MOSecondBeanA");
            bean_2.setFirstBeans(new ArrayList<>() {{
                add(bean_1a);
                add(bean_1b);
            }});

            // save the second bean
            assertTrue(secondManager_.save(bean_2) > -1);

            // restore the second bean
            var second_bean = secondManager_.restore(bean_2.getIdentifier());
            assertEquals(2, second_bean.getFirstBeans().size());

            // delete the second bean
            assertTrue(secondManager_.delete(bean_2.getIdentifier()));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidationContextManyToOne(Datasource datasource) {
        setup(datasource);
        try {
            var bean = new MOFirstBean();

            bean.setFirstString("This is my test string");

            // add the many-to-one relations to the bean instance
            var bean_2a = new MOSecondBean();
            bean_2a.setIdentifier(23);
            bean_2a.setSecondString("MOSecondBeanA");

            var bean_2b = new MOSecondBean();
            bean_2b.setIdentifier(24);
            bean_2b.setSecondString("MOSecondBeanB");

            bean.setSecondBean(bean_2a);
            bean.setSecondBean2(bean_2b);

            // validate the bean instance
            ValidationError error;
            assertFalse(bean.validate(firstManager_));
            assertEquals(2, bean.getValidationErrors().size());
            var error_it = bean.getValidationErrors().iterator();
            error = error_it.next();
            assertEquals(error.getSubject(), "secondBean");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            error = error_it.next();
            assertEquals(error.getSubject(), "secondBean2");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            assertFalse(error_it.hasNext());

            bean.resetValidation();

            // store the first associated bean
            secondManager_.save(bean_2a);

            // validate the bean instance again
            assertFalse(bean.validate(firstManager_));
            assertEquals(1, bean.getValidationErrors().size());
            error_it = bean.getValidationErrors().iterator();
            error = error_it.next();
            assertEquals(error.getSubject(), "secondBean2");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            assertFalse(error_it.hasNext());

            // store the second associated bean
            secondManager_.save(bean_2b);

            bean.resetValidation();

            // validate the bean instance a last time
            assertTrue(bean.validate(firstManager_));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidationContextManyToOneAssociation(Datasource datasource) {
        setup(datasource);
        try {
            final var bean_1a = new MOFirstBean();
            bean_1a.setIdentifier(23);
            bean_1a.setFirstString("This is my test string");

            final var bean_1b = new MOFirstBean();
            bean_1b.setIdentifier(27);
            bean_1b.setFirstString("This is my test string B");

            var bean_2 = new MOSecondBean();
            bean_2.setSecondString("MOSecondBeanA");
            bean_2.setFirstBeans(new ArrayList<>() {{
                add(bean_1a);
                add(bean_1b);
            }});

            // validate the bean instance
            ValidationError error;
            assertFalse(bean_2.validate(secondManager_));
            assertEquals(1, bean_2.getValidationErrors().size());
            var error_it = bean_2.getValidationErrors().iterator();
            error = error_it.next();
            assertEquals(error.getSubject(), "firstBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            assertFalse(error_it.hasNext());

            bean_2.resetValidation();

            // store the first associated bean
            firstManager_.save(bean_1a);

            // validate the bean instance
            assertFalse(bean_2.validate(secondManager_));
            assertEquals(1, bean_2.getValidationErrors().size());
            error_it = bean_2.getValidationErrors().iterator();
            error = error_it.next();
            assertEquals(error.getSubject(), "firstBeans");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            assertFalse(error_it.hasNext());

            bean_2.resetValidation();

            // store the first associated bean
            firstManager_.save(bean_1b);

            // validate the bean instance a last time
            assertTrue(bean_2.validate(secondManager_));
        } finally {
            tearDown();
        }
    }
}
