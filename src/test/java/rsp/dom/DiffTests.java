package rsp.dom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiffTests {
    final TreePositionPath basePath = new TreePositionPath(1);

    @Test
    void should_be_empty_diff_for_same_single_tags() {
        final Tag tree1 = new Tag(XmlNs.html, "html", false);
        final Tag tree2 = new Tag(XmlNs.html, "html", false);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("", cp.resultAsString());
    }

    @Test
    void should_remove_and_create_for_different_single_tags() {
        final Tag tree1 = new Tag(XmlNs.html, "html", false);
        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addAttribute("attr0", "value0", true);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath,  cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE::1 +TAG:1:div +ATTR:1:attr0=value0:true", cp.resultAsString());
    }

    @Test
    void should_create_tags_for_added_children() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addChild(new Tag(XmlNs.html, "span", false));
        tree2.addChild(new Tag(XmlNs.html, "span", false));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+TAG:1_1:span +TAG:1_2:span", cp.resultAsString());
    }

    @Test
    void should_remove_and_add_for_replaced_tag() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addChild(new Tag(XmlNs.html, "span", false));

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addChild(new Tag(XmlNs.html, "a", false));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE:1:1_1 +TAG:1_1:a", cp.resultAsString());
    }

    @Test
    void should_replace_text_with_text() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addChild(new Text("abc"));

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addChild(new Text("123"));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+TEXT:1:1_1=123", cp.resultAsString());
    }


    @Test
    void should_replace_tag_with_text() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addChild(new Tag(XmlNs.html, "span", false));

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addChild(new Text("abc"));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE:1:1_1+TEXT:1:1_1=abc", cp.resultAsString());
    }

    @Test
    void should_replace_text_with_tag() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addChild(new Text("abc"));

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addChild(new Tag(XmlNs.html, "span", false));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE:1:1_1 +TAG:1_1:span", cp.resultAsString());
    }

    @Test
    void should_remove_text_node() {
        final Tag tree1 = new Tag(XmlNs.html, "p", false);
        tree1.addChild(new Text("abc"));
        tree1.addChild(new Tag(XmlNs.html, "br", true));
        tree1.addChild(new Text("xyz"));

        final Tag tree2 = new Tag(XmlNs.html, "p", false);
        tree2.addChild(new Text("abc"));
        tree2.addChild(new Tag(XmlNs.html, "br", true));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE:1:1_3", cp.resultAsString());
    }

    @Test
    void should_add_text_node() {
        final Tag tree1 = new Tag(XmlNs.html, "p", false);
        tree1.addChild(new Text("abc"));
        tree1.addChild(new Tag(XmlNs.html, "br", true));

        final Tag tree2 = new Tag(XmlNs.html, "p", false);
        tree2.addChild(new Text("abc"));
        tree2.addChild(new Tag(XmlNs.html, "br", true));
        tree2.addChild(new Text("xyz"));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+TEXT:1:1_3=xyz", cp.resultAsString());
    }

    @Test
    void should_modify_text_node() {
        final Tag tree1 = new Tag(XmlNs.html, "p", false);
        tree1.addChild(new Text("abc"));
        tree1.addChild(new Tag(XmlNs.html, "br", true));
        tree1.addChild(new Text("xyz"));

        final Tag tree2 = new Tag(XmlNs.html, "p", false);
        tree2.addChild(new Text("abc"));
        tree2.addChild(new Tag(XmlNs.html, "br", true));
        tree2.addChild(new Text("klm"));

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+TEXT:1:1_3=klm", cp.resultAsString());
    }

    @Test
    void should_remove_and_add_for_replaced_tag_with_children() {
        final Tag tree1 = new Tag(XmlNs.html, "body", false);

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        final Tag child21 = new Tag(XmlNs.html, "a", false);
        child21.addChild(new Tag(XmlNs.html, "canvas", false));
        child21.addChild(new Tag(XmlNs.html, "span", false));
        tree2.addChild(child21);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-NODE::1 +TAG:1:div +TAG:1_1:a +TAG:1_1_1:canvas +TAG:1_1_2:span", cp.resultAsString());
    }

    @Test
    void should_add_for_a_new_child() {
        final Tag ul1 = new Tag(XmlNs.html, "ul", false);
        final Tag li11 = new Tag(XmlNs.html, "li", false);
        li11.addChild(new Text("first"));
        ul1.addChild(li11);
        final Tag li12 = new Tag(XmlNs.html, "li", false);
        li12.addChild(new Text("second"));
        ul1.addChild(li12);

        final Tag ul2 = new Tag(XmlNs.html, "ul", false);
        final Tag li21 = new Tag(XmlNs.html, "li", false);
        li21.addChild(new Text("first"));
        ul2.addChild(li21);
        final Tag li22 = new Tag(XmlNs.html, "li", false);
        li22.addChild(new Text("second"));
        ul2.addChild(li22);
        final Tag li23 = new Tag(XmlNs.html, "li", false);
        li23.addChild(new Text("third"));
        ul2.addChild(li23);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(ul1, ul2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+TAG:1_3:li+TEXT:1_3:1_3_1=third", cp.resultAsString());
    }


    @Test
    void should_add_attribute() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addAttribute("attr1", "value1", true);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+ATTR:1:attr1=value1:true", cp.resultAsString());
    }

    @Test
    void should_remove_attribute() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addAttribute("attr1", "value1", true);

        final Tag tree2 = new Tag(XmlNs.html, "div", false);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-ATTR:1:attr1", cp.resultAsString());
    }

    @Test
    void should_add_style() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);

        final Tag tree2 = new Tag(XmlNs.html, "div", false);
        tree2.addStyle("style1", "value1");

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("+STYLE:1:style1=value1", cp.resultAsString());
    }

    @Test
    void should_remove_style() {
        final Tag tree1 = new Tag(XmlNs.html, "div", false);
        tree1.addStyle("style1", "value1");

        final Tag tree2 = new Tag(XmlNs.html, "div", false);

        final TestChangesContext cp = new TestChangesContext();
        Diff.diff(tree1, tree2, basePath, cp, new HtmlBuilder(new StringBuilder()));
        assertEquals("-STYLE:1:style1", cp.resultAsString());
    }

    static class TestChangesContext implements DomChangesContext {
        final StringBuilder sb = new StringBuilder();

        public String resultAsString() {
            return sb.toString().trim();
        }

        @Override
        public void removeNode(final TreePositionPath parentId, final TreePositionPath id) {
            insertDelimiter(sb);
            sb.append("-NODE:" + parentId + ":" + id);
        }

        @Override
        public void createTag(final TreePositionPath id, final XmlNs xmlNs, final String tag) {
            insertDelimiter(sb);
            sb.append("+TAG:" + id + ":" + tag);
        }

        @Override
        public void removeAttr(final TreePositionPath id, final XmlNs xmlNs, final String name, final boolean isProperty) {
            insertDelimiter(sb);
            sb.append("-ATTR:" + id + ":" + name);
        }

        @Override
        public void setAttr(final TreePositionPath id, final XmlNs xmlNs, final String name, final String value, final boolean isProperty) {
            insertDelimiter(sb);
            sb.append("+ATTR:" + id + ":" + name + "=" + value + ":" + isProperty);
        }

        @Override
        public void removeStyle(final TreePositionPath id, final String name) {
            insertDelimiter(sb);
            sb.append("-STYLE:" + id + ":" + name);
        }

        @Override
        public void setStyle(final TreePositionPath id, final String name, final String value) {
            sb.append("+STYLE:" + id + ":" + name + "=" + value);
            insertDelimiter(sb);
        }

        @Override
        public void createText(final TreePositionPath parenPath, final TreePositionPath path, final String text) {
            sb.append("+TEXT:" + parenPath + ":" + path + "=" + text);
            insertDelimiter(sb);
        }

        private void insertDelimiter(final StringBuilder sb) {
            if (sb.length() != 0) sb.append(" ");
        }
    }
}
