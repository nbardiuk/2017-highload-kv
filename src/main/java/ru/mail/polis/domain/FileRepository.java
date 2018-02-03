package ru.mail.polis.domain;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.NoSuchElementException;

public class FileRepository implements Repository {

    @NotNull private final File dir;

    public FileRepository(@NotNull File dir) {
        this.dir = dir;
    }

    @NotNull @Override
    public byte[] get(@NotNull String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        final File file = file(key);
        if (!file.exists()) {
            throw new NoSuchElementException();
        }
        try (final InputStream is = new FileInputStream(file)) {
            final byte[] data = new byte[(int) file.length()];
            if (is.read(data) < data.length) {
                throw new IOException("Can't read from file");
            }
            return data;
        }
    }

    @Override public void delete(@NotNull String key) throws IllegalArgumentException, IOException {
        file(key).delete();
    }


    @Override
    public void upsert(@NotNull String key, @NotNull byte[] data) throws IllegalArgumentException, IOException {
        try (final OutputStream os = new FileOutputStream(file(key), false)) {
            os.write(data);
        }
    }

    @NotNull private File file(@NotNull String key) {
        return new File(dir, key);
    }
}
