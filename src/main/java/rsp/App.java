package rsp;

import rsp.component.ComponentDefinition;
import rsp.html.TagDefinition;
import rsp.page.PageLifeCycle;
import rsp.page.QualifiedSessionId;
import rsp.page.RenderedPage;
import rsp.routing.Routing;
import rsp.server.HttpRequest;
import rsp.stateview.ComponentView;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An assembly point for everything needed to set off a UI application.
 * This class object itself to be provided to a hosting web container, for example {@link rsp.jetty.JettyServer}.
 * @param <S> the type of the applications root component's state, should be an immutable class
 */
public final class App<S> {
    /**
     * The application's configuration.
     */
    public final AppConfig config;

    /**
     * An implementation of the lifecycle events listener.
     */
    public final PageLifeCycle<S> lifeCycleEventsListener;

    /**
     * The root of the components tree.
     */
    public final ComponentDefinition<S> rootComponent;

    public final Map<QualifiedSessionId, RenderedPage<S>> pagesStorage = new ConcurrentHashMap<>();

    /**
     * Creates an instance of an application.
     * @param config an application config
     * @param state2path a function that dispatches a current state snapshot to the browser's navigation bar's path
     * @param lifeCycleEventsListener a listener for the app pages lifecycle events
     * @param routing a function that dispatches an incoming HTTP request to a page's initial state
     * @param rootComponent the root of the components tree
     */
    private App(final AppConfig config,
                final PageLifeCycle<S> lifeCycleEventsListener,
                final ComponentDefinition<S> rootComponent) {
        this.config = Objects.requireNonNull(config);
        this.lifeCycleEventsListener = Objects.requireNonNull(lifeCycleEventsListener);
        this.rootComponent = Objects.requireNonNull(rootComponent);
    }

    /**
     * Creates an instance of an application with the default configuration.
     * @param routing a function that dispatches an incoming HTTP request to a page's initial state
     * @param rootComponentView the root of the components tree
     */
    public App(final Routing<HttpRequest, S> routing,
               final ComponentView<S> rootComponentView) {
        this(AppConfig.DEFAULT,
             new PageLifeCycle.Default<>(),
             new ComponentDefinition<>(routing.toInitialStateFunction(), (s, p) -> p, rootComponentView));
    }


    /**
     * Creates an instance of an application with the default config
     * and default routing which maps any request to the initial state.
     * @param initialState the initial state snapshot
     * @param rootComponentView the root of the components tree
     */
    public App(final S initialState,
               final ComponentView<S> rootComponentView) {
        this(AppConfig.DEFAULT,
             new PageLifeCycle.Default<>(),
             new ComponentDefinition<S>(new Routing<HttpRequest, S>(request -> Optional.of(CompletableFuture.completedFuture(initialState))).toInitialStateFunction(),
                                       (__, p) ->  p,
                                       rootComponentView));
    }

    public App(final S initialState,
               final Function<S, TagDefinition> rootView) {
        this(AppConfig.DEFAULT,
             new PageLifeCycle.Default<>(),
             new ComponentDefinition<S>(new Routing<HttpRequest, S>(request -> Optional.of(CompletableFuture.completedFuture(initialState))).toInitialStateFunction(),
                                        (__, p) ->  p,
                                        state -> newState -> rootView.apply(state)));
    }

    /**
     * Sets the application's config.
     * @param config an application config
     * @return a new application object with the same field values except of the provided field
     */
/*    public App<S> config(final AppConfig config) {
        return new App<S>(config, this.state2path, this.lifeCycleEventsListener, this.routing, this.rootComponent);
    }*/

    /**
     * Sets the application's global state to the browser's navigation path function.
     * @param stateToPath a function that dispatches a current state snapshot to the browser's navigation bar's path
     * @return a new application object with the same field values except of the provided field
     */
/*    public App<S> stateToPath(final BiFunction<S, Path, Path> stateToPath) {
        return new App<S>(this.config, stateToPath, this.lifeCycleEventsListener, this.routing, this.rootComponent);
    }*/

    /**
     * Sets a listener for the app pages lifecycle events.
     * @see PageLifeCycle
     *
     * @param lifeCycleEventsListener the listener interface for receiving page lifecycle events.
     * @return a new application object with the same field values except of the provided field
     */
 /*   public App<S> pageLifeCycle(final PageLifeCycle<S> lifeCycleEventsListener) {
        return new App<S>(this.config, this.state2path, lifeCycleEventsListener, this.routing, this.rootComponent);
    }*/
}

