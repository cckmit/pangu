package com.pangu.framework.resource.excel;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pangu.framework.resource.anno.Static;

/**
 * {@link Static}注释测试
 * @author author
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class StaticTest {

	@Autowired
	private StaticTestTarget target;
	
	@Test
	public void test() {
		Assert.assertThat(target, CoreMatchers.notNullValue());
	}
}
