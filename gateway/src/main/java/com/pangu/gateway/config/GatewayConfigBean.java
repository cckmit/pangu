package com.pangu.gateway.config;

import com.pangu.core.anno.ComponentGate;
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

@ComponentGate
@Slf4j
public class GatewayConfigBean implements FactoryBean<GatewayConfig> {

    private GatewayConfig gatewayConfig;

    @Override
    public GatewayConfig getObject() {
        if (gatewayConfig != null) {
            return gatewayConfig;
        }
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(options);
        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        constructor.setPropertyUtils(propertyUtils);
        Yaml yaml = new Yaml(constructor);
        ClassPathResource resource = new ClassPathResource("gateway.yml");
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            gatewayConfig = yaml.loadAs(reader, GatewayConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gatewayConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return GatewayConfig.class;
    }
}
