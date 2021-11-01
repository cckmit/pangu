package com.pangu.framework.protocol.schema;

import com.pangu.framework.protocol.annotation.Transable;
import com.pangu.framework.protocol.IndexedClass;
import com.pangu.framework.protocol.TransferFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

/**
 * 配置定义处理器
 * @author frank
 */
public class ConfigDefinitionParser extends AbstractBeanDefinitionParser {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDefinitionParser.class);

	/** 默认资源匹配符 */
	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/** 资源搜索分析器 */
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	/** 类的元数据读取器，由它来负责读取类上的注释信息 */
	private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		// 要创建的对象信息
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TransferFactory.class);

		String coder = element.getAttribute(SchemaNames.ATTR_CODER);
		if (StringUtils.isBlank(coder)) {
			coder = "com.pangu.framework.socket.handler.param.ProtocolCoder";
		}
		String id = element.getAttribute(SchemaNames.ATTR_ID);
		BeanDefinitionBuilder protocolCoder = BeanDefinitionBuilder.rootBeanDefinition(coder);
		protocolCoder.addConstructorArgReference(id);
		parserContext.getRegistry().registerBeanDefinition("protocolCoder", protocolCoder.getBeanDefinition());

		// 要创建的对象信息
		List<IndexedClass> resources = new ManagedList<>();

		// 检查XML内容
		NodeList child = element.getChildNodes();
		for (int i = 0; i < child.getLength(); i++) {
			Node node = child.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String name = node.getLocalName();

			if (name.equals(SchemaNames.PACKAGE_ELEMENT)) {
				// 自动包扫描处理
				String packageName = ((Element) node).getAttribute(SchemaNames.PACKAGE_ATTRIBUTE_NAME);
				String indexName = ((Element) node).getAttribute(SchemaNames.INDEX_ATTRIBUTE_NAME);
				int index = 0;
				try {
					index = Integer.parseInt(indexName);
				} catch (Exception e) {
					FormattingTuple message = MessageFormatter.format("无法初始化的传输包[{}]：[{}]", packageName, indexName);
					logger.error(message.getMessage());
					throw new RuntimeException(message.getMessage(), e);
				}

				String[] names = getResources(packageName);
				for (String resource : names) {
					Class<?> clz;
					try {
						clz = Class.forName(resource);
					} catch (ClassNotFoundException e) {
						FormattingTuple message = MessageFormatter.format("无法获取的资源类[{}]", resource);
						logger.error(message.getMessage());
						throw new RuntimeException(message.getMessage(), e);
					}
					IndexedClass ic = new IndexedClass(clz, index);
					resources.add(ic);
					if (index > 0) {
						index++;
					}
				}
			}

			if (name.equals(SchemaNames.CLASS_ELEMENT)) {
				// 自动类加载处理
				String className = ((Element) node).getAttribute(SchemaNames.CLASS_ATTRIBUTE_NAME);
				String indexName = ((Element) node).getAttribute(SchemaNames.INDEX_ATTRIBUTE_NAME);
				Class<?> clz;
				int index = 0;
				try {
					index = Integer.parseInt(indexName);
					clz = Class.forName(className);
				} catch (Exception e) {
					FormattingTuple message = MessageFormatter.format("无法初始化的传输类[{}]：[{}]", className, indexName);
					logger.error(message.getMessage());
					throw new RuntimeException(message.getMessage(), e);
				}
				// 不检查标注
				IndexedClass ic = new IndexedClass(clz, index);
				resources.add(ic);
			}
		}
		Collections.sort(resources);
		factory.addPropertyValue("transables", resources);
		return factory.getBeanDefinition();
	}

	/**
	 * 获取指定包下的静态资源对象
	 * @param packageName 包名
	 * @return
	 */
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
			logger.debug("解析协议包扫描耗时 {} 毫秒", (System.currentTimeMillis() - s));
			ArrayList<String> sort = new ArrayList<>(result);
			Collections.sort(sort);
			return sort.toArray(new String[0]);
		} catch (IOException e) {
			String message = "无法读取资源信息";
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
	}

	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
}
