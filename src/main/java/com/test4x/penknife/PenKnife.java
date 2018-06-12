package com.test4x.penknife;

import com.test4x.penknife.converter.HttpMessageConverter;
import com.test4x.penknife.converter.JsonConverter;
import com.test4x.penknife.converter.PlainConverter;
import io.netty.handler.codec.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class PenKnife {

    private List<Route> routes = new LinkedList<>();

    private LinkedList<HttpMessageConverter> converters = new LinkedList<>();

    private PenKnife addAction(HttpMethod httpMethod, String path, Action action) {
        routes.add(new Route(httpMethod, path, action));
        return this;
    }


    public PenKnife register(HttpMessageConverter httpMessageConverter) {
        converters.push(httpMessageConverter);
        return this;
    }

    List<HttpMessageConverter> converters() {
        return converters;
    }

    public PenKnife get(String path, Action action) {
        return addAction(HttpMethod.GET, path, action);
    }

    public PenKnife post(String path, Action action) {
        return addAction(HttpMethod.POST, path, action);
    }

    public PenKnife delete(String path, Action action) {
        return addAction(HttpMethod.DELETE, path, action);
    }

    public PenKnife put(String path, Action action) {
        return addAction(HttpMethod.PUT, path, action);
    }


    public Route.MatchResult match(HttpMethod httpMethod, String uri) {
        for (Route route : routes) {
            final Route.MatchResult matchResult = route.match(httpMethod, uri);
            if (matchResult.getResult()) {
                return matchResult;
            }
        }
        return null;
    }


    public static PenKnife INSTANCE = new PenKnife();

    static {
        INSTANCE.register(new PlainConverter());
        INSTANCE.register(new JsonConverter());
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                new NettyServer(this).start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }




}
