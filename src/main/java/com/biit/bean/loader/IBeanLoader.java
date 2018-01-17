package com.biit.bean.loader;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public interface IBeanLoader {

	/**
	 * Loads Beans deployed as a JAR in the folder specific in the
	 * configuration.
	 * 
	 * @param filter
	 *            Class or interface that must implement the bean to be
	 *            registered in Spring.
	 * @param packetPrefixFilter
	 *            Starting prefix for the packet. For example
	 *            '"com.biit.infographic"'.
	 */
	<T> void loadBeansInJar(Class<T> filter, String packetPrefixFilter);

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
	<T> Collection<T> getLoadedBeansOfType(Class<T> filter);

	Collection<?> getLoadedBeansOfType(String className);
}
