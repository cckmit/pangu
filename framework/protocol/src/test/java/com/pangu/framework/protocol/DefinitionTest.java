package com.pangu.framework.protocol;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DefinitionTest {

    private List<Long> ids;

    private Map<Integer, Long> mapLong;

    @Test
    public void test_convert() throws NoSuchFieldException {
        Field idsField = DefinitionTest.class.getDeclaredField("ids");
        Type longListType = idsField.getGenericType();
        Field mapField = DefinitionTest.class.getDeclaredField("mapLong");
        Type mapLongType = mapField.getGenericType();

        Definition definition = new Definition();

        long l = 18016117872132097L;
        List<Long> listLongOri = Arrays.asList(l, l, l);

        String lStr = "18016117872132097";
        Long ret = definition.convert(lStr, long.class);
        assertEquals(ret.longValue(), Long.parseLong(lStr));

        String[] idsStr = new String[]{lStr, lStr, lStr};
        long[] convert = definition.convert(idsStr, long[].class);
        assertArrayEquals(convert, new long[]{l, l, l});

        List<Long> arrayToList = definition.convert(idsStr, longListType);
        assertEquals(arrayToList, listLongOri);

        List<String> listStr = Arrays.asList(lStr, lStr, lStr);
        List<Long> listLong = definition.convert(listStr, longListType);
        assertEquals(listLong, listLongOri);

        Map<Integer, String> mapStr = Collections.singletonMap(1, lStr);
        Map<Integer, Long> mapLong = definition.convert(mapStr, mapLongType);
        assertEquals(mapLong, Collections.singletonMap(1, l));
    }
}