/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

// spots the changes that make the reloader restart the application or refresh the browser
class ReloadScanner {
    static final long POLL_INTERVAL_MS = 250;
    static final Predicate<Path> CLASS_FILES = file -> file.getFileName().toString().endsWith(".class");
    static final Predicate<Path> OTHER_FILES = CLASS_FILES.negate();

    // a build writes its files in bursts, waiting for several quiet scans keeps
    // the application from being restarted while it's still busy
    private static final int STABLE_SCANS = 2;

    record FileStamp(FileTime modified, long size) {
    }

    private record Scan(Map<Path, FileStamp> files, boolean complete) {
    }

    private final List<Path> directories_ = new ArrayList<>();
    private final Set<Path> present_ = new HashSet<>();
    private final Predicate<Path> files_;
    private Map<Path, FileStamp> snapshot_;
    private boolean changed_ = false;
    private boolean incomplete_;
    private boolean incompleteReported_ = false;
    private int stable_ = 0;

    ReloadScanner(Collection<Path> directories, Predicate<Path> files) {
        directories_.addAll(directories);
        files_ = files;

        var scan = scan(directories_);
        snapshot_ = scan.files();
        incomplete_ = !scan.complete();
    }

    // the files of a new directory are taken along right away, a change to them
    // before the next scan would otherwise be seen as the directory appearing
    synchronized void add(Path directory) {
        if (directories_.contains(directory)) {
            return;
        }

        directories_.add(directory);
        var scan = scan(List.of(directory));
        snapshot_.putAll(scan.files());
        incomplete_ = incomplete_ || !scan.complete();
    }

    // reports that the files stopped changing rather than the change itself, so
    // that a build which writes a batch of files only leads to one reload
    synchronized boolean settled() {
        var scan = scan(directories_);
        if (!scan.complete()) {
            // the files that couldn't be read would look like they were deleted
            reportIncomplete();
            return false;
        }
        incompleteReported_ = false;

        if (incomplete_) {
            // the previous files were only partly known, take these as the
            // starting point instead of reporting the difference as a change
            incomplete_ = false;
            snapshot_ = scan.files();
            changed_ = false;
            stable_ = 0;
            return false;
        }

        if (!scan.files().equals(snapshot_)) {
            snapshot_ = scan.files();
            changed_ = true;
            stable_ = 0;
            return false;
        }

        if (changed_ && ++stable_ >= STABLE_SCANS) {
            changed_ = false;
            stable_ = 0;
            return true;
        }

        return false;
    }

    private void reportIncomplete() {
        if (!incompleteReported_) {
            incompleteReported_ = true;
            Logger.getLogger("rife.engine").warning("Some of the watched directories couldn't be read, " +
                "changes will not be picked up until they can be.");
        }
    }

    private Scan scan(List<Path> directories) {
        var walk = new Walk();
        for (var directory : directories) {
            if (!Files.isDirectory(directory)) {
                // a directory that was there before is being rebuilt, its files aren't gone
                if (present_.contains(directory)) {
                    walk.complete = false;
                }
                continue;
            }
            present_.add(directory);

            try {
                // build directories are regularly symbolic links towards somewhere
                // else, their contents still have to be seen
                Files.walkFileTree(directory, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, walk);
            } catch (IOException e) {
                walk.complete = false;
            }
        }

        return new Scan(walk.files, walk.complete);
    }

    private class Walk extends SimpleFileVisitor<Path> {
        final Map<Path, FileStamp> files = new HashMap<>();
        final Set<Path> visited = new HashSet<>();
        boolean complete = true;

        @Override
        public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
            // links towards each other turn a tree into a web that would
            // otherwise be walked over and over again
            if (!visited.add(realPath(directory))) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            if (files_.test(file)) {
                files.put(file.toAbsolutePath().normalize(), new FileStamp(attributes.lastModifiedTime(), attributes.size()));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return classify(e);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, IOException e) {
            return classify(e);
        }

        // a link that points back into the tree is not a change, and neither is a
        // file that a compiler deleted while the walk was busy
        private FileVisitResult classify(IOException e) {
            if (e != null &&
                !(e instanceof FileSystemLoopException) &&
                !(e instanceof NoSuchFileException)) {
                complete = false;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private static Path realPath(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path.toAbsolutePath().normalize();
        }
    }

    static List<Path> directories(String... paths) {
        var result = new LinkedHashSet<Path>();
        for (var path : paths) {
            if (null == path || path.isEmpty()) {
                continue;
            }

            for (var entry : path.split(File.pathSeparator)) {
                var lowercase = entry.toLowerCase(Locale.ROOT);
                if (!entry.isEmpty() &&
                    !entry.endsWith("*") &&
                    !lowercase.endsWith(".jar") &&
                    !lowercase.endsWith(".zip")) {
                    var directory = directory(entry);
                    if (directory != null) {
                        result.add(directory);
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    // resolving the real path keeps the same directory under several names,
    // for instance through a link, from being watched more than once
    static Path directory(String path) {
        try {
            var result = Path.of(path).toAbsolutePath().normalize();
            if (Files.exists(result)) {
                return result.toRealPath();
            }
            return result;
        } catch (InvalidPathException e) {
            // a resource base can also sit inside an archive, which has no directory to watch
            return null;
        } catch (IOException e) {
            return Path.of(path).toAbsolutePath().normalize();
        }
    }
}
