package com.routeanalyzer.api.common;

import java.util.function.Supplier;

public interface ThrowingSupplier<T, E extends Throwable> {

    T get() throws E;
    static <T, E extends Throwable> Supplier<T> unchecked(ThrowingSupplier<T, E> s) {
        return () -> {
            try {
                return s.get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
