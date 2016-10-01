/*
 * Copyright 2016 Johns Hopkins University
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
package com.github.jasminb.jsonapi.models.recursion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ProbeResolver;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.annotations.Type;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Insures that all relationships get resolved when there are multiple relationships in a single JSON object point to
 * the same thing.
 * <p>
 * There was a bug in the code that prevented recursive loops: when it found a relationship URL that had been resolved,
 * it simply returned.  The proper behavior, however, is to 1) set the object for the relationship, even if the
 * relationship has already been seen, and 2) <em>continue</em> the loop (rather than breaking) to process additional
 * relationships.
 * </p>
 * <p>
 * This test insures that relationships continue to be resolved; in this test, the relationship is the "bizrel"
 * relationship.
 * </p>
 */
@Type("node")
public class Continue {

    @Test
    public void testContinue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Class<BizRelNode> typeUnderTest = BizRelNode.class;

        ProbeResolver resolver = new ProbeResolver(new HashMap<String, String>() {
            {
                put("http://example.com/node/1",
                        org.apache.commons.io.IOUtils.toString(
                                this.getClass().getResourceAsStream("node-1.json"), Charset.forName("UTF-8")));
            }
        });

        ResourceConverter underTest = new ResourceConverter(mapper, typeUnderTest, Node.class);
        underTest.setGlobalResolver(resolver);

        BizRelNode node = underTest.readObject(
                org.apache.commons.io.IOUtils.toByteArray(
                        this.getClass().getResource("continue.json")), typeUnderTest);

        // Sanity

        assertNotNull(node);
        assertEquals("3", node.getId());
        assertEquals("bar", node.getName());
        assertNotNull(node.getNode());
        assertNotNull(node.getParent());
        assertSame(node.getNode(), node.getParent());

        // The bizrel relationship should be set.

        assertEquals("http://www.google.com", node.getBizrel());
    }
}
