package com.pangu.framework.socket.handler;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.pangu.framework.socket.core.Command;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import com.pangu.framework.socket.exception.ExceptionCode;
import com.pangu.framework.socket.exception.SocketException;
import com.pangu.framework.socket.handler.command.CommandRegister;
import com.pangu.framework.socket.handler.command.MethodDefine;
import com.pangu.framework.socket.handler.command.MethodProcessor;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.handler.param.JsonCoder;
import com.pangu.framework.socket.handler.param.Parameters;
import com.pangu.framework.socket.monitor.SocketCatCollector;
import com.pangu.framework.utils.ManagedException;
import com.pangu.framework.utils.codec.ZlibUtils;
import com.pangu.framework.utils.lang.ByteUtils;
import com.pangu.framework.utils.model.Result;
import com.pangu.framework.utils.thread.AbortPolicyWithReport;
import com.pangu.framework.utils.thread.NamedThreadFactory;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultDispatcher implements Dispatcher {

    // 指令注册
    private final CommandRegister commandRegister = new CommandRegister();

    @Setter
    private MessageProcessor messageProcessor;

    private final Map<Byte, Coder> coders = new HashMap<>();

    // 默认编解码器
    private Coder defaultCoder;

    // 同步线程池
    @Getter
    private static SyncSupport syncSupport;

    // 业务线程池
    @Getter
    private static ThreadPoolExecutor[] messagePoolExecutors;

    // 管理后台线程池
    @Getter
    private static ThreadPoolExecutor managedExecutorService;

    // 线程池引用数量
    private static final AtomicInteger threadPoolRef = new AtomicInteger();

    // 业务线程池数量
    @Setter
    private int thread;

    // 管理后台ip地址接口使用独立线程池
    @Setter
    private boolean manageUseThread;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void start() {
        boolean change = running.compareAndSet(false, true);
        if (!change) {
            return;
        }
        SocketCatCollector.register(this);
        if (thread <= 0) {
            int processors = Runtime.getRuntime().availableProcessors();
            thread = calDefaultThreadCount(processors);
        }
        if (defaultCoder == null) {
            defaultCoder = new JsonCoder();
        }
        coders.put(defaultCoder.getFormat(), defaultCoder);
        if (threadPoolRef.incrementAndGet() == 1) {
            syncSupport = new SyncSupport();
            {
                ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(3000);
                String threadName = "管理后台处理请求线程";
                AbortPolicyWithReport policy = new AbortPolicyWithReport(threadName);
                managedExecutorService = new ThreadPoolExecutor(thread + 1,
                        thread + 1,
                        0,
                        TimeUnit.MINUTES,
                        workQueue,
                        new NamedThreadFactory(threadName),
                        policy);
                messagePoolExecutors = new ThreadPoolExecutor[thread + 1];
            }
            {
                String threadName = "服务器通讯线程";
                AbortPolicyWithReport policy = new AbortPolicyWithReport(threadName);
                for (int i = 0; i < thread + 1; ++i) {
                    ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(3_000);
                    messagePoolExecutors[i] = new ThreadPoolExecutor(1,
                            1,
                            0,
                            TimeUnit.MILLISECONDS,
                            workQueue,
                            new NamedThreadFactory(threadName + "-" + i),
                            policy);
                }
            }
        }
    }

    static int calDefaultThreadCount(int processors) {
        int pow2 = pow2(processors) - 1;
        if (pow2 > processors) {
            pow2 = pow2 >>> 1;
        }
        return Math.max(1, pow2);
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     * See Hackers Delight, sec 3.2
     */
    private static int pow2(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= 1 << 30) ? 1 << 30 : n + 1;
    }

    @Override
    public void receive(Message message, Session session) {
        if (message.getHeader().isResponse()) {
            log.debug("推送请求收到客户端响应，直接忽视[{}][{}]", session, message);
            return;
        }
        MethodProcessor processor = commandRegister.getProcessor(message.getHeader().getCommand());
        if (processor == null) {
            if (messageProcessor == null) {
                commandNotFound(message, session);
                return;
            }
            messageProcessor.process(session, message);
            return;
        }
        MethodDefine define = processor.getMethodDefine();
        Parameters params = define.getParams();

        if (checkRequestNotValid(message, session, params)) return;

        String syncQueueName = define.getSyncQueueName();
        // 同步线程
        if (StringUtils.isNotBlank(syncQueueName)) {
            syncSupport.run(syncQueueName, () -> processRequest(processor, message, session));
            return;
        }
        // 管理后台线程
        if (define.isManager()) {
            managedExecutorService.submit(() -> processRequest(processor, message, session));
            return;
        }

        // 玩家线程尽量绑定到固定线程ID，防止一个玩家出问题导致所有玩家出问题
        long sessionId = session.getId();
        if (session.getIdentity() != null) {
            sessionId = session.getIdentity().hashCode();
        }
        ThreadPoolExecutor executor = messagePoolExecutors[(int) (sessionId & thread)];
        executor.submit(() -> processRequest(processor, message, session));
    }

    private void commandNotFound(Message message, Session session) {
        Header header = responseHeader(message, session);
        header.addState(StateConstant.COMMAND_NOT_FOUND);
        Message res = Message.valueOf(header);
        session.write(res);
    }

    private Header responseHeader(Message message, Session session) {
        Header originHeader = message.getHeader();
        long originHeaderSession = originHeader.getSession();
        Header header = Header.valueOf(originHeader.getFormat(),
                0,
                originHeader.getSn(),
                originHeaderSession,
                originHeader.getCommand());
        if (originHeaderSession == 0) {
            header.setSession(session.getId());
        }
        header.addState(StateConstant.STATE_RESPONSE);
        if (originHeader.hasState(StateConstant.REDIRECT)) {
            header.addState(StateConstant.REDIRECT);
        }
        if (originHeader.hasState(StateConstant.HEART_BEAT)) {
            header.addState(StateConstant.HEART_BEAT);
        }
        return header;
    }

    private void processRequest(MethodProcessor processor, Message message, Session session) {
        if (message.hasState(StateConstant.STATE_COMPRESS)) {
            byte[] body = message.getBody();
            if (body != null && body.length > 0) {
                Command command = message.getHeader().getCommand();
                Transaction transaction = Cat.newTransaction("socket.handler.decompress", command.getModule() + "_" + command.getCommand());
                try {
                    byte[] unzipBody = ZlibUtils.unzip(body);
                    message.removeState(StateConstant.STATE_COMPRESS);
                    message = Message.valueOf(message.getHeader(), unzipBody, message.getAttachment());
                } catch (Exception e) {
                    transaction.setStatus(e);
                    Header header = responseHeader(message, session);
                    header.addError(StateConstant.DECODE_EXCEPTION);
                    log.warn("会话[{}]SESSION解压缩异常，连接[{}]将被强制关闭", session.getId(), session.getRemoteAddress());
                    session.write(Message.valueOf(header));
                    session.close();
                    return;
                } finally {
                    transaction.complete();
                }
            }
        }
        byte format = message.getHeader().getFormat();
        Coder coder = coders.get(format);
        // 设置默认编解码类型
        session.getChannel().attr(Coder.LAST_CODER).set(format);

        MethodDefine define = processor.getMethodDefine();
        Parameters params = define.getParams();
        Header header = responseHeader(message, session);
        Command command = header.getCommand();
        String commandId = command.getModule() + "_" + command.getCommand();
        Transaction transaction = Cat.newTransaction("socket.handler.process", commandId);
        transaction.addData("identity", session.getIdentity());
        transaction.addData("input", message.getBody().length);
        try {
            CompletableFuture<?> completableFuture = null;
            if (params.isFuture()) {
                completableFuture = new CompletableFuture<>();
                Message finalMessage = message;
                completableFuture.whenComplete((result, thr) -> {
                    if (thr != null) {
                        processException(thr, header, coder, define, session, null);
                        return;
                    }
                    byte[] body;
                    if (result != null) {
                        if (result instanceof byte[] && define.isResponseRaw()) {
                            body = (byte[]) result;
                        } else {
                            body = coder.encodeResponse(result);
                        }
                    } else {
                        body = new byte[0];
                    }
                    byte[] attachment = finalMessage.getAttachment();
                    byte[] resAttach = null;
                    if (attachment != null && attachment.length >= 8) {
                        resAttach = new byte[16];
                        byte[] responseTimestamp = ByteUtils.longToByte(System.currentTimeMillis());
                        System.arraycopy(attachment, 0, resAttach, 0, 8);
                        System.arraycopy(responseTimestamp, 0, resAttach, 8, 8);
                    }
                    Message resMsg = Message.valueOf(header, body, resAttach);
                    session.write(resMsg);
                });
            }
            Object[] methodParameters = coder.decodeRequest(message, session, params, completableFuture);
            Object result = processor.process(methodParameters);

            if (completableFuture != null || define.isIgnoreResponse()) {
                return;
            }
            byte[] body;
            if (result != null) {
                if (result instanceof byte[] && define.isResponseRaw()) {
                    body = (byte[]) result;
                } else {
                    body = coder.encodeResponse(result);
                }
            } else {
                body = new byte[0];
            }
            transaction.addData("output", body.length);
            byte[] attachment = message.getAttachment();
            byte[] resAttach = null;
            if (attachment != null && attachment.length >= 8) {
                resAttach = new byte[16];
                byte[] responseTimestamp = ByteUtils.longToByte(System.currentTimeMillis());
                System.arraycopy(attachment, 0, resAttach, 0, 8);
                System.arraycopy(responseTimestamp, 0, resAttach, 8, 8);
            }
            if (!header.hasState(StateConstant.STATE_COMPRESS) && body.length > StateConstant.COMPRESS_LIMIT) {
                body = ZlibUtils.zip(body);
                header.addState(StateConstant.STATE_COMPRESS);
            }
            Message resMsg = Message.valueOf(header, body, resAttach);
            session.write(resMsg);
        } catch (Throwable throwable) {
            processException(throwable, header, coder, define, session, transaction);
        } finally {
            transaction.complete();
        }
    }

    private void processException(Throwable unknown, Header header, Coder coder, MethodDefine define, Session session, Transaction transaction) {
        Throwable e = getRealException(unknown);
        if (e instanceof ManagedException) {
            header.addError(StateConstant.MANAGED_EXCEPTION);
            byte[] bytes = doWithManageExceptionResult(coder, define, ((ManagedException) e).getCode());
            log.debug("managed业务[{}]异常", header, e);
            session.write(Message.valueOf(header, bytes, null));
            return;
        }
        if (transaction != null) {
            if (e != null) {
                String msg = e.getClass().getName() + ":" + e.getMessage();
                transaction.setStatus(msg);
            } else {
                transaction.setStatus("unknow error");
            }
        }
        if (e instanceof SocketException) {
            SocketException socketException = (SocketException) e;
            ExceptionCode code = socketException.getCode();
            header.addError(code.getCode());
            session.write(Message.valueOf(header));
            log.warn("socket异常[{}]", header, e);
            if (code == ExceptionCode.IDENTITY_PARAMETER || code == ExceptionCode.MANAGED_IP || code == ExceptionCode.SESSION_PARAM) {
                session.close();
            }
            return;
        }
        log.error("[{}]异常", header, e);
        header.addError(StateConstant.UNKNOWN_EXCEPTION);
        session.write(Message.valueOf(header));
    }

    private Throwable getRealException(Throwable e) {
        while ((e instanceof InvocationTargetException) || (e instanceof UndeclaredThrowableException)) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                continue;
            }
            e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
        }
        return e;
    }

    private byte[] doWithManageExceptionResult(Coder coder, MethodDefine define, int code) {
        Type responseType = define.getResponse();
        byte[] errorBody;
        // Result<T>型返回值
        if ((responseType instanceof ParameterizedType && ((ParameterizedType) responseType).getRawType() == Result.class)
                || responseType == Result.class) {
            errorBody = coder.encodeResponse(Result.ERROR(code));
        } else if (responseType == Integer.class || responseType == int.class) {
            errorBody = coder.encodeResponse(code);
        } else {
            errorBody = new byte[0];
        }
        return errorBody;
    }

    public boolean checkRequestNotValid(Message message, Session session, Parameters params) {
        if (params.isIdentity()) {
            if (session.getIdentity() == null) {
                Header header = responseHeader(message, session);
                header.addError(StateConstant.IDENTITY_EXCEPTION);
                session.write(Message.valueOf(header));
                return true;
            }
        }
        if (params.isManager()) {
            Channel channel = session.getChannel();
            Attribute<Boolean> manager = channel.attr(Session.MANAGER);
            if (manager.get() == null || !manager.get()) {
                Header header = responseHeader(message, session);
                header.addError(StateConstant.MANAGE_IP_EXCEPTION);
                session.write(Message.valueOf(header));
                return true;
            }
        }
        String[] sessionKeys = params.getSessionKeys();
        if (sessionKeys != null && sessionKeys.length > 0) {
            for (String key : sessionKeys) {
                Object value = session.getCtx(key);
                if (value == null) {
                    Header header = responseHeader(message, session);
                    header.addError(StateConstant.SESSION_EXCEPTION);
                    session.write(Message.valueOf(header));
                    return true;
                }
            }
        }
        return false;
    }

    public void register(Object object) {
        commandRegister.register(object);
    }

    public void addCoder(Coder coder) {
        Coder pre = coders.putIfAbsent(coder.getFormat(), coder);
        if (pre != null) {
            log.debug("编码格式 " + coder.getFormat() + "编解码器冲突" + coder);
        }
    }

    public void setDefaultCoder(byte defaultCoder) {
        Coder coder = coders.get(defaultCoder);
        if (coder == null) {
            return;
        }
        this.defaultCoder = coder;
    }

    @Override
    public Coder getDefaultCoder() {
        return defaultCoder;
    }

    @Override
    public Map<Byte, Coder> getCoders() {
        return coders;
    }

    @Override
    public void shutdown() {
        int current = threadPoolRef.decrementAndGet();
        if (current > 0) {
            return;
        }
        if (messagePoolExecutors != null) {
            for (ThreadPoolExecutor executor : messagePoolExecutors) {
                executor.shutdownNow();
            }
        }
        syncSupport.shutdown();
        if (managedExecutorService != null) {
            managedExecutorService.shutdownNow();
        }
    }
}
