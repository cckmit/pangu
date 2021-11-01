package com.pangu.framework.socket.exception;

/**
 * 通信异常
 */
public class SocketException extends RuntimeException {

    private static final long serialVersionUID = -721014715061800917L;

    private final ExceptionCode code;

    public SocketException(ExceptionCode code, Throwable cause) {
        super(code.getMsg(), cause);
        this.code = code;
    }

    public SocketException(ExceptionCode code) {
        super(code.getMsg());
        this.code = code;
    }

    public SocketException(ExceptionCode code, String message, Throwable cause) {
        super(code.getMsg() + message, cause);
        this.code = code;
    }

    public SocketException(ExceptionCode code, String message) {
        super(code.getMsg() + message);
        this.code = code;
    }

    public ExceptionCode getCode() {
        return code;
    }
}
