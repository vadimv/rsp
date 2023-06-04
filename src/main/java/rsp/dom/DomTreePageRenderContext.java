package rsp.dom;

import rsp.ref.Ref;
import rsp.page.EventContext;
import rsp.page.PageRenderContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DomTreePageRenderContext implements PageRenderContext {
    public final Map<Event.Target, Event> events = new ConcurrentHashMap<>();
    public final Map<Ref, VirtualDomPath> refs = new ConcurrentHashMap<>();
    private final Deque<Tag> tagsStack = new ArrayDeque<>();
    private final VirtualDomPath rootPath;

    private int statusCode;
    private Map<String, String> headers;
    private String docType;
    private Tag root;


    public DomTreePageRenderContext(final VirtualDomPath rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String docType() {
        return docType;
    }

    public Tag tag() {
        return root;
    }

    public int statusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void setHeaders(final Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void setDocType(final String docType) {
        this.docType = docType;
    }

    @Override
    public void openNode(final XmlNs xmlns, final String name) {
        if (root == null) {
            root = new Tag(rootPath, xmlns, name);
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
    public void closeNode(final String name, final boolean upgrade) {
        tagsStack.pop();
    }

    @Override
    public void setAttr(final XmlNs xmlNs, final String name, final String value, final boolean isProperty) {
        tagsStack.peek().addAttribute(name, value, isProperty);
    }

    @Override
    public void setStyle(final String name, final String value) {
        tagsStack.peek().addStyle(name, value);
    }

    @Override
    public void addTextNode(final String text) {
        tagsStack.peek().addChild(new Text(tagsStack.peek().path, text));
    }

    @Override
    public void addEvent(final Optional<VirtualDomPath> elementPath,
                         final String eventType,
                         final Consumer<EventContext> eventHandler,
                         final boolean preventDefault,
                         final Event.Modifier modifier) {
        final VirtualDomPath eventPath = elementPath.orElse(tagsStack.peek().path);
        final Event.Target eventTarget = new Event.Target(eventType, eventPath);
        events.put(eventTarget, new Event(eventTarget, eventHandler, preventDefault, modifier));
    }

    @Override
    public void addRef(final Ref ref) {
        refs.put(ref, tagsStack.peek().path);
    }

    @Override
    public PageRenderContext newInstance() {
        return new DomTreePageRenderContext(root.path);
    }

    @Override
    public Map<Event.Target, Event> events() {
        return events;
    }

    @Override
    public Map<Ref, VirtualDomPath> refs() {
        return refs;
    }

    @Override
    public String toString() {
        if (root == null) {
            throw new IllegalStateException("DOM tree not initialized");
        }
        final StringBuilder sb = new StringBuilder();
        if (docType != null) {
            sb.append(docType);
        }
        root.appendString(sb);
        return sb.toString();
    }
}


