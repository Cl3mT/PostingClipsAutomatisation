/**
This file is part of the CSC4509 teaching unit.

Copyright (C) 2012-2018 Télécom SudParis

This is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This software platform is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the CSC4509 teaching unit. If not, see <http://www.gnu.org/licenses/>.

Initial developer(s): Denis Conan
Contributor(s):
 */
package common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;


/**
 * This class contains the configuration of some logging facilities.
 * 
 * To recapitulate, logging levels are: TRACE, DEBUG, INFO, WARN, ERROR, FATAL.
 * 
 * @author Denis Conan
 * 
 */
public final class Log {
	/**
	 * states whether logging is enabled or not.
	 */
	public static final boolean ON = true;
	/**
	 * name of logger for the general part (config, etc.).
	 */
	public static final String LOGGER_NAME_GEN = "general";
	/**
	 * logger object for the general part.
	 */
	public static final Logger GEN = LogManager.getLogger(LOGGER_NAME_GEN);
	/**
	 * name of logger for the communication part.
	 */
	public static final String LOGGER_NAME_COMM = "communication";
	/**
	 * logger object for the communication part.
	 */
	public static final Logger COMM = LogManager.getLogger(LOGGER_NAME_COMM);
	/**
	 * name of logger for the testing part (used in JUnit classes).
	 */
	public static final String LOGGER_NAME_TEST = "test";
	/**
	 * logger object for the testing part.
	 */
	public static final Logger TEST = LogManager.getLogger(LOGGER_NAME_TEST);
	/**
	 * name of logger for the controlling part.
	 */
	public static final String LOGGER_NAME_CTRL = "Controleur";
	/**
	 * logger object for the controlling part.
	 */
	public static final Logger CTRL = LogManager.getLogger(LOGGER_NAME_CTRL);
	
	/*
	 * static configuration, which can be changed by command line options.
	 */
	static {
		setLevel(GEN, Level.WARN);
		setLevel(COMM, Level.WARN);
		setLevel(TEST, Level.WARN);
		setLevel(CTRL, Level.WARN);
	}

	/**
	 * configures a logger to a level.
	 * 
	 * @param logger the logger.
	 * @param level  the level.
	 */
	public static void setLevel(final Logger logger, final Level level) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final var config = ctx.getConfiguration();
		var loggerConfig = config.getLoggerConfig(logger.getName());
		var specificConfig = loggerConfig;
		// We need a specific configuration for this logger,
		// otherwise we would change the level of all other loggers
		// having the original configuration as parent as well
		if (!loggerConfig.getName().equals(logger.getName())) {
			specificConfig = new LoggerConfig(logger.getName(), level, true);
			specificConfig.setParent(loggerConfig);
			config.addLogger(logger.getName(), specificConfig);
		}
		specificConfig.setLevel(level);
		ctx.updateLoggers();
	}

	/**
	 * private constructor to avoid instantiation.
	 */
	private Log() {
	}

	
}
