/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ManyToManyList<E> extends AbstractManyToManyCollection<E> implements List<E> {
    private List<E> delegate_;

    ManyToManyList(AbstractGenericQueryManager manager, String columnName1, int objectId, ManyToManyDeclaration declaration) {
        super(manager, columnName1, objectId, declaration);
    }

    protected void ensurePopulatedDelegate() {
        if (null == delegate_) {
            delegate_ = restoreManyToManyMappings();
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

    public boolean addAll(int index, Collection<? extends E> c) {
        ensurePopulatedDelegate();
        return delegate_.addAll(index, c);
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

    public E get(int index) {
        ensurePopulatedDelegate();
        return delegate_.get(index);
    }

    public E set(int index, E element) {
        ensurePopulatedDelegate();
        return delegate_.set(index, element);
    }

    public void add(int index, E element) {
        ensurePopulatedDelegate();
        delegate_.add(index, element);
    }

    public E remove(int index) {
        ensurePopulatedDelegate();
        return delegate_.remove(index);
    }

    public int indexOf(Object o) {
        ensurePopulatedDelegate();
        return delegate_.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        ensurePopulatedDelegate();
        return delegate_.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        ensurePopulatedDelegate();
        return delegate_.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        ensurePopulatedDelegate();
        return delegate_.listIterator(index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        ensurePopulatedDelegate();
        return delegate_.subList(fromIndex, toIndex);
    }
}
