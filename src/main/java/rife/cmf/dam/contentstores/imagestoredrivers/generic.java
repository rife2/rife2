/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.imagestoredrivers;

import rife.cmf.Content;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentstores.DatabaseImageStore;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;
import rife.database.queries.Delete;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.engine.Context;

import java.sql.Blob;

public class generic extends DatabaseImageStore {
    protected CreateTable createTableContent_;
    protected final DropTable dropTableContent_;
    protected final Insert storeContentData_;
    protected final Delete deleteContentData_;
    protected final Select retrieveContent_;
    protected final Select retrieveSize_;
    protected final Select hasContentData_;

    public generic(Datasource datasource) {
        super(datasource);

        createTableContent_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreImage())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .column("content", Blob.class)
            .primaryKey(("PK_" + RifeConfig.cmf().getTableContentStoreImage()).toUpperCase(), "contentId")
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentStoreImage()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        dropTableContent_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreImage());

        storeContentData_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentStoreImage())
            .fieldParameter("contentId")
            .fieldParameter("contentSize")
            .fieldParameter("content");

        deleteContentData_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreImage())
            .whereParameter("contentId", "=");

        retrieveContent_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreImage())
            .field("content")
            .field("contentSize")
            .whereParameter("contentId", "=");

        retrieveSize_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreImage())
            .field("contentSize")
            .whereParameter("contentId", "=");

        hasContentData_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreImage())
            .field("contentId")
            .whereParameter("contentId", "=")
            .whereAnd("contentSize", "!=", 0);
    }

    public boolean install()
    throws ContentManagerException {
        return _install(createTableContent_);
    }

    public boolean remove()
    throws ContentManagerException {
        return _remove(dropTableContent_);
    }

    public boolean storeContentData(int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        return _storeContentData(storeContentData_, id, content, transformer);
    }

    public boolean deleteContentData(int id)
    throws ContentManagerException {
        return _deleteContentData(deleteContentData_, id);
    }

    public void useContentData(int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        _useContentData(retrieveContent_, id, user);
    }

    public <ResultType> ResultType useContentDataResult(int id, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        return _useContentDataResult(retrieveContent_, id, user);
    }

    public int getSize(int id)
    throws ContentManagerException {
        return _getSize(retrieveSize_, id);
    }

    public boolean hasContentData(int id)
    throws ContentManagerException {
        return _hasContentData(hasContentData_, id);
    }

    public void serveContentData(Context context, int id)
    throws ContentManagerException {
        _serveContentData(retrieveContent_, context, id);
    }
}
