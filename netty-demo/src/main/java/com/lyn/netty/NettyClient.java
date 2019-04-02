package com.lyn.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class NettyClient {


    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());
            Channel channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 9999)).sync().channel();

            exchange(channel);

        } catch (Exception e) {
            e.printStackTrace();
            eventLoopGroup.shutdownGracefully();
        }
    }

    private static void exchange(Channel channel) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ChannelFuture channelFuture = null;
        while (true) {
            String msg = reader.readLine();
            if (msg == null || "".equals(msg)) {
                continue;
            }
            channelFuture = channel.writeAndFlush(msg + "\r\n");
            if ("bye".equals(msg)) {
                channel.closeFuture().sync();
                break;
            }
        }
        if (channelFuture != null) {
            channelFuture.sync();
        }
    }

}
