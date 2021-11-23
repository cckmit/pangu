package com.pangu.framework.socket.client;

import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.command.CommandRegister;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.handler.param.Parameter;
import com.pangu.framework.socket.handler.param.Parameters;
import com.pangu.framework.socket.handler.param.type.InBodyParameter;
import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.codec.ZlibUtils;
import com.pangu.framework.utils.lang.ByteUtils;
import com.pangu.framework.utils.model.Result;
import com.pangu.framework.utils.model.ResultCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static com.pangu.framework.socket.core.StateConstant.COMPRESS_LIMIT;

@Slf4j
public class Client {

    public static final AttributeKey<Client> CLIENT_KEY = AttributeKey.newInstance("clientRef");
    public static final AttributeKey<Long> LAST_MESSAGE_TIME = AttributeKey.newInstance("lastMessageTime");
    public static final AttributeKey<InetSocketAddress> CLIENT_HEART_BEAT = AttributeKey.newInstance("client-heart-beat");

    private static final ConcurrentHashMap<Command, MethodDefine> commandDefines = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Map<Method, MethodDefine>> classDefines = new ConcurrentHashMap<>();

    public static AtomicLong sn = new AtomicLong(1);

    private final ConcurrentHashMap<Long, CompletableFuture<Object>> futures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> proxy = new ConcurrentHashMap<>();

    // 连接地址
    private InetSocketAddress address;
    // 当前与服务器连接的会话(如果没有强制调用connect，那么在第一个数据包发送之前，channel未创建)
    private final Channel channel;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Setter
    private Coder coder;

    @Getter
    @Setter
    private int readTimeout = 3000;

    // 正在通信
    private final LongAdder concurrent = new LongAdder();

    public Client(Channel channel) {
        this.channel = channel;
    }

    public Coder getDefaultCoder() {
        return coder;
    }

    public <T> CompletableFuture<T> send(Command command, Map<String, Object> paramMap) {
        MethodDefine methodDefine = commandDefines.get(command);
        if (methodDefine == null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new SocketException(ExceptionCode.TYPE_DEFINE_NOT_FOUND, command.toString()));
            return future;
        }
        Parameters paramsDefine = methodDefine.getParams();
        Object[] argument = parseMapToArray(paramMap, paramsDefine);
        byte[] requestBody = coder.encodeParameters(argument, paramsDefine);
        Header header = new Header();
        header.setFormat(coder.getFormat());
        header.setCommand(methodDefine.getCommand());
        byte[] timestamp = ByteUtils.longToByte(System.currentTimeMillis());
        Message message = Message.valueOf(header, requestBody, timestamp);
        return send(message, false);
    }

    private Object[] parseMapToArray(Map<String, Object> paramMap, Parameters paramsDefine) {
        Parameter[] params = paramsDefine.getParameters();
        int size = params.length;
        Object[] arguments = new Object[size];

        if (size == 0) {
            return arguments;
        }
        // inBody和body只会同时出现一种
        if (paramsDefine.isInBody()) {
            for (int i = 0; i < size; ++i) {
                Parameter parameter = params[i];
                if (!(parameter instanceof InBodyParameter)) {
                    continue;
                }
                Object o = paramMap.get(((InBodyParameter) parameter).getName());
                arguments[i] = o;
            }
            return arguments;
        } else {
            throw new SocketException(ExceptionCode.PARAM_PHASE);
        }
    }

    public <T> CompletableFuture<T> send(Message message, boolean ignoreResponse) {
        if (!channel.isActive()) {
            log.error("连接[{}]已经关闭", channel);
            throw new SocketException(ExceptionCode.CONNECT_HAS_CLOSED);
        }
        if (closed.get()) {
            throw new SocketException(ExceptionCode.CONNECT_HAS_CLOSED);
        }
        byte[] requestBody = message.getBody();
        if (!message.hasState(StateConstant.STATE_COMPRESS) && requestBody != null && requestBody.length > COMPRESS_LIMIT) {
            requestBody = ZlibUtils.zip(requestBody);
            message.setBody(requestBody);
            message.addState(StateConstant.STATE_COMPRESS);
        }
        long sn = Client.sn.getAndIncrement() & 0xFFFFFFFFFFFFL;
        message.updateSn(sn);
        CompletableFuture<Object> completableFuture = null;
        if (!ignoreResponse) {
            completableFuture = new CompletableFuture<>();
            CompletableFuture<Object> pre = futures.put(sn, completableFuture);
            if (pre != null) {
                log.warn("理论上不会到达这个位置,[{}]序列号的数据包依然存在", sn);
            }
            completableFuture.exceptionally(k -> {
                if (k != null) {
                    futures.remove(sn);
                }
                return k;
            });
        }
        // 增加并发度
        concurrent.increment();
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener(future -> concurrent.decrement());
        if (!message.hasState(StateConstant.HEART_BEAT)) {
            Attribute<Long> attr = channel.attr(LAST_MESSAGE_TIME);
            attr.set(System.currentTimeMillis());
        }
        if (ignoreResponse) {
            return CompletableFuture.completedFuture(null);
        }
        // 防止编码或者其他异常未发送成功，导致没有服务端返回，future无法正常移除
        channelFuture.addListener(new SendMessageListener(sn, ignoreResponse));
        //noinspection unchecked
        return (CompletableFuture<T>) completableFuture;
    }

    public <T> T getProxy(Class<T> clz) {
        Object o = proxy.get(clz);
        if (o != null) {
            //noinspection unchecked
            return (T) o;
        }
        Map<Method, MethodDefine> methodMethodDefineMap = registerAndGetMethodDefine(clz);

        ClientSendProxyInvoker proxyInvoker = new ClientSendProxyInvoker(this, methodMethodDefineMap);

        Object o1 = Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, proxyInvoker);
        proxy.put(clz, o1);
        //noinspection unchecked
        return (T) o1;
    }

    public static <T> Map<Method, MethodDefine> registerAndGetMethodDefine(Class<T> clz) {
        Map<Method, MethodDefine> methodMethodDefineMap = classDefines.get(clz);
        if (methodMethodDefineMap != null) {
            return methodMethodDefineMap;
        }
        List<MethodDefine> defines = CommandRegister.toMethodDefine(clz);
        if (defines.isEmpty()) {
            throw new IllegalArgumentException(clz.getName() + "没有配置@SocketModule或者@SocketCommand");
        }
        methodMethodDefineMap = new HashMap<>(defines.size());
        for (MethodDefine define : defines) {
            methodMethodDefineMap.put(define.getMethod(), define);
            commandDefines.put(define.getCommand(), define);
        }
        classDefines.put(clz, methodMethodDefineMap);
        return methodMethodDefineMap;
    }

    void receive(Message message) {
        Header header = message.getHeader();
        if (!header.isResponse()) {
            log.debug("Client收到请求消息，直接忽视[{}][{}]", channel, message);
            return;
        }
        if (!message.hasState(StateConstant.HEART_BEAT)) {
            Attribute<Long> attr = channel.attr(LAST_MESSAGE_TIME);
            attr.set(System.currentTimeMillis());
        }
        byte[] body = message.getBody();
        if (message.hasState(StateConstant.STATE_COMPRESS) && body != null) {
            body = ZlibUtils.unzip(body);
            message.removeState(StateConstant.STATE_COMPRESS);
            message.setBody(body);
        }
        long sn = header.getSn();
        CompletableFuture<Object> future = futures.remove(sn);
        if (future == null) {
            byte[] attachment = message.getAttachment();
            long send = 0, serverProcess = 0;
            if (attachment != null && attachment.length > 8) {
                send = ByteUtils.longFromByte(attachment, 0);
                if (attachment.length >= 16) {
                    serverProcess = ByteUtils.longFromByte(attachment, 8);
                }
            }
            long now = System.currentTimeMillis();
            log.info("收到数据包[{}]没有找到sn对应future，可能请求超时删除[{}]-[{}]-[{}],diff[{}]", message, send, serverProcess, now, now - send);
            return;
        }
        if (header.hasState(StateConstant.REDIRECT)) {
            future.complete(message);
            return;
        }
        int errorCode = header.getErrorCode();
        Command command = message.getHeader().getCommand();
        MethodDefine methodDefine = commandDefines.get(command);
        if (errorCode != 0) {
            if (header.hasError(StateConstant.MANAGED_EXCEPTION)) {
                int code = ResultCode.UNKNOWN_ERROR;
                Type responseType = methodDefine.getResponse();
                if (body != null && body.length > 0) {
                    if ((responseType instanceof ParameterizedType && ((ParameterizedType) responseType).getRawType() == Result.class)
                            || responseType == Result.class) {
                        Object res = coder.decodeResponse(message, methodDefine);
                        code = ((Result<?>) res).getCode();
                    } else if (responseType == Integer.class || responseType == int.class) {
                        Object res = coder.decodeResponse(message, methodDefine);
                        code = (Integer) res;
                    }
                }
                future.completeExceptionally(new ManagedException(code));
                return;
            } else if (header.hasError(StateConstant.IDENTITY_EXCEPTION)) {
                future.completeExceptionally(new SocketException(ExceptionCode.IDENTITY_PARAMETER));
                return;
            } else if (header.hasError(StateConstant.MANAGE_IP_EXCEPTION)) {
                future.completeExceptionally(new SocketException(ExceptionCode.MANAGED_IP));
                return;
            }
            future.completeExceptionally(new SocketException(ExceptionCode.of(errorCode)));
            return;
        }
        if (methodDefine == null) {
            future.completeExceptionally(new SocketException(ExceptionCode.TYPE_DEFINE_NOT_FOUND, command.toString()));
            return;
        }
        if (methodDefine.getResponse() == Void.TYPE) {
            future.complete(null);
            return;
        }
        if (body == null || body.length == 0) {
            future.complete(null);
            return;
        }
        try {
            // 原生字节数组
            if (methodDefine.isResponseRaw() && methodDefine.getResponse() == byte[].class) {
                future.complete(body);
                return;
            }
            Object res = getDefaultCoder().decodeResponse(message, methodDefine);
            future.complete(res);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (Map.Entry<Long, CompletableFuture<Object>> entry : futures.entrySet()) {
            CompletableFuture<Object> v = entry.getValue();
            v.completeExceptionally(new SocketException(ExceptionCode.CONNECT_HAS_CLOSED));
            log.debug("sn[" + entry.getKey() + "]消息因关闭取消");
        }
        if (channel != null) {
            log.debug("client连接端关闭[{}]", channel);
            channel.close();
        }
    }

    public synchronized boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public void heartBeat() {
        Header header = new Header();
        header.setFormat(coder.getFormat());
        header.setCommand(new Command());
        header.addState(StateConstant.HEART_BEAT);
        Message message = Message.valueOf(header, new byte[0], new byte[0]);
        send(message, true);
    }

    public MethodDefine getMethodDefine(Class<?> clz, Method method) {
        Map<Method, MethodDefine> methodMethodDefineMap = registerAndGetMethodDefine(clz);
        return methodMethodDefineMap.get(method);
    }

    public long getConcurrent() {
        return concurrent.sum();
    }

    private class SendMessageListener implements GenericFutureListener<Future<? super Void>> {
        private final long sn;
        private final boolean ignoreResponse;


        public SendMessageListener(long sn, boolean ignoreResponse) {
            this.sn = sn;
            this.ignoreResponse = ignoreResponse;
        }

        @Override
        public void operationComplete(Future<? super Void> future) {
            Throwable cause = future.cause();
            if (cause == null) {
                return;
            }
            if (ignoreResponse) {
                log.warn("client发送数据[" + sn + "]异常", cause);
                return;
            }
            CompletableFuture<Object> remove = futures.remove(sn);
            if (remove == null) {
                return;
            }
            remove.completeExceptionally(cause);
        }
    }
}
