package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.RemovePassive;
import com.pangu.logic.module.battle.model.report.values.TransformValue;
import com.pangu.logic.module.battle.service.action.custom.TransformEndAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.TransformParam;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 变身效果
 * 添加变身后的被动、buff，移除并缓存变身前的被动、buff，启动变身附带技能，并启动变身倒计时
 */
@Component
public class TransformEffect implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.TRANSFORM;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final TransformParam param = state.getParam(TransformParam.class);
        //添加被动、buff
        String[] addPassives = param.getAddPassives();
        Addition addition = getAddition(state);
        Map<String, PassiveState> passiveStatesById = target.getPassiveStatesById();
        if (addPassives != null) {
            for (String passiveId : addPassives) {
                if (passiveStatesById.containsKey(passiveId)) {
                    continue;
                }
                PassiveState passiveState = PassiveFactory.initState(passiveId, time);
                target.addPassive(passiveState, owner);
                addition.getTransformPassives().add(passiveState);
//                skillReport.add(time, target.getId(), new AddPassive(passiveId));
            }
        }
        if (param.getAddBuffs() != null) {
            for (String addBuff : param.getAddBuffs()) {
                final BuffState buffState = BuffFactory.addBuff(addBuff, owner, target, time, skillReport, null);
                addition.getTransformBuffs().add(buffState);
            }
        }

        //移除被动、buff
        final String[] removePassives = param.getRemovePassives();
        if (removePassives != null) {
            for (String passiveId : removePassives) {
                if (!passiveStatesById.containsKey(passiveId)) {
                    continue;
                }
                PassiveState passiveState = passiveStatesById.get(passiveId);
                target.removePassive(passiveState);
                skillReport.add(time, target.getId(), new RemovePassive(passiveId));
                addition.getOriginPassives().add(passiveState);
            }
        }
        if (param.getRemoveBuffs() != null) {
            for (String removeBuff : param.getRemoveBuffs()) {
                BuffFactory.removeBuffState(removeBuff, target, time);
                addition.getOriginBuffs().add(removeBuff);
            }
        }
        //缓存原本的模型
        addition.setOriginBaseId(target.getModel().getBaseId());
        addition.setTransformBaseId(param.getBaseId());
        //输出变身模型战报，通知前端变身
        target.setTransformState(param.getState());
        skillReport.add(time, target.getId(), new TransformValue(param.getState()));
        //释放附带技能
        if (param.getSkillId() != null) SkillFactory.updateNextExecuteSkill(time, target, param.getSkillId());
        //若未配置持续时间，则默认永久变身，不配置变身解除行为
        if (param.getDuration() <= 0) return;
        //提交一个移除变身的行为
        final TransformEndAction transformEndAction = TransformEndAction.builder()
                .time(time + param.getDuration())
                .effectState(state)
                .skillReport(skillReport)
                .target(target)
                .build();
        //角色变身途中死亡，应当在死后一段时间变回人形。否则若被复活可能永久维持变身形态
        owner.getBattle().addWorldAction(transformEndAction);
    }

    private Addition getAddition(EffectState state) {
        Addition addition = state.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            state.setAddition(addition);
        }
        return addition;
    }

    @Data
    public static class Addition {
        private int originBaseId;
        private int transformBaseId;
        private Set<PassiveState> originPassives = new HashSet<>();
        private Set<PassiveState> transformPassives = new HashSet<>();

        private List<String> originBuffs = new ArrayList<>();
        private List<BuffState> transformBuffs = new ArrayList<>();
    }

}
