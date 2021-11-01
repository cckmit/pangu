package com.pangu.framework.socket.handler.param;

import com.pangu.framework.protocol.Transfer;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.type.*;
import com.pangu.framework.socket.core.CoderType;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.param.type.*;
import com.pangu.framework.utils.reflect.Assert;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <code>Prorocol</code>格式的编解码器
 * 默认格式为 0
 */
@Slf4j
public class ProtocolCoder implements Coder {

    private final Transfer transfer;

    public ProtocolCoder(Transfer transfer) {
        this.transfer = transfer;
    }

    private final ByteBufAllocator alloc = new PooledByteBufAllocator();

    @Override
    public byte getFormat() {
        return CoderType.PROTOCOL;
    }

    @Override
    public Object[] decodeRequest(Message message, Session session, Parameters params, CompletableFuture<?> completableFuture) {
        Assert.notNull(transfer, "protocol协议未实现");
        Parameter[] parameters = params.getParameters();
        if (parameters == null || parameters.length == 0) {
            return new Object[0];
        }
        List<Object> objects = new ArrayList<>(parameters.length);
        Object decode = null;

        for (Parameter parameter : parameters) {
            if (parameter instanceof InBodyParameter) {
                if (decode == null) {
                    decode = decode(message);
                }
                InBodyParameter real = (InBodyParameter) parameter;
                Object value = parseInBodyParam(message, real, decode);
                objects.add(value);
                continue;
            }
            if (parameter instanceof IdentityParameter) {
                objects.add(session.getIdentity());
                continue;
            }
            if (parameter instanceof MessageParameter) {
                objects.add(message);
                continue;
            }
            if (parameter instanceof SessionParameter) {
                objects.add(session);
                continue;
            }
            if (parameter instanceof FutureParameter) {
                objects.add(completableFuture);
                continue;
            }
            if (parameter instanceof BodyParameter) {
                BodyParameter bodyParameter = (BodyParameter) parameter;
                if (bodyParameter.isRaw() && bodyParameter.getType() == byte[].class) {
                    objects.add(message.getBody());
                    continue;
                }
                decode = decode(message);
                objects.add(decode);
                continue;
            }
            if (parameter instanceof InSessionParameter) {
                InSessionParameter rel = (InSessionParameter) parameter;
                String key = rel.getKey();
                Object sessionCtx = session.getCtx(key);
                if (sessionCtx == null) {
                    if (rel.isRequired()) {
                        log.info("请求[{}]需要session数据[{}]不存在", message.getHeader().getCommand(), key);
                        throw new SocketException(ExceptionCode.SESSION_PARAM);
                    }
                }
                objects.add(sessionCtx);
            }
        }
        return objects.toArray(new Object[0]);
    }

    private Object decode(Message message) {
        byte[] body = message.getBody();
        if (body != null && body.length > 0) {
            ByteBuf byteBuf = alloc.buffer(body.length);
            byteBuf.writeBytes(body);
            try {
                return transfer.decode(byteBuf);
            } catch (Exception e) {
                log.error("解码信息[" + message.getHeader() + "]异常", e);
                throw new SocketException(ExceptionCode.DECODE_ERROR);
            } finally {
                byteBuf.release();
            }
        }
        return null;
    }

    @Override
    public byte[] encodeParameters(Object[] args, Parameters params) {
        Parameter[] parameters = params.getParameters();
        int size = args == null ? 0 : args.length;
        if (size != parameters.length) {
            throw new IllegalArgumentException("请求参数数量[" + size + "不一致[" + parameters.length + "]");
        }
        if (size == 0) {
            return new byte[0];
        }
        Object param = null;
        if (params.isInBody()) {
            Map<String, Object> values = new HashMap<>(size);
            for (int i = 0; i < size; ++i) {
                Parameter parameter = parameters[i];
                if (!(parameter instanceof InBodyParameter)) {
                    continue;
                }
                values.put(((InBodyParameter) parameter).getName(), args[i]);
            }
            param = values;
        } else {
            for (int i = 0; i < size; ++i) {
                Parameter parameter = parameters[i];
                if (!(parameter instanceof BodyParameter)) {
                    continue;
                }
                param = args[i];
                if (((BodyParameter) parameter).isRaw() && param instanceof byte[]) {
                    return (byte[]) param;
                }
                break;
            }
        }
        if (param == null) {
            return new byte[0];
        }
        ByteBuf encode = transfer.encode(alloc, param);
        byte[] bytes = new byte[encode.readableBytes()];
        encode.readBytes(bytes);
        encode.release();
        return bytes;
    }

    @Override
    public byte[] encodeResponse(Object result) {
        Assert.notNull(transfer, "protocol协议未实现");
        ByteBuf encode = transfer.encode(alloc, result);
        byte[] bytes = new byte[encode.readableBytes()];
        encode.readBytes(bytes);
        encode.release();
        return bytes;
    }

    @Override
    public Object decodeResponse(Message message, MethodDefine methodDefine) {
        byte[] body = message.getBody();
        ByteBuf byteBuf = alloc.buffer(body.length);
        byteBuf.writeBytes(body);
        try {
            Assert.notNull(transfer, "protocol协议未实现");
            return transfer.decode(byteBuf);
        } finally {
            byteBuf.release();
        }
    }


    private Object parseInBodyParam(Message message, InBodyParameter real, Object object) {
        if (object == null) {
            if (real.isRequired()) {
                log.warn("消息[{}]注释[{}]请求的参数不存在", message.getHeader().getCommand(), real.getName());
                throw new SocketException(ExceptionCode.PARAM_PHASE);
            }
            return null;
        }
        String name = real.getName();
        if (object instanceof Map) {
            //noinspection rawtypes
            Map map = (Map) object;
            if (!map.containsKey(name)) {
                if (real.isRequired()) {
                    log.warn("消息[{}]注释[{}]请求的参数不存在", message.getHeader().getCommand(), real.getName());
                    throw new SocketException(ExceptionCode.PARAM_PHASE);
                }
                return null;
            }
            Object oriValue = map.get(name);
            Type type = real.getType();
            if (oriValue == null) {
                if (real.isRequired()) {
                    throw new SocketException(ExceptionCode.PARAM_PHASE);
                }
                if ((type instanceof Class) && ((Class<?>) type).isPrimitive()) {
                    throw new SocketException(ExceptionCode.PARAM_PHASE);
                }
            }

            return transfer.convert(oriValue, type);
        }
        throw new IllegalArgumentException("protocol协议解码获得对象类型为" + object.getClass());
    }
}
