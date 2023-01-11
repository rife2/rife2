/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.cmf.Content;
import rife.cmf.ContentInfo;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.cmf.transform.ContentTransformer;
import rife.engine.Context;
import rife.engine.Route;

/**
 * A {@code ContentManager} manages content that is stored in a back-end
 * data store.
 * <p>Content is isolated in repositories that should have unique names. The
 * installation of a content manager creates an initial default repository. If
 * others are needed, they have to be created explicitly.
 * <p>All content is identified by a unique {@code location}. The
 * location is formatted like this:
 * <pre>repository:path</pre>
 * <p>If the {@code repository:} prefix is omitted, the content will be
 * stored in the default repository (see {@link
 * rife.cmf.ContentRepository#DEFAULT ContentRepository.DEFAULT}).
 * <p>The path should start with a slash that makes it 'absolute', this is
 * completely analogue to file system paths.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)i
 * @since 1.0
 */
public interface ContentManager {
    /**
     * Installs a content manager.
     *
     * @return {@code true} if the installation was successful; or
     * <p>{@code false} if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean install()
    throws ContentManagerException;

    /**
     * Removes a content manager.
     *
     * @return {@code true} if the removal was successful; or
     * <p>{@code false} if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean remove()
    throws ContentManagerException;

    /**
     * Creates a new repository.
     *
     * @param name the name of the repository to create
     * @return {@code true} if the creation was successful; or
     * <p>{@code false} if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean createRepository(String name)
    throws ContentManagerException;

    /**
     * Checks if the content manager contains a certain repository.
     *
     * @param name the name of the repository to check
     * @return {@code true} if the repository exists; or
     * <p>{@code false} if it doesn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.4
     */
    boolean containsRepository(String name)
    throws ContentManagerException;

    /**
     * Store content at a certain location.
     * <p>If content is already present at this location, the new content will
     * become the current version and the old content remains available as an
     * older version.
     *
     * @param location    the location where the content has to be stored.
     * @param content     the content that has to be stored
     * @param transformer a transformer that will modify the content data; or
     *                    <p>{@code null} if the content data should stay intact
     * @return {@code true} if the storing was successfully; or
     * <p>{@code false} if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean storeContent(String location, Content content, ContentTransformer transformer)
    throws ContentManagerException;

    /**
     * Delete the content at a certain location.
     * <p>This will delete all versions of the content at that location.
     *
     * @param location the location where the content has to be deleted
     * @return {@code true} if the deletion was successfully; or
     * <p>{@code false} if it wasn't.
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    boolean deleteContent(String location)
    throws ContentManagerException;

    /**
     * Use the data of content at a certain location.
     * <p>Some content data will only be available during this method call due
     * to their volatile nature (certain streams for instance). Therefore, one
     * has to be careful when trying to move the data that is provided to the
     * content user outside this method. The behaviour is undefined.
     *
     * @param location the location whose content will be used
     * @param user     the content user instance that will be called to use
     *                 content data
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    void useContentData(String location, ContentDataUserWithoutResult user)
    throws ContentManagerException;

    /**
     * Use the data of content at a certain location.
     * <p>Some content data will only be available during this method call due
     * to their volatile nature (certain streams for instance). Therefore, one
     * has to be careful when trying to move the data that is provided to the
     * content user outside this method. The behaviour is undefined.
     *
     * @param location the location whose content will be used
     * @param user     the content user instance that will be called to use
     *                 content data
     * @return the data that the {@link ContentDataUser#useContentData(Object)}
     * returns after its usage
     * @throws ContentManagerException if an unexpected error occurred
     * @since 1.0
     */
    <ResultType> ResultType useContentDataResult(String location, ContentDataUser<ResultType> user)
    throws ContentManagerException;

    /**
     * Checks whether content data is available at a certain location.
     *
     * @param location the location that has to be checked
     * @return {@code true} if content data is available; or
     * <p>{@code false} if it isn't.
     * @throws ContentManagerException if an expected error occurred
     * @since 1.0
     */
    boolean hasContentData(String location)
    throws ContentManagerException;

    /**
     * Retrieves the content info from a certain location.
     *
     * @param location the location whose content info has to be retrieved
     * @return an instance of {@code ContentInfo}; or
     * <p>{@code null} if no content is present at the location
     * @throws ContentManagerException if an expected error occurred
     * @since 1.0
     */
    ContentInfo getContentInfo(String location)
    throws ContentManagerException;

    /**
     * Serves content data from a certain location through the provided
     * element.
     * <p>This is intended to take over the complete handling of the request,
     * so no other content should be output and no headers manipulated in the
     * element if this method is called.
     *
     * @param context  an active web engine context
     * @param location the location whose content data has to be served
     * @throws ContentManagerException if an expected error occurred
     * @since 1.0
     */
    void serveContentData(Context context, String location)
    throws ContentManagerException;

    /**
     * Retrieves a content representation for use in html.
     * <p>This is mainly used to integrate content data inside a html
     * document. For instance, html content will be displayed as-is, while
     * image content will cause an image tag to be generated with the correct
     * source URL to serve the image.
     *
     * @param location the location whose content will be displayed
     * @param context  an active web engine context
     * @param route    a route that leads to a {@code rife.cmf.elements.ServeContent} element
     * @throws ContentManagerException if an expected error occurred
     * @since 1.0
     */
    String getContentForHtml(String location, Context context, Route route)
    throws ContentManagerException;
}
