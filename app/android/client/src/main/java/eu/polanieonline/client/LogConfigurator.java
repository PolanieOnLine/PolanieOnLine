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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;


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
		final ConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<>();
		builder.setStatusLevel(Level.WARN);
		builder.setConfigurationName("PolanieOnlineLogConfig");

		final LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
			.addAttribute("pattern", LOG_PATTERN);

		final String fileName = new File(targetDir, "client.log").getAbsolutePath();
		final String filePattern = new File(targetDir, "client-%d{yyyy-MM-dd}-%i.log.gz").getAbsolutePath();

		final ComponentBuilder<?> policies = builder.newComponent("Policies")
			.addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", ROLLOVER_SIZE));

		final ComponentBuilder<?> strategy = builder.newComponent("DefaultRolloverStrategy")
			.addAttribute("max", MAX_ROLLED_FILES);

		final AppenderComponentBuilder rollingFile = builder.newAppender("RollingFile", "RollingFile")
			.addAttribute("fileName", fileName)
			.addAttribute("filePattern", filePattern)
			.add(layout)
			.addComponent(policies)
			.addComponent(strategy);
		builder.add(rollingFile);

		if (BuildConfig.DEBUG) {
			final AppenderComponentBuilder console = builder.newAppender("Console", "Console")
				.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
				.add(layout);
			builder.add(console);
		}

		final RootLoggerComponentBuilder rootLogger = builder.newRootLogger(
			BuildConfig.DEBUG ? Level.DEBUG : Level.INFO);
		rootLogger.add(builder.newAppenderRef("RollingFile"));
		if (BuildConfig.DEBUG) {
			rootLogger.add(builder.newAppenderRef("Console"));
		}
		builder.add(rootLogger);

		Configurator.initialize(builder.build());
	}
}
