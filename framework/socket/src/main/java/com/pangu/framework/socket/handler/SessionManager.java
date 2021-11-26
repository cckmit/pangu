package com.pangu.framework.socket.handler;

import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.push.PushManger;
import com.pangu.framework.socket.handler.session.IdentitySessionCloseListener;
import com.pangu.framework.socket.handler.session.SessionNotFountListener;
import com.pangu.framework.socket.utils.IpUtils;
import com.pangu.framework.socket.utils.ServerIdGenerator;
import com.pangu.framework.utils.lang.ByteUtils;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SessionManager {

    // 所有Session
    private final ConcurrentHashMap<Long, Session> anonymous = new ConcurrentHashMap<>(200);

    // 已授权session
    private final ConcurrentHashMap<Object, Session> identities = new ConcurrentHashMap<>(200);

    // 连接关闭监听器
    private final CopyOnWriteArrayList<IdentitySessionCloseListener> closeListeners = new CopyOnWriteArrayList<>();

    // 推送时找不到Identity session时回调
    private final CopyOnWriteArrayList<SessionNotFountListener> sessionNotFountListeners = new CopyOnWriteArrayList<>();

    private final AtomicLong sequence = new AtomicLong(0x00FFFF00L);

    private final PushManger pushManger;

    private final ServerIdGenerator idGenerator = new ServerIdGenerator();

    public SessionManager(Dispatcher dispatcher) {
        this.pushManger = new PushManger(this, dispatcher.getDefaultCoder(), dispatcher.getCoders());
    }

    public Session createSession(Channel channel) {
        long id = nextId();

        Session session = new Session(id, channel);
        Session absent = anonymous.putIfAbsent(id, session);
        if (absent != null && absent != session) {
            int times = 1;
            while (absent != null && absent != session) {
                if (times >= 10) {
                    log.error("无法成功创建SessionID[{}][{}]", id, anonymous.size());
                    throw new SocketException(ExceptionCode.SESSION_CREATE);
                }
                log.error("SESSION ID [{}] 已被使用 , 重试[{}]生成...", id, times++);
                id = nextId();
                session.updateId(id);
                absent = anonymous.putIfAbsent(id, session);
            }
        }
        Attribute<Session> sessionKey = channel.attr(Session.SESSION_KEY);
        sessionKey.set(session);
        Attribute<Boolean> manageAttr = channel.attr(Session.MANAGER);
        Boolean manage = manageAttr.get();
        if (manage != null && manage) {
            session.putCtx(Session.MANAGEMENT, IpUtils.getIp(channel));
        }
        return session;
    }

    public Session lookup(Channel channel) {
        Attribute<Session> sessionKey = channel.attr(Session.SESSION_KEY);
        return sessionKey.get();
    }

    public void detach(Channel channel) {
        Attribute<Session> sessionKey = channel.attr(Session.SESSION_KEY);
        Session session = sessionKey.get();
        if (session == null) {
            return;
        }
        sessionKey.set(null);
        session.clearChannel();
        Object identity = session.getIdentity();
        if (identity == null) {
            anonymous.remove(session.getId());
        } else {
            boolean remove = identities.remove(identity, session);
            // 只有成功删除，才代表是普通关闭，否则是session替换
            if (remove) {
                notifyIdentityClose(identity, channel);
            }
        }
    }

    public void bind(Session session, Long identity) {
        if (identity == null) {
            throw new IllegalArgumentException("session 身份不可为空");
        }
        session.updateIdentity(identity);
        Session preSession = identities.put(identity, session);
        if (preSession != null) {
            if (preSession == session) {
                return;
            }
            preSession.close();
        }
        anonymous.remove(session.getId(), session);
    }

    public Session attach(Channel channel, Message msg) {
        Session session = lookup(channel);
        long sessionId = msg.getHeader().getSession();
        if (sessionId == session.getId()) {
            return session;
        }
        if (sessionId == 0 || sessionId == -1) {
            return session;
        }
        return session;
    }

    public Set<Session> kick(Object... identities) {
        Set<Session> sessions = new HashSet<>(identities.length);
        for (Object identity : identities) {
            Session session = getIdentity(identity);
            if (session == null) {
                continue;
            }
            try {
                session.close();
            } catch (Exception e) {
                log.info("强制关闭意外信息", e);
            }
            sessions.add(session);
        }
        return sessions;
    }

    public void kickAll() {
        for (Map.Entry<Long, Session> entry : anonymous.entrySet()) {
            Session v = entry.getValue();
            v.close();
        }
        for (Map.Entry<Object, Session> entry : identities.entrySet()) {
            Session v = entry.getValue();
            v.close();
        }
    }

    long nextId() {
        return idGenerator.getNext();
    }

    public Collection<Session> getAllIdentitySession() {
        return identities.values();
    }

    public Session getIdentity(Object o) {
        return identities.get(o);
    }

    public Session getAnonymous(long o) {
        return anonymous.get(o);
    }

    public <T> T getPushProxy(Class<T> clz) {
        return pushManger.getProxy(clz);
    }

    public int identityAmount() {
        return identities.size();
    }

    public int anonymousAmount() {
        return anonymous.size();
    }

    public int connectAmount() {
        return identityAmount() + anonymousAmount();
    }

    public boolean isOnline(Object identity) {
        Session session = identities.get(identity);
        if (session == null) {
            return false;
        }
        return session.isConnected();
    }

    private void notifyIdentityClose(Object identity, Channel channel) {
        if (closeListeners.isEmpty()) {
            return;
        }
        for (IdentitySessionCloseListener listener : closeListeners) {
            try {
                listener.close(identity, channel);
            } catch (Exception e) {
                log.error("server回调session关闭listener异常");
            }
        }
    }

    public void notifyIdentitySessionNotFound(List<Object> identities, MethodDefine methodDefine, Object[] args) {
        if (identities == null || identities.isEmpty()) {
            return;
        }
        if (sessionNotFountListeners.isEmpty()) {
            return;
        }
        for (SessionNotFountListener listener : sessionNotFountListeners) {
            listener.notify(identities, methodDefine, args);
        }
    }

    public Set<Object> getAllIdentity() {
        return identities.keySet();
    }

    public void addListener(IdentitySessionCloseListener identitySessionCloseListener) {
        closeListeners.add(identitySessionCloseListener);
    }

    public void addListener(SessionNotFountListener identitySessionCloseListener) {
        sessionNotFountListeners.add(identitySessionCloseListener);
    }

    public boolean hasSessionNotFountListeners() {
        return sessionNotFountListeners.size() > 0;
    }

    public ServerIdGenerator getIdGenerator() {
        return idGenerator;
    }
}
