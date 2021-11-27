package com.pangu.framework.socket.handler;

import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.utils.collection.CopyOnWriteHashMap;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.SocketAddress;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Session {

    public static final String IDENTITY = "identity";
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.newInstance("session_key");

    public static final AttributeKey<Long> CREATE_TIME = AttributeKey.newInstance("session_create_time");

    public static final String MANAGEMENT = "management";
    public static final AttributeKey<Boolean> MANAGER = AttributeKey.newInstance(MANAGEMENT);

    // session id
    private long id;

    // 授权ID
    private Long identity;

    // netty连接
    private Channel channel;

    // 最近访问时间
    private long lastTime;

    // 上下文
    private final Map<String, String> ctx = new CopyOnWriteHashMap<>();

    public Session(long id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.lastTime = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public Long getIdentity() {
        return identity;
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    void updateId(long id) {
        this.id = id;
    }

    public boolean isClosed() {
        return channel == null;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    synchronized void clearChannel() {
        this.channel = null;
    }

    synchronized Channel attachChannel(Channel channel) {
        Channel preChannel = this.channel;
        this.channel = channel;
        return preChannel;
    }

    public String getCtx(String key) {
        return ctx.get(key);
    }

    public void write(Message message) {
        if (isClosed()) {
            return;
        }
        channel.writeAndFlush(message);
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public void updateIdentity(Long identity) {
        this.identity = identity;
        ctx.put(IDENTITY, identity.toString());
    }

    public void putCtx(String key, String value) {
        ctx.put(key, value);
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", identity=" + identity +
                ", channel=" + channel +
                ", lastTime=" + lastTime +
                '}';
    }

    public byte getFormat() {
        if (channel == null) {
            return 0;
        }
        Attribute<Byte> attr = channel.attr(Coder.LAST_CODER);
        Byte preByte = attr.get();
        return preByte == null ? 0 : preByte;
    }
}
