package rsp.html;

import rsp.component.ComponentRenderContext;

import java.util.Arrays;

/**
 * A sequence of definitions.
 */
public final class SequenceDefinition implements SegmentDefinition {
    public final SegmentDefinition[] items;

    /**
     * Creates a definition of a sequence of nodes definitions.
     * @param items to be rendered one ofter another
     */
    public SequenceDefinition(final SegmentDefinition[] items) {
        super();
        this.items = items;
    }

    @Override
    public boolean render(final ComponentRenderContext renderContext) {
        Arrays.stream(items).forEach(c -> c.render(renderContext));
        return true;
    }
}
