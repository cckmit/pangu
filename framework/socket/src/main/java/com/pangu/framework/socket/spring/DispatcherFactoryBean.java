package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.handler.DefaultDispatcher;
import com.pangu.framework.socket.handler.param.Coder;
import com.pangu.framework.socket.handler.param.JsonCoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collection;

public class DispatcherFactoryBean implements FactoryBean<DefaultDispatcher>, BeanPostProcessor, InitializingBean {

    private DefaultDispatcher dispatcher;

    @Value("${socket.coder:1}")
    private byte defaultCoder;

    @Value("${socket.manage-thread:false}")
    private boolean manageUseThread;

    @Autowired(required = false)
    private Collection<Coder> coders;

    @Override
    public DefaultDispatcher getObject() throws Exception {
        return dispatcher;
    }

    @Override
    public Class<?> getObjectType() {
        return DefaultDispatcher.class;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        dispatcher.register(bean);
        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dispatcher = new DefaultDispatcher();
        if (coders == null || coders.isEmpty()) {
            dispatcher.addCoder(new JsonCoder());
            dispatcher.start();
            return;
        }
        for (Coder coder : coders) {
            dispatcher.addCoder(coder);
        }
        dispatcher.setDefaultCoder(defaultCoder);
        dispatcher.setManageUseThread(manageUseThread);
        dispatcher.start();
    }
}
