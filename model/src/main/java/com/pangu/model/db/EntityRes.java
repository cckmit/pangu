package com.pangu.model.db;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.Map;

@Transable
@Getter
public class EntityRes {
    // 字段列表
    private Map<String, Object> columns;

    private boolean error;

    private String msg;

    public static EntityRes of(Map<String, Object> row) {
        EntityRes r = new EntityRes();
        r.columns = row;
        return r;
    }

    public static EntityRes err(String message) {
        EntityRes r = new EntityRes();
        r.error = true;
        r.msg = message;
        return r;
    }
}
