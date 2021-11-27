package com.pangu.framework.socket.exception;

public enum ExceptionCode {

    PROCESS_NOT_FOUND(17, "指令号无法对应处理器"),

    DECODE_ERROR(18, "解码错误"),

    ENCODE_ERROR(19, "编码异常"),

    IDENTITY_PARAMETER(22, "需要Identity参数"),

    MANAGED_EXCEPTION(23, "业务异常"),

    SESSION_PARAM(24, "Session中保存参数异常"),

    UNKNOWN(25, "系统异常"),

    REMOTE_ERROR(26, "远程调用异常"),

    MANAGED_IP(27, "非管理后台IP"),

    CODER_NOT_FOUND(1, "不支持此编解码code"),

    CONNECT_HAS_CLOSED(2, "Socket连接已关闭"),

    CONNECT_INTERRUPTED(3, "连接被终止"),

    MD5_VALID(4, "消息MD5校验失败"),

    MESSAGE_SN_MISS(5, "没有找到消息序列号"),

    PARAM_PHASE(6, "请求参数解析失败"),

    PROCESS_ERROR(7, "业务处理异常"),

    PUSH_NOT_FOUND(8, "找不到推送处理器"),

    TASK_REJECT(9, "消息处理线程池已满"),

    TYPE_DEFINE_NOT_FOUND(10, "MethodDefine定义配置不存在"),

    INVALID_MESSAGE(11, "无效数据包"),

    SESSION_CREATE(12, "session创建失败"),

    TIME_OUT(13, "请求超时");

    private static ExceptionCode[] CODES;

    static {
        ExceptionCode[] values = ExceptionCode.values();
        int maxCode = 0;
        for (ExceptionCode item : values) {
            if (item.code > maxCode) {
                maxCode = item.code;
            }
        }
        CODES = new ExceptionCode[maxCode + 1];
        for (ExceptionCode item : values) {
            CODES[item.code] = item;
        }
    }

    // 错误码
    private final int code;
    // 错误描述
    private final String msg;

    ExceptionCode(int code, String msg) {
        this.code = code;
        this.msg = "[" + code + "]" + msg;
    }

    public static ExceptionCode of(int code) {
        if (code < 0 || code >= CODES.length) {
            return ExceptionCode.UNKNOWN;
        }
        ExceptionCode valid = CODES[code];
        if (valid == null) {
            return ExceptionCode.UNKNOWN;
        }
        return valid;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
