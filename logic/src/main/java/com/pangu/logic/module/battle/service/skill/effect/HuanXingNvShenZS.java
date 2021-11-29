package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HuanXingNvShenZSParam;
import com.pangu.logic.module.battle.service.skill.param.YuanXingDeHuHuanParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 唤星女神·维纳斯专属装备
 * 1：当自己使用技能复活队友时，复活的队友获得300能量
 * 10：当自己使用技能复活队友时，复活的队友获得450能量
 * 20：当自己使用技能复活队友时，复活的队友未来10秒内获得15%伤害加成
 * 30：当自己使用技能复活队友时，复活的队友获得600能量
 *
 * @author Kubby
 */
@Component
public class HuanXingNvShenZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.HUAN_XING_NV_SHEN_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        HuanXingNvShenZSParam param = state.getParam(HuanXingNvShenZSParam.class);
        for (SkillState ss : owner.getActiveSkills()) {
            for (EffectState es : ss.getEffectStates()) {
                if (es.getType() == EffectType.YUAN_XING_DE_HU_HUAN) {
                    YuanXingDeHuHuanParam esParam = es.getParam(YuanXingDeHuHuanParam.class);
                    final YuanXingDeHuHuanParam enhancedParam = new YuanXingDeHuHuanParam();
                    BeanUtils.copyProperties(esParam, enhancedParam);

                    enhancedParam.setZsMpAdd(param.getZsMpAdd());
                    enhancedParam.setZsBuffId(param.getZsBuffId());
                    enhancedParam.setZsSelfMpAdd(param.getZsSelfMpAdd());

                    es.setParamOverride(enhancedParam);
                }
            }
        }
    }
}
