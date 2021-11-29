package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.XiongEJianTaParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 凶恶践踏：
 * 造成伤害时有20%触发践踏，对目标周围圆形范围内的敌人额外造成造成125%的物理伤害,每1秒只能触发1次
 * 2级：伤害提升至150%
 * 3级：伤害提升至175%
 * 4级：概率提升至40%
 */
@Component
public class XiongEJianTa implements AttackPassive {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        XiongEJianTaParam param = passiveState.getParam(XiongEJianTaParam.class);
        if (!RandomUtils.isHit(param.getRate())) {
            return;
        }
        List<Unit> enemies = FilterType.ENEMY.filter(owner, time);

        EffectState effectState = skillState.getEffectStates().get(0);
        effectState.setParamOverride(new DamageParam(param.getFactor()));
        int range = param.getRange();
        Point targetPoint = target.getPoint();
        for (Unit item : enemies) {
            if (targetPoint.distance(item.getPoint()) > range) {
                continue;
            }
            PassiveUtils.hpPhysicsDamage(physicsDamage, owner, item, skillState, effectState, time, context, skillReport, passiveState);
        }
        effectState.setParamOverride(null);
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.XIONG_E_JIAN_TA;
    }
}
