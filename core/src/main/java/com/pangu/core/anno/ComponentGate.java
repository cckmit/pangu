package com.pangu.core.anno;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 网关
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile("gateway")
@Component
public @interface ComponentGate {
}
