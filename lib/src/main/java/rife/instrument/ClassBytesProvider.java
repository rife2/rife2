/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

public interface ClassBytesProvider {
    byte[] getClassBytes(String className, boolean reloadAutomatically)
    throws ClassNotFoundException;
}

