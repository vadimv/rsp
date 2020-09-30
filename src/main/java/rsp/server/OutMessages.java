package rsp.server;

import rsp.Event;
import rsp.dom.Path;
import rsp.dom.RemoteDomChangesPerformer;

import java.util.List;

public interface OutMessages {
    void setRenderNum(int renderNum);
    void listenEvent( String eventType, boolean preventDefault, Path path, Event.Modifier modifier);
    void extractProperty(int descriptor, Path path, String name);
    void modifyDom(List<RemoteDomChangesPerformer.DomChange> domChange);
    void evalJs(int descriptor, String js);
}
