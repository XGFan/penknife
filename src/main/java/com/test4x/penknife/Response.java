package com.test4x.penknife;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Response {
    private HttpHeaders headers = new DefaultHttpHeaders(false);
    private Set<Cookie> cookies = new HashSet<>(4);
    private HttpResponseStatus statusCode = HttpResponseStatus.OK;
    private CharSequence contentType = null;

    public HttpResponseStatus statusCode() {
        return this.statusCode;
    }

    public Response status(HttpResponseStatus status) {
        this.statusCode = status;
        return this;
    }

    public Response contentType(@NonNull CharSequence contentType) {
        this.contentType = contentType;
        return this;
    }


    public String contentType() {
        return null == this.contentType ? null : String.valueOf(this.contentType);
    }


    public Map<String, String> headers() {
        Map<String, String> map = new HashMap<>(this.headers.size());
        this.headers.forEach(header -> map.put(header.getKey(), header.getValue()));
        return map;
    }


    public Response header(CharSequence name, CharSequence value) {
        this.headers.set(name, value);
        return this;
    }


    public Response cookie(@NonNull Cookie cookie) {
        this.cookies.add(cookie);
        return this;
    }


    public Response removeCookie(@NonNull String name) {
        Optional<Cookie> cookieOpt = this.cookies.stream().filter(cookie -> cookie.name().equals(name)).findFirst();
        cookieOpt.ifPresent(cookie -> {
            cookie.setValue("");
            cookie.setMaxAge(-1);
        });
        Cookie nettyCookie = new io.netty.handler.codec.http.cookie.DefaultCookie(name, "");
        nettyCookie.setMaxAge(-1);
        this.cookies.add(nettyCookie);
        return this;
    }


    public Map<String, String> cookies() {
        Map<String, String> map = new HashMap<>(8);
        this.cookies.forEach(cookie -> map.put(cookie.name(), cookie.value()));
        return map;
    }


    public Response() {
    }

    public FullHttpResponse send() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, this.statusCode);
    }

}
