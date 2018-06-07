package ru.kazantsev.template.util;

public interface Delegate<T> {
    void call(T self);
}
