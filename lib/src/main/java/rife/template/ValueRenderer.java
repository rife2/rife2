/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

/**
 * An object which can render content for a value in a template. To use a
 * renderer in a template, the template should contain a value with an ID like
 * <code>"render:org.rifers.something.MyRenderer"</code>, where
 * <code>MyRenderer</code> is your <code>ValueRenderer</code> class.
 * <p>Value renderer implementations must provide a public zero-argumnet
 * no-arg constructor.
 *
 * @author Keith Lea (keith[remove] at cs dot oswego dot edu)
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public interface ValueRenderer {
    /**
     * Renders the specified value in the given template. The value ID will be
     * of the form "<code>render:<em>className</em></code>" or "<code>render:<em>className</em>:<em>differentiator</em></code>",
     * where "<code>className</code>" is the fully qualified name of this
     * class.
     *
     * @param template       the template into which the returned string will be
     *                       inserted
     * @param valueId        the ID of the value in the given template whose value
     *                       will be set to the returned string
     * @param differentiator the differentiator string passed as part of the
     *                       value ID, or <code>null</code> if none was provided
     * @return the rendered text
     * @since 1.0
     */
    public String render(Template template, String valueId, String differentiator);
}


