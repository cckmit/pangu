package com.pangu.framework.socket.handler.param;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Attachment {
    private long identity;

    private Map<String, String> attach;

    private Map<String, String> once;

    public void identity(long identity) {
        this.identity = identity;
    }

    public void putAddition(String key, String value) {
        if (attach == null) {
            attach = new HashMap<>(2);
        }
        this.attach.put(key, value);
    }

    public void putOnce(String key, String value) {
        if (this.once == null) {
            this.once = new HashMap<>(2);
        }
        this.once.put(key, value);
    }

    public boolean notEmpty() {
        if (identity > 0) {
            return true;
        }
        if (attach != null) {
            return true;
        }
        return once != null;
    }
}
