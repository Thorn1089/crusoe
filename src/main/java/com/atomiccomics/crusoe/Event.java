package com.atomiccomics.crusoe;

public record Event<T>(Name name, T payload) {

    public static <T> Event<T> create(T payload) {
        return new Event<>(new Name(payload.getClass().getSimpleName()), payload);
    }

    public record Name(String value) {

    }
}
