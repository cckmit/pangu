package com.pangu.framework.socket.spring.protocol;

import com.pangu.framework.socket.anno.PushInject;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.socket.handler.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FacadeImpl implements Facade {

    @Value("${server.socket.firewall.allows}")
    private String value;

    @PushInject
    private FacadePush facadePush;

    @Autowired
    private SessionManager sessionManager;

    public FacadeImpl() {
        System.out.println();
    }

    @Override
    public PlayerVo say(Session session, String hello) {
        PlayerVo playerVo = new PlayerVo();
        playerVo.setName("res:" + hello);
        sessionManager.bind(session, 1L);
        facadePush.say(hello);
        return playerVo;
    }
}
