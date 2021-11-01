package com.pangu.framework.socket.core;

/**
 * Header#state字段，共32个比特位[4:保留][8:错误状态码][20:比特状态位]
 */
public interface StateConstant {
    // 压缩限制条件
    int COMPRESS_LIMIT = 1024 * 1024;
    // 包标识
    int PACKAGE_IDENTITY_PREFIX = (short) 0xFFFFFFFF;

    // 用于推送的消息序号
    long DEFAULT_SN = -1;

    // Head#state 信息状态部分

    // 状态:正常(请求状态)
    int STATE_NORMAL = 0;

    //状态:回应(不是回应就是请求)
    int STATE_RESPONSE = 1;

    // 压缩标记位(没有该状态代表未经压缩)
    int STATE_COMPRESS = 1 << 1;

    // 转发标记位
    int STATE_FORWARD = 1 << 2;

    // 原生信息标记位(有该状态代表信息体为原生类型，即不进行编解码)
    int STATE_RAW = 1 << 4;

    // 心跳类型消息
    int HEART_BEAT = 1 << 5;
    // 转发消息
    int REDIRECT = 1 << 6;

    //--------------------------------------------------------
    //--------------错误码保存高20位    << 20-------------------
    //--------------------------------------------------------
    // 请求指令不存在
    int COMMAND_NOT_FOUND = 17;

    // 解码异常
    int DECODE_EXCEPTION = 18;

    // 编码异常
    int ENCODE_EXCEPTION = 19;

    // 会话身份异常
    int IDENTITY_EXCEPTION = 22;

    // 业务异常(此异常，将会把错误码通过Result结构返回给客户端)
    int MANAGED_EXCEPTION = 23;

    // session中缓存值异常
    int SESSION_EXCEPTION = 24;

    // 未知异常
    int UNKNOWN_EXCEPTION = 25;

    // 网络相关异常
    int SOCKET_EXCEPTION = 26;

    // 需要管理后台IP
    int MANAGE_IP_EXCEPTION = 27;
}
