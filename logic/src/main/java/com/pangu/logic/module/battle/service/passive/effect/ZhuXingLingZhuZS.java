package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.OnceParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ZhuXingLingZhuZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 1：大招每命中一个物理单位，生成一个火球，+4%造成的伤害/每火球。每命中一个法系单位，生成一个冰球，+7%能量恢复速率/每冰球
 * 10：+6.5%造成的伤害/每火球，+10%能量恢复速率/每冰球
 * 20：+9%造成的伤害/每火球，+15%能量恢复速率/每冰球
 * 30：下次释放末日审判时会消耗所有法球附带额外效果，并根据击中的目标重新生成法球
 * 混沌陨石：大招伤害+10%/每火球，降低命中目标10%魔防/冰球
 * 电磁脉冲：降低命中目标10%攻速/每火球，降低命中目标10%能量恢复速度/冰球
 */
@Component("PASSIVE:ZhuXingLingZhuZS")
public class ZhuXingLingZhuZS implements AttackPassive, SkillReleasePassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ZhuXingLingZhuZSParam param = passiveState.getParam(ZhuXingLingZhuZSParam.class);
        //所有伤害均能享受法球基本伤害
        final Addition addition = getAddition(passiveState);
        final Map<UnitType, Set<Unit>> availableOrbs = addition.availableOrbs;
        int availableFireOrbs = availableOrbs.get(UnitType.AGILITY).size() + availableOrbs.get(UnitType.STRENGTH).size();
        long dmgUp = (long) (availableFireOrbs * param.getDmgUp() * damage);

        //根据法球数量，实时生成buff效果
        if (skillState.getType() == SkillType.SPACE) {
            if (param.isConsumeOrbs()) {
                //使用陨石时额外增伤、添加debuff
                final Map<UnitType, Set<Unit>> consumedOrbs = addition.consumedOrbs;
                final int consumedIceOrbs = consumedOrbs.get(UnitType.INTELLECT).size();
                final int consumedFireOrbs = consumedOrbs.get(UnitType.INTELLECT).size() + consumedOrbs.get(UnitType.STRENGTH).size();
                if (skillState.getTag().equals("mo_ri_shen_pan_meteor")) {
                    dmgUp += (long) (consumedFireOrbs * param.getMeteorDmgUp() * damage);
                    final OnceParam onceParam = new OnceParam();
                    onceParam.setCalType(CalType.EXP);
                    final Map<AlterType, String> alters = new HashMap<>();
                    alters.put(AlterType.DEFENCE_M, "-target.getOriginValue('DEFENCE_M')*" + param.getMeteorDefenceCutRatePerIceOrb() * consumedIceOrbs);
                    onceParam.setAlters(alters);
                    final BuffSetting setting = BuffFactory.getSetting(param.getMeteorDeBuffId());
                    final BuffSetting buffSetting = new BuffSetting(setting.getId(), BuffType.ONCE, setting.getTag(), Collections.emptySet(), param.getMeteorDeBuffDuration(), 0, setting.getDispelType(), null, onceParam, false, 0);
                    BuffFactory.addBuff(buffSetting, owner, target, time, skillReport, null);
                }
                //使用电磁球时添加debuff
                if (skillState.getTag().equals("mo_ri_shen_pan_electric")) {
                    final OnceParam onceParam = new OnceParam();
                    onceParam.setCalType(CalType.VALUE);
                    final Map<AlterType, String> alters = new HashMap<>();
                    alters.put(AlterType.RATE_NORMAL_SKILL_DOWN, "+" + param.getElectricNormalSpeedDownPerFireOrb() * consumedFireOrbs);
                    alters.put(AlterType.RATE_MP_ADD_RATE, "-" + param.getElectricMpCutRatePerIceOrb() * consumedIceOrbs);
                    onceParam.setAlters(alters);
                    final BuffSetting setting = BuffFactory.getSetting(param.getElectricDeBuffId());
                    final BuffSetting buffSetting = new BuffSetting(setting.getId(), BuffType.ONCE, setting.getTag(), Collections.emptySet(), param.getElectricDeBuffDuration(), 0, setting.getDispelType(), null, onceParam, false, 0);
                    BuffFactory.addBuff(buffSetting, owner, target, time, skillReport, null);
                }
            }
            //使用大招必然伴随着生成法球
            final Collection<UnitType> unitTypes = target.getProfession();
            for (UnitType unitType : unitTypes) {
                final Set<Unit> units = addition.availableOrbs.computeIfAbsent(unitType, k -> new HashSet<>());
                units.add(target);
            }
        }
        PassiveUtils.hpUpdate(context, skillReport, target, dmgUp, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZHU_XING_LING_ZHU_ZS;
    }

    //释放大招时消耗法球
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        final ZhuXingLingZhuZSParam param = passiveState.getParam(ZhuXingLingZhuZSParam.class);
        if (!param.isConsumeOrbs()) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        final Map<UnitType, Set<Unit>> availableOrbs = addition.availableOrbs;
        final Map<UnitType, Set<Unit>> consumedOrbs = addition.consumedOrbs;
        //统计已消耗的法球数量，供大招提供特殊效果
        consumedOrbs.putAll(availableOrbs);
        //清空可用法球
        for (UnitType type : UnitType.values()) {
            availableOrbs.put(type, new HashSet<>());
        }
    }

    @Getter
    public static class Addition {
        //大招已消耗的法球
        private Map<UnitType, Set<Unit>> consumedOrbs;

        //可供消耗的法球
        private Map<UnitType, Set<Unit>> availableOrbs;
    }

    public Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            addition.consumedOrbs = new HashMap<>();
            addition.availableOrbs = new HashMap<>();
            for (UnitType type : UnitType.values()) {
                addition.consumedOrbs.put(type, new HashSet<>());
                addition.availableOrbs.put(type, new HashSet<>());
            }
            passiveState.setAddition(addition);
        }
        return addition;
    }
}
