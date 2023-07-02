package rsp.component;

import rsp.dom.Event;
import rsp.dom.Tag;
import rsp.dom.VirtualDomPath;
import rsp.html.SegmentDefinition;
import rsp.page.LivePage;
import rsp.page.RenderContext;
import rsp.ref.Ref;
import rsp.server.Out;
import rsp.server.Path;
import rsp.stateview.ComponentView;
import rsp.stateview.NewState;
import rsp.util.Lookup;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Component<T, S> implements NewState<S> {

    private final Map<Event.Target, Event> events = new HashMap<>();
    private final Map<Ref, VirtualDomPath> refs = new HashMap<>();
    private final List<Component<?, ?>> children = new ArrayList<>();

    private final Lookup stateOriginLookup;
    private final Class<T> stateOriginClass;
    private final Function<T, CompletableFuture<S>> resolveStateFunction;
    private final BiFunction<S, Path, Path> state2pathFunction;
    private final ComponentView<S> componentView;
    private final RenderContext parentRenderContext;
    private final AtomicReference<LivePage> livePageContext;

    private S state;
    public Tag tag;

    public Component(final Lookup stateOriginLookup,
                     final Class<T> stateOriginClass,
                     final Function<T, CompletableFuture<S>> resolveStateFunction,
                     final BiFunction<S, Path, Path> state2pathFunction,
                     final ComponentView<S> componentView,
                     final RenderContext parentRenderContext,
                     final AtomicReference<LivePage> livePageSupplier) {
        this.stateOriginLookup = stateOriginLookup;
        this.stateOriginClass = Objects.requireNonNull(stateOriginClass);
        this.resolveStateFunction = Objects.requireNonNull(resolveStateFunction);
        this.state2pathFunction = Objects.requireNonNull(state2pathFunction);
        this.componentView = Objects.requireNonNull(componentView);
        this.parentRenderContext = Objects.requireNonNull(parentRenderContext);
        this.livePageContext = Objects.requireNonNull(livePageSupplier);
    }

    public void addChild(final Component<?, ?> component) {
        children.add(component);
    }

    public S resolveState() {
        final T stateOrigin = stateOriginLookup.lookup(stateOriginClass);
        final CompletableFuture<S> initialStateCompletableFuture = resolveStateFunction.apply(stateOrigin);
        try {
            return initialStateCompletableFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void resolveAndSet() {
        set(resolveState());
    }

    public S getState() {
        return state;
    }

    @Override
    public void set(final S newState) {
        apply(s -> newState);
    }

    @Override
    public void applyWhenComplete(final CompletableFuture<S> newState) {
        newState.thenAccept(s -> set(s));
    }

    @Override
    public void applyIfPresent(final Function<S, Optional<S>> stateTransformer) {
        stateTransformer.apply(state).ifPresent(s -> set(s));
    }

    @Override
    public void apply(final Function<S, S> newStateFunction) {
        final LivePage livePage = livePageContext.get();
        synchronized (livePage) {
            assert tag != null;
            final Tag oldTag = tag;
            final Map<Event.Target, Event> oldEvents = new HashMap<>(events);
            state = newStateFunction.apply(state);
            final RenderContext renderContext = parentRenderContext.newSharedContext(oldTag.path);

            events.clear();
            refs.clear();

            renderContext.openComponent(this);
            final SegmentDefinition view = componentView.apply(state).apply(this);
            view.render(renderContext);
            renderContext.closeComponent();

            tag = renderContext.rootTag();
            final Set<VirtualDomPath> elementsToRemove = livePage.updateElements(Optional.ofNullable(oldTag),
                                                                                 renderContext.rootTag());
            livePage.updateEvents(new HashSet<>(oldEvents.values()), new HashSet<>(events.values()), elementsToRemove);

            // Browser's navigation
            livePage.applyToPath(path -> state2pathFunction.apply(state, path));
        }
    }

    public Map<Event.Target, Event> recursiveEvents() {
        final Map<Event.Target, Event> recursiveEvents = new HashMap<>();
        recursiveEvents.putAll(events);
        for (Component<?, ?> childComponent : children) {
            recursiveEvents.putAll(childComponent.recursiveEvents());
        }
        return recursiveEvents;
    }

    public Map<Ref, VirtualDomPath> recursiveRefs() {
        final Map<Ref, VirtualDomPath> recursiveRefs = new HashMap<>();
        recursiveRefs.putAll(refs);
        for (Component<?, ?> childComponent : children) {
            recursiveRefs.putAll(childComponent.recursiveRefs());
        }
        return recursiveRefs;
    }

    public void listenEvents(final Out out) {
        out.listenEvents(events.values().stream().collect(Collectors.toList()));
        children.forEach(childComponent -> childComponent.listenEvents(out));
    }

    public void addEvent(Event.Target eventTarget, Event event) {
        events.put(eventTarget, event);
    }

    public void addRef(Ref ref, VirtualDomPath path) {
        refs.put(ref, path);
    }
}
