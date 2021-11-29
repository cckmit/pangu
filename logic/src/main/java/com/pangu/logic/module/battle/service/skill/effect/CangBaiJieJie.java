package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.AttackParam;
import com.pangu.logic.module.battle.service.skill.param.CangBaiJieJieParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 苍白结界
 * 在自身脚下召唤结界,降低结界内敌方单位18%的攻击力敌方首次尝试出入结界时,会受到220%攻击力的伤害
 * 2级:伤害提升至240%攻击力
 * 3级:伤害提升至260%攻击力
 * 4级:降低敌人25%的攻击
 */
@Component
public class CangBaiJieJie implements SkillEffect {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public EffectType getType() {
        return EffectType.CANG_BAI_JIE_JIE;
    }

    private static class Addition {
        //结界位置选择器
        private SelectSetting selectSetting;

        //记录已被结界伤害过的目标
        private Set<Unit> attackedUnits;
    }

    private Addition getAddition(Context context) {
        Addition addition = context.getRootSkillEffectAction().getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            addition.attackedUnits = new HashSet<>();

            //该技能循环生效，故需缓存数据至SkillEffectAction
            context.getRootSkillEffectAction().setAddition(addition);
        }
        return addition;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final CangBaiJieJieParam param = state.getParam(CangBaiJieJieParam.class);
        final Addition addition = getAddition(context);

        //构造目标选择策略
        if (addition.selectSetting == null) {
            final AreaParam areaParam = AreaParam.builder()
                    .shape(AreaType.CIRCLE)
                    .points(new int[][]{{owner.getPoint().x, owner.getPoint().y}})
                    .r(param.getR())
                    .build();
            addition.selectSetting = SelectSetting.builder()
                    .realParam(areaParam)
                    .selectType(SelectType.AREA)
                    .filter(param.getFilterType())
                    .build();
        }

        //每次循环执行时动态筛选目标
        final List<Unit> targets = TargetSelector.select(owner, time, addition.selectSetting);

        //对处于结界中的目标释放debuff
        final BuffUpdateParam buff = param.getBuff();
        if (buff != null) {
            state.setParamOverride(buff);
            for (Unit unit : targets) {
                buffUpdate.execute(state, owner, unit, skillReport, time, skillState, context);
            }
            state.setParamOverride(null);
        }

        //对处于结界中的目标添加异常状态
        final StateAddParam stateParam = param.getStateParam();
        if (stateParam != null) {
            for (Unit unit : targets) {
                SkillUtils.addState(owner, unit, stateParam.getState(), time, stateParam.getTime() + time, skillReport, context);
            }
        }

        //造成伤害
        final AttackParam attackParam = param.getAttackParam();
        if (attackParam == null) {
            return;
        }
        if (attackParam.getAttackType() != EffectType.HP_M_DAMAGE && attackParam.getAttackType() != EffectType.HP_P_DAMAGE) {
            return;
        }
        state.setParamOverride(attackParam.getDmg());
        final SkillEffect hpDmgEffect = SkillFactory.getSkillEffect(attackParam.getAttackType());
        for (Unit unit : targets) {
            //对同一目标只造成一次伤害
            if (addition.attackedUnits.contains(unit)) {
                continue;
            }
            hpDmgEffect.execute(state, owner, unit, skillReport, time, skillState, context);
            addition.attackedUnits.add(unit);
        }
        state.setParamOverride(null);
    }
}
