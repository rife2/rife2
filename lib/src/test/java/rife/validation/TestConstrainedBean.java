/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestConstrainedBean {
    @Test
    void testInstantiation() {
        ConstrainedBean bean = new ConstrainedBean();
        assertNull(bean.getAssociations());
        assertFalse(bean.hasAssociations());
        assertNull(bean.getUniques());
        assertFalse(bean.hasUniques());
        assertNull(bean.getTextualIdentifier());
        assertFalse(bean.hasTextualIdentifier());
        assertNull(bean.getDefaultOrdering());
        assertFalse(bean.hasDefaultOrdering());
    }

    @Test
    void testAssociations() {
        Class[] associations = new Class[]{RegularBeanImpl.class, InitializedBeanImpl.class};

        ConstrainedBean bean = new ConstrainedBean();
        assertSame(bean, bean.associations(associations));
        assertTrue(bean.hasAssociations());
        assertSame(associations, bean.getAssociations());

        assertSame(bean, bean.associations((Class[]) null));
        assertFalse(bean.hasAssociations());
        assertNull(bean.getAssociations());

        associations = new Class[0];
        assertSame(bean, bean.associations(associations));
        assertFalse(bean.hasAssociations());
        assertSame(associations, bean.getAssociations());
    }

    @Test
    void testUniques() {
        ConstrainedBean bean = new ConstrainedBean();
        assertSame(bean, bean.unique());
        assertTrue(bean.hasUniques());
        assertNotNull(bean.getUniques());
        assertEquals(1, bean.getUniques().size());
        assertEquals(0, ((String[]) bean.getUniques().get(0)).length);

        assertSame(bean, bean.uniques(null));
        assertFalse(bean.hasUniques());
        assertNull(bean.getUniques());

        final String[] first_unique = new String[]{"one", "two", "three"};
        final String[] second_unique = new String[]{"one2", "two2", "three2", "four2"};
        final String[] third_unique = new String[]{"one3", "two3"};

        assertSame(bean, bean.unique(first_unique));
        assertTrue(bean.hasUniques());
        assertNotNull(bean.getUniques());
        assertEquals(1, bean.getUniques().size());
        assertSame(first_unique, bean.getUniques().get(0));

        assertSame(bean, bean.unique(second_unique));
        assertTrue(bean.hasUniques());
        assertEquals(2, bean.getUniques().size());
        assertSame(first_unique, bean.getUniques().get(0));
        assertSame(second_unique, bean.getUniques().get(1));

        assertSame(bean, bean.unique(third_unique));
        assertTrue(bean.hasUniques());
        assertEquals(3, bean.getUniques().size());
        assertSame(first_unique, bean.getUniques().get(0));
        assertSame(second_unique, bean.getUniques().get(1));
        assertSame(third_unique, bean.getUniques().get(2));

        assertSame(bean, bean.uniques(null));
        assertFalse(bean.hasUniques());
        assertNull(bean.getUniques());

        assertSame(bean, bean.uniques(new ArrayList<String[]>() {{
            add(first_unique);
            add(second_unique);
            add(third_unique);
        }}));
        assertTrue(bean.hasUniques());
        assertEquals(3, bean.getUniques().size());
        assertSame(first_unique, bean.getUniques().get(0));
        assertSame(second_unique, bean.getUniques().get(1));
        assertSame(third_unique, bean.getUniques().get(2));
    }

    @Test
    void testTextualIdentifier() {
        TextualIdentifierGenerator<InitializedBeanImpl> identifier = new TextualIdentifierGenerator<InitializedBeanImpl>() {
            public void setBean(InitializedBeanImpl bean) {
            }

            public String generateIdentifier() {
                return null;
            }
        };

        ConstrainedBean bean = new ConstrainedBean();
        assertSame(bean, bean.textualIdentifier(identifier));
        assertTrue(bean.hasTextualIdentifier());
        assertSame(identifier, bean.getTextualIdentifier());

        assertSame(bean, bean.textualIdentifier(null));
        assertFalse(bean.hasTextualIdentifier());
        assertNull(bean.getTextualIdentifier());
    }

    @Test
    void testDefaultOrdering() {
        ConstrainedBean bean = new ConstrainedBean();
        assertSame(bean, bean.defaultOrder("col1"));
        assertTrue(bean.hasDefaultOrdering());
        assertEquals(1, bean.getDefaultOrdering().size());
        assertEquals("col1", ((ConstrainedBean.Order) bean.getDefaultOrdering().get(0)).getPropertyName());
        assertSame(ConstrainedBean.ASC, ((ConstrainedBean.Order) bean.getDefaultOrdering().get(0)).getDirection());
        assertSame(bean, bean.defaultOrder("col2", ConstrainedBean.DESC));
        assertTrue(bean.hasDefaultOrdering());
        assertEquals(2, bean.getDefaultOrdering().size());
        assertEquals("col2", ((ConstrainedBean.Order) bean.getDefaultOrdering().get(1)).getPropertyName());
        assertSame(ConstrainedBean.DESC, ((ConstrainedBean.Order) bean.getDefaultOrdering().get(1)).getDirection());

        assertSame(bean, bean.defaultOrdering(null));
        assertFalse(bean.hasDefaultOrdering());
        assertNull(bean.getDefaultOrdering());

        List<ConstrainedBean.Order> ordering = new ArrayList<ConstrainedBean.Order>();
        ordering.add(new ConstrainedBean.Order("col3", ConstrainedBean.DESC));
        ordering.add(new ConstrainedBean.Order("col4", ConstrainedBean.ASC));
        ordering.add(new ConstrainedBean.Order("col5", ConstrainedBean.DESC));
        assertSame(bean, bean.defaultOrdering(ordering));
        assertTrue(bean.hasDefaultOrdering());
        assertEquals(3, bean.getDefaultOrdering().size());
        assertEquals("col3", ((ConstrainedBean.Order) bean.getDefaultOrdering().get(0)).getPropertyName());
        assertSame(ConstrainedBean.DESC, ((ConstrainedBean.Order) bean.getDefaultOrdering().get(0)).getDirection());
        assertEquals("col4", ((ConstrainedBean.Order) bean.getDefaultOrdering().get(1)).getPropertyName());
        assertSame(ConstrainedBean.ASC, ((ConstrainedBean.Order) bean.getDefaultOrdering().get(1)).getDirection());
        assertEquals("col5", ((ConstrainedBean.Order) bean.getDefaultOrdering().get(2)).getPropertyName());
        assertSame(ConstrainedBean.DESC, ((ConstrainedBean.Order) bean.getDefaultOrdering().get(2)).getDirection());
    }
}
