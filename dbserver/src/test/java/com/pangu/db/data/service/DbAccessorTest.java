package com.pangu.db.data.service;

import com.pangu.db.config.DbConfig;
import com.pangu.db.config.DbConfigBean;
import com.pangu.core.config.JdbcConfig;
import com.pangu.core.db.EntityRes;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DbAccessorTest {

    private DbAccessor dbAccessor;

    @Before
    public void setUp() {
        DbConfigBean dbConfigBean = new DbConfigBean();
        DbConfig dbConfig = dbConfigBean.getObject();
        JdbcConfig jdbc = dbConfig.getJdbc();
        Flyway flyway = Flyway.configure()
                .schemas(jdbc.getDatabasePrefix() + "test_db")
                .locations("classpath:db/dbcachetest")
                .dataSource(jdbc.getUrlPrefix() + jdbc.getAddr() + jdbc.getParams(), jdbc.getUsername(), jdbc.getPassword())
                .load();
        flyway.clean();
        flyway.migrate();
        dbAccessor = new DbAccessor("test_db");
        dbAccessor.setDbConfig(dbConfig);
        dbAccessor.init();
    }

    @Test
    public void test_queryById() throws SQLException {
        EntityRes entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", 1);
        Map<String, Object> columns = entityRes.getColumns();
        assertEquals(7, columns.size());
    }

    @Test
    public void test_insert_exact_type() throws SQLException {
        Map<String, Object> columns = new HashMap<>();
        int i = 2;
        columns.put("iInt", i);
        boolean t = true;
        columns.put("bBit", t);
        String s = "string value";
        columns.put("vVarchar", s);
        long now = (System.currentTimeMillis() / 1000) * 1000;
        Date d = new Date(now);
        columns.put("dDateTime", d);
        long l = Long.MAX_VALUE;
        columns.put("bBigint", l);
        String lt = "this is long text";
        columns.put("lLongText", lt);
        byte[] lb = new byte[10];
        Arrays.fill(lb, (byte) 1);
        columns.put("lLongblob", lb);
        dbAccessor.insert("MultiTypeTable", columns);

        EntityRes entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", i);
        Map<String, Object> res = entityRes.getColumns();
        assertNotNull(res);
        assertEquals(i, res.get("iInt"));
        assertEquals(t, res.get("bBit"));
        assertEquals(s, res.get("vVarchar"));
        Object dDateTime = res.get("dDateTime");
        assertSame(Date.class, dDateTime.getClass());
        long expected = d.getTime() / 1000;
        assertEquals(expected, ((Date) dDateTime).getTime() / 1000);
        assertEquals(l, res.get("bBigint"));
        assertEquals(lt, res.get("lLongText"));
        assertArrayEquals(lb, (byte[]) res.get("lLongblob"));
    }

    @Test
    public void test_insert_similar_type() throws SQLException {
        Map<String, Object> columns = new HashMap<>();
        short i = 2;
        columns.put("iInt", i);
        boolean t = true;
        columns.put("bBit", t);
        String s = "string value";
        columns.put("vVarchar", s);
        long now = (System.currentTimeMillis() / 1000) * 1000;
        Date d = new Date(now);
        assertEquals(now, d.getTime());
        Timestamp timestamp = new Timestamp(now);
        assertEquals(now, timestamp.getTime());
        columns.put("dDateTime", d);
        int l = Integer.MAX_VALUE;
        columns.put("bBigint", l);
        String lt = "this is long text";
        columns.put("lLongText", lt);
        byte[] lb = new byte[10];
        Arrays.fill(lb, (byte) 1);
        columns.put("lLongblob", lb);
        dbAccessor.insert("MultiTypeTable", columns);

        EntityRes entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", i);
        Map<String, Object> res = entityRes.getColumns();
        assertNotNull(res);
        assertEquals((int) i, res.get("iInt"));
        assertEquals(t, res.get("bBit"));
        assertEquals(s, res.get("vVarchar"));
        Object dDateTime = res.get("dDateTime");

        assertSame(Date.class, dDateTime.getClass());
        long expected = d.getTime() / 1000;
        assertEquals(expected, ((Date) dDateTime).getTime() / 1000);
        assertEquals((long) l, res.get("bBigint"));
        assertEquals(lt, res.get("lLongText"));
        assertArrayEquals(lb, (byte[]) res.get("lLongblob"));
    }

    @Test
    public void test_update() throws SQLException {
        Map<String, Object> columns = new HashMap<>();
        short i = 1;
        boolean t = true;
        columns.put("bBit", t);
        String s = "string value";
        columns.put("vVarchar", s);
        long now = (System.currentTimeMillis() / 1000) * 1000;
        Date d = new Date(now);
        columns.put("dDateTime", d);

        dbAccessor.update("MultiTypeTable", "iInt", i, columns);

        EntityRes entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", i);
        Map<String, Object> res = entityRes.getColumns();
        assertNotNull(res);
        assertEquals((int) i, res.get("iInt"));
        assertEquals(t, res.get("bBit"));
        assertEquals(s, res.get("vVarchar"));
        Object dDateTime = res.get("dDateTime");

        assertSame(Date.class, dDateTime.getClass());
        long expected = d.getTime() / 1000;
        assertEquals(expected, ((Date) dDateTime).getTime() / 1000);
    }

    @Test
    public void test_delete() throws SQLException {
        int id = 1;
        EntityRes entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", id);
        assertNotNull(entityRes.getColumns());
        assertNotEquals(0, entityRes.getColumns().size());

        dbAccessor.delete("MultiTypeTable", "iInt", id);

        entityRes = dbAccessor.queryById("MultiTypeTable", "iInt", id);
        assertNull(entityRes.getColumns());

    }
}