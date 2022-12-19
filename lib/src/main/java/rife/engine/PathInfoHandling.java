/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.ArrayList;
import java.util.List;

public class PathInfoHandling {
    public static final PathInfoHandling NONE = new PathInfoHandling(PathInfoType.NONE);
    public static final PathInfoHandling CAPTURE = new PathInfoHandling(PathInfoType.CAPTURE);

    public static PathInfoHandling MAP(PathInfoBuilder... builder) {
        List<PathInfoMapping> mappings = new ArrayList<>();
        for (var b : builder) {
            var mapping = new PathInfoMapping();
            b.build(mapping);
            mappings.add(mapping);
        }
        return new PathInfoHandling(mappings);
    }

    private final PathInfoType type_;
    private final List<PathInfoMapping> mappings_;

    private PathInfoHandling(PathInfoType type) {
        if (type == null) {
            type = PathInfoType.NONE;
        }
        type_ = type;
        mappings_ = null;
    }

    private PathInfoHandling(List<PathInfoMapping> mapping) {
        type_ = PathInfoType.MAP;
        mappings_ = mapping;
    }

    public PathInfoType type() {
        return type_;
    }

    public List<PathInfoMapping> mappings() {
        return mappings_;
    }
}