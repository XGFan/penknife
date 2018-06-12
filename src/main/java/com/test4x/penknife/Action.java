package com.test4x.penknife;

import com.test4x.penknife.message.Request;
import com.test4x.penknife.message.Response;

@FunctionalInterface
public interface Action {
    void invoke(Request req, Response res) throws InterruptedException;
}
