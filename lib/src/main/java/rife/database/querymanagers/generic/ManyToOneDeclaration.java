/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

public class ManyToOneDeclaration {
    private boolean isBasic_ = true;
    private Class associationType_;
    private String associationTable_;
    private String associationColumn_;
    private GenericQueryManager associationManager_;

    public void setIsBasic(boolean isBasic) {
        isBasic_ = isBasic;
    }

    public ManyToOneDeclaration isBasic(boolean isBasic) {
        setIsBasic(isBasic);

        return this;
    }

    public boolean isBasic() {
        return isBasic_;
    }

    public void setAssociationType(Class type) {
        associationType_ = type;
    }

    public ManyToOneDeclaration associationType(Class type) {
        setAssociationType(type);

        return this;
    }

    public Class getAssociationType() {
        return associationType_;
    }

    public void setAssociationTable(String associationTable) {
        associationTable_ = associationTable;
    }

    public ManyToOneDeclaration associationTable(String associationTable) {
        setAssociationTable(associationTable);

        return this;
    }

    public String getAssociationTable() {
        return associationTable_;
    }

    public void setAssociationColumn(String associationColumn) {
        associationColumn_ = associationColumn;
    }

    public ManyToOneDeclaration associationColumn(String associationColumn) {
        setAssociationColumn(associationColumn);

        return this;
    }

    public String getAssociationColumn() {
        return associationColumn_;
    }

    public void setAssociationManager(GenericQueryManager associationManager) {
        associationManager_ = associationManager;
    }

    public ManyToOneDeclaration associationManager(GenericQueryManager associationManager) {
        setAssociationManager(associationManager);

        return this;
    }

    public GenericQueryManager getAssociationManager() {
        return associationManager_;
    }
}
