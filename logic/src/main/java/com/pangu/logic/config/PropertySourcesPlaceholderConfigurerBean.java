package com.pangu.logic.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class PropertySourcesPlaceholderConfigurerBean {

    @Bean
    public static PropertySourcesPlaceholderConfigurer getBean(){
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new ClassPathResource("logic.yml"));
        Properties object = bean.getObject();
        if (object == null) {
            return configurer;
        }
        Map<String, Object> map = new HashMap<>(object.size());
        for (Map.Entry<Object, Object> entry : object.entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            map.put(k.toString(), v);
        }
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addLast(new SystemEnvironmentPropertySource("logic.yml", map) {
        });
        configurer.setPropertySources(propertySources);
        return configurer;
    }
}
