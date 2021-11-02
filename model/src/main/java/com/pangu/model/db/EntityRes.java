package com.pangu.model.db;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.Map;

@Transable
@Getter
public class EntityRes {
    // 字段列表
    private Map<String, Object> columns;
}
