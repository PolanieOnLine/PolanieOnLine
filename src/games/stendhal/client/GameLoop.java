/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import games.stendhal.client.gui.settings.SettingsProperties;
import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.MathHelper;

/**
 * Game loop thread.
 */
public class GameLoop {
	private static final Logger logger = Logger.getLogger(GameLoop.class);

	private static class Holder {
		static GameLoop instance = new GameLoop();
	}

	/** The actual game loop thread. */
	private final Thread loopThread;
	/** Main game loop content. Run at every cycle.*/
	private PersistentTask persistentTask;
	/** Run once tasks, requested by other threads. */
	private final Queue<Runnable> temporaryTasks = new ConcurrentLinkedQueue<Runnable>();
	/** Tasks to be run when the game loop exits. */
	private final List<Runnable> cleanupTasks = new ArrayList<Runnable>();
	/**
	 * <code>true</code> when the game loop should keep looping,
	 * <code>false</code>, when it should continue to the cleanup tasks.
	 */
	private volatile boolean running;

	private volatile long frameLengthNanos;
	private volatile double logicStepMillis;
	private static final double MAX_FRAME_DELTA_MS = 250.0;
	private static final int MAX_CATCH_UP_STEPS = 5;

	private volatile int currentFps;

	private final SettingChangeListener fpsLimitListener;

	/**
	 * Create a new GameLoop.
	 */
	private GameLoop() {
		fpsLimitListener = new SettingChangeListener() {
			@Override
			public void changed(String newValue) {
				int limit = MathHelper.parseIntDefault(newValue, stendhal.getFpsLimit());
				updateFrameLength(limit);
			}
		};
		int configuredLimit = WtWindowManager.getInstance().getPropertyInt(
				SettingsProperties.FPS_LIMIT_PROPERTY, stendhal.getFpsLimit());
		updateFrameLength(configuredLimit);
		WtWindowManager.getInstance().registerSettingChangeListener(
				SettingsProperties.FPS_LIMIT_PROPERTY, fpsLimitListener);
		loopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loop();
				// gameLoop runs until the client quit
				for (Runnable cleanup : cleanupTasks) {
					cleanup.run();
				}
			}
		}, "Game loop");
	}

	private void updateFrameLength(int limit) {
		int sanitized = Math.max(1, limit);
		stendhal.setFpsLimit(sanitized);
		frameLengthNanos = Math.max(1L, Math.round(1_000_000_000.0 / sanitized));
		logicStepMillis = Math.max(1.0, 1000.0 / sanitized);
		currentFps = sanitized;
	}


	/**
	 * Get the GameLoop instance.
	 *
	 * @return game loop instance
	 */
	public static GameLoop get() {
		return Holder.instance;
	}

	/**
	 * Check if the code is running in the game loop.
	 *
	 * @return <code>true</code> if the called from the game loop thread,
	 * 	<code>false</code> otherwise.
	 */
	public static boolean isGameLoop() {
		return Thread.currentThread() == get().loopThread;
	}

	/**
	 * Start the game loop.
	 */
	public void start() {
		running = true;
		loopThread.start();
	}

	/**
	 * Call at client quit. Tells the game loop to continue to the cleanup
	 * tasks.
	 */
	public void stop() {
		running = false;
	}

	/**
	 * Set the task that should be run at every loop iteration. This <b>must
	 * not</b> be called after the GameLoop has been started.
	 *
	 * @param task
	 */
	public void runAllways(PersistentTask task) {
		persistentTask = task;
	}

	/**
	 * Add a task that should be run when the client quits. This <b>must
	 * not</b> be called after the GameLoop has been started.
	 *
	 * @param task
	 */
	public void runAtQuit(Runnable task) {
		cleanupTasks.add(task);
	}

	/**
	 * Add a task that should be run once in the game loop thread.
	 *
	 * @param task
	 */
	public void runOnce(Runnable task) {
		temporaryTasks.add(task);
	}

	/**
	 * The actual game loop.
	 */
	private void loop() {
		int fps = 0;
		double accumulator = 0.0;
		long lastFrameTime = System.nanoTime();
		long lastFpsTime = lastFrameTime;

		while (running) {
			final long frameStart = System.nanoTime();
			try {
				long now = frameStart;
				double frameDelta = (now - lastFrameTime) / 1_000_000.0;
				if (frameDelta < 0.0) {
					frameDelta = 0.0;
				}
				frameDelta = Math.min(frameDelta, MAX_FRAME_DELTA_MS);
				lastFrameTime = now;
				accumulator = Math.min(accumulator + frameDelta, MAX_FRAME_DELTA_MS);

				double stepMillis = logicStepMillis;
				int updateCount = 0;
				while ((accumulator >= stepMillis) && (updateCount < MAX_CATCH_UP_STEPS)) {
					persistentTask.run((int) Math.max(1L, Math.round(stepMillis)));
					accumulator -= stepMillis;
					updateCount++;
				}

				if ((updateCount == 0) && (accumulator >= 1.0)) {
					int delta = (int) Math.max(1L, Math.round(Math.min(accumulator, stepMillis)));
					persistentTask.run(delta);
					accumulator -= delta;
				}
				accumulator = Math.max(0.0, accumulator);

				Runnable tempTask = temporaryTasks.poll();
				while (tempTask != null) {
					tempTask.run();
					tempTask = temporaryTasks.poll();
				}

				fps++;
				if ((now - lastFpsTime) >= 1_000_000_000L) {
					currentFps = fps;
					if (logger.isDebugEnabled()) {
						reportClientInfo(fps);
					}
					fps = 0;
					lastFpsTime = now;
				}
			} catch (RuntimeException e) {
				logger.error(e, e);
			} finally {
				long frameDuration = frameLengthNanos;
				long elapsed = System.nanoTime() - frameStart;
				long wait = frameDuration - elapsed;
				if (wait > 0) {
					sleepNanos(wait);
				}
			}
		}
	}

	private void sleepNanos(long nanos) {
		long millis = nanos / 1_000_000L;
		int nanosPart = (int) (nanos % 1_000_000L);
		try {
			Thread.sleep(millis, nanosPart);
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}


	/**
	 * Write debugging data about the client memory usage and running speed.
	 *
	 * @param refreshTime
	 * @param lastFpsTime
	 * @param fps
	 */
	private void reportClientInfo(int fps) {
		logger.debug("FPS: " + fps);
		final long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
		final long totalMemory = Runtime.getRuntime().totalMemory() / 1024;

		logger.debug("Total/Used memory: " + totalMemory + "/"
				+ (totalMemory - freeMemory));
	}


	public int getCurrentFps() {
		return currentFps;
	}

	/**
	 * Interface for the main game loop task.
	 */
	public interface PersistentTask {
		/**
		 * Run the task.
		 *
		 * @param delta time since the last run
		 */
		void run(int delta);
	}
}
