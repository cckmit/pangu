package com.pangu.framework.socket.handler.param.type;

import com.pangu.framework.socket.handler.param.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdentityParameter implements Parameter {
    private boolean required;
}
