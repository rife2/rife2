/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.textstoredrivers;

import rife.cmf.Content;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentstores.DatabaseTextStore;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.*;
import rife.engine.Context;

public class generic extends DatabaseTextStore {
    protected CreateTable createTableContent_;
    protected DropTable dropTableContent_;
    protected Insert storeContentData_;
    protected Delete deleteContentData_;
    protected Select retrieveContent_;
    protected Select retrieveSize_;
    protected Select hasContentData_;

    public generic(Datasource datasource) {
        super(datasource);

        createTableContent_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreText())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .column("content", String.class)
            .primaryKey(("PK_" + RifeConfig.cmf().getTableContentStoreText()).toUpperCase(), "contentId")
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentStoreText()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        dropTableContent_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreText());

        storeContentData_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentStoreText())
            .fieldParameter("contentId")
            .fieldParameter("contentSize")
            .fieldParameter("content");

        deleteContentData_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreText())
            .whereParameter("contentId", "=");

        retrieveContent_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreText())
            .field("content")
            .field("contentSize")
            .whereParameter("contentId", "=");

        retrieveSize_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreText())
            .field("contentSize")
            .whereParameter("contentId", "=");

        hasContentData_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreText())
            .field("contentId")
            .whereParameter("contentId", "=")
            .whereAnd("contentSize", "!=", 0);
    }

    @Override
    public boolean install()
    throws ContentManagerException {
        return _install(createTableContent_);
    }

    @Override
    public boolean remove()
    throws ContentManagerException {
        return _remove(dropTableContent_);
    }

    @Override
    public boolean storeContentData(int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        return _storeContentData(storeContentData_, id, content, transformer);
    }

    @Override
    public boolean deleteContentData(int id)
    throws ContentManagerException {
        return _deleteContentData(deleteContentData_, id);
    }

    @Override
    public void useContentData(int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        _useContentData(retrieveContent_, id, user);
    }

    @Override
    public <ResultType> ResultType useContentDataResult(int id, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        return _useContentDataResult(retrieveContent_, id, user);
    }

    @Override
    public int getSize(int id)
    throws ContentManagerException {
        return _getSize(retrieveSize_, id);
    }

    @Override
    public boolean hasContentData(int id)
    throws ContentManagerException {
        return _hasContentData(hasContentData_, id);
    }

    @Override
    public void serveContentData(Context context, int id)
    throws ContentManagerException {
        _serveContentData(retrieveContent_, context, id);
    }
}
