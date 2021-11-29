package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.WuShuangTieBiParam;
import org.springframework.stereotype.Component;

/**
 * 无双铁壁：
 * 每次盾击结束之后，会进入举盾状态，持续4秒，提升自身30%的免伤效果,同时大幅度降低自身攻击速度.释放必杀技后，解除该状态
 * 2级:免伤效果提升40%
 * 3级:同时反弹受到的30%的魔法伤害
 * 4級:免伤效果提升50%
 */
@Component
public class WuShuangTieBi implements SkillReleasePassive, DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.WU_SHUANG_TIE_BI;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        WuShuangTieBiParam param = passiveState.getParam(WuShuangTieBiParam.class);
        String tag = skillState.getTag();
        if (param.getStartSkillTag().equals(tag)) {
            String buff = param.getBuff();
            BuffState buffState = BuffFactory.addBuff(buff, owner, owner, time, skillReport, null);
            if (buffState == null) {
                return;
            }
            passiveState.setAddition(time + buffState.getTime());
            return;
        }
        if (param.getEndSkillTag() != null && param.getEndSkillTag().equals(tag)) {
            String buff = param.getBuff();
            BuffFactory.removeBuffState(buff, owner, time);
            passiveState.setAddition(0);
        }
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        Integer validTime = passiveState.getAddition(int.class);
        if (validTime == null) {
            return;
        }
        if (validTime < time) {
            return;
        }
        WuShuangTieBiParam param = passiveState.getParam(WuShuangTieBiParam.class);
        final long hpChange = context.getHpChange(owner);
        long reduce = -(long) (param.getReduceDamageRate() * hpChange);
        if (reduce != 0) {
            skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(reduce)));
            context.addPassiveValue(owner, AlterType.HP, reduce);
        }
        long reflect = (long) (param.getReflectRate() * hpChange);
        if (reflect != 0 && context.isMagic(owner)) {
            skillReport.add(time, attacker.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(reflect)));
            context.addPassiveValue(attacker, AlterType.HP, reduce);
        }
    }
}
