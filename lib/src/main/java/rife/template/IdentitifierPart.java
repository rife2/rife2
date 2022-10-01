/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

class IdentitifierPart implements ParseCondition {
    @Override
    public boolean isValid(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }
}
