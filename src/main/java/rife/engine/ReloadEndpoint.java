/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.ExceptionUtils;
import rife.tools.StringUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// the part of the Reloader that runs inside the application
class ReloadEndpoint implements AutoCloseable {
    static final String PATH = Router.RESERVED_PATH_PREFIX + "reload";
    static final String MARKER = "data-rife-reload";
    static final String WATCHER_THREAD = "rife-reload-watcher";

    private static final String EVENT_INSTANCE = "instance";
    private static final String EVENT_RELOAD = "reload";
    private static final String BODY_END = "</body>";
    private static final int RETRY_MS = 250;
    private static final int MAX_CONNECTIONS = 32;
    private static final Duration HEARTBEAT = Duration.ofSeconds(15);

    private final String instance_;
    private final SseBroadcaster broadcaster_;
    private final ReloadScanner scanner_;
    private final CompletableFuture<Void> supervisorWatch_;
    private final ScheduledExecutorService watcher_;
    private volatile boolean closed_ = false;

    // the reloader launches the application itself, so it has to be the parent
    // process, which keeps a property that lingers in an environment or in a
    // startup script from opening this endpoint in a deployment
    static ReloadEndpoint activate() {
        var instance = System.getProperty(Reloader.INSTANCE_PROPERTY);
        var supervisor = System.getProperty(Reloader.SUPERVISOR_PROPERTY);
        if (null == instance ||
            instance.isEmpty() ||
            null == supervisor) {
            return null;
        }

        var handle = supervisorHandle(supervisor);
        if (null == handle) {
            return null;
        }

        return new ReloadEndpoint(instance, handle);
    }

    private static ProcessHandle supervisorHandle(String supervisor) {
        long pid;
        try {
            pid = Long.parseLong(supervisor.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        return ProcessHandle.current().parent()
            .filter(parent -> parent.pid() == pid && parent.isAlive())
            .orElse(null);
    }

    private ReloadEndpoint(String instance, ProcessHandle supervisor) {
        instance_ = instance;
        broadcaster_ = new SseBroadcaster().heartbeat(HEARTBEAT);
        watcher_ = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, WATCHER_THREAD);
            thread.setDaemon(true);
            return thread;
        });

        try {
            scanner_ = new ReloadScanner(
                ReloadScanner.directories(System.getProperty("java.class.path"), System.getProperty("jdk.module.path")),
                ReloadScanner.OTHER_FILES);

            // a killed reloader can't stop the application, which would then keep its port
            supervisorWatch_ = supervisor.onExit().thenRun(this::endWithSupervisor);

            watcher_.scheduleWithFixedDelay(this::scan, ReloadScanner.POLL_INTERVAL_MS, ReloadScanner.POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            watcher_.shutdownNow();
            broadcaster_.close();
            throw e;
        }

        Logger.getLogger("rife.engine").info("Reloading is active, the browser reloads through " + PATH);
    }

    private void endWithSupervisor() {
        if (!closed_) {
            Logger.getLogger("rife.engine").info("The reloader stopped, ending the application.");
            System.exit(0);
        }
    }

    // the files of a static resource base aren't on the classpath
    void watch(String path) {
        if (closed_) {
            return;
        }

        var directory = ReloadScanner.directory(path);
        if (directory != null) {
            scanner_.add(directory);
        }
    }

    // tells whether the response was turned into an event stream
    boolean connect(Context context) {
        if (context.method() != RequestMethod.GET) {
            context.setStatus(405);
            return false;
        }

        // a page from another site has no reason to open these streams
        if ("cross-site".equals(context.header("Sec-Fetch-Site"))) {
            context.setStatus(403);
            return false;
        }

        if (closed_ ||
            broadcaster_.connectionCount() >= MAX_CONNECTIONS) {
            // an error status would keep the browser from ever connecting again,
            // an empty stream lets it come back when the application is up
            context.setContentType("text/event-stream");
            context.print("retry: " + RETRY_MS + "\n\n");
            return false;
        }

        var connection = context.sse(broadcaster_);
        if (closed_) {
            connection.close();
            return true;
        }

        connection.send(new ServerSentEvent()
            .name(EVENT_INSTANCE)
            .retry(RETRY_MS)
            .data(instance_));
        return true;
    }

    // templates and resource bundles are reloaded by the application itself,
    // the browser merely has to fetch the page again
    private void scan() {
        try {
            if (scanner_.settled() && !closed_) {
                broadcaster_.send(new ServerSentEvent().name(EVENT_RELOAD).data(instance_));
            }
        } catch (Throwable e) {
            // a single failed scan or broadcast must not end the watching,
            // it would leave a stream that looks healthy but never reloads
            Logger.getLogger("rife.engine").warning("Error while watching for reloadable changes\n" + ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    // the window property keeps htmx-boosted pages from opening a stream per
    // navigation, a stream that failed for good doesn't count as one
    String script(String gateUrl) {
        if (closed_) {
            return "";
        }

        return """
            <script %s>\
            if(!window.rifeReload||window.rifeReload.readyState===2){\
            window.rifeReload=new EventSource("%s");\
            window.rifeReload.addEventListener("%s",e=>{if(e.data!=="%s")location.replace(location.href)});\
            window.rifeReload.addEventListener("%s",()=>location.replace(location.href))}\
            </script>"""
            .formatted(MARKER, StringUtils.encodeJson(gateUrl + PATH), EVENT_INSTANCE, StringUtils.encodeJson(instance_), EVENT_RELOAD);
    }

    // htmx fragments are swapped into a page that has the script already
    void inject(Context context, Response response) {
        if (closed_ ||
            context.isHxRequest() ||
            !(response instanceof AbstractResponse abstract_response)) {
            return;
        }

        var buffer = abstract_response.textBuffer_;
        var content_type = abstract_response.getContentType();
        if (null == buffer ||
            buffer.isEmpty() ||
            null == content_type ||
            !content_type.startsWith("text/html")) {
            return;
        }

        // a length that was set explicitly no longer matches once the script is added
        if (abstract_response.containsHeader("Content-Length")) {
            return;
        }

        for (var text : buffer) {
            if (text.toString().contains(MARKER)) {
                return;
            }
        }

        for (var i = buffer.size() - 1; i >= 0; i--) {
            var text = buffer.get(i).toString();
            var index = lastIndexOfIgnoreCase(text, BODY_END);
            if (index != -1) {
                buffer.set(i, text.substring(0, index) + script(context.gateUrl()) + text.substring(index));
                return;
            }
        }

        // a page without a body tag still reloads, the script simply comes last
        buffer.add(script(context.gateUrl()));
    }

    private static int lastIndexOfIgnoreCase(String text, String part) {
        for (var i = text.length() - part.length(); i >= 0; i--) {
            if (text.regionMatches(true, i, part, 0, part.length())) {
                return i;
            }
        }
        return -1;
    }

    public void close() {
        closed_ = true;
        supervisorWatch_.cancel(false);
        watcher_.shutdownNow();
        broadcaster_.close();
    }
}
