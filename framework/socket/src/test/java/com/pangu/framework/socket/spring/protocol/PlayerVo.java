package com.pangu.framework.socket.spring.protocol;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

@Transable
@Data
public class PlayerVo {
    private String name;
}
