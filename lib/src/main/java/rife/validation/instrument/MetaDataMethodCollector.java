/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation.instrument;

import java.util.*;

import rife.asm.*;

import static rife.asm.Opcodes.ASM9;

class MetaDataMethodCollector extends ClassVisitor {
    private Map<String, List<String>> methods_ = null;

    MetaDataMethodCollector() {
        super(ASM9);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (null == methods_) {
            methods_ = new HashMap<>();
        }
        List<String> descriptions = methods_.computeIfAbsent(name, k -> new ArrayList<>());
        descriptions.add(desc);

        return null;
    }

    public Map<String, List<String>> getMethods() {
        if (null == methods_) {
            return Collections.emptyMap();
        }

        return methods_;
    }
}
