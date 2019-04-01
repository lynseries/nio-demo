package com.lyn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NioClient {

    private SocketChannel socketChannel;

    private Selector selector;

    private List<String> responseStrs = new ArrayList<>();

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public NioClient() throws IOException, InterruptedException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        socketChannel.connect(new InetSocketAddress("127.0.0.1",8888));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleKey();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //if(countDownLatch.getCount() != 0){
            countDownLatch.await();
        //}

        System.out.println("Nio Client 启动完成");
    }

    private void handleKey() throws IOException {
        while (true){
            int num = selector.select(1*1000);
            if(num == 0){
                continue;
            }

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
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

        if(key.isConnectable()){
            handleConnectKey(key);
        }

        if(key.isWritable()){
            handleWriteKey(key);
        }

        if(key.isReadable()){
            handleReadKey(key);
        }
    }

    private void handleReadKey(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = CodecUtils.read(channel);
        if(byteBuffer.position()>0){
            String content = CodecUtils.newString(byteBuffer);
            System.out.println("接收到服务端数据:"+content);
           // channel.register(selector,SelectionKey.OP_WRITE,responseStrs);
        }
    }

    private void handleWriteKey(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();

        List<String> res = (List<String>) key.attachment();

        for(String re:res){
            System.out.println("写入数据:"+re+",clientId = "+Thread.currentThread().getId());
            CodecUtils.write(channel,re);
        }
        res.clear();

        channel.register(selector,SelectionKey.OP_READ,res);

    }

    private void handleConnectKey(SelectionKey key) throws IOException {
        if(!socketChannel.isConnectionPending()){
            return;
        }
        socketChannel.finishConnect();
        System.out.println("接收新的连接 channel");
        socketChannel.register(selector,SelectionKey.OP_READ,responseStrs);
        countDownLatch.countDown();
    }

    public void send(String content) throws ClosedChannelException {
        responseStrs.add(content);
        System.out.println("发送数据到服务端:"+content);
        socketChannel.register(selector,SelectionKey.OP_WRITE,responseStrs);
        selector.wakeup();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        NioClient nioClient = new NioClient();

        for(int i = 0;i<5;i++){
            nioClient.send("Hello,lmm,time:"+i);
            Thread.sleep(1000*5);
        }

    }

}
