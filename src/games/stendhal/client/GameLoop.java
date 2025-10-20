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
import java.util.concurrent.locks.LockSupport;

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

	/** The actual game logic loop thread. */
	private final Thread logicThread;
	/** Dedicated rendering loop thread. */
	private final Thread renderThread;
	/** Main game loop content. Run at every cycle.*/
	private PersistentTask persistentTask;
	/** Render task executed at the rendering rate. */
	private Runnable renderTask;
	/** Run once tasks, requested by other threads. */
	private final Queue<Runnable> temporaryTasks = new ConcurrentLinkedQueue<Runnable>();
	/** Tasks to be run when the game loop exits. */
	private final List<Runnable> cleanupTasks = new ArrayList<Runnable>();
	/**
	 * <code>true</code> when the game loop should keep looping,
	 * <code>false</code>, when it should continue to the cleanup tasks.
	 */
	private volatile boolean running;
	private volatile boolean renderRunning;

	private volatile long frameLengthNanos;
	private volatile long logicStepNanos;
	private static final long MAX_FRAME_DELTA_NANOS = 250_000_000L;
	private static final int MAX_CATCH_UP_STEPS = 5;
	private static final long SPIN_THRESHOLD_NANOS = 200_000L;
	private static final long PARK_MARGIN_NANOS = 100_000L;

	private volatile int currentFps;

	private final SettingChangeListener fpsLimitListener;
	private long logicResidualNanos;
	private long logicSleepAdjustmentNanos;
	private long renderSleepAdjustmentNanos;

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
		logicThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loop();
				// gameLoop runs until the client quit
				for (Runnable cleanup : cleanupTasks) {
					cleanup.run();
				}
			}
		}, "Game loop");
		renderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				renderLoop();
			}
		}, "Render loop");
	}

	private void updateFrameLength(int limit) {
		int sanitized = Math.max(1, limit);
		stendhal.setFpsLimit(sanitized);
		frameLengthNanos = Math.max(1L, Math.round(1_000_000_000.0 / sanitized));
		logicStepNanos = frameLengthNanos;
		logicSleepAdjustmentNanos = 0L;
		renderSleepAdjustmentNanos = 0L;
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
		return Thread.currentThread() == get().logicThread;
	}

	/**
	 * Start the game loop.
	 */
	public void start() {
		running = true;
		renderRunning = true;
		logicThread.start();
		renderThread.start();
	}

	/**
	 * Call at client quit. Tells the game loop to continue to the cleanup
	 * tasks.
	 */
	public void stop() {
		running = false;
		renderRunning = false;
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
	 * Set the render task that should be run at the rendering rate. This <b>must
	 * not</b> be called after the GameLoop has been started.
	 *
	 * @param task render task
	 */
	public void runRenderer(Runnable task) {
		renderTask = task;
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
		long accumulator = 0L;
		long lastFrameTime = System.nanoTime();

		while (running) {
			final long frameStart = System.nanoTime();
			try {
				long now = frameStart;
				long frameDelta = now - lastFrameTime;
				if (frameDelta < 0L) {
					frameDelta = 0L;
				}
				frameDelta = Math.min(frameDelta, MAX_FRAME_DELTA_NANOS);
				lastFrameTime = now;
				accumulator = Math.min(accumulator + frameDelta, MAX_FRAME_DELTA_NANOS);

				long stepNanos = logicStepNanos;
				int updateCount = 0;
				while ((accumulator >= stepNanos) && (updateCount < MAX_CATCH_UP_STEPS)) {
					persistentTask.run(nanosToDelta(stepNanos));
					accumulator -= stepNanos;
					updateCount++;
				}

				if ((updateCount == 0) && (accumulator >= 1_000_000L)) {
					long slice = Math.min(accumulator, stepNanos);
					persistentTask.run(nanosToDelta(slice));
					accumulator -= slice;
				}
				accumulator = Math.max(0L, accumulator);

				Runnable tempTask = temporaryTasks.poll();
				while (tempTask != null) {
					tempTask.run();
					tempTask = temporaryTasks.poll();
				}
			} catch (RuntimeException e) {
				logger.error(e, e);
			} finally {
				long frameDuration = frameLengthNanos;
				long elapsed = System.nanoTime() - frameStart;
				long wait = frameDuration - elapsed + logicSleepAdjustmentNanos;
				if (wait > 0L) {
					logicSleepAdjustmentNanos = sleepNanos(wait);
				} else {
					logicSleepAdjustmentNanos = 0L;
				}
			}
		}
	}

	private void renderLoop() {
		int fps = 0;
		long lastFpsTime = System.nanoTime();
		while (renderRunning) {
			final long frameStart = System.nanoTime();
			try {
				Runnable task = renderTask;
				if (task != null) {
					task.run();
				}
				fps++;
				if ((frameStart - lastFpsTime) >= 1_000_000_000L) {
					currentFps = fps;
					if (logger.isDebugEnabled()) {
						reportClientInfo(fps);
					}
					fps = 0;
					lastFpsTime = frameStart;
				}
			} catch (RuntimeException e) {
				logger.error(e, e);
			} finally {
				long elapsed = System.nanoTime() - frameStart;
				long wait = frameLengthNanos - elapsed + renderSleepAdjustmentNanos;
				if (wait > 0L) {
					renderSleepAdjustmentNanos = sleepNanos(wait);
				} else {
					renderSleepAdjustmentNanos = 0L;
				}
			}
		}
		currentFps = 0;
	}

	private int nanosToDelta(long nanos) {
		long total = nanos + logicResidualNanos;
		long rounded = Math.max(1L, (total + 500_000L) / 1_000_000L);
		logicResidualNanos = total - (rounded * 1_000_000L);
		if (rounded > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) rounded;
	}

	private long sleepNanos(long nanos) {
		long start = System.nanoTime();
		long target = start + nanos;
		long remaining = nanos;
		if (remaining > SPIN_THRESHOLD_NANOS + PARK_MARGIN_NANOS) {
			long parkNanos = remaining - SPIN_THRESHOLD_NANOS;
			if (parkNanos > 0L) {
				LockSupport.parkNanos(parkNanos);
			}
		}
		while ((remaining = target - System.nanoTime()) > 0L) {
			if (remaining > 1_000L) {
				Thread.onSpinWait();
			}
		}
		long actual = System.nanoTime() - start;
		return nanos - actual;
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
