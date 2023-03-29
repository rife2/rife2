/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

record RepositoryArtifact(Repository repository, String url) {
    RepositoryArtifact appendPath(String path) {
        return new RepositoryArtifact(repository, url + path);
    }
}
