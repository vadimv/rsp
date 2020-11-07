package rsp.examples.crud.entities;

import java.util.HashSet;
import java.util.Set;

public class Author {
    public final Name name;
    public final Set<KeyedEntity<Long, Book>> books;

    public Author(Name name, Set<KeyedEntity<Long, Book>> books) {
        this.name = name;
        this.books = books;
    }

    public Author(Name name) {
        this(name, Set.of());
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public Author addBook(KeyedEntity<Long, Book> book) {
        final var b = new HashSet<>(books);
        b.add(book);
        return new Author(name, b);
    }
}