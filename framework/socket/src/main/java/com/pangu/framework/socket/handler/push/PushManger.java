package com.pangu.framework.socket.handler.push;

import com.pangu.framework.socket.handler.command.CommandRegister;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.handler.SessionManager;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PushManger {

    private final SessionManager sessionManager;

    private final Coder defaultCoder;
    private final Map<Byte, Coder> coders;

    private final ConcurrentHashMap<Class<?>, Object> pushProxy = new ConcurrentHashMap<>();

    public PushManger(SessionManager sessionManager, Coder defaultCoder, Map<Byte, Coder> coders) {
        this.sessionManager = sessionManager;
        this.defaultCoder = defaultCoder;
        this.coders = coders;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clz) {
        Object proxy = pushProxy.get(clz);
        if (proxy != null) {
            return (T) proxy;
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (clz) {
            proxy = pushProxy.get(clz);
            if (proxy != null) {
                return (T) proxy;
            }
            List<MethodDefine> methodDefines = CommandRegister.toMethodDefine(clz);
            proxy = Proxy.newProxyInstance(clz.getClassLoader(),
                    new Class[]{clz},
                    new PushInvoker(sessionManager, methodDefines, defaultCoder, coders));
            pushProxy.put(clz, proxy);
        }
        return (T) proxy;
    }
}
