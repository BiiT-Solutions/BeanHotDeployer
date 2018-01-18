package com.biit.bean.loader.configuration;

import java.nio.file.Path;

import com.biit.bean.loader.logger.BeanLoaderLogger;
import com.biit.utils.configuration.ConfigurationReader;
import com.biit.utils.configuration.PropertiesSourceFile;
import com.biit.utils.configuration.SystemVariablePropertiesSourceFile;
import com.biit.utils.configuration.exceptions.PropertyNotFoundException;
import com.biit.utils.file.watcher.FileWatcher.FileModifiedListener;

public class BeanLoaderConfigurationReader extends ConfigurationReader {
	private static final String CONFIG_FILE = "settings.conf";
	private static final String SYSTEM_VARIABLE_CONFIG = "BEANLOADER_CONFIG";
	private static BeanLoaderConfigurationReader instance;

	// Tags
	private static final String ID_BEANS_FOLDER = "bean.deploy.folder";
	private static final String ID_DEFAULT_BEAN_CLASS = "bean.default.class";
	private static final String ID_DEFAULT_BEAN_PACKET = "bean.default.packet.prefix";

	// Default values
	private static final String BEANS_FOLDER = System.getProperty("java.io.tmpdir");

	private BeanLoaderConfigurationReader() {
		super();

		addProperty(ID_BEANS_FOLDER, BEANS_FOLDER);
		addProperty(ID_DEFAULT_BEAN_CLASS, "");
		addProperty(ID_DEFAULT_BEAN_PACKET, "");

		PropertiesSourceFile sourceFile = new PropertiesSourceFile(CONFIG_FILE);
		sourceFile.addFileModifiedListeners(new FileModifiedListener() {

			@Override
			public void changeDetected(Path pathToFile) {
				BeanLoaderLogger.info(this.getClass().getName(), "BeanLoader settings file '" + pathToFile + "' change detected.");
				readConfigurations();
			}
		});
		addPropertiesSource(sourceFile);

		SystemVariablePropertiesSourceFile systemSourceFile = new SystemVariablePropertiesSourceFile(SYSTEM_VARIABLE_CONFIG, CONFIG_FILE);
		systemSourceFile.addFileModifiedListeners(new FileModifiedListener() {

			@Override
			public void changeDetected(Path pathToFile) {
				BeanLoaderLogger.info(this.getClass().getName(), "System variable settings file '" + pathToFile + "' change detected.");
				readConfigurations();
			}
		});
		addPropertiesSource(systemSourceFile);

		readConfigurations();
	}

	public static BeanLoaderConfigurationReader getInstance() {
		if (instance == null) {
			synchronized (BeanLoaderConfigurationReader.class) {
				if (instance == null) {
					instance = new BeanLoaderConfigurationReader();
				}
			}
		}
		return instance;
	}

	private String getPropertyLogException(String propertyId) {
		try {
			return getProperty(propertyId);
		} catch (PropertyNotFoundException e) {
			BeanLoaderLogger.errorMessage(this.getClass().getName(), e);
			return null;
		}
	}

	@SuppressWarnings("unused")
	private String[] getPropertyCommaSeparatedValuesLogException(String propertyId) {
		try {
			return getCommaSeparatedValues(propertyId);
		} catch (PropertyNotFoundException e) {
			BeanLoaderLogger.errorMessage(this.getClass().getName(), e);
			return null;
		}
	}

	public String getBeansFolder() {
		return getPropertyLogException(ID_BEANS_FOLDER);
	}

	public String getDefaultBeanClassName() {
		return getPropertyLogException(ID_DEFAULT_BEAN_CLASS);
	}

	public String getDefaultBeanPacketPrefix() {
		return getPropertyLogException(ID_DEFAULT_BEAN_PACKET);
	}

}
