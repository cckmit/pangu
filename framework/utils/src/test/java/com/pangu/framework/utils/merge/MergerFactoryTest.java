package com.pangu.framework.utils.merge;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class MergerFactoryTest {

    @Test
    public void test_long_array() {
        Merger<long[]> merger = MergerFactory.getMerger(long[].class);
        long[][] values = {new long[]{1L, 2L}, new long[]{3, 4}, new long[]{5, 6}};
        long[] result = merger.merge(values);
        assertArrayEquals(new long[]{1, 2, 3, 4, 5, 6}, result);
    }

    @Test
    public void test_int_array() {
        Merger<int[]> merger = MergerFactory.getMerger(int[].class);
        int[][] values = {new int[]{1, 2}, new int[]{3, 4}, new int[]{5, 6}};
        int[] result = merger.merge(values);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, result);
    }

    @Test
    public void test_object_array() {
        Merger<Object[]> merger = MergerFactory.getMerger(Object[].class);
        Object[][] values = {new Object[]{1, 2}, new Object[]{3, 4}, new Object[]{5, 6}};
        Object[] result = merger.merge(values);
        assertArrayEquals(new Object[]{1, 2, 3, 4, 5, 6}, result);
    }

    @Test
    public void test_list() {
        Merger<List> merger = MergerFactory.getMerger(List.class);
        List[] values = {Arrays.asList(Model.of(1), Model.of(2)),
                Arrays.asList(Model.of(3), Model.of(4)),
                Arrays.asList(Model.of(5), Model.of(6), Model.of(7), Model.of(8))};
        List list = merger.merge(values);
        assertEquals(Arrays.asList(Model.of(1), Model.of(2), Model.of(3), Model.of(4), Model.of(5), Model.of(6), Model.of(7), Model.of(8)),
                list);
    }

    @Test
    public void test_Set() {
        Merger<Set> merger = MergerFactory.getMerger(Set.class);
        HashSet<Integer> v1 = new HashSet<>(Arrays.asList(1, 2));
        HashSet<Integer> v2 = new HashSet<>(Arrays.asList(3, 4));
        HashSet<Integer> v3 = new HashSet<>(Arrays.asList(5, 6, 7, 8));
        Set[] values = {v1, v2, v3};
        Set set = merger.merge(values);
        assertEquals((v1.size() + v2.size() + v3.size()), set.size());
        set.removeAll(v1);
        set.removeAll(v2);
        set.removeAll(v3);
        assertEquals(0, set.size());
    }

    @Test
    public void test_map() {
        Merger<Map> merger = MergerFactory.getMerger(Map.class);
        Map<Integer, Model> v1 = Collections.singletonMap(1, Model.of(1));
        Map<Integer, Model> v2 = Collections.singletonMap(2, Model.of(2));
        Map<Integer, Model> v3 = Collections.singletonMap(3, Model.of(3));
        Map[] values = {v1, v2, v3};
        Map set = merger.merge(values);
        assertEquals((v1.size() + v2.size() + v3.size()), set.size());
        assertTrue(set.remove(1, Model.of(1)));
        assertTrue(set.remove(2, Model.of(2)));
        assertTrue(set.remove(3, Model.of(3)));
        assertEquals(0, set.size());
    }

    private static class Model {
        int value;

        public Model(int value) {
            this.value = value;
        }

        public static Model of(int v) {
            return new Model(v);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Model model = (Model) o;
            return value == model.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "v=" + value + " ";
        }
    }

}