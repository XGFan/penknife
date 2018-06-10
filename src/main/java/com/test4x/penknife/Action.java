package com.test4x.penknife;

@FunctionalInterface
public interface Action {
    void invoke(Request req, Response res) throws InterruptedException;
}
