package com.pangu.framework.protocol.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可传输对象
 * @author Ramon
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transable {
	/**
	 * 别名
	 */
	String alias() default "";

	/**
	 * 是否Field
	 */
	boolean field() default true;
}
