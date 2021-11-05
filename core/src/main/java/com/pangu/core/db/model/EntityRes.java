package com.pangu.core.db.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Transable
@Getter
@ToString
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
