package com.pangu.framework.socket.spring.protocol;

import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.handler.param.ProtocolCoder;
import com.pangu.framework.socket.spring.EnableClientFactory;
import com.pangu.framework.socket.spring.EnableSocketServer;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@EnableSocketServer
@EnableClientFactory
@ImportResource(value = {"classpath:com/pangu/framework/socket/spring/protocol/socket.xml"})
@ComponentScan("com.pangu.framework.socket.spring.protocol")
public class ProtocolTest {

    @Test
    public void test_simple() throws InterruptedException {
        ClassPathResource classPathResource = new ClassPathResource("xaigame.jks");
        try {
            String absolutePath = classPathResource.getFile().getAbsolutePath();
            System.out.println(absolutePath);
            System.getProperties().put("xa.socket.ssl.storePath", absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ProtocolTest.class);
        ClientFactory clientFactory = ctx.getBean(ClientFactory.class);
        ProtocolCoder bean = ctx.getBean(ProtocolCoder.class);
        try {
            Client connect = clientFactory.connect();
            connect.setCoder(bean);
            PlayerVo hello = connect.getProxy(Facade.class).say(null, "hello");
            assertEquals(hello.getName(), "res:hello");
        } finally {
            Thread.sleep(500);
            ctx.close();
        }
    }
}
