/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.PathInfoMappingPatternInvalidException;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PathInfoMapping {
    private final StringBuilder mappingRegexp_ = new StringBuilder();
    private final List<String> parameters_ = new ArrayList<>();
    private final List<PathInfoMappingSegment> segments_ = new ArrayList<>();
    private Pattern regexp_ = null;

    public PathInfoMapping s() {
        return t("/");
    }

    public PathInfoMapping t(String literal) {
        if (literal.length() > 0) {
            mappingRegexp_.append(StringUtils.encodeRegexp(literal));
            segments_.add(PathInfoMappingSegment.createTextSegment(literal));
        }

        return this;
    }

    public PathInfoMapping p(String name) {
        return p(name, "\\w+");
    }

    public PathInfoMapping p(String name, String regexp) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            throw new PathInfoMappingPatternInvalidException(name, regexp, e);
        }

        parameters_.add(name);

        mappingRegexp_.append("(");
        mappingRegexp_.append(regexp);
        mappingRegexp_.append(")");
        segments_.add(PathInfoMappingSegment.createRegexpSegment(pattern));
        return this;
    }

    public List<String> getParameters() {
        return parameters_;
    }

    public List<PathInfoMappingSegment> getSegments() {
        return segments_;
    }

    public Pattern getRegexp() {
        return regexp_;
    }

    void compile() {
        regexp_ = Pattern.compile(mappingRegexp_.toString());
    }
}