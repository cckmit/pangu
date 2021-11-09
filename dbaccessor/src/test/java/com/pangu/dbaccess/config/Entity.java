package com.pangu.dbaccess.config;

import com.pangu.dbaccess.anno.Idx;
import com.pangu.dbaccess.anno.Unique;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Map;

@javax.persistence.Entity
@Getter
@Setter(AccessLevel.PROTECTED)
public class Entity {

    @Id
    long id;

    @Idx
    @Column(name = "dbColName")
    String name;

    Map<String, Integer> baseIds;

    @Unique
    long unique;
}
