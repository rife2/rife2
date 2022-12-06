/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.regex.Pattern;

record PathInfoSegment(String text, Pattern pattern) {
    private PathInfoSegment(String value) {
        this(value, null);
    }

    private PathInfoSegment(Pattern pattern) {
        this(null, pattern);
    }

    static PathInfoSegment createTextSegment(String text) {
        return new PathInfoSegment(text);
    }

    static PathInfoSegment createRegexpSegment(Pattern pattern) {
        return new PathInfoSegment(pattern);
    }

    boolean isRegexp() {
        return pattern() != null;
    }
}
