/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.Cookie;
import rife.config.RifeConfig;
import rife.engine.exceptions.MultipartFileErrorException;
import rife.engine.exceptions.MultipartFileTooBigException;
import rife.engine.exceptions.MultipartInvalidUploadDirectoryException;
import rife.engine.exceptions.MultipartRequestException;
import rife.ioc.HierarchicalProperties;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Runs a site inside your own application, in the same process, without a
 * servlet container and without a socket.
 * <p>Your application passes the requests that it receives to the site, from
 * a web view of its own for instance, and gets the responses back:
 * <pre>
 * var runner = new SiteRunner()
 *     .scheme("myapp")
 *     .serverName("app")
 *     .staticUberJarResourceBase("webapp")
 *     .start(new MySite());
 *
 * var response = runner.handle("/login", new SiteRequest(RequestMethod.POST)
 *     .header("Content-Type", "application/x-www-form-urlencoded")
 *     .bodyAsBytes(body));
 * </pre>
 * <p>A response is returned once it's complete, or you can receive it while
 * it's being written with a {@link SiteResponseListener}, which is better
 * suited for large downloads and event streams.
 * <p>Requests can be handled from any number of threads at the same time.
 * A request runs on the thread that calls {@code handle}, and the only
 * thread it can start is a short one that purges the continuations that
 * expired. Sessions are kept in memory and identified with a
 * cookie, and an optional {@link SiteCookieJar} can act as the browser for
 * the cookies when your web view doesn't keep them.
 * <p>When no route handles a request and no static file is served for it,
 * the response has the status {@code 404} and {@link SiteResponse#isHandled()}
 * returns {@code false}, so that your application can still answer it in its
 * own way.
 * <p>Several site runners can run side by side, but keep in mind that the
 * {@linkplain RifeConfig.EngineConfig engine configuration} is shared by
 * all the sites of a process. Its proxy root URL doesn't apply to a site
 * runner, since you configure the scheme and the server name of the runner
 * directly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SiteRequest
 * @see SiteResponse
 * @since 1.11.0
 */
public class SiteRunner implements AutoCloseable {
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_SERVER_NAME = "localhost";
    public static final int DEFAULT_SERVER_PORT = -1;
    public static final String DEFAULT_CONTEXT_PATH = "";
    public static final int DEFAULT_SESSION_TIMEOUT = 1800;
    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public static final int DEFAULT_MAX_FORM_CONTENT_SIZE = 200000;
    public static final int DEFAULT_MAX_FORM_PARAMETERS = 1000;

    static final String SESSION_URL_PARAMETER = "jsessionid";
    private static final long LOADED = System.currentTimeMillis();
    static final String WEBJARS_RESOURCE_BASE = "META-INF/resources";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final HierarchicalProperties properties_;
    private final Gate gate_ = new Gate();
    private final ConcurrentHashMap<String, SiteSession> sessions_ = new ConcurrentHashMap<>();
    private final Set<SiteResponse> detachedResponses_ = ConcurrentHashMap.newKeySet();

    private volatile String scheme_ = DEFAULT_SCHEME;
    private volatile String serverName_ = DEFAULT_SERVER_NAME;
    private volatile int serverPort_ = DEFAULT_SERVER_PORT;
    private volatile String contextPath_ = DEFAULT_CONTEXT_PATH;
    private volatile int sessionTimeout_ = DEFAULT_SESSION_TIMEOUT;
    private volatile String sessionCookieName_ = DEFAULT_SESSION_COOKIE_NAME;
    private volatile boolean sessionUrlParameters_ = false;
    private volatile boolean secure_ = false;
    private volatile int maxFormContentSize_ = DEFAULT_MAX_FORM_CONTENT_SIZE;
    private volatile int maxFormParameters_ = DEFAULT_MAX_FORM_PARAMETERS;
    private volatile Path staticResourceBase_ = null;
    private volatile String staticClasspathBase_ = null;
    private volatile SiteStaticResources staticResources_ = null;
    private volatile SiteCookieJar cookieJar_ = null;
    private volatile LongSupplier clock_ = System::currentTimeMillis;
    private volatile boolean started_ = false;
    private volatile boolean stopped_ = false;

    /**
     * Instantiates a new site runner, whose properties have the system
     * properties as their parent.
     *
     * @since 1.11.0
     */
    public SiteRunner() {
        this(new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance()));
    }

    /**
     * Instantiates a new site runner that will set up its site with the
     * properties that you provide.
     * <p>They're used as they are, without the system properties being
     * added as a parent.
     *
     * @param properties the properties of the site
     * @since 1.11.0
     */
    public SiteRunner(HierarchicalProperties properties) {
        if (null == properties) throw new IllegalArgumentException("properties can't be null");

        properties_ = properties;
    }

    /**
     * Configures the scheme of the origin that the site is reached at, which
     * can be a custom scheme that only your application uses.
     *
     * @param scheme the scheme (defaults to {@code http})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner scheme(String scheme) {
        if (null == scheme) throw new IllegalArgumentException("scheme can't be null");
        if (scheme.isEmpty()) throw new IllegalArgumentException("scheme can't be empty");

        scheme_ = scheme;
        return this;
    }

    /**
     * Configures the host name of the origin that the site is reached at.
     *
     * @param serverName the host name (defaults to {@code localhost})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner serverName(String serverName) {
        if (null == serverName) throw new IllegalArgumentException("serverName can't be null");
        if (serverName.isEmpty()) throw new IllegalArgumentException("serverName can't be empty");

        serverName_ = serverName;
        return this;
    }

    /**
     * Configures the port of the origin that the site is reached at.
     *
     * @param serverPort the port, or {@code -1} to leave it out of the URLs
     *                   (defaults to {@code -1})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner serverPort(int serverPort) {
        serverPort_ = serverPort;
        return this;
    }

    /**
     * Configures the context path that the site is reached at. The paths
     * that are handled have to start with it.
     *
     * @param contextPath the context path (defaults to none)
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner contextPath(String contextPath) {
        if (null == contextPath || contextPath.equals("/")) {
            contextPath = DEFAULT_CONTEXT_PATH;
        }
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        if (!contextPath.isEmpty() && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        contextPath_ = contextPath;
        return this;
    }

    String getScheme() {
        return scheme_;
    }

    String getServerName() {
        return serverName_;
    }

    int getServerPort() {
        return serverPort_;
    }

    /**
     * Retrieves the context path that the site is reached at.
     *
     * @return the context path, which is empty when there's none
     * @see #contextPath(String)
     * @since 1.11.0
     */
    public String getContextPath() {
        return contextPath_;
    }

    /**
     * Retrieves the root URL of the origin that the site is reached at,
     * {@code myapp://app} for instance.
     *
     * @return the root URL, without the context path
     * @since 1.11.0
     */
    public String getServerRootUrl() {
        return formatRootUrl(scheme_, serverName_, serverPort_);
    }

    // the port is left out when there's none, or when it's the default one of the scheme
    static String formatRootUrl(String scheme, String serverName, int port) {
        var root = new StringBuilder();
        root.append(scheme).append("://").append(serverName);
        if (port > -1 &&
            !(port == 80 && "http".equalsIgnoreCase(scheme)) &&
            !(port == 443 && "https".equalsIgnoreCase(scheme))) {
            root.append(':').append(port);
        }
        return root.toString();
    }

    /**
     * Configures the directory with the static files of the site. They're
     * served for the {@code GET} and {@code HEAD} requests that no route
     * handles and for the ones that end with a
     * {@linkplain RifeConfig.EngineConfig#getPassThroughSuffixes() pass-through suffix}.
     * <p>Like with {@link Server}, there's one static resource base, so this
     * replaces a base that was set before. The {@code META-INF/resources}
     * directory of web jars is always searched as well, even without a
     * static resource base.
     *
     * @param base the path of the directory, or {@code null} for none
     * @return the instance of the site runner that's being configured
     * @see #staticUberJarResourceBase(String)
     * @since 1.11.0
     */
    public SiteRunner staticResourceBase(String base) {
        staticResourceBase_ = null == base ? null : Path.of(base);
        staticClasspathBase_ = null;
        staticResources_ = null;
        return this;
    }

    /**
     * Configures the base directory in the classpath where the static files
     * of the site will be looked up from, which in a native image means its
     * embedded resources.
     * <p>Like with {@link Server}, there's one static resource base, so this
     * replaces a base that was set before. The {@code META-INF/resources}
     * directory of web jars is always searched as well.
     * <p>This can't be the root of the classpath, since your classes and
     * your configuration would then be served too.
     *
     * @param base the base directory in the classpath, {@code webapp} for
     *             instance
     * @return the instance of the site runner that's being configured
     * @see #staticResourceBase(String)
     * @since 1.11.0
     */
    public SiteRunner staticUberJarResourceBase(String base) {
        if (null == base) throw new IllegalArgumentException("base can't be null");

        while (base.startsWith("/")) {
            base = base.substring(1);
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.isEmpty()) throw new IllegalArgumentException("base can't be the root of the classpath");

        staticClasspathBase_ = base;
        staticResourceBase_ = null;
        staticResources_ = null;
        return this;
    }

    /**
     * Configures after how many seconds a session that isn't used will
     * expire.
     *
     * @param seconds the timeout, or zero or less for sessions that only end
     *                when they're invalidated (defaults to {@code 1800})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner sessionTimeout(int seconds) {
        sessionTimeout_ = seconds;
        return this;
    }

    /**
     * Configures the name of the cookie that identifies the session.
     *
     * @param name the name of the cookie (defaults to {@code JSESSIONID})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner sessionCookieName(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        sessionCookieName_ = name;
        return this;
    }

    String getSessionCookieName() {
        return sessionCookieName_;
    }

    /**
     * Configures whether a session can also be identified by a
     * {@code ;jsessionid=} path parameter, which {@link Response#encodeURL}
     * will then add when the session didn't come from a cookie.
     * <p>This is disabled by default, since such a link can put someone in
     * another person's session.
     *
     * @param enabled {@code true} to identify sessions in URLs; or
     *                {@code false} otherwise (defaults to {@code false})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner sessionUrlParameters(boolean enabled) {
        sessionUrlParameters_ = enabled;
        return this;
    }

    boolean isSessionUrlParameters() {
        return sessionUrlParameters_;
    }

    /**
     * Configures whether the origin is secure even though its scheme isn't
     * {@code https}, which is the case for a custom scheme that can only be
     * reached from within your application.
     * <p>The requests will then be secure, so that cookies that are marked
     * as secure can be set and sent.
     *
     * @param secure {@code true} if the origin is secure; or
     *               {@code false} otherwise (defaults to {@code false})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner secure(boolean secure) {
        secure_ = secure;
        return this;
    }

    boolean isSecure() {
        return secure_;
    }

    /**
     * Configures the maximum size of a urlencoded form body. A request with
     * a larger body will be answered with {@code 400}, like with Jetty.
     *
     * @param size the maximum size in bytes, or a negative number for no
     *             limit (defaults to {@code 200000})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner maxFormContentSize(int size) {
        maxFormContentSize_ = size;
        return this;
    }

    int getMaxFormContentSize() {
        return maxFormContentSize_;
    }

    /**
     * Configures the maximum number of parameters in a urlencoded form body,
     * where all the values of one name count as one parameter. A request
     * with more parameters will be answered with {@code 400}, like with
     * Jetty.
     *
     * @param parameters the maximum number of parameters, or a negative
     *                   number for no limit (defaults to {@code 1000})
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner maxFormParameters(int parameters) {
        maxFormParameters_ = parameters;
        return this;
    }

    int getMaxFormParameters() {
        return maxFormParameters_;
    }

    /**
     * Configures the cookie jar that keeps the cookies of the responses and
     * adds them to the requests, like a browser does.
     * <p>The jar then replaces the {@code Cookie} header of the requests that
     * it applies to, so that only the cookies that the site set reach it.
     *
     * @param jar the cookie jar, or {@code null} when your application keeps
     *            the cookies itself (defaults to none)
     * @return the instance of the site runner that's being configured
     * @since 1.11.0
     */
    public SiteRunner cookieJar(SiteCookieJar jar) {
        cookieJar_ = jar;
        return this;
    }

    SiteCookieJar getCookieJar() {
        return cookieJar_;
    }

    // lets tests control the time for the sessions
    void clock(LongSupplier clock) {
        clock_ = clock;
    }

    long now() {
        return clock_.getAsLong();
    }

    /**
     * Retrieves the hierarchical properties that the site is set up with.
     *
     * @return the properties of the site
     * @since 1.11.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Sets up and deploys a site, after which it will handle requests.
     * <p>Errors while the site is set up are handled like in a deployed
     * site: the requests will get the error page, or the error is thrown
     * when {@linkplain RifeConfig.EngineConfig#getPrettyEngineExceptions() pretty engine exceptions}
     * are disabled.
     * <p>When you set a {@linkplain RifeConfig.EngineConfig#setFileUploadPath(String) file upload path}
     * of your own, the uploads that an earlier run of your application left
     * there are removed, since they're otherwise only deleted when the JVM
     * exits.
     *
     * @param site the site to run
     * @return the instance of the site runner that's being started
     * @throws IllegalArgumentException when the site is {@code null}
     * @throws IllegalStateException    when the runner was already started
     *                                  or stopped
     * @since 1.11.0
     */
    public SiteRunner start(Site site) {
        if (null == site) throw new IllegalArgumentException("site can't be null");

        synchronized (this) {
            if (started_) throw new IllegalStateException("the site runner has already been started");
            if (stopped_) throw new IllegalStateException("the site runner has been stopped and can't be started again");
            gate_.setup(properties_, site);
            started_ = true;
        }
        removeEarlierUploads();
        return this;
    }

    // uploads are only deleted when the JVM exits, which an app on a phone
    // rarely does, so the ones that an earlier process left in the upload
    // directory of the application are removed, but never in the shared
    // default one
    private static void removeEarlierUploads() {
        var path = RifeConfig.engine().getFileUploadPath();
        if (path.equals(RifeConfig.global().getTempPath() + File.separator + RifeConfig.EngineConfig.DEFAULT_FILE_UPLOAD_RIFE_FOLDER)) {
            return;
        }

        var uploads = new File(path).listFiles((directory, name) -> name.startsWith("upl") && name.endsWith(".tmp"));
        if (null == uploads) {
            return;
        }
        for (var upload : uploads) {
            if (upload.isFile() && upload.lastModified() < LOADED) {
                upload.delete();
            }
        }
    }

    /**
     * Retrieves the site that's being run.
     *
     * @return the site; or
     * <p>{@code null} if the runner hasn't been started yet
     * @since 1.11.0
     */
    public Site getSite() {
        return gate_.getSite();
    }

    /**
     * Shuts the site down, closes the detached event streams that are still
     * open and forgets the sessions. Calling this method more than once
     * doesn't have any additional effect.
     * <p>Schedulers and datasources belong to the whole process, so they
     * aren't stopped here. When your application ends, you can stop them with
     * {@link rife.scheduler.Scheduler#stopAllActiveSchedulers()} and
     * {@link rife.database.Datasource#closeAllActiveDatasources()}.
     * <p>Closing an event stream waits for the event that's being written to
     * its listener. This means that you shouldn't stop the runner from a
     * thread that your listener is waiting for, since neither of them would
     * be able to continue.
     *
     * @since 1.11.0
     */
    public void stop() {
        synchronized (this) {
            if (stopped_) {
                return;
            }
            stopped_ = true;
        }
        try {
            if (started_) {
                gate_.destroy();
            }
        } finally {
            if (!isServingAfterStop()) {
                for (var response : new ArrayList<>(detachedResponses_)) {
                    closeDetached(response);
                }
                detachedResponses_.clear();
                for (var session : sessions_.values()) {
                    session.expire();
                }
                sessions_.clear();
            }
        }
    }

    /**
     * Stops this site runner, which makes it possible to use it inside a
     * try-with-resources block.
     *
     * @see #stop()
     * @since 1.11.0
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Indicates whether requests are still handled after this runner was
     * stopped and its site was shut down. This is what out-of-container
     * testing has always done. The event streams and the sessions are then
     * kept as well.
     * <p>This returns {@code false} by default.
     *
     * @return {@code true} if requests are still handled after a stop; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    protected boolean isServingAfterStop() {
        return false;
    }

    /**
     * Indicates whether a request can be handled more than once, which
     * out-of-container tests have always been able to do.
     * <p>This returns {@code false} by default.
     *
     * @return {@code true} if a request can be handled again; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    protected boolean isReusingRequests() {
        return false;
    }

    /**
     * Indicates whether a detached event stream stays open when there's no
     * listener for the response, so that out-of-container tests can look at
     * the events that are sent afterwards.
     * <p>This returns {@code false} by default, since there's otherwise
     * nothing that could receive those events.
     *
     * @return {@code true} if detached event streams stay open; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    protected boolean isKeepingDetachedStreamsOpen() {
        return false;
    }

    /**
     * Indicates whether the cookies that a site sets are checked for
     * characters that can't be sent, like with Jetty. Out-of-container tests
     * have always been able to set any value.
     * <p>This returns {@code true} by default.
     *
     * @return {@code true} if cookies are checked; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    protected boolean isValidatingCookies() {
        return true;
    }

    /**
     * Indicates whether static files are served for the requests that no
     * route handles. Out-of-container tests have never served them.
     * <p>This returns {@code true} by default.
     *
     * @return {@code true} if static files are served; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    protected boolean isServingStaticResources() {
        return true;
    }

    /**
     * Handles a {@code GET} request.
     *
     * @param url the path with its raw query string, or a complete URL of the
     *            origin of this runner
     * @return the response
     * @throws IllegalArgumentException when the URL is {@code null}
     * @throws IllegalStateException    when the runner isn't running
     * @see #handle(String, SiteRequest)
     * @since 1.11.0
     */
    public SiteResponse handle(String url) {
        return handle(url, new SiteRequest());
    }

    /**
     * Handles a request and returns its response once it's complete.
     * <p>The exceptions that the site doesn't handle itself are thrown, like
     * a deployed site leaves them to the servlet container. A detached event
     * stream is closed as soon as its element returns, since its later events
     * could only reach a listener.
     *
     * @param url     the path with its raw query string, or a complete URL of
     *                the origin of this runner
     * @param request the request, which can only be handled once
     * @return the response
     * @throws IllegalArgumentException when an argument is {@code null}
     * @throws IllegalStateException    when the runner isn't running or the
     *                                  request was already handled
     * @see #handle(String, SiteRequest, SiteResponseListener)
     * @since 1.11.0
     */
    public SiteResponse handle(String url, SiteRequest request) {
        var response = prepareHandling(url, request, null);
        process(url, request, response);
        return response;
    }

    /**
     * Handles a request and gives its response to a listener while it's being
     * written.
     * <p>This method returns once the element is done, while the response of
     * a detached event stream continues after that. The failures of the site
     * go to {@link SiteResponseListener#failed}, and so do the requests that
     * arrive after the runner was stopped. This method only throws for a
     * mistake in how it's called, and the listener then isn't called at
     * all.
     *
     * @param url      the path with its raw query string, or a complete URL
     *                 of the origin of this runner
     * @param request  the request, which can only be handled once
     * @param listener the listener that receives the response
     * @return the response, for its status and its headers
     * @throws IllegalArgumentException when an argument is {@code null}
     * @throws IllegalStateException    when the runner wasn't started or the
     *                                  request was already handled
     * @since 1.11.0
     */
    public SiteResponse handle(String url, SiteRequest request, SiteResponseListener listener) {
        if (null == listener) throw new IllegalArgumentException("listener can't be null");

        var response = prepareHandling(url, request, listener);
        // requests can still be on their way while the runner is stopped,
        // which a listener can be told about like any other failure
        if (isRefusingRequests()) {
            request.closeBody();
            if (request.isCancelled()) {
                response.cancel();
            } else {
                response.fail(new IllegalStateException("the site runner has been stopped"));
            }
            return response;
        }

        try {
            process(url, request, response);
        } catch (Throwable e) {
            response.fail(e);
        }
        return response;
    }

    private boolean isRefusingRequests() {
        return stopped_ && !isServingAfterStop();
    }

    private SiteResponse prepareHandling(String url, SiteRequest request, SiteResponseListener listener) {
        if (null == url) throw new IllegalArgumentException("url can't be null");
        if (null == request) throw new IllegalArgumentException("request can't be null");
        if (!started_) throw new IllegalStateException("the site runner hasn't been started");
        if (null == listener && isRefusingRequests()) throw new IllegalStateException("the site runner has been stopped");
        if (request.getResponse() != null && !isReusingRequests()) throw new IllegalStateException("the request has already been handled");

        request.attach(this);
        var response = request.createResponse();
        response.setListener(listener);
        return response;
    }

    private void process(String url, SiteRequest request, SiteResponse response) {
        var target = Target.parse(url, request.getServerRootUrl(-1), contextPath_);
        request.prepare(response, target.path());

        if (request.isCancelled()) {
            request.closeBody();
            response.cancel();
            return;
        }

        // a request for another origin or outside the context path is left
        // to the application, with its body untouched
        if (null == target.elementUrl()) {
            response.markUnhandled();
            response.finish();
            return;
        }

        try {
            var jar = cookieJar_;
            if (jar != null) {
                jar.apply(request, target.path());
            }

            try {
                request.parse(target.query(), target.sessionId());
            } catch (InvalidFormException | MultipartRequestException e) {
                // the upload directory and its files are the site's own
                // problem, the rest is what the request sent
                if (e instanceof MultipartInvalidUploadDirectoryException ||
                    e instanceof MultipartFileErrorException) {
                    throw e;
                }
                response.setStatus(e instanceof MultipartFileTooBigException ? 413 : 400);
                response.finish();
                return;
            }

            if (request.isCancelled()) {
                response.cancel();
                return;
            }

            var element_url = target.elementUrl();
            var extension = FileUtils.getExtension(element_url);
            var pass_through = extension != null &&
                RifeConfig.engine().getPassThroughSuffixes().contains(extension);
            if (pass_through ||
                !gate_.handleRequest(contextPath_, element_url, request, response)) {
                response.clearBuffer();
                if (!isServingStaticResources() ||
                    !staticResources().serve(request, response, element_url)) {
                    response.markUnhandled();
                }
            }
        } finally {
            request.closeBody();
        }

        if (response.isDetached()) {
            trackDetached(response);
        } else {
            response.finish();
        }
    }

    // the parts of a requested URL without its path parameters, with a null
    // element URL when the request isn't for this site
    private record Target(String path, String query, String sessionId, String elementUrl) {
        static Target parse(String url, String root, String contextPath) {
            // the fragment never reaches a server
            var fragment = url.indexOf('#');
            if (fragment != -1) {
                url = url.substring(0, fragment);
            }

            String query = null;
            var query_index = url.indexOf('?');
            if (query_index != -1) {
                query = url.substring(query_index + 1);
                url = url.substring(0, query_index);
            }

            var foreign = false;
            if (!url.startsWith("/") && url.contains("://")) {
                if (isSameOrigin(url, root)) {
                    url = url.substring(root.length());
                } else {
                    foreign = true;
                }
            }
            if (!url.startsWith("/")) {
                url = "/" + url;
            }

            var session_id = findSessionId(url);
            var stripped = Gate.stripPathParameters(url);

            String element_url = null;
            if (!foreign) {
                if (contextPath.isEmpty()) {
                    element_url = stripped;
                } else if (stripped.equals(contextPath) || stripped.startsWith(contextPath + "/")) {
                    element_url = stripped.substring(contextPath.length());
                }
            }

            return new Target(stripped, query, session_id, element_url);
        }

        // the session id can be in any segment, like /a;x=1/b;jsessionid=id
        private static String findSessionId(String url) {
            var marker = ";" + SESSION_URL_PARAMETER + "=";
            String session_id = null;
            var index = url.indexOf(marker);
            while (index != -1) {
                var start = index + marker.length();
                var end = start;
                while (end < url.length() && url.charAt(end) != ';' && url.charAt(end) != '/') {
                    end++;
                }
                if (end > start) {
                    session_id = url.substring(start, end);
                }
                index = url.indexOf(marker, end);
            }
            return session_id;
        }
    }

    // the scheme and the host aren't case-sensitive, and the root has to be
    // followed by a path or by path parameters
    static boolean isSameOrigin(String url, String root) {
        if (!url.regionMatches(true, 0, root, 0, root.length())) {
            return false;
        }
        if (url.length() == root.length()) {
            return true;
        }
        var next = url.charAt(root.length());
        return next == '/' || next == ';';
    }

    private void trackDetached(SiteResponse response) {
        if (null == response.getListener() && !isKeepingDetachedStreamsOpen()) {
            closeDetached(response);
            return;
        }

        detachedResponses_.add(response);
        // the stream can have ended, or the runner stopped, before it was tracked
        if (response.isFinished()) {
            detachedResponses_.remove(response);
        } else if (isRefusingRequests()) {
            closeDetached(response);
        }
    }

    private static void closeDetached(SiteResponse response) {
        var connection = response.detachedConnection();
        if (connection != null) {
            connection.close();
        }
    }

    void detachedResponseEnded(SiteResponse response) {
        detachedResponses_.remove(response);
    }

    private SiteStaticResources staticResources() {
        var resources = staticResources_;
        if (resources != null) {
            return resources;
        }

        var directory = staticResourceBase_;
        var classpath_base = staticClasspathBase_;
        var bases = new ArrayList<String>();
        if (classpath_base != null) {
            bases.add(classpath_base);
        }
        bases.add(WEBJARS_RESOURCE_BASE);
        var site = getSite();
        var class_loader = null == site ? SiteRunner.class.getClassLoader() : site.getClass().getClassLoader();
        resources = new SiteStaticResources(directory, bases, class_loader);
        staticResources_ = resources;
        return resources;
    }

    SiteSession findSession(String id) {
        var session = sessions_.get(id);
        if (null == session) {
            return null;
        }

        if (!session.isValid()) {
            if (sessions_.remove(id, session)) {
                session.expire();
            }
            return null;
        }

        return session;
    }

    SiteSession createSession() {
        purgeExpiredSessions();

        String id;
        SiteSession session;
        do {
            var bytes = new byte[18];
            RANDOM.nextBytes(bytes);
            id = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session = new SiteSession(this, id, sessionTimeout_);
        } while (sessions_.putIfAbsent(id, session) != null);

        return session;
    }

    private void purgeExpiredSessions() {
        var now = now();
        for (var entry : sessions_.entrySet()) {
            if (entry.getValue().isExpired(now) &&
                sessions_.remove(entry.getKey(), entry.getValue())) {
                entry.getValue().expire();
            }
        }
    }

    int sessionCount() {
        return sessions_.size();
    }

    void removeSession(String id) {
        sessions_.remove(id);
    }

    /**
     * Creates the cookie that identifies a new session. Extending classes can
     * override this to change its attributes.
     *
     * @param request the request that created the session
     * @param id      the identifier of the session
     * @return the session cookie
     * @since 1.11.0
     */
    protected Cookie createSessionCookie(SiteRequest request, String id) {
        var cookie = new Cookie(sessionCookieName_, id);
        var context_path = request.getContextPath();
        cookie.setPath(null == context_path || context_path.isEmpty() ? "/" : context_path);
        cookie.setHttpOnly(true);
        if (request.isSecure()) {
            cookie.setSecure(true);
        }
        return cookie;
    }
}
