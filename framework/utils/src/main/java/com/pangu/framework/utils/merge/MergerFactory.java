/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pangu.framework.utils.merge;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 从Dubbo2.7.7-SNAPSHOT拷贝代码
 */
public class MergerFactory {

    private static final ConcurrentMap<Class<?>, Merger<?>> MERGER_CACHE =
            new ConcurrentHashMap<Class<?>, Merger<?>>();

    /**
     * Find the merger according to the returnType class, the merger will
     * merge an array of returnType into one
     *
     * @param returnType the merger will return this type
     * @return the merger which merges an array of returnType into one, return null if not exist
     * @throws IllegalArgumentException if returnType is null
     */
    public static <T> Merger<T> getMerger(Class<T> returnType) {
        if (returnType == null) {
            throw new IllegalArgumentException("returnType is null");
        }

        Merger result;
        if (returnType.isArray()) {
            Class type = returnType.getComponentType();
            result = MERGER_CACHE.get(type);
            if (result == null) {
                loadMergers();
                result = MERGER_CACHE.get(type);
            }
            if (result == null && !type.isPrimitive()) {
                result = ArrayMerger.INSTANCE;
            }
        } else {
            result = MERGER_CACHE.get(returnType);
            if (result == null) {
                loadMergers();
                result = MERGER_CACHE.get(returnType);
            }
        }
        return result;
    }

    static void loadMergers() {
        List<Merger<?>> mergers = Arrays.asList(
                new BooleanArrayMerger(),
                new ByteArrayMerger(),
                new CharArrayMerger(),
                new DoubleArrayMerger(),
                new FloatArrayMerger(),
                new IntArrayMerger(),
                new ListMerger(),
                new LongArrayMerger(),
                new MapMerger(),
                new SetMerger(),
                new ShortArrayMerger());
        for (Merger<?> m : mergers) {
            MERGER_CACHE.putIfAbsent(getGenericClass(m.getClass()), m);
        }
    }

    private static Class<?> getGenericClass(Class<?> cls) {
        try {
            ParameterizedType parameterizedType = ((ParameterizedType) cls.getGenericInterfaces()[0]);
            Object genericClass = parameterizedType.getActualTypeArguments()[0];

            // handle nested generic type
            if (genericClass instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) genericClass).getRawType();
            }

            // handle array generic type
            if (genericClass instanceof GenericArrayType) {
                return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
            }

            // Requires JDK 7 or higher, Foo<int[]> is no longer GenericArrayType
            if (((Class) genericClass).isArray()) {
                return ((Class) genericClass).getComponentType();
            }
            return (Class<?>) genericClass;
        } catch (Throwable e) {
            throw new IllegalArgumentException(cls.getName() + " generic type undefined!", e);
        }
    }

}
