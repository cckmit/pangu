package com.pangu.framework.socket.utils.bytecode;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MethodCallJmhTest {

    @State(Scope.Benchmark)
    public static class ChannelState {
        final Class<?>[] parameterTypes;
        final Method method;
        final String name = "hello";
        final FacadeImpl obj = new FacadeImpl();
        private final Wrapper wrapper;
        private final Facade facade;
        private final Facade javassistFacade;

        public ChannelState() {
            Method mm = null;
            try {
                mm = FacadeImpl.class.getDeclaredMethod(name, int.class);
            } catch (java.lang.NoSuchMethodException e) {
                e.printStackTrace();
            }
            this.method = mm;
            parameterTypes = method.getParameterTypes();
            wrapper = Wrapper.getWrapper(FacadeImpl.class);

            facade = (Facade) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Facade.class}, new InvokeHandlder());
            javassistFacade = (Facade) com.pangu.framework.socket.utils.bytecode.Proxy.getProxy(Facade.class).newInstance(new InvokeHandlder());

        }

        static class InvokeHandlder implements InvocationHandler {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return "hello" + args[0];
            }
        }
    }

    @Benchmark
    public Object test_javassist(ChannelState state) throws InvocationTargetException {
        return state.wrapper.invokeMethod(state.obj, state.name, state.parameterTypes, new Object[]{1});
    }

    @Benchmark
    public Object test_reflect_invoke(ChannelState state) throws InvocationTargetException, IllegalAccessException {
        return state.method.invoke(state.obj, 1);
    }

    @Benchmark
    public Object test_direct(ChannelState state) throws InvocationTargetException, IllegalAccessException {
        return state.obj.hello(1);
    }

    @Benchmark
    public Object test_java_proxy(ChannelState state) throws InvocationTargetException, IllegalAccessException {
        return state.facade.hello(1);
    }

    @Benchmark
    public Object test_javassist_proxy(ChannelState state) throws InvocationTargetException, IllegalAccessException {
        return state.javassistFacade.hello(1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(MethodCallJmhTest.class.getSimpleName())
                // 预热5轮
                .warmupIterations(5)
                // 度量10轮
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
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
