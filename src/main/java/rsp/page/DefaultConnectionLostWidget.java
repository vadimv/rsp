package rsp.page;

import rsp.component.LivePageContext;
import rsp.dom.DomTreeRenderContext;
import rsp.dom.VirtualDomPath;
import rsp.html.TagDefinition;
import rsp.html.HtmlDsl;

public final class DefaultConnectionLostWidget {

    public static final String HTML;

    static {
        final DomTreeRenderContext rc = new DomTreeRenderContext(VirtualDomPath.DOCUMENT, new LivePageContext());
        widget().render(rc);
        HTML = rc.toString();
    }

    private static TagDefinition widget() {
        return HtmlDsl.div( HtmlDsl.style("position", "fixed"),
                    HtmlDsl.style("top", "0"),
                    HtmlDsl.style("left", "0"),
                    HtmlDsl.style("right", "0"),
                    HtmlDsl.style("background-color", "lightyellow"),
                    HtmlDsl.style("border-bottom", "1px solid black"),
                    HtmlDsl.style("padding", "10px"),
                    HtmlDsl.text("Connection lost. Waiting to resume."));
    }
}
