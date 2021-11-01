package com.pangu.framework.socket.handler.param.type;

import com.pangu.framework.socket.handler.param.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
@AllArgsConstructor
public class BodyParameter implements Parameter {

    private Type type;

    // 字节数组
    private boolean raw;
}
