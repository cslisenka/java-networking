package com.slisenko.server.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Server received " + msg);
        ctx.write(msg + " server time: " + System.currentTimeMillis() + "\r\n");
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
//            .addListener(ChannelFutureListener.CLOSE); // Close connection after response
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
