package com.biit.bean.loader.configuration;

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
	private static final String ID_BEAN_PACKET = "bean.packet.prefix";

	// Default values
	private static final String DEFAULT_BEANS_FOLDER = System.getProperty("java.io.tmpdir");
	private static final String DEFAULT_BEANS_PACKET = "com.biit";

	private BeanLoaderConfigurationReader() {
		super();

		addProperty(ID_BEANS_FOLDER, DEFAULT_BEANS_FOLDER);
		addProperty(ID_BEAN_PACKET, DEFAULT_BEANS_PACKET);

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

	public String getBeanPacketPrefix() {
		return getPropertyLogException(ID_BEAN_PACKET);
	}

}
