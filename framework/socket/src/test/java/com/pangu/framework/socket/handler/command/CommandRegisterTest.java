package com.pangu.framework.socket.handler.command;

import com.pangu.framework.socket.handler.param.Parameters;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import static org.junit.Assert.*;

public class CommandRegisterTest {

    @Test
    public void test_register() {
        List<MethodDefine> methodDefines = CommandRegister.toMethodDefine(TestModuleDefineClass.class);
        assertEquals(1, methodDefines.size());
        MethodDefine methodDefine = methodDefines.get(0);
        Parameters params = methodDefine.getParams();
        assertEquals(3, params.getParameters().length);
    }

    @Test
    public void test_get_method_name_by_default() throws NoSuchMethodException {
        Method method = CommandRegisterTest.class.getMethod("method", String.class);
        Parameter[] parameters = method.getParameters();
        assertTrue(parameters[0].isNamePresent());
    }

    @Test
    public void test_get_method_name_by_asm() throws NoSuchMethodException {
        Method method = CommandRegisterTest.class.getMethod("method", String.class);
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameters = discoverer.getParameterNames(method);
        assertNotNull(parameters);
        assertEquals("paramA", parameters[0]);
    }

    public void method(String paramA) {

    }
}