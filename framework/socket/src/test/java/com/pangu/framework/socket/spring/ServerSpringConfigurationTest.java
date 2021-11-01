package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.handler.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:file.properties")
@Configuration
@EnableSocketServer
@EnableClientFactory
@ComponentScan("com.pangu.framework.socket.spring")
public class ServerSpringConfigurationTest {

    @Autowired
    private ClientFactory clientFactory;

    @Autowired
    private SessionManager sessionManager;

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServerSpringConfigurationTest.class);

        ServerSpringConfigurationTest bean = context.getBean(ServerSpringConfigurationTest.class);
        Client connect = bean.clientFactory.connect();
        String res = connect.getProxy(Facade.class).say("hello world");
        System.out.println(res);

        context.registerShutdownHook();
        context.close();
    }
}