package com.pangu.framework.socket.client;

import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.utils.lang.ByteUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientSendProxyInvoker implements InvocationHandler {

    // 消息分发器
    private final Client client;

    private final Map<Method, MethodDefine> parameters;

    ClientSendProxyInvoker(Client client, Map<Method, MethodDefine> parameters) {
        this.client = client;
        this.parameters = parameters;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RpcContext.unset();
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return method.toString();
            } else if ("hashCode".equals(methodName)) {
                return method.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return method.equals(args[0]);
        }

        Coder coder = client.getDefaultCoder();

        String name = method.getName();
        MethodDefine methodDefine = this.parameters.get(method);
        if (methodDefine == null) {
            throw new IllegalStateException("调用方法[" + name + "]没有添加 @SocketCommand 注解");
        }
        CompletableFuture<Object> send = null;
        try {
            byte[] requestBody = coder.encodeParameters(args, methodDefine.getParams());
            Header header = new Header();
            header.setFormat(coder.getFormat());
            header.setCommand(methodDefine.getCommand());

            byte[] timestamp = ByteUtils.longToByte(System.currentTimeMillis());
            Message message = Message.valueOf(header, requestBody, timestamp);
            send = client.send(message, methodDefine.isIgnoreResponse());
            if (methodDefine.isIgnoreResponse()) {
                return null;
            }
            if (methodDefine.isAsync()) {
                RpcContext.set(send);
                return null;
            }
            return send.get(client.getReadTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    send.completeExceptionally(cause);
                    throw (RuntimeException) cause;
                }
            }
            if (send != null && !send.isDone()) {
                send.completeExceptionally(e);
            }
            if (e instanceof TimeoutException || e instanceof InterruptedException) {
                throw new SocketException(ExceptionCode.TIME_OUT);
            }
            if (e instanceof SocketException) {
                throw (SocketException) e;
            }
            throw new SocketException(ExceptionCode.UNKNOWN, e);
        }
    }

}
