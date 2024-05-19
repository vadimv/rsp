package rsp.page;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import rsp.component.*;
import rsp.server.Path;
import rsp.server.TestCollectingRemoteOut;
import rsp.server.http.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static rsp.html.HtmlDsl.*;
import static rsp.page.PageRendering.DOCUMENT_DOM_PATH;

@Disabled
class LivePageTests {

    static final QualifiedSessionId QID = new QualifiedSessionId("1", "1");

    static final ComponentView<State> view = state -> newState -> html(
            body(
                    span(state.toString())
            )
    );

    @Test
    void should_generate_html_listen_event_and_update_commands_for_new_state() {
        final TestCollectingRemoteOut remoteOut = new TestCollectingRemoteOut();
        final State initialState = new State(10);

        final URI uri = URI.create("http://localhost");
        final HttpRequest httpRequest = new HttpRequest(HttpRequest.HttpMethod.GET,
                                                        uri,
                                                        uri.toString(),
                                                        Path.ROOT);

        final PageConfigScript pageConfigScript = new PageConfigScript(QID.sessionId,
                                                                       "/",
                                                                       DefaultConnectionLostWidget.HTML,
                                                                       1000);
        final PageStateOrigin httpStateOrigin = new PageStateOrigin(httpRequest);
        final TemporaryBufferedPageCommands commandsBuffer = new TemporaryBufferedPageCommands();
        final Object sessionLock = new Object();
        final PageRenderContext domTreeContext = new PageRenderContext(new QualifiedSessionId("device0", "session0"),
                                                                       pageConfigScript.toString(),
                                                                       DOCUMENT_DOM_PATH,
                                                                       httpStateOrigin,
                                                                       commandsBuffer,
                                                                       sessionLock);

        final StatefulComponentDefinition<State> componentDefinition = ComponentDsl.pathComponent(p -> CompletableFuture.completedFuture(initialState),
                                                                                                  (s, p) -> p,
                                                                                                  view);
        componentDefinition.render(domTreeContext);
        assertFalse(domTreeContext.html().isBlank());

        final LivePageSession livePage = new LivePageSession(QID,
                                                             domTreeContext,
                                                             remoteOut,
                                                             sessionLock);
        livePage.init();
        livePage.shutdown();
    }

    static final class State {
        public final int value;

        private State(final int value) {
        this.value = value;
        }

        @Override
        public String toString() {
        return Integer.toString(value);
        }
    }
}