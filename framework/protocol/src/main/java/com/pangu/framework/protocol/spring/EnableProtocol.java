package com.pangu.framework.protocol.spring;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ProtocolSpringConfiguration.class)
public @interface EnableProtocol {
    ProtocolPackage[] packages() default {};

    ProtocolClass[] clz() default {};
}
