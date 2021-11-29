package com.pangu.framework.protocol.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser(SchemaNames.CONFIG_ELEMENT, new ConfigDefinitionParser());
	}

}
