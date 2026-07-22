/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;
import rife.engine.RequestMethod;
import rife.engine.exceptions.CsrfTokenException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Verifies the CSRF token of the requests that change state, so that other
 * sites can't perform them on behalf of your users.
 * <p>This element is added before the routes that should be protected,
 * like any other element:
 * <pre>public class MySite extends Site {
 *     public void setup() {
 *         before(new CsrfProtected());
 *         post("/transfer", c -&gt; { ... });
 *     }
 * }</pre>
 * <p>The state-changing request methods {@code POST}, {@code PUT},
 * {@code PATCH} and {@code DELETE} are verified, the ones that only read
 * are not, they establish the token instead. On a page whose token is
 * established this way, the {@code route:inputs:} filtered tag includes it
 * in the forms it generates, and the {@code context:csrfToken} template
 * value produces the token on its own for the requests that JavaScript
 * performs.
 * <p>A request that doesn't provide a valid token is refused: the
 * {@link #refused} hook produces the response and processing stops before
 * the protected route runs. The default sends {@code 403 Forbidden} with a
 * short message, override the hook to tailor the response, for instance to
 * render a template or redirect to a page that reloads:
 * <pre>before(new CsrfProtected() {
 *     protected void refused(Context c, CsrfTokenException e) {
 *         c.setStatus(Context.SC_FORBIDDEN);
 *         c.print(c.template("reload"));
 *     }
 * });</pre>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#csrfToken()
 * @see Context#verifyCsrfToken()
 * @since 1.10
 */
public class CsrfProtected implements Element {
    /**
     * The state-changing request methods that are verified by default:
     * {@code POST}, {@code PUT}, {@code PATCH} and {@code DELETE}.
     *
     * @since 1.10
     */
    public static final Set<RequestMethod> DEFAULT_PROTECTED_METHODS =
        EnumSet.of(RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE);

    private final Set<RequestMethod> protectedMethods_;

    /**
     * Creates a new instance that verifies the
     * {@link #DEFAULT_PROTECTED_METHODS state-changing request methods}.
     *
     * @since 1.10
     */
    public CsrfProtected() {
        this(DEFAULT_PROTECTED_METHODS);
    }

    /**
     * Creates a new instance that verifies the provided request methods.
     * <p>The methods that aren't provided only establish the token, they
     * are not verified. For instance, to also protect {@code GET}:
     * <pre>new CsrfProtected(EnumSet.of(RequestMethod.GET, RequestMethod.POST))</pre>
     *
     * @param protectedMethods the request methods that are verified
     * @since 1.10
     */
    public CsrfProtected(Set<RequestMethod> protectedMethods) {
        if (null == protectedMethods) throw new IllegalArgumentException("protectedMethods can't be null");

        protectedMethods_ = EnumSet.copyOf(protectedMethods);
    }

    public void process(Context c) {
        if (protectedMethods_.contains(c.method())) {
            try {
                c.verifyCsrfToken();
            } catch (CsrfTokenException e) {
                refused(c, e);
                // stop the chain so the protected route doesn't run
                c.respond();
            }
            return;
        }

        c.ensureCsrfToken();
    }

    /**
     * Hook that produces the response for a request whose token couldn't be
     * verified, called before the protected route runs.
     * <p>The default sends {@code 403 Forbidden} with a short message.
     * Override it to tailor the response, the element stops the processing
     * afterwards so the protected route is never reached. A hook that
     * interrupts the processing itself, by calling
     * {@link Context#redirect redirect} or {@link Context#respond respond},
     * is equally fine.
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
