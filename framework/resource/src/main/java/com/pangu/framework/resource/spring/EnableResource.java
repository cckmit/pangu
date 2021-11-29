package com.pangu.framework.resource.spring;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ResourceSpringConfiguration.class)
public @interface EnableResource {
    ResourceFormat format();

    ResourcePackage[] value() default {};

    ResourceClass[] clz() default {};
}
