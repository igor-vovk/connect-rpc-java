package me.ivovk.connect_rpc_java.core;

public interface Configurer<T> {
    static <T> Configurer<T> noop() {
        return t -> t;
    }

    T configure(T something);
}
