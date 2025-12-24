/***************************************************************************
 *                 Copyright © 2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package eu.polanieonline.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;


/**
 * Configures Log4j appenders and log directory for the Android client.
 */
public final class LogConfigurator {

	/** Rolling log pattern. */
	private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36} - %msg%n";

	/** Maximum number of rotated log files to keep. */
	private static final int MAX_ROLLED_FILES = 5;

	/** Rolling threshold in megabytes. */
	private static final String ROLLOVER_SIZE = "5MB";

	/** Whether configuration finished successfully. */
	private static boolean configured = false;

	/** Resolved log directory. */
	private static File logDirectory;


	private LogConfigurator() {
		// static methods only
	}

	/**
	 * Configures Log4j appenders and log directory.
	 *
	 * @param context
	 *   Application context.
	 */
	public static synchronized void configure(final Context context) {
		if (configured) {
			return;
		}

		logDirectory = resolveLogDirectory(context);
		if (logDirectory == null) {
			Log.e("LogConfigurator", "Unable to determine log directory; falling back to cache dir");
			logDirectory = context.getCacheDir();
		}

		if (!logDirectory.exists() && !logDirectory.mkdirs()) {
			Log.e("LogConfigurator", "Failed to create log directory: " + logDirectory.getAbsolutePath());
		}

		try {
			applyConfiguration(logDirectory);
			configured = true;
		} catch (final Exception e) {
			Log.e("LogConfigurator", "Failed to initialize Log4j configuration", e);
		}
	}

	/**
	 * Retrieves configured log directory.
	 *
	 * @return
	 *   Directory for log output.
	 */
	public static File getLogDirectory() {
		return logDirectory;
	}

	/**
	 * Retrieves configured log directory path.
	 *
	 * @return
	 *   Absolute path to log directory, or an empty string if not configured.
	 */
	public static String getLogDirectoryPath() {
		return logDirectory != null ? logDirectory.getAbsolutePath() : "";
	}

	/**
	 * Optionally shows a UI notification while keeping log output intact.
	 *
	 * @param level
	 *   Log level to display.
	 * @param message
	 *   Notification body.
	 * @param notify
	 *   Whether to show the notification.
	 */
	public static void notifyUser(final Level level, final String message, final boolean notify) {
		if (notify) {
			final String title = level == null ? Level.INFO.name() : level.name();
			Notifier.showMessage(message, true, title);
		}
	}

	/**
	 * Shows a UI notification while keeping log output intact.
	 *
	 * @param level
	 *   Log level to display.
	 * @param message
	 *   Notification body.
	 */
	public static void notifyUser(final Level level, final String message) {
		notifyUser(level, message, true);
	}

	private static File resolveLogDirectory(final Context context) {
		final List<File> candidates = new ArrayList<>();

		final File homeDir = new File(System.getProperty("user.home", ""));
		if (homeDir.exists() || homeDir.mkdirs()) {
			candidates.add(new File(homeDir, "PolanieOnline/logs"));
		}

		if (context.getFilesDir() != null) {
			candidates.add(new File(context.getFilesDir(), "logs"));
		}

		final File externalFiles = context.getExternalFilesDir(null);
		if (externalFiles != null) {
			candidates.add(new File(externalFiles, "logs"));
		}

		for (final File candidate : candidates) {
			try {
				if (!candidate.exists() && !candidate.mkdirs()) {
					continue;
				}

				if (candidate.canWrite()) {
					return candidate;
				}
			} catch (final SecurityException e) {
				Log.w("LogConfigurator", "Unable to access log dir candidate: " + candidate.getAbsolutePath(), e);
			}
		}

		return null;
	}

	private static void applyConfiguration(final File targetDir) {
		final LoggerContext context = (LoggerContext) LogManager.getContext(false);
		final Configuration config = context.getConfiguration();

		final PatternLayout layout = PatternLayout.createLayout(
			LOG_PATTERN, config, null, null, true, false, null, null);

		final String fileName = new File(targetDir, "client.log").getAbsolutePath();
		final String filePattern = new File(targetDir, "client-%d{yyyy-MM-dd}-%i.log.gz").getAbsolutePath();

		final SizeBasedTriggeringPolicy policy = SizeBasedTriggeringPolicy.createPolicy(ROLLOVER_SIZE);
		final DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy(
			String.valueOf(MAX_ROLLED_FILES), null, null, null, config);

		final RollingFileAppender rollingFileAppender = RollingFileAppender.createAppender(
			fileName, filePattern, "true", "RollingFile", "true", "8192", "true", policy, strategy,
			layout, null, "false", null, config);
		rollingFileAppender.start();
		config.addAppender(rollingFileAppender);

		Appender consoleAppender = null;
		if (BuildConfig.DEBUG) {
			consoleAppender = ConsoleAppender.createAppender(
				layout, null, "SYSTEM_OUT", "Console", "true", "false");
			consoleAppender.start();
			config.addAppender(consoleAppender);
		}

		final LoggerConfig rootLogger = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		rootLogger.addAppender(rollingFileAppender, BuildConfig.DEBUG ? Level.DEBUG : Level.INFO, null);
		if (consoleAppender != null) {
			rootLogger.addAppender(consoleAppender, Level.DEBUG, null);
		}
		rootLogger.setLevel(BuildConfig.DEBUG ? Level.DEBUG : Level.INFO);

		context.updateLoggers();
	}
}
