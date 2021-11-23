package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.filter.SocketFilter;
import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.handler.SslConfig;
import com.pangu.framework.socket.server.SocketServer;
import com.pangu.framework.socket.server.SocketServerBuilder;
import com.pangu.framework.utils.reflect.Assert;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;

public class ServerFactoryBean implements FactoryBean<SocketServer>, ApplicationListener<ApplicationEvent> {

    // 读缓冲
    @Value("${socket.serverRead:2048}")
    private Integer readBuffSize;
    // 写缓冲
    @Value("${socket.serverWrite:2048}")
    private Integer writeBuffSize;
    // 等待连接最大数量
    @Value("${socket.backlog:256}")
    private Integer backlog;

    @Value("${socket.address:0.0.0.0:11111}")
    private String address;

    @Value("${socket.bossThreadName:服务器BOSS线程}")
    private String bossThreadName;

    @Autowired(required = false)
    @Qualifier("serverBossGroup")
    private NioEventLoopGroup bossEventGroup;

    @Value("${socket.workThreadName:服务器NIO线程}")
    private String workThreadName;

    @Autowired(required = false)
    @Qualifier("serverWorkGroup")
    private NioEventLoopGroup workEventGroup;

    @Value("${socket.workThreadAmount:0}")
    private int workThreadAmount;

    @Value("${socket.autoStart:true}")
    private boolean autoStart;

    // 是否启用
    @Value("${socket.sslServerEnable:false}")
    private boolean enabled;

    //证书密码
    @Value("${socket.sslPassword:}")
    private String password;

    //证书类型
    @Value("${socket.sslStoreType:}")
    private String storeType;

    // 证书路径
    @Value("${socket.sslStorePath:}")
    private String storePath;

    @Value("${socket.bootstrap:io.netty.channel.socket.nio.NioServerSocketChannel}")
    private Class<? extends ServerSocketChannel> bootstrapClass;

    @Autowired
    private DefaultDispatcher dispatcher;

    @Autowired
    private SessionManager sessionManager;

    @Autowired(required = false)
    private Collection<SocketFilter> filters;

    private SocketServer socketServer;

    @Override
    public SocketServer getObject() {
        if (socketServer == null) {
            SocketServerBuilder builder = new SocketServerBuilder();
            SslConfig sslConfig = new SslConfig(enabled, password, storeType, storePath);
            builder.address(address)
                    .readBuffSize(readBuffSize)
                    .writeBuffSize(writeBuffSize)
                    .bootstrapClass(bootstrapClass)
                    .backlog(backlog)
                    .bossThreadName(bossThreadName)
                    .work(workThreadAmount, workThreadName)
                    .sslConfig(sslConfig)
                    .filters(filters)
                    .dispatcher(dispatcher)
                    .sessionManager(sessionManager);
            if (bossEventGroup != null) {
                builder.bossEventLoop(bossEventGroup);
            }
            if (workEventGroup != null) {
                builder.workEventLoop(workEventGroup);
            }
            socketServer = builder.build();
        }
        return socketServer;
    }

    @Override
    public Class<?> getObjectType() {
        return SocketServer.class;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            SocketServer socketServer = getObject();
            Assert.notNull(socketServer, "SocketServer未完成初始化");
            if (autoStart) {
                socketServer.start();
            }
        } else if (event instanceof ContextClosedEvent) {
            if (!socketServer.getStatus().get()) {
                return;
            }
            if (socketServer != null) {
                socketServer.stop();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
