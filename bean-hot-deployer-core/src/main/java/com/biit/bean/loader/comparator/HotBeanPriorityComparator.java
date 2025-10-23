package com.biit.bean.loader.comparator;

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

import java.util.Comparator;

import com.biit.bean.loader.HotBean;

public class HotBeanPriorityComparator implements Comparator<Class<?>> {

	@Override
	public int compare(Class<?> bean1, Class<?> bean2) {
		HotBean annotation1 = bean1.getAnnotation(HotBean.class);
		HotBean annotation2 = bean2.getAnnotation(HotBean.class);

		if (annotation1 == null && annotation2 == null) {
			return 0;
		} else if (annotation2 == null) {
			return -1;
		} else if (annotation1 == null) {
			return 1;
		} else if (annotation1.priority() > annotation2.priority()) {
			return -1;
		} else if (annotation1.priority() < annotation2.priority()) {
			return 1;
		} else {
			return 0;
		}
	}

}
