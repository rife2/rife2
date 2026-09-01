/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */

/**
 * Provides a generated administration for bean classes, which is derived
 * from the constraints that they are already described with.
 * <p>A bean that is administered has to validate, which it does by
 * extending {@link rife.validation.Validation} or {@link
 * rife.validation.MetaData}, by carrying a metadata class of its own that is
 * merged into it, or by implementing {@link rife.validation.Validated}
 * itself, and it has to be constructible without arguments. Both are
 * verified when it's registered, the first one with an {@link
 * rife.crud.exceptions.UnvalidatedBeanException} and the second one with an
 * {@link rife.crud.exceptions.InstanceCreationException}.
 * <p>Every bean also needs an identifier: an {@code int} or {@code Integer}
 * property that can be read and written and that the database keeps, named
 * by the {@code identifier} constraint or called {@code id} when the bean
 * doesn't declare one.
 * @see rife.crud.CrudAdmin
 * @since 1.10
 */
package rife.crud;
