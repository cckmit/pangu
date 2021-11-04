package com.pangu.logic.module.account.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

/**
 * 登录返回信息VO
 */
@Transable
@Getter
public class LoginInfoVo {
    /**
     * 各模块登录信息(模块号->模块vo信息)
     */
    private Map<Short, Object> content;
    /**
     * 微登陆经验加成
     */
    private double pcLoginExpAddition;

    /**
     * 开服时间
     */
    private Date openServer;

    /**
     * 头衔ID
     */
    private int honorId;
}
