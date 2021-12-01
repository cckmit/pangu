package com.pangu.logic.module.battle.service.buff;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.action.BuffAction;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;
import lombok.Setter;

@Getter
public class BuffState {

    // buff配置
    private final BuffSetting buffSetting;
    private final Unit caster;

    // 战报，从出生开始便开始持有
    private final BuffReport buffReport;

    // 添加成功，Buff.add添加过程将会在下一个时间段开始生效，只有生效成功，此状态才会变为true
    private boolean success;

    //自定义标签（针对可叠加BUFF）
    @Setter
    private String customTag;

    // 用于执行、移除buffstate,有可能为空，可能下一个时间才会创建
    @Setter
    private BuffAction buffAction;

    // 附加信息，不同的buff可以额外保存信息
    @Setter
    private Object addition;

    
    private int keepTime;

    public BuffState(BuffSetting buffSetting, Unit caster, BuffReport buffReport, int keepTime, Object addition) {
        this.buffSetting = buffSetting;
        this.caster = caster;
        this.buffReport = buffReport;
        this.addition = addition;
        this.keepTime = keepTime;
    }

    public String getTag() {
        if (customTag != null) {
            return customTag;
        }
        return buffSetting.getTag();
    }

    public String getId() {
        return buffSetting.getId();
    }

    public DispelType getDispelType() {
        return buffSetting.getDispelType();
    }

    public BuffType getType() {
        return buffSetting.getType();
    }

    public int getTime() {
        return keepTime;
    }

    public void updateRemoveTime(int time) {
        //仅更新buffAction的移除时间，buffReport中的removeTime由BuffFactory在移除时进行，以便超长时间的buff战报移除时间统一显示为0
        buffAction.setRemoveTime(time);
        buffAction.getOwner().timeActUpdate(buffAction);
    }

    public int getInterval() {
        return buffSetting.getInterval();
    }

    public <T> T getParam(Class<T> clz) {
        //noinspection unchecked
        return (T) buffSetting.getRealParam();
    }

    public <T> T getAddition(Class<T> clz) {
        //noinspection unchecked
        return (T) addition;
    }

    public <T> T getAddition(Class<T> clz, T def) {
        if (addition == null) {
            addition = def;
        }
        return (T) addition;
    }

    public boolean hasClassify(String classify) {
        return getBuffSetting().getClassifys().contains(classify);
    }

    public void setSuccess(boolean success) {
        this.success = success;
        if (buffReport != null) {
            buffReport.setSuccess(success);
        }
    }
}
