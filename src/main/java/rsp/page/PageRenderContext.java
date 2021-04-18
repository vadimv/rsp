package rsp.page;

import rsp.dom.Event;
import rsp.dom.VirtualDomPath;
import rsp.dom.XmlNs;
import rsp.ref.Ref;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface PageRenderContext {
    void setStatusCode(int statusCode);
    void setHeaders(Map<String, String> headers);
    void setDocType(String docType);
    void openNode(XmlNs xmlns, String name);
    void closeNode(String name, boolean upgrade);
    void setAttr(XmlNs xmlNs, String name, String value, boolean isProperty);
    void setStyle(String name, String value);
    void addTextNode(String text);
    <S> void addEvent(Optional<VirtualDomPath> elementPath,
                  String eventName,
                  Consumer<EventContext<S>> eventHandler,
                  boolean preventDefault,
                  Event.Modifier modifier);
    void addRef(Ref ref);
  /*  void openComponent(Function<Consumer<Object>, Consumer<Object>> componentSetState);*/
    void openComponent();
    void closeComponent();

    <S2, S1> void openComponent(Function<Consumer<S2>, Consumer<S1>> transformation);
}
