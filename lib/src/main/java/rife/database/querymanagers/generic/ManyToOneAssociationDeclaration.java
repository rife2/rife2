/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

public class ManyToOneAssociationDeclaration {
    private Class mainType_;
    private String mainProperty_;
    private Class collectionType_;
    private ManyToOneDeclaration mainDeclaration_;

    public void setMainType(Class type) {
        mainType_ = type;
    }

    public ManyToOneAssociationDeclaration mainType(Class type) {
        setMainType(type);

        return this;
    }

    public Class getMainType() {
        return mainType_;
    }

    public void setMainProperty(String mainProperty) {
        mainProperty_ = mainProperty;
    }

    public ManyToOneAssociationDeclaration mainProperty(String mainProperty) {
        setMainProperty(mainProperty);
        return this;
    }

    public String getMainProperty() {
        return mainProperty_;
    }

    public void setCollectionType(Class type) {
        collectionType_ = type;
    }

    public ManyToOneAssociationDeclaration collectionType(Class type) {
        setCollectionType(type);

        return this;
    }

    public Class getCollectionType() {
        return collectionType_;
    }

    public void setMainDeclaration(ManyToOneDeclaration mainDeclaration) {
        mainDeclaration_ = mainDeclaration;
    }

    public ManyToOneAssociationDeclaration mainDeclaration(ManyToOneDeclaration mainDeclaration) {
        setMainDeclaration(mainDeclaration);

        return this;
    }

    public ManyToOneDeclaration getMainDeclaration() {
        return mainDeclaration_;
    }
}
