/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.CantOpenResourceStreamException;
import rife.resources.exceptions.CantRetrieveResourceContentException;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class offers <code>ResourceFinder</code> capabilities for resources that
 * are available through the classloader. This is done for directories as well
 * as for jar files. Basically, this corresponds to the resources that are
 * available through the classpath.
 * <p>
 * Since the application's classloader isn't supposed to change in a global way,
 * the <code>ResourceFinderClasspath</code> class can only be instantiated
 * through the static <code>instance()</code> method that always returns
 * the same instance as a singleton.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceFinder
 * @since 1.0
 */
public class ResourceFinderClasspath extends AbstractResourceFinder {
    protected ResourceFinderClasspath() {
    }

    /**
     * Returns the shared singleton instance of the
     * <code>ResourceFinderClasspath</code> class.
     *
     * @return the singleton <code>ResourceFinderClasspath</code> instance
     * @since 1.0
     */
    public static ResourceFinderClasspath instance() {
        return ResourceFinderClasspathSingleton.INSTANCE;
    }

    public URL getResource(String name) {
        URL resource = null;

        if (this.getClass().getClassLoader() != null) {
            // Try the class loader that loaded this class.
            resource = this.getClass().getClassLoader().getResource(name);
        } else {
            // Try the system class loader.
            resource = ClassLoader.getSystemClassLoader().getResource(name);
        }

        if (null == resource) {
            // if not found in classpath fall back to default
            resource = this.getClass().getResource(name);
        }

        return resource;
    }

    public <ResultType> ResultType useStream(URL resource, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException, InnerClassException {
        if (null == resource ||
            null == user) {
            return null;
        }

        InputStream stream = null;
        try {
            var connection = resource.openConnection();
            connection.setUseCaches(false);
            stream = connection.getInputStream();
            return user.useInputStream(stream);
        } catch (IOException e) {
            throw new CantOpenResourceStreamException(resource, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // couldn't close stream since it probably already has been
                    // closed after an exception
                    // proceed without reporting an error message.
                }
            }
        }
    }

    public String getContent(URL resource, String encoding)
    throws ResourceFinderErrorException {
        if (null == resource) {
            return null;
        }

        try {
            return FileUtils.readString(resource, encoding);
        } catch (FileUtilsErrorException e) {
            throw new CantRetrieveResourceContentException(resource, encoding, e);
        }
    }

    public long getModificationTime(URL resource)
    throws ResourceFinderErrorException {
        return ModificationTimeClasspath.getModificationTime(resource);
    }
}
