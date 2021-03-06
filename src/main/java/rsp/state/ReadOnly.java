package rsp.state;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A read-only state container.
 * An attempt to invoke write operations result in an exception.
 * @param <S> the type of the state snapshot, an immutable class
 */
public final class ReadOnly<S> implements UseState<S> {

    private final S state;

    /**
     * Creates a new instance of the read-only container.
     * @param state an initial state snapshot
     */
    public ReadOnly(S state) {
        this.state = state;
    }

    @Override
    public S get() {
        return state;
    }

    @Override
    public void accept(S state) {
        throw new IllegalStateException("Set state is not allowed");
    }

    @Override
    public void accept(CompletableFuture<S> completableFuture) {
        throw new IllegalStateException("Set state is not allowed");
    }

    @Override
    public void accept(Function<S, S> function) {
        throw new IllegalStateException("Set state is not allowed");
    }

    @Override
    public void acceptOptional(Function<S, Optional<S>> function) {
        throw new IllegalStateException("Set state is not allowed");
    }
}
