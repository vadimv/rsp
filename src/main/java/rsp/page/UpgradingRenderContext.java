package rsp.page;

import rsp.component.Component;
import rsp.dom.Event;
import rsp.dom.Tag;
import rsp.dom.VirtualDomPath;
import rsp.dom.XmlNs;
import rsp.ref.Ref;
import rsp.stateview.ComponentView;
import rsp.stateview.NewState;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class UpgradingRenderContext implements RenderContext {

    private final RenderContext renderContext;
    private final String pageInfo;

    private boolean headWasOpened = false;

    private UpgradingRenderContext(final RenderContext renderContext, final String pageInfo) {
        this.renderContext = renderContext;
        this.pageInfo = pageInfo;
    }

    public static UpgradingRenderContext create(final RenderContext context,
                                                final String sessionId,
                                                final String path,
                                                final String connectionLostWidgetHtml,
                                                final int heartBeatInterval) {
        final String cfg = "window['kfg']={"
                + "sid:'" + sessionId + "',"
                + "r:'" + path + "',"
                + "clw:'" + connectionLostWidgetHtml + "',"
                + "heartbeatInterval:" + heartBeatInterval
                + "}";
        return new UpgradingRenderContext(context, cfg);
    }

    @Override
    public void setStatusCode(final int statusCode) {
        renderContext.setStatusCode(statusCode);
    }

    @Override
    public void setHeaders(final Map<String, String> headers) {
        renderContext.setHeaders(headers);
    }

    @Override
    public void setDocType(final String docType) {
        renderContext.setDocType(docType);
    }

    @Override
    public void openNode(final XmlNs xmlNs, final String name) {
        if (!headWasOpened && xmlNs.equals(XmlNs.html) && name.equals("body")) {
            // No <head> have opened above
            // it means a programmer didn't include head() in the page
            renderContext.openNode(XmlNs.html, "head");
            upgradeHeadTag();
            renderContext.closeNode("head", false);
        } else if (xmlNs.equals(XmlNs.html) && name.equals("head")) {
            headWasOpened = true;
        }
        renderContext.openNode(xmlNs, name);
    }

    @Override
    public void closeNode(final String name, final boolean upgrade) {
        if (headWasOpened && upgrade && name.equals("head")) {
            upgradeHeadTag();
        }
        renderContext.closeNode(name, upgrade);
    }

    private void upgradeHeadTag() {
        renderContext.openNode(XmlNs.html, "script");
        renderContext.addTextNode(pageInfo);
        renderContext.closeNode("script", false);

        renderContext.openNode(XmlNs.html, "script");
        renderContext.setAttr(XmlNs.html, "src", "/static/rsp-client.min.js", false);
        renderContext.setAttr(XmlNs.html, "defer", "defer", true);
        renderContext.closeNode("script", true);
    }

    @Override
    public void setAttr(final XmlNs xmlNs, final String name, final String value, final boolean isProperty) {
        renderContext.setAttr(xmlNs, name, value, isProperty);
    }

    @Override
    public void setStyle(final String name, final String value) {
        renderContext.setStyle(name, value);
    }

    @Override
    public void addTextNode(final String text) {
        renderContext.addTextNode(text);
    }

    @Override
    public void addEvent(final Optional<VirtualDomPath> elementPath,
                         final String eventName,
                         final Consumer<EventContext> eventHandler,
                         final boolean preventDefault,
                         final Event.Modifier modifier) {
       renderContext.addEvent(elementPath, eventName, eventHandler, preventDefault, modifier);
    }

    @Override
    public void addRef(final Ref ref) {
        renderContext.addRef(ref);
    }

    @Override
    public <S> NewState<S> openComponent(final S initialState, final ComponentView<S> view) {
        return renderContext.openComponent(initialState, view);
    }

    @Override
    public <S> void openComponent(Component<S> component) {
        renderContext.openComponent(component);
    }

    @Override
    public void closeComponent() {
        renderContext.closeComponent();
    }

    @Override
    public Tag rootTag() {
        return renderContext.rootTag();
    }

    @Override
    public <S> Component<S> rootComponent() {
        return renderContext.rootComponent();
    }

    @Override
    public Tag parentTag() {
        return renderContext.parentTag();
    }

    @Override
    public Tag currentTag() {
        return renderContext.currentTag();
    }

    @Override
    public RenderContext newSharedContext(VirtualDomPath path) {
        return new UpgradingRenderContext(renderContext.newSharedContext(path), pageInfo);
    }

    @Override
    public VirtualDomPath rootPath() {
        return renderContext.rootPath();
    }

    @Override
    public LivePage livePage() {
        return renderContext.livePage();
    }

    @Override
    public String toString() {
        return renderContext.toString();
    }
}
