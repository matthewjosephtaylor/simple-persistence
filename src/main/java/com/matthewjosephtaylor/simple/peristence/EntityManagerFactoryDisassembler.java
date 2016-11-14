package com.matthewjosephtaylor.simple.peristence;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;

public class EntityManagerFactoryDisassembler implements AutoCloseable {
	private static final Logger logger = Logger.getLogger(EntityManagerFactoryDisassembler.class);

	private Class<Driver> driverClass;
	private EntityManagerFactory entityManagerFactory;

	public EntityManagerFactoryDisassembler(Class<Driver> driverClass, EntityManagerFactory entityManagerFactory) {
		this.driverClass = driverClass;
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void close() throws Exception {
		entityManagerFactory.close();

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass() == driverClass) {
				try {
					logger.info("Deregistering JDBC driver " + driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					logger.error("Error deregistering JDBC driver " + driver, ex);
				}
			} else {
				logger.trace("Not deregistering JDBC driver " + driver
						+ " as it does not belong to this webapp's ClassLoader");
			}
		}
	}

}
