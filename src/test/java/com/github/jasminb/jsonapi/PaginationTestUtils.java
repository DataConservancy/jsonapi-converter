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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class PaginationTestUtils {

    private PaginationTestUtils() {
        // Dis-allow construction
    }

    /**
     * Returns a List of {@code TestResource} using the supplied ids.  Each id will be used to create one {@code
     * TestResource} object.  The returned List will be ordered as {@code ids}.
     *
     * @param ids the ids used for TestResource instances.
     * @return a List of TestResource objects
     */
    @SuppressWarnings("unchecked")
    static List ofIds(final String... ids) {
        final ArrayList resources = new ArrayList();
        for (String id : ids) {
            resources.add(new TestResource(id));
        }

        return resources;
    }

    /**
     * A simple object carrying a string identifier, and a hashCode() and equals() implementation.
     */
    static class TestResource {
        private final String id;

        public TestResource(final String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TestResource that = (TestResource) o;

            return id != null ? id.equals(that.id) : that.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TestResource{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    /**
     * A Meta object used to supply pagination information.
     *
     * @param <T>
     */
    static class Meta<T> extends HashMap<String, T> {

        private final Integer total;

        private final Integer perPage;

        @SuppressWarnings("unchecked")
        public Meta(final Integer total, final Integer perPage) {
            this.total = total;
            this.perPage = perPage;

            put("total", (T) total);
            put("per_page", (T) perPage);
        }

        public int getTotal() {
            return total;
        }

        public int getPerPage() {
            return perPage;
        }
    }

    static class Links extends HashMap<String, Link> {
        private Link first;
        private Link last;
        private Link next;
        private Link prev;

        public Links() {

        }

        public Links(String first, String last, String next, String prev) {
            this.first = new Link(first);
            this.last = new Link(last);
            this.next = new Link(next);
            this.prev = new Link(prev);
            put(JSONAPISpecConstants.FIRST, this.first);
            put(JSONAPISpecConstants.LAST, this.last);
            put(JSONAPISpecConstants.NEXT, this.next);
            put(JSONAPISpecConstants.PREV, this.prev);
        }

        public Links(Link first, Link last, Link next, Link prev) {
            this.first = first;
            this.last = last;
            this.next = next;
            this.prev = prev;
            put(JSONAPISpecConstants.FIRST, this.first);
            put(JSONAPISpecConstants.LAST, this.last);
            put(JSONAPISpecConstants.NEXT, this.next);
            put(JSONAPISpecConstants.PREV, this.prev);
        }

        public Link getFirst() {
            return first;
        }

        public Link getLast() {
            return last;
        }

        public Link getNext() {
            return next;
        }

        public Link getPrev() {
            return prev;
        }

        public void setFirst(Link first) {
            this.first = first;
            put(JSONAPISpecConstants.FIRST, this.first);
        }

        public void setLast(Link last) {
            this.last = last;
            put(JSONAPISpecConstants.LAST, this.last);
        }

        public void setNext(Link next) {
            this.next = next;
            put(JSONAPISpecConstants.NEXT, this.next);
        }

        public void setPrev(Link prev) {
            this.prev = prev;
            put(JSONAPISpecConstants.PREV, this.prev);
        }
    }

    static class ResourceListBuilder<T> {
        private LinksBuilder<T> linksBuilder;
        private MetaBuilder<T> metaBuilder;
        private ResourceList<T> list;
        private Links links;
        private Map<String, Object> meta;

        ResourceListBuilder() {
            linksBuilder = new LinksBuilder<>();
            metaBuilder = new MetaBuilder<>();
        }

        ResourceListBuilder<T> wrap(List<T> toWrap) {
            this.list = new ResourceList<>(toWrap);
            return this;
        }

        LinksBuilder<T> withLinks() {
            if (list == null) {
                throw new IllegalStateException("Must call wrap(...) first.");
            }
            return linksBuilder.newBuilder(this);
        }

        MetaBuilder<T> withMeta() {
            if (list == null) {
                throw new IllegalStateException("Must call wrap(...) first.");
            }
            return metaBuilder.newBuilder(this);
        }

        ResourceList<T> finish() {
            if (links != null) {
                this.list.setLinks(links);
            }
            if (meta != null) {
                this.list.setMeta(meta);
            }
            return this.list;
        }
    }

    static class LinksBuilder<T> {
        private ResourceListBuilder<T> listBuilder;

        LinksBuilder<T> newBuilder(ResourceListBuilder<T> listBuilder) {
            this.listBuilder = listBuilder;
            this.listBuilder.links = new Links();
            return this;
        }

        LinksBuilder<T> addNext(String href) {
            this.listBuilder.links.setNext(new Link(href));
            return this;
        }

        LinksBuilder<T> addPrev(String href) {
            this.listBuilder.links.setPrev(new Link(href));
            return this;
        }

        LinksBuilder<T> addFirst(String href) {
            this.listBuilder.links.setFirst(new Link(href));
            return this;
        }

        LinksBuilder<T> addLast(String href) {
            this.listBuilder.links.setLast(new Link(href));
            return this;
        }

        ResourceListBuilder<T> and() {
            return listBuilder;
        }

        ResourceList<T> finish() {
            return listBuilder.finish();
        }
    }

    static class MetaBuilder<T> {
        private ResourceListBuilder<T> listBuilder;

        MetaBuilder<T> newBuilder(ResourceListBuilder<T> listBuilder) {
            this.listBuilder = listBuilder;
            this.listBuilder.meta = new HashMap<>();
            return this;
        }

        MetaBuilder<T> add(String key, Object value) {
            this.listBuilder.meta.put(key, value);
            return this;
        }

        ResourceListBuilder<T> and() {
            return listBuilder;
        }

        ResourceList<T> finish() {
            return listBuilder.finish();
        }
    }
}
