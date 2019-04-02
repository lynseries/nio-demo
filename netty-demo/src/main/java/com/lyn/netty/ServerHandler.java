package com.lyn.netty;

import io.netty.channel.*;

import java.net.InetAddress;

@ChannelHandler.Sharable
public class ServerHandler extends  ChannelInboundHandlerAdapter {

    /**
     * 建立新连接时，发送消息给客户端.
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive...");
        ctx.write("Welcome to " +
                InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is \" + new Date() + \" now.\r\n");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead...msg====>" +msg);
        String response ;
        boolean close = false;
        if (msg == null ||"".equals(msg)){
            response = "please input something.\r\n";
        }else if("bye".equals(msg)){
            response = "have a good day.\r\n";
            close = true;
        }else {
            response = "did you say something ?\r\n";
        }

        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        if(close){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught...");
        cause.printStackTrace();
        ctx.close();
    }
}
