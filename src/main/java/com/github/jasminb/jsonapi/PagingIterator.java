/*
 * Copyright 2017 Johns Hopkins University
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jasminb.jsonapi;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class PagingIterator<E> implements Iterator<E> {

    private final RelationshipResolver resolver;

    private final ResourceConverter converter;

    private final Class<E> type;

    ResourceList<E> currentList;

    Iterator<E> currentItr;

    /**
     * @param resolver
     * @param initial
     * @param type
     */
    public PagingIterator(final RelationshipResolver resolver, final ResourceConverter converter, final ResourceList<E> initial,
                          final Class<E> type) {
        if (resolver == null) {
            throw new IllegalArgumentException("OsfService must not be null.");
        }

        if (converter == null) {
            throw new IllegalArgumentException("ResourceConverter must not be null");
        }

        if (initial == null) {
            throw new IllegalArgumentException("PaginatedList must not be null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }

        this.resolver = resolver;
        this.converter = converter;
        this.type = type;
        this.currentList = initial;
        this.currentItr = initial.iterator();
    }

    @Override
    public boolean hasNext() {
        if (currentItr == null) {
            return false;
        }

        if (currentItr.hasNext()) {
            return true;
        }

        // can we get more pages?
        if (getNextInternal()) {
            return currentItr.hasNext();
        }

        return false;
    }

    @Override
    public E next() {
        if (currentItr == null) {
            throw new NoSuchElementException();
        }

        if (currentItr.hasNext()) {
            return currentItr.next();
        }

        // can we get more pages?
        if (getNextInternal()) {
            return currentItr.next();
        }

        throw new NoSuchElementException();
    }

    /**
     * Manages the state of {@code currentList} and {@code currentItr}
     *
     * @throws IOException
     */
    boolean getNextInternal() {
        final String next = currentList.getNext();
        if (next == null) {
            currentList = null;
            currentItr = null;
            return false;
        }

        try {
            currentList = converter.readObjectCollection(resolver.resolve(next), type);
            currentItr = currentList.iterator();
            return true;
        } catch (Exception e) {
//            LOG.info("Error retrieving results page '{}': {}", next, e.getMessage(), e);
            currentList = null;
            currentItr = null;
        }

        return false;
    }

}
