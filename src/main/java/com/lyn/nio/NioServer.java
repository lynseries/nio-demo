package com.lyn.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * NIO Server
 */
public class NioServer {

    /**
     * 服务端Channel
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * 事件选择器
     */
    private Selector selector;

    public NioServer() throws IOException {
        //创建channel
        this.serverSocketChannel = ServerSocketChannel.open();

        //设置非阻塞
        this.serverSocketChannel.configureBlocking(false);

        //监听
        this.serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1",8888));

        //创建选择器
        this.selector = Selector.open();

        //服务端channel注册接收连接事件
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        System.out.println("Nio Server started");

        //处理
        handleKey();
    }

    private void handleKey() throws IOException {

        while (true){
            int num = this.selector.select(1 * 1000);

            if(num == 0){
                continue;
            }

            Set<SelectionKey> keys = this.selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(!key.isValid()){
                    continue;
                }

                handleKey(key);
            }
        }

    }

    private void handleKey(SelectionKey key) throws IOException {
        if(key.isAcceptable()){
            handleKeyAccept(key);
        }

        if (key.isReadable()){
            handleKeyRead(key);
        }

        if(key.isWritable()){
            handleKeyWrite(key);
        }

        if(key.isConnectable()){
            handleKeyConnect(key);
        }
    }

    private void handleKeyConnect(SelectionKey key) {
        System.out.println("处理连接事件");
    }

    /**
     * 处理写
     * @param key
     * @throws IOException
     */
    private void handleKeyWrite(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<String> reponseStrs = (List<String>) key.attachment();
        for(String content:reponseStrs){
            System.out.println("写入客户端数据:"+content);
            CodecUtils.write(socketChannel,content);
        }
        reponseStrs.clear();
        socketChannel.register(selector,SelectionKey.OP_READ,reponseStrs);
    }

    /**
     * 处理读
     * @param key
     * @throws IOException
     */
    private void handleKeyRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = CodecUtils.read(socketChannel);
       // System.out.printf("处理读取数据:"+byteBuffer.get);
        if(byteBuffer == null){
            System.out.println("断开连接Channel");
            socketChannel.register(selector,SelectionKey.OP_READ);
            return;
        }

        if(byteBuffer.position()>0){
            String content = CodecUtils.newString(byteBuffer);
            System.out.println("接收到客户端消息为:"+content);

           List<String> responseStrs = (List<String>) key.attachment();

           responseStrs.add(content + "\r");
           socketChannel.register(selector,SelectionKey.OP_WRITE,responseStrs);
        }


    }

    /**
     * 处理连接
     * @param key
     * @throws IOException
     */
    private void handleKeyAccept(SelectionKey key) throws IOException {
        SocketChannel clientSocketChannel = ((ServerSocketChannel) key.channel()).accept();
        clientSocketChannel.configureBlocking(false);
        System.out.println("接收到新连接:"+clientSocketChannel.getRemoteAddress().toString());
        clientSocketChannel.register(selector,SelectionKey.OP_READ,new ArrayList<String>());
    }



    public static void main(String[] args) throws IOException {
        int a = 1 << 0;
        System.out.println(a);

        NioServer nioServer = new NioServer();
    }



}
