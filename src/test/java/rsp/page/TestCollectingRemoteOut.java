package rsp.page;

import rsp.dom.DefaultDomChangesContext;
import rsp.dom.Event;
import rsp.dom.VirtualDomPath;
import rsp.server.RemoteOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestCollectingRemoteOut implements RemoteOut {
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

        @Override
        public String toString() {
            return "ListenEventOutMessage{" +
                    "eventType='" + eventType + '\'' +
                    ", preventDefault=" + preventDefault +
                    ", path=" + path +
                    ", modifier=" + modifier +
                    '}';
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
