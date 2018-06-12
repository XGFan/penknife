package com.test4x.penknife.converter;

import java.nio.charset.StandardCharsets;

public class PlainConverter implements HttpMessageConverter {
    @Override
    public boolean accept(Object body) {
        return true;
    }

    @Override
    public byte[] convert(Object body) throws Exception {
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() {
        return "text/plain";
    }
}
