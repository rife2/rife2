/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.tools.exceptions.FileUtilsErrorException;

import java.net.URL;

/**
 * Utility class to load the bytes of class files.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class ClassBytesLoader {
    private final ClassLoader classLoader_;

    /**
     * Instantiates a new bytes loader for class files.
     *
     * @param classLoader the classloader that should be used to search for the
     *                    classes
     * @since 1.0
     */
    public ClassBytesLoader(ClassLoader classLoader) {
        classLoader_ = classLoader;
    }

    /**
     * Retrieves a byte array that contains the bytecode for a specific Java
     * class.
     *
     * @param classFileName the file name of the class whose bytes should be
     *                      loaded, note that this is not the Java FQN ({@code rife.Version}),
     *                      but the real name of the file resource ({@code rife/Version.java})
     * @return an array with the bytes of the class; or<p>
     * {@code null} if no bytes could be loaded
     * @see #getClassBytes(String, URL)
     * @since 1.0
     */
    public byte[] getClassBytes(String classFileName)
    throws ClassNotFoundException {
        return getClassBytes(classFileName, null);
    }

    /**
     * Retrieves a byte array that contains the bytecode for a specific Java
     * class.
     *
     * @param classFileName the file name of the class whose bytes should be
     *                      loaded, note that this is not the Java FQN ({@code rife.Version}),
     *                      but the real name of the file resource ({@code rife/Version.java})
     * @param classResource the resource that can be used to load the class
     *                      bytes from if it couldn't be obtained by using the file name, if no
     *                      resource is provided and the bytes couldn't be loaded by simply using
     *                      the class' file name, a resource will be looked up for the file name
     *                      through the class loader that was provided to the constructor
     * @return an array with the bytes of the class; or<p>
     * {@code null} if no bytes could be loaded
     * @see #getClassBytes(String)
     * @since 1.0
     */
    public byte[] getClassBytes(String classFileName, URL classResource)
    throws ClassNotFoundException {
        byte[] raw_bytes = null;

        // get the class bytes through a regular method that works on any JVM
        if (null == raw_bytes) {
            if (null == classResource &&
                classFileName != null) {
                classResource = classLoader_.getResource(classFileName);
            }

            if (classResource != null) {
                try {
                    raw_bytes = FileUtils.readBytes(classResource);
                } catch (FileUtilsErrorException e) {
                    throw new ClassNotFoundException("Unexpected error while reading the bytes of the class resource '" + classResource + "'.", e);
                }
            }
        }

        return raw_bytes;
    }
}


