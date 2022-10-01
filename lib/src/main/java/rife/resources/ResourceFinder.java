/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;

import java.net.URL;

/**
 * This interface defines the methods that classes with
 * <code>ResourceFinder</code> functionalities have to implement.
 * <p>
 * A <code>ResourceFinder</code> provides an abstract way of working
 * with resources. According to a name, a resource can be searched for and its
 * location is returned as an <code>URL</code> object.
 * <p>
 * It also possible to obtain a stream to read the resource's content,
 * to retrieve all its contents as a <code>String</code> and to obtain the
 * modification time of the resource.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceWriter
 * @since 1.0
 */
public interface ResourceFinder {
    /**
     * Retrieves the resource that corresponds to the provided name.
     * <p>
     * This method never throws an exception, but returns <code>null</code> in
     * case of an exception.
     *
     * @param name the name of the resource to retrieve
     * @return the <code>URL</code> object that corresponds to the provided
     * name; or
     * <p>
     * <code>null</code> if the resource couldn't be found or if an error
     * occurred.
     * @since 1.0
     */
    URL getResource(String name);

    /**
     * Returns a stream that can be used to read the contents of the resource
     * that corresponds to the provided name.
     *
     * @param name the name of the resource to retrieve
     * @param user an instance of <code>InputStreamUser</code>
     *             that contains the logic that will be executed with this stream
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      creation or opening of the stream.
     * @throws InnerClassException          when errors occurs inside the
     *                                      <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see #useStream(URL, InputStreamUser)
     * @since 1.0
     */
    <ResultType> ResultType useStream(String name, InputStreamUser user)
    throws ResourceFinderErrorException, InnerClassException;

    /**
     * Returns a stream that can be used to read the contents of the provided
     * resource.
     *
     * @param resource the resource to retrieve
     * @param user     an instance of <code>InputStreamUser</code>
     *                 that contains the logic that will be executed with this stream
     * @return the return value from the <code>useInputStream</code> method of
     * the provided <code>InputStreamUser</code> instance
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      creation or opening of the stream.
     * @throws InnerClassException          when errors occurs inside the
     *                                      <code>InputStreamUser</code>
     * @see InputStreamUser
     * @see #useStream(String, InputStreamUser)
     * @since 1.0
     */
    <ResultType> ResultType useStream(URL resource, InputStreamUser user)
    throws ResourceFinderErrorException, InnerClassException;

    /**
     * Retrieves the complete content of the resource that corresponds to the
     * provided name. The content will be read into a string by using the
     * platform's default encoding.
     *
     * @param name the name of the resource to retrieve
     * @return a <code>String</code> object that contains the complete content
     * of the resource with the provided name; or
     * <p>
     * <code>null</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the content.
     * @see #getContent(String, String)
     * @see #getContent(URL, String)
     * @since 1.0
     */
    String getContent(String name)
    throws ResourceFinderErrorException;

    /**
     * Retrieves the complete content of the resource that corresponds to the
     * provided name.
     *
     * @param name     the name of the resource to retrieve the content from
     * @param encoding the encoding that should be used to read the content
     * @return a <code>String</code> object that contains the complete content
     * of the resource with the provided name; or
     * <p>
     * <code>null</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the content or when the encoding is not supported.
     * @see #getContent(String)
     * @see #getContent(URL)
     * @see #getContent(URL, String)
     * @since 1.0
     */
    String getContent(String name, String encoding)
    throws ResourceFinderErrorException;

    /**
     * Retrieves the complete content of the provided resource. The content will
     * be read into a string by using the platform's default encoding.
     *
     * @param resource the resource to retrieve the content from
     * @return a <code>String</code> object that contains the complete content
     * of the resource with the provided name; or
     * <p>
     * <code>null</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the content or when the encoding is not supported.
     * @see #getContent(String)
     * @see #getContent(String, String)
     * @see #getContent(URL, String)
     * @since 1.0
     */
    String getContent(URL resource)
    throws ResourceFinderErrorException;

    /**
     * Retrieves the complete content of the provided resource.
     *
     * @param resource the resource to retrieve the content from
     * @param encoding the encoding that should be used to read the content
     * @return a <code>String</code> object that contains the complete content
     * of the resource with the provided name; or
     * <p>
     * <code>null</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the content or when the encoding is not supported.
     * @see #getContent(String)
     * @see #getContent(String, String)
     * @see #getContent(URL)
     * @since 1.0
     */
    String getContent(URL resource, String encoding)
    throws ResourceFinderErrorException;

    /**
     * Retrieves the modification time of the resource that corresponds to the
     * provided name.
     *
     * @param name the name of the resource to retrieve
     * @return a positive <code>long</code> with the modification time in
     * milliseconds; or
     * <p>
     * <code>-1</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the modification time.
     * @see #getModificationTime(URL)
     * @since 1.0
     */
    long getModificationTime(String name)
    throws ResourceFinderErrorException;

    /**
     * Retrieves the modification time of the provided resource.
     *
     * @param resource the resource to retrieve the modification time from
     * @return a positive <code>long</code> with the modification time in
     * milliseconds; or
     * <p>
     * <code>-1</code> if the resource couldn't be found.
     * @throws ResourceFinderErrorException when an error occurred during the
     *                                      retrieval of the modification time.
     * @see #getModificationTime(String)
     * @since 1.0
     */
    long getModificationTime(URL resource)
    throws ResourceFinderErrorException;
}
