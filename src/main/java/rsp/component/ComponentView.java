package rsp.component;

import rsp.html.SegmentDefinition;

import java.util.function.Function;

/**
 * A function to create a stateful component's view representation.
 * @param <S> the type of the document part's related state, should be an immutable class
 */
@FunctionalInterface
public interface ComponentView<S> extends Function<S, Function<StateUpdate<S>, SegmentDefinition>> {}
