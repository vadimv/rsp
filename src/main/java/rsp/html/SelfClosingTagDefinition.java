package rsp.html;

import rsp.component.ComponentRenderContext;
import rsp.dom.XmlNs;

import java.util.Arrays;

public class SelfClosingTagDefinition implements SegmentDefinition {

    protected final XmlNs ns;
    protected final String name;
    protected final AttributeDefinition[] attributeDefinitions;

    public SelfClosingTagDefinition(XmlNs ns, String name, AttributeDefinition... attributeDefinitions) {
        this.ns = ns;
        this.name = name;
        this.attributeDefinitions = attributeDefinitions;
    }

    @Override
    public boolean render(final ComponentRenderContext renderContext) {
        renderContext.openNode(ns, name, true);
        Arrays.stream(attributeDefinitions).forEach(c -> c.render(renderContext));
        renderContext.closeNode(name, false);
        return true;
    }
}
