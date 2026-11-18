/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import rife.config.RifeConfig;
import rife.engine.*;
import rife.ioc.HierarchicalProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

/**
 * Runs the RIFE2 site of an app in the app's own process, without a server:
 * each request of the app's web view is handed to a {@link SiteRunner} and
 * its response is handed back while it's written.
 * <p>The site is the one that the app names in
 * {@code META-INF/services/rife.engine.Site}, or that the
 * {@link SiteProvider} makes which it names in
 * {@code META-INF/services/rife.nativeapp.SiteProvider}, which is used when
 * both are named, unless one is given to the constructor. What the app
 * tells the site, before its first request, are properties that the site
 * can read in its {@code setup()}, like
 * {@link #DATA_PROPERTY} for the directory where it keeps what should last.
 * <p>The C functions of {@link EntryPoints} call one instance of this
 * class, which runs on the JVM as well, for tests and for checks before
 * the native build.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public class NativeApp implements AutoCloseable {
    /**
     * The platform that the app runs on, like {@code ios}, {@code mac} or
     * {@code android}. When it's set, the {@code context:nativeAppScript} value of
     * a template includes the script that does in a page what the app's web
     * view doesn't do by itself.
     *
     * @since 1.11.0
     */
    public static final String PLATFORM_PROPERTY = "rife.nativeapp.platform";
    /**
     * The directory where the app keeps what should last, the cookies of
     * the site among them.
     *
     * @since 1.11.0
     */
    public static final String DATA_PROPERTY = "rife.nativeapp.data";
    /**
     * The scheme of the address that the web view has the site at.
     *
     * @since 1.11.0
     */
    public static final String SCHEME_PROPERTY = "rife.nativeapp.scheme";
    /**
     * The host of the address that the web view has the site at.
     *
     * @since 1.11.0
     */
    public static final String HOST_PROPERTY = "rife.nativeapp.host";
    /**
     * The directory for temporary files, uploads among them, when the one
     * of Java isn't one that the app may write in.
     *
     * @since 1.11.0
     */
    public static final String TEMP_PROPERTY = "rife.nativeapp.temp";
    /**
     * The version of the app.
     *
     * @since 1.11.0
     */
    public static final String VERSION_PROPERTY = "rife.nativeapp.version";
    /**
     * {@code true} when the app takes each answer all at once, on the thread
     * that asked, like a web view of Android does. A response that goes on
     * after the request returns, like an event stream, is then cancelled
     * when it returns, and nothing is told from another thread.
     *
     * @since 1.11.0
     */
    public static final String WHOLE_RESPONSES_PROPERTY = "rife.nativeapp.wholeResponses";
    /**
     * The build number of the app.
     *
     * @since 1.11.0
     */
    public static final String BUILD_NUMBER_PROPERTY = "rife.nativeapp.buildNumber";

    /**
     * The scheme that the site is at when the app doesn't give one.
     *
     * @since 1.11.0
     */
    public static final String DEFAULT_SCHEME = "app";
    /**
     * The host that the site is at when the app doesn't give one.
     *
     * @since 1.11.0
     */
    public static final String DEFAULT_HOST = "site";
    /**
     * The classpath directory that the static files of the site are served
     * from, which is where an app puts its {@code src/main/webapp}.
     *
     * @since 1.11.0
     */
    public static final String STATIC_RESOURCE_BASE = "webapp";
    /**
     * The classpath directory of the script that the pages need inside an
     * app, which a site runner only serves when {@link #PLATFORM_PROPERTY} is set.
     * It's no package, so that it's also found when RIFE2 is a module.
     *
     * @since 1.11.0
     */
    public static final String SCRIPT_RESOURCE_BASE = "META-INF/rife2-native-app";

    /**
     * A request that ended with all of its answer, as {@link EntryPoints} tells it.
     *
     * @since 1.11.0
     */
    public static final int COMPLETED = 0;
    /**
     * A request that failed before all of its answer was given.
     *
     * @since 1.11.0
     */
    public static final int FAILED = 1;
    /**
     * A request that the app cancelled.
     *
     * @since 1.11.0
     */
    public static final int CANCELLED = 2;

    private static final String SAVED = "rife.nativeapp.saved";

    private final Map<String, String> properties_ = new ConcurrentHashMap<>();
    // the requests that are being handled, by the number that the app gave
    // them, to cancel them
    private final Map<Long, SiteRequest> active_ = new ConcurrentHashMap<>();
    // a cancel that came before its request was taken, which another thread
    // can do; bounded, since one for a request that already ended stays
    private final Set<Long> cancelledEarly_ = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 64;
        }
    }));
    private Site site_;
    private SiteRunner runner_;
    // a site that couldn't start isn't tried again for each request
    private RuntimeException failure_;

    /**
     * Creates an app whose site is named in
     * {@code META-INF/services/rife.engine.Site}, or made by the
     * {@link SiteProvider} named in
     * {@code META-INF/services/rife.nativeapp.SiteProvider}. The system
     * properties that start with {@code rife.nativeapp.} are its first
     * properties.
     *
     * @since 1.11.0
     */
    public NativeApp() {
        for (var name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith("rife.nativeapp.") && !name.equals(SAVED)) {
                properties_.put(name, System.getProperty(name));
            }
        }
    }

    /**
     * Creates an app with the site that's given. The system properties
     * that start with {@code rife.nativeapp.} are its first properties.
     *
     * @param site the site
     * @since 1.11.0
     */
    public NativeApp(Site site) {
        this();
        if (null == site) throw new IllegalArgumentException("site can't be null");
        site_ = site;
    }

    /**
     * Sets a property of the site, before its first request.
     *
     * @param name  the name of the property
     * @param value its value
     * @return this app
     * @throws IllegalStateException when the site already started
     * @since 1.11.0
     */
    public NativeApp property(String name, String value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (null == value) throw new IllegalArgumentException("value can't be null");

        synchronized (this) {
            if (runner_ != null || failure_ != null) throw new IllegalStateException("the site already started");
            properties_.put(name, value);
        }
        return this;
    }

    /**
     * Retrieves the runner of the site, which is started by the first call.
     *
     * @return the site runner
     * @throws RuntimeException why the site couldn't start, every time
     * @since 1.11.0
     */
    public synchronized SiteRunner runner() {
        if (failure_ != null) {
            throw failure_;
        }
        if (null == runner_) {
            try {
                // A site inside an app has no display, and images are drawn
                // without one, the only way that a native image on a Mac draws
                // them; an app that wants otherwise says so first.
                if (null == System.getProperty("java.awt.headless")) {
                    System.setProperty("java.awt.headless", "true");
                }
                var properties = new HierarchicalProperties();
                properties_.forEach(properties::put);
                var temp = properties_.get(TEMP_PROPERTY);
                if (temp != null) {
                    RifeConfig.global().setTempPath(temp);
                }
                var runner = new SiteRunner(properties)
                    .scheme(properties_.getOrDefault(SCHEME_PROPERTY, DEFAULT_SCHEME))
                    .serverName(properties_.getOrDefault(HOST_PROPERTY, DEFAULT_HOST))
                    .staticUberJarResourceBase(STATIC_RESOURCE_BASE);
                // a web view doesn't keep the cookies of an address like this one
                var data = properties_.get(DATA_PROPERTY);
                runner.cookieJar(null == data ? new SiteCookieJar() : new SiteCookieJar(new SiteCookieFileStore(new File(data, "cookies"))));
                runner_ = runner.start(null == site_ ? namedSite(properties) : site_);
            } catch (RuntimeException e) {
                failure_ = e;
                throw e;
            } catch (LinkageError | ServiceConfigurationError e) {
                // a class that's missing or can't be made, which another try doesn't change
                failure_ = new IllegalStateException(e);
                throw failure_;
            }
        }
        return runner_;
    }

    private static Site namedSite(HierarchicalProperties properties) {
        var provider = ServiceLoader.load(SiteProvider.class).findFirst();
        if (provider.isPresent()) {
            return provider.get().site(properties);
        }
        return ServiceLoader.load(Site.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No site is named in META-INF/services/" + Site.class.getName() + " or " + SiteProvider.class.getName()));
    }

    /**
     * Handles a request of the app. The listener is told the status and the
     * headers once, the content as it's written, and once how the request
     * ended. A request that the site couldn't handle at all is only told
     * that it failed. Nothing is told after the end.
     *
     * @param id       a number that no other request had before, to
     *                 {@link #cancel} it by
     * @param url      the address, either a path or an absolute address of
     *                 the site, with its query string
     * @param method   the request method, like {@code GET}
     * @param headers  lines of a name, a colon and a value
     * @param body     what was posted; or {@code null}
     * @param listener what's told of the response
     * @since 1.11.0
     */
    public void handle(long id, String url, String method, String headers, byte[] body, SiteResponseListener listener) {
        if (Boolean.parseBoolean(properties_.get(WHOLE_RESPONSES_PROPERTY))) {
            handleWhole(id, url, method, headers, body, listener);
            return;
        }
        var told = new Told(id, listener);
        handleTold(id, url, method, headers, body, told, told);
    }

    // only the thread that asked tells the listener, and only until the request returns
    private void handleWhole(long id, String url, String method, String headers, byte[] body, SiteResponseListener listener) {
        var asking = Thread.currentThread();
        var open = new AtomicBoolean(true);
        var told = new Told(id, listener);
        var guarded = new SiteResponseListener() {
            private boolean allowed() {
                return open.get() && Thread.currentThread() == asking;
            }

            public void committed(SiteResponse response) {
                if (allowed()) {
                    told.committed(response);
                }
            }

            public void content(byte[] bytes, int offset, int length) {
                if (allowed()) {
                    told.content(bytes, offset, length);
                }
            }

            public void completed() {
                if (allowed()) {
                    told.completed();
                }
            }

            public void failed(Throwable exception) {
                if (allowed()) {
                    told.failed(exception);
                }
            }

            public void cancelled() {
                if (allowed()) {
                    told.cancelled();
                }
            }
        };
        handleTold(id, url, method, headers, body, told, guarded);
        if (!told.over_.get()) {
            cancel(id);
            // when another thread was writing to it, that one is told the
            // cancel, and ignored
            told.cancelled();
        }
        open.set(false);
    }

    private void handleTold(long id, String url, String method, String headers, byte[] body, Told told, SiteResponseListener forRunner) {
        try {
            var request = new SiteRequest(RequestMethod.valueOf(method));
            told.request_ = request;
            if (headers != null) {
                for (var line : headers.split("\n")) {
                    var colon = line.indexOf(':');
                    if (colon > 0) {
                        request.addHeader(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
                    }
                }
            }
            if (body != null) {
                request.bodyAsBytes(body);
            }
            active_.put(id, request);
            if (cancelledEarly_.remove(id)) {
                active_.remove(id, request);
                request.cancel();
            }
            var response = runner().handle(url, request, forRunner);
            // an event stream that goes on after this can still be cancelled
            if (!response.isDetached()) {
                active_.remove(id, request);
            }
        } catch (Throwable e) {
            told.failed(e);
        }
    }

    /**
     * Tells the site that nobody waits for the response of a request
     * anymore.
     *
     * @param id the number that the request was handled with
     * @since 1.11.0
     */
    public void cancel(long id) {
        var request = active_.remove(id);
        if (null == request) {
            cancelledEarly_.add(id);
            // the request can have been taken between the two, and missed this
            request = active_.remove(id);
            if (null == request) {
                return;
            }
            cancelledEarly_.remove(id);
        }
        request.cancel();
    }

    /**
     * Stops the site, when it was started.
     *
     * @since 1.11.0
     */
    public synchronized void stop() {
        if (runner_ != null) {
            runner_.stop();
        }
    }

    /**
     * Stops the site, when it was started, like {@link #stop()}.
     *
     * @since 1.11.0
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * The headers of a response as lines of a name, a colon and a value,
     * without its cookies, which the site's cookie jar keeps.
     *
     * @param response the response
     * @return the lines
     * @since 1.11.0
     */
    public static String headerLines(SiteResponse response) {
        var lines = new StringBuilder();
        for (var header : response.getSingleValueHttpHeaders().entrySet()) {
            // a line break in a value would tell the app another header
            lines.append(oneLine(header.getKey())).append(": ").append(oneLine(header.getValue())).append('\n');
        }
        return lines.toString();
    }

    private static String oneLine(String text) {
        // and a zero would end the text where the app reads it
        return text.replaceAll("[\\r\\n\\x00]+", " ");
    }

    // tells the app once that a request is over, and forgets the request
    private class Told implements SiteResponseListener {
        private final long id_;
        private final SiteResponseListener listener_;
        private final AtomicBoolean over_ = new AtomicBoolean();
        private volatile SiteRequest request_;

        Told(long id, SiteResponseListener listener) {
            id_ = id;
            listener_ = listener;
        }

        // what a thread of the site writes after the end goes nowhere, the app let go of the request
        public void committed(SiteResponse response) {
            if (!over_.get()) {
                listener_.committed(response);
            }
        }

        public void content(byte[] bytes, int offset, int length) {
            if (!over_.get()) {
                listener_.content(bytes, offset, length);
            }
        }

        public void completed() {
            if (ended()) {
                listener_.completed();
            }
        }

        public void failed(Throwable exception) {
            if (ended()) {
                listener_.failed(exception);
            }
        }

        public void cancelled() {
            if (ended()) {
                listener_.cancelled();
            }
        }

        private boolean ended() {
            if (request_ != null) {
                active_.remove(id_, request_);
            }
            return over_.compareAndSet(false, true);
        }
    }

    /**
     * Asks the site for addresses on the JVM and says what it answered, to
     * see what's wrong in seconds rather than after a native build. An
     * argument {@code -Dname=value} sets a property of the site first, as
     * the {@code ask} check of rife2-native-app does for a native library;
     * so do the system properties that start with {@code rife.nativeapp.}.
     * The system property {@code rife.nativeapp.saved} names a directory
     * that gets every answer as a file.
     *
     * @param arguments the properties and the addresses
     * @since 1.11.0
     */
    public static void main(String[] arguments) {
        var app = new NativeApp();
        var saved = System.getProperty(SAVED);
        var addresses = new java.util.ArrayList<String>();
        for (var argument : arguments) {
            var equals = argument.indexOf('=');
            if (argument.startsWith("-D") && equals > 2) {
                app.property(argument.substring(2, equals), argument.substring(equals + 1));
            } else {
                addresses.add(argument);
            }
        }
        var failed = false;
        var number = 0L;
        for (var url : addresses) {
            var started = System.nanoTime();
            var size = new int[1];
            var checksum = new CRC32();
            var said = new StringBuilder();
            var answer = new ByteArrayOutputStream();
            app.handle(++number, url, "GET", "", null, new SiteResponseListener() {
                public void committed(SiteResponse response) {
                    said.append(response.getStatus()).append(' ').append(response.getContentType()).append(response.isHandled() ? "" : " unhandled");
                }

                public void content(byte[] bytes, int offset, int length) {
                    size[0] += length;
                    checksum.update(bytes, offset, length);
                    if (saved != null) {
                        answer.write(bytes, offset, length);
                    }
                }

                public void completed() {
                    said.append(" completed");
                }

                public void failed(Throwable exception) {
                    said.append(" failed: ").append(exception);
                }

                public void cancelled() {
                    said.append(" cancelled");
                }
            });
            failed |= said.indexOf(" failed: ") >= 0;
            if (saved != null) {
                try {
                    var name = url.replaceFirst("^/", "").replace('/', '_');
                    Files.createDirectories(Path.of(saved));
                    Files.write(Path.of(saved, name.isEmpty() ? "index" : name), answer.toByteArray());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            System.out.println(url + " -> " + said + ", " + size[0] + " bytes with checksum " + checksum.getValue() + " in " + (System.nanoTime() - started) / 1_000_000 + " ms");
        }
        app.stop();
        if (failed) {
            System.exit(1);
        }
    }
}
