package com.pangu.framework.socket.handler.command;

import com.pangu.framework.socket.utils.bytecode.Wrapper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@ToString
@Slf4j
@Getter
public class MethodProcessor {

    private final MethodDefine methodDefine;

    private final Object target;

    private final Class<?>[] paramTypes;

    private final String name;

    private final Wrapper proxyWrapper;

    public MethodProcessor(MethodDefine methodDefine, Object target, Method method, Wrapper proxyWrapper) {
        this.methodDefine = methodDefine;
        this.target = target;
        this.proxyWrapper = proxyWrapper;
        this.paramTypes = method.getParameterTypes();
        this.name = method.getName();
    }

    public Object process(Object[] params) throws Exception {
        return proxyWrapper.invokeMethod(target, name, paramTypes, params);
    }
}
