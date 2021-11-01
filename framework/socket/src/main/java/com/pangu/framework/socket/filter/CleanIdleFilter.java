package com.pangu.framework.socket.filter;

import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 清理空闲连接过滤器
 */
@Sharable
@Slf4j
@Setter
public class CleanIdleFilter extends ChannelInboundHandlerAdapter {

    private static final AttributeKey<Long> ACCESS_TIME = AttributeKey.newInstance("idle.accessTime");
    private static final AttributeKey<Future<?>> TIMEOUT_FUTURE = AttributeKey.newInstance("idle.timeFuture");


    // SESSION管理器
    @Autowired
    private SessionManager sessionManager;

    // 超时时间秒
    private long timeoutSeconds;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            Channel channel = ctx.channel();
            // 更新时间
            updateTime(channel);
            // 提交定时任务
            submitTask(ctx, timeoutSeconds);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 更新时间
        updateTime(channel);
        // 提交定时任务
        submitTask(ctx, timeoutSeconds);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        // 更新时间
        updateTime(channel);
        // 不用提交定时任务
        super.channelRead(ctx, msg);
    }

    private void submitTask(ChannelHandlerContext ctx, long delay) {
        Channel channel = ctx.channel();
        Attribute<Future<?>> attrFuture = channel.attr(TIMEOUT_FUTURE);
        Future<?> future = attrFuture.get();
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
        future = ctx.executor().schedule(new ReadTimeoutTask(ctx), delay, TimeUnit.SECONDS);
        attrFuture.set(future);
    }

    protected void updateTime(Channel channel) {
        Attribute<Long> attrTime = channel.attr(ACCESS_TIME);
        long currentTime = System.currentTimeMillis() / 1000;
        attrTime.set(currentTime);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destory(ctx);
        super.channelInactive(ctx);
    }

    private void destory(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Attribute<Future<?>> attr = channel.attr(TIMEOUT_FUTURE);
        Future<?> future = attr.get();
        attr.remove();
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }

    private final class ReadTimeoutTask implements Runnable {
        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            Channel channel = ctx.channel();
            if (!channel.isOpen()) {
                return;
            }

            // 上次访问时间
            Attribute<Long> attrTime = channel.attr(ACCESS_TIME);
            Long lastReadTime = attrTime.get();
            long currentTime = System.currentTimeMillis() / 1000;
            if (lastReadTime == null) {
                lastReadTime = currentTime;
            }
            long nextDelay = timeoutSeconds - (currentTime - lastReadTime);
            if (nextDelay <= 0) {
                if (log.isInfoEnabled()) {
                    log.info("连接[{}]最后访问[{}]超时关闭...", channel.remoteAddress(), new Date(lastReadTime));
                }
                try {
                    Session session = sessionManager.lookup(channel);
                    if (session != null) {
                        Object identity = session.getIdentity();
                        if (log.isDebugEnabled()) {
                            long sessionId = session.getId();
                            SocketAddress remoteAddress = session.getRemoteAddress();
                            if (identity != null) {
                                log.debug("连接[{}]会话[{}]超时移除 - 用户[{}]", remoteAddress, sessionId, identity);
                            } else {
                                log.debug("连接[{}]会话[{}]超时移除", remoteAddress, sessionId);
                            }
                        }
                        sessionManager.detach(channel);
                    }
                    channel.close();
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("连接[{}]超时时间更新...", channel.remoteAddress());
                }
                submitTask(ctx, timeoutSeconds);
            }
        }
    }
}
