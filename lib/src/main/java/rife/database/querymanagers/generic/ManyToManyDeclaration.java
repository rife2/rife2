/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

public class ManyToManyDeclaration {
    private Class associationType_;
    private Class collectionType_;
    private boolean reversed_;

    public void setAssociationType(Class type) {
        associationType_ = type;
    }

    public ManyToManyDeclaration associationType(Class type) {
        setAssociationType(type);

        return this;
    }

    public Class getAssociationType() {
        return associationType_;
    }

    public void setCollectionType(Class type) {
        collectionType_ = type;
    }

    public ManyToManyDeclaration collectionType(Class type) {
        setCollectionType(type);

        return this;
    }

    public Class getCollectionType() {
        return collectionType_;
    }

    public void setReversed(boolean reversed) {
        reversed_ = reversed;
    }

    public ManyToManyDeclaration reversed(boolean reversed) {
        setReversed(reversed);

        return this;
    }

    public boolean isReversed() {
        return reversed_;
    }
}
