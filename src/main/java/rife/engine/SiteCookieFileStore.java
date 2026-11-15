/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.tools.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores the cookies of a {@link SiteCookieJar} in a file.
 * <p>The file is replaced as a whole each time the cookies change, so that
 * it's never left half written when your application is stopped. Only
 * {@code java.io} is used for this, since on iOS parts of
 * {@code java.nio.file} bring along a disk space call that Apple wants a
 * reason for in the privacy manifest of your app.
 * <p>The cookies can be credentials, so the file should be in a location
 * that your application protects, its own application support directory for
 * instance.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public class SiteCookieFileStore implements SiteCookieStore {
    private static final String SEPARATOR = "\t";
    private static final int FIELDS = 9;

    private final File file_;

    /**
     * Creates a store that keeps the cookies in a file.
     *
     * @param file the file of the cookies, which doesn't have to exist yet
     * @since 1.11.0
     */
    public SiteCookieFileStore(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null");

        file_ = file;
    }

    /**
     * Retrieves the file of the cookies.
     *
     * @return the file of this store
     * @since 1.11.0
     */
    public File getFile() {
        return file_;
    }

    public synchronized Collection<SiteCookie> load() {
        var cookies = new ArrayList<SiteCookie>();
        if (!file_.exists()) {
            return cookies;
        }

        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var cookie = parse(line);
                if (cookie != null) {
                    cookies.add(cookie);
                }
            }
        } catch (IOException e) {
            throw new EngineException(e);
        }
        return cookies;
    }

    public synchronized void save(Collection<SiteCookie> cookies) {
        var directory = file_.getAbsoluteFile().getParentFile();
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            throw new EngineException(new IOException("the directory '" + directory + "' couldn't be created"));
        }

        var temporary = new File(file_.getAbsolutePath() + ".tmp");
        try (var output = new FileOutputStream(temporary)) {
            var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            for (var cookie : cookies) {
                writer.write(format(cookie));
                writer.write('\n');
            }
            writer.flush();
            output.getFD().sync();
        } catch (IOException e) {
            temporary.delete();
            throw new EngineException(e);
        }

        // a rename doesn't replace an existing file on every platform
        if (!temporary.renameTo(file_) &&
            !(file_.delete() && temporary.renameTo(file_))) {
            temporary.delete();
            throw new EngineException(new IOException("the cookies couldn't be written to '" + file_ + "'"));
        }
    }

    private static String format(SiteCookie cookie) {
        return String.join(SEPARATOR, List.of(
            encode(cookie.name()),
            encode(cookie.value()),
            encode(cookie.host()),
            encode(cookie.domain()),
            encode(cookie.path()),
            String.valueOf(cookie.expires()),
            String.valueOf(cookie.secure()),
            String.valueOf(cookie.httpOnly()),
            encode(cookie.sameSite())));
    }

    // a line that can't be read is left out, like the jar does with a cookie
    // that it can't use
    private static SiteCookie parse(String line) {
        var fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELDS) {
            return null;
        }

        try {
            return new SiteCookie(decode(fields[0]), decode(fields[1]), decode(fields[2]), decode(fields[3]), decode(fields[4]),
                Long.parseLong(fields[5]), Boolean.parseBoolean(fields[6]), Boolean.parseBoolean(fields[7]), decode(fields[8]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // a part that's set starts with a marker, so that an empty value isn't
    // read back as null
    private static String encode(String value) {
        return null == value ? "" : "=" + StringUtils.encodeUrl(value);
    }

    private static String decode(String value) {
        if (value.isEmpty()) {
            return null;
        }
        if (!value.startsWith("=")) {
            throw new IllegalArgumentException(value);
        }
        return StringUtils.decodeUrl(value.substring(1));
    }
}
