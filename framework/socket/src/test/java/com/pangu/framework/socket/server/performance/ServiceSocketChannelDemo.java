package com.pangu.framework.socket.server.performance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


/*服务器端，:接收客户端发送过来的数据并显示，
 *服务器把上接收到的数据加上"echo from service:"再发送回去*/
public class ServiceSocketChannelDemo {

    public static void main(String[] args) throws InterruptedException, IOException {
        TCPEchoServer server = new TCPEchoServer(18888);
        server.run();
    }

    public static class TCPEchoServer implements Runnable {

        
        private final InetSocketAddress localAddress;

        public TCPEchoServer(int port) throws IOException {
            this.localAddress = new InetSocketAddress(port);
        }


        @Override
        public void run() {

            Charset utf8 = StandardCharsets.UTF_8;

            ServerSocketChannel ssc = null;
            Selector selector = null;

            Random rnd = new Random();

            try {
                
                selector = Selector.open();

                
                ssc = ServerSocketChannel.open();
                ssc.setOption(StandardSocketOptions.SO_RCVBUF, 128);
                ssc.configureBlocking(false);

                
                ssc.bind(localAddress, 100);

                
                ssc.register(selector, SelectionKey.OP_ACCEPT);

            } catch (IOException e1) {
                System.out.println("server start failed");
                return;
            }

            System.out.println("server start with address : " + localAddress);

            
            try {

                while (!Thread.currentThread().isInterrupted()) {

                    int n = selector.select();
                    if (n == 0) {
                        continue;
                    }

                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = keySet.iterator();
                    SelectionKey key = null;

                    while (it.hasNext()) {

                        key = it.next();
                        
                        it.remove();

                        
                        try {
                            
                            if (key.isAcceptable()) {

                                /*accept方法会返回一个普通通道，
                                     每个通道在内核中都对应一个socket缓冲区*/
                                SocketChannel sc = ssc.accept();
                                sc.setOption(StandardSocketOptions.SO_RCVBUF, 128);
                                sc.configureBlocking(false);

                                
                                int interestSet = SelectionKey.OP_READ;
                                sc.register(selector, interestSet);

                                System.out.println("accept from " + sc.getRemoteAddress());
                            }


                            
                            if (key.isReadable()) {

                                

                                
                                SocketChannel sc = (SocketChannel) key.channel();
                                ByteBuffer readBuffer = ByteBuffer.allocate(128);
                                
                                sc.read(readBuffer);
                                Thread.sleep(1000);
                                readBuffer.flip();

                                System.out.println(readBuffer.remaining());

                                readBuffer.rewind();

                                readBuffer.clear();

                            }
                        } catch (IOException e) {
                            System.out.println("service encounter client error");
                            
                            key.cancel();
                            key.channel().close();
                        }

                    }

                    Thread.sleep(rnd.nextInt(500));
                }

            } catch (InterruptedException e) {
                System.out.println("serverThread is interrupted");
            } catch (IOException e1) {
                System.out.println("serverThread selecotr error");
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    System.out.println("selector close failed");
                } finally {
                    System.out.println("server close");
                }
            }

        }
    }

}