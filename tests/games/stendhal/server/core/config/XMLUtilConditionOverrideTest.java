package games.stendhal.server.core.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;

public class XMLUtilConditionOverrideTest {
	private static final String PROPERTY = "stendhal.test.runtime.condition";

	@After
	public void clearProperty() {
		System.clearProperty(PROPERTY);
	}

	@Test
	public void overrideDoesNotChangeSystemProperty() {
		System.clearProperty(PROPERTY);
		assertFalse(XMLUtil.checkCondition(PROPERTY));

		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, true)) {
			assertTrue(XMLUtil.checkCondition(PROPERTY));
			assertTrue(XMLUtil.checkCondition("!other.property"));
			assertTrue(System.getProperty(PROPERTY) == null);
		}

		assertFalse(XMLUtil.checkCondition(PROPERTY));
	}

	@Test
	public void nestedOverrideRestoresPreviousValue() {
		try (XMLUtil.ConditionOverride outer = XMLUtil.overrideCondition(PROPERTY, true)) {
			assertTrue(XMLUtil.checkCondition(PROPERTY));
			try (XMLUtil.ConditionOverride inner = XMLUtil.overrideCondition(PROPERTY, false)) {
				assertFalse(XMLUtil.checkCondition(PROPERTY));
				assertTrue(XMLUtil.checkCondition("!" + PROPERTY));
			}
			assertTrue(XMLUtil.checkCondition(PROPERTY));
		}
	}

	@Test
	public void overrideIsThreadLocal() throws Exception {
		System.clearProperty(PROPERTY);
		final CountDownLatch insideOverride = new CountDownLatch(1);
		final CountDownLatch finish = new CountDownLatch(1);
		final AtomicBoolean workerSawOverride = new AtomicBoolean(false);

		Thread worker = new Thread(new Runnable() {
			@Override
			public void run() {
				try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, true)) {
					workerSawOverride.set(XMLUtil.checkCondition(PROPERTY));
					insideOverride.countDown();
					try {
						finish.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		worker.start();

		insideOverride.await();
		assertTrue(workerSawOverride.get());
		assertFalse(XMLUtil.checkCondition(PROPERTY));
		finish.countDown();
		worker.join();
	}
}
