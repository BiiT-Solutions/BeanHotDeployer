package com.biit.bean.loader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.biit.bean.loader.configuration.BeanLoaderConfigurationReader;
import com.biit.bean.loader.logger.BeanLoaderLogger;

@Component
public class BeanLoader implements IBeanLoader {

	@Autowired
	private ApplicationContext applicationContext;

	public BeanLoader() {

	}

	@PostConstruct
	private void loadSettings() {
		String defaultBeanClass = BeanLoaderConfigurationReader.getInstance().getDefaultBeanClassName();
		String defaultBeanPacketPrefix = BeanLoaderConfigurationReader.getInstance().getDefaultBeanPacketPrefix();
		// Load beans if settings are set.
		if (defaultBeanClass.length() > 3 && defaultBeanPacketPrefix.length() > 3) {
			BeanLoaderLogger.debug(this.getClass().getName(), "Reading configuration defined beans.");
			try {
				Class<?> beanFilter = Class.forName(defaultBeanClass);
				loadBeansInJar(beanFilter, defaultBeanPacketPrefix);
			} catch (ClassNotFoundException e) {
				BeanLoaderLogger.warning(this.getClass().getName(), "Class not found '" + defaultBeanClass + "'.");
			}
		}
		BeanLoaderLogger.info(this.getClass().getName(), "Beans loaded of type '" + defaultBeanClass + "' are '" + getLoadedBeansOfType(defaultBeanClass)
				+ "'.");
	}

	private String getJarPath() {
		return "/tmp/usmoorganization-patient-infographic-engine.jar";
	}

	@Override
	public Collection<?> getLoadedBeansOfType(String className) {
		Class<?> beanFilter;
		try {
			beanFilter = Class.forName(className);
			return getLoadedBeansOfType(beanFilter);
		} catch (ClassNotFoundException e) {
			BeanLoaderLogger.errorMessage(this.getClass().getName(), e);
		}
		return null;
	}

	@Override
	public <T> Collection<T> getLoadedBeansOfType(Class<T> filter) {
		@SuppressWarnings("unchecked")
		Map<String, T> beans = (Map<String, T>) applicationContext.getBeansOfType(filter.getClass());
		return beans.values();
	}

	@Override
	public <T> void loadBeansInJar(Class<T> filter, String packetPrefixFilter) {
		String pathToJar = getJarPath();
		try (JarFile jarFile = new JarFile(pathToJar)) {
			Enumeration<JarEntry> entries = jarFile.entries();

			URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL("jar:file:" + pathToJar + "!/") }, applicationContext.getClassLoader());

			while (entries.hasMoreElements()) {
				JarEntry je = entries.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				// -6 because of .class
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				if (className.startsWith(packetPrefixFilter)) {
					BeanLoaderLogger.debug(this.getClass().getName(), "Loading class '" + className + "'.");
					try {
						if (!isClassLoaded(classLoader, className)) {
							Class<?> classLoaded = classLoader.loadClass(className);
							BeanLoaderLogger.debug(this.getClass().getName(), "Class '" + classLoaded.getCanonicalName() + "' loaded.");

							// Add it as a bean.
							if (!classLoaded.isInterface() && filter.getClass().isAssignableFrom(classLoaded)) {
								ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
								// Create bean if does not exists.
								if (beanFactory.getSingleton(classLoaded.getCanonicalName()) == null) {
									Object bean = classLoaded.getDeclaredConstructor().newInstance();
									beanFactory.registerSingleton(classLoaded.getCanonicalName(), bean);
									BeanLoaderLogger.info(this.getClass().getName(), "Bean '" + bean + "' created.");
								}
							}
						} else {
							BeanLoaderLogger.debug(this.getClass().getName(), "Class '" + className + "' already loaded!");
						}
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| ClassNotFoundException | InstantiationException e) {
						BeanLoaderLogger.errorMessage(this.getClass().getName(), e);
					}
				}
			}
		} catch (IOException ioe) {
			BeanLoaderLogger.errorMessage(this.getClass().getName(), ioe);
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
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Override
	public boolean isClassLoaded(ClassLoader classLoader, String classToCheck) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// A little of reflection.
		Method findLoadedClassMethod;
		findLoadedClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
		findLoadedClassMethod.setAccessible(true);
		Object result = findLoadedClassMethod.invoke(classLoader, classToCheck);
		return (result != null);
	}

}
