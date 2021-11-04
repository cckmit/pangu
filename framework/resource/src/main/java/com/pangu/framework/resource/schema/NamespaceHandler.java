package com.pangu.framework.resource.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 模块的 XML 命名空间注册器
 * @author frank
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser(SchemaNames.CONFIG_ELEMENT, new ConfigDefinitionParser());
	}

}
