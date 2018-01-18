package com.biit.bean.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	private final static String JAR_EXTENSION = ".jar";

	@Autowired
	private ApplicationContext applicationContext;

	public BeanLoader() {

	}

	@PostConstruct
	private void loadSettings() {
		String jarFolder = BeanLoaderConfigurationReader.getInstance().getBeansFolder();
		String defaultBeanPacketPrefix = BeanLoaderConfigurationReader.getInstance().getBeanPacketPrefix();
		// Load beans if settings are set.
		if (jarFolder.length() > 0) {
			BeanLoaderLogger.debug(this.getClass().getName(), "Reading beans in '" + jarFolder + "'.");
			loadBeansFromJar(HotBean.class, jarFolder, defaultBeanPacketPrefix);
		}
	}

	@Override
	public <T> Set<Object> getLoadedBeansOfType(Class<T> type) {
		Set<Object> beansFiltered = new HashSet<Object>();
		for (Object bean : getLoadedBeansWithAnnotation(HotBean.class)) {
			if (type.isAssignableFrom(bean.getClass())) {
				beansFiltered.add(bean);
			}
		}
		return beansFiltered;
	}

	@Override
	public <T extends Annotation> Collection<Object> getLoadedBeansWithAnnotation(Class<T> beanAnnotation) {
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(beanAnnotation);
		BeanLoaderLogger.info(this.getClass().getName(), "Beans loaded of type '" + beanAnnotation.getCanonicalName() + "' are '" + beans.values() + "'.");
		return beans.values();
	}

	@Override
	public <T extends Annotation> void loadBeansFromJar(Class<T> beanAnnotation, String folderWithJars, String packetPrefixFilter) {
		for (String pathToJar : getJars(folderWithJars)) {
			try (JarFile jarFile = new JarFile(pathToJar)) {
				Enumeration<JarEntry> entries = jarFile.entries();

				URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL("jar:file:" + pathToJar + "!/") }, applicationContext.getClassLoader());

				while (entries.hasMoreElements()) {
					// Read jar elements.
					JarEntry jarEntry = entries.nextElement();
					// Search only for classes.
					if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
						continue;
					}
					// Get the class name (-6 because of '.class').
					String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
					className = className.replace('/', '.');
					if (className.startsWith(packetPrefixFilter)) {
						BeanLoaderLogger.debug(this.getClass().getName(), "Loading class '" + className + "'.");
						try {
							// Is already on memory?
							if (!isClassLoaded(classLoader, className)) {
								Class<?> classLoaded = classLoader.loadClass(className);
								BeanLoaderLogger.debug(this.getClass().getName(), "Class '" + classLoaded.getCanonicalName() + "' loaded.");

								// Add it as a bean.
								if (!classLoaded.isInterface() && hasBasicConstructor(classLoaded)) {
									// Has @HotBean annotation.
									for (Annotation annotation : classLoaded.getDeclaredAnnotations()) {
										if (annotation.annotationType().equals(beanAnnotation)) {
											BeanLoaderLogger.debug(this.getClass().getName(), "Class '" + classLoaded.getCanonicalName()
													+ "' implements annotation '" + HotBean.class + "'.");
											ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext)
													.getBeanFactory();
											// Create bean if does not exists.
											if (beanFactory.getSingleton(classLoaded.getCanonicalName()) == null) {
												Object bean = classLoaded.getDeclaredConstructor().newInstance();
												beanFactory.registerSingleton(classLoaded.getCanonicalName(), bean);
												BeanLoaderLogger.debug(this.getClass().getName(), "Bean '" + bean + "' created.");
											}
										}
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

	private boolean hasBasicConstructor(Class<?> clazz) {
		for (Constructor<?> constructor : clazz.getConstructors()) {
			if (constructor.getParameterTypes().length == 0) {
				return true;
			}
		}

		return false;
	}

	private Set<String> getJars(String folderPath) {
		Set<String> jarPaths = new HashSet<>();
		File dir = new File(folderPath);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String fileName) {
				BeanLoaderLogger.debug(this.getClass().getName(), "Found jar '" + fileName + "'.");
				return fileName.endsWith(JAR_EXTENSION);
			}
		});

		for (File jarfile : files) {
			jarPaths.add(jarfile.getAbsolutePath());
			System.out.println(jarfile.getAbsolutePath());
		}
		return jarPaths;
	}

}
