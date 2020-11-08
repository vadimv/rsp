package rsp.dsl;

import rsp.RenderContext;
import rsp.XmlNs;

public class AttributeDefinition extends DocumentPartDefinition {
    public final String name;
    public final String value;
    public final boolean isProperty;

    public AttributeDefinition(String name, String value, boolean isProperty) {
        super(DocumentPartDefinition.DocumentPartKind.ATTR);
        this.name = name;
        this.value = value;
        this.isProperty = isProperty;
    }

    @Override
    public void accept(RenderContext renderContext) {
        renderContext.setAttr(XmlNs.html, name, value, isProperty);
    }
}
