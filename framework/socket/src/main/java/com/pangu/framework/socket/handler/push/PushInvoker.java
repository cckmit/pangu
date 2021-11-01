package com.pangu.framework.socket.handler.push;

import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class PushInvoker implements InvocationHandler {
    private static final AtomicLong sequence = new AtomicLong(1);

    private final SessionManager sessionManager;

    private final Map<Method, PushDefine> methodDefines;

    private final Coder coder;
    private final Map<Byte, Coder> coders;

    public PushInvoker(SessionManager sessionManager, List<MethodDefine> methodDefines, Coder coder, Map<Byte, Coder> coders) {
        this.sessionManager = sessionManager;
        this.methodDefines = new HashMap<>(methodDefines.size());
        this.coder = coder;
        this.coders = coders;
        for (MethodDefine methodDefine : methodDefines) {
            this.methodDefines.put(methodDefine.getMethod(), new PushDefine(methodDefine));
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            if ("toString".equals(methodName)) {
                return method.toString();
            } else if ("hashCode".equals(methodName)) {
                return method.hashCode();
            }
        } else if (parameterTypes.length == 1 && "equals".equals(methodName)) {
            return method.equals(args[0]);
        }
        PushDefine pushDefine = methodDefines.get(method);
        if (pushDefine == null) {
            throw new SocketException(ExceptionCode.PUSH_NOT_FOUND);
        }
        Session[] sessions = null;
        List<Object> notFoundSessionIdentities = null;
        if (pushDefine.getParamIndex() >= 0) {
            boolean hasNotFountListeners = sessionManager.hasSessionNotFountListeners();
            if (hasNotFountListeners) {
                notFoundSessionIdentities = new ArrayList<>();
            }
            sessions = findSessions(args[pushDefine.getParamIndex()], notFoundSessionIdentities);
        } else if (pushDefine.isAllIdentity()) {
            sessions = sessionManager.getAllIdentitySession().toArray(new Session[0]);
        }
        if (sessions == null || sessions.length == 0) {
            return null;
        }
        MethodDefine methodDefine = pushDefine.getMethodDefine();

        Map<Byte, List<Session>> formatSession = new HashMap<>(2);
        int size = sessions.length;
        for (Session session : sessions) {
            Channel channel = session.getChannel();
            if (channel == null) {
                continue;
            }
            Attribute<Byte> formatAttr = channel.attr(Coder.LAST_CODER);
            Byte format = formatAttr.get();
            if (format == null) {
                format = coder.getFormat();
            }
            List<Session> list = formatSession.computeIfAbsent(format, k -> new ArrayList<>(size));
            list.add(session);
        }
        for (Map.Entry<Byte, List<Session>> entry : formatSession.entrySet()) {
            Byte format = entry.getKey();
            List<Session> list = entry.getValue();

            Coder coder = coders.get(format);

            Header header = Header.valueOf(coder.getFormat(), 0, next(), 0, methodDefine.getCommand());
            byte[] bytes = coder.encodeParameters(args, methodDefine.getParams());
            Message message = Message.valueOf(header, bytes, new byte[0]);

            for (Session session : list) {
                session.write(message);
            }
        }

        if (notFoundSessionIdentities != null && notFoundSessionIdentities.size() > 0) {
            sessionManager.notifyIdentitySessionNotFound(notFoundSessionIdentities, methodDefine, args);
        }

        return null;
    }

    private Session[] findSessions(Object identity, List<Object> notFoundSessionIdentities) {
        if (identity instanceof Session) {
            return new Session[]{((Session) identity)};
        }

        if (identity instanceof Collection) {
            Collection<?> identityCollection = (Collection<?>) identity;
            List<Session> sessions = new ArrayList<>(identityCollection.size());
            for (Object curIdentity : identityCollection) {
                foundIdentity(notFoundSessionIdentities, sessions, curIdentity);
            }
            return sessions.toArray(new Session[0]);
        }
        if (identity.getClass().isArray()) {
            int len = Array.getLength(identity);
            List<Session> sessions = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object curIdentity = Array.get(identity, i);
                foundIdentity(notFoundSessionIdentities, sessions, curIdentity);
            }
            return sessions.toArray(new Session[0]);
        }
        Session session = sessionManager.getIdentity(identity);
        if (session == null) {
            return new Session[0];
        }
        return new Session[]{session};
    }

    private void foundIdentity(List<Object> notFoundSessionIdentities, List<Session> sessions, Object o) {
        if (o instanceof Session) {
            sessions.add((Session) o);
        } else {
            Session session = sessionManager.getIdentity(o);
            if (session == null) {
                if (notFoundSessionIdentities != null) {
                    notFoundSessionIdentities.add(o);
                }
                return;
            }
            sessions.add(session);
        }
    }

    public static long next() {
        long result = sequence.incrementAndGet();
        return result & 0x007fffffffffffffL;
    }
}
