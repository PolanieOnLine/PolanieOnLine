package games.stendhal.client.fx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;

/**
 * Utility for ensuring that the JavaFX platform is initialised exactly once.
 */
final class FxPlatform {

    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final CountDownLatch STARTUP_LATCH = new CountDownLatch(1);

    private FxPlatform() {
    }

    static void ensureStarted() {
        if (STARTED.get()) {
            waitForStartup();
            return;
        }
        synchronized (STARTED) {
            if (STARTED.get()) {
                waitForStartup();
                return;
            }
            try {
                Platform.startup(() -> {
                    STARTED.set(true);
                    STARTUP_LATCH.countDown();
                });
            } catch (IllegalStateException alreadyStarted) {
                // JavaFX runtime has already been initialised by another caller.
                STARTED.set(true);
                STARTUP_LATCH.countDown();
            }
        }
        waitForStartup();
    }

    private static void waitForStartup() {
        boolean interrupted = false;
        while (STARTUP_LATCH.getCount() > 0) {
            try {
                STARTUP_LATCH.await();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
