package com.pangu.framework.socket.filter;

import com.dianping.cat.Cat;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Dispatcher;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;

@Sharable
@Slf4j
public class ServerHandler extends ChannelDuplexHandler {

    // 消息分发器
    private Dispatcher dispatcher;

    // SESSION管理器
    private SessionManager sessionManager;

    public ServerHandler(SessionManager sessionManager, Dispatcher dispatcher) {
        this.sessionManager = sessionManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        Session session = sessionManager.createSession(channel);
        if (log.isDebugEnabled()) {
            log.debug("[{}]连接会话[{}]创建添加", session.getId(), channel);
        }
        Cat.logMetricForCount("socket.active");
        channel.attr(Session.CREATE_TIME).set(System.currentTimeMillis());

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        sessionManager.detach(channel);
        Cat.logMetricForCount("socket.inactive");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 找回SESSION
        Channel channel = ctx.channel();
        Message message = (Message) msg;
        Session session = sessionManager.attach(channel, message);
        // 处理请求数据
        dispatcher.receive(message, session);
        Cat.logMetricForCount("socket.message");
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        SocketAddress remoteAddress = ctx.channel().remoteAddress();

        // write to closed socket channel
        if (throwable instanceof SocketException || throwable instanceof DecoderException) {
            log.info("socket层发生未知错误" + throwable.getMessage());
            return;
        }
        if (throwable instanceof IOException) {
            log.debug("socket关闭时仍有数据通信产生异常[{}]", remoteAddress);
            return;
        }

        // 生成错误堆栈信息
        StringBuilder sb = new StringBuilder();
        Throwable ex = throwable;
        while (ex != null) {
            StackTraceElement[] stackTrace = ex.getStackTrace();
            for (StackTraceElement st : stackTrace) {
                String className = st.getClassName();

                if (className.startsWith("sun.") || className.startsWith("java.")
                        || className.startsWith("org.springframework")) {
                    continue;
                }
                sb.append("\t").append(st.toString()).append("\n");
                // NETTY底层错误不再打印
                if (className.startsWith("io.netty.handler")) {
                    break;
                }
            }

            if (ex != ex.getCause()) {
                ex = ex.getCause();
                if (ex != null) {
                    sb.append("CAUSE\n").append(ex.getMessage()).append(ex).append("\n");
                }

            } else {
                break;
            }
        }

        log.error("{}\tError: {} - {}\n{}",
                throwable.getClass().getName(), throwable.getMessage(), remoteAddress, sb.toString());
    }
}
