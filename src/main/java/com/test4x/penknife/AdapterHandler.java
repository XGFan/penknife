package com.test4x.penknife;

import com.test4x.penknife.message.Request;
import com.test4x.penknife.message.Response;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

@Slf4j
@ChannelHandler.Sharable
public class AdapterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private PenKnife penKnife;

    public AdapterHandler(PenKnife penKnife) {
        this.penKnife = penKnife;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        String url = fullHttpRequest.uri();
        final HttpMethod method = fullHttpRequest.method();
        int pathEndPos = url.indexOf('?');
        String noQueryUrl = pathEndPos < 0 ? url : url.substring(0, pathEndPos);
        final Response response = new Response();
        final Route.MatchResult matchResult = penKnife.match(method, noQueryUrl);
        final boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        if (matchResult != null) {
            final Request request = new Request(fullHttpRequest, matchResult.getPathArgMap());
            try {
                matchResult.getAction().invoke(request, response);
            } catch (Exception e) {
                log.error("{} {} Error", method, url, e);
                response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.status(HttpResponseStatus.NOT_FOUND);
        }
        if (keepAlive) {
            response.header(HttpHeaderNames.CONNECTION, KEEP_ALIVE);
            ctx.write(response, ctx.voidPromise());
        } else {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
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
        log.error("", cause);
        if (ctx.channel().isOpen() && ctx.channel().isActive() && ctx.channel().isWritable()) {
            ctx.close();
        }
    }


}
