/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.rawstoredrivers;

import rife.cmf.Content;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.cmf.dam.contentstores.DatabaseRawStore;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.*;
import rife.engine.Context;

import java.sql.Blob;

public class generic extends DatabaseRawStore {
    protected CreateTable createTableContentInfo_;
    protected CreateTable createTableContentChunk_;
    protected DropTable dropTableContentInfo_;
    protected DropTable dropTableContentChunk_;
    protected Insert storeContentInfo_;
    protected Delete deleteContentInfo_;
    protected Select retrieveSize_;
    protected Select hasContentData_;
    protected Insert storeContentChunk_;
    protected Delete deleteContentChunk_;
    protected Select retrieveContentChunks_;

    public generic(Datasource datasource) {
        super(datasource);

        createTableContentInfo_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawInfo())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .primaryKey(("PK_" + RifeConfig.cmf().getTableContentStoreRawInfo()).toUpperCase(), "contentId")
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentStoreRawInfo()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        createTableContentChunk_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawChunk())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("ordinal", int.class, CreateTable.NOTNULL)
            .column("chunk", Blob.class)
            .primaryKey(("PK_" + RifeConfig.cmf().getTableContentStoreRawChunk()).toUpperCase(), new String[]{"contentId", "ordinal"})
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentStoreRawChunk()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        dropTableContentInfo_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawInfo());

        dropTableContentChunk_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawChunk());

        storeContentInfo_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentStoreRawInfo())
            .fieldParameter("contentId")
            .fieldParameter("contentSize");

        deleteContentInfo_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreRawInfo())
            .whereParameter("contentId", "=");

        retrieveSize_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreRawInfo())
            .field("contentSize")
            .whereParameter("contentId", "=");

        hasContentData_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreRawInfo())
            .field("contentId")
            .whereParameter("contentId", "=")
            .whereAnd("contentSize", "!=", 0);

        storeContentChunk_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentStoreRawChunk())
            .fieldParameter("contentId")
            .fieldParameter("ordinal")
            .fieldParameter("chunk");

        deleteContentChunk_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreRawChunk())
            .whereParameter("contentId", "=");

        retrieveContentChunks_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentStoreRawChunk())
            .field("chunk")
            .whereParameter("contentId", "=")
            .orderBy("ordinal");
    }

    public boolean install()
    throws ContentManagerException {
        return _install(createTableContentInfo_, createTableContentChunk_);
    }

    public boolean remove()
    throws ContentManagerException {
        return _remove(dropTableContentInfo_, dropTableContentChunk_);
    }

    public boolean storeContentData(int id, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        return _storeContentData(storeContentInfo_, storeContentChunk_, id, content, transformer);
    }

    public boolean deleteContentData(int id)
    throws ContentManagerException {
        return _deleteContentData(deleteContentInfo_, deleteContentChunk_, id);
    }

    public void useContentData(int id, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        _useContentData(retrieveContentChunks_, id, user);
    }

    public <ResultType> ResultType useContentDataResult(int id, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        return _useContentDataResult(retrieveContentChunks_, id, user);
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
        _serveContentData(retrieveContentChunks_, context, id);
    }
}
