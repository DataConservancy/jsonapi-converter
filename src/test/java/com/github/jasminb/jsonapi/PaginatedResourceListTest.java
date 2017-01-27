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

import com.github.jasminb.jsonapi.PaginationTestUtils.Meta;
import com.github.jasminb.jsonapi.PaginationTestUtils.ResourceListBuilder;
import com.github.jasminb.jsonapi.PaginationTestUtils.TestResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static com.github.jasminb.jsonapi.PaginationTestUtils.ofIds;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class PaginatedResourceListTest {

    private RelationshipResolver resolver = mock(RelationshipResolver.class);

    private ResourceConverter converter = mock(ResourceConverter.class);

    private final ResourceList<TestResource> resources = mock(ResourceList.class);

    private final Class<TestResource> clazz = TestResource.class;

    private PaginatedResourceList<TestResource> underTest;

    /**
     * By default create a PaginatedResourceList that is composed of mock objects.  Individual test methods may
     * choose to compose a PaginatedResourceList differently (e.g. with a concrete ResourceList instance).
     */
    @Before
    public void setUp() {
        underTest = new PaginatedResourceList<>(resources, resolver, converter, clazz);
    }

    /**
     * When there is no 'links.next', the 'total' in top-level meta is ignored; the underlying list size is used.
     */
    @Test
    public void testTotalNoNext() {
        final Integer meta_total = 1000;
        final List results = ofIds("1", "2", "3");

        ResourceList<TestResource> page_1 = new ResourceListBuilder<TestResource>()
                .wrap(results)
                .withMeta()
                    .add("total", meta_total)
                .finish();

        underTest = new PaginatedResourceList<>(page_1, resolver, converter, clazz);

        assertEquals(results.size(), underTest.total());
        verifyZeroInteractions(resolver, converter);
    }

    /**
     * When there is a 'links.next', the 'total' in top-level meta is used; the underlying list size is ignored.
     */
    @Test
    public void testTotalWithNext() {
        final Integer meta_total = 1000;
        final List results = ofIds("1", "2", "3");

        ResourceList<TestResource> page_1 = new ResourceListBuilder<TestResource>()
                .wrap(results)
                .withMeta()
                    .add("total", meta_total)
                .and()
                .withLinks()
                    .addNext("any string")
                .finish();

        underTest = new PaginatedResourceList<>(page_1, resolver, converter, clazz);

        assertEquals(meta_total.intValue(), underTest.total());
        verifyZeroInteractions(resolver, converter);
    }

    /**
     * Demonstrates a collection can be streamed when the total size of the collection and results per page are unknown.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testPaginationWithNoTotalOrPerPage() {

        // Three pages in the collection, with one result per page.
        // There is no top-level meta object (which might otherwise contain pagination metadata)
        // Set the top-level 'links.next' of page_1 to page_2, and page_2 to page_3
        ResourceList page_1 = new ResourceListBuilder()
                .wrap(ofIds("1"))
                .withLinks()
                    .addNext("page 2")
                .finish();

        ResourceList page_2 = new ResourceListBuilder()
                .wrap(ofIds("2"))
                .withLinks()
                    .addNext("page 3")
                .finish();

        ResourceList page_3 = new ResourceListBuilder()
                .wrap(ofIds("3"))
                .finish();

        // Mock the resolver's response
        when(resolver.resolve("page 2")).thenReturn("page 2".getBytes());
        when(resolver.resolve("page 3")).thenReturn("page 3".getBytes());

        // Mock the converter to return page 2 and page 3
        when(converter.readObjectCollection(eq("page 2".getBytes()), any())).thenReturn(page_2);
        when(converter.readObjectCollection(eq("page 3".getBytes()), any())).thenReturn(page_3);


        underTest = new PaginatedResourceList(page_1, resolver, converter, clazz);

        // The collection can be paginated still (e.g. stream() will still work) but the collection is of unknown size
        assertEquals(-1, underTest.total());
        assertEquals(-1, underTest.perPage());
        assertEquals(3, underTest.stream().count());

        verify(resolver, times(2)).resolve(any());
        verify(converter, times(2)).readObjectCollection(new byte[anyByte()], any());
    }

    /**
     * Demonstrates that a collection can be streamed when the total size of the collection and results per page are
     * known.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testPaginationWithTotalAndPerPage() {

        // Three pages in the collection, with one result per page.
        // Set the top-level meta object with plausible pagination metadata, 'total' size of the collection and
        //   'per_page' max results per page
        // Set the top-level 'links.next' of page_1 to page_2, and page_2 to page_3
        ResourceList page_1 = new ResourceListBuilder()
                .wrap(ofIds("1"))
                .withLinks()
                    .addNext("page 2")
                    .and()
                .withMeta()
                    .add("total", 3)
                    .add("per_page", 1)
                .finish();

        ResourceList page_2 = new ResourceListBuilder()
                .wrap(ofIds("2"))
                .withLinks()
                    .addNext("page 3")
                    .and()
                .withMeta()
                    .add("total", 3)
                    .add("per_page", 1)
                .finish();

        ResourceList page_3 = new ResourceListBuilder()
                .wrap(ofIds("3"))
                .withMeta()
                    .add("total", 3)
                    .add("per_page", 1)
                .finish();

        // Mock the resolver's response
        when(resolver.resolve("page 2")).thenReturn("page 2".getBytes());
        when(resolver.resolve("page 3")).thenReturn("page 3".getBytes());

        // Mock the converter to return page 2 and page 3
        when(converter.readObjectCollection(eq("page 2".getBytes()), any())).thenReturn(page_2);
        when(converter.readObjectCollection(eq("page 3".getBytes()), any())).thenReturn(page_3);


        underTest = new PaginatedResourceList(page_1, resolver, converter, clazz);

        // The collection can be paginated, size and max results per page are known
        assertEquals(3, underTest.total());
        assertEquals(1, underTest.perPage());
        assertEquals(3, underTest.stream().count());

        verify(resolver, times(2)).resolve(any());
        verify(converter, times(2)).readObjectCollection(new byte[anyByte()], any());
    }

    /**
     * Demonstrates that a collection with no top-level 'links.next' value cannot be streamed.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testPaginationWithNoNext() {

        // Three pages in the collection, with two results per page.
        // Set the top-level meta object with plausible pagination metadata, 'total' size of the collection and
        //   'per_page' max results per page
        // Do _not_ set the top-level 'links.next'; without this, pagination cannot occur.
        ResourceList page_1 = new ResourceListBuilder()
                .wrap(ofIds("1", "2"))
                .withMeta()
                    .add("total", 4)
                    .add("per_page", 2)
                .finish();

        ResourceList page_2 = new ResourceListBuilder()
                .wrap(ofIds("3", "4"))
                .withMeta()
                    .add("total", 4)
                    .add("per_page", 2)
                .finish();

        ResourceList page_3 = new ResourceListBuilder()
                .wrap(ofIds("3"))
                .withMeta()
                .add("total", 3)
                .add("per_page", 1)
                .finish();

        // Mock the resolver's response
        when(resolver.resolve("page 2")).thenReturn("page 2".getBytes());
        when(resolver.resolve("page 3")).thenReturn("page 3".getBytes());

        // Mock the converter to return page 2 and page 3
        when(converter.readObjectCollection(eq("page 2".getBytes()), any())).thenReturn(page_2);
        when(converter.readObjectCollection(eq("page 3".getBytes()), any())).thenReturn(page_3);


        underTest = new PaginatedResourceList(page_1, resolver, converter, clazz);

        // Note that the 'total' pagination metadata is ignored when no 'links.next' is present (2 instead of 4)
        assertEquals(2, underTest.total());
        assertEquals(2, underTest.perPage());
        // Only two results are in the stream because the second page was not retrieved (missing a 'links.next')
        assertEquals(2, underTest.stream().count());

        verify(resolver, never()).resolve(any());
        verify(converter, never()).readObjectCollection(new byte[anyByte()], any());
    }

    @Test
    public void testNoTotalWithNoNextPage() throws Exception {
        final int size = 30;
        when(resources.getMeta()).thenReturn(null);
        when(resources.getNext()).thenReturn(null);
        when(resources.size()).thenReturn(size);

        // The collection cannot be paginated, there is no next page of results, so the size is equal to the size of the
        // underlying resource
        assertEquals(size, underTest.total());
        assertEquals(-1, underTest.perPage());

        verify(resources, atLeastOnce()).getMeta();
        verify(resources, atLeastOnce()).getNext();
        verify(resources, atLeastOnce()).size();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testSpliteratorKnownSizeNoNext() throws Exception {
        when(resources.size()).thenReturn(1);

        final Spliterator result = underTest.spliterator();

        assertEquals(Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SUBSIZED | Spliterator.SIZED,
                result.characteristics());
        verify(resources, atLeastOnce()).getMeta();
        verify(resources, atLeastOnce()).size();
        verifyZeroInteractions(resolver, converter);
        assertEquals(1, result.getExactSizeIfKnown());
    }

    @Test
    public void testSpliteratorKnownSizeWithNext() throws Exception {
        when(resources.getMeta()).thenReturn(new Meta<>(1, 10));
        when(resources.getNext()).thenReturn("any string");

        final Spliterator result = underTest.spliterator();

        assertEquals(Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SUBSIZED | Spliterator.SIZED,
                result.characteristics());
        verify(resources, atLeastOnce()).getMeta();
        verify(resources, atLeastOnce()).getNext();
        verify(resources, never()).size();
        verifyZeroInteractions(resolver, converter);
        assertEquals(1, result.getExactSizeIfKnown());
    }

    @Test
    public void testSpliteratorUnknownSize() throws Exception {
        when(resources.getMeta()).thenReturn(null);
        when(resources.getNext()).thenReturn("");

        final Spliterator result = underTest.spliterator();

        assertEquals(Spliterator.ORDERED | Spliterator.NONNULL, result.characteristics());
        verify(resources, atLeastOnce()).getMeta();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testStreamSpliteratorKnownSize() throws Exception {
        final List testResources = ofIds("1", "2");
        when(resources.getMeta()).thenReturn(new Meta<>(testResources.size(), 10));
        when(resources.iterator()).thenReturn(testResources.iterator());

        // verify the initial state of the spliterator is sized
        assertEquals(Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SUBSIZED | Spliterator.SIZED,
                underTest.spliterator().characteristics());

        final List results = underTest.stream().collect(Collectors.toList());

        assertEquals(testResources.size(), results.size());
        assertTrue(results.containsAll(testResources));
        verify(resources, atLeastOnce()).getMeta();
        verify(resources, times(2)).iterator();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testStreamSpliteratorUnknownSize() throws Exception {
        final List testResources = ofIds("1", "2");
        when(resources.getMeta()).thenReturn(null);
        when(resources.getNext()).thenReturn("");
        when(resources.iterator()).thenReturn(testResources.iterator());

        // verify the initial state of the spliterator is unsized
        assertEquals(Spliterator.ORDERED | Spliterator.NONNULL, underTest.spliterator().characteristics());

        final List results = underTest.stream().collect(Collectors.toList());

        assertEquals(testResources.size(), results.size());
        assertTrue(results.containsAll(testResources));
        // once for verifying the initial state of the spliterator in the test, once for the execution in
        // PaginatedIterator
        verify(resources, times(2)).iterator();
        verify(resources, atLeastOnce()).getMeta();
        verify(resolver).resolve("");
        verify(converter).readObjectCollection(any(), any());
    }

    @Test
    public void testIsEmpty() throws Exception {
        when(resources.isEmpty()).thenReturn(true);
        assertTrue(underTest.isEmpty());
        verify(resources).isEmpty();

        reset(resources);

        when(resources.isEmpty()).thenReturn(false);
        assertFalse(underTest.isEmpty());
        verify(resources).isEmpty();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testToArray() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        final Object[] arrayResources = underTest.toArray();

        assertEquals(arrayResources.length, testResources.size());
        assertEquals(arrayResources[0], testResources.get(0));
        assertEquals(arrayResources[1], testResources.get(1));
        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testToTypedArray() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        final TestResource[] arrayResources = underTest.toArray(new TestResource[]{});

        assertEquals(arrayResources.length, testResources.size());
        assertEquals(arrayResources[0], testResources.get(0));
        assertEquals(arrayResources[1], testResources.get(1));
        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testToOversizedTypedArray() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        final TestResource[] arrayResources = underTest.toArray(new TestResource[testResources.size() + 1]);

        assertEquals(arrayResources.length, testResources.size() + 1);
        assertEquals(arrayResources[0], testResources.get(0));
        assertEquals(arrayResources[1], testResources.get(1));
        assertEquals(null, arrayResources[2]);
        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testToUndersizedTypedArray() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        final TestResource[] arrayResources = underTest.toArray(new TestResource[testResources.size() - 1]);

        assertEquals(arrayResources.length, testResources.size());
        assertEquals(arrayResources[0], testResources.get(0));
        assertEquals(arrayResources[1], testResources.get(1));
        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testParallelStreamSupport() throws Exception {
        final List testResources = ofIds("1", "2");

        prepareForStream(testResources);
        assertFalse(underTest.stream().isParallel());
        verifyForStream();

        reset(resources);

        prepareForStream(testResources);
        // parallel streams are not supported
        assertFalse(underTest.parallelStream().isParallel());
        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNegativeIndex() throws Exception {
        underTest.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexExceedsSize() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);
        underTest.get(testResources.size() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexExceedsSizeUnknownStreamLength() throws Exception {
        final List testResources = ofIds("1", "2");
        when(resources.getMeta()).thenReturn(null);
        when(resources.getNext()).thenReturn("");
        when(resources.iterator()).thenReturn(testResources.iterator());

        underTest.get(testResources.size() + 1);
    }

    @Test
    public void testGetIndex() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        assertEquals(testResources.get(1), underTest.get(1));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testContains() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        assertTrue(underTest.contains(testResources.get(0)));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertFalse(underTest.contains(new TestResource("3")));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testIndexOf() throws Exception {
        final List testResources = ofIds("1", "2");
        prepareForStream(testResources);

        assertEquals(0, underTest.indexOf(testResources.get(0)));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertEquals(-1, underTest.indexOf(new TestResource("3")));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
    }

    @Test
    public void testLastIndexOf() throws Exception {
        final List testResources = ofIds("1", "2", "2");
        prepareForStream(testResources);

        assertEquals(2, underTest.lastIndexOf(testResources.get(2)));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertEquals(-1, underTest.indexOf(new TestResource("3")));

        verifyForStream();
    }

    @Test
    public void testSubList() throws Exception {
        final List testResources = ofIds("1", "2", "3");
        prepareForStream(testResources);

        assertEquals(testResources.subList(0, 1), underTest.subList(0, 1));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertEquals(testResources.subList(1, 2), underTest.subList(1, 2));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertEquals(testResources.subList(0, 2), underTest.subList(0, 2));

        verifyForStream();
        verifyZeroInteractions(resolver, converter);
        reset(resources);
        prepareForStream(testResources);

        assertEquals(testResources.subList(2, 2), underTest.subList(2, 2));
    }

    /**
     * Prepares the mocks such that PaginatedListAdapter.stream will return a stream over the supplied list.
     *
     * @param toStream
     */
    @SuppressWarnings("unchecked")
    void prepareForStream(final List toStream) {
        when(resources.size()).thenReturn(toStream.size());
        when(resources.getMeta()).thenReturn(new Meta<>(toStream.size(), toStream.size()));
        when(resources.iterator()).thenReturn(toStream.iterator());
    }

    /**
     * Verify mocks that should have been acted on in order to return a stream.
     */
    void verifyForStream() {
        verify(resources, atLeastOnce()).getMeta();
        verify(resources).iterator();
    }

}