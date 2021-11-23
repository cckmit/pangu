package com.pangu.stress.config;

import com.pangu.core.anno.ConfigurationStress;
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

@ConfigurationStress
@Slf4j
public class StressConfigBean implements FactoryBean<StressConfig> {

    private StressConfig stressConfig;

    @Override
    public StressConfig getObject() {
        if (stressConfig != null) {
            return stressConfig;
        }
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(options);
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(propertyUtils);
        Yaml yaml = new Yaml(constructor);
        ClassPathResource resource = new ClassPathResource("stress.yml");
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            stressConfig = yaml.loadAs(reader, StressConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stressConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return StressConfig.class;
    }
}
