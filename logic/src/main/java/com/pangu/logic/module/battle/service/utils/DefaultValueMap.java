package com.pangu.logic.module.battle.service.utils;

import com.pangu.framework.utils.rhino.Rhino;

import java.util.HashMap;

public class DefaultValueMap extends HashMap<String, Object> {
    public final HashMap<String, Object> ctx;

    public DefaultValueMap(HashMap<String, Object> ctx) {
        super(0);
        this.ctx = ctx;
    }

    @Override
    public Object get(Object key) {
        Object o = ctx.get(key);
        if (o == null) {
            return 0.0;
        }
        return o;
    }

    @Override
    public boolean containsKey(Object key) {
        return !Rhino.isConst(key.toString());
    }

    @Override
    public String toString() {
        return ctx.toString();
    }
}