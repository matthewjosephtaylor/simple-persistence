package com.matthewjosephtaylor.simple.peristence;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.matthewjosephtaylor.simple.service.ServiceLocator;

public final class EntityManagerFactoryAssembler {
	private static final Logger logger = Logger.getLogger(EntityManagerFactoryAssembler.class);

	private EntityManagerFactoryAssembler() {
	}

	private static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
	private static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	private static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
	private static final String HIBERNATE_DIALECT = "hibernate.dialect";
	private static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
	private static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
	private static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";
	private static final String CONNECTION_DRIVER_CLASS = "connection.driver_class";
	private static final String HIBERNATE_ARCHIVE_AUTODETECTION = "hibernate.archive.autodetection";
	private static final String PERSISTENCE_UNIT_NAME = "javax.persistence.persistence_unit_name";

	public static void assemble() {
		ServiceLocator.require(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {

				logger.info("Start initializeDatabase...");
				Configuration config = ServiceLocator.get(Configuration.class);
				Class<Driver> driverClass;
				try {
					driverClass = (Class<Driver>) Class.forName(config.getString(CONNECTION_DRIVER_CLASS));
				} catch (final ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				Map<String, String> properties = new HashMap<>();
				properties.put(CONNECTION_DRIVER_CLASS, config.getString(CONNECTION_DRIVER_CLASS));
				properties.put(HIBERNATE_CONNECTION_URL, config.getString(HIBERNATE_CONNECTION_URL));
				properties.put(HIBERNATE_CONNECTION_USERNAME, config.getString(HIBERNATE_CONNECTION_USERNAME));
				properties.put(HIBERNATE_CONNECTION_PASSWORD, config.getString(HIBERNATE_CONNECTION_PASSWORD));
				properties.put(HIBERNATE_DIALECT, config.getString(HIBERNATE_DIALECT));
				properties.put(HIBERNATE_SHOW_SQL, config.getString(HIBERNATE_SHOW_SQL));
				properties.put(HIBERNATE_FORMAT_SQL, config.getString(HIBERNATE_FORMAT_SQL));
				properties.put(HIBERNATE_ARCHIVE_AUTODETECTION, config.getString(HIBERNATE_ARCHIVE_AUTODETECTION));

				if (config.containsKey(HIBERNATE_HBM2DDL_AUTO)) {
					properties.put(HIBERNATE_HBM2DDL_AUTO, config.getString(HIBERNATE_HBM2DDL_AUTO));
				}

				EntityManagerFactory emf = Persistence
						.createEntityManagerFactory(config.getString(PERSISTENCE_UNIT_NAME), properties);
				ServiceLocator.register(EntityManagerFactory.class, emf);
				ServiceLocator.register(EntityManagerFactoryDisassembler.class,
						new EntityManagerFactoryDisassembler(driverClass, emf));

				logger.info("End initializeDatabase");

			}

		}, Configuration.class);
	}

}
