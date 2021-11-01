package com.pangu.framework.socket.handler.param.type;

import com.pangu.framework.socket.handler.param.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InSessionParameter implements Parameter {

    private String key;

    private boolean required;
}
