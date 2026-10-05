/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.util.UserInterfaceTestHelper;

public class FeatureEnabledItemPanelTest {
	@BeforeClass
	public static void initialize() { UserInterfaceTestHelper.initUserInterface(); }

	@Test
	public void unattachedSlotCanBecomeVisibleWithoutNullParentError() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final FeatureEnabledItemPanel panel = new FeatureEnabledItemPanel("test_pouch", null);
			panel.featureEnabled("test_pouch", "");
			assertTrue(panel.isVisible());
			panel.featureDisabled("test_pouch");
			assertFalse(panel.isVisible());
		});
	}

	@Test
	public void queuedFeaturesApplyLatestStateOnEventThread() throws Exception {
		final TrackingPanel[] panel = new TrackingPanel[1];
		SwingUtilities.invokeAndWait(() -> panel[0] = new TrackingPanel());
		final CountDownLatch blocked = new CountDownLatch(1);
		final CountDownLatch release = new CountDownLatch(1);
		SwingUtilities.invokeLater(() -> {
			blocked.countDown();
			try { release.await(5, TimeUnit.SECONDS); }
			catch (final InterruptedException e) { Thread.currentThread().interrupt(); }
		});
		assertTrue(blocked.await(5, TimeUnit.SECONDS));
		try {
			panel[0].featureEnabled("test_pouch", "");
			panel[0].featureDisabled("test_pouch");
		} finally { release.countDown(); }
		SwingUtilities.invokeAndWait(() -> {
			assertFalse(panel[0].isVisible());
			assertEquals(0, panel[0].visibleUpdates);
		});
	}

	private static final class TrackingPanel extends FeatureEnabledItemPanel {
		private static final long serialVersionUID = 1L;
		private int visibleUpdates;
		TrackingPanel() { super("test_pouch", null); visibleUpdates = 0; }
		@Override
		public void setVisible(final boolean visible) {
			assertTrue(SwingUtilities.isEventDispatchThread());
			if (visible) { visibleUpdates++; }
			super.setVisible(visible);
		}
	}
}
