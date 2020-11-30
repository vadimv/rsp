package rsp.javax.web;

import rsp.Component;
import rsp.QualifiedSessionId;
import rsp.RenderContext;
import rsp.server.DeserializeKorolevInMessage;
import rsp.server.HttpRequest;
import rsp.server.OutMessages;
import rsp.server.SerializeKorolevOutMessages;
import rsp.services.LivePage;
import rsp.services.PageRendering;
import rsp.util.Log;

import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MainWebSocketEndpoint<S> extends Endpoint {
    public static final String HANDSHAKE_REQUEST_PROPERTY_NAME = "handshakereq";
    private static final String LIVE_PAGE_SESSION_USER_PROPERTY_NAME = "livePage";

    private final Component<S> documentDefinition;
    private final Function<HttpRequest, CompletableFuture<S>> routing;
    private final BiFunction<String, S, String> state2route;
    private final Map<QualifiedSessionId, PageRendering.RenderedPage<S>> renderedPages;
    private final BiFunction<String, RenderContext, RenderContext> enrich;
    private final Supplier<ScheduledExecutorService> schedulerSupplier;
    private final Log.Reporting log;

    public MainWebSocketEndpoint(Function<HttpRequest, CompletableFuture<S>> routing,
                                 BiFunction<String, S, String> state2route,
                                 Map<QualifiedSessionId, PageRendering.RenderedPage<S>> renderedPages,
                                 Component<S> documentDefinition,
                                 BiFunction<String, RenderContext, RenderContext> enrich,
                                 Supplier<ScheduledExecutorService> schedulerSupplier,
                                 Log.Reporting log) {
        this.routing = routing;
        this.state2route = state2route;
        this.renderedPages = renderedPages;
        this.documentDefinition = documentDefinition;
        this.enrich = enrich;
        this.schedulerSupplier = schedulerSupplier;
        this.log = log;
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        log.trace(l -> l.log("Websocket endpoint opened, session: " + session.getId()));
        final OutMessages out = new SerializeKorolevOutMessages((msg) -> sendText(session, msg));
        final HttpRequest handshakeRequest = (HttpRequest) endpointConfig.getUserProperties().get(HANDSHAKE_REQUEST_PROPERTY_NAME);
        LivePage<S> livePage = LivePage.of(handshakeRequest,
                                           new QualifiedSessionId(session.getPathParameters().get("pid"),
                                                                  session.getPathParameters().get("sid")),
                                           routing,
                                           state2route,
                                           renderedPages,
                                           documentDefinition,
                                           enrich,
                                           schedulerSupplier.get(),
                                           out,
                                           log);
        final DeserializeKorolevInMessage in = new DeserializeKorolevInMessage(livePage, log);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                log.trace(l -> l.log("-> " + s));
                in.parse(s);
            }
        });
        livePage.start();
        session.getUserProperties().put(LIVE_PAGE_SESSION_USER_PROPERTY_NAME, livePage);
    }

    private void sendText(Session session, String text) {
        try {
            log.trace(l -> l.log("<- " + text));
            session.getBasicRemote().sendText(text);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        shutdown(session);
        log.info(l -> l.log("WebSocket closed " + closeReason.getReasonPhrase()));
    }

    @Override
    public void onError(Session session, Throwable thr) {
        shutdown(session);
        log.error(l -> l.log("WebSocket error: " + thr.getLocalizedMessage(), thr));
    }

    private void shutdown(Session session) {
        final LivePage<S> livePage = (LivePage<S>) session.getUserProperties().get(LIVE_PAGE_SESSION_USER_PROPERTY_NAME);
        if (livePage != null) {
            livePage.shutdown();
        }
    }

    public static HttpRequest of(HandshakeRequest handshakeRequest) {
        return HttpRequest.of(handshakeRequest);
    }
}
