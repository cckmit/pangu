package com.pangu.framework.socket.filter;

import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@ChannelHandler.Sharable
@Slf4j
public class HeartBeatFilter extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            boolean heartBeat = message.hasState(StateConstant.HEART_BEAT);
            if (heartBeat) {
                if (message.hasState(StateConstant.STATE_RESPONSE)) {
                    log.trace("收到心跳响应数据包:" + new Date());
                    return;
                }
                message.addState(StateConstant.STATE_RESPONSE);
                ctx.channel().writeAndFlush(message);
                if (log.isDebugEnabled()) {
                    log.trace("收到心跳包:" + new Date());
                }
                return;
            }
        }
        super.channelRead(ctx, msg);
    }
}
