package com.pangu.framework.socket.filter;

import io.netty.channel.ChannelHandler;

public interface SocketFilter extends ChannelHandler {

    int DEBUG = 10;
    String DEBUG_NAME = "debug-filter";

    int POLICY = 20;
    String POLICY_NAME = "as-policy";

    int TGW =30;
    String TGW_NAME = "TGW-FILTER";

    int MANAGE = 40;
    String MANAGE_NAME = "manager-filter";

    int FIRE_WALL = 50;
    String FIRE_WALL_NAME = "fire-wall";

    /**
     * 排序，直接由决定netty生效顺序
     *
     * @return
     */
    int getIndex();

    /**
     * 名称
     *
     * @return
     */
    String getName();
}
