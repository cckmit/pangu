package com.pangu.framework.resource.i18n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pangu.framework.resource.I18NStorage;
import com.pangu.framework.resource.StorageManager;
import com.pangu.framework.resource.anno.Static;

/**
 * 静态资源注入基本功能测试用例
 * @author author
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Component
public class BasicTest {
	
	@Autowired
	private StorageManager resourceManager;
	@Static
	private I18NStorage<Integer, Human> storage;
	@Static("1")
	private Human human1;
	@Static("2")
	private Human human2;
	@Static("1")
	private Human humanOther;
	@Static(value = "-1", required = false)
	private Human humanNotFound;
	

	@Test
	public void test_i18n() {
		Human p1 = storage.get("zh_cn", 1, true);
		Human p2 = storage.get("zh_tw", 1, true);
		Assert.assertNotSame(p1,  p2);
		Assert.assertThat(p1.getName() + "_TW", equalTo(p2.getName()));
		
		Human p3 = storage.getUnique("zh_tw", Human.INDEX_NAME, "author_TW");
		assertThat(p3, notNullValue());
		assertThat(p3.getId(), is(1));
		assertThat(p3.getName(), is("author_TW"));
		
		List<Human> list = storage.getIndex("zh_tw", Human.INDEX_AGE, 32);
		assertThat(list, notNullValue());
		assertThat(list.size(), is(2));

		Human p4 = list.get(0);
		assertThat(p4.getId(), is(3));
		assertThat(p4.getName(), is("Kyle_TW"));

		Human p5 = list.get(1);
		assertThat(p5.getId(), is(1));
		assertThat(p5.getName(), is("author_TW"));
	}
	
	/**
	 * 测试管理对象注入
	 */
	@Test
	public void test_get_manager() {
		assertThat(resourceManager, notNullValue());
	}

	/**
	 * 测试管理对象基本功能
	 */
	@Test
	public void test_basic() {
		Human p1 = resourceManager.getResource(1, Human.class);
		assertThat(p1.getId(), is(1));
		assertThat(p1.getName(), is("author"));
		Human same = resourceManager.getResource(1, Human.class);
		assertThat(p1, sameInstance(same));
	}
	
	/**
	 * 测试指定资源存储空间注入
	 */
	@Test
	public void test_inject_storage() {
		assertThat(storage, notNullValue());
		Human p1 = resourceManager.getResource(1, Human.class);
		assertThat(p1.getId(), is(1));
		assertThat(p1.getName(), is("author"));
		Human same = resourceManager.getResource(1, Human.class);
		assertThat(p1, sameInstance(same));
	}
	
	/**
	 * 测试唯一索引获取
	 */
	@Test
	public void test_index_unique() {
		Human p1 = storage.getUnique(Human.INDEX_NAME, "author");
		assertThat(p1, notNullValue());
		assertThat(p1.getId(), is(1));
		assertThat(p1.getName(), is("author"));
		assertThat(p1, sameInstance(human1));
	}
	
	/**
	 * 测试列表索引获取
	 */
	@Test
	public void test_index_list() {
		List<Human> list = storage.getIndex(Human.INDEX_AGE, 32);
		assertThat(list, notNullValue());
		assertThat(list.size(), is(2));
		
		Human p1 = list.get(0);
		assertThat(p1.getId(), is(3));
		assertThat(p1.getName(), is("Kyle"));
		
		Human p2 = list.get(1);
		assertThat(p2.getId(), is(1));
		assertThat(p2.getName(), is("author"));
		assertThat(p2, sameInstance(human1));
	}

	/**
	 * 测试资源实例注入
	 * @throws InterruptedException 
	 */
	@Test
	public void test_inject_instance() throws InterruptedException {
		Thread.sleep(1000);
		
		assertThat(human1, notNullValue());
		assertThat(human2, notNullValue());
		assertThat(humanOther, notNullValue());
		assertThat(humanNotFound, nullValue());
		
		assertThat(human1.getId(), is(1));
		assertThat(human1.getName(), is("author"));

		assertThat(human2.getId(), is(2));
		assertThat(human2.getName(), is("May"));
		
		assertThat(human1, sameInstance(humanOther));
	}
	
}
