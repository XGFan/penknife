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

import java.util.concurrent.CountDownLatch;

@Slf4j
public class NettyServer {
    private PenKnife penKnife;

    final private NioEventLoopGroup boss = new NioEventLoopGroup();
    final private NioEventLoopGroup worker = new NioEventLoopGroup();

    static CountDownLatch latch = new CountDownLatch(1);

    public NettyServer(PenKnife penKnife) {
        this.penKnife = penKnife;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void start(int port) throws Exception {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        final SimpleChannelInboundHandler<FullHttpRequest> adapter = new AdapterHandler(penKnife);
        final ConvertHandler convertHandler = new ConvertHandler(penKnife);
        final EventExecutorGroup group = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2 + 1);
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
//                                    .addLast(new LoggingHandler(LogLevel.TRACE))
                                .addLast(new HttpContentCompressor())
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpServerExpectContinueHandler())
                                .addLast(new HttpObjectAggregator(100 * 1024 * 1024))
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build()))
                                .addLast(convertHandler)
                                .addLast(group, adapter);
                    }
                });
        bootstrap
                .bind(port)
                .addListener(future -> {
                            latch.countDown();
                        }
                )
                .channel()
                .closeFuture()
                .sync();
    }

    public void stop() {
        try {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
            latch = new CountDownLatch(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void waitForStarted() throws InterruptedException {
        latch.await();
    }
}
