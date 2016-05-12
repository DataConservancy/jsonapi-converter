package com.github.jasminb.jsonapi.models.link;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Links;
import com.github.jasminb.jsonapi.annotations.Type;

/**
 * Domain object which stores JSON-API links objects in a domain object named {@link NodeLinks}.
 */
@Type("node")
public class NodeLinksAsDomainObject {

    @Id
    private String id;

    @Links
    private NodeLinks links;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeLinks getLinks() {
        return links;
    }

    public void setLinks(NodeLinks links) {
        this.links = links;
    }

    /**
     * Models string representations of JSON-API links objects as a domain object (in contrast to a {@code Map}).
     * Sample JSON:
     * <pre>
     * "links": {
     *   "foo": "bar",
     *   "baz": "biz"
     * }
     * </pre>
     */
    public static class NodeLinks {

        private String foo;

        private String baz;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public String getBaz() {
            return baz;
        }

        public void setBaz(String baz) {
            this.baz = baz;
        }
    }
}
