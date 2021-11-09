package com.pangu.core.common;

import com.pangu.core.anno.SingleService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class SingleServiceCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        BeanDefinitionRegistry registry = context.getRegistry();
        Map<String, Object> attrs = metadata.getAnnotationAttributes(SingleService.class.getName());
        if (attrs != null) {
            Class<?> value = (Class<?>) attrs.get("value");
            return !registry.containsBeanDefinition(value.getSimpleName());
        }
        return true;
    }
}
