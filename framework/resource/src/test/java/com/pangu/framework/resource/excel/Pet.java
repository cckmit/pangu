package com.pangu.framework.resource.excel;

import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;

@Resource
public class Pet {

	@Id
	private Integer id;
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
