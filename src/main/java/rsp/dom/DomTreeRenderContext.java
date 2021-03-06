package rsp.dom;

import rsp.ref.Ref;
import rsp.page.EventContext;
import rsp.page.RenderContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DomTreeRenderContext implements RenderContext {

    public final ConcurrentHashMap<Event.Target, Event> events = new ConcurrentHashMap();
    public final ConcurrentHashMap<Ref, VirtualDomPath> refs = new ConcurrentHashMap();

    public Tag root;
    private Deque<Tag> tagsStack = new ArrayDeque<>();

    public DomTreeRenderContext() {
    }

    @Override
    public void openNode(XmlNs xmlns, String name) {
        if (root == null) {
            root = new Tag(VirtualDomPath.DOCUMENT, xmlns, name);
            tagsStack.push(root);
        } else {
            final Tag parent = tagsStack.peek();
            final int nextChild = parent.children.size() + 1;
            final Tag newTag = new Tag(parent.path.childNumber(nextChild), xmlns, name);
            parent.addChild(newTag);
            tagsStack.push(newTag);
        }
    }

    @Override
    public void closeNode(String name) {
        tagsStack.pop();
    }

    @Override
    public void setAttr(XmlNs xmlNs, String name, String value, boolean isProperty) {
        tagsStack.peek().addAttribute(name, value, isProperty);
    }

    @Override
    public void setStyle(String name, String value) {
        tagsStack.peek().addStyle(name, value);
    }

    @Override
    public void addTextNode(String text) {
        tagsStack.peek().addChild(new Text(tagsStack.peek().path, text));
    }

    @Override
    public void addEvent(Optional<VirtualDomPath> elementPath,
                         String eventType,
                         Consumer<EventContext> eventHandler,
                         boolean preventDefault,
                         Event.Modifier modifier) {
        final VirtualDomPath eventPath = elementPath.orElse(tagsStack.peek().path);
        final Event.Target eventTarget = new Event.Target(eventType, eventPath);
        events.put(eventTarget, new Event(eventTarget, eventHandler, preventDefault, modifier));
    }

    @Override
    public void addRef(Ref ref) {
        refs.put(ref, tagsStack.peek().path);
    }

    @Override
    public String toString() {
        if (root == null) {
            throw new IllegalStateException("DOM tree not initialized");
        }
        final StringBuilder sb = new StringBuilder();
        root.appendString(sb);
        return sb.toString();
    }
}


