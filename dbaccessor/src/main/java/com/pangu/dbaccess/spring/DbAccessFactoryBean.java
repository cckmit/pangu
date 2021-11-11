package com.pangu.dbaccess.spring;

import com.pangu.core.anno.SingleService;
import com.pangu.dbaccess.service.EntityService;
import com.pangu.dbaccess.service.IDbServerAccessor;
import com.pangu.framework.socket.client.ClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DbAccessFactoryBean {

    @Bean
    @SingleService(EntityService.class)
    public static EntityService getService(ClientFactory clientFactory, IDbServerAccessor serverManager) {
        return new EntityService(serverManager, clientFactory);
    }
}