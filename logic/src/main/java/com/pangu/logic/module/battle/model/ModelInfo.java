package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import com.pangu.framework.resource.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

/**
 * 战斗单位的模型信息
 */
@Transable
@Getter
@Setter
public class ModelInfo implements Cloneable, JsonObject {

    /**
     * 单位名
     */
    private String name;
    /**
     * 等级
     */
    private int level;
    /**
     * 武将标识
     */
    private int baseId = -1;
    /**
     * 模型标识
     */
    private int model = -1;
    /**
     * 品质
     */
    private int quality = -1;

    /**
     * 属性类型
     */
    @Deprecated
    private UnitType profession;

    /**
     * 多属性英雄改为使用该战报
     */
    private Collection<UnitType> professions;

    /**
     * 阵营(种族)
     */
    private HeroRaceType raceType;
    /**
     * 职业
     */
    private HeroJobType job;

    /**
     * 星级
     */
    private int star;

    /**
     * 英雄ID
     */
    private Long heroId;

    /**
     * 战力
     */
    private long fight;

    /**
     * 皮肤id
     */
    private int skinId;

    /**
     * 性别
     */
    private boolean female;

    public ModelInfo copyOrClone(String name) {
        ModelInfo result = clone();
        if (name != null) {
            result.name = name;
        }
        return result;
    }

    @Override
    protected ModelInfo clone() {
        try {
            return (ModelInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("对象无法被克隆", e);
        }
    }

    public Collection<UnitType> getProfessions() {
        return professions == null ? Collections.singleton(profession) : professions;
    }

    public void merge(ModelInfo modelInfo) {
        if (modelInfo.getName() != null) {
            this.name = modelInfo.getName();
        }
        if (modelInfo.getJob() != null) {
            this.job = modelInfo.getJob();
        }
        if (modelInfo.getRaceType() != null) {
            this.raceType = modelInfo.raceType;
        }
        if (modelInfo.getProfession() != null) {
            this.profession = modelInfo.getProfession();
        }
        if (modelInfo.getProfessions() != null) {
            this.professions = modelInfo.getProfessions();
        }
        if (modelInfo.getStar() != -1) {
            this.star = modelInfo.getStar();
        }
        if (modelInfo.getBaseId() != -1) {
            this.baseId = modelInfo.getBaseId();
        }
        if (modelInfo.getQuality() != -1) {
            modelInfo.quality = quality;
        }
        if (modelInfo.getModel() != -1) {
            modelInfo.model = model;
        }
        if (modelInfo.getHeroId() != null) {
            this.heroId = modelInfo.getHeroId();
        }
    }

    /**
     * 构造方法
     */
    public static ModelInfo valueOf(String name, int baseId, int model) {
        ModelInfo result = new ModelInfo();
        result.name = name;
        result.baseId = baseId;
        result.model = model;
        return result;
    }

}
