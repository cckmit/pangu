package com.pangu.framework.socket.handler.push;

import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.Parameter;
import com.pangu.framework.socket.handler.param.Parameters;
import com.pangu.framework.socket.handler.param.type.IdentityParameter;
import com.pangu.framework.socket.handler.param.type.SessionParameter;
import com.pangu.framework.socket.anno.PushAllIdentityClient;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class PushDefine {

    // 接口定义
    private final MethodDefine methodDefine;

    private final int paramIndex;

    private boolean allIdentity;

    public PushDefine(MethodDefine methodDefine) {
        this.methodDefine = methodDefine;
        Parameters params = methodDefine.getParams();
        Parameter[] parameters = params.getParameters();
        this.paramIndex = identityIndex(parameters);
        if (paramIndex >= 0) {
            return;
        }
        Method method = methodDefine.getMethod();
        this.allIdentity = method.getAnnotation(PushAllIdentityClient.class) != null;
    }

    private int identityIndex(Parameter[] parameters) {
        int size = parameters.length;
        for (int i = 0; i < size; ++i) {
            Parameter parameter = parameters[i];
            if (parameter instanceof SessionParameter) {
                return i;
            }
            if (parameter instanceof IdentityParameter) {
                return i;
            }
        }
        return -1;
    }
}
