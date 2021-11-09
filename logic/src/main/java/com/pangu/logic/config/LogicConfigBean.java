package com.pangu.logic.config;

import com.pangu.core.anno.ComponentLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;

@ComponentLogic
@Slf4j
public class LogicConfigBean implements FactoryBean<LogicConfig> {

    private LogicConfig logicConfig;

    @Override
    public LogicConfig getObject() {
        if (logicConfig != null) {
            return logicConfig;
        }
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(options);
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(propertyUtils);
        Yaml yaml = new Yaml(constructor);
        ClassPathResource resource = new ClassPathResource("logic.yml");
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            logicConfig = yaml.loadAs(reader, LogicConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return logicConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return LogicConfig.class;
    }
}
