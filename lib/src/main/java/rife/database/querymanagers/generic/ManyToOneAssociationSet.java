/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class ManyToOneAssociationSet<E> extends AbstractManyToOneAssociationCollection<E> implements Set<E> {
    private Set<E> delegate_;

    ManyToOneAssociationSet(AbstractGenericQueryManager manager, int objectId, ManyToOneAssociationDeclaration declaration) {
        super(manager, objectId, declaration);
    }

    protected void ensurePopulatedDelegate() {
        if (null == delegate_) {
            delegate_ = new HashSet<E>(restoreManyToOneAssociations());
        }
    }

    public int size() {
        ensurePopulatedDelegate();
        return delegate_.size();
    }

    public boolean isEmpty() {
        ensurePopulatedDelegate();
        return delegate_.isEmpty();
    }

    public boolean contains(Object o) {
        ensurePopulatedDelegate();
        return delegate_.contains(o);
    }

    public Iterator<E> iterator() {
        ensurePopulatedDelegate();
        return delegate_.iterator();
    }

    public Object[] toArray() {
        ensurePopulatedDelegate();
        return delegate_.toArray();
    }

    public <T extends Object> T[] toArray(T[] a) {
        ensurePopulatedDelegate();
        return delegate_.toArray(a);
    }

    public boolean add(E o) {
        ensurePopulatedDelegate();
        return delegate_.add(o);
    }

    public boolean remove(Object o) {
        ensurePopulatedDelegate();
        return delegate_.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        ensurePopulatedDelegate();
        return delegate_.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        ensurePopulatedDelegate();
        return delegate_.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        ensurePopulatedDelegate();
        return delegate_.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        ensurePopulatedDelegate();
        return delegate_.retainAll(c);
    }

    public void clear() {
        ensurePopulatedDelegate();
        delegate_.clear();
    }
}
