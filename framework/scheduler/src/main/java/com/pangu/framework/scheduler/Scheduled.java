package com.pangu.framework.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 计划任务触发条件声明注释(使用 Cron 表达式)
 * @author author
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {

	
	String name();

	
	String value();
	
	
	ValueType type() default ValueType.EXPRESSION;
	
	
	String defaultValue() default "";
	
}
