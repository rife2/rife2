/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.authentication.elements.AuthConfig;
import rife.engine.Context;
import rife.engine.Element;
import rife.engine.RequestMethod;
import rife.engine.exceptions.CsrfTokenException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Verifies the CSRF token of the requests that change state, so that other
 * sites can't perform them on behalf of your users.
 * <p>You add this element before the routes that should be protected,
 * just like any other element:
 * <pre>public class MySite extends Site {
 *     public void setup() {
 *         before(new CsrfProtected());
 *         post("/transfer", c -&gt; { ... });
 *     }
 * }</pre>
 * <p>The state-changing request methods {@code POST}, {@code PUT},
 * {@code PATCH} and {@code DELETE} will be verified, while the ones that
 * only read aren't, since they establish the token instead. On a page
 * whose token was established this way, the {@code route:inputs:} filtered
 * tag will include the token in the forms that it generates, while the
 * {@code context:csrfToken} template value provides the token on its own
 * for the requests that JavaScript performs.
 * <p>A request that doesn't provide a valid token will be refused, which
 * means that the {@link #refused} hook produces the response and that the
 * processing stops before the protected route runs. By default this sends
 * {@code 403 Forbidden} with a short message, but you can override the
 * hook to tailor the response, for instance to render a template or to
 * redirect to a page that reloads:
 * <pre>before(new CsrfProtected() {
 *     protected void refused(Context c, CsrfTokenException e) {
 *         c.setStatus(Context.SC_FORBIDDEN);
 *         c.print(c.template("reload"));
 *     }
 * });</pre>
 * <p>When your users log in with RIFE2's authentication, you can provide
 * its {@link AuthConfig}. If {@code RifeConfig.engine()} also has a
 * {@linkplain rife.config.RifeConfig.EngineConfig#setSecret(String) secret},
 * the token of a user that's logged in will be signed for that user. This
 * protects you against tokens that someone else managed to plant in the
 * browser, and the browser will get a new token when the user logs in or
 * out:
 * <pre>before(new CsrfProtected(config));</pre>
 * <p>Since the new token is given out when a page is loaded, a page that
 * was loaded before the user logged in can't submit its forms anymore, so
 * you should load a page after logging in, for instance by redirecting.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#csrfToken()
 * @see Context#verifyCsrfToken()
 * @see Context#ensureCsrfToken()
 * @since 1.10
 */
public class CsrfProtected implements Element {
    /**
     * The state-changing request methods that are verified by default,
     * which are {@code POST}, {@code PUT}, {@code PATCH} and
     * {@code DELETE}.
     *
     * @see #CsrfProtected()
     * @since 1.10
     */
    public static final Set<RequestMethod> DEFAULT_PROTECTED_METHODS =
        EnumSet.of(RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE);

    private final Set<RequestMethod> protectedMethods_;
    private final AuthConfig authConfig_;

    /**
     * Creates a new instance that verifies the
     * {@link #DEFAULT_PROTECTED_METHODS state-changing request methods}.
     *
     * @see #CsrfProtected(Set)
     * @since 1.10
     */
    public CsrfProtected() {
        this(DEFAULT_PROTECTED_METHODS);
    }

    /**
     * Creates a new instance that verifies the provided request methods.
     * <p>The methods that aren't provided will not be verified, since they
     * only establish the token. If you want to protect {@code GET} as
     * well, for instance:
     * <pre>new CsrfProtected(EnumSet.of(RequestMethod.GET, RequestMethod.POST))</pre>
     *
     * @param protectedMethods the request methods that are verified
     * @see #CsrfProtected()
     * @see #DEFAULT_PROTECTED_METHODS
     * @since 1.10
     */
    public CsrfProtected(Set<RequestMethod> protectedMethods) {
        this(null, protectedMethods);
    }

    /**
     * Creates a new instance that verifies the
     * {@link #DEFAULT_PROTECTED_METHODS state-changing request methods} and
     * binds the token to the user that's logged in with the provided
     * authentication configuration.
     *
     * @param authConfig the authentication configuration whose cookie
     *                   identifies the user that's logged in
     * @see #CsrfProtected(AuthConfig, Set)
     * @since 1.11.0
     */
    public CsrfProtected(AuthConfig authConfig) {
        this(authConfig, DEFAULT_PROTECTED_METHODS);
    }

    /**
     * Creates a new instance that verifies the provided request methods and
     * binds the token to the user that's logged in with the provided
     * authentication configuration.
     *
     * @param authConfig       the authentication configuration whose cookie
     *                         identifies the user that's logged in; or
     *                         {@code null} to not bind the token
     * @param protectedMethods the request methods that are verified
     * @see #CsrfProtected(AuthConfig)
     * @since 1.11.0
     */
    public CsrfProtected(AuthConfig authConfig, Set<RequestMethod> protectedMethods) {
        if (null == protectedMethods) throw new IllegalArgumentException("protectedMethods can't be null");

        authConfig_ = authConfig;
        protectedMethods_ = EnumSet.copyOf(protectedMethods);
    }

    public void process(Context c) {
        var identity = authConfig_ == null ? null : c.cookieValue(authConfig_.authCookieName());
        if (protectedMethods_.contains(c.method())) {
            try {
                c.verifyCsrfToken(identity);
            } catch (CsrfTokenException e) {
                refused(c, e);
                // stop the chain so the protected route doesn't run
                c.respond();
            }
            return;
        }

        c.ensureCsrfToken(identity);
    }

    /**
     * Provides the response for a request whose token couldn't be verified,
     * called before the protected route runs.
     * <p>This sends {@code 403 Forbidden} with a short message by default.
     * You can override this method to tailor the response and the element
     * will stop the processing afterwards, so that the protected route is
     * never reached. A hook that interrupts the processing itself, by
     * calling {@link Context#redirect redirect} or
     * {@link Context#respond respond}, is equally fine.
     *
     * @param c the context of the refused request
     * @param e the exception that describes why the token was refused, either
     *          {@link rife.engine.exceptions.CsrfTokenMissingException} or
     *          {@link rife.engine.exceptions.CsrfTokenInvalidException}
     * @since 1.10
     */
    protected void refused(Context c, CsrfTokenException e) {
        c.setStatus(Context.SC_FORBIDDEN);
        c.print("The CSRF token verification failed.");
    }
}
