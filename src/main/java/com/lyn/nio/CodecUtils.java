package com.lyn.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CodecUtils {

    public static ByteBuffer read(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int num = socketChannel.read(byteBuffer);
        if(num == 0){
            return null;
        }
        return byteBuffer;

    }

    public static void write(SocketChannel socketChannel,String content) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(content.getBytes("GBK"));
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    public static String newString(ByteBuffer byteBuffer) throws UnsupportedEncodingException {
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.remaining()];
        System.arraycopy(byteBuffer.array(),byteBuffer.position(),bytes,0,byteBuffer.remaining());

        return new String(bytes,"GBK");
    }
}
