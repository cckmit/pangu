package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗单位的数值属性
 */
@Transable
public enum UnitValue {

	/** 力量 */
	STRENGTH,
	/** 敏捷 */
	AGILITY,
	/** 智力 */
	INTELLECT,
	/** 生命 */
	HP,
	/** 生命上限 */
	HP_MAX,
	/** 生命回复值*/
	HP_RECOVER,
	/** 韧性*/
	TOUGHNESS,
	
	/** 怒气 */
	MP,
	/** 怒气上限 */
	MP_MAX,

	/**	能量和能量上限，部分特殊机制的英雄使用*/
	EP,
	EP_MAX,

	/** 普攻释放增加怒气值 */
	NORMAL_MP_ADD,
	/** 击杀增加怒气值*/
	KILL_MP_ADD,
	/** 受伤怒气增加值 */
	DAMAGE_MP_ADD,
	
	/** 伤害吸收护盾 */
	SHIELD,
	/** 回光盾*/
	LIGHT_SHIELD,
	/** 物理攻击*/
	ATTACK_P,
	/** 法术攻击*/
	ATTACK_M,
	/** 物理防御*/
	DEFENCE_P,
	/** 法术防御*/
	DEFENCE_M,
	
	/** 护甲穿透*/
	PHYSICAL_PENETRATION,
	/** 法术穿透*/
	MAGIC_PENETRATION,
	/** 真实伤害*/
	REAL_DAMAGE,
	
	/** 移动速度 */
	SPEED,

	/** 治疗量 */
	CURE_VALUE,
	/** 被治疗量 */
	BE_CURE_VALUE,
	;
}
