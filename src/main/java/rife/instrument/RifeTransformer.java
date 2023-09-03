/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * An abstract base class that will only execute the bytecode transformation
 * when the class is considered to not be part of a core package, like for
 * example the JDK or the standard XML parsers.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class RifeTransformer implements ClassFileTransformer {
    public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
    throws IllegalClassFormatException {
        if (className.startsWith("apple/security/") ||
            className.startsWith("jakarta/") ||
            className.startsWith("java/") ||
            className.startsWith("javax/") ||
            className.startsWith("jdk/") ||
            (className.startsWith("com/") &&
             (className.startsWith("com/esotericsoftware/") ||
              className.startsWith("com/google/") ||
              className.startsWith("com/mysql/") ||
              className.startsWith("com/sun/"))) ||
            className.startsWith("net/rubygrapefruit/") ||
            className.startsWith("net/sourceforge/htmlunit/") ||
            className.startsWith("oracle/") ||
            (className.startsWith("org/") &&
             (className.startsWith("org/antlr/") ||
              className.startsWith("org/apache/") ||
              className.startsWith("org/apiguardian/") ||
              className.startsWith("org/eclipse/jetty/") ||
              className.startsWith("org/hamcrest/") ||
              className.startsWith("org/h2/") ||
              className.startsWith("org/hsqldb/") ||
              className.startsWith("org/gradle/") ||
              className.startsWith("org/junit/") ||
              className.startsWith("org/opentest4j/") ||
              className.startsWith("org/postgresql/") ||
              className.startsWith("org/slf4j/") ||
              className.startsWith("org/xml/") ||
              className.startsWith("org/w3c/"))) ||
            className.startsWith("sun/") ||
            className.startsWith("worker/org/gradle/") ||
            (className.startsWith("rife/") &&
             !className.startsWith("rife/Hello") &&
             !className.startsWith("rife/models/") &&
             !className.startsWith("rife/engine/continuations/Test"))) {
            return classfileBuffer;
        }

        return transformRife(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    /**
     * This transform method will only be called when the class is not part of
     * a core package.
     * <p>For the rest it functions exactly as the regular
     * {@code transform} method.
     *
     * @see #transform
     * @since 1.0
     */
    protected abstract byte[] transformRife(ClassLoader loader, String classNameInternal, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
    throws IllegalClassFormatException;
}
