package com.github.jasminb.jsonapi.models.link;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Link;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.Map;

/**
 * Class that contains multiple fields annotated with {@code Link}, which is not allowed.
 */
@Type("node")
public class NodeWithMultipleLinks {

    @Id
    private String id;

    @Link
    private Map<String, String> links;

    @Link
    private Map<String, String> anotherLinkObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public Map<String, String> getAnotherLinkObject() {
        return anotherLinkObject;
    }

    public void setAnotherLinkObject(Map<String, String> anotherLinkObject) {
        this.anotherLinkObject = anotherLinkObject;
    }
}
