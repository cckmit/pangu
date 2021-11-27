package com.pangu.gateway.rout;

import com.pangu.core.anno.ComponentGate;
import com.pangu.core.common.ServerInfo;
import com.pangu.framework.socket.client.Client;
import com.pangu.framework.socket.client.ClientFactory;
import com.pangu.framework.socket.core.Header;
import com.pangu.framework.socket.core.Message;
import com.pangu.framework.socket.core.StateConstant;
import com.pangu.framework.socket.handler.MessageProcessor;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import com.pangu.framework.socket.handler.param.Attachment;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.gateway.server.GatewayServerManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ComponentGate
@Slf4j
public class RoutProcessor implements MessageProcessor {
    private GatewayServerManager serverManager;
    private final SessionManager sessionManager;

    private final ClientFactory clientFactory;

    public RoutProcessor(SessionManager sessionManager, ClientFactory clientFactory) {
        this.sessionManager = sessionManager;
        this.clientFactory = clientFactory;
    }

    @Override
    public void process(Session session, Message message) {
        List<ServerInfo> logicServers = serverManager.getLogicServers();
        if (logicServers == null || logicServers.isEmpty()) {
            Header header = responseHeader(message, session);
            header.addState(StateConstant.NO_LOGIC_EXCEPTION);
            Message res = Message.valueOf(header);
            session.write(res);
            return;
        }
        int idx = RandomUtils.nextInt(logicServers.size());
        ServerInfo serverInfo = logicServers.get(idx);
        Client client = clientFactory.getClient(serverInfo.getAddress());
        message.addState(StateConstant.REDIRECT);
        message.getHeader().setSession(session.getId());

        Long curIdentity = session.getIdentity();
        long longIdentity = curIdentity == null ? 0 : curIdentity;
        Attachment sessionAttach = new Attachment(longIdentity, session.getCtx(), Collections.emptyMap());
        byte[] bytes = JsonUtils.object2Bytes(sessionAttach);
        message.setAttachment(bytes);
        long originSn = message.getHeader().getSn();
        CompletableFuture<Message> send = client.send(message, false);
        send.whenComplete((Message resMsg, Throwable v) -> {
            if (v != null) {
                log.info("转发消息出现异常", v);
                return;
            }
            byte[] resAttach = resMsg.getAttachment();
            if (resAttach != null && resAttach.length != 0) {
                Attachment attachment = JsonUtils.bytes2Object(resAttach, Attachment.class);
                if (attachment != null) {
                    long identity = attachment.getIdentity();
                    if (identity > 0 && session.getIdentity() == null) {
                        sessionManager.bind(session, identity);
                    }
                    Map<String, String> attach = attachment.getAttach();
                    if (attach != null && attach.size() > 0) {
                        for (Map.Entry<String, String> entry : attach.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            session.putCtx(key, value);
                        }
                    }
                }
            }
            Header header = resMsg.getHeader();
            header.setSn(originSn);
            header.removeState(StateConstant.REDIRECT);
            session.write(resMsg);
        });
    }

    private Header responseHeader(Message message, Session session) {
        Header originHeader = message.getHeader();
        Header header = Header.valueOf(originHeader.getFormat(),
                0,
                originHeader.getSn(),
                0L,
                originHeader.getCommand());
        header.setSession(session.getId());
        header.addState(StateConstant.STATE_RESPONSE);
        if (originHeader.hasState(StateConstant.REDIRECT)) {
            header.addState(StateConstant.REDIRECT);
        }
        if (originHeader.hasState(StateConstant.HEART_BEAT)) {
            header.addState(StateConstant.HEART_BEAT);
        }
        return header;
    }

    public void init(GatewayServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
