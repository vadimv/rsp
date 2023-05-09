package rsp.html;

import rsp.ref.ElementRef;
import rsp.page.PageRenderContext;

import java.util.Objects;

/**
 * A reference to an element.
 */
public final class ElementRefDefinition implements DocumentPartDefinition, ElementRef {

    @Override
    public void render(PageRenderContext renderContext) {
        renderContext.addRef(this);
    }

    public <K> KeyRef<K> withKey(K key) {
        return new KeyRef<K>(this, key);
    }

    public static class KeyRef<K> implements ElementRef, DocumentPartDefinition {
        private final ElementRefDefinition parentRef;
        private final K key;
        public KeyRef(ElementRefDefinition parentRef, K key) {
            super();
            this.parentRef = Objects.requireNonNull(parentRef);
            this.key = Objects.requireNonNull(key);
        }

        @Override
        public void render(PageRenderContext renderContext) {
            renderContext.addRef(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyRef<?> keyRef = (KeyRef<?>) o;
            return parentRef.equals(keyRef.parentRef) && key.equals(keyRef.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentRef, key);
        }
    }
}
