package com.pangu.dbaccess.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
public class EntityConfig {

    // 表名
    private String tableName;

    // ID列名
    private String idName;

    // 单个区域缓存
    private String singleRegionName;

    // 区域实体 列名 列表
    private Set<String> regionNames;

    // 唯一键 列名 列表
    private Set<String> uniqueNames;

    private List<FieldDesc> fieldDesc;

    public void setSingleRegionName(String singleRegionName) {
        if (this.singleRegionName == null) {
            this.singleRegionName = singleRegionName;
            return;
        }
        this.regionNames = new HashSet<>(2);
        this.regionNames.add(this.singleRegionName);
        this.regionNames.add(singleRegionName);
        this.singleRegionName = null;
    }

    public void addUniqueField(String fieldName) {
        if (this.uniqueNames == null) {
            this.uniqueNames = new HashSet<>(2);
        }
        this.uniqueNames.add(fieldName);
    }

    public void checkValid() {
        if (StringUtils.isEmpty(tableName)) {
            throw new IllegalStateException("实体表名设置异常" + tableName);
        }
        if (StringUtils.isEmpty(idName)) {
            throw new IllegalStateException("实体[" + tableName + "]没有找到Id字段");
        }
    }
}
