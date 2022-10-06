/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import rife.template.exceptions.TemplateException;

/**
 * A template is used to construct, manipulate and produce text content.
 * <p>Templates can be used for a variety of text types, including XHTML,
 * plain text, XML, SQL and even Java source. Each template type has similar
 * features, the biggest difference is the syntax of the invisible tag which
 * takes the form of a comment in the corresponding language (<code>&lt;!--
 * --&gt; </code>for XHTML and XML, <code>/* -* /</code> for Java and SQL,
 * ...).
 *
 * @author Keith Lea (keith[remove] at cs dot oswego dot edu)
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public interface Template extends Cloneable {
    /**
     * Appends the content of a block to a value. The values used by the block
     * will be captured when this method is called, so any future changes to
     * template values will not affect text which was appended when this
     * method is called.
     *
     * @param valueBlockId the ID of the value and the block
     * @throws TemplateException when the <code>valueId</code> or
     *                           <code>blockId</code> aren't known
     * @see #setBlock
     * @see #getBlock
     * @see #getContent
     * @see #hasBlock
     * @since 1.0
     */
    public void appendBlock(String valueBlockId)
    throws TemplateException;

    /**
     * Appends the content of a block to a value. The values used by the block
     * will be captured when this method is called, so any future changes to
     * template values will not affect text which was appended when this
     * method is called.
     *
     * @param valueId the ID of the value
     * @param blockId the ID of the block
     * @throws TemplateException when the <code>valueId</code> or
     *                           <code>blockId</code> aren't known
     * @see #setBlock
     * @see #getBlock
     * @see #getContent
     * @see #hasBlock
     * @since 1.0
     */
    public void appendBlock(String valueId, String blockId)
    throws TemplateException;

    /**
     * Replaces the specified value with the content of the specified block.
     * The values used by the block will be captured when this method is
     * called, so any future changes to template values will not affect the
     * specified value text.
     *
     * @param valueBlockId the ID of the value and the block
     * @throws TemplateException when the <code>valueId</code> or
     *                           <code>blockId</code> aren't known
     * @see #appendBlock
     * @see #getBlock
     * @see #getContent
     * @see #hasBlock
     * @since 2.0
     */
    public void setBlock(String valueBlockId)
    throws TemplateException;

    /**
     * Replaces the specified value with the content of the specified block.
     * The values used by the block will be captured when this method is
     * called, so any future changes to template values will not affect the
     * specified value text.
     *
     * @param valueId the ID of the value
     * @param blockId the ID of the block
     * @throws TemplateException when the <code>valueId</code> or
     *                           <code>blockId</code> aren't known
     * @see #appendBlock
     * @see #getBlock
     * @see #getContent
     * @see #hasBlock
     * @since 1.0
     */
    public void setBlock(String valueId, String blockId)
    throws TemplateException;

    /**
     * Returns the evaluated content of the specified block as a text.
     *
     * @param id the ID of the block in the template
     * @return the evaluated textual content of the specified block
     * @throws TemplateException when the block ID isn't known
     * @see #appendBlock
     * @see #setBlock
     * @see #getContent
     * @see #hasBlock
     * @since 1.0
     */
    public String getBlock(String id)
    throws TemplateException;

    /**
     * Returns the entire content of the template and finalize all non
     * evaluated values. The content is the root block with has an empty
     * string as identifier.
     * <p>Values without content will either use their default value if it has
     * been provided, or the tag that was used to declare the value will be
     * output as-is.
     * <p>All specialized tags will also be evaluated (resource bundle
     * localization, block localization, value renderers, expressions, ...).
     *
     * @return the entire textual content of the template
     * @throws TemplateException when an error occurred during the
     *                           processing of the specialized tags
     * @see #appendBlock
     * @see #setBlock
     * @see #getBlock
     * @see #hasBlock
     * @since 1.0
     */
    public String getContent()
    throws TemplateException;

    /**
     * Writes the {@linkplain #getBlock(String) evaluated contents} of the
     * specified block to the given output stream, using <code>UTF-8</code>
     * encoding.
     *
     * @param id  the ID of the block
     * @param out the stream to write to
     * @throws IOException       when errors occur during the manipulation of the
     *                           output stream
     * @throws TemplateException when the block ID isn't known
     * @see #writeContent(OutputStream)
     * @see #writeContent(OutputStream, String)
     * @see #write
     * @since 1.0
     */
    public void writeBlock(String id, OutputStream out)
    throws IOException, TemplateException;

    /**
     * Writes the {@linkplain #getContent() complete evaluated template
     * content} to the given stream, using UTF-8 encoding.
     *
     * @param out the stream to which the template contents should be written
     * @throws IOException       when errors occur during the manipulation of the
     *                           output stream
     * @throws TemplateException when an error occurs during the template
     *                           content evaluation
     * @see #writeBlock
     * @see #writeContent(OutputStream, String)
     * @see #write
     * @since 1.0
     */
    public void writeContent(OutputStream out)
    throws IOException, TemplateException;

    /**
     * Writes the {@linkplain #getContent() complete evaluated template
     * content} to the given stream, using the specified charset for encoding.
     *
     * @param out         the stream to which the template contents should be written
     * @param charsetName the name of the charset to use
     * @throws IOException       when errors occur during the manipulation of the
     *                           output stream; or
     *                           <p>when the character set isn't valid
     * @throws TemplateException when an error occurs during the template
     *                           content evaluation
     * @see #writeBlock
     * @see #writeContent(OutputStream)
     * @see #write
     * @since 1.0
     */

    public void writeContent(OutputStream out, String charsetName)
    throws IOException, TemplateException;

    /**
     * This method is a shorthand for {@link #writeContent(OutputStream)}.
     *
     * @param out the stream to which the template contents should be written
     * @throws IOException       when errors occur during the manipulation of the
     *                           output stream; or
     *                           <p>when the character set isn't valid
     * @throws TemplateException when an error occurs during the template
     *                           content evaluation
     * @see #writeBlock
     * @see #writeContent(OutputStream)
     * @see #writeContent(OutputStream, String)
     * @since 1.0
     */
    public void write(OutputStream out)
    throws IOException, TemplateException;

    /**
     * Returns the content of the specified block as a list with all the
     * individual parts.
     * <p>This list is the internal representation of all content with
     * placeholders for the values that aren't filled in yet. This structure
     * is mainly used internally by the framework. The list structure also
     * makes it possible to optimize performance and memory usage.
     *
     * @param id the ID of a block in this template
     * @return a list of the contents of the specified block
     * @throws TemplateException if no such block exists; or
     *                           <p>if an error occurred during the retrieval
     * @see #getDeferredContent
     * @since 1.0
     */
    public List<CharSequence> getDeferredBlock(String id)
    throws TemplateException;

    /**
     * Returns the content of this template as a list with all the individual
     * parts.
     * <p>This list is the internal representation of all content with
     * placeholders for the values that aren't filled in yet. This structure
     * is mainly used internally by the framework. The list structure also
     * makes it possible to optimize performance and memory usage.
     *
     * @return a list of the contents of this template
     * @throws TemplateException if an error occurred during the retrieval
     * @see #getDeferredBlock
     * @since 1.0
     */
    public List<CharSequence> getDeferredContent()
    throws TemplateException;

    /**
     * Returns an anonymous value that can be used to construct complex
     * content for use within this template. See {@link InternalValue} for
     * details.
     * <p>The returned internal value is tied closely to the template it was
     * obtained from, methods like {@link InternalValue#appendBlock(String)
     * InternalValue.appendBlock} reference blocks within this template.
     *
     * @return a new internal value instance for constructing more complex
     * parts of this template
     * @see InternalValue
     * @since 1.0
     */
    public InternalValue createInternalValue();

    /**
     * Sets the specified value in this template to content that's structured
     * in the internal format.
     *
     * @param id              the ID of the value in this template
     * @param deferredContent content in the internal format
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, List<CharSequence> deferredContent)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the value of the given
     * {@linkplain #createInternalValue() internal value}.
     *
     * @param id            the ID of the value in this template
     * @param internalValue an internal value, <code>null</code> set the value
     *                      content to blank content
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, InternalValue internalValue)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the result of calling
     * {@link String#valueOf(Object) String.valueOf} on the given
     * <code>value</code>.
     *
     * @param id    the ID of the value in this template
     * @param value an object
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, Object value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to <code>true</code> or
     * <code>false</code> depending on the given <code>value</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a boolean value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, boolean value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the single specified
     * character.
     *
     * @param id    the ID of the value in this template
     * @param value a character
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, char value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given characters. The
     * given value must not be <code>null</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a string of characters
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, char[] value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the specified range of the
     * given character string. The specified number of bytes from
     * <code>value</code> will be used, starting at the character specified by
     * <code>offset</code>.
     *
     * @param id     the ID of the value in this template
     * @param value  a character string
     * @param offset the index in <code>value</code> of the first character to
     *               use
     * @param count  the number of characters to use
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, char[] value, int offset, int count)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given double precision
     * floating point value. This method uses the {@linkplain
     * String#valueOf(double) String.valueOf} method to print the given value,
     * which probably prints more digits than you like. You probably want
     * {@link String#format String.format} or {@link NumberFormat} instead.
     *
     * @param id    the ID of the value in this template
     * @param value a floating point value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, double value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given floating point
     * value. This method uses the {@linkplain String#valueOf(float)
     * String.valueOf} method to print the given value, which probably prints
     * more digits than you like. You probably want {@link String#format
     * String.format} or {@link NumberFormat} instead.
     *
     * @param id    the ID of the value in this template
     * @param value a floating point value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, float value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given integer.
     *
     * @param id    the ID of the value in this template
     * @param value an integer
     * @throws TemplateException if the specified value does not exist in
     *                           this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, int value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given long.
     *
     * @param id    the ID of the value in this template
     * @param value a long
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, long value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given string, or an
     * empty string if <code>value</code> is <code>null</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a string, or <code>null</code>
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, String value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the given character sequence,
     * or an empty character sequence if <code>value</code> is <code>null</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a character sequence, or <code>null</code>
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.5
     */
    public void setValue(String id, CharSequence value)
    throws TemplateException;

    /**
     * Sets the specified value in this template to the current {@linkplain
     * #getContent() content} of the given template. The given template's
     * value will be evaluated immediately, instead of being stored to be
     * evaluated later.
     * <p>If the given template is <code>null</code>, the specified value will
     * be set to an empty string.
     *
     * @param id       the ID of the value in this template
     * @param template a template
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template; or
     *                           <p>if an error occurred during the evaluation of the template parameter
     * @see #appendValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void setValue(String id, Template template)
    throws TemplateException;

    /**
     * Sets all values in this template whose identifiers match names of
     * properties in the given bean.
     * <p>For example, given a class:
     * <pre>
     * class Person {
     *   private String first;
     *   private String last;
     *
     *   public String getFirstName() { return first; }
     *   public void setFirstName(String name) { this.first = name; }
     *
     *   public String getLastName() { return last; }
     *   public void setLastName(String name) { this.last = name; }
     * </pre>
     * <p>And given a template:
     * <pre>
     * Hello &lt;!--v firstName/--&gt; &lt;!--v lastName/--&gt;.
     * </pre>
     * <p>Calling this method with an instance of Person where
     * <code>first</code> was "Jim" and <code>last</code> was "James", would
     * produce:
     * <pre>Hello Jim James.</pre>
     * <p>Calling this method is equivalent to calling {@link
     * #setValue(String, String) setValue} individually for each property of
     * the bean.
     * <p>This method uses this template's {@linkplain #getEncoder encoder} to
     * encode the bean properties before setting the values. To prevent this,
     * use {@linkplain #setBean(Object, String, boolean) the other form of
     * setBean}.
     * <p>Only <em>bean properties</em> will be considered for insertion in
     * the template. This means only properties with a <em>getter and a setter</em>
     * will be considered.
     *
     * @param bean a bean whose properties will be used to fill in values in
     *             the template
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @see #removeBean
     * @since 1.0
     */
    public void setBean(Object bean)
    throws TemplateException;

    /**
     * Sets all values in this template whose names match names of properties
     * in the given bean, preceded by the given prefix.
     * <p>For example, given a class:
     * <pre>
     * class Person {
     *   private String first;
     *   private String last;
     *
     *   public String getFirstName() { return first; }
     *   public void setFirstName(String name) { this.first = name; }
     *
     *   public String getLastName() { return last; }
     *   public void setLastName(String name) { this.last = name; }
     * </pre>
     * <p>And given a template:
     * <pre>
     * Hello &lt;!--v NAME:firstName/--&gt; &lt;!--v NAME:lastName/--&gt;.
     * </pre>
     * <p>Calling this method with an instance of Person where
     * <code>first</code> was "Jim" and <code>last</code> was "James", and the
     * prefix "NAME:", would produce:
     * <pre>Hello Jim James.</pre>
     * <p>Calling this method is equivalent to calling {@link
     * #setValue(String, String) setValue} individually for each property of
     * the bean prefixed with the given prefix.
     * <p>This method uses this template's {@linkplain #getEncoder encoder} to
     * encode the bean properties before setting the values. To prevent this,
     * use {@linkplain #setBean(Object, String, boolean) the other form of
     * setBean}.
     * <p>Only <em>bean properties</em> will be considered for insertion in
     * the template. This means only properties with a <em>getter and a setter</em>
     * will be considered.
     *
     * @param bean   a bean whose properties will be used to fill in values in
     *               the template
     * @param prefix the prefix of values which will be filled with the given
     *               bean's property values
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @see #removeBean
     * @since 1.0
     */
    public void setBean(Object bean, String prefix)
    throws TemplateException;

    /**
     * Sets all values in this template whose names match names of properties
     * in the given bean, preceded by the given prefix, if present. If the
     * given prefix is <code>null</code>, it is ignored.
     * <p>For example, given a class:
     * <pre>
     * class Person {
     *   private String first;
     *   private String last;
     *
     *   public String getFirstName() { return first; }
     *   public void setFirstName(String name) { this.first = name; }
     *
     *   public String getLastName() { return last; }
     *   public void setLastName(String name) { this.last = name; }
     * </pre>
     * <p>And given a template:
     * <pre>
     * Hello &lt;!--v NAME:firstName/--&gt; &lt;!--v NAME:lastName/--&gt;.
     * </pre>
     * <p>Calling this method with an instance of Person where
     * <code>first</code> was "Jim" and <code>last</code> was "James", and the
     * prefix "NAME:", would produce:
     * <pre>Hello Jim James.</pre>
     * <p>Calling this method is equivalent to calling {@link
     * #setValue(String, String) setValue} individually for each property of
     * the bean prefixed with the given prefix.
     * <p>If <code>encode</code> is <code>true</code>, this method will use
     * this template's {@linkplain #getEncoder encoder} to encode the bean
     * properties before setting the values.
     * <p>Only <em>bean properties</em> will be considered for insertion in
     * the template. This means only properties with a <em>getter and a setter</em>
     * will be considered.
     *
     * @param bean   a bean whose properties will be used to fill in values in
     *               the template
     * @param prefix the prefix of values which will be filled with the given
     *               bean's property values
     * @param encode <code>true</code> if the bean property values have to be
     *               encoded; or
     *               <p><code>false</code> otherwise
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @see #removeBean
     * @since 1.0
     */
    public void setBean(Object bean, String prefix, boolean encode)
    throws TemplateException;

    /**
     * Reverts all values to their defaults when the identifiers match
     * properties of the given bean, whether those values were set with
     * a previous call to {@link #setBean(Object) setBean}. The values of the
     * bean's properties are ignored.
     * <p>Calling this method is equivalent to calling {@link #removeValue
     * removeValue} once for the name of each property of the given bean.
     *
     * @param bean a bean
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @see #setBean
     * @since 1.0
     */
    public void removeBean(Object bean)
    throws TemplateException;

    /**
     * Reverts all values to their defaults when the identifiers match
     * properties of the given bean preceded by the given prefix, whether or
     * not those values were set with a previous call to {@link
     * #setBean(Object) setBean}. The values of the bean's properties are
     * ignored.
     * <p>Calling this method is equivalent to calling {@link #removeValue
     * removeValue} once for the name of each property of the given bean,
     * prefixed with the given prefix.
     *
     * @param bean   a bean whose properties will be used to determine which
     *               values to remove from the template
     * @param prefix a prefix
     * @throws TemplateException if this template has no bean handling
     *                           capability; or
     *                           <p>an error occurred during the introspection of the bean
     * @see #setBean
     * @since 1.0
     */
    public void removeBean(Object bean, String prefix)
    throws TemplateException;

    /**
     * Appends the result of calling {@link String#valueOf(Object)
     * String.valueOf} on the given <code>value</code> to the specified value
     * in this template.
     *
     * @param id    the ID of the value in this template
     * @param value an object
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, Object value)
    throws TemplateException;

    /**
     * Appends <code>"true"</code> or <code>"false"</code> to the specified
     * value in this template, depending on the given <code>value</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a boolean value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, boolean value)
    throws TemplateException;

    /**
     * Appends the single specified character to the specified value in this
     * template.
     *
     * @param id    the ID of the value in this template
     * @param value a character
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, char value)
    throws TemplateException;

    /**
     * Appends the given characters to the specified value in this template.
     * The given value must not be <code>null</code>.
     *
     * @param id    the ID of the value in this template
     * @param value a string of characters
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, char[] value)
    throws TemplateException;

    /**
     * Appends the specified range of the given character string to the
     * specified value in this template. The specified number of bytes from
     * <code>value</code> will be used, starting at the character specified by
     * <code>offset</code>.
     *
     * @param id     the ID of the value in this template
     * @param value  a character string
     * @param offset the index in <code>value</code> of the first character to
     *               use
     * @param count  the number of characters to use
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, char[] value, int offset, int count)
    throws TemplateException;

    /**
     * Appends the given double precision floating point value to the
     * specified value in this template. This method uses the {@linkplain
     * String#valueOf(double) String.valueOf} method to print the given value,
     * which probably prints more digits than you like. You probably want
     * {@link String#format String.format} or {@link NumberFormat} instead.
     *
     * @param id    the ID of the value in this template
     * @param value a floating point value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, double value)
    throws TemplateException;

    /**
     * Appends the given floating point value to the specified value in this
     * template. This method uses the {@linkplain String#valueOf(float)
     * String.valueOf} method to print the given value, which probably prints
     * more digits than you like. You probably want {@link String#format
     * String.format} or {@link NumberFormat} instead.
     *
     * @param id    the ID of the value in this template
     * @param value a floating point value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, float value)
    throws TemplateException;

    /**
     * Appends the given integer to the specified value in this template.
     *
     * @param id    the ID of the value in this template
     * @param value an integer
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, int value)
    throws TemplateException;

    /**
     * Appends the given long to the specified value in this template.
     *
     * @param id    the ID of the value in this template
     * @param value a long
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, long value)
    throws TemplateException;

    /**
     * Appends the given string, or an empty string if <code>value</code> is
     * <code>null</code>, to the specified value in this template.
     *
     * @param id    the ID of the value in this template
     * @param value a string, or <code>null</code>
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public void appendValue(String id, String value)
    throws TemplateException;

    /**
     * Returns the current content of the specified value as a string.
     *
     * @param id the ID of a value in this template
     * @return the current content value of the specified value
     * @throws TemplateException if the specified value ID does not exist
     *                           in this template
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public String getValue(String id)
    throws TemplateException;

    /**
     * Returns the original text of the specified value, before any
     * modification that may have been made using {@link #setValue} or similar
     * methods.
     * <p>If no default value was specified for the given value, this method
     * will return <code>null</code>.
     *
     * @param id the ID of a value in this template, or <code>null</code>
     * @return the original text value of the specified value
     * @see #hasDefaultValue
     * @since 1.0
     */
    public String getDefaultValue(String id);

    /**
     * Returns whether a {@linkplain #getDefaultValue default value} was
     * specified in this template for the specified value.
     *
     * @param id the ID of a value in this template
     * @return whether the specified value has a default value
     * @see #getDefaultValue
     * @since 1.0
     */
    public boolean hasDefaultValue(String id);

    /**
     * Each template type supports a set of special block tags that are used
     * for adding automated features like localization, block value scripting,
     * config value setting, ... Instead of having to parse all template block
     * identifiers each time these features are used, RIFE filters them out at
     * template compilation and keeps them available in a separate collection.
     * <p>This method is mainly used by the framework itself, the supported
     * filter regular expressions are available from {@link TemplateFactory}
     * and {@link TemplateFactoryEngineTypes}.
     *
     * @param filter a template factory regular expression
     * @return a list of captured groups for matching block ID's
     * @see #hasFilteredBlocks
     * @since 1.0
     */
    public List<String[]> getFilteredBlocks(String filter);

    /**
     * Returns whether any block matched a particular filter at template
     * compilation.
     *
     * @param filter a template factory regular expression
     * @return whether any matching blocks exist in this template
     * @see #getFilteredBlocks
     * @since 1.0
     */
    public boolean hasFilteredBlocks(String filter);

    /**
     * Each template type supports a set of special value tags that are used
     * for adding automated features like embedded elements, localization,
     * block value scripting, config value setting, ... Instead of having to
     * parse all template value identifiers each time these features are used,
     * RIFE filters them out at template compilation and keeps them available
     * in a separate collection.
     * <p>This method is mainly used by the framework itself, the supported
     * filter regular expressions are available from {@link TemplateFactory}
     * and {@link TemplateFactoryEngineTypes}.
     *
     * @param filter a template factory regular expression
     * @return a list of captured groups for matching value ID's
     * @see #hasFilteredValues
     * @since 1.0
     */
    public List<String[]> getFilteredValues(String filter);

    /**
     * Returns whether any value matched a particular filter at template
     * compilation.
     *
     * @param filter a template factory regular expression
     * @return whether any matching values exist in this template
     * @see #getFilteredValues
     * @since 1.0
     */
    public boolean hasFilteredValues(String filter);

    /**
     * Fills all values in this template which match "<code>L10N:<em>key</em></code>",
     * where "<code>key</code>" is a {@linkplain
     * ResourceBundle#getObject(String) key} in a {@linkplain
     * #addResourceBundle(ResourceBundle) resource bundle} registered for this
     * template. Each value will be filled with the value in the resource
     * bundle with the corresponding key.
     * <p>This method is normally called automatically during template
     * initialization. You should call it if you wish to re-evaluate the tags
     * at any time during the template's life.
     *
     * @return the list of names of the template values that were generated
     * @since 1.0
     */
    public List<String> evaluateL10nTags();

    /**
     * Fills all values in this template which match "<code>CONFIG:<em>key</em></code>",
     * where "<code>key</code>" is a configuration value name in the {@link
     * Config} instance returned by {@link Config#getRepInstance()}. Each
     * valuev will be filled with the value of the configuration option with
     * the corresponding key.
     * <p>This method is normally called automatically during template
     * initialization. You should call it if you wish to re-evaluate the tags
     * at any time during the template's life.
     *
     * @return the list of names of the template values that were generated
     * @since 1.0
     */
    public List<String> evaluateConfigTags();

    /**
     * Fills the value "<code>LANG:<em>id</em></code>" with the value of the
     * block "<code>LANG:<em>id</em>:<em>langid</em></code>", where "<code>id</code>"
     * is the given ID, and "<code>langid</code>" is this template's
     * {@linkplain #getLanguage() current language ID}.
     * <p>If no matching block for the current language is found, the content
     * of the specified value will not be modified.
     * <p>This method is called automatically when the output is generated
     * (such as when calling {@link #getContent()}). You can manually call
     * this method to force evaluation of the tags earlier than that.
     *
     * @param id the ID whose language tag should be filled with the
     *           appropriate block
     * @return the list of names of the template values that were generated
     * @since 1.0
     */
    public List<String> evaluateLangTags(String id);
//
//    /**
//     * Evaluates the specified OGNL, Groovy, or Janino expression tag. For
//     * example, if a value exists with ID "<code>OGNL:<em>id</em>:[[<em>script</em>]]</code>",
//     * where "<code>id</code>" is the given ID and "<code>script</code>" is
//     * some OGNL expression, this method will replace this value with the
//     * value of the evaluated OGNL expression, using the current set of
//     * {@linkplain #getExpressionVars() expression variables}.
//     * <p>The prefix for OGNL is "OGNL:", the prefix for Groovy is "GROOVY:"
//     * and the prefix for Janino is "JANINO:".
//     * <p>This method is called automatically when the output is generated
//     * (such as when calling {@link #getContent()}). You can manually call
//     * this method to force evaluation of the tags earlier than that.
//     *
//     * @param id the ID whose expression tag will be replaced with the value
//     *           of the evaluated expression in the tag ID
//     * @return the list of names of the template values that were generated
//     * @since 1.0
//     */
//    public List<String> evaluateExpressionTags(String id);
//
//    /**
//     * Evaluates the specified OGNL, Groovy, or Janino configuration
//     * expression tag. For example, if a value exists with ID "OGNL:CONFIG:<em>id</em>:[[<em>script</em>]]",
//     * where "id" is the given ID and "script" is some OGNL expression, this
//     * method will replace this value with the value of the evaluated OGNL
//     * expression, using the current set of {@linkplain #getExpressionVars()
//     * expression variables}.
//     * <p>The prefix for OGNL is "<code>OGNL:</code>", the prefix for Groovy
//     * is "<code>GROOVY:</code>" and the prefix for Janino is "<code>JANINO:</code>".
//     * <p>The context for the expressions will be the {@link Config} object
//     * returned by {@link Config#getRepInstance()}.
//     * <p>Expression config tags are evaluated automatically when the output
//     * is generated (such as when calling {@link #getContent()}). You can
//     * manually call this method to force evaluation of the tags earlier than
//     * that.
//     *
//     * @param id the ID whose expression tag will be replaced with the value
//     *           of the evaluated expression in the tag ID
//     * @return the list of names of the template values that were generated
//     * @since 1.0
//     */
//    public List<String> evaluateExpressionConfigTags(String id);

    /**
     * Evalutes all values in this template with ID's of the form "<code>render:<em>class</em></code>"
     * or "<code>render:<em>class</em>:<em>differentiator</em></code><em>r</em>",
     * where "<code>class</code>" is the fully-qualified name of a class which
     * extends {@link ValueRenderer}, the result of calling {@link
     * ValueRenderer#render(Template, String, String) ValueRenderer.render} on
     * a new instance of the class. The class must contain a zero-argument
     * ("no-arg") constructor.
     * <p>For example, given a class <code>MyRenderer</code> in the package "<code>org.rifers.something</code>",
     * which extends {@link ValueRenderer}, a value "<code>render:org.rifers.something.MyRenderer:test</code>"
     * would create a new instance of <code>MyRenderer</code> (using its
     * no-arg constructor), call <code>render(this,
     * "render:org.rifers.something.MyRenderer:test", "test")</code>, and set
     * the value in this template to whatever value the call returns.
     * <p>Value renderer tags are evaluated automatically when the output is
     * generated (such as when calling {@link #getContent()}). You can
     * manually call this method to force evaluation of the tags earlier than
     * that.
     *
     * @return the list of names of the template values that were generated
     * @throws TemplateException if a class in a render tag cannot be
     *                           instantiated
     * @since 1.0
     */
    public List<String> evaluateRenderTags()
    throws TemplateException;

    /**
     * Returns whether this template contains a block with the given ID.
     *
     * @param id the prospective ID of a block
     * @return whether this template contains a block with the given ID
     * @see #appendBlock
     * @see #setBlock
     * @see #getBlock
     * @see #getContent
     * @since 1.0
     */
    public boolean hasBlock(String id);

    /**
     * Returns whether the specified value has been set. If this method
     * returns <code>false</code>, the value has its original default value.
     * <p>If no such value exists in this template, this method will not throw
     * an exception, it will return <code>false</code>.
     *
     * @param id the ID of a value in this template
     * @return whether the specified value has been set
     * @see #appendValue
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public boolean isValueSet(String id);

    /**
     * Returns the number of values in this template which {@linkplain
     * #isValueSet(String) have been set}.
     *
     * @return the number of values in this template which have been set
     * @since 1.0
     */
    public int countValues();

    /**
     * Reverts the specified value back to its default value.
     *
     * @param id the ID of a value in this template
     * @see #appendValue
     * @see #setValue
     * @see #isValueSet
     * @see #hasValueId
     * @since 1.0
     */
    public void removeValue(String id);

    /**
     * Reverts the specified values back to their default value.
     *
     * @param ids the IDs of values in this template
     * @see #appendValue
     * @see #setValue
     * @see #isValueSet
     * * @see #removeValue
     * @see #hasValueId
     * @since 1.6
     */
    public void removeValues(List<String> ids);

    /**
     * Set the content of the specified value to an empte string.
     *
     * @param id the ID of a value in this template
     * @see #appendValue
     * @see #setValue
     * @see #isValueSet
     * @see #hasValueId
     * @see #removeValue
     * @see #removeValues
     * @since 1.4
     */
    public void blankValue(String id);

    /**
     * Resets all values in this template and removes any resource bundles.
     * Configuration and localization tags are re-evaluated.
     *
     * @since 1.0
     */
    public void clear();

    /**
     * Returns a list of the ID's of all values present in this template,
     * including set and unset values.
     *
     * @return a list of ID's of all set and unset value
     * @since 1.0
     */
    public String[] getAvailableValueIds();

    /**
     * Returns a list of the ID's of all values in this template which
     * {@linkplain #isValueSet(String) have not been set}.
     *
     * @return a list of ID's of values in this template which have not been
     * set
     * @since 1.0
     */
    public Collection<String> getUnsetValueIds();

    /**
     * Returns whether this template contains a value with the given ID.
     *
     * @param id the potential ID of a value in this template
     * @return whether this template contains a value with the given ID
     * @see #appendValue
     * @see #setValue
     * @see #isValueSet
     * @see #removeValue
     * @see #removeValues
     * @see #blankValue
     * @see #hasValueId
     * @since 1.0
     */
    public boolean hasValueId(String id);

    /**
     * Returns when the file corresponding to this template was modified, in
     * milliseconds since the Unix epoch.
     *
     * @return the time at which the underlying template file was modified
     * @since 1.0
     */
    public long getModificationTime();

    /**
     * Returns this template's {@linkplain BeanHandler bean handler}. The bean
     * handler is used for filling bean values into template values, and for
     * building forms.
     *
     * @return this template's bean handler
     * @since 1.0
     */
    public BeanHandler getBeanHandler();

    /**
     * Returns the encoder that this template uses to convert strings to
     * values in the template's generated text output. In an HTML template,
     * for example, this encoder may be used to convert text which may contain
     * HTML special characters like <code>&lt;&gt;</code> to corresponding
     * escaped strings.
     *
     * @return this template's encoder
     * @since 1.0
     */
    public TemplateEncoder getEncoder();

    /**
     * Adds a resource bundle to this template. Resource bundles are used in
     * many places, including when generating labels for forms, generating
     * options for <code>&lt;select&gt;</code> tags, and {@linkplain
     * #evaluateL10nTags() using localized text}.
     *
     * @param resourceBundle a resource bundle
     * @see #getResourceBundles
     * @see #hasResourceBundles
     * @since 1.0
     */
    public void addResourceBundle(ResourceBundle resourceBundle);

    /**
     * Returns a list of the resource bundles used by this template. This will
     * contain any bundles added through {@link #addResourceBundle
     * addResourceBundle} as well as any default resource bundles.
     *
     * @return a list of this template's resource bundles
     * @see #addResourceBundle
     * @see #hasResourceBundles
     * @since 1.0
     */
    public Collection<ResourceBundle> getResourceBundles();

    /**
     * Returns whether this template has any resource bundles {@linkplain
     * #addResourceBundle registered}.
     *
     * @return whether this template contains any resource bundles
     * @see #addResourceBundle
     * @see #getResourceBundles
     * @since 1.0
     */
    public boolean hasResourceBundles();

    /**
     * Sets this template's current language code, such as "en".
     * <p>This is used when {@link #evaluateL10nTags filling localized text
     * values}.
     *
     * @param lang a 2-letter language code for the language to be used by
     *             this template
     * @see #getLanguage
     * @since 1.0
     */
    public void setLanguage(String lang);

    /**
     * Returns this template's current 2-letter language code. This code is
     * used when {@link #evaluateL10nTags filling localized text values}. If
     * the language has not been {@linkplain #setLanguage set}, it defaults to
     * the language set by the RIFE configuration parameter "<code>L10N_DEFAULT_LANGUAGE</code>".
     *
     * @return the 2-letter language code currently used by this template
     * @see #setLanguage
     * @since 1.0
     */
    public String getLanguage();
//
//    /**
//     * Sets a variable which can be accessed by {@linkplain
//     * #evaluateExpressionTags expression tags} in OGNL, Groovy, or Janino.
//     *
//     * @param name  the name of the variable
//     * @param value the value to associate with the given variable name
//     * @see #setExpressionVars
//     * @see #getExpressionVars
//     * @since 1.0
//     */
//    public void setExpressionVar(String name, Object value);
//
//    /**
//     * Sets the given variables to the given corresponding values, for use in
//     * {@linkplain #evaluateExpressionTags expression tags}. Calling this
//     * method is equivalent to calling {@link #setExpressionVar
//     * setExpressionVar} for each entry in the given map.
//     *
//     * @param map a map from variable name to variable value
//     * @see #setExpressionVar
//     * @see #getExpressionVars
//     * @since 1.0
//     */
//    public void setExpressionVars(Map<String, Object> map);
//
//    /**
//     * Returns the name and value of all of the expression variables which
//     * have been {@linkplain #setExpressionVar set} in this template.
//     *
//     * @return the expression variables currently set in this template
//     * @see #setExpressionVar
//     * @see #setExpressionVars
//     * @since 1.0
//     */
//    public Map<String, Object> getExpressionVars();
//
//    /**
//     * Stores the given value in a cache, associated with the given key. This
//     * is mainly used by OGNL, Groovy, and Janino expression evaluation system
//     * to store caches to classes. You should probably not use the template
//     * caching system to avoid conflicting with the expression evaluation
//     * system.
//     *
//     * @param key   a name under which the given value should be stored
//     * @param value an object
//     * @since 1.0
//     */
//    public void cacheObject(String key, Object value);
//
//    /**
//     * Returns the value corresponding to the given key in this template's
//     * cache, or <code>null</code> if no such cached object exists. As noted
//     * in {@link #cacheObject}, you should probably not use this method to
//     * avoid conflicting with RIFE's internal use of the cache.
//     *
//     * @param key a key whose associated cached object should be returned
//     * @return the value associated with the given key, or <code>null</code>
//     * if none exists
//     * @since 1.0
//     */
//    public Object getCacheObject(String key);

    /**
     * Returns a list of URL's that this template depends on, and their last
     * modification dates (in milliseconds since the Unix epoch). This method
     * should return templates which are included by this template, and any
     * other resources which are required to fully render the template.
     *
     * @return a list of URL's and last modification dates of resources on
     * which this template depends
     * @since 1.0
     */
    public Map<URL, Long> getDependencies();

    /**
     * Returns this template's name, without path information or file
     * extension. For example, for /Users/me/Something/templates/xyz.html,
     * this method will return "xyz".
     *
     * @return this template's name, without path or file extension
     * @since 1.0
     */
    public String getName();

    /**
     * Returns this template's full name, as it was used to instantiate it
     * by a template factory.
     *
     * @return this template's full name
     * @since 1.6
     */
    public String getFullName();

    /**
     * Returns this template's default content type, for example <code>text/html</code>.
     *
     * @return this template's default content type; or
     * <p><code>null</code> if no default content type is known for this template instance
     * @since 1.3
     */
    public String getDefaultContentType();

    /**
     * Returns a shallow copy of this template, with the same values,
     * expression variables, and so on.
     *
     * @return a shallow copy of this template
     * @since 1.0
     */
    public Object clone();
}