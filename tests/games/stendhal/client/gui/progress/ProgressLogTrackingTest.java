package games.stendhal.client.gui.progress;

import static org.junit.Assert.*;
import games.stendhal.client.entity.User;
import games.stendhal.client.util.UserInterfaceTestHelper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import marauroa.common.game.RPObject;
import org.junit.Test;

public class ProgressLogTrackingTest {
	private Object field(Object target, String name) throws Exception {
		Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
	}
	@Test public void bookControlsFollowPossessionSelectionAndStaleDetailResponses() throws Exception {
		UserInterfaceTestHelper.initUserInterface();
		Field proxy = User.class.getDeclaredField("userProxy"); proxy.setAccessible(true);
		Object previous = proxy.get(null);
		RPObject state = new RPObject();
		state.put("compass_available", 1);
		new User() { @Override public RPObject getRPObject() { return state; } };
		try {
			SwingUtilities.invokeAndWait(() -> {
				ProgressLog book = new ProgressLog("Dziennik zadań", null);
				try {
					book.setPages(Arrays.asList("Zadania poboczne"), ignored -> { });
					book.setPageIndex("Zadania poboczne", Arrays.asList("Węgiel do Grilla", "Dostawca Pizzy"), ignored -> { });
					Object page = ((List<?>) field(book, "pages")).get(0);
					Field selected = page.getClass().getDeclaredField("contentQueryData"); selected.setAccessible(true);
					selected.set(page, "Węgiel do Grilla");
					book.setPageContent("Zadania poboczne", "Węgiel do Grilla", "Haunchy potrzebuje węgla do grilla.", "",
							Arrays.asList("Zbierz 25 kawałków węgla, a następnie wróć do Haunchy'ego."), null, "coal_for_haunchy");
					JButton button = (JButton) field(page, "trackButton");
					assertTrue(button.isVisible()); assertEquals("Śledź kompasem", button.getText());
					state.put("compass_tracked", "coal_for_haunchy");
					state.put("compass_tracked_title", "Węgiel do Grilla");
					book.refreshTracking(false);
					assertEquals("Przestań śledzić", button.getText());
					String index = ((JEditorPane) field(page, "indexArea")).getText();
					assertTrue(index.contains("⌖") || index.contains("8982"));
					String preview = System.getProperty("quest.tracking.preview");
					if (preview != null) {
						JDialog window = (JDialog) book.getWindow(); window.pack(); window.validate();
						BufferedImage image = new BufferedImage(window.getContentPane().getWidth(),
								window.getContentPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
						java.awt.Graphics2D g = image.createGraphics(); window.getContentPane().printAll(g); g.dispose();
						ImageIO.write(image, "png", new File(preview));
					}
					state.put("compass_available", 0); book.refreshTracking(false); assertFalse(button.isVisible());
					state.put("compass_available", 1); book.refreshTracking(false); assertTrue(button.isVisible());
					book.setPageContent("Zadania poboczne", "Nieaktualna odpowiedź", "", "", Collections.emptyList(), null, "wrong");
					assertEquals("coal_for_haunchy", field(page, "trackingSlot"));
					((javax.swing.event.HyperlinkListener) page).hyperlinkUpdate(new javax.swing.event.HyperlinkEvent(
							page, javax.swing.event.HyperlinkEvent.EventType.ACTIVATED, null, "Dostawca Pizzy"));
					assertFalse("Previous quest cannot be tracked while new details are loading", button.isVisible());
					book.setPageContent("Zadania poboczne", "Węgiel do Grilla", "", "", Collections.emptyList(), null, "coal_for_haunchy");
					assertFalse("Late reply cannot restore the previous tracking control", button.isVisible());
					book.setPageIndex("Zadania poboczne", Collections.emptyList(), ignored -> { });
					assertFalse(button.isVisible());
				} catch (Exception e) { throw new RuntimeException(e); }
				finally { book.getWindow().dispose(); }
			});
		} finally { proxy.set(null, previous); }
	}
}
