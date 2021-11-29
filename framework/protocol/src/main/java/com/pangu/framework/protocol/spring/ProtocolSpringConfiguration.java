package com.pangu.framework.protocol.spring;

import com.pangu.framework.protocol.IndexedClass;
import com.pangu.framework.protocol.TransferFactory;
import com.pangu.framework.protocol.annotation.Transable;
import com.pangu.framework.protocol.schema.SchemaNames;
import com.pangu.framework.utils.model.Page;
import com.pangu.framework.utils.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SystemPropertyUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ProtocolSpringConfiguration implements ImportBeanDefinitionRegistrar, BeanDefinitionRegistryPostProcessor {

    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        String coder = "com.pangu.framework.socket.handler.param.ProtocolCoder";
        BeanDefinitionBuilder protocolCoder = BeanDefinitionBuilder.rootBeanDefinition(coder);
        registry.registerBeanDefinition("protocolCoder", protocolCoder.getBeanDefinition());

        String className = importingClassMetadata.getClassName();
        Class<?> annoClz;
        try {
            annoClz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        EnableProtocol annotation = annoClz.getAnnotation(EnableProtocol.class);
        ProtocolClass[] classes = annotation.clz();
        List<IndexedClass> resources = new ManagedList<>();
        for (ProtocolClass item : classes) {
            Class<?> cc = item.clz();
            if (cc == Result.class) {
                continue;
            }
            if (cc == Page.class) {
                continue;
            }
            int index = item.index();
            // 不检查标注
            IndexedClass ic = new IndexedClass(cc, index);
            resources.add(ic);
        }
        resources.add(new IndexedClass(Result.class, 1));
        resources.add(new IndexedClass(Page.class, 2));

        ProtocolPackage[] packages = annotation.packages();
        if (packages.length == 0) {
            String packageName = annoClz.getPackage().getName();
            List<IndexedClass> indexedClasses = scanPackage(10, packageName);
            resources.addAll(indexedClasses);
        } else {
            for (ProtocolPackage pack : packages) {
                String packageName = pack.path();
                List<IndexedClass> indexedClasses = scanPackage(pack.index(), packageName);
                resources.addAll(indexedClasses);
            }
        }
        Collections.sort(resources);
        // 要创建的对象信息
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TransferFactory.class);
        factory.addPropertyValue("transables", resources);
        AbstractBeanDefinition beanDefinition = factory.getBeanDefinition();
        registry.registerBeanDefinition("transfer", beanDefinition);
    }

    private List<IndexedClass> scanPackage(int index, String packageName) {
        List<IndexedClass> resources = new ArrayList<>();
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
            IndexedClass ic = new IndexedClass(clz, index);
            resources.add(ic);
            if (index > 0) {
                index++;
            }
        }
        return resources;
    }

    private String[] getResources(String packageName) {
        try {
            long s = System.currentTimeMillis();

            // 搜索资源
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + resolveBasePackage(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            // 提取资源
            Set<String> result = new HashSet<String>();
            String name = Transable.class.getName();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                // 判断是否静态资源
                MetadataReader metaReader = this.metadataReaderFactory.getMetadataReader(resource);
                ClassMetadata clzMeta = metaReader.getClassMetadata();
                if (clzMeta.isInterface() || clzMeta.isAbstract()) {
                    // 忽略无法实例化的接口和抽象类
                    continue;
                }

                AnnotationMetadata annoMeta = metaReader.getAnnotationMetadata();
                if (annoMeta.hasAnnotation(name)) {
                    result.add(clzMeta.getClassName());
                } else {
                    // 接口是否有标注
                    String[] interfaceNames = clzMeta.getInterfaceNames();
                    for (String interfaceName : interfaceNames) {
                        MetadataReader interfaceReader = this.metadataReaderFactory.getMetadataReader(interfaceName);
                        if (interfaceReader.getAnnotationMetadata().hasAnnotation(name)) {
                            result.add(clzMeta.getClassName());
                            break;
                        }
                    }
                }
            }
            log.debug("解析协议包扫描耗时 {} 毫秒", (System.currentTimeMillis() - s));
            ArrayList<String> sort = new ArrayList<>(result);
            Collections.sort(sort);
            return sort.toArray(new String[0]);
        } catch (IOException e) {
            String message = "无法读取资源信息";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
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
