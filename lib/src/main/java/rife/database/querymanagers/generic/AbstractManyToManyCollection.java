/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import static rife.database.querymanagers.generic.GenericQueryManagerRelationalUtils.generateManyToManyJoinColumnName;
import static rife.database.querymanagers.generic.GenericQueryManagerRelationalUtils.generateManyToManyJoinTableName;

import java.util.Collection;
import java.util.List;

import rife.database.queries.Select;

abstract class AbstractManyToManyCollection<E> implements Collection<E> {
    private final AbstractGenericQueryManager queryManager_;
    private final String columnName1_;
    private final int objectId_;
    private final ManyToManyDeclaration declaration_;

    AbstractManyToManyCollection(AbstractGenericQueryManager manager, String columnName1, int objectId, ManyToManyDeclaration declaration) {
        queryManager_ = manager;
        columnName1_ = columnName1;
        objectId_ = objectId;
        declaration_ = declaration;
    }

    protected List restoreManyToManyMappings() {
        GenericQueryManager association_manager = queryManager_.createNewManager(declaration_.getAssociationType());
        String join_table = generateManyToManyJoinTableName(declaration_, queryManager_, association_manager);
        final String column2_name = generateManyToManyJoinColumnName(association_manager);

        RestoreQuery restore_mappings = association_manager.getRestoreQuery()
            .fields(declaration_.getAssociationType())
            .joinInner(join_table, Select.ON, association_manager.getTable() + "." + association_manager.getIdentifierName() + " = " + join_table + "." + column2_name)
            .where(join_table + "." + columnName1_, "=", objectId_);

        // restore the many to many associations
        return association_manager.restore(restore_mappings);
    }
}
