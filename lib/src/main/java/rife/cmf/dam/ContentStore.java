/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.MimeType;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.format.Formatter;
import rife.cmf.transform.ContentTransformer;
import rife.engine.Context;
import rife.engine.Route;

import java.util.Collection;

/**
 * A <code>ContentStore</code> stores the actual content data and is
 * responsible for managing it.
 * <p>The store doesn't work with paths, but with content ids. Each id
 * identifies a specific content instance at a certain location and with a
 * certain version number.
 * <p>Each store is only capable of storing content with certain mime types.
 * The store is optimized for a certain kind of content and will maybe not be
 * able to correctly handle other types.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)i
 * @since 1.0
 */
public interface ContentStore {
    /**
     * Installs a content store.
     *
     * @return <code>true</code> if the installation was successful; or
     * <p><code>false</code> if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean install()
    throws ContentManagerException;

    /**
     * Removes a content store.
     *
     * @return <code>true</code> if the removal was successful; or
     * <p><code>false</code> if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean remove()
    throws ContentManagerException;

    /**
     * Returns the collection of mime types that the content store supports.
     *
     * @return the collection of supported mime types
     * @since 1.0
     */
    Collection<MimeType> getSupportedMimeTypes();

    /**
     * Generates the HTTP content type that corresponds best to the
     * information in the provided <code>ContentInfo</code>.
     *
     * @param contentInfo the content info instance for which the content type
     *                    has to be generated
     * @return the generated content type
     * @since 1.0
     */
    String getContentType(ContentInfo contentInfo);

    /**
     * Returns a <code>Formatter</code> instance that will be used to load and
     * to format the content data.
     *
     * @param mimeType the mime type for which the formatter will be returned
     * @param fragment <code>true</code> if the content that has to be
     *                 formatter is a fragment; or
     *                 <p><code>false</code> otherwise
     * @return the corresponding formatter
     * @since 1.0
     */
    Formatter getFormatter(MimeType mimeType, boolean fragment);

    /**
     * Stores the content data for a certain content id.
     *
     * @param id          the id of the content whose data will be stored
     * @param content     the content whose data has to be stored
     * @param transformer a transformer that will modify the content data; or
     *                    <p><code>null</code> if the content data should stay intact
     * @return <code>true</code> if the storing was successfully; or
     * <p><code>false</code> if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean storeContentData(int id, Content content, ContentTransformer transformer)
    throws ContentManagerException;

    /**
     * Deletes the content data for a certain content id.
     *
     * @param id the id of the content whose data will be deleted
     * @return <code>true</code> if the deletion was successfully; or
     * <p><code>false</code> if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean deleteContentData(int id)
    throws ContentManagerException;

    /**
     * Use the data of a certain content id.
     * <p>Some content data will only be available during this method call due
     * to their volatile nature (certain streams for instance). Therefore, one
     * has to be careful when trying to move the data that is provided to the
     * content user outside this method. The behaviour is undefined.
     *
     * @param id   the id of the content whose data will be used
     * @param user the content user instance that will be called to use
     *             content data
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    void useContentData(int id, ContentDataUserWithoutResult user)
    throws ContentManagerException;

    /**
     * Use the data of a certain content id.
     * <p>Some content data will only be available during this method call due
     * to their volatile nature (certain streams for instance). Therefore, one
     * has to be careful when trying to move the data that is provided to the
     * content user outside this method. The behaviour is undefined.
     *
     * @param id   the id of the content whose data will be used
     * @param user the content user instance that will be called to use
     *             content data
     * @return the data that the {@link ContentDataUser#useContentData(Object)}
     * returns after its usage
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    <ResultType> ResultType useContentDataResult(int id, ContentDataUser<ResultType> user)
    throws ContentManagerException;

    /**
     * Checks whether content data is available for a certain content id.
     *
     * @param id the id of the content whose data availability will be checked
     * @return <code>true</code> if content data is available; or
     * <p><code>false</code> if it isn't.
     * @throws ContentManagerException if an expected error occurred
     * @since 1.0
     */
    boolean hasContentData(int id)
    throws ContentManagerException;

    /**
     * Retrieves the size of the content data for a certain content id.
     * <p>Note that the result is specific to the data store. For instance,
     * text data could return the number of characters, while image data could
     * return the number of bytes.
     *
     * @param id the id of the content whose data size will be returned
     * @return <code>-1</code> if no data is available for the provided
     * content id; or
     * <p>the requested content data size.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    int getSize(int id)
    throws ContentManagerException;

    /**
     * Serves content data for a certain content id through the provided
     * element.
     * <p>This is intended to take over the complete handling of the request,
     * so no other content should be output and no headers manipulated in the
     * element if this method is called.
     *
     * @param context an active web engine context
     * @param id      the id of the content whose data will be served
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    void serveContentData(Context context, int id)
    throws ContentManagerException;

    /**
     * Retrieves a content data representation for use in html.
     * <p>This is mainly used to integrate content data inside a html
     * document. For instance, html content will be displayed as-is, while
     * image content will cause an image tag to be generated with the correct
     * source URL to serve the image.
     *
     * @param id      the id of the content whose data will be displayed
     * @param info    the content info instance for which the html content
     *                has to be generated
     * @param context an active web engine context
     * @param route   a route that leads to a {@code rife.cmf.elements.ServeContent} element
     * @return the html content representation
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    String getContentForHtml(int id, ContentInfo info, Context context, Route route)
    throws ContentManagerException;
}
