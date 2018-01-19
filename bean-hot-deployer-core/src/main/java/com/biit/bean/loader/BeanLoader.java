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
import java.nio.file.Path;
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
import com.biit.logger.BiitCommonLogger;
import com.biit.utils.file.watcher.FileWatcher;
import com.biit.utils.file.watcher.FileWatcher.FileAddedListener;
import com.biit.utils.file.watcher.FileWatcher.FileRemovedListener;

@Component
public class BeanLoader implements IBeanLoader {
	private final static String JAR_EXTENSION = ".jar";
	private FileWatcher fileWatcher;

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
			susbscribeToFolder(jarFolder);
		}
	}

	private void susbscribeToFolder(String directoryToWatch) {
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		try {
			fileWatcher = new FileWatcher(directoryToWatch);
			fileWatcher.addFileAddedListener(new FileAddedListener() {

				@Override
				public void fileCreated(Path pathToFile) {
					System.out.println("####################################");
					System.out.println(" NEW BEAN!!");
					System.out.println("####################################");
				}
			});
			fileWatcher.addFileRemovedListener(new FileRemovedListener() {

				@Override
				public void fileDeleted(Path pathToFile) {
					System.out.println("####################################");
					System.out.println(" Removed BEAN!!");
					System.out.println("####################################");
				}
			});
		} catch (IOException e) {
			BiitCommonLogger.errorMessageNotification(this.getClass(), e);
		} catch (NullPointerException npe) {
			BiitCommonLogger.warning(this.getClass(), "Directory to watch not found!");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Set<T> getLoadedBeansOfType(Class<T> type) {
		Set<T> beansFiltered = new HashSet<T>();
		for (Object bean : applicationContext.getBeansWithAnnotation(HotBean.class).values()) {
			if (type.isAssignableFrom(bean.getClass())) {
				beansFiltered.add((T) bean);
			}
		}
		BeanLoaderLogger.info(this.getClass().getName(), "Beans loaded of type '" + type.getCanonicalName() + "' are '" + beansFiltered + "'.");
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
				return fileName.endsWith(JAR_EXTENSION);
			}
		});

		for (File jarfile : files) {
			BeanLoaderLogger.debug(this.getClass().getName(), "JAR file found '" + jarfile.getAbsolutePath() + "'.");
			jarPaths.add(jarfile.getAbsolutePath());
		}
		return jarPaths;
	}
}
