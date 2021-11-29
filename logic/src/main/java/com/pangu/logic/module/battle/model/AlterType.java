package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.alter.*;
import com.pangu.framework.protocol.annotation.Transable;

/**
 * 值修改类型
 */
@Transable
public enum AlterType {

	// 数值属性
	/** 力量**/
	STRENGTH(new UnitValueAlter(UnitValue.STRENGTH)),
	/** 敏捷**/
	AGILITY(new UnitValueAlter(UnitValue.AGILITY)),
	/** 智力**/
	INTELLECT(new UnitValueAlter(UnitValue.INTELLECT)),
	/** 生命 */
	HP(new HpAlter()),
	/** 生命上限 */
	HP_MAX(new UnitValueAlter(UnitValue.HP_MAX)),
	/** 生命(最大值与生命数值) */
	LIFE(new LifeAlter()),
	/** 生命回复*/
	HP_RECOVER(new UnitValueAlter(UnitValue.HP_RECOVER)),

	/** 怒气 */
	MP(new MpAlter()),
	MP_SUCK(new MpSuckAlter()),
	/** 怒气上限 */
	MP_MAX(new UnitValueAlter(UnitValue.MP_MAX)),

	// 怒气增加率
	RATE_MP_ADD_RATE(new RateAlter(UnitRate.MP_ADD_RATE)),

	/** 普攻释放增加怒气比率 */
	RATE_NORMAL_MP_ADD(new RateAlter(UnitRate.NORMAL_MP_ADD)),
	/** 击杀增加怒气比率*/
	RATE_KILL_MP_ADD(new RateAlter(UnitRate.KILL_MP_ADD)),
	/** 受伤怒气增加比率 */
	RATE_DAMAGE_MP_ADD(new RateAlter(UnitRate.DAMAGE_MP_ADD)),

	/** 普攻释放增加怒气值 */
	NORMAL_MP_ADD(new UnitValueAlter(UnitValue.NORMAL_MP_ADD)),
	/** 击杀增加怒气值*/
	KILL_MP_ADD(new UnitValueAlter(UnitValue.KILL_MP_ADD)),
	/** 受伤怒气增加值 */
	DAMAGE_MP_ADD(new UnitValueAlter(UnitValue.DAMAGE_MP_ADD)),

	/** 怒气增长附加 */
	@Deprecated
	MP_ADD(new RateAlter(UnitRate.MP_ADD_RATE)),
	// 受伤怒气增加率
	@Deprecated
	RATE_BE_ATTACKED_MP(new RateAlter(UnitRate.DAMAGE_MP_ADD)),
	/** 怒气回复*/
	@Deprecated
	MP_RECOVER(new RateAlter(UnitRate.MP_ADD_RATE)),

    /** 能量修改*/
	EP(new UnitValueAlter(UnitValue.EP)),

	/** 设置伤害吸收护盾 */
	SHIELD_SET(new ShieldUpdateAlter()),
	/** 修改伤害吸收护盾 */
	SHIELD_UPDATE(new ShieldUpdateAlter()),
	/** 回光盾*/
	LIGHT_SHIELD(new UnitValueAlter(UnitValue.LIGHT_SHIELD)),
	/** 物理攻击*/
	ATTACK_P(new UnitValueAlter(UnitValue.ATTACK_P)),
	/** 法术攻击*/
	ATTACK_M(new UnitValueAlter(UnitValue.ATTACK_M)),
	/** 护甲 */
	DEFENCE_P(new UnitValueAlter(UnitValue.DEFENCE_P)),
	/** 魔抗 */
	DEFENCE_M(new UnitValueAlter(UnitValue.DEFENCE_M)),

	/** 护甲穿透 */
	PHYSICAL_PENETRATION(new UnitValueAlter(UnitValue.PHYSICAL_PENETRATION)),
	/** 法术穿透 */
	MAGIC_PENETRATION(new UnitValueAlter(UnitValue.MAGIC_PENETRATION)),
	/** 真实伤害*/
	REAL_DAMAGE(new UnitValueAlter(UnitValue.REAL_DAMAGE)),

	/** 移动速度 */
	SPEED(new UnitValueAlter(UnitValue.SPEED)),

	// 比率属性(累加关系)
	//抗暴率
	RATE_UNCRIT(new RateAlter(UnitRate.UNCRIT)),
	//暴击率
	RATE_CRIT(new RateAlter(UnitRate.CRIT)),
	// 被暴击率(一般用于加到被攻击放，增加攻击方的暴击率)
	RATE_BE_CRIT(new RateAlter(UnitRate.BE_CRIT)),
	/** 物理攻击加成*/
	RATE_ATTACK_P_ADD(new RateAlter(UnitRate.ATTACK_P_ADD)),
	/** 法术强度加成*/
	RATE_ATTACK_M_ADD(new RateAlter(UnitRate.ATTACK_M_ADD)),
	/** 护甲加成*/
	RATE_DEFENCE_P_ADD(new RateAlter(UnitRate.DEFENCE_P_ADD)),
	/** 魔抗加成*/
	RATE_DEFENCE_M_ADD(new RateAlter(UnitRate.DEFENCE_M_ADD)),
	/** 血量加成*/
	RATE_HP_MAX_ADD(new RateAlter(UnitRate.HP_MAX_ADD)),

	/** 普攻速度提升 */
	RATE_NORMAL_SKILL_UP(new RateAlter(UnitRate.NORMAL_SKILL_UP)),
	/** 普攻速度提升 */
	RATE_NORMAL_SKILL_DOWN(new RateAlter(UnitRate.NORMAL_SKILL_DOWN)),

	/** 命中 */
	RATE_HIT(new RateAlter(UnitRate.HIT)),
	/** 闪避 */
	RATE_DODGY(new RateAlter(UnitRate.DODGY)),
	/** 怒气减免率*/
	@Deprecated
	RATE_MP_DERATE(new RateAlter(UnitRate.MP_ADD_RATE)),
	@Deprecated
	RATE_NORMAL_MP_DERATE(new RateAlter(UnitRate.MP_ADD_RATE)),
	//暴击伤害增加
	RATE_CRIT_DAMAGE(new RateAlter(UnitRate.CRIT_DAMAGE)),
	//暴击伤害减免
	RATE_UNCRIT_DAMAGE(new RateAlter(UnitRate.UNCRIT_DAMAGE)),

	/** 伤害暴击*/
	RATE_HURT_CRIT(new RateAlter(UnitRate.HURT_CRIT)),
	//增加技能cd
	RATE_SKILL_CD_UP(new RateAlter(UnitRate.SKILL_CD_UP)),
	//减少技能CD
	RATE_SKILL_CD_DOWN(new RateAlter(UnitRate.SKILL_CD_DOWN)),

	/** 格挡 */
	RATE_BLOCK(new RateAlter(UnitRate.BLOCK)),
	/** 免格挡 */
	RATE_UNBLOCK(new RateAlter(UnitRate.UNBLOCK)),

	/** 物理伤害率 */
	RATE_HARM_P(new RateAlter(UnitRate.HARM_P)),
	/** 魔法伤害率 */
	RATE_HARM_M(new RateAlter(UnitRate.HARM_M)),
	/**	统一修改伤害率*/
	RATE_HARM(new HarmAlter()),
	/** 物理免伤率 */
	RATE_UNHARM_P(new RateAlter(UnitRate.UNHARM_P)),
	/** 魔法免伤率 */
	RATE_UNHARM_M(new RateAlter(UnitRate.UNHARM_M)),
	/**	统一修改免伤率*/
	RATE_UNHARM(new UnHarmAlter()),
	//吸血比率
	RATE_SUCK(new RateAlter(UnitRate.SUCK)),

	/** 昏迷率 */
	RATE_DISABLE(new RateAlter(UnitRate.DISABLE)),
	/** 抗昏迷率 */
	RATE_UNDISABLE(new RateAlter(UnitRate.UNDISABLE)),
	/** 混乱率 */
	RATE_CHAOS(new RateAlter(UnitRate.CHAOS)),
	/** 抗混乱率 */
	RATE_UNCHAOS(new RateAlter(UnitRate.UNCHAOS)),
	/** 迷糊率 */
	RATE_FUZZY(new RateAlter(UnitRate.FUZZY)),
	/** 治疗率*/
	RATE_CURE(new RateAlter(UnitRate.CURE)),
	/** 被治疗率*/
	RATE_BCURE(new RateAlter(UnitRate.BCURE)),
	RATE_BCURE_DECREASE(new RateAlter(UnitRate.BCURE_DECREASE)),

	/**护甲穿透率*/
	RATE_ARMOR_PENETRATION(new RateAlter(UnitRate.ARMOR_PENETRATION)),
	/**法术穿透率*/
	RATE_SPELL_PENETRATION(new RateAlter(UnitRate.SPELL_PENETRATION)),

	/** 移动速度提高 */
	RATE_SPEED_UP(new RateAlter(UnitRate.SPEED_UP)),

	/** 移动速度降低 */
	RATE_SPEED_DOWN(new RateAlter(UnitRate.SPEED_DOWN)),

	/**经验加成率*/
	RATE_EXP(new RateAlter(UnitRate.EXP)),
	/**银币加成率*/
	RATE_COPPER(new RateAlter(UnitRate.COPPER)),
	/**血量恢复效果将超出血量上限生命转换为护盾 */
	RATE_RECOVER_HP_TO_SHIELD(new RateAlter(UnitRate.RECOVER_HP_TO_SHIELD)),

	// 普通技能伤害加深
	RATE_NORMAL_SKILL_ADD(new RateAlter(UnitRate.NORMAL_SKILL_ADD)),
	// 技能伤害加深
	RATE_SKILL_ADD(new RateAlter(UnitRate.SKILL_ADD)),
	// 大招伤害加深
	RATE_SPACE_ADD(new RateAlter(UnitRate.SPACE_ADD)),

	/** 有害BUFF持续时间减免 */
	RAE_BUFF_HARM_DEC(new RateAlter(UnitRate.BUFF_HARM_DEC)),

	/** 有害状态持续时间减免 */
	RATE_STATE_HARM_DEC(new RateAlter(UnitRate.STATE_HARM_DEC)),

	/** PVE伤害减免 */
	RATE_PVE_ATTACK_DEC(new RateAlter(UnitRate.PVE_ATTACK_DEC)),

	/** PVP伤害增加 */
	RATE_PVP_ATTACK_ADD(new RateAlter(UnitRate.PVP_ATTACK_ADD)),

	/**	控制免疫率*/
	RATE_CONTROL_STATE_IMMUNE(new RateAlter(UnitRate.CONTROL_STATE_IMMUNE_RATE)),

	/**	对男性伤害加深率*/
	RATE_MALE_DEEPEN(new RateAlter(UnitRate.MALE_DEEPEN)),

	/**	对女性伤害加深率*/
	RATE_FEMALE_DEEPEN(new RateAlter(UnitRate.MALE_DEEPEN)),
	;

	/** 构造方法 */
	AlterType(Alter alter) {
		this.alter = alter;
	}

	/** 修改器 */
	private final Alter alter;

	public Alter getAlter() {
		return alter;
	}

	public Object toValue(String value) {
		return alter.toValue(value);
	}

}
