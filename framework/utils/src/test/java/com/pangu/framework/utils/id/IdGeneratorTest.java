package com.pangu.framework.utils.id;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdGeneratorTest {
    @Test
    public void test_get_next() {
        long[] limits = IdGenerator.getLimits(255, 999);
        IdGenerator idGenerator = new IdGenerator(255, 999, limits[0]);
        long pre = 0;
        for (int i = 0; i < 600_0000; ++i) {
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
    public void test_parse() {
        int oid = 255;
        int sid = 8000;
        long idx = 1L;
        IdGenerator idGenerator = new IdGenerator(oid, sid, idx);
        long next = idGenerator.getNext();
        long[] limits = IdGenerator.getLimits(oid, sid);
        assertTrue(next >= limits[0] && next <= limits[1]);
        IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(next);
        assertEquals(oid, idInfo.getOperator());
        assertEquals(sid, idInfo.getServer());
        assertEquals(idx + 1, idInfo.getIncrease());
    }

    @Test
    public void test_oid_limit() {
        for (int oid = 0; oid < ((1 << IdGenerator.operatorBit) - 1); ++oid) {
            IdGenerator idGenerator = new IdGenerator(oid, 1, 0L);
            long next = idGenerator.getNext();
            assertEquals(oid, (next >> (IdGenerator.serverBit + IdGenerator.versionBit + IdGenerator.idxBit)));
        }
    }

    @Test
    public void test_sid_limit() {
        for (int sid = 0; sid < ((1 << IdGenerator.serverBit) - 1); ++sid) {
            IdGenerator idGenerator = new IdGenerator(1, sid, 0L);
            long next = idGenerator.getNext();
            assertEquals(sid, (next >> (IdGenerator.versionBit + IdGenerator.idxBit)) & ((1 << IdGenerator.serverBit) - 1));
        }
    }
}