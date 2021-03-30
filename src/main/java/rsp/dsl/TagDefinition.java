package rsp.dsl;

import rsp.page.PageRenderContext;
import rsp.dom.XmlNs;

import java.util.Arrays;
import java.util.Objects;

/**
 * A definition of an XML tag.
 */
public class TagDefinition<S> implements DocumentPartDefinition<S> {
    protected final XmlNs ns;
    protected final String name;
    protected final DocumentPartDefinition<S>[] children;

    /**
     * Creates a new instance of an XML tag's definition.
     * @param name the tag's name
     * @param children the children definitions, this could be another tags, attributes, events, references etc
     */
    @SafeVarargs
    public TagDefinition(XmlNs ns, String name, DocumentPartDefinition<S>... children) {
        this.ns = Objects.requireNonNull(ns);
        this.name = Objects.requireNonNull(name);
        this.children = children;
    }

    @Override
    public void accept(PageRenderContext renderContext) {
        renderContext.openNode(ns, name);
        Arrays.stream(children).forEach(c -> c.accept(renderContext));
        renderContext.closeNode(name, true);
    }
}
