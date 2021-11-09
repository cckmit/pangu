package com.pangu.core.anno;

import com.pangu.core.common.SingleServiceCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(SingleServiceCondition.class)
public @interface SingleService {
    Class<?> value();
}
