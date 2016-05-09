package com.github.jasminb.jsonapi.models.link;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.jasminb.jsonapi.ResourceConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Insures that fields annotated with {@link com.github.jasminb.jsonapi.annotations.Link} are properly mapped from the JSON serialization to
 * their Java representation.
 */
public class LinksTest {

    /**
     * Insures that the modeler can use a custom domain object - {@link NodeLinksAsDomainObject.NodeLinks} - to model a links object in
     * primary data.
     * <pre>
     * {
     *   "data": [{
     *     "links": {
     *       "foo": "bar",
     *       "baz": "biz"
     *     },
     *     "type": "node",
     *     "id": "ts6h8"
     *   }]
     * }
     * </pre>
     * In this sample JSON, the links are simple strings.  The primary data domain object - {@link NodeLinksAsDomainObject} - stores
     * these links as an instance of {@code NodeLinks}.
     *
     * @throws Exception
     */
    @Test
    public void testPrimaryDataLinksAsDomainObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        Class<NodeLinksAsDomainObject> typeUnderTest = NodeLinksAsDomainObject.class;

        ResourceConverter underTest = new ResourceConverter(mapper, typeUnderTest);

        List<NodeLinksAsDomainObject> nodes = underTest.readObjectCollection(
                org.apache.commons.io.IOUtils.toByteArray(
                        this.getClass().getResource("links.json")), typeUnderTest);

        Assert.assertNotNull(nodes);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("ts6h8", nodes.get(0).getId());

        Assert.assertEquals("bar", nodes.get(0).getLinks().getFoo());
        Assert.assertEquals("biz", nodes.get(0).getLinks().getBaz());
    }

    /**
     * Insures that the modeler can use a simple {@code Map} to model a links object in primary data.
     * <pre>
     * {
     *   "data": [{
     *     "links": {
     *       "foo": "bar",
     *       "baz": "biz"
     *     },
     *     "type": "node",
     *     "id": "ts6h8"
     *   }]
     * }
     * </pre>
     * In this sample JSON, the links are simple strings.  The primary data domain object - {@link NodeLinksAsMap} - stores
     * these links in a {@code Map}, keyed by link type ("foo", "baz").  The values in the map are simple strings containing the URL.
     *
     * @throws Exception
     */
    @Test
    public void testPrimaryDataLinksAsMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        Class<NodeLinksAsMap> typeUnderTest = NodeLinksAsMap.class;

        ResourceConverter underTest = new ResourceConverter(mapper, typeUnderTest);

        List<NodeLinksAsMap> nodes = underTest.readObjectCollection(
                org.apache.commons.io.IOUtils.toByteArray(
                        this.getClass().getResource("links.json")), typeUnderTest);

        Assert.assertNotNull(nodes);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("ts6h8", nodes.get(0).getId());

        Assert.assertEquals("bar", nodes.get(0).getLinks().get("foo"));
        Assert.assertEquals("biz", nodes.get(0).getLinks().get("baz"));
    }

    /**
     * Insures that the modeler can use a simple {@code Map} to model a links object with a meta member in primary data.
     * <pre>
     * {
     *   "data": [{
     *     "links": {
     *       "foo": {
     *         "href": "bar",
     *         "meta": {
     *           "count": 10
     *         }
     *       },
     *       "baz": {
     *         "href": "biz",
     *         "meta": {
     *           "count": 8
     *         }
     *       }
     *     },
     *     "type": "node",
     *     "id": "ts6h8"
     *   }]
     * }
     * </pre>
     * In this sample JSON, the links are objects.  The primary data domain object - {@link NodeLinksAsMap} - stores
     * these link objects in a {@code Map}.  Keys are the name of the member.  Primitive values are stored as-is,
     * objects are stored as {@code Map}s.  This JSON will result in a link Map containing two members, "foo" and "baz".
     * The values for "foo" and "baz" will be a {@code Map} with the following keys: "href" and "meta".  The "href"
     * key returns a string, the "meta" key will return a {@code Map} containing a key named "count".
     *
     * @throws Exception
     */
    @Test
    public void testPrimaryDataLinksAsMapWithMeta() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

        Class<NodeLinksAsMap> typeUnderTest = NodeLinksAsMap.class;

        ResourceConverter underTest = new ResourceConverter(mapper, typeUnderTest);

        List<NodeLinksAsMap> nodes = underTest.readObjectCollection(
                org.apache.commons.io.IOUtils.toByteArray(
                        this.getClass().getResource("links-with-meta.json")), typeUnderTest);

        Assert.assertNotNull(nodes);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("ts6h8", nodes.get(0).getId());

        Assert.assertEquals(2, nodes.get(0).getLinks().size());

        Map<String, ?> links = nodes.get(0).getLinks();
        Assert.assertEquals("bar", ((Map) links.get("foo")).get("href"));
        Assert.assertEquals(10, ((Map) ((Map) links.get("foo")).get("meta")).get("count"));

        Assert.assertEquals("biz", ((Map) links.get("baz")).get("href"));
        Assert.assertEquals(8, ((Map) ((Map) links.get("baz")).get("meta")).get("count"));
    }

//    @Test
//    public void testPrimaryDataLinksAsDomainObjectMeta() throws Exception {
//        TypeFactory tf = TypeFactory.defaultInstance();
//        MapLikeType mlt = tf.constructMapLikeType(HashMap.class, String.class, NodeLinkWithMeta.class);
//        mlt.
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
//
//        Class<NodeLinksAsDomainObjectMeta> typeUnderTest = NodeLinksAsDomainObjectMeta.class;
//
//        ResourceConverter underTest = new ResourceConverter(mapper, typeUnderTest);
//
//        List<NodeLinksAsDomainObjectMeta> nodes = underTest.readObjectCollection(
//                org.apache.commons.io.IOUtils.toByteArray(
//                        this.getClass().getResource("links-with-meta.json")), typeUnderTest);
//
//        Assert.assertNotNull(nodes);
//        Assert.assertEquals(1, nodes.size());
//        Assert.assertEquals("ts6h8", nodes.get(0).getId());
//
//        Assert.assertEquals("bar", nodes.get(0).getLinks().get("foo").getHref());
//        Assert.assertEquals(10, nodes.get(0).getLinks().get("foo").getNodeMeta().getCount());
//    }

    /**
     * Insures that primary types contain at most one field annotated with {@code Link}.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMultipleLinkAnnotations() throws Exception {
        new ResourceConverter(new ObjectMapper(), NodeWithMultipleLinks.class);
    }
}
