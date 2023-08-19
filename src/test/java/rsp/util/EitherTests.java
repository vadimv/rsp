package rsp.util;

import org.junit.jupiter.api.Test;
import rsp.util.data.Either;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class EitherTests {

    @Test
    public void should_create_right_either() {
        final Either<Integer, String> e = Either.right("foo");

        final AtomicReference<String> c = new AtomicReference<>();
        e.on(l -> fail(),  r -> c.set(r));
        assertEquals("foo", c.get());
    }

    @Test
    public void should_create_left_either() {
        final Either<Integer, String> e = Either.left(10);

        final AtomicReference<Integer> c = new AtomicReference<>();
        e.on(l -> c.set(10), r -> fail());
        assertEquals(Integer.valueOf(10), c.get());
    }

    @Test
    public void should_map_correctly() {
        final Either<Integer, String> e = Either.left(10);
        e.map(v -> v + 1, v -> v).on(l -> assertEquals(Integer.valueOf(11), l), r -> fail());
    }

    @Test
    public void should_flatmap_correctly() {
        final Either<Integer, String> e = Either.left(10);
        e.flatMap(v -> Either.right(v + 1), v -> Either.left(v))
         .on(l -> fail(), r -> assertEquals(Integer.valueOf(11), r));
    }
}
