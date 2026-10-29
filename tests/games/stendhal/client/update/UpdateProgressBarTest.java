package games.stendhal.client.update;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.image.BufferedImage;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import games.stendhal.client.gui.styled.StyledButtonUI;
import games.stendhal.client.gui.styled.StyledCheckBoxUI;
import games.stendhal.client.gui.styled.styles.WoodStyle;
import games.stendhal.client.sprite.ImageSprite;

/** Exercises the standalone updater without downloading or launching the game. */
public class UpdateProgressBarTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private String originalHome;
    private UpdateProgressBar window;

    @Before
    public void createWindow() throws Exception {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", temporary.getRoot().getAbsolutePath());
        SwingUtilities.invokeAndWait(() -> window = new UpdateProgressBar(100, null, "1.43.4", "1.44"));
    }

    @After
    public void closeWindow() throws Exception {
        SwingUtilities.invokeAndWait(() -> window.dispose());
        System.setProperty("user.home", originalHome);
    }

    @Test
    public void retryDoesNotCountRejectedTransferOrEnablePlay() throws Exception {
        assertTrue(control("play", JButton.class).getUI() instanceof StyledButtonUI);
        assertTrue(control("cancel-update", JButton.class).getUI() instanceof StyledButtonUI);
        assertTrue(control("auto-launch", JCheckBox.class).getUI() instanceof StyledCheckBoxUI);
        window.beginFile();
        window.onDownloadCompleted(40);
        window.fileVerified();
        window.beginFile();
        window.onDownloadCompleted(60);
        flush();
        assertProgress(100);
        assertFalse(control("play", JButton.class).isEnabled());
        assertFalse(window.isReady());
        // Reject that transfer's signature and retry it from another mirror.
        window.beginFile();
        window.onDownloadCompleted(20);
        flush();
        assertProgress(60);
        window.fileVerified();
        window.beginFile();
        window.onDownloadCompleted(40);
        flush();
        assertProgress(100);
        assertFalse(control("play", JButton.class).isEnabled());
    }

    @Test
    public void manualLaunchWaitsForPlayAfterVerification() throws Exception {
        SwingUtilities.invokeAndWait(() -> control("auto-launch", JCheckBox.class).setSelected(false));
        final CompletableFuture<Void> waiting = CompletableFuture.runAsync(window::awaitLaunch);
        try {
            waitUntilReady();
            assertFalse(waiting.isDone());
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(control("play", JButton.class).isEnabled());
                assertFalse(control("cancel-update", JButton.class).isVisible());
                control("play", JButton.class).doClick();
            });
            waiting.get(5, TimeUnit.SECONDS);
            flush();
            assertFalse(window.isDisplayable());
        } finally {
            SwingUtilities.invokeAndWait(() -> control("play", JButton.class).doClick());
        }
    }

    @Test
    public void defaultAutomaticLaunchDoesNotWaitForAButton() throws Exception {
        CompletableFuture.runAsync(window::awaitLaunch).get(5, TimeUnit.SECONDS);
        flush();
        assertTrue(window.isReady());
        assertFalse(window.isDisplayable());
    }

    @Test
    public void bootstrapButtonsDoNotFreezeTheStyleChosenLaterByTheGame() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            final Object originalStyle = UIManager.get("StendhalStyle");
            try {
                for (int i = 0; i < 2; i++) {
                    final WoodStyle style = new WoodStyle(new ImageSprite(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB)));
                    UIManager.put("StendhalStyle", style);
                    final JButton button = new JButton("test");
                    button.setUI((StyledButtonUI) StyledButtonUI.createUI(button));
                    assertSame(style.getBorder(), button.getBorder());
                }
            } finally {
                UIManager.put("StendhalStyle", originalStyle);
            }
        });
    }

    private void waitUntilReady() throws Exception {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!window.isReady() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        flush();
        assertTrue(window.isReady());
    }

    private void assertProgress(final int expected) throws Exception {
        SwingUtilities.invokeAndWait(() -> assertEquals(expected, control("update-progress", JProgressBar.class).getValue()));
    }

    private void flush() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }

    private <T extends Component> T control(final String name, final Class<T> type) {
        final AtomicReference<T> found = new AtomicReference<>();
        find(window, name, type, found);
        assertNotNull(name, found.get());
        return found.get();
    }

    private <T extends Component> void find(final Container root, final String name, final Class<T> type,
            final AtomicReference<T> found) {
        for (final Component component : root.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) {
                found.set(type.cast(component));
            } else if (component instanceof Container) {
                find((Container) component, name, type, found);
            }
        }
    }
}
