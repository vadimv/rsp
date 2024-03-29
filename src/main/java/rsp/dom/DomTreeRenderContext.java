package rsp.dom;

import rsp.component.Component;
import rsp.component.ComponentView;
import rsp.page.EventContext;
import rsp.page.RenderContext;
import rsp.ref.Ref;
import rsp.server.Path;
import rsp.server.RemoteOut;
import rsp.server.http.HttpStateOriginLookup;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DomTreeRenderContext implements RenderContext {
    private final VirtualDomPath rootDomPath;
    private final Path baseUrlPath;
    private final HttpStateOriginLookup httpStateOriginLookup;
    private final AtomicReference<RemoteOut> remoteOutReference;

    private final Deque<Tag> tagsStack = new ArrayDeque<>();
    private final Deque<Component<?, ?>> componentsStack = new ArrayDeque<>();

    private int statusCode;
    private Map<String, String> headers;
    private String docType;
    private Tag rootTag;
    private Component<?, ?> rootComponent;

    public DomTreeRenderContext(final VirtualDomPath rootDomPath,
                                final Path baseUrlPath,
                                final HttpStateOriginLookup httpStateOriginLookup,
                                final AtomicReference<RemoteOut> remoteOutReference) {
        this.baseUrlPath = Objects.requireNonNull(baseUrlPath);
        this.rootDomPath = Objects.requireNonNull(rootDomPath);
        this.httpStateOriginLookup = Objects.requireNonNull(httpStateOriginLookup);
        this.remoteOutReference = Objects.requireNonNull(remoteOutReference);
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String docType() {
        return docType;
    }

    public Tag rootTag() {
        return rootTag;
    }

    @Override @SuppressWarnings("unchecked")
    public <T, S> Component<T, S> rootComponent() {
        return (Component<T, S>) rootComponent;
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
        if (rootTag == null) {
            rootTag = new Tag(rootDomPath, xmlns, name);
            tagsStack.push(rootTag);
            trySetCurrentComponentRootTag(rootTag);
        } else {
            final Tag parent = tagsStack.peek();
            assert parent != null;
            final int nextChild = parent.children.size() + 1;
            final Tag newTag = new Tag(parent.path().childNumber(nextChild), xmlns, name);
            parent.addChild(newTag);
            tagsStack.push(newTag);
            trySetCurrentComponentRootTag(newTag);
        }
    }

    private void trySetCurrentComponentRootTag(final Tag newTag) {
        final Component<?, ?> component = componentsStack.peek();
        if (component != null) {
            component.setRootTagIfNotSet(newTag);
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
        tagsStack.peek().addChild(new Text(tagsStack.peek().path(), text));
    }

    @Override
    public void addEvent(final Optional<VirtualDomPath> elementPath,
                         final String eventType,
                         final Consumer<EventContext> eventHandler,
                         final boolean preventDefault,
                         final Event.Modifier modifier) {
        final VirtualDomPath eventPath = elementPath.orElse(tagsStack.peek().path());
        final Event.Target eventTarget = new Event.Target(eventType, eventPath);
        final Component<?, ?> component = componentsStack.peek();
        assert component != null;
        component.addEvent(eventTarget, new Event(eventTarget, eventHandler, preventDefault, modifier));
    }

    @Override
    public void addRef(final Ref ref) {
        final Component<?, ?> component = componentsStack.peek();
        assert component != null;
        component.addRef(ref, tagsStack.peek().path());
    }

    @Override
    public <T, S> Component<T, S> openComponent(final Class<T> stateOriginClass,
                                                final Function<T, CompletableFuture<? extends S>> initialStateFunction,
                                                final BiFunction<S, Path, Path> state2pathFunction,
                                                final ComponentView<S> componentView) {
        final Component<T, S> newComponent = new Component<T, S>(baseUrlPath,
                                                                 httpStateOriginLookup,
                                                                 stateOriginClass,
                                                                 initialStateFunction,
                                                                 state2pathFunction,
                                                                 componentView,
                                                                this,
                                                                 remoteOutReference);
        if (rootComponent == null) {
            rootComponent = newComponent;
        } else {
            final Component<?, ?> parentComponent = componentsStack.peek();
            parentComponent.addChild(newComponent);
        }
        componentsStack.push(newComponent);

        return newComponent;
    }

    @Override
    public <T, S> void openComponent(Component<T, S> component) {
        componentsStack.push(component);
    }

    @Override
    public void closeComponent() {
        componentsStack.pop();
    }

    @Override
    public RenderContext newContext(final VirtualDomPath domPath) {
        return new DomTreeRenderContext(domPath,
                                        baseUrlPath,
                                        httpStateOriginLookup,
                                        remoteOutReference);
    }

    @Override
    public RenderContext newContext() {
        return new DomTreeRenderContext(rootDomPath,
                                        baseUrlPath,
                httpStateOriginLookup,
                                        remoteOutReference);
    }

    @Override
    public String toString() {
        if (rootTag == null) {
            throw new IllegalStateException("DOM tree not initialized");
        }
        final StringBuilder sb = new StringBuilder();
        if (docType != null) {
            sb.append(docType);
        }
        rootTag.appendString(sb);
        return sb.toString();
    }
}


