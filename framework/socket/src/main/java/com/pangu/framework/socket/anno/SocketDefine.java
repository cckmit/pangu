package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

/**
 * Socket模块指令定义<br>
 * 无实际作用，用于客户端生成Socket指令
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketDefine {
	
}
