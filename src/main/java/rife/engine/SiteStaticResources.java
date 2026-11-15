/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

// serves the files of a directory or of the classpath for GET and HEAD, with
// conditional requests and single byte ranges
class SiteStaticResources {
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private static final long[] UNSATISFIABLE = new long[0];

    private final Path directory_;
    private final List<String> classpathBases_;
    private final ClassLoader classLoader_;
    private final Map<String, Found> nativeResources_ = new ConcurrentHashMap<>();

    private interface Opener {
        InputStream open() throws IOException;
    }

    // the version identifies the content for the ETag
    private record Found(Opener opener, long length, long lastModified, String version) {
    }

    SiteStaticResources(Path directory, List<String> classpathBases, ClassLoader classLoader) {
        directory_ = null == directory ? null : directory.toAbsolutePath().normalize();
        classpathBases_ = List.copyOf(classpathBases);
        classLoader_ = classLoader;
    }

    // the decoded segments of the path, or null when it can't be a file
    static String relativePath(String path) {
        if (null == path) {
            return null;
        }

        String decoded;
        try {
            decoded = StringUtils.decodeUrl(path);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (decoded.indexOf('\\') != -1 || decoded.indexOf('\0') != -1) {
            return null;
        }

        var result = new StringBuilder();
        for (var segment : StringUtils.split(decoded, "/")) {
            if (segment.isEmpty()) {
                continue;
            }
            if (segment.equals(".") || segment.equals("..") || segment.indexOf(':') != -1) {
                return null;
            }
            // these are private to a web application, like with a servlet container
            if (result.isEmpty() &&
                (segment.equalsIgnoreCase("WEB-INF") || segment.equalsIgnoreCase("META-INF"))) {
                return null;
            }
            if (!result.isEmpty()) {
                result.append('/');
            }
            result.append(segment);
        }

        if (result.isEmpty() || decoded.endsWith("/")) {
            return null;
        }

        return result.toString();
    }

    boolean serve(SiteRequest request, SiteResponse response, String path) {
        var method = request.getMethod();
        if (method != RequestMethod.GET && method != RequestMethod.HEAD) {
            return false;
        }

        var relative = relativePath(path);
        if (null == relative) {
            return false;
        }

        Found found;
        try {
            found = find(relative);
        } catch (IOException | InvalidPathException | SecurityException e) {
            return false;
        }
        if (null == found) {
            return false;
        }

        try {
            if (found.length() < 0) {
                byte[] bytes;
                try (var stream = found.opener().open()) {
                    bytes = FileUtils.readBytes(stream);
                }
                found = new Found(() -> new ByteArrayInputStream(bytes), bytes.length, found.lastModified(), found.version());
            }
            answer(request, response, relative, found);
        } catch (IOException e) {
            // a cancelled download ends like a closed connection
            if (!response.isCancelled()) {
                throw new UncheckedIOException(e);
            }
        }

        return true;
    }

    private Found find(String relative)
    throws IOException {
        if (directory_ != null) {
            var file = directory_.resolve(relative).normalize();
            // symbolic links can't lead out of the directory
            if (file.startsWith(directory_) && Files.isRegularFile(file) &&
                file.toRealPath().startsWith(directory_.toRealPath())) {
                return fileFound(file);
            }
        }

        for (var base : classpathBases_) {
            var name = base + "/" + relative;
            var url = classLoader_.getResource(name);
            if (url != null) {
                var found = fromUrl(name, url);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private static Found fileFound(Path file)
    throws IOException {
        var length = Files.size(file);
        var last_modified = Files.getLastModifiedTime(file).toMillis();
        return new Found(() -> Files.newInputStream(file), length, last_modified,
            Long.toHexString(last_modified / 1000 * 1000) + "-" + Long.toHexString(length));
    }

    private Found fromUrl(String name, URL url)
    throws IOException {
        if ("file".equals(url.getProtocol())) {
            Path file;
            try {
                file = Path.of(url.toURI());
            } catch (URISyntaxException | IllegalArgumentException e) {
                return null;
            }
            if (!Files.isRegularFile(file)) {
                return null;
            }
            return fileFound(file);
        }

        var connection = url.openConnection();
        if (connection instanceof JarURLConnection jar_connection) {
            var entry = jar_connection.getJarEntry();
            if (null == entry || entry.isDirectory()) {
                return null;
            }
            // the times in reproducible jars don't change with the content
            return new Found(() -> url.openConnection().getInputStream(), entry.getSize(), entry.getTime(),
                Long.toHexString(entry.getCrc()) + "-" + Long.toHexString(entry.getSize()));
        }

        // in a native image, a directory is a resource too
        if (classLoader_.getResource(name + "/") != null) {
            return null;
        }
        // the resources of a native image have no times and their content
        // never changes, so their checksum is computed once
        var found = nativeResources_.get(name);
        if (null == found) {
            var crc = new CRC32();
            long length = 0;
            try (var input = url.openConnection().getInputStream()) {
                var buffer = new byte[64 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    crc.update(buffer, 0, read);
                    length += read;
                }
            }
            found = new Found(() -> url.openConnection().getInputStream(), length, connection.getLastModified(),
                Long.toHexString(crc.getValue()) + "-" + Long.toHexString(length));
            nativeResources_.put(name, found);
        }
        return found;
    }

    private static void answer(SiteRequest request, SiteResponse response, String relative, Found found)
    throws IOException {
        var length = found.length();
        var last_modified = found.lastModified() > 0 ? found.lastModified() / 1000 * 1000 : 0;
        var etag = "\"" + found.version() + "\"";

        response.setStatus(200);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", etag);
        if (last_modified > 0) {
            response.setDateHeader("Last-Modified", last_modified);
        }

        if (isNotModified(request, etag, last_modified)) {
            response.setStatus(304);
            return;
        }

        var extension = FileUtils.getExtension(relative);
        String mime_type = null;
        if (extension != null) {
            mime_type = RifeConfig.Mime.getMimeType(extension.toLowerCase(Locale.ENGLISH));
        }
        response.setHeader("Content-Type", null == mime_type ? DEFAULT_MIME_TYPE : mime_type);

        long start = 0;
        long end = length - 1;
        var range = request.getHeader("Range");
        if (range != null && ifRangeMatches(request, etag, last_modified)) {
            var parsed = parseRange(range, length);
            if (parsed == UNSATISFIABLE) {
                response.setStatus(416);
                response.setHeader("Content-Range", "bytes */" + length);
                response.setHeader("Content-Length", "0");
                return;
            }
            if (parsed != null) {
                start = parsed[0];
                end = parsed[1];
                response.setStatus(206);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
            }
        }

        var count = Math.max(0, end - start + 1);
        response.setHeader("Content-Length", String.valueOf(count));

        var output = response.rawOutputStream();
        if (request.getMethod() == RequestMethod.HEAD || 0 == count) {
            output.flush();
            return;
        }

        try (var input = found.opener().open()) {
            input.skipNBytes(start);
            var buffer = new byte[64 * 1024];
            var remaining = count;
            while (remaining > 0) {
                var read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (-1 == read) {
                    break;
                }
                output.write(buffer, 0, read);
                remaining -= read;
            }
        }
        output.flush();
    }

    private static boolean isNotModified(SiteRequest request, String etag, long lastModified) {
        var if_none_match = request.getHeader("If-None-Match");
        if (if_none_match != null) {
            for (var candidate : StringUtils.split(if_none_match, ",")) {
                var tag = candidate.trim();
                if (tag.startsWith("W/")) {
                    tag = tag.substring(2);
                }
                if (tag.equals("*") || tag.equals(etag)) {
                    return true;
                }
            }
            return false;
        }

        if (lastModified > 0) {
            try {
                var since = request.getDateHeader("If-Modified-Since");
                return since >= 0 && lastModified <= since;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return false;
    }

    private static boolean ifRangeMatches(SiteRequest request, String etag, long lastModified) {
        var if_range = request.getHeader("If-Range");
        if (null == if_range) {
            return true;
        }
        if (if_range.startsWith("\"")) {
            return if_range.equals(etag);
        }
        try {
            return lastModified > 0 && SiteHeaders.parseDate(if_range) == lastModified;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // a single range as its start and end, or null to serve the whole file
    static long[] parseRange(String header, long length) {
        var value = header.trim();
        if (!value.toLowerCase(Locale.ENGLISH).startsWith("bytes=")) {
            return null;
        }
        value = value.substring(6).trim();
        if (value.indexOf(',') != -1) {
            return null;
        }

        var dash = value.indexOf('-');
        if (-1 == dash) {
            return null;
        }

        try {
            var first = value.substring(0, dash).trim();
            var last = value.substring(dash + 1).trim();
            long start;
            long end;
            if (first.isEmpty()) {
                if (last.isEmpty()) {
                    return null;
                }
                var suffix = Long.parseLong(last);
                if (suffix <= 0) {
                    return UNSATISFIABLE;
                }
                start = Math.max(0, length - suffix);
                end = length - 1;
            } else {
                start = Long.parseLong(first);
                end = length - 1;
                if (!last.isEmpty()) {
                    var requested_end = Long.parseLong(last);
                    // a range that ends before it starts is invalid and ignored
                    if (requested_end < start) {
                        return null;
                    }
                    end = Math.min(requested_end, length - 1);
                }
            }
            if (start >= length || 0 == length) {
                return UNSATISFIABLE;
            }
            return new long[]{start, end};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
