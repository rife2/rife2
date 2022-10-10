/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.template.exceptions.TemplateException;

/**
 * Handles the process of setting values in a template based on a Java bean
 * instance.
 *
 * @author Keith Lea (keith[remove] at cs dot oswego dot edu)
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public interface BeanHandler {
    /**
     * Sets all values in the given template whose names match names of
     * properties in the given bean, preceded by the given prefix, if present.
     * If the given prefix is <code>null</code>, it is ignored.
     * <p>For example, given a class:
     * <pre>class Person {
     *    private String first;
     *    private String last;
     *
     *    public String getFirstName() { return first; }
     *    public void setFirstName(String name) { this.first = name; }
     *
     *    public String getLastName() { return last; }
     *    public void setLastName(String name) { this.last = name; }
     * }</pre>
     * <p>And given a template:
     * <pre>Hello &lt;!--V 'NAME:firstName'/--&gt; &lt;!--V 'NAME:lastName'/--&gt;.</pre>
     * <p>Calling this method with an instance of Person where
     * <code>first</code> was "<code>Jim</code>" and <code>last</code> was "<code>James</code>",
     * and the prefix "<code>NAME:</code>", would produce:
     * <pre>Hello Jim James.</pre>
     * <p>Calling this method is equivalent to calling {@link
     * Template#setValue(String, String) setValue} individually for each
     * property of the bean prefixed with the given prefix.
     * <p>If <code>encode</code> is <code>true</code>, this method will use
     * the template's {@linkplain Template#getEncoder encoder} to encode the
     * bean properties before setting the values.
     * <p>Only <em>bean properties</em> will be considered for insertion in
     * the template. This means only properties with a <em>getter and a setter</em>
     * will be considered.
     *
     * @param template the template whose values will be filled
     * @param bean     a bean whose properties will be used to fill in values in
     *                 the template
     * @param prefix   the prefix of values which will be filled with the given
     *                 bean's property values
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @since 1.0
     */
    public void setBean(Template template, Object bean, String prefix, boolean encode)
    throws TemplateException;

    /**
     * Reverts all values to their defaults when the identifiers match
     * properties of the given bean preceded by the given prefix, whether
     * those values were set with a previous call to {@link
     * Template#setBean(Object) }. The values of the bean's properties are
     * ignored.
     * <p>Calling this method is equivalent to calling {@link
     * Template#removeValue } once for the name of each property of the given
     * bean, prefixed with the given prefix.
     *
     * @param bean   a bean whose property names will be used to determine
     *               whether
     * @param prefix a prefix
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @since 1.0
     */
    public void removeBean(Template template, Object bean, String prefix)
    throws TemplateException;

//    /**
//     * Returns a form builder which will be used to {@linkplain
//     * rife.engine.Element#generateForm(Template, Object) generate
//     * forms} in the corresponding template.
//     *
//     * @return a form builder for use with the corresponding template
//     * @since 1.0
//     */
//    public FormBuilder getFormBuilder();
}


