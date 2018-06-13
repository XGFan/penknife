package com.test4x.penknife.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonConverter implements HttpMessageConverter {

    private ObjectMapper mapper;


    public JsonConverter() {
        this(new ObjectMapper());
    }

    public JsonConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean accept(Object body) {
        //不支持基本类型和包装类型
        final Class<?> clazz = body.getClass();
        return !(body instanceof CharSequence ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Float.class) ||
                clazz.isPrimitive());
    }

    @Override
    public byte[] convert(Object body) throws IOException {
        return mapper.writeValueAsBytes(body);
    }

    @Override
    public String contentType() {
        return "application/json";
    }
}
