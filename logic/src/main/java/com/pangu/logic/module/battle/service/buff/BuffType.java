package com.pangu.logic.module.battle.service.buff;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.buff.param.*;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;

/**
 * buff类型
 */
public enum BuffType {
    //  持续伤害计数器
    DOT_COUNTER(DotCounterParam.class),
    //  目标选择器
    TARGET_SELECTOR_BUFF(String.class),
    //  属性汲取
    VAL_DRAIN(ValDrainParam.class),
    //  根据持有者状态，自行决定BUFF生命周期的BUFF
    DYNAMIC_REMOVE_ONCE(DynamicRemoveOnceParam.class),
    //  传承被动无声突袭
    WU_SHENG_TU_XI_CC(WuShengTuXiCCParam.class),
    //  哪吒专属
    NEZHA_ZS(NeZhaZSParam.class),
    //  当此BUFF持有者连续5秒锁定同一个目标后，每3秒为自己和目标分别添加一种BUFF，切换目标后添加的BUFF立即移除
    YUE_GUANG_ZHI_SU_LISTENER(YueGuangZhiSuListenerParam.class),
    //  监听指定单元的状态，并根据其状态动态修改属性
    LISTENER_AND_MODIFIER(ListenerAndModifierParam.class),
    //  根据目标数量动态调整属性
    CAL_VALUES_BY_TARGET_SIZE(CalValuesByTargetSizeParam.class),
    //  定时炸弹
    BOMB(String.class),
    //  动态监测当前跟随目标是否可选中，若不可选中，则切换其他跟随目标
    PRIEST_FOLLOW_TARGET_ROUTER(String.class),
    //  为身后扇形目标添加buff
    SHI_HUANG_ZHI_REN_ZS(ShiHuangZhiRenZSParam.class),
    //  当该buff持有者的指定属性变化指定幅度后，根据计数器持有者的位置，执行不同效果
    SUI_YUAN_BI_ZHU(SuiYuanBiZhuParam.class),
    //  检测场上印记的数量动态调整自身数值,
    CANG_BAI_ZHI_PU_ZS(Double.class),
    //  持有该buff的角色每秒将固定百分比生命值转移给buff释放者
    DRAIN(String.class),
    //  哪吒火焰印记计数器
    FIRE_MARK_COUNTER(FireMarkCounterParam.class),
    //  通用属性修改计数器
    VAL_MOD_COUNTER(ValModCounterParam.class),
    //  技能执行计数器，达到指定数值后更新目标行为,
    SKILL_UPDATE_COUNTER(SkillUpdateCounterParam.class),
    //  死亡计数器，达到指定数值后即死。
    DEATH_COUNTER(CounterParam.class),
    //  一个附带被动的计数器
    COUNTER(CounterParam.class),
    //  检测自身冰法球的数量动态调整自身数值
    ZHU_XING_LING_ZHU_ZS(Double.class),
    //  根据战场中友军数量来调整自身基础数值
    GUANG_ZHI_JUAN_ZHE(GuangZhiJuanZheParam.class),
    //  根据指定区域是否包含敌人来为某区域中的对象添加不同的buff
    JIN_ZHAN_ZHE(JinZhanZheParam.class),
    //  对指定选择器筛选出的的单元施加可继承BUFF
    UPDATE_BUFF(UpdateBuffParam.class),
    //  感电层数计数器，叠满X层后触发Y异常持续Z秒
    GAN_DIAN_GUANG_HUAN(GanDianGuangHuanParam.class),
    //  指定选择器筛选出的单元数量为0时，为另一指定选择器筛选出的单元添加BUFF
    ZHUAN_ZHU_SHE_JI(ZhuanZhuSheJiParam.class),
    //  重复型:多次修改战斗单位数值，移除时回复修改的数值
    REPEAT(RepeatParam.class),
    //  单次型:一次性修改战斗单位数值
    ONCE(OnceParam.class),
    //  定时型:在BUFF移除时一次性修改战斗单位数值
    TIME,
    //  被动效果型:给目标添加被动效果
    PASSIVE,
    //  状态型:添加时修改状态,移除时恢复状态(会检查状态免疫)
    STATE(UnitState.class),
    //  出手速度
    SHOT,
    // 替换普攻
    REPLACE_SKILL,
    // 闪电球效果buff
    LIGHTNING_BALL,
    // 神殇效果
    SHEN_SHANG,
    // 添加监听器
    LISTENER,
    //能量屏障
    // * 制造一个圆形区域（罩子，半径1.5格），区域内友方部队攻速提升10%，受到伤害降低10%。屏障持续6秒
    ENERGY_BARRIER(EnergyBarrierParam.class),
    // 标记类BUFF，无任何效果
    FLAG,
    // 固定生效位置
    FIX_POSITION_BUFF,
    // 每个间隔扣除一定百分比血量
    DEDUCT_PERCENT_HP,
    // 复仇执念
    FU_CHOU_ZHI_NIAN,
    // 添加护盾，移除buff后，护盾将会消失
    SHIELD,
    // 添加Buff那一刻，确定属性值，定时间隔生效
    INTERVAL_VALUES(DefaultAddValueParam.class),
    // INTERVAL_VALUES的子类，附带被动效果
    INTERVAL_VALUES_WITH_PASSIVE(IntervalValuesWithPassiveParam.class),
    INTERVAL_STATE(StateAddParam.class),
    //初始添加一个属性 然后逐级递减
    DECREASE_VALUE(DecreaseValuesParam.class),
    //陷阵之志 进入敌方半场后增加属性
    XZZZ(DefaultAddValueParam.class),
    //惧生灵兽 我方半场没有敌方增加属性
    JSLS(DefaultAddValueParam.class),
    //月之力（月之石） 能量大于50%持续恢复生命值
    MOON_POWER(PowerParam.class),
    //日之力（日之石） 血量大于50%持续恢复能量
    SUN_POWER(PowerParam.class),
    //毒液之爪 持续掉落生命值 可叠加
    DYZZ(Double.class),
    //监测目标周围的单元，处在范围内，添加buff，反之，移除buff
    SELF_CIRCLE_CHECK(SelfCircleCheckParam.class),
    //自身周围没有敌方时添加属性
    NO_ENEMY_AROUND_ADD_VALUES(NoEnemyAroundAddValuesParam.class),
    //遗忘沙漏 每秒回10能量 同队中每有一个队友拥有该BUFF 则效果削弱15%
    YWSL(YiWangShaLouParam.class),
    //每当MP大于800时添加一个BUFF给作用者
    CHECK_MP_ADD_BUFFS(CheckMpAddBuffsParam.class),
    //持续性伤害
    DAMAGE_OVER_TIME(DamageOverTimeParam.class),
    // 连续处于指定范围超过X秒的目标，会添加BUFF
    INTERVAL_RANGE_AURA(IntervalRangeAuraParam.class),
    // 蝴蝶仙子·莉亚娜技能：迷蝶花海
    MI_DIE_HUA_HAI(String.class),
    ;

    private final Class<?> paramType;

    BuffType() {
        this.paramType = String.class;
    }

    BuffType(Class<?> paramType) {
        this.paramType = paramType;
    }

    public Class<?> getParamType() {
        return paramType;
    }
}
