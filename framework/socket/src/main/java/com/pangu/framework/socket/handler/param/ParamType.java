package com.pangu.framework.socket.handler.param;

public enum ParamType {

    // 连接session
    SESSION,

    // 授权ID
    IDENTITY,

    // 请求中获取
    IN_BODY,

    // 请求头
    HEADER,

    // 请求整个Message
    MESSAGE,
}
