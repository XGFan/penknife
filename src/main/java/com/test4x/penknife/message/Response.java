package com.test4x.penknife.message;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class Response {

    private final DefaultFullHttpResponse delegate;

    private Object body = null;


    public Response() {
        delegate = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }


    public HttpResponseStatus status() {
        return delegate.status();
    }

    public Response status(HttpResponseStatus status) {
        delegate.setStatus(status);
        return this;
    }

    public String contentType() {
        return delegate.headers().getAsString(HttpHeaderNames.CONTENT_TYPE);
    }

    public Response contentType(@NonNull CharSequence contentType) {
        delegate.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return this;
    }


    public Map<String, String> headers() {
        Map<String, String> map = new HashMap<>(delegate.headers().size());
        delegate.headers().forEach(header -> map.put(header.getKey(), header.getValue()));
        return map;
    }

    @Deprecated
    HttpHeaders getHeaders() {
        return delegate.headers();
    }

    public Response header(CharSequence name, CharSequence value) {
        delegate.headers().set(name, value);
        return this;
    }

    public Response cookie(@NonNull Cookie cookie) {
        final String cookieStr = delegate.headers().get(HttpHeaderNames.SET_COOKIE);
        final Set<Cookie> cookieSet = new HashSet<>(ServerCookieDecoder.LAX.decode(cookieStr));
        cookieSet.add(cookie);
        delegate.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookieSet));
        return this;
    }

    public Response removeCookie(@NonNull String name) {
        final String cookieStr = delegate.headers().get(HttpHeaderNames.SET_COOKIE);
        final Set<Cookie> cookieSet = new HashSet<>(ServerCookieDecoder.LAX.decode(cookieStr));
        boolean exist = false;
        for (Cookie cookie : cookieSet) {
            if (cookie.name().equals(name)) {
                cookie.setValue("");
                cookie.setMaxAge(-1);
                exist = true;
            }
        }
        if (!exist) {
            Cookie nettyCookie = new DefaultCookie(name, "");
            nettyCookie.setMaxAge(-1);
            cookieSet.add(nettyCookie);
        }
        delegate.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookieSet));
        return this;
    }

    public Response bodyText(String text) {
        this.body = text;
        return this;
    }

    public Response body(Object object) {
        this.body = object;
        return this;
    }

    public Object body() {
        return body;
    }


    /**
     * 这儿复制出一个新的HttpResponse
     */
    public FullHttpResponse build(String contentType, byte[] bytes) {
        final FullHttpResponse duplicate = delegate.duplicate();
        duplicate.headers().set(HttpHeaderNames.SERVER, "PenKnife");
        duplicate.headers().set(HttpHeaderNames.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        if (contentType != null) {
            duplicate.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        }
        if (bytes != null) {
            duplicate.content().writeBytes(bytes);
        }
        duplicate.headers().set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(duplicate.content().readableBytes()));
        return duplicate;
    }

}
