package com.pangu.logic.module.battle.model.report.values;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 属性并更操作集合
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReviveValue.class, name = "REVIVE"),
        @JsonSubTypes.Type(value = BuffAdd.class, name = "BUFF_ADD"),
        @JsonSubTypes.Type(value = Mark.class, name = "MARK"),
        @JsonSubTypes.Type(value = RemovePassive.class, name = "REMOVE_PASSIVE"),
        @JsonSubTypes.Type(value = TransformValue.class, name = "TRANSFORM"),
        @JsonSubTypes.Type(value = AddPassive.class, name = "ADD_PASSIVE"),
        @JsonSubTypes.Type(value = Hp.class, name = "HP"),
        @JsonSubTypes.Type(value = Immune.class, name = "IMMUNE"),
        @JsonSubTypes.Type(value = Miss.class, name = "MISS"),
        @JsonSubTypes.Type(value = Mp.class, name = "MP"),
        @JsonSubTypes.Type(value = PassiveValue.class, name = "PASSIVE"),
        @JsonSubTypes.Type(value = PositionChange.class, name = "POSITION"),
        @JsonSubTypes.Type(value = UnitValues.class, name = "UNIT_VALUE"),
        @JsonSubTypes.Type(value = StateRemove.class, name = "STATE_REMOVE"),
        @JsonSubTypes.Type(value = StateAdd.class, name = "STATE_ADD"),
        @JsonSubTypes.Type(value = SummonRemove.class, name = "SUMMON_REMOVE"),
        @JsonSubTypes.Type(value = SummonUnits.class, name = "SUMMON_UNIT"),
        @JsonSubTypes.Type(value = ItemAdd.class, name = "ITEM_ADD"),
        @JsonSubTypes.Type(value = ItemMove.class, name = "ITEM_MOVE"),
        @JsonSubTypes.Type(value = ItemRemove.class, name = "ITEM_REMOVE"),
        @JsonSubTypes.Type(value = Death.class, name = "DEATH"),
        @JsonSubTypes.Type(value = Follow.class, name = "Follow"),
})
public interface IValues {

}
