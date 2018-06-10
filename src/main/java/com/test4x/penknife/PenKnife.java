package com.test4x.penknife;

import io.netty.handler.codec.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class PenKnife {
    static List<Route> routes = new LinkedList<>();

    private static void addAction(HttpMethod httpMethod, String path, Action action) {
        routes.add(new Route(httpMethod, path, action));
    }

    public static void get(String path, Action action) {
        addAction(HttpMethod.GET, path, action);
    }

    public static void post(String path, Action action) {
        addAction(HttpMethod.POST, path, action);
    }

    public static void delete(String path, Action action) {
        addAction(HttpMethod.DELETE, path, action);
    }

    public static void put(String path, Action action) {
        addAction(HttpMethod.PUT, path, action);
    }


    static void invoke(HttpMethod httpMethod, String url) {
        for (Route route : routes) {
            final Route.MatchResult matchResult = route.match(httpMethod, url);
            if (matchResult.getResult()) {
                final Action action = matchResult.getAction();


            }

        }

    }


    static void newJob(HttpMethod httpMethod, String url) {
        for (Route route : routes) {
            final Route.MatchResult matchResult = route.match(httpMethod, url);
            if (matchResult.getResult()) {
                final Action action = matchResult.getAction();
            }
        }

    }

    public static void start(int port) {
        new Thread(() -> {
            try {
                NettyServer.start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void main(String[] args) {
        PenKnife.get("index",(req, res) -> {
            res.bodyText(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        });
        PenKnife.post("what",(req, res) -> {
            res.bodyText(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        });
        PenKnife.start(8080);
        System.out.println("hello");
    }

}
