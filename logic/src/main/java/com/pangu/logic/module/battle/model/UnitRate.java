package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗单位的比率属性(累加关系)
 */
@Transable
public enum UnitRate {

	/** 命中 */
	HIT,
	/** 闪避 */
	DODGY,
	/**	控制免疫率*/
	CONTROL_STATE_IMMUNE_RATE,
	/** 怒气增加率（全局回能增加，支持负数） */
	MP_ADD_RATE,

	/** 普攻释放增加怒气比率 */
	NORMAL_MP_ADD,
	/** 击杀增加怒气比率*/
	KILL_MP_ADD,
	 /** 受伤怒气增加比率 */
	DAMAGE_MP_ADD,

	/** 普攻速度提高 */
	NORMAL_SKILL_UP,
	/** 普攻速度降低 */
	NORMAL_SKILL_DOWN,

	//技能CD增加（普攻不算）
	SKILL_CD_UP,
	//技能CD减少（普攻不算）
	SKILL_CD_DOWN,

	/** 暴击 */
	CRIT,
	/** 被暴击率(一般用于加到被攻击放，增加攻击方的暴击率) */
	BE_CRIT,
	/** 抗暴 */
	UNCRIT,
	/** 暴击伤害 */
	CRIT_DAMAGE,
	/** 暴击伤害减免 */
	UNCRIT_DAMAGE,
	/** 伤害:暴击 */
	HURT_CRIT,
	//吸血比率
	SUCK,
	
	/** 格挡 */
	BLOCK,
	/** 精准[免格挡] */
	UNBLOCK,
	
	/** 物理伤害率 */
	HARM_P,
	/** 魔法伤害率 */
	HARM_M,
	/** 物理免伤率 */
	UNHARM_P,
	/** 法术免伤率 */
	UNHARM_M,
	
	/** 昏迷率 */
	DISABLE,
	/** 抗昏迷率 */
	UNDISABLE,
	/** 混乱率 */
	CHAOS,
	/** 抗混乱率 */
	UNCHAOS,
	
	/** 迷糊率 */
	FUZZY,
	/** 治疗率*/
	CURE,
	/** 被治疗率*/
	BCURE,
	BCURE_DECREASE,
	
	/** 生命提升(天赋) */
	LIFE_UP,

	/** 移动速度提高 */
	SPEED_UP,

	/** 移动速度降低 */
	SPEED_DOWN,
	/** 攻击提升(天赋) */
	ATTACK_UP,
	
	/**护甲穿透率*/
	ARMOR_PENETRATION,
	/**法术穿透率*/
	SPELL_PENETRATION,
	
	
	/** 物理攻击加成*/
	ATTACK_P_ADD,
	/** 法术强度加成*/
	ATTACK_M_ADD,
	/** 护甲加成*/
	DEFENCE_P_ADD,
	/** 魔抗加成*/
	DEFENCE_M_ADD,
	/** 血量加成*/
	HP_MAX_ADD,
	/******* 种族克制属性********/
	/** 对银白帝国伤害加深*/
	YBDG_DEEPEN,
	/** 对荒野之地伤害加深*/
	HYZD_DEEPEN,
	/** 对自然回响伤害加深*/
	ZRHX_DEEPEN,
	/** 对坠星之城伤害加深*/
	ZXZC_DEEPEN,
	/** 对深渊魔井伤害加深*/
	SYMJ_DEEPEN,
	/** 对永恒神域伤害加深*/
	YHSY_DEEPEN,
	/** 克制伤害加深*/
	RACE_DEEPEN_ADD,
	/**	对女性英雄伤害加深*/
	FEMALE_DEEPEN,
	/**	对男性英雄伤害加深*/
	MALE_DEEPEN,

	/******* 奖励加成属性********/
	
	/** 经验加成*/
	EXP,
	/** 银币加成*/
	COPPER,

	/** pve伤害增加 */
	PVE_ATTACK_ADD,

	/** PVE伤害减免 */
	PVE_ATTACK_DEC,

	/** PVP伤害增加 */
	PVP_ATTACK_ADD,

	/** PVP伤害减免 */
	PVP_ATTACK_DEC,

	/** 血量恢复效果将超出血量上限生命转换为护盾 */
	RECOVER_HP_TO_SHIELD,

	// 普通技能伤害加深
	NORMAL_SKILL_ADD,
	// 技能伤害加深
	SKILL_ADD,
	// 大招伤害加深
	SPACE_ADD,

	/** 有害BUFF持续时间减免 */
	BUFF_HARM_DEC,
	/** 有害状态持续时间减免 */
	STATE_HARM_DEC,

	;
}
