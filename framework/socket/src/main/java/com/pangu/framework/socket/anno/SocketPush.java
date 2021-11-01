package com.pangu.framework.socket.anno;

import java.lang.annotation.*;

/**
 * 推送接口声明<br>
 * @author Ramon
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketPush {
	/**
	 * 当使用短连接时是否开启模块延迟推送<br>
	 * 不使用短连接时该配置无效
	 */
	boolean delayed() default true;
}
