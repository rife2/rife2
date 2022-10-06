/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

public interface ManyToOneJoinColumnProcessor {
    boolean processJoinColumn(String columnName, String propertyName, ManyToOneDeclaration declaration);
}
