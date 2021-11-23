package com.pangu.gm.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.shell.converters.ArrayConverter;
import org.springframework.shell.converters.AvailableCommandsConverter;
import org.springframework.shell.converters.FileConverter;
import org.springframework.shell.converters.SimpleFileConverter;
import org.springframework.shell.legacy.LegacyMethodTargetRegistrar;
import org.springframework.shell.legacy.LegacyParameterResolver;

import java.io.File;

@Configuration
@ComponentScan(
        basePackageClasses = {ArrayConverter.class},
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {AvailableCommandsConverter.class, SimpleFileConverter.class}
        )}
)
public class ShellConfiguration {
    public ShellConfiguration() {
    }

    @Bean
    public LegacyMethodTargetRegistrar legacyMethodTargetResolver() {
        return new LegacyMethodTargetRegistrar();
    }

    @Bean
    public LegacyParameterResolver legacyParameterResolver() {
        return new LegacyParameterResolver();
    }

    @Bean
    public FileConverter fileConverter() {
        return new FileConverter() {
            protected File getWorkingDirectory() {
                return new File(".");
            }
        };
    }
}
