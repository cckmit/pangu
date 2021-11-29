package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class GongJiangDaShiZSParam {

    /** 每累计回复生命值比率，增加BUFF */
    private double preAddBuffRecoverRate;
    /** BUFF标识 */
    private String buffId;
    /** BUFF叠加次数上限 */
    private int buffOverlayLimit;
    /** BUFF达到叠加次数上限是否霸体 */
    private boolean bati;

}
