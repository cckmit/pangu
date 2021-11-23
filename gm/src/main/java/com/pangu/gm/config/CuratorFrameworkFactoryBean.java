package com.pangu.gm.config;

import com.pangu.core.config.ZookeeperConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class CuratorFrameworkFactoryBean implements FactoryBean<CuratorFramework> {

    private final GMConfig gmConfig;

    public CuratorFrameworkFactoryBean(GMConfig gmConfig) {
        this.gmConfig = gmConfig;
    }

    @Override
    public CuratorFramework getObject() {
        ZookeeperConfig zookeeper = gmConfig.getZookeeper();
        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .connectString(zookeeper.getAddr())
                .retryPolicy(new RetryForever(3000))
                .build();
        framework.start();
        return framework;
    }

    @Override
    public Class<?> getObjectType() {
        return CuratorFramework.class;
    }
}
