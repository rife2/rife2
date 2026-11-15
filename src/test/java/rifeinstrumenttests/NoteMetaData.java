/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeinstrumenttests;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class NoteMetaData extends MetaData {
    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("title").notNull(true));
    }
}
