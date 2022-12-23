/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

/**
 * This interface is merely a marker interface to indicate to RIFE2 that it
 * should consider this class as a <code>MetaData</code> class whose
 * interfaces will be automatically merged into the sibling class that it's
 * augmenting.
 * <p>So, consider a class <code>Foo</code> and another class
 * <code>FooMetaData</code>. When <code>FooMetaData</code> implements
 * <code>MetaDataMerged</code>, RIFE2 will adapt <code>Foo</code> and make it
 * implement all the interfaces that <code>FooMetaData</code> implements.
 * Also, when the default constructor of <code>Foo</code> is called, an
 * instance of <code>FooMetaData</code> will be created and stored in a new
 * hidden member variable. The added method implementations simple delegate to
 * the instance of <code>FooMetaData</code>.
 * <p>Optionally, <code>FooMetaData</code> can also implement
 * <code>MetaDataBeanAware</code>, in which case the instance of
 * <code>FooMetaData</code> will receive the instance of Foo that it belongs
 * to right after it has been instantiated.
 * <p>Note that the relationship between <code>Foo</code> and
 * <code>FooMetaData</code> is purely name based (the <code>MetaData</code>
 * suffix). RIFE2 will look up the metadata class through the classpath, which
 * means that it's possible to augment any class, anywhere, in any package,
 * even without having the source code.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see MetaDataBeanAware
 * @since 1.0
 */
public interface MetaDataMerged {
}
