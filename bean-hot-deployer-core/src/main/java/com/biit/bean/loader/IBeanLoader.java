package com.biit.bean.loader;

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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IBeanLoader {

	/**
	 * Loads Beans deployed as a JAR in the folder specific in the
	 * configuration.
	 * 
	 * @param beanAnnotation
	 *            annotation used to distinguish the bean.
	 * @param folderWithJars
	 *            path to a folder with jars that contains the beans.
	 * @param packetPrefixFilter
	 *            only scan classes that the packet starts with this string.
	 */
	<T extends HotBean> void loadBeansFromFolder(Class<T> beanAnnotation, String folderWithJars, String packetPrefixFilter);

	/**
	 * Reads from a classLoader if a class has bean loaded or not.
	 * 
	 * @param classLoader
	 *            the classLoader
	 * @param classToCheck
	 *            the class that is checked.
	 * @return true if the class is loaded.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	boolean isClassLoaded(ClassLoader classLoader, String classToCheck) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	/**
	 * Gets all beans that already exist in the application context and match
	 * the selected filter.
	 *
	 * @return a list of bean classes.
	 */
	<T extends java.lang.annotation.Annotation> Collection<Object> getLoadedBeansWithAnnotation(Class<T> beanAnnotation);

	<T> Set<T> getLoadedBeansOfType(Class<T> type);

	void loadSettings(String jarFolder, String beanPacketPrefix);

	Map<String, Class<?>> getBeansClassLoaded();
}
