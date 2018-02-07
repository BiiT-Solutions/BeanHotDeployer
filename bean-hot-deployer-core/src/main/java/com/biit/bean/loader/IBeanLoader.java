package com.biit.bean.loader;

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
	 * Gets all beans that already exists in the application context and match
	 * the selected filter.
	 * 
	 * @param filter
	 *            filtering which beans are selected.
	 * @return a list of bean classes.
	 */
	<T extends java.lang.annotation.Annotation> Collection<Object> getLoadedBeansWithAnnotation(Class<T> beanAnnotation);

	<T> Set<T> getLoadedBeansOfType(Class<T> type);

	void loadSettings(String jarFolder, String beanPacketPrefix);

	Map<String, Class<?>> getBeansClassLoaded();
}
