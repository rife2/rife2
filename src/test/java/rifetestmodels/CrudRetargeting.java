/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.database.querymanagers.generic.Callbacks;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

/**
 * A bean whose callbacks point it at another row than the one that was asked
 * for, which is what the administration has to keep working through.
 */
public class CrudRetargeting extends MetaData implements Callbacks<CrudRetargeting> {
    public static int retargetTo = -1;
    public static int scopeTo = -1;
    public static int retargetAfterStoring = -1;
    public static int scopeAfterStoring = -1;

    private int id_ = -1;
    private String title_ = null;
    private int section_ = 1;
    private int ordinal_ = -1;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        addConstraint(new ConstrainedProperty("title").notNull(true).notEmpty(true).maxLength(60).listed(true));
        addConstraint(new ConstrainedProperty("section").notNull(true).listed(true));
        addConstraint(new ConstrainedProperty("ordinal").ordinal(true, "section"));
    }

    public void setId(int id) { id_ = id; }
    public int getId() { return id_; }
    public void setTitle(String title) { title_ = title; }
    public String getTitle() { return title_; }
    public void setSection(int section) { section_ = section; }
    public int getSection() { return section_; }
    public void setOrdinal(int ordinal) { ordinal_ = ordinal; }
    public int getOrdinal() { return ordinal_; }

    public boolean afterRestore(CrudRetargeting object) {
        if (retargetTo >= 0) {
            object.setId(retargetTo);
        }
        if (scopeTo >= 0) {
            object.setSection(scopeTo);
        }
        return true;
    }

    public boolean beforeValidate(CrudRetargeting object) { return true; }
    public boolean beforeInsert(CrudRetargeting object) { return true; }
    public boolean beforeDelete(int objectId) { return true; }
    public boolean beforeSave(CrudRetargeting object) { return true; }
    public boolean beforeUpdate(CrudRetargeting object) { return true; }
    public boolean afterValidate(CrudRetargeting object) { return true; }
    public boolean afterInsert(CrudRetargeting object, boolean success) {
        if (retargetAfterStoring >= 0) {
            object.setId(retargetAfterStoring);
        }
        if (scopeAfterStoring >= 0) {
            object.setSection(scopeAfterStoring);
            object.setOrdinal(999);
        }
        return true;
    }
    public boolean afterDelete(int objectId, boolean success) { return true; }
    public boolean afterSave(CrudRetargeting object, boolean success) { return true; }
    public boolean afterUpdate(CrudRetargeting object, boolean success) {
        if (retargetAfterStoring >= 0) {
            object.setId(retargetAfterStoring);
        }
        if (scopeAfterStoring >= 0) {
            object.setSection(scopeAfterStoring);
            object.setOrdinal(999);
        }
        return true;
    }
}
