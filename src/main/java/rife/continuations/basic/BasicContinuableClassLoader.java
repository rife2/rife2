/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.basic;

import rife.continuations.ContinuationConfigInstrument;
import rife.continuations.instrument.*;
import rife.instrument.ClassBytesProvider;
import rife.tools.ClassBytesLoader;

/**
 * Classloader implementation that will transform bytecode for classes that
 * should receive the continuations functionalities.
 * <p>Note that this is a basic classloader implementation. For your own
 * application you should probably create your own or at least read over
 * the source code of this one. It's even better to not use a custom
 * classloader and only rely on the {@link ContinuationsAgent}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class BasicContinuableClassLoader extends ClassLoader implements ClassBytesProvider {
    private final ContinuationConfigInstrument config_;
    private final ClassBytesLoader bytesLoader_;
    private final ContinuableDetector continuableDetector_;

    /**
     * Creates a new classloader instance with the context classloader as
     * the parent classloader.
     *
     * @param config the instance of the instrumentation configuration that
     *               will be used for the transformation
     * @since 1.0
     */
    public BasicContinuableClassLoader(ContinuationConfigInstrument config) {
        this(Thread.currentThread().getContextClassLoader(), config);
    }

    /**
     * Creates a new classloader instance.
     *
     * @param parent the parent classloader
     * @param config the instance of the instrumentation configuration that
     *               will be used for the transformation
     * @since 1.0
     */
    public BasicContinuableClassLoader(ClassLoader parent, ContinuationConfigInstrument config) {
        super(parent);

        config_ = config;
        bytesLoader_ = new ClassBytesLoader(getParent());
        continuableDetector_ = new ContinuableDetector(config_, this);
    }

    public byte[] getClassBytes(String className, boolean reloadAutomatically)
    throws ClassNotFoundException {
        return bytesLoader_.getClassBytes(className.replace('.', '/') + ".class");
    }

    public Class loadClass(String name)
    throws ClassNotFoundException {
        // disable this classloader and delegate to the parent if the continuations
        // agent is active
        if (Boolean.getBoolean(ContinuationsAgent.AGENT_ACTIVE_PROPERTY)) {
            return getParent().loadClass(name);
        }

        // if the class wasn't previously loaded by this one, perform the instrumentation
        synchronized (name.intern()) {
            if (null == findLoadedClass(name)) {
                var bytes = getClassBytes(name, false);
                if (bytes == null) {
                    return super.loadClass(name);
                }

                if (continuableDetector_.detect(bytes, false)) {
                    var resume_bytes = ContinuationsBytecodeTransformer.transformIntoResumableBytes(config_, bytes, name);

                    if (resume_bytes != null) {
                        bytes = resume_bytes;
                    }

                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
        }

        return super.loadClass(name);
    }
}
