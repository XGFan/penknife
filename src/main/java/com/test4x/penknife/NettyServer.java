package com.test4x.penknife;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer {
    private PenKnife penKnife;

    public NettyServer(PenKnife penKnife) {
        this.penKnife = penKnife;
    }

    public void start(int port) throws Exception {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        final SimpleChannelInboundHandler<FullHttpRequest> adapter = new AdapterHandler(penKnife);
        final ConvertHandler convertHandler = new ConvertHandler(penKnife);
        final EventExecutorGroup group = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2 + 1);
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
                                    .addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build()))
                                    .addLast(convertHandler)
                                    .addLast(group, adapter);
                        }
                    });
            final Channel ch = bootstrap.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        }
    }
}
