package com.pangu.logic.utils;

import com.pangu.framework.utils.id.IdGenerator;

public class ServerIdUtils {

    public static String toOidSid(long id) {
        IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(id);
        return idInfo.getOperator() + "_" + idInfo.getServer();
    }

    public static String extractServerId(String account) {
        int index = account.indexOf(".");
        if (index < 0) {
            throw new IllegalStateException("[" + account + "]字符串无法解析得到serverId，格式为 【name.oid_sid】");
        }
        return account.substring(index + 1);
    }
}
