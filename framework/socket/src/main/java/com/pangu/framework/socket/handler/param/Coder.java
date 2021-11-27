package com.pangu.framework.socket.handler.param;

import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.command.MethodDefine;
import io.netty.util.AttributeKey;

import java.util.concurrent.CompletableFuture;

/**
 * 编解码传输内容定义
 */
public interface Coder {

    AttributeKey<Byte> LAST_CODER = AttributeKey.newInstance("last_coder_id");

    /**
     * 传输定义
     *
     * @return
     */
    byte getFormat();

    Object[] decodeRequest(Message message, Session session, Parameters params, CompletableFuture<?> completableFuture, Attachment attachment);

    byte[] encodeParameters(Object[] args, Parameters params);

    byte[] encodeResponse(Object result);

    Object decodeResponse(Message message, MethodDefine methodDefine);
}
