package com.test4x.penknife.message;

import com.test4x.penknife.entity.Parameter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Request {

    //    private FullHttpRequest delegate;
    private Map<String, String> pathParameters;
    private Map<String, List<String>> bodyParameters = Collections.emptyMap();
    private Map<String, List<String>> queryParameters;
    private HttpHeaders headers;

    private final static HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    //todo
    public Request(FullHttpRequest fullHttpRequest, Map<String, String> pathArgMaps) {
        FullHttpRequest delegate = fullHttpRequest;
        //path
        if (pathArgMaps != null) {
            this.pathParameters = pathArgMaps;
        }
        //query
        this.queryParameters = new QueryStringDecoder(delegate.uri(), CharsetUtil.UTF_8).parameters();
        //header
        headers = delegate.headers();
        //body
        if (!delegate.method().equals(HttpMethod.GET)) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, delegate);
            final List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();

            if (!bodyHttpDatas.isEmpty()) {
                final InterfaceHttpData.HttpDataType firstType = bodyHttpDatas.get(0).getHttpDataType();
                if (firstType != InterfaceHttpData.HttpDataType.Attribute) {
                    throw new UnsupportedOperationException(firstType.toString());
                } else {
                    bodyParameters = bodyHttpDatas.stream().map(it -> {
                        try {
                            return new Parameter(it.getName(), ((Attribute) it).getValue());
                        } catch (IOException e) {
                            throw new UnsupportedOperationException(e);
                        }
                    }).collect(Collectors.toMap(
                            Parameter::getKey,
                            Parameter::getValues,
                            (s1, s2) -> {
                                s1.addAll(s2);
                                return s1;
                            }));
                }
            }
        }
    }


    public Map<String, Cookie> cookie() {
        final String cookieStr = headers.get(HttpHeaderNames.COOKIE);
        if (cookieStr != null) {
            return ServerCookieDecoder.LAX.decode(cookieStr).stream().collect(Collectors.toMap(Cookie::name, Function.identity()));
        } else {
            return Collections.emptyMap();
        }
    }

    public String header(String name) {
        return headers.get(name);
    }

    public List<String> queryList(String str) {
        return queryParameters.getOrDefault(str, Collections.emptyList());
    }

    public String query(String str) {
        final List<String> strings = queryParameters.get(str);
        if (strings != null) {
            return String.join(",", strings);
        } else {
            return null;
        }
    }

    public String path(String str) {
        return pathParameters.get(str);
    }

    public List<String> bodyList(String str) {
        return bodyParameters.get(str);
    }

    public String body(String str) {
        final List<String> strings = bodyParameters.get(str);
        if (strings == null) {
            return null;
        }
        return String.join(",", strings);
    }

}
