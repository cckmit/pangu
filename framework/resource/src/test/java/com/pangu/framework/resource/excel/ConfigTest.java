package com.pangu.framework.resource.excel;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Component
public class ConfigTest {

	@Static
	private Storage<Integer, Human> storage;
	
	@Test
	public void test() {
		for (Human h : storage.getAll()) {
			assertThat(h.isSex(), is(false));
		}
	}
	
}
