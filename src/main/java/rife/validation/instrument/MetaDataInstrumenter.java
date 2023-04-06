/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

/**
 * This is a bytecode instrumenter that will modify classes so that they
 * receive the functionalities that are required to support meta-data
 * merging.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MetaDataInstrumenter {
    public static final String META_DATA_SUFFIX = "MetaData";

    public static byte[] instrument(final ClassLoader loader, final String classNameDotted, final byte[] classfileBuffer) {
        Class metadata_class = null;

        var metadata_classname = MetaDataClassAnnotationDetector.getMetaDataClassName(classfileBuffer);

        if (null == metadata_classname &&
            !classNameDotted.endsWith(META_DATA_SUFFIX)) {
            metadata_classname = classNameDotted + META_DATA_SUFFIX;
        }

        if (metadata_classname != null) {
            try {
                metadata_class = loader.loadClass(metadata_classname);
            } catch (ClassNotFoundException e) {
                metadata_class = null;
            }
        }

        if (metadata_class != null) {
            var result = MetaDataBytecodeTransformer.mergeMetaDataIntoBytes(classfileBuffer, metadata_class);
            if (result != null) {
                return result;
            }
        }

        return classfileBuffer;
    }
}