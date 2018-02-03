package ru.mail.polis.domain;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

public interface Repository {

    @NotNull byte[] get(@NotNull String key) throws NoSuchElementException, IllegalArgumentException, IOException;

    void delete(@NotNull String key) throws IllegalArgumentException, IOException;

    void upsert(@NotNull String key, @NotNull byte[] data) throws IllegalArgumentException, IOException;
}
