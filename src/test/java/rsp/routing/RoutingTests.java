package rsp.routing;

import org.junit.jupiter.api.Test;
import rsp.server.http.HttpRequest;
import rsp.server.Path;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rsp.routing.RoutingDsl.*;

class RoutingTests {

    @Test
    void should_correctly_route_simple_for_method() throws ExecutionException, InterruptedException {
        final Route<HttpRequest, String> r = concat(
                get("/*", req -> CompletableFuture.completedFuture("A")),
                post("/*", req -> CompletableFuture.completedFuture("B")),
                get("/*", req -> CompletableFuture.completedFuture("C")));
        final URI requestUri = URI.create("http://localhost");
        final var s = r.apply(new HttpRequest(HttpRequest.HttpMethod.POST,
                                                                  requestUri,
                                                                  requestUri.toString(),
                                                                  Path.of(requestUri.getPath())));
        assertTrue(s.isPresent());
        assertEquals("B", s.get().get());
    }

    @Test
    void should_correctly_route_simple_for_path() throws ExecutionException, InterruptedException {
        final Route<HttpRequest, String> r = concat(
                get("/A", req -> CompletableFuture.completedFuture("A")),
                get("/B", req -> CompletableFuture.completedFuture("B")),
                get("/C", req -> CompletableFuture.completedFuture("C")));
        final URI requestUri = URI.create("http://localhost/B");
        final var s = r.apply(new HttpRequest(HttpRequest.HttpMethod.GET,
                                                                                  requestUri,
                                                                                  requestUri.toString(),
                                                                                  Path.of(requestUri.getPath())));
        assertTrue(s.isPresent());
        assertEquals("B", s.get().get());
    }

    @Test
    void should_correctly_route_simple_for_sub_path() throws ExecutionException, InterruptedException {
        final Route<HttpRequest, String> r = concat(get(req -> paths()),
                                                    post("/B", req -> CompletableFuture.completedFuture("C")));
        final URI requestUri = URI.create("http://localhost/B");
        final var s = r.apply(new HttpRequest(HttpRequest.HttpMethod.GET,
                                                                                  requestUri,
                                                                                  requestUri.toString(),
                                                                                  Path.of(requestUri.getPath())));
        assertTrue(s.isPresent());
        assertEquals("A", s.get().get());

    }

    private static Route<Path, String> paths() {
        return concat(path("/:a", s -> CompletableFuture.completedFuture("A")),
                      path("/:a/:b", s -> CompletableFuture.completedFuture("B")));
    }
}
