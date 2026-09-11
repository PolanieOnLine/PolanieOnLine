/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CameraSmootherTest {
	@Test
	public void followsMovingTargetWithoutMicroStalls() {
		final CameraSmoother camera = new CameraSmoother();
		camera.reset(0.0, 0.0);

		int previous = camera.getPixelX();
		double target = 0.0;
		for (int frame = 0; frame < 120; frame++) {
			target += 1.75;
			camera.setTarget(target, 0.0);
			camera.update(1000.0 / 60.0);

			final int current = camera.getPixelX();
			if (frame >= 30) {
				final int movement = current - previous;
				assertTrue("camera stalled while following a moving target", movement >= 1);
				assertTrue("camera jumped too far in one frame", movement <= 3);
			}
			previous = current;
		}
	}

	@Test
	public void smoothingIsFrameRateIndependent() {
		final CameraSmoother sixtyFps = new CameraSmoother();
		final CameraSmoother thirtyFps = new CameraSmoother();
		sixtyFps.reset(0.0, 0.0);
		thirtyFps.reset(0.0, 0.0);
		sixtyFps.setTarget(100.0, 80.0);
		thirtyFps.setTarget(100.0, 80.0);

		for (int i = 0; i < 60; i++) {
			sixtyFps.update(1000.0 / 60.0);
		}
		for (int i = 0; i < 30; i++) {
			thirtyFps.update(1000.0 / 30.0);
		}

		assertEquals(sixtyFps.getX(), thirtyFps.getX(), 0.001);
		assertEquals(sixtyFps.getY(), thirtyFps.getY(), 0.001);
	}

	@Test
	public void retainsSubPixelProgressAtHighFrameRates() {
		final CameraSmoother camera = new CameraSmoother();
		camera.reset(0.0, 0.0);

		double previousPosition = camera.getX();
		int previousPixel = camera.getPixelX();
		double target = 0.0;
		boolean repeatedPixel = false;
		for (int frame = 0; frame < 60; frame++) {
			target += 0.35;
			camera.setTarget(target, 0.0);
			camera.update(1000.0 / 144.0);

			final double currentPosition = camera.getX();
			final int currentPixel = camera.getPixelX();
			assertTrue("fractional camera position did not advance", currentPosition > previousPosition);
			if (currentPixel == previousPixel) {
				repeatedPixel = true;
			}
			previousPosition = currentPosition;
			previousPixel = currentPixel;
		}

		assertTrue("test did not exercise sub-pixel movement", repeatedPixel);
	}

	@Test
	public void snapsTinyRemainderInsteadOfCreepingByPixels() {
		final CameraSmoother camera = new CameraSmoother();
		camera.reset(10.0, 20.0);
		camera.setTarget(10.1, 19.9);
		camera.update(1000.0 / 60.0);

		assertEquals(10.1, camera.getX(), 0.0);
		assertEquals(19.9, camera.getY(), 0.0);
		assertTrue(camera.isSettled());
	}
}
