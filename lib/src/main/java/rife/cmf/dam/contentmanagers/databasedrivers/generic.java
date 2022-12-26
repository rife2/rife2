/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentmanagers.databasedrivers;

import rife.cmf.dam.ContentDataUserWithoutResult;
import rife.database.queries.*;

import rife.cmf.Content;
import rife.cmf.ContentRepository;
import rife.cmf.dam.ContentDataUser;
import rife.cmf.dam.contentmanagers.DatabaseContent;
import rife.cmf.dam.contentmanagers.DatabaseContentInfo;
import rife.cmf.dam.contentmanagers.exceptions.InstallContentErrorException;
import rife.cmf.dam.contentmanagers.exceptions.RemoveContentErrorException;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.exceptions.DatabaseException;
import rife.engine.Context;
import rife.engine.Route;

public class generic extends DatabaseContent {
    protected CreateSequence createSequenceContentRepository_;
    protected CreateSequence createSequenceContentInfo_;
    protected CreateTable createTableContentRepository_;
    protected CreateTable createTableContentInfo_;
    protected CreateTable createTableContentAttribute_;
    protected CreateTable createTableContentProperty_;
    protected String createContentInfoPathIndex_;
    protected String createContentInfoPathNameIndex_;
    protected DropSequence dropSequenceContentRepository_;
    protected DropSequence dropSequenceContentInfo_;
    protected DropTable dropTableContentRepository_;
    protected DropTable dropTableContentInfo_;
    protected DropTable dropTableContentAttribute_;
    protected DropTable dropTableContentProperties_;
    protected String dropContentInfoPathIndex_;
    protected String dropContentInfoPathNameIndex_;
    protected SequenceValue getNewContentRepositoryId_;
    protected SequenceValue getNewContentId_;
    protected Select getContentRepositoryId_;
    protected Select getContentInfo_;
    protected Select getVersion_;
    protected Insert storeContentRepository_;
    protected Select containsContentRepository_;
    protected Insert storeContentInfo_;
    protected Insert storeContentAttribute_;
    protected Insert storeContentProperty_;
    protected Delete deleteContentInfo_;
    protected Delete deleteContentAttributes_;
    protected Delete deleteContentProperties_;
    protected Select getLatestContentInfo_;
    protected Select getContentAttributes_;
    protected Select getContentProperties_;

    public generic(Datasource datasource) {
        super(datasource);

        createSequenceContentRepository_ = new CreateSequence(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentRepository());

        createSequenceContentInfo_ = new CreateSequence(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentInfo());

        createTableContentRepository_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentRepository())
            .columns(ContentRepository.class)
            .column("repositoryId", int.class)
            .primaryKey("PK_" + RifeConfig.cmf().getTableContentRepository(), "repositoryId");

        createTableContentInfo_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentInfo())
            .columns(DatabaseContentInfo.class)
            .column("repositoryId", int.class, CreateTable.NOTNULL)
            .defaultFunction("created", "CURRENT_TIMESTAMP")
            .unique(("UQ_" + RifeConfig.cmf().getTableContentInfo()).toUpperCase(), new String[]{"repositoryId", "path", "version"})
            .foreignKey("FK_" + RifeConfig.cmf().getTableContentInfo() + "_REPOSITORYID", RifeConfig.cmf().getTableContentRepository(), "repositoryId", "repositoryId");

        createTableContentAttribute_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentAttribute())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("name", String.class, 255, CreateTable.NOTNULL)
            .column("attVal", String.class, 255, CreateTable.NOTNULL)
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentAttribute()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        createTableContentProperty_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentProperty())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("name", String.class, 255, CreateTable.NOTNULL)
            .column("propVal", String.class, 255, CreateTable.NOTNULL)
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentProperty()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        createContentInfoPathIndex_ = "CREATE INDEX " + RifeConfig.cmf().getTableContentInfo() + "_path ON " + RifeConfig.cmf().getTableContentInfo() + " (path)";
        createContentInfoPathNameIndex_ = "CREATE INDEX " + RifeConfig.cmf().getTableContentInfo() + "_pathname ON " + RifeConfig.cmf().getTableContentInfo() + " (path, name)";

        dropSequenceContentRepository_ = new DropSequence(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentRepository());

        dropSequenceContentInfo_ = new DropSequence(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentInfo());

        dropTableContentRepository_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentRepository());

        dropTableContentInfo_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentInfo());

        dropTableContentAttribute_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentAttribute());

        dropTableContentProperties_ = new DropTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentProperty());

        dropContentInfoPathIndex_ = "DROP INDEX " + RifeConfig.cmf().getTableContentInfo() + "_path";

        dropContentInfoPathNameIndex_ = "DROP INDEX " + RifeConfig.cmf().getTableContentInfo() + "_pathname";

        getNewContentRepositoryId_ = new SequenceValue(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentRepository())
            .next();

        getNewContentId_ = new SequenceValue(getDatasource())
            .name(RifeConfig.cmf().getSequenceContentInfo())
            .next();

        getContentRepositoryId_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentRepository())
            .field("repositoryId")
            .whereParameter("name", "repository", "=");

        getVersion_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .field("COALESCE(MAX(version)+1, 0)")
            .whereParameter("repositoryId", "=")
            .whereParameterAnd("path", "=");

        getContentInfo_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .join(RifeConfig.cmf().getTableContentRepository())
            .field(RifeConfig.cmf().getTableContentInfo() + ".*")
            .where(RifeConfig.cmf().getTableContentInfo() + ".repositoryId = " + RifeConfig.cmf().getTableContentRepository() + ".repositoryId")
            .whereParameter("path", "=")
            .whereParameterAnd(RifeConfig.cmf().getTableContentRepository() + ".name", "repository", "=")
            .orderBy("version", Select.DESC);

        storeContentRepository_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentRepository())
            .fieldsParameters(ContentRepository.class)
            .fieldParameter("repositoryId");

        containsContentRepository_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentRepository())
            .field("count(1)")
            .whereParameter("name", "=");

        storeContentInfo_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentInfo())
            .fieldsParameters(DatabaseContentInfo.class)
            .fieldParameter("repositoryId")
            .field("version", getVersion_);

        storeContentAttribute_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentAttribute())
            .fieldParameter("contentId")
            .fieldParameter("name")
            .fieldParameter("attVal");

        storeContentProperty_ = new Insert(getDatasource())
            .into(RifeConfig.cmf().getTableContentProperty())
            .fieldParameter("contentId")
            .fieldParameter("name")
            .fieldParameter("propVal");

        deleteContentInfo_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .whereParameter("contentId", "=");

        deleteContentAttributes_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentAttribute())
            .whereParameter("contentId", "=");

        deleteContentProperties_ = new Delete(getDatasource())
            .from(RifeConfig.cmf().getTableContentProperty())
            .whereParameter("contentId", "=");

        getLatestContentInfo_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentInfo())
            .join(RifeConfig.cmf().getTableContentRepository())
            .field(RifeConfig.cmf().getTableContentInfo() + ".*")
            .where(RifeConfig.cmf().getTableContentInfo() + ".repositoryId = " + RifeConfig.cmf().getTableContentRepository() + ".repositoryId")
            .whereParameterAnd(RifeConfig.cmf().getTableContentRepository() + ".name", "repository", "=")
            .startWhereAnd()
                .whereParameter("path", "=")
                .startWhereOr()
                    .whereParameter("path", "pathpart", "=")
                    .whereParameterAnd(RifeConfig.cmf().getTableContentInfo() + ".name", "namepart", "=")
                .end()
            .end()
            .orderBy("version", Select.DESC)
            .limit(1);

        getContentAttributes_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentAttribute())
            .field("contentId")
            .field("name")
            .field("attVal")
            .whereParameter("contentId", "=");

        getContentProperties_ = new Select(getDatasource())
            .from(RifeConfig.cmf().getTableContentProperty())
            .field("contentId")
            .field("name")
            .field("propVal")
            .whereParameter("contentId", "=");
    }

    public boolean install()
    throws ContentManagerException {
        boolean result = _install(createSequenceContentRepository_, createSequenceContentInfo_,
            createTableContentRepository_, createTableContentInfo_, createTableContentAttribute_, createTableContentProperty_);
        try {
            executeUpdate(createContentInfoPathIndex_);
            executeUpdate(createContentInfoPathNameIndex_);
        } catch (DatabaseException e) {
            throw new InstallContentErrorException(e);
        }
        return result;
    }

    public boolean remove()
    throws ContentManagerException {
        try {
            executeUpdate(dropContentInfoPathNameIndex_);
            executeUpdate(dropContentInfoPathIndex_);
        } catch (DatabaseException e) {
            throw new RemoveContentErrorException(e);
        }
        return _remove(dropSequenceContentRepository_, dropSequenceContentInfo_,
            dropTableContentRepository_, dropTableContentInfo_, dropTableContentAttribute_, dropTableContentProperties_);
    }

    public boolean createRepository(String name)
    throws ContentManagerException {
        return _createRepository(getNewContentRepositoryId_, storeContentRepository_, name);
    }

    public boolean containsRepository(String name)
    throws ContentManagerException {
        return _containsRepository(containsContentRepository_, name);
    }

    public boolean storeContent(String location, Content content, ContentTransformer transformer)
    throws ContentManagerException {
        return _storeContent(getNewContentId_, getContentRepositoryId_, storeContentInfo_, storeContentAttribute_, storeContentProperty_, location, content, transformer);
    }

    public boolean deleteContent(String location)
    throws ContentManagerException {
        return _deleteContent(getContentInfo_, deleteContentInfo_, deleteContentAttributes_, deleteContentProperties_, location);
    }

    public void useContentData(String location, ContentDataUserWithoutResult user)
    throws ContentManagerException {
        _useContentData(getLatestContentInfo_, location, user);
    }

    public <ResultType> ResultType useContentDataResult(String location, ContentDataUser<ResultType> user)
    throws ContentManagerException {
        return _useContentDataResult(getLatestContentInfo_, location, user);
    }

    public boolean hasContentData(String location)
    throws ContentManagerException {
        return _hasContentData(getLatestContentInfo_, location);
    }

    public void serveContentData(Context context, String location)
    throws ContentManagerException {
        _serveContentData(context, location);
    }

    public DatabaseContentInfo getContentInfo(String location)
    throws ContentManagerException {
        return _getContentInfo(getLatestContentInfo_, getContentAttributes_, getContentProperties_, location);
    }

    public String getContentForHtml(String location, Context context, Route route)
    throws ContentManagerException {
        return _getContentForHtml(location, context, route);
    }
}
