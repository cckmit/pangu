package com.pangu.framework.socket.spring;

import com.pangu.framework.socket.handler.SessionManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class ServerSpringConfiguration implements ImportBeanDefinitionRegistrar, BeanDefinitionRegistryPostProcessor {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        RootBeanDefinition dispatcherFactory = new RootBeanDefinition(DispatcherFactoryBean.class);
        String DISPATCHER_BEAN_NAME = "dispatcher";
        registry.registerBeanDefinition(DISPATCHER_BEAN_NAME, dispatcherFactory);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SessionManager.class);
        builder.addConstructorArgReference(DISPATCHER_BEAN_NAME);
        AbstractBeanDefinition sessionBeanDefine = builder.getBeanDefinition();
        registry.registerBeanDefinition("sessionManager", sessionBeanDefine);

        RootBeanDefinition pushInjectProcessor = new RootBeanDefinition(PushInjectProcessor.class);
        registry.registerBeanDefinition("pushInjectProcessor", pushInjectProcessor);

        RootBeanDefinition beanDefinition = new RootBeanDefinition(ServerFactoryBean.class);
        beanDefinition.setDependsOn(DISPATCHER_BEAN_NAME, "sessionManager");
        String SERVER_BEAN_NAME = "socketServer";
        registry.registerBeanDefinition(SERVER_BEAN_NAME, beanDefinition);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerBeanDefinitions(null ,registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
