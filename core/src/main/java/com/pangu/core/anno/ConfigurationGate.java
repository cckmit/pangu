package com.pangu.core.anno;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile("gateway")
@Configuration(proxyBeanMethods = false)
public @interface ConfigurationGate {
}
