package com.pangu.dbaccess.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityConfigParserTest {

    @Test
    public void test_parse_default() {
        EntityConfig parse = EntityConfigParser.parse(Entity.class);
        assertEquals("id", parse.getIdName());
        assertEquals("name", parse.getSingleRegionName());
        assertEquals(1, parse.getUniqueNames().size());
        assertEquals("unique", parse.getUniqueNames().toArray(new String[0])[0]);
    }

}