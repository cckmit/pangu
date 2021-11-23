package com.pangu.gm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;

@Configuration
@Slf4j
public class GMConfigBean implements FactoryBean<GMConfig> {

    private GMConfig GMConfig;

    @Override
    public GMConfig getObject() {
        if (GMConfig != null) {
            return GMConfig;
        }
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(options);
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(propertyUtils);
        Yaml yaml = new Yaml(constructor);
        ClassPathResource resource = new ClassPathResource("gm.yml");
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            GMConfig = yaml.loadAs(reader, GMConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return GMConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return GMConfig.class;
    }
}
