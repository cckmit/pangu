package com.pangu.framework.utils.id;

import com.pangu.framework.utils.time.DateUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdGeneratorTest {

    @Test
    public void test_hour_count() {
        int BASE_UNIT = 2 * 60 * 1000;

        long from = DateUtils.string2Date("2020-01-01", DateUtils.PATTERN_DATE).getTime();
        System.out.println(from);
        // 当前值
        long cur = System.currentTimeMillis();
        long count = (cur - from) / BASE_UNIT;
        assertTrue(Long.toBinaryString(count).length() <= 21);

        cur = DateUtils.string2Date("2027-01-01", DateUtils.PATTERN_DATE).getTime();
        System.out.println(cur);
        count = (cur - from) / BASE_UNIT;
        assertTrue(Long.toBinaryString(count).length() <= 21);

    }

    @Test
    public void test_get_next() {
        long[] limits = IdGenerator.getLimits(500, 999);
        IdGenerator idGenerator = new IdGenerator(500, 999, limits[0]);
        long pre = 0;
        for (int i = 0; i < 524287; ++i) {
            long cur = idGenerator.getNext();
            if (pre == 0) {
                pre = cur;
                continue;
            }
            assertEquals(cur - pre, 1L);
            pre = cur;
        }
    }

    @Test
    public void test_get_next_expand_max() {
        long[] limits = IdGenerator.getLimits(500, 999);
        IdGenerator idGenerator = new IdGenerator(500, 999, limits[0]);
        long pre = 0;
        for (int i = 0; i < 524287 + 1; ++i) {
            long cur = idGenerator.getNext();
            if (pre == 0) {
                pre = cur;
                continue;
            }
            if (cur > pre) {
                assertEquals(cur - pre, 1L);
            } else {
                // 超过上限一个数，则代表从0开始
                assertEquals((cur - pre), -524287);
            }

            pre = cur;
        }
    }
}