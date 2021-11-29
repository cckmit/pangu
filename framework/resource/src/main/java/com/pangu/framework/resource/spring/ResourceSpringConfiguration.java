package com.pangu.framework.resource.spring;

import com.pangu.framework.resource.StorageManagerFactory;
import com.pangu.framework.resource.other.FormatDefinition;
import com.pangu.framework.resource.other.ResourceDefinition;
import com.pangu.framework.resource.reader.ExcelReader;
import com.pangu.framework.resource.reader.JsonReader;
import com.pangu.framework.resource.reader.ReaderHolder;
import com.pangu.framework.resource.reader.XlsxReader;
import com.pangu.framework.resource.schema.StaticInjectProcessor;
import com.pangu.framework.resource.support.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ResourceSpringConfiguration implements ImportBeanDefinitionRegistrar, BeanDefinitionRegistryPostProcessor {

    /**
     * 默认资源匹配符
     */
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /**
     * 资源搜索分析器，由它来负责检索EAO接口
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    /**
     * 类的元数据读取器，由它来负责读取类上的注释信息
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String className = importingClassMetadata.getClassName();
        Class<?> annoClz;
        try {
            annoClz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EnableResource annotation = annoClz.getAnnotation(EnableResource.class);
        ResourceFormat formatAnnotation = annotation.format();
        FormatDefinition format = new FormatDefinition(formatAnnotation.value(), formatAnnotation.type(), formatAnnotation.i18n(), formatAnnotation.suffix(), formatAnnotation.config());
        registerDepender(format, registry);

        registerConvert(registry);

        ManagedList<BeanDefinition> resources = new ManagedList<>();
        ResourcePackage[] value = annotation.value();
        for (ResourcePackage pack : value) {
            // 自动包扫描处理
            String packageName = pack.value();
            String[] names = getResources(packageName);
            for (String resource : names) {
                Class<?> clz;
                try {
                    clz = Class.forName(resource);
                } catch (ClassNotFoundException e) {
                    FormattingTuple message = MessageFormatter.format("无法获取的资源类[{}]", resource);
                    log.error(message.getMessage());
                    throw new RuntimeException(message.getMessage(), e);
                }
                BeanDefinition definition = parseResource(clz, format);
                resources.add(definition);
            }
        }
        ResourceClass[] resourceClasses = annotation.clz();
        for (ResourceClass resourceClass : resourceClasses) {
            Class<?> clz = resourceClass.value();
            BeanDefinition definition = parseResource(clz, format);
            resources.add(definition);
        }

        // 要创建的对象信息
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(StorageManagerFactory.class);
        factory.addPropertyValue("definitions", resources);
        registry.registerBeanDefinition("storageManager", factory.getBeanDefinition());
    }

    private void registerConvert(BeanDefinitionRegistry registry) {
//        <bean class="org.springframework.context.support.ConversionServiceFactoryBean">
//        <property name="converters">
//            <list>
//                <bean class="com.pangu.framework.resource.support.StringToDateConverter"/>
//                <bean class="com.pangu.framework.resource.support.StringToClassConverter"/>
//                <bean class="com.pangu.framework.resource.support.JsonToMapConverter"/>
//                <bean class="com.pangu.framework.resource.support.JsonToArrayConverter"/>
//                <bean class="com.pangu.framework.resource.support.JsonToCollectionConverter"/>
//                <bean class="com.pangu.framework.resource.support.JsonToObjectConverter"/>
//                <bean class="com.xa.shennu.game.utils.StringToNumberConverter"/>
//            </list>
//        </property>
//    </bean>
        if (registry.containsBeanDefinition("conversionService")) {
            return;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ConversionServiceFactoryBean.class);
        ManagedList<BeanDefinition> resources = new ManagedList<>();
        resources.add(new RootBeanDefinition(StringToDateConverter.class));
        resources.add(new RootBeanDefinition(StringToClassConverter.class));
        resources.add(new RootBeanDefinition(JsonToMapConverter.class));
        resources.add(new RootBeanDefinition(JsonToArrayConverter.class));
        resources.add(new RootBeanDefinition(JsonToCollectionConverter.class));
        resources.add(new RootBeanDefinition(JsonToObjectConverter.class));
        resources.add(new RootBeanDefinition(StringToNumberConverter.class));
        builder.addPropertyValue("converters", resources);
        registry.registerBeanDefinition("conversionService", builder.getBeanDefinition());
    }

    private void registerDepender(FormatDefinition format, BeanDefinitionRegistry registry) {
        // 注册 XlsxReader
        String name = StringUtils.uncapitalize(XlsxReader.class.getSimpleName());
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(XlsxReader.class);
        registry.registerBeanDefinition(name, factory.getBeanDefinition());

        // 注册 ExcelReader
        name = StringUtils.uncapitalize(ExcelReader.class.getSimpleName());
        factory = BeanDefinitionBuilder.rootBeanDefinition(ExcelReader.class);
        registry.registerBeanDefinition(name, factory.getBeanDefinition());

        // 注册 JsonReader
        name = StringUtils.uncapitalize(JsonReader.class.getSimpleName());
        factory = BeanDefinitionBuilder.rootBeanDefinition(JsonReader.class);
        registry.registerBeanDefinition(name, factory.getBeanDefinition());

        name = StringUtils.uncapitalize(ReaderHolder.class.getSimpleName());
        factory = BeanDefinitionBuilder.rootBeanDefinition(ReaderHolder.class);
        factory.addPropertyValue(ReaderHolder.FORMAT_SETTER, format);
        registry.registerBeanDefinition(name, factory.getBeanDefinition());

        name = StringUtils.uncapitalize(StaticInjectProcessor.class.getSimpleName());
        factory = BeanDefinitionBuilder.rootBeanDefinition(StaticInjectProcessor.class);
        registry.registerBeanDefinition(name, factory.getBeanDefinition());
    }

    private String[] getResources(String packageName) {
        try {
            // 搜索资源
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            // 提取资源
            Set<String> result = new HashSet<String>();
            String name = com.pangu.framework.resource.anno.Resource.class.getName();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                // 判断是否静态资源
                MetadataReader metaReader = this.metadataReaderFactory.getMetadataReader(resource);
                AnnotationMetadata annoMeta = metaReader.getAnnotationMetadata();
                if (!annoMeta.hasAnnotation(name)) {
                    continue;
                }
                ClassMetadata clzMeta = metaReader.getClassMetadata();
                result.add(clzMeta.getClassName());
            }

            return result.toArray(new String[0]);
        } catch (IOException e) {
            String message = "无法读取资源信息";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private BeanDefinition parseResource(Class<?> clz, FormatDefinition format) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ResourceDefinition.class);
        builder.addConstructorArgValue(clz);
        builder.addConstructorArgValue(format);
        return builder.getBeanDefinition();
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerBeanDefinitions(null, registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
