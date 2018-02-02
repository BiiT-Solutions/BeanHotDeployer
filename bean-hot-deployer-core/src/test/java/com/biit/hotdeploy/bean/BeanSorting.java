package com.biit.hotdeploy.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.biit.bean.loader.HotBean;
import com.biit.bean.loader.comparator.HotBeanPriorityComparator;

@Test(groups = { "beanSorting" })
public class BeanSorting {

	@HotBean(priority = 3)
	class ABean {

	}

	@HotBean(priority = 4)
	class BBean {

	}

	@HotBean(priority = 1)
	class CBean {

	}

	@HotBean(priority = 2)
	class DBean {

	}

	@Test
	private void sortByPriority() {
		List<Class<?>> beansToAdd = new ArrayList<>();
		beansToAdd.add(ABean.class);
		beansToAdd.add(BBean.class);
		beansToAdd.add(CBean.class);
		beansToAdd.add(DBean.class);

		Collections.sort(beansToAdd, new HotBeanPriorityComparator());

		Assert.assertEquals(BBean.class, beansToAdd.get(0));
		Assert.assertEquals(ABean.class, beansToAdd.get(1));
		Assert.assertEquals(DBean.class, beansToAdd.get(2));
		Assert.assertEquals(CBean.class, beansToAdd.get(3));
	}

	@Test
	private void sortByPriorityRandomListOrder() {
		List<Class<?>> beansToAdd = new ArrayList<>();
		beansToAdd.add(ABean.class);
		beansToAdd.add(BBean.class);
		beansToAdd.add(CBean.class);
		beansToAdd.add(DBean.class);

		Collections.shuffle(beansToAdd);
		Collections.sort(beansToAdd, new HotBeanPriorityComparator());
		Assert.assertEquals(BBean.class, beansToAdd.get(0));
		Assert.assertEquals(ABean.class, beansToAdd.get(1));
		Assert.assertEquals(DBean.class, beansToAdd.get(2));
		Assert.assertEquals(CBean.class, beansToAdd.get(3));
	}
}
