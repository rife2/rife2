/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import rife.asm.ClassReader;
import rife.asm.ClassVisitor;
import rife.asm.ClassWriter;
import rife.validation.MetaDataMerged;

/**
 * This utility class provides an entrance method to modify the bytecode of a
 * class so that metadata from a sibling class is merged into the first class.
 * <p>
 * Basically, this automatically creates an instance of the metadata class and
 * stores it as a field of the modified class. All the interfaces of the metadata
 * class are also automatically implemented by the modified class by
 * delegating all the method calls to the added field instance.
 * <p>
 * WARNING: this class is not supposed to be used directly, it is made public
 * since the RIFE2 agent has to be able to access it.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class MetaDataBytecodeTransformer {
    /**
     * Performs the actual merging of the metadata class' functionalities into
     * the bytes of the class that were provided.
     *
     * @param origBytes the bytes that have to be modied
     * @param metaData  the metadata classes that will be merged into it
     * @return the modified bytes
     * @since 1.0
     */
    public static byte[] mergeMetaDataIntoBytes(byte[] origBytes, Class metaData) {
        // only perform the instrumentation if the MetaDataMerged interface is implemented
        if (!MetaDataMerged.class.isAssignableFrom(metaData)) {
            return origBytes;
        }

        // merge the metadata class into the original bytes
		var cr = new ClassReader(origBytes);

		var method_collector = new MetaDataMethodCollector();
        cr.accept(method_collector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

		var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        var meta_data_adapter = new MetaDataClassAdapter(method_collector.getMethods(), metaData, cw);
        cr.accept(meta_data_adapter, ClassReader.SKIP_FRAMES);

        return cw.toByteArray();
    }
}
