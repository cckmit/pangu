package com.pangu.framework.resource.other;

public class FormatDefinition {

	
	private final String location;
	
	private final String type;
	
	private final String i18n;
	
	private final String suffix;
	
	private final String config;

	public FormatDefinition(String location, String type, String n18, String suffix, String config) {
		this.location = location;
		this.type = type;
		this.i18n = n18;
		this.suffix = suffix;
		this.config = config;
	}

	// Getter and Setter ...

	public String getLocation() {
		return location;
	}

	public String getType() {
		return type;
	}

	public String getI18N() {
		return i18n;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getConfig() {
		return config;
	}

}
