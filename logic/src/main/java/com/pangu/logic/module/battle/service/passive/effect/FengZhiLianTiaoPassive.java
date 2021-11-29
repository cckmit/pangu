package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.effect.FengZhiLianTiao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 风暴女皇·艾琳技能：风之链条
 * 战斗开始时,将最近与最远的敌人链接连接双方其中一个敌入受到伤害时,其伤害量的40%会传递给另一个敌人
 * 2级:被连接的双方,其中一名敌人死亡时,另一名敌入将会被小旋风吹起来,持续3秒
 * 3级:连接双方其中一个敌人受到伤害时,传递的伤害量提升至55%
 * 4级:被链接的敌人,魔防降低20%
 * @author Kubby
 */
@Component
public class FengZhiLianTiaoPassive implements DamagePassive, OwnerDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.FENG_ZHI_LIAN_TIAO;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {

        FengZhiLianTiao.FengZhiLianTiaoAddition addition = passiveState
                .getAddition(FengZhiLianTiao.FengZhiLianTiaoAddition.class);

        Unit other = addition.getOther(owner);

        long otherDamage = (long) (damage * addition.getFactor());

        context.addPassiveValue(other, AlterType.HP, otherDamage);
        skillReport.add(time, other.getId(),
                PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.HP, otherDamage)));
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time,
                    Context context) {
        /* 其中一个死亡后，链接相关信息清除 */

        FengZhiLianTiao.FengZhiLianTiaoAddition addition = passiveState
                .getAddition(FengZhiLianTiao.FengZhiLianTiaoAddition.class);

        SkillState skillState = addition.getSkillState();
        skillState.setAddition(null);

        Unit other = addition.getOther(owner);

        addition.getUnit1().removePassive(passiveState);
        addition.getUnit2().removePassive(passiveState);

        /* 清除链接BUFF */
        BuffFactory.removeBuffState(addition.getLinkBuffId(), addition.getUnit1(), time);
        BuffFactory.removeBuffState(addition.getLinkBuffId(), addition.getUnit2(), time);

        /* 清除链接时添加的BUFF */
        if (!StringUtils.isBlank(addition.getBuffId())) {
            BuffFactory.removeBuffState(addition.getBuffId(), addition.getUnit1(), time);
            BuffFactory.removeBuffState(addition.getBuffId(), addition.getUnit2(), time);
        }

        /* 其中一个死亡后，另一个要被旋风吹起来 */
        if (!other.isDead() && !StringUtils.isBlank(addition.getXuanFengSkillId())) {
            SkillFactory.updateNextExecuteSkill(time, other, addition.getXuanFengSkillId());
        }

    }
}
