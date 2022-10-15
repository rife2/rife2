/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;

public class Uncached implements Element
{
	public void process(Context c)
	throws Exception {
		c.preventCaching();
	}
}

