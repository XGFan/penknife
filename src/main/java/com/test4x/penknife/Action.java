package com.test4x.penknife;

@FunctionalInterface
public interface Action {
    void invoke(Request request, Response response);
}
