package com.slisenko.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    public static void main(String[] args) throws InterruptedException {
        // We must bind event loop to every channel. All I/O for single channel is always in a single thread.
        // Event loop is a single thread
        // One event loop is shared between multiple channels

        // Two separate groups are used. If we have a high load and data volume and only 1 event loop group
        // it becomes too busy to accept new connections and they fail by timeout.
        // That's why separate event loop is used for only accepting connections.
        // Event loop is shared between channels. Here is the reason why we MUST NOT block it.
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // For accepting new connections
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // For processing I/O at existing connections

        // Netty has channel pipeline - list of channel handlers
        // pipeline contains of inbound and outbound handlers
        // handlers MUST NOT block I/O threads!
        // If we need to make a blocking operation - do it in another thread
        // Netty offers EventExecutorGroup which we can attach to each handler
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                // Can be standard IO (OioServerSocketChannel), but we need OioEventLoopGroup
                .channel(NioServerSocketChannel.class) // Netty has many different transports, one of them is NIO

                // For each channel Netty defines a pipeline
                // Pipeline is an ordered list if handlers
                // Handler is responsible for processing a message
                // Like servlets, or filters
                // Handler may process event and optionally pass it to next handler
                // Each handler may send messages back

                // We can add encode/decode handlers. For example HTTP or Protobuf handler
                // What if we need to do encoding/decoding and not block event loop?
                // Netty has feature of EventExecutor as separate thread
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LineBasedFrameDecoder(1000));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new EchoServerHandler());
                    }
                });

            ChannelFuture f = b.bind(45002).sync();
            System.out.println("Starting nio server at " + f.channel().localAddress());

            // Wait until server socket is closed
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}