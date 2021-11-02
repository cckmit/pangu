package com.pangu.db.config;

import com.pangu.model.anno.ComponentDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.Reader;

@ComponentDb
@Slf4j
public class DbConfigBean implements FactoryBean<DbConfig> {

    private DbConfig dbConfig;

    @Override
    public DbConfig getObject() {
        if (dbConfig != null) {
            return dbConfig;
        }
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(options);
        ClassPathResource resource = new ClassPathResource("db.yml");
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            dbConfig = yaml.loadAs(reader, DbConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dbConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return DbConfig.class;
    }
}
