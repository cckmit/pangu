package com.pangu.framework.socket.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public abstract class AnnotationUtils {

    public static <T extends Annotation> T findAnnotation(Class<?> clz, Class<T> targetAnnotation) {
        while (clz != Object.class) {
            T declaredAnnotation = clz.getDeclaredAnnotation(targetAnnotation);
            if (declaredAnnotation != null) {
                return declaredAnnotation;
            }

            Class<?>[] interfaces = clz.getInterfaces();
            for (Class<?> inter : interfaces) {
                declaredAnnotation = inter.getDeclaredAnnotation(targetAnnotation);
                if (declaredAnnotation != null) {
                    return declaredAnnotation;
                }
            }

            clz = clz.getSuperclass();
        }
        return null;
    }

    public static Method findAnnotationMethod(Method method, Class<? extends Annotation> targetAnnotation) {
        Annotation annotation = method.getAnnotation(targetAnnotation);
        if (annotation != null) {
            return method;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?>[] interfaces = declaringClass.getInterfaces();
        if (interfaces.length == 0) {
            return null;
        }
        for (Class<?> inter : interfaces) {
            try {
                Method interMethod = inter.getMethod(method.getName(), method.getParameterTypes());
                annotation = interMethod.getAnnotation(targetAnnotation);
                if (annotation != null) {
                    return interMethod;
                }
            } catch (NoSuchMethodException ignore) {
            }
        }
        return null;
    }

    public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotation) {
        Method annotationMethod = findAnnotationMethod(method, annotation);
        if (annotationMethod == null) {
            return null;
        }
        return annotationMethod.getAnnotation(annotation);
    }

}
