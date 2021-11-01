package com.pangu.framework.socket.utils;

import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AnnotationUtilsTest {
    @Test
    public void test_findAnnotation_no_annotation() {
        SocketModule annotation = AnnotationUtils.findAnnotation(NoAnnotation.class, SocketModule.class);
        assertNull(annotation);
    }

    @Test
    public void test_findAnnotation_annotation_class() {
        SocketModule annotation = AnnotationUtils.findAnnotation(Annotation.class, SocketModule.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @Test
    public void test_findAnnotation_annotation_super_class() {
        SocketModule annotation = AnnotationUtils.findAnnotation(ClassWithSuperAnnotation.class, SocketModule.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @Test
    public void test_findAnnotation_annotation_interface() {
        SocketModule annotation = AnnotationUtils.findAnnotation(AnnotationInterface.class, SocketModule.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @Test
    public void test_findAnnotation_annotation_super_interface() {
        SocketModule annotation = AnnotationUtils.findAnnotation(ClassWithInterface.class, SocketModule.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @Test
    public void test_findAnnotation_method_no_annotation() throws NoSuchMethodException {
        Method method = NoAnnotation.class.getMethod("get");
        SocketCommand annotation = AnnotationUtils.findAnnotation(method, SocketCommand.class);
        assertNull(annotation);
    }

    @Test
    public void test_findAnnotation_method_annotation() throws NoSuchMethodException {
        Method method = Annotation.class.getMethod("get");
        SocketCommand annotation = AnnotationUtils.findAnnotation(method, SocketCommand.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @Test
    public void test_findAnnotation_method_interface_annotation() throws NoSuchMethodException {
        Method method = ClassWithInterface.class.getMethod("get");
        SocketCommand annotation = AnnotationUtils.findAnnotation(method, SocketCommand.class);
        assertNotNull(annotation);
        assertThat(annotation.value(), is(1));
    }

    @SocketModule(1)
    public class Annotation {

        @SocketCommand(1)
        public String get() {
            return null;
        }
    }

    public class ClassWithSuperAnnotation extends Annotation {
    }

    public class NoAnnotation {
        public String get() {
            return null;
        }
    }

    @SocketModule(1)
    public interface AnnotationInterface {
        @SocketCommand(1)
        String get();
    }

    public class ClassWithInterface implements AnnotationInterface {
        @Override
        public String get() {
            return null;
        }
    }
}