package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.BuffCastOnIndirectlyKillParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 康斯坦丁在每参与击杀一名敌人后，就会获得30%攻击速度，如果击杀的目标为克制阵营与深渊魔井的目标时，将额外获得30%攻击力的提升，持续8秒
 */
@Component
public class BuffCastOnIndirectlyKill implements AttackPassive, UnitDiePassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Set<Unit> attackedTargets = passiveState.getAddition(HashSet.class, new HashSet<Unit>(6));
        attackedTargets.add(target);
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final BuffCastOnIndirectlyKillParam param = passiveState.getParam(BuffCastOnIndirectlyKillParam.class);
        if (param.isNeedDirectKill() && attacker != owner) {
            return;
        }

        final HashSet<Unit> attackedUnits = passiveState.getAddition(HashSet.class);
        if (CollectionUtils.isEmpty(attackedUnits)) {
            return;
        }

        final ArrayList<Unit> kills = new ArrayList<>();
        for (Unit dieUnit : dieUnits) {
            if (attackedUnits.contains(dieUnit)) {
                kills.add(dieUnit);
            }
        }
        if (CollectionUtils.isEmpty(kills)) {
            return;
        }

        final List<Unit> buffTargets = TargetSelector.select(owner, param.getTarget(), time);
        final VerifyCtx ctx = new VerifyCtx(owner, kills);
        for (Unit buffTarget : buffTargets) {
            for (Map.Entry<String, String> e : param.getBuff2ConExp().entrySet()) {
                final String conExp = e.getValue();
                if (!StringUtils.isEmpty(conExp) && !ExpressionHelper.invoke(conExp, boolean.class, ctx)) {
                    continue;
                }
                BuffFactory.addBuff(e.getKey(), owner, buffTarget, time, damageReport, null);
            }
        }

        passiveState.addCD(time);
    }

    @AllArgsConstructor
    @Getter
    public class VerifyCtx{
        private Unit owner;
        private List<Unit> dieUnits;

        public boolean dieUnitsCounteredByOwner(){
            for (Unit dieUnit : dieUnits) {
                if (owner.counter(dieUnit)) {
                    return true;
                }
            }
            return false;
        }

        public boolean dieUnitsRaceInclude(int type){
            for (Unit dieUnit : dieUnits) {
                if (dieUnit.isHeroRaceType(type)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BUFF_CAST_ON_INDIRECTLY_KILL;
    }
}
