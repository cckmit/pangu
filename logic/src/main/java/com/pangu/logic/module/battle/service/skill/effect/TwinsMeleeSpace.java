package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.TwinsMeleeSpaceParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * "引动万象星辰的力量将剑巨大化，对敌方全体施展出三次斩击，每一击对敌人造成150%攻击力伤害，随着自身星辰之力的大小会额外附加不同的效果：
 * 当星辰之力大于30时，造成伤害的5%将被用于治疗自身；
 * 当星辰之力大于50时，对被控制的敌人伤害增加10%；
 * 当星辰之力大于80时，释放大招时增加自身10%暴击伤害
 * 2级：造成伤害的15%将被用于治疗自身
 * 3级：对被剑气控制的敌人伤害增加20%
 * 4级：释放大招时增加自身40%暴击伤害，最后一击必定暴击"
 */
@Component
public class TwinsMeleeSpace implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.TWINS_MELEE_SPACE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final TwinsMeleeSpaceParam param = state.getParam(TwinsMeleeSpaceParam.class);
        final SkillEffectAction sea = context.getRootSkillEffectAction();
        final int loopTimes = context.getLoopTimes();
        //  造成伤害
        DamageParam dmgParam;
        if (param.isCrit() && loopTimes == sea.getTotalExecTimes()) {
            dmgParam = param.getDmgParam().copy();
            dmgParam.setCritExp("1==1");
        } else {
            dmgParam = param.getDmgParam();
        }

        state.setParamOverride(dmgParam);
        //  增加暴击伤害
        if (owner.getValue(UnitValue.EP) > param.getCritDmgEp()) {
            owner.increaseRate(UnitRate.CRIT_DAMAGE, param.getCritDmgRate());
        }
        magicDamage.execute(state, owner, target, skillReport, time, skillState, context);
        if (owner.getValue(UnitValue.EP) > param.getCritDmgEp()) {
            owner.increaseRate(UnitRate.CRIT_DAMAGE, -param.getCritDmgRate());
        }
        state.setParamOverride(null);

        //  对控制单位增伤
        if (owner.getValue(UnitValue.EP) > param.getDmgEp() && target.underControl(time)) {
            long hpChange = (long) (context.getHpChange(target) * param.getDmgRate());
            context.addValue(target, AlterType.HP, hpChange);
            skillReport.add(time, target.getId(), Hp.of(hpChange));
        }

        //  吸血
        if (owner.getValue(UnitValue.EP) > param.getSuckEp()) {
            final long hpChange = context.getHpChange(target);
            final long suck = -(long) (hpChange * param.getSuckRate());
            context.addValue(owner, AlterType.HP, suck);
            skillReport.add(time, owner.getId(), Hp.of(hpChange));
        }
    }
}
