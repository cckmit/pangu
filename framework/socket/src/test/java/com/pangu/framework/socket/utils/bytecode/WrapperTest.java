package com.pangu.framework.socket.utils.bytecode;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class WrapperTest {

    @Test
    public void test_wrapper() throws java.lang.NoSuchMethodException, InvocationTargetException {
        Wrapper wrapper = Wrapper.getWrapper(FacadeImpl.class);
        FacadeImpl facade = new FacadeImpl();
        Method method = FacadeImpl.class.getDeclaredMethod("hello", int.class);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object hello = wrapper.invokeMethod(facade, "hello", parameterTypes, new Object[]{1});
        assertEquals(hello, "hello:1");
    }

    @SocketModule(1)
    private interface Facade {
        @SocketCommand(2)
        String hello(@InBody int value);
    }

    static class FacadeImpl implements Facade {

        @Override
        public String hello(int value) {
            return "hello:" + value;
        }
    }
}