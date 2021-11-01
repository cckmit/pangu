package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.client.ClientFactoryBuilder;
import com.pangu.framework.socket.handler.SslConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;

public class ClientFactoryBean implements FactoryBean<ClientFactory> {

    // 读缓冲
    @Value("${xa.socket.client.read:16384}")
    private Integer readBuffSize;
    // 写缓冲
    @Value("${xa.socket.client.write:16384}")
    private Integer writeBuffSize;

    // 连接请求超时
    @Value("${xa.socket.client.timeout.connect:3000}")
    private int connectTimeout;
    // 同步请求超时
    @Value("${xa.socket.client.timeout.read:3000}")
    private int readTimeout;

    @Value("${xa.socket.address:0.0.0.0:11111}")
    private String address;

    @Value("${xa.socket.workThreadName:客户端NIO线程}")
    private String workThreadName;

    @Value("${xa.socket.workThreadAmount:0}")
    private int workThreadAmount;

    @Value("${xa.socket.heartBeat:true}")
    private boolean heartBeat;
    @Value("${xa.socket.heartBeatIntervalMs:5000}")
    private int heartBeatIntervalMs;
    @Value("${xa.socket.keepAliveMs:300000}")
    private int keepAliveMs;

    // 是否启用
    @Value("${xa.socket.ssl.client.enable:false}")
    private boolean enabled;

    private ClientFactory clientFactory;

    @Override
    public ClientFactory getObject() {
        if (clientFactory == null) {
            SslConfig sslConfig = new SslConfig(enabled);
            ClientFactoryBuilder builder = new ClientFactoryBuilder();
            clientFactory = builder.address(address)
                    .readBuffSize(readBuffSize)
                    .writeBuffSize(writeBuffSize)
                    .work(workThreadAmount, workThreadName)
                    .ssl(sslConfig)
                    .heartBeat(heartBeat)
                    .heartBeatIntervalMs(heartBeatIntervalMs)
                    .keepAliveMs(keepAliveMs)
                    .connectTimeout(connectTimeout)
                    .readTimeout(readTimeout)
                    .build();
            clientFactory.start();
        }
        return clientFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return ClientFactory.class;
    }
}
