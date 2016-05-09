package com.github.jasminb.jsonapi.models.link;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Link;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.Map;

/**
 * Domain object which stores JSON-API links objects in a simple {@code Map}.  If the JSON-API links object is represented as strings, then
 * the {@code Map} values will be strings.  If the JSON-API links object is represented as objects (e.g. containing "href" and/or "meta"
 * members), then the {@code Map} values will be {@code Map}s keyed by the JSON member names (e.g. "href", "meta").
 * Sample JSON with a links object represented as strings:
 * <pre>
 * "links": {
 *   "foo": "bar",
 *   "baz": "biz"
 * }
 * </pre>
 * Sample JSON with a links object represented as objects:
 * <pre>
 * "links": {
 *   "foo": {
 *     "href": "bar",
 *     "meta": {
 *       "count": 10
 *     }
 *   },
 *   "baz": {
 *     "href": "biz",
 *     "meta": {
 *       "count": 8
 *     }
 *   }
 * }
 * </pre>
 */
@Type("node")
public class NodeLinksAsMap {

    @Id
    private String id;

    @Link
    private Map<String, ?> links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, ?> getLinks() {
        return links;
    }

    public void setLinks(Map<String, ?> links) {
        this.links = links;
    }
}
