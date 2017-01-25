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

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class PagingIteratorTest {

    private final RelationshipResolver resolver = mock(RelationshipResolver.class);

    private final ResourceConverter converter = mock(ResourceConverter.class);

    private final ResourceList<?> resources = mock(ResourceList.class);

    private final Class<?> clazz = Object.class;

    private final PagingIterator underTest = new PagingIterator(resolver, converter, resources, clazz);

    @Test
    public void testGetNextInternalThrowsIOE() throws Exception {
        when(resources.getNext()).thenReturn("http://example.org/");
        when(resolver.resolve(anyString())).thenThrow(new RuntimeException("Error message here"));

        assertFalse(underTest.getNextInternal());

        // Iterator retrieved on construction, but it is *not* retrieved in getNextInternal()
        verify(resources, times(1)).iterator();
        assertNull(underTest.currentItr);
        assertNull(underTest.currentList);
        verify(resources).getNext();
        verify(resolver).resolve(anyString());

        // hasNext() should be false
        assertFalse(underTest.hasNext());

        // next() should returne NSEE
        try {
            underTest.next();
            fail("Expected a NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

}