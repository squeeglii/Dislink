package me.squeeglii.plugin.dislink.data;


import me.squeeglii.plugin.dislink.util.Check;

import java.util.Objects;

public class Key<T> {

    private final String key;

    public Key(String key) {
        Check.nullParam(key, "Key String");
        String modifiedKey = key.trim().toLowerCase();

        if(modifiedKey.isEmpty())
            throw new IllegalArgumentException("Key cannot be made of only whitespace.");

        this.key = modifiedKey;
    }

    public String name() {
        return this.key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key<?>)) return false;
        Key<?> key1 = (Key<?>) o;
        return Objects.equals(key, key1.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public static <T> Key<T> of(String string) {
        return new Key<>(string);
    }
}