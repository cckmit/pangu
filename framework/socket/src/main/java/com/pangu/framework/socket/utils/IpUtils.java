package com.pangu.framework.socket.utils;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

public abstract class IpUtils {

    /**
     * 获取会话的IP地址
     *
     * @param session
     * @return
     */
    public static String getIp(Channel session) {
        if (session == null || session.remoteAddress() == null) {
            return "UNKNOWN";
        }
        String ip = session.remoteAddress().toString();
        return StringUtils.substringBetween(ip, "/", ":");
    }

    /**
     * 字符串转换为InetSocketAddress
     */
    public static InetSocketAddress toInetSocketAddress(String addr) {
        int colonIndex = addr.lastIndexOf(":");
        if (colonIndex < 0) {
            return new InetSocketAddress(Integer.parseInt(addr));
        }
        int port = Integer.parseInt((addr.substring(colonIndex + 1)));
        if (colonIndex > 0) {
            String host = addr.substring(0, colonIndex);
            if (!"*".equals(host)) {
                return new InetSocketAddress(host, port);
            }
        }
        return new InetSocketAddress(port);
    }
}
