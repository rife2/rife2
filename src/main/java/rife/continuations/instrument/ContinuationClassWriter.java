/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.ClassReader;
import rife.asm.ClassWriter;
import rife.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes frame hierarchy relationships from class-file resources instead
 * of loading classes that may still be in the process of being defined.
 */
class ContinuationClassWriter extends ClassWriter {
    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";

    private final ClassLoader classLoader_;
    private final Map<String, ClassInfo> classes_ = new HashMap<>();

    ContinuationClassWriter(int flags, byte[] currentClassBytes, ClassLoader classLoader) {
        super(flags);

        classLoader_ = classLoader;
        var current_class = new ClassReader(currentClassBytes);
        classes_.put(current_class.getClassName(), new ClassInfo(current_class));
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (isAssignableFrom(type1, type2)) {
            return type1;
        }
        if (isAssignableFrom(type2, type1)) {
            return type2;
        }

        var class1 = classInfo(type1);
        var class2 = classInfo(type2);
        if (class1.isInterface() || class2.isInterface()) {
            return OBJECT_INTERNAL_NAME;
        }

        var common = class1.superName_;
        while (common != null) {
            if (isAssignableFrom(common, type2)) {
                return common;
            }
            common = classInfo(common).superName_;
        }

        return OBJECT_INTERNAL_NAME;
    }

    private boolean isAssignableFrom(String target, String source) {
        if (target.equals(source) || OBJECT_INTERNAL_NAME.equals(target)) {
            return true;
        }

        return isAssignableFrom(target, source, new HashSet<>());
    }

    private boolean isAssignableFrom(String target, String source, Set<String> visited) {
        if (target.equals(source)) {
            return true;
        }
        if (!visited.add(source)) {
            return false;
        }

        var source_class = classInfo(source);
        for (var interface_name : source_class.interfaces_) {
            if (isAssignableFrom(target, interface_name, visited)) {
                return true;
            }
        }

        return source_class.superName_ != null &&
               isAssignableFrom(target, source_class.superName_, visited);
    }

    private ClassInfo classInfo(String internalName) {
        var result = classes_.get(internalName);
        if (result != null) {
            return result;
        }

        var resource_name = internalName + ".class";
        try (InputStream input = classLoader_ == null
                                 ? ClassLoader.getSystemResourceAsStream(resource_name)
                                 : classLoader_.getResourceAsStream(resource_name)) {
            if (input == null) {
                throw new TypeNotPresentException(internalName, null);
            }

            result = new ClassInfo(new ClassReader(input));
            classes_.put(internalName, result);
            return result;
        } catch (IOException e) {
            throw new TypeNotPresentException(internalName, e);
        }
    }

    private static class ClassInfo {
        private final int access_;
        private final String superName_;
        private final String[] interfaces_;

        private ClassInfo(ClassReader reader) {
            access_ = reader.getAccess();
            superName_ = reader.getSuperName();
            interfaces_ = reader.getInterfaces();
        }

        private boolean isInterface() {
            return (access_ & Opcodes.ACC_INTERFACE) != 0;
        }
    }
}
