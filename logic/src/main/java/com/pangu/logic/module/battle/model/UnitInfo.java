package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

import java.util.stream.Collectors;

/**
 * 战斗单元信息
 */
@Transable
@Data
public class UnitInfo {

    /**
     * 标识
     */
    private String id;
    /**
     * 等级
     */
    private int level;
    /**
     * 模型信息
     */
    private ModelInfo model;

    /**
     * 生命
     */
    private long hp;
    /**
     * 生命上限
     */
    private long hpMax;
    /**
     * 怒气
     */
    private long mp;
    /**
     * 怒气上限
     */
    private long mpMax;

    /**
     * 位置
     */
    private Point point;

    /**
     * 技能列表
     */
    private String[] skillList;
    /**
     * 速度
     */
    private long moveSpeed;

    // 站位顺序
    private int sequence;

    /**
     * 构造方法
     */
    public static UnitInfo valueOf(Unit unit) {
        UnitInfo result = new UnitInfo();
        result.id = unit.getId();
        result.level = unit.getLevel();
        result.model = unit.getModel();
        result.hp = unit.getValue(UnitValue.HP);
        result.hpMax = unit.getValue(UnitValue.HP_MAX);
        result.mp = unit.getValue(UnitValue.MP);
        result.mpMax = unit.getValue(UnitValue.MP_MAX);
        Point point = unit.getPoint();
        if (point != null) {
            result.point = new Point(point.getX(), point.getY());
        }
        if (unit.getActiveSkills() != null) {
            result.skillList = unit.getActiveSkills().stream().map(SkillState::getId).toArray(String[]::new);
        }
        result.moveSpeed = unit.getValue(UnitValue.SPEED);
        result.sequence = unit.getSequence();
        return result;
    }
}
