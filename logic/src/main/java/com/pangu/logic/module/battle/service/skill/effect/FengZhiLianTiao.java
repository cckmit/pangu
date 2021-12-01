package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.FengZhiLianTiaoParam;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 风暴女皇·艾琳技能：风之链条
 * 战斗开始时,将最近与最远的敌人链接连接双方其中一个敌入受到伤害时,其伤害量的40%会传递给另一个敌人
 * 2级:被连接的双方,其中一名敌人死亡时,另一名敌入将会被小旋风吹起来,持续3秒
 * 3级:连接双方其中一个敌人受到伤害时,传递的伤害量提升至55%
 * 4级:被链接的敌人,魔防降低20%
 *
 * @author Kubby
 */
@Component
public class FengZhiLianTiao implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.FENG_ZHI_LIAN_TIAO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {

        if (skillState.getAddition() != null) {
            return;
        }

        Unit minDisUnit = null;
        Unit maxDisUnit = null;
        int minDis = -1;
        int maxDis = -1;
        for (Unit enemy : owner.getEnemy().getCurrent()) {

            if (!enemy.getPassiveStateByType(PassiveType.FENG_ZHI_LIAN_TIAO).isEmpty()) {
                continue;
            }
            int distance = owner.getPoint().distance(enemy.getPoint());
            if (minDisUnit == null || distance < minDis) {
                minDisUnit = enemy;
                minDis = distance;
            }
            if (maxDisUnit == null || distance > maxDis) {
                maxDisUnit = enemy;
                maxDis = distance;
            }
        }

        if (minDisUnit == null || minDisUnit == maxDisUnit) {
            return;
        }

        FengZhiLianTiaoParam param = state.getParam(FengZhiLianTiaoParam.class);

        FengZhiLianTiaoAddition addition = FengZhiLianTiaoAddition
                .of(skillState, param.getFactor(), param.getXuanFengSkillId(), minDisUnit, maxDisUnit);

        PassiveState passiveState = PassiveFactory.initState(param.getPassiveId(), time);
        passiveState.setAddition(addition);

        minDisUnit.addPassive(passiveState, owner);
        maxDisUnit.addPassive(passiveState, owner);


        BuffFactory.addBuff(param.getLinkBuffId(), owner, minDisUnit, time, skillReport, null);
        BuffFactory.addBuff(param.getLinkBuffId(), owner, maxDisUnit, time, skillReport, null);
        addition.setLinkBuffId(param.getLinkBuffId());

        if (!StringUtils.isBlank(param.getBuffId())) {
            BuffFactory.addBuff(param.getBuffId(), owner, minDisUnit, time, skillReport, null);
            BuffFactory.addBuff(param.getBuffId(), owner, maxDisUnit, time, skillReport, null);
            addition.setBuffId(param.getBuffId());
        }
    }

    @Getter
    public static class FengZhiLianTiaoAddition {

        private SkillState skillState;

        private String xuanFengSkillId;

        private double factor;

        private Unit unit1;

        private Unit unit2;

        @Setter
        private String linkBuffId;
        @Setter
        private String buffId;

        public Unit getOther(Unit unit) {
            if (unit == unit1) {
                return unit2;
            }
            if (unit == unit2) {
                return unit1;
            }
            return null;
        }

        public static FengZhiLianTiaoAddition of(SkillState skillState, double factor, String xuanFengSkillId,
                                                 Unit unit1, Unit unit2) {
            FengZhiLianTiaoAddition addition = new FengZhiLianTiaoAddition();
            addition.skillState = skillState;
            addition.factor = factor;
            addition.xuanFengSkillId = xuanFengSkillId;
            addition.unit1 = unit1;
            addition.unit2 = unit2;
            return addition;
        }
    }
}
