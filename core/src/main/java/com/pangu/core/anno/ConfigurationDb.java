package com.pangu.core.anno;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.annotation.*;

/**
 * 标注为DB服
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile("db")
@Configuration
public @interface ConfigurationDb {
}
