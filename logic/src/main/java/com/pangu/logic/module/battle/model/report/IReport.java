package com.pangu.logic.module.battle.model.report;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 战报抽象接口
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuffReport.class, name = "BUF"),
        @JsonSubTypes.Type(value = MoveReport.class, name = "MOVE"),
        @JsonSubTypes.Type(value = MpReport.class, name = "MP"),
        @JsonSubTypes.Type(value = SkillReport.class, name = "SKILL"),
})
public interface IReport {
}
