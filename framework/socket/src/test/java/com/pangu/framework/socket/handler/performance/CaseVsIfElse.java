package com.pangu.framework.socket.handler.performance;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class CaseVsIfElse {
    @State(Scope.Thread)
    public static class FCase {
        public IType[] types = new IType[]{new A(), new B(), new C(), new D(), new E(), new F(), new G()};
        public Type t = Type.G;
    }

    @State(Scope.Thread)
    public static class FIfElse {
        public IType[] types = new IType[]{new A(), new B(), new C(), new D(), new E(), new F(), new G()};
        public IType t = new G();
    }

    @Benchmark
    public int testIfElse(FIfElse obj) {
        int count = 1;
        for (IType t1 : obj.types) {
            if (t1 instanceof A) {
                count += 1;
                continue;
            }
            if (t1 instanceof B) {
                count += 2;
                continue;
            }
            if (t1 instanceof C) {
                count += 3;
                continue;
            }
            if (t1 instanceof D) {
                count += 4;
                continue;
            }
            if (t1 instanceof E) {
                count += 5;
                continue;
            }
            if (t1 instanceof F) {
                count += 6;
                continue;
            }
            if (t1 instanceof G) {
                count += 7;
            }
        }
        return count;
    }

    @Benchmark
    public int testSwitch(FCase obj) {
        int count = 0;
        for (IType t : obj.types) {
            switch (t.get()) {

                case A:
                    count += 1;
                    continue;
                case B:
                    count += 2;
                    continue;
                case C:
                    count += 3;
                    continue;
                case D:
                    count += 4;
                    continue;
                case E:
                    count += 5;
                    continue;
                case F:
                    count += 6;
                    continue;
                case G:
                    count += 7;
            }
        }
        return count;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // 导入要测试的类
                .include(CaseVsIfElse.class.getSimpleName())
                // 预热5轮
                .warmupIterations(5)
                // 度量10轮
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    enum Type {
        A,
        B,
        C,
        D,
        E,
        F,
        G,
        ;
    }

    interface IType {
        Type get();
    }

    static class A implements IType {

        @Override
        public Type get() {
            return Type.A;
        }
    }

    static class B implements IType {

        @Override
        public Type get() {
            return Type.B;
        }
    }

    static class C implements IType {

        @Override
        public Type get() {
            return Type.C;
        }
    }

    static class D implements IType {

        @Override
        public Type get() {
            return Type.D;
        }
    }

    static class E implements IType {

        @Override
        public Type get() {
            return Type.E;
        }
    }

    static class F implements IType {

        @Override
        public Type get() {
            return Type.F;
        }
    }

    static class G implements IType {

        @Override
        public Type get() {
            return Type.G;
        }
    }
}
