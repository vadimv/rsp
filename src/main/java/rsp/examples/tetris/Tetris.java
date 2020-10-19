package rsp.examples.tetris;

import rsp.App;
import rsp.Component;
import rsp.jetty.JettyServer;
import rsp.server.StaticResources;
import rsp.state.UseState;

import java.io.File;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static rsp.dsl.Html.*;

public class Tetris {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        final AtomicReference<ScheduledFuture<?>> timer = new AtomicReference();
        final Component<State> render = useState ->
            html(on("keydown",  c -> {
                        final String keyCode = c.eventObject().apply("keyCode").orElse("noKeyCode");
                        System.out.println("keydown " + keyCode);
                        if ("37".equals(keyCode)) {
                            tryMoveLeft(useState);
                        } else if ("39".equals(keyCode)) {
                            tryMoveRight(useState);
                        } else if ("40".equals(keyCode)) {
                            tryMoveDown(useState);
                        } else if ("38".equals(keyCode)) {
                            tryRotate(useState);
                        }
                    }),
                head(link(attr("rel", "stylesheet"), attr("href","/res/style.css"))),
                body(
                    div(attr("class", "stage"),
                        of(Arrays.stream(useState.get().stage.cells()).flatMap(row ->
                                CharBuffer.wrap(row).chars().mapToObj(i -> (char)i)).map(cell ->
                                    div(attr("class", "cell t" + cell))))),
                    aside(button(attr("type", "button"),
                                 text("Start"),
                                 on("click", c -> {
                                       System.out.println("Start clicked");
                                       setTetramino(useState);
                                       timer.set(c.scheduleAtFixedRate(() -> {
                                           System.out.println("Schedule command");
                                           if (!tryMoveDown(useState)) {
                                               setTetramino(useState);
                                           }
                                       }, 0, 1, TimeUnit.SECONDS));
                                   })),
                         button(attr("type", "button"),
                            text("Stop"),
                            on("click", c -> {
                                final var t = timer.get();
                                if (t != null) {
                                    t.cancel(false);
                                }
                            })))));

        final var s = new JettyServer(DEFAULT_PORT,
                                "",
                                new App(State.initialState(),
                                        render),
                                Optional.of(new StaticResources(new File("src/main/java/rsp/examples/tetris"),
                                                "/res/*")));
        s.start();
        s.join();
    }

    private static void setTetramino(UseState<State> s) {
        final Tetromions.Tetromino t = Tetromions.randomTetromino();
        s.accept(s.get().addTetraminoToCells()
                        .collapseFullLayers()
                        .setTetramino(t, (Stage.WIDTH - t.shape.length) / 2, 0));
    }

    private static void tryMoveLeft(UseState<State> s) {
        if (!s.get().checkCollision(-1, 0, false)) {
            s.accept(s.get().moveTetraminoLeft());
        }
    }

    private static void tryMoveRight(UseState<State> s) {
        if (!s.get().checkCollision(1, 0, false)) {
            s.accept(s.get().moveTetraminoRight());
        }
    }

    private static boolean tryMoveDown(UseState<State> s) {
        if (!s.get().checkCollision(0, 1, false)) {
            s.accept(s.get().moveTetraminoDown());
            return true;
        } else {
            return false;
        }
    }

    private static void tryRotate(UseState<State> s) {
        if (!s.get().checkCollision(0, 0, true)) {
            s.accept(s.get().rotateTetramino());
        }
    }
}
