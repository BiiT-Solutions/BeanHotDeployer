package com.biit.hotdeploy.bean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.biit.hotdeploy.logger.BeanHotDeployLogger;

@Resource
public class BeanLoader {
	private final static String PACKAGE_TO_SCAN = "com.biit.infographic";

	@Autowired
	private ApplicationContext applicationContext;

	private String getJarPath() {
		return "/tmp/test-patient-infographic-engine.jar";
	}

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
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public <T> void loadTestJar(Class<T> filter, String packetPrefixFilter) throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String pathToJar = getJarPath();
		try (JarFile jarFile = new JarFile(pathToJar)) {
			Enumeration<JarEntry> e = jarFile.entries();

			URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL("jar:file:" + pathToJar + "!/") }, applicationContext.getClassLoader());

			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				// -6 because of .class
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				if (className.startsWith(packetPrefixFilter)) {
					BeanHotDeployLogger.debug(this.getClass().getName(), "Loading class '" + className + "'.");
					if (isClassNotLoaded(classLoader, className)) {
						Class<?> classLoaded = classLoader.loadClass(className);
						BeanHotDeployLogger.info(this.getClass().getName(), "Class '" + classLoaded.getCanonicalName() + "' loaded.");

						// Add it as a bean.
						if (!classLoaded.isInterface() && filter.getClass().isAssignableFrom(classLoaded)) {
							ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
							// Create bean if does not exists.
							if (beanFactory.getSingleton(classLoaded.getCanonicalName()) == null) {
								Object bean = classLoaded.getDeclaredConstructor().newInstance();
								beanFactory.registerSingleton(classLoaded.getCanonicalName(), bean);
								BeanHotDeployLogger.info(this.getClass().getName(), "Bean '" + bean + "' created.");
							}
						}
					} else {
						BeanHotDeployLogger.debug(this.getClass().getName(), "Class '" + className + "' already loaded!");
					}
				}
			}
		}
	}

	/**
	 * Reads from a classLoader if a class has bean loaded or not.
	 * 
	 * @param classLoader
	 *            the classLoader
	 * @param classToCheck
	 *            the class that is checked.
	 * @return true if the class is loaded.
	 */
	private boolean isClassNotLoaded(ClassLoader classLoader, String classToCheck) {
		// A little of reflection.
		Method findLoadedClassMethod;
		try {
			findLoadedClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
			findLoadedClassMethod.setAccessible(true);
			Object result = findLoadedClassMethod.invoke(classLoader, classToCheck);
			if (result == null) {
				return true;
			} else {
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			BeanHotDeployLogger.errorMessage(this.getClass().getName(), e);
		}
		return false;
	}

}
