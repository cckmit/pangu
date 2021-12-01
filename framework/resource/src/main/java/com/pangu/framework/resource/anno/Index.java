package com.pangu.framework.resource.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Index {
	
	
	String name();
	
	
	boolean unique() default false;

	
	@SuppressWarnings("rawtypes")
	Class<? extends Comparator> comparatorClz() default Comparator.class;
	
}
