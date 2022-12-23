/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.beans.ConstrainedBean;
import rife.database.querymanagers.generic.beans.LinkBean;
import rife.validation.ValidationError;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerConstrained {
    private GenericQueryManager<LinkBean> linkManager_ = null;
    private GenericQueryManager<ConstrainedBean> constrainedManager_ = null;

    protected void setUp(Datasource datasource) {
        linkManager_ = new TestGenericQueryManagerDelegate.GQMLinkBean(datasource);
        constrainedManager_ = new TestGenericQueryManagerDelegate.GQMConstrainedBean(datasource);
        linkManager_.install();
        constrainedManager_.install();
    }

    protected void tearDown() {
        constrainedManager_.remove();
        linkManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClass(Datasource datasource) {
        setUp(datasource);
        try {
            assertSame(LinkBean.class, linkManager_.getBaseClass());
            assertSame(ConstrainedBean.class, constrainedManager_.getBaseClass());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQuery(Datasource datasource) {
        setUp(datasource);
        try {
            constrainedManager_.remove();
            linkManager_.remove();

            linkManager_.install(linkManager_.getInstallTableQuery());
            constrainedManager_.install(constrainedManager_.getInstallTableQuery());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveRestoreConstrained(Datasource datasource) {
        setUp(datasource);
        try {
            var bean = new ConstrainedBean();
            ConstrainedBean new_bean = null;

            bean.setTestString("This is my test string");

            var id = constrainedManager_.save(bean);

            new_bean = constrainedManager_.restore(id);

            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);
            assertEquals(new_bean.getTestString(), bean.getTestString());
            assertEquals(new_bean.getIdentifier(), id);

            bean.setIdentifier(id);
            bean.setTestString("This is a new test string");

            assertEquals(constrainedManager_.save(bean), id);
            assertEquals(bean.getIdentifier(), id);

            new_bean = constrainedManager_.restore(id);

            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);

            assertEquals(new_bean.getTestString(), "This is a new test string");

            bean.setIdentifier(999999);
            bean.setTestString("This is another test string");

            assertNotEquals(999999, constrainedManager_.save(bean));
            assertEquals(bean.getIdentifier(), id + 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidationContextManyToOne(Datasource datasource) {
        setUp(datasource);
        try {
            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();
            var link_bean3 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");
            link_bean3.setTestString("linkbean 3");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);
            linkManager_.save(link_bean3);

            var bean1 = new ConstrainedBean();
            bean1.setTestString("test_string1");
            assertTrue(bean1.validate(constrainedManager_));
            var id1 = constrainedManager_.save(bean1);

            var bean2 = new ConstrainedBean();
            bean2.setTestString("test_string2");
            bean2.setLinkBean(link_bean1.getId());
            assertTrue(bean2.validate(constrainedManager_));
            var id2 = constrainedManager_.save(bean2);
            assertTrue(id1 != id2);

            var bean3 = new ConstrainedBean();
            bean3.setTestString("test_string2");
            bean3.setLinkBean(23);
            assertFalse(bean3.validate(constrainedManager_));
            var error = (ValidationError) bean3.getValidationErrors().iterator().next();
            assertEquals(error.getSubject(), "linkBean");
            assertEquals(error.getIdentifier(), ValidationError.IDENTIFIER_INVALID);
            try {
                constrainedManager_.save(bean3);
                fail("exception not thrown");
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof SQLException);
            }

            bean3.resetValidation();
            bean3.setLinkBean(link_bean3.getId());
            assertTrue(bean3.validate(constrainedManager_));
            var id3 = constrainedManager_.save(bean3);
            assertTrue(id2 != id3);
        } finally {
            tearDown();
        }
    }
}
