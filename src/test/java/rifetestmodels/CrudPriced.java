/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class CrudPriced extends MetaData {
    private int id_ = -1;
    private String name_ = null;
    private double price_ = 0.0;
    private int stock_ = 0;
    private int tier_ = 0;
    private Boolean approved_ = null;
    private boolean listedInShop_ = false;
    private Boolean shipping_ = null;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("name").notNull(true).maxLength(60).listed(true).position(0));
        addConstraint(new ConstrainedProperty("price").format(new DecimalFormat("$#,##0.00")).listed(true).position(1));
        addConstraint(new ConstrainedProperty("stock").listed(true).position(2));
        addConstraint(new ConstrainedProperty("tier").inList("1000", "2000").format(new DecimalFormat("$#,##0")).listed(true).position(3));
        // deliberately nullable, so that not having been decided about is
        // not the same as having been refused
        addConstraint(new ConstrainedProperty("approved").format(new DecisionFormat()).listed(true).position(4));
        addConstraint(new ConstrainedProperty("listedInShop").format(new BooleanFormat()).listed(true).position(5));
        addConstraint(new ConstrainedProperty("shipping").inList("true", "false").format(new BooleanFormat()).listed(true).position(6));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public void setPrice(double price) {
        price_ = price;
    }

    public double getPrice() {
        return price_;
    }

    public void setStock(int stock) {
        stock_ = stock;
    }

    /**
     * Writes a boolean in words of its own, so that what a control says can
     * be told apart from what it submits.
     */
    public static class DecisionFormat extends Format {
        public StringBuffer format(Object object, StringBuffer buffer, FieldPosition position) {
            return buffer.append(Boolean.TRUE.equals(object) ? "Approved" : "Refused");
        }

        public Object parseObject(String source, ParsePosition position) {
            position.setIndex(source.length());
            return "Approved".equals(source);
        }
    }

    /**
     * Writes a boolean the way the browse table should read, which is
     * not the same as the value that a form submits.
     */
    public static class BooleanFormat extends Format {
        public StringBuffer format(Object object, StringBuffer buffer, FieldPosition position) {
            return buffer.append(Boolean.TRUE.equals(object) ? "Yes" : "No");
        }

        public Object parseObject(String source, ParsePosition position) {
            position.setIndex(source.length());
            return "Yes".equals(source) || "true".equals(source);
        }
    }

    public int getStock() {
        return stock_;
    }

    public void setTier(int tier) {
        tier_ = tier;
    }

    public int getTier() {
        return tier_;
    }

    public void setApproved(Boolean approved) {
        approved_ = approved;
    }

    public Boolean getApproved() {
        return approved_;
    }

    public void setListedInShop(boolean listedInShop) {
        listedInShop_ = listedInShop;
    }

    public boolean isListedInShop() {
        return listedInShop_;
    }

    public void setShipping(Boolean shipping) {
        shipping_ = shipping;
    }

    public Boolean getShipping() {
        return shipping_;
    }

}
