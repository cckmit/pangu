package com.pangu.framework.socket.handler.param.type;

import com.pangu.framework.socket.handler.param.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Type;

@Getter
@AllArgsConstructor
public class InBodyParameter implements Parameter {

    private String name;

    private Type type;

    private boolean required;
}
