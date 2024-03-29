package rsp.browserautomation;

import rsp.App;
import rsp.html.SegmentDefinition;
import rsp.routing.Routing;
import rsp.server.Path;
import rsp.server.StaticResources;
import rsp.component.ComponentView;
import rsp.jetty.JettyServer;
import rsp.server.http.HttpRequest;
import rsp.component.View;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static rsp.component.ComponentDsl.component;
import static rsp.component.ComponentDsl.statelessComponent;
import static rsp.html.HtmlDsl.*;
import static rsp.routing.RoutingDsl.*;

public class SimpleServer {

    public static final int PORT = 8085;
    public final JettyServer<AppState> jetty;


    private static Routing<HttpRequest, AppState> appRouting() {
        return new Routing<>(get("/:id(^\\d+$)/:id(^\\d+$)", __ -> CompletableFuture.completedFuture(new CountersState())),
                new NotFoundState() );
    }

    private static SegmentDefinition incrementCounterComponent1(final String name) {
        return component(routing1(),
                         (count, path) -> Path.of("/" + count + "/" + path.get(1)),
                         incrementCounterComponentView(name));
    }

    private static Routing<Path, Integer> routing1() {
        return routing(path("/:id(^\\d+$)/*", id -> CompletableFuture.completedFuture(Integer.parseInt(id))),
                      -1);
    }


    private static SegmentDefinition incrementCounterComponent2(final String name) {
        return component(routing2(),
                        (count, path) -> Path.of("/" + path.get(0) + "/" + count),
                        incrementCounterComponentView(name));
    }

    private static Routing<Path, Integer> routing2() {
        return routing(path("/*/:id(^\\d+$)", id -> CompletableFuture.completedFuture(Integer.parseInt(id))),
                       -1);
    }


    private static ComponentView<Integer> incrementCounterComponentView(String name) {
        return state -> newState ->
                div(div(button(attr("type", "button"),
                                attr("id", name + "_b0"),
                                text("+1"),
                                on("click",
                                        d -> newState.set(state + 1)))),
                        div(span(attr("id", name + "_s0"),
                                 attr("class", state % 2 == 0 ? "red" : "blue"),
                                 text(state))));
    }

    private static final ComponentView<CountersState> countersComponentView = state -> newState ->
            html(head(title("test-server-title"),
                            link(attr("rel", "stylesheet"),
                                 attr("href", "/res/style.css"))),
                    body(incrementCounterComponent1("c1"),
                         incrementCounterComponent2("c2")
                    ));

    private static final View<NotFoundState> notFoundStatelessView = __ ->
            html(head(HeadType.PLAIN, title("Not found")),
                 body(h1("Not found 404"))).statusCode(404);

    private static final View<AppState> appComponentView = state -> {
        if (state instanceof NotFoundState) {
            return statelessComponent((NotFoundState) state, notFoundStatelessView);
        } else if (state instanceof CountersState) {
            return component((CountersState) state, countersComponentView);
        } else {
            throw new IllegalStateException();
        }
    };


    public SimpleServer(final JettyServer<AppState> jetty) {
        this.jetty = jetty;
    }

    public static void main(final String[] args) {
        run(true);
    }

    public static SimpleServer run(final boolean blockCurrentThread) {
        final App<AppState> app = new App<>(appRouting(),
                                            appComponentView);
        final SimpleServer s = new SimpleServer(new JettyServer<>(8085,
                                                                  "",
                                                                   app,
                                                                   new StaticResources(new File("src/test/java/rsp/browserautomation"),
                                                                                       "/res/*")));
        s.jetty.start();
        if (blockCurrentThread) {
            s.jetty.join();
        }
        return s;
    }


    sealed interface AppState {
    }

    static final class NotFoundState implements AppState {
    }

    static final class CountersState implements AppState {
    }
}
