package com.pangu.framework.socket.handler.command;

import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.anno.*;
import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.handler.param.Parameters;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Getter
public class MethodDefine {

    // 模块号-命令号
    private final Command command;

    // 参数集合
    private final Parameters params;

    // 返回值，泛型
    private final Type response;

    // 方法引用
    private final Method method;

    // 异步调用
    private final boolean async;

    // 不需要返回值
    private final boolean ignoreResponse;

    // 同步调用
    private final String syncQueueName;

    // session必须要授权
    private final boolean identity;

    // 管理后台IP
    private final boolean manager;

    // 请求数据是纯字节数组
    private final boolean requestRaw;

    // 响应数据是纯字节数组
    private final boolean responseRaw;

    public MethodDefine(Command command, Parameters params, Type response, Method annotationMethod, Method method, Raw raw) {
        this.command = command;
        this.params = params;
        this.identity = params.isIdentity();
        this.manager = annotationMethod.getAnnotation(Manager.class) != null;
        this.params.setManager(this.manager);
        this.response = response;
        this.method = method;
        this.async = annotationMethod.getAnnotation(Async.class) != null;
        this.ignoreResponse = annotationMethod.getAnnotation(IgnoreResponse.class) != null;
        Sync syncAnnotation = annotationMethod.getAnnotation(Sync.class);
        syncQueueName = syncAnnotation == null ? null : syncAnnotation.value();
        this.requestRaw = raw.request();
        this.responseRaw = raw.response();
    }

    @Override
    public String toString() {
        return "CommandDefine{" +
                "command=" + command +
                ", method=" + method +
                '}';
    }
}
