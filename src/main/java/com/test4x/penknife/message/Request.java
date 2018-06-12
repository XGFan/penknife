package com.test4x.penknife.message;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Request {

    private FullHttpRequest delegate;
    private Map<String, String> pathParameters;
    private Map<String, Object> bodyParameters = Collections.emptyMap();


    private final static HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    //todo
    public Request(FullHttpRequest fullHttpRequest, Map<String, String> pathArgMaps) {
        this.delegate = fullHttpRequest.duplicate();
        this.pathParameters = pathArgMaps;

        //body
        if (!fullHttpRequest.method().equals(HttpMethod.GET)) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
            final List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();

            if (!bodyHttpDatas.isEmpty()) {
                final InterfaceHttpData.HttpDataType firstType = bodyHttpDatas.get(0).getHttpDataType();
                if (firstType != InterfaceHttpData.HttpDataType.Attribute) {
                    throw new UnsupportedOperationException(firstType.toString());
                } else {
                    bodyParameters = bodyHttpDatas.stream().map(it -> {
                        try {
                            return new AbstractMap.SimpleEntry<>(it.getName(), ((Attribute) it).getValue());
                        } catch (IOException e) {
                            throw new UnsupportedOperationException(e);
                        }
                    }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey,
                            AbstractMap.SimpleEntry::getValue,
                            (s, s2) -> Arrays.asList(s, s2)));
                }
            }
        }
    }


    public Map<String, Cookie> cookie() {
        final String cookieStr = delegate.headers().get(HttpHeaderNames.COOKIE);
        if (cookieStr != null) {
            return ServerCookieDecoder.LAX.decode(cookieStr).stream().collect(Collectors.toMap(Cookie::name, Function.identity()));
        } else {
            return Collections.emptyMap();
        }
    }

    public String header(String name) {
        return delegate.headers().get(name);
    }

    public List<String> query(String str) {
        return new QueryStringDecoder(delegate.uri(), CharsetUtil.UTF_8).parameters().getOrDefault(str, Collections.emptyList());
    }

    public String path(String str) {
        return pathParameters.get(str);
    }

    public Object body(String str) {
        return bodyParameters.get(str);
    }

}
