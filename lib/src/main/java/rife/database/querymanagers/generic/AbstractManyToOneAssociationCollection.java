/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import java.util.Collection;
import java.util.List;

import static rife.database.querymanagers.generic.GenericQueryManagerRelationalUtils.generateManyToOneJoinColumnName;

abstract class AbstractManyToOneAssociationCollection<E> implements Collection<E> {
    private final AbstractGenericQueryManager queryManager_;
    private final int objectId_;
    private ManyToOneAssociationDeclaration declaration_;

    AbstractManyToOneAssociationCollection(AbstractGenericQueryManager manager, int objectId, ManyToOneAssociationDeclaration declaration) {
        queryManager_ = manager;
        objectId_ = objectId;
        declaration_ = declaration;
    }

    protected List restoreManyToOneAssociations() {
        GenericQueryManager association_manager = queryManager_.createNewManager(declaration_.getMainType());

        RestoreQuery restore_mappings = association_manager.getRestoreQuery()
            .fields(declaration_.getMainType())
            .where(generateManyToOneJoinColumnName(declaration_.getMainProperty(), declaration_.getMainDeclaration()), "=", objectId_);

        // restore the many to one associations
        return association_manager.restore(restore_mappings);
    }
}
