package rsp.page;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import rsp.stateview.ComponentView;
import rsp.component.LivePageContext;
import rsp.dom.*;
import rsp.server.Out;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LivePageStateTests {

    private static final QualifiedSessionId QID = new QualifiedSessionId("1", "1");

    @Test
    public void should_generate_update_commands_for_same_state() {
/*        final TestCollectingOutMessages out = new TestCollectingOutMessages();
        final LivePageState<State> livePageState = create(new State(0), out);
        livePageState.run();//(new State(0));
        Assert.assertEquals(List.of(), out.commands);*/
    }

    @Test
    public void should_generate_update_commands_for_new_state() {
/*        final TestCollectingOutMessages out = new TestCollectingOutMessages();
        final LivePageState<State> livePageState = create(new State(0), out);
        livePageState.run();//.accept(new State(100));
        Assert.assertEquals(List.of(new ModifyDomOutMessage(List.of(new DefaultDomChangesContext.CreateText(VirtualDomPath.of("1"),
                                                                                                      VirtualDomPath.of("1_1"),
                                                                                                      "100"))),
                                    new PushHistoryMessage("/100")),
                            out.commands);*/
    }

/*    private LivePageState<State> create(final State state, final OutMessages out) {
        final CreateViewFunction<State> rootCreateViewFunction = (sv, sc) -> span(sv.toString());

        final LivePageSnapshot<State> lpps = new LivePageSnapshot<>(null,
                                                                    null,
                                                                    Path.of("/" + state),
                                                                    domRoot(rootCreateViewFunction, state),
                                                                    Collections.emptyMap(),
                                                                    Collections.emptyMap());

        final StateToRouteDispatch<State> state2route = new StateToRouteDispatch<>(Path.of(""), (s, p) -> Path.of("/" + s.value));

        return new LivePageState<>(QID, lpps, state2route, enrichFunction(), out);
    }*/

    private static Tag domRoot(final ComponentView<State> component, final State state) {
        final DomTreeRenderContext domTreeContext = new DomTreeRenderContext(VirtualDomPath.DOCUMENT, new LivePageContext());
        component.apply(state).apply(s -> {}).render(enrichFunction().apply(QID.sessionId, domTreeContext));

        return domTreeContext.rootTag();
    }

    private static BiFunction<String, RenderContext, RenderContext> enrichFunction() {
        return (sessionId, ctx) -> ctx;
    }

    @Test
    public void should_comply_to_equals_hash_contract_for_helper_classes() {
        EqualsVerifier.forClass(SetRenderNumOutMessage.class).verify();
        EqualsVerifier.forClass(ListenEventOutMessage.class).verify();
        EqualsVerifier.forClass(ForgetEventOutMessage.class).verify();
        EqualsVerifier.forClass(ExtractPropertyOutMessage.class).verify();
        EqualsVerifier.forClass(ModifyDomOutMessage.class).verify();
        EqualsVerifier.forClass(PushHistoryMessage.class).verify();
    }

    private static class State {
        public final int value;

        private State(final int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    private static class TestCollectingOut implements Out {
        public final List<Message> commands = new ArrayList<>();

        @Override
        public void setRenderNum(final int renderNum) {
            commands.add(new SetRenderNumOutMessage(renderNum));
        }

        @Override
        public void listenEvents(final List<Event> events) {
                commands.addAll(events.stream().map(e -> new ListenEventOutMessage(e.eventTarget.eventType,
                                                                                   e.preventDefault,
                                                                                   e.eventTarget.elementPath,
                                                                                   e.modifier)).collect(Collectors.toList()));
        }

        @Override
        public void forgetEvent(final String eventType, final VirtualDomPath elementPath) {
            commands.add(new ForgetEventOutMessage(eventType, elementPath));

        }

        @Override
        public void extractProperty(final int descriptor, final VirtualDomPath path, final String name) {
            commands.add(new ExtractPropertyOutMessage(descriptor, path, name));
        }

        @Override
        public void modifyDom(final List<DefaultDomChangesContext.DomChange> domChange) {
            commands.add(new ModifyDomOutMessage(domChange));
        }

        @Override
        public void setHref(final String path) {
            throw new IllegalStateException();
        }

        @Override
        public void pushHistory(final String path) {
            commands.add(new PushHistoryMessage(path));
        }

        @Override
        public void evalJs(final int descriptor, final String js) {
            throw new IllegalStateException();
        }
    }

    final static class SetRenderNumOutMessage implements Message {
        public final int renderNum;
        SetRenderNumOutMessage(final int renderNum) {
            this.renderNum = renderNum;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final SetRenderNumOutMessage that = (SetRenderNumOutMessage) o;
            return renderNum == that.renderNum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(renderNum);
        }
    }

    interface Message {}

    final static class ListenEventOutMessage implements Message {
        public final String eventType;
        public final boolean preventDefault;
        public final VirtualDomPath path;
        public final Event.Modifier modifier;

        public ListenEventOutMessage(final String eventType, final boolean preventDefault, final VirtualDomPath path, final Event.Modifier modifier) {
            this.eventType = eventType;
            this.preventDefault = preventDefault;
            this.path = path;
            this.modifier = modifier;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ListenEventOutMessage that = (ListenEventOutMessage) o;
            return preventDefault == that.preventDefault &&
                    Objects.equals(eventType, that.eventType) &&
                    Objects.equals(path, that.path) &&
                    Objects.equals(modifier, that.modifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, preventDefault, path, modifier);
        }
    }

    static final class ForgetEventOutMessage implements Message {
        public final String eventType;
        public final VirtualDomPath elementPath;

        public ForgetEventOutMessage(final String eventType, final VirtualDomPath elementPath) {
            this.eventType = eventType;
            this.elementPath = elementPath;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ForgetEventOutMessage that = (ForgetEventOutMessage) o;
            return Objects.equals(eventType, that.eventType) &&
                    Objects.equals(elementPath, that.elementPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, elementPath);
        }
    }

    static final class ExtractPropertyOutMessage implements Message {
        public final int descriptor;
        public final VirtualDomPath path;
        public final String name;

        public ExtractPropertyOutMessage(final int descriptor, final VirtualDomPath path, final String name) {
            this.descriptor = descriptor;
            this.path = path;
            this.name = name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ExtractPropertyOutMessage that = (ExtractPropertyOutMessage) o;
            return descriptor == that.descriptor &&
                    Objects.equals(path, that.path) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(descriptor, path, name);
        }
    }

    static final class ModifyDomOutMessage implements Message {
        public final List<DefaultDomChangesContext.DomChange> domChange;

        ModifyDomOutMessage(final List<DefaultDomChangesContext.DomChange> domChange) {
            this.domChange = domChange;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final ModifyDomOutMessage that = (ModifyDomOutMessage) o;
            return Objects.equals(domChange, that.domChange);
        }

        @Override
        public int hashCode() {
            return Objects.hash(domChange);
        }

        @Override
        public String toString() {
            return "ModifyDomOutMessage{" +
                    "domChange=" + domChange +
                    '}';
        }
    }

    static final class PushHistoryMessage implements Message {
        public final String path;

        public PushHistoryMessage(final String path) {
            this.path = path;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PushHistoryMessage that = (PushHistoryMessage) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }

        @Override
        public String toString() {
            return "PushHistoryMessage{" +
                    "path='" + path + '\'' +
                    '}';
        }
    }
}
