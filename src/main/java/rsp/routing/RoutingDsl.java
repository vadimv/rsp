package rsp.routing;

import rsp.server.HttpRequest;
import rsp.util.TriFunction;
import rsp.util.data.Tuple2;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides HTTP request routing DSL functions.
 * DSL functions define routes for an incoming request on the base of an HTTP method and an URL's path.
 * The framework tries to match the HTTP request's method and path match and in case of success calls the matching function
 * to obtain a CompletableFuture with a global state object.
 *
 * @see Routing
 */
public class RoutingDsl {

    public static <S> RouteDefinition<S> get(String pathPattern, Function<HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.GET.equals(req.method) && pp.match(req.path),
                                     new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(req)));
    }

    public static <S> RouteDefinition<S> get(String pathPattern, BiFunction<String, HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.GET.equals(req.method) && pp.match(req.path),
                                      new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(p1, req)));
    }

    public static <S> RouteDefinition<S> get(String pathPattern, TriFunction<String, String, HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.GET.equals(req.method) && pp.match(req.path),
                                    new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(p1, p2, req)));
    }


    public static <S> RouteDefinition<S> post(String pathPattern, Function<HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.POST.equals(req.method) && pp.match(req.path),
                new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(req)));
    }

    public static <S> RouteDefinition<S> post(String pathPattern, BiFunction<String, HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.POST.equals(req.method) && pp.match(req.path),
                new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(p1, req)));
    }

    public static <S> RouteDefinition<S> post(String pathPattern, TriFunction<String, String, HttpRequest, CompletableFuture<S>> matchFun) {
        final PathPattern pp = PathPattern.of(pathPattern);
        return new RouteDefinition<>(req -> HttpRequest.HttpMethod.POST.equals(req.method) && pp.match(req.path),
                new Tuple2<>(pp, (p1, p2, req) -> matchFun.apply(p1, p2, req)));
    }

/*    public static <S> RouteDefinition<S> post(String pathPattern, Function<HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.POST, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> post(String pathPattern, BiFunction<String, HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.POST, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> post(String pathPattern, TriFunction<String, String, HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.POST, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> put(String pathPattern, Function<HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.PUT, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> put(String pathPattern, BiFunction<String, HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.PUT, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> put(String pathPattern, TriFunction<String, String, HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(HttpRequest.HttpMethod.PUT, pathPattern, matchFun);
    }

    public static <S> RouteDefinition<S> route(HttpRequest.HttpMethod method, String pathPattern, Function<HttpRequest, CompletableFuture<S>> matchFun) {
        return new RouteDefinition<>(method, pathPattern, matchFun);
    }*/
}