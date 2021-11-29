package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗单元类型 / 兵种
 */
@Transable
public enum UnitType {
	
	/** 力量 */
	STRENGTH,
	
	/** 敏捷 */
	AGILITY,
	
	/** 智力 */
	INTELLECT;
}
