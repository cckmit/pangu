package com.pangu.model.anno;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile("logic")
@Service
public @interface ServiceLogic {
}
