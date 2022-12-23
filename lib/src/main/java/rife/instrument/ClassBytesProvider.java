/*
 * Copyright 2001-2005 Patrick Lightbody and
 * Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.instrument;

public interface ClassBytesProvider {
    byte[] getClassBytes(String className, boolean reloadAutomatically)
    throws ClassNotFoundException;
}

