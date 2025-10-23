package com.biit.hotdeploy.bean;

/*-
 * #%L
 * Bean Hot Deployer (Core)
 * %%
 * Copyright (C) 2022 - 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
