/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.regex.Pattern;

class PathInfoMappingSegment {
    private final String text_;
    private final Pattern pattern_;

    private PathInfoMappingSegment(String value) {
        text_ = value;
        pattern_ = null;
    }

    private PathInfoMappingSegment(Pattern pattern) {
        text_ = null;
        pattern_ = pattern;
    }

    static PathInfoMappingSegment createTextSegment(String text) {
        return new PathInfoMappingSegment(text);
    }

    static PathInfoMappingSegment createRegexpSegment(Pattern pattern) {
        return new PathInfoMappingSegment(pattern);
    }

    String getText() {
        return text_;
    }

    Pattern getPattern() {
        return pattern_;
    }

    boolean isRegexp() {
        return pattern_ != null;
    }
}
