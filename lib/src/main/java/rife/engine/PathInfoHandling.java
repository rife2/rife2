/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class PathInfoHandling {
    public static final PathInfoHandling NONE = new PathInfoHandling(PathInfoType.NONE);
    public static final PathInfoHandling CAPTURE = new PathInfoHandling(PathInfoType.CAPTURE);

    public static PathInfoHandling MAP(PathInfoBuilder builder) {
        var mapping = new PathInfoMapping();
        builder.build(mapping);
        return new PathInfoHandling(mapping);
    }

    private final PathInfoType type_;
    private final PathInfoMapping mapping_;

    private PathInfoHandling(PathInfoType type) {
        if (type == null) {
            type = PathInfoType.NONE;
        }
        type_ = type;
        mapping_ = null;
    }

    private PathInfoHandling(PathInfoMapping mapping) {
        type_ = PathInfoType.MAP;
        mapping_ = mapping;
    }

    public PathInfoType type() {
        return type_;
    }

    public PathInfoMapping mapping() {
        return mapping_;
    }
}