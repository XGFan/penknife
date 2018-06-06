package com.test4x.penknife;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Http（TCP）服务端
 * 用于测试
 */
public class NettyServer {
    public static void main(String[] args) throws Exception {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");//简单设置日志
        final ServerBootstrap bootstrap = new ServerBootstrap();
        final Logger logger = LoggerFactory.getLogger(NettyServer.class);
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline()
//                                    .addLast(new LoggingHandler(LogLevel.TRACE))
                                    .addLast(new HttpContentCompressor())
                                    .addLast(new HttpServerCodec(36192 * 2, 36192 * 8, 36192 * 16, false))
                                    .addLast(new HttpServerExpectContinueHandler())
                                    .addLast(new HttpObjectAggregator(100 * 1024 * 1024))
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

                                            final Request request = new Request(msg);
                                            logger.info("{}", request.getUri());
                                            logger.info("{}", request.getHeaders());
                                            logger.info("{}", request.getParameters());
                                            logger.info("{}", request.cookie());
                                            final String ip = ((InetSocketAddress) ctx.channel().localAddress()).getHostString();
                                            final DefaultFullHttpResponse response =
                                                    new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                                            response.content().writeCharSequence(ip, StandardCharsets.UTF_8);
                                            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                        }
                                    });

                        }
                    });

            final Channel ch = bootstrap.bind(8080).sync().channel();
            ch.closeFuture().sync();
        } finally {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        }
    }
}
