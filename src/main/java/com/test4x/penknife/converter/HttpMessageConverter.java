package com.test4x.penknife.converter;

public interface HttpMessageConverter {

    boolean accept(Object body);

    byte[] convert(Object body) throws Exception;

    String contentType();
}
