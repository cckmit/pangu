package com.pangu.logic.module.account.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

import java.util.Date;

/**
 * 账户VO
 *
 * @author Ramon
 */
@Transable
@Data
public class AccountVo {

    /**
     * 账户编号
     */
    private Long id;
    /**
     * 账号
     */
    private String name;
    /**
     * 创建时间
     */
    private Date createdOn;
    /**
     * 状态
     */
    private int state;

    /**
     * 最后登录时间
     */
    private Date loginOn;
    /**
     * 最后登出时间
     */
    private Date logoutOn;

    /**
     * 当天累计时间
     */
    private long timeByDay;
    /**
     * 累计在线时间
     */
    private long timeByTotal;

    /**
     * 累计在线天数(从0开始)
     */
    private int dayByTotal;
    /**
     * 连续登录天数(从0开始)
     */
    private int dayByContinuous;

    /**
     * 是否在线状态
     */
    private boolean online;

    /**
     * 渠道
     */
    private String channel;
}
