package com.test4x.penknife;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

@Slf4j
public class Response {
    private HttpHeaders headers = new DefaultHttpHeaders(false);
    private Set<Cookie> cookies = new HashSet<>(4);
    private int statusCode = 200;
    private ChannelHandlerContext ctx = null;
    private CharSequence contentType = null;
    private CharSequence dateString = null;

    private volatile boolean isCommit = false;


    public int statusCode() {
        return this.statusCode;
    }

    public Response status(int status) {
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


    public Response cookie(String name, String value) {
        this.cookies.add(new io.netty.handler.codec.http.cookie.DefaultCookie(name, value));
        return this;
    }


    public Response cookie(@NonNull String name, @NonNull String value, int maxAge) {
        Cookie nettyCookie = new io.netty.handler.codec.http.cookie.DefaultCookie(name, value);
        nettyCookie.setPath("/");
        nettyCookie.setMaxAge(maxAge);
        this.cookies.add(nettyCookie);
        return this;
    }


    public Response cookie(@NonNull String name, @NonNull String value, int maxAge, boolean secured) {
        Cookie nettyCookie = new io.netty.handler.codec.http.cookie.DefaultCookie(name, value);
        nettyCookie.setPath("/");
        nettyCookie.setMaxAge(maxAge);
        nettyCookie.setSecure(secured);
        this.cookies.add(nettyCookie);
        return this;
    }


    public Response cookie(@NonNull String path, @NonNull String name, @NonNull String value, int maxAge, boolean secured) {
        Cookie nettyCookie = new io.netty.handler.codec.http.cookie.DefaultCookie(name, value);
        nettyCookie.setMaxAge(maxAge);
        nettyCookie.setSecure(secured);
        nettyCookie.setPath(path);
        this.cookies.add(nettyCookie);
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


    public void download(@NonNull String fileName, @NonNull File file) throws Exception {
        if (!file.exists() || !file.isFile()) {
            throw new NotFoundException("Not found file: " + file.getPath());
        }

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        Long fileLength = raf.length();
        this.contentType = StringKit.mimeType(file.getName());

        io.netty.handler.codec.http.HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpHeaders httpHeaders = httpResponse.headers().add(getDefaultHeader());

        boolean keepAlive = WebContext.request().keepAlive();
        if (keepAlive) {
            httpResponse.headers().set(HttpConst.CONNECTION, KEEP_ALIVE);
        }
        httpHeaders.set(HttpConst.CONTENT_TYPE, this.contentType);
        httpHeaders.set("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO8859_1"));
        httpHeaders.setInt(HttpConst.CONTENT_LENGTH, fileLength.intValue());

        // Write the initial line and the header.
        ctx.write(httpResponse);

        ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
        // Write the end marker.
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        sendFileFuture.addListener(ProgressiveFutureListener.build(raf));
        // Decide whether to close the connection or not.
        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
        isCommit = true;
    }


    public OutputStreamWrapper outputStream() throws IOException {
        File file = Files.createTempFile("blade", ".temp").toFile();
        OutputStream outputStream = new FileOutputStream(file);
        return new OutputStreamWrapper(outputStream, file, ctx);
    }


    public void redirect(@NonNull String newUri) {
        headers.set("Location", newUri);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        this.send(response);
    }


    public boolean isCommit() {
        return isCommit;
    }


    public void send(@NonNull FullHttpResponse response) {
        isCommit = true;
        response.headers().set(getDefaultHeader());
        boolean keepAlive = WebContext.request().keepAlive();
        if (!response.headers().contains(HttpConst.CONTENT_LENGTH)) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(HttpConst.CONTENT_LENGTH, String.valueOf(response.content().readableBytes()));
        }

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(HttpConst.CONNECTION, KEEP_ALIVE);
            ctx.write(response, ctx.voidPromise());
        }

    }

    private HttpHeaders getDefaultHeader() {
        headers.set(HttpConst.DATE, dateString);
        headers.set(HttpConst.CONTENT_TYPE, HttpConst.getContentType(this.contentType));
        headers.set(HttpConst.X_POWER_BY, HttpConst.VERSION);
        if (!headers.contains(HttpConst.SERVER)) {
            headers.set(HttpConst.SERVER, HttpConst.VERSION);
        }
        if (this.cookies.size() > 0) {
            this.cookies.forEach(cookie -> headers.add(HttpConst.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookie)));
        }
        return headers;
    }

    public Response(Response response) {
        this.contentType = response.contentType();
        this.statusCode = response.statusCode();
        if (null != response.headers()) {
            response.headers().forEach(this.headers::add);
        }
        if (null != response.cookies()) {
            response.cookies().forEach((k, v) -> this.cookies.add(new DefaultCookie(k, v)));
        }
    }

    public Response() {
    }

    public static HttpResponse build(ChannelHandlerContext ctx, CharSequence dateString) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.ctx = ctx;
        httpResponse.dateString = dateString;
        return httpResponse;
    }
}
