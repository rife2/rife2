/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.validation;

/**
 * This interface can optionally be implemented by a class implementing the
 * <code>MetaDataMerged</code> interface.
 * <p>By implementing the methods here, each metadata instance will be made
 * aware of the bean that has been associated with.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see MetaDataMerged
 * @since 1.0
 */
public interface MetaDataBeanAware {
    /**
     * <p>This method will be called by RIFE2 when a new instance of the
     * metadata class has been created.
     *
     * @param bean the bean instance that this particular metadata instance
     *             has been associated with
     * @since 1.0
     */
    void setMetaDataBean(Object bean);

    /**
     * Has to return the bean instance that has been associated with this
     * metadata class instance.
     *
     * @return this metadata's bean instance
     * @since 1.0
     */
    Object retrieveMetaDataBean();
}
