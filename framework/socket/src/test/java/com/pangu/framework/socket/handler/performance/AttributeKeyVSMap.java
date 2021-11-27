package com.pangu.framework.socket.handler.performance;

import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;

public class AttributeKeyVSMap {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(AttributeKeyVSMap.class.getSimpleName())
                // 预热5轮
                .warmupIterations(5)
                // 度量10轮
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public Object testAttributeGetAndSet(ChannelState state) {
        Attribute<String> attr = state.channel.attr(state.keyAtt);
        attr.set("test");
        return attr;
    }

    @Benchmark
    public Object testMapGetAndSet(MapState state) {
        return state.map.computeIfAbsent(state.keyStr, k -> "test");
    }

    @State(Scope.Thread)
    public static class ChannelState {
        public final AttributeKey<String> keyAtt = AttributeKey.newInstance("key");
        public final String keyStr = "key";
        public NioSocketChannel channel = new NioSocketChannel();
    }

    @State(Scope.Thread)
    public static class MapState {
        public final String keyStr = "key";
        public HashMap<String, String> map = new HashMap<>();
    }
}
