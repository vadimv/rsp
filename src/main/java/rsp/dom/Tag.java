package rsp.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Tag implements Node {

    public final XmlNs xmlns;
    public final String name;
    private final boolean isSelfClosing;

    public final CopyOnWriteArraySet<Attribute> attributes = new CopyOnWriteArraySet<>();
    public final CopyOnWriteArraySet<Style> styles = new CopyOnWriteArraySet<>();
    public final List<Node> children = new ArrayList<>();

    public Tag(final XmlNs xmlns, final String name, boolean isSelfClosing) {
        this.xmlns = xmlns;
        this.name = name;
        this.isSelfClosing = isSelfClosing;
    }

    public void addChild(final Node node) {
        children.add(node);
    }

    public void addAttribute(final String name, final String value, final boolean isProperty) {
        attributes.add(new Attribute(name, value, isProperty));
    }

    public void addStyle(final String name, final String value) {
        styles.add(new Style(name, value));
    }


    @Override
    public void appendString(final StringBuilder sb) {
        sb.append('<');
        sb.append(name);
        if (styles.size() > 0) {
            sb.append(" style=\"");
            for (final Style style: styles) {
                sb.append(style.name);
                sb.append(":");
                sb.append(style.value);
                sb.append(";");
            }
            sb.append('"');
        }
        if (attributes.size() > 0) {
            for (final Attribute attribute: attributes) {
                sb.append(' ');
                sb.append(attribute.name);
                sb.append('=');
                sb.append('"');
                sb.append(attribute.value);
                sb.append('"');
            }
        }
        if (isSelfClosing) {
            sb.append(" />");
        } else {
            sb.append('>');

            if (children.size() > 0) {
                for (final Node childNode: children) {
                    childNode.appendString(sb);
                }
            }

            sb.append("</");
            sb.append(name);
            sb.append('>');
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        appendString(sb);
        return sb.toString();
    }
}
