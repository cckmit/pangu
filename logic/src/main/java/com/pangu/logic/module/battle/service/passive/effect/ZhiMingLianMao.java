package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ZhiMingLianMaoParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 致命连矛:
 * 燃烧自己8%的最大生命值,在后续的10秒内，攻击速度提升35%
 * 2级:持续时间内击杀或协助击杀敌方单位，将重置该状态的持续时间
 * 3级:持续时间内击杀或协助击杀敌方单位，还将恢复自身15%的最大生命值
 * 4级:攻击速度提升至55%
 */
@Component
public class ZhiMingLianMao implements UnitDiePassive, AttackPassive {
    @Static
    private Storage<String, BuffSetting> configs;

    @Override
    public PassiveType getType() {
        return PassiveType.ZHI_MING_LIAN_MAO;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        //判断死者是否直接或间接死于被动持有者
        final ZhiMingLianMaoAddition addition = getAddition(passiveState);
        final boolean dieDueToOwner = dieUnits.stream().anyMatch(dead-> addition.damageUnitIds.contains(dead.getId()));
        if (!dieDueToOwner) return;
        final ZhiMingLianMaoParam param = passiveState.getParam(ZhiMingLianMaoParam.class);

        //重置buff持续时间
        final String passiveKeeperBuffId = param.getBuffId();
        BuffFactory.addBuff(passiveKeeperBuffId,owner,owner,time, damageReport, null);

        //恢复百分比最大生命值
        long addHp = (long) (owner.getValue(UnitValue.HP_MAX) * param.getRate());
        context.addPassiveValue(owner, AlterType.HP, addHp);
        damageReport.add(time, owner.getId(), Hp.of(addHp));
        passiveState.addCD(time);
    }


    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //攻击自己时无需触发
        if (owner == target) return;
        //每次攻击造成伤害时，将目标id写入PassiveState.addition中，以此来维护 在当前被动状态下，攻击过的单位
        final ZhiMingLianMaoAddition addition = getAddition(passiveState);
        addition.damageUnitIds.add(target.getId());
        //没有修改任何数值，无需修改战报
    }

    private ZhiMingLianMaoAddition getAddition(PassiveState passiveState) {
        ZhiMingLianMaoAddition addition = passiveState.getAddition(ZhiMingLianMaoAddition.class);
        if (addition == null) {
            addition = new ZhiMingLianMaoAddition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class ZhiMingLianMaoAddition {
        private final Set<String> damageUnitIds = new HashSet<>();
    }
}
