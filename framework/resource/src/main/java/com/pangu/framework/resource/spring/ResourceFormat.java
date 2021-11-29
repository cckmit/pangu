package com.pangu.framework.resource.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface ResourceFormat {
    String value();

    String type() default "excel";

    String i18n() default "false";

    String suffix() default "xlsx";

    String config() default "SERVER:0:true";
}
