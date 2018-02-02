package com.biit.bean.loader.comparator;

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