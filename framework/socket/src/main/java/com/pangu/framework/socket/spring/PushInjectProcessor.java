package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.anno.PushInject;
import com.pangu.framework.utils.reflect.ReflectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import java.lang.reflect.Field;

public class PushInjectProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    @Autowired
    private SessionManager sessionManager;

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                PushInject annotation = field.getAnnotation(PushInject.class);
                if (annotation == null) {
                    return;
                }
                Class<?> clz = field.getType();
                Object pushProxy = sessionManager.getPushProxy(clz);
                field.setAccessible(true);
                field.set(bean, pushProxy);
            }
        });
        return true;
    }

}
