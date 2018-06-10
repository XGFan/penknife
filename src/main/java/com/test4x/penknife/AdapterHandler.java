package com.test4x.penknife;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ChannelHandler.Sharable
public class AdapterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        final Request request = new Request(fullHttpRequest);
        final Response response = new Response();
        Boolean flag = true;
        for (Route route : PenKnife.routes) {
            final Route.MatchResult matchResult = route.match(request.getHttpMethod(), request.getUrl());
            if (matchResult.getResult()) {
                flag = false;
                final Action action = matchResult.getAction();
                executorService.submit(() -> {
                    try {
                        action.invoke(request, response);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ctx.writeAndFlush(response.send()).addListener(ChannelFutureListener.CLOSE);
                });
                break;
            }
        }
        if (flag) {
            ctx.writeAndFlush(response.status(HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (ctx.channel().isOpen() && ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isOpen() && ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.close();
        }
    }

}
