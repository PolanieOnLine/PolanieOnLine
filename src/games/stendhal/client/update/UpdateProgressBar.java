/* Copyright (C) 2003-2026 PolanieOnLine and Stendhal; GPL version 2 or later. */
package games.stendhal.client.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyledButtonUI;
import games.stendhal.client.gui.styled.StyledCheckBoxUI;
import games.stendhal.client.gui.styled.StyledProgressBarUI;
import games.stendhal.client.gui.styled.styles.WoodStyle;
import games.stendhal.client.sprite.Sprite;

/** Standalone update UI: no dependency on the game jars being downloaded. */
class UpdateProgressBar extends JFrame implements HttpClient.ProgressListener {
    private static final long serialVersionUID = -1607102841664745919L;
    private static final Color TEXT = new Color(255, 241, 220);
    private static final Color MUTED = new Color(225, 193, 154);
    private static final Color GOLD = new Color(255, 208, 116);
    private final long totalBytes;
    private final CountDownLatch launch = new CountDownLatch(1);
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel status = label("Pobieram aktualizacje...", TEXT, 14, true);
    private final JLabel transferred = label("", MUTED, 12, false);
    private final JLabel speed = label("", MUTED, 12, false);
    private final JLabel percentage = label("0%", TEXT, 12, false);
    private final JCheckBox autoLaunch = new JCheckBox(UpdateLanguage.getText("Uruchom grę po aktualizacji"), true);
    private final JButton play = button("Graj");
    private final JButton cancel = button("Przerwij");
    private volatile boolean ready;
    // Transfer counters belong to the download thread; EDT receives snapshots.
    private long verifiedBytes;
    private long currentBytes;
    private long startedAt;
    private long lastRefresh;

    UpdateProgressBar(final int max, final String urlBase, final String fromVersion, final String toVersion) {
        super(UpdateLanguage.getText("Aktualizacja " + ClientGameConfiguration.get("GAME_NAME")));
        totalBytes = Math.max(0, max);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new UpdateProgressBarWindowListener());
        setLocationByPlatform(true);
        final Image icon = loadImage(ClientGameConfiguration.get("GAME_ICON"));
        if (icon != null) {
            setIconImage(icon);
        }
        initializeComponents(fromVersion, toVersion);
        setMinimumSize(new Dimension(680, 440));
        setSize(760, 480);
    }

    private void initializeComponents(final String fromVersion, final String toVersion) {
        final JPanel content = new JPanel(new BorderLayout());
        final JPanel artwork = new ArtworkPanel(loadImage(ClientGameConfiguration.get("GAME_SPLASH_BACKGROUND")), false);
        artwork.setPreferredSize(new Dimension(290, 420));
        content.add(artwork, BorderLayout.WEST);
        final Image wood = loadImage("data/gui/panel_wood.jpg");
        final Style controlsStyle = wood == null ? null : new WoodStyle(new ResourceSprite(wood));
        final JPanel details = new ArtworkPanel(wood, true);
        details.setLayout(new GridBagLayout());
        details.setBorder(BorderFactory.createEmptyBorder(20, 18, 16, 18));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 6, 0);
        addRow(details, constraints, label("Przygotowujemy grę", GOLD, 24, true));
        final String versions = fromVersion == null ? UpdateLanguage.getText("Pierwsza instalacja")
                : UpdateLanguage.getText("Wersja") + " " + fromVersion;
        addRow(details, constraints, label(versions + (toVersion == null ? "" : " → " + toVersion), MUTED, 12, false));
        final JTextArea message = new JTextArea(UpdateLanguage.getText("Pobieramy i sprawdzamy pliki potrzebne do uruchomienia gry."), 2, 0);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setEditable(false);
        message.setFocusable(false);
        message.setOpaque(false);
        message.setForeground(TEXT);
        message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        message.setMinimumSize(new Dimension(0, 38));
        addRow(details, constraints, message);
        constraints.weighty = 1;
        addRow(details, constraints, new JLabel());
        constraints.weighty = 0;
        final JPanel statusRow = new JPanel(new BorderLayout(8, 0));
        statusRow.setOpaque(false);
        statusRow.add(status, BorderLayout.CENTER);
        statusRow.add(percentage, BorderLayout.EAST);
        addRow(details, constraints, statusRow);
        if (controlsStyle != null) {
            play.setUI(new StyledButtonUI(controlsStyle));
            cancel.setUI(new StyledButtonUI(controlsStyle));
            autoLaunch.setUI(new StyledCheckBoxUI(controlsStyle));
            progress.setUI(new StyledProgressBarUI(controlsStyle));
        }
        progress.setPreferredSize(new Dimension(250, 20));
        progress.setName("update-progress");
        addRow(details, constraints, progress);
        addRow(details, constraints, transferred);
        addRow(details, constraints, speed);
        autoLaunch.setName("auto-launch");
        autoLaunch.setOpaque(false);
        autoLaunch.addActionListener(event -> {
            if (ready && autoLaunch.isSelected()) {
                launch.countDown();
            }
        });
        addRow(details, constraints, autoLaunch);
        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        cancel.setName("cancel-update");
        cancel.addActionListener(event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        play.setName("play");
        play.setEnabled(false);
        play.addActionListener(event -> launch.countDown());
        actions.add(cancel);
        actions.add(play);
        constraints.insets = new Insets(4, 0, 0, 0);
        addRow(details, constraints, actions);
        content.add(details, BorderLayout.CENTER);
        setContentPane(content);
        renderProgress(0, 0);
    }

    private static void addRow(final JPanel panel, final GridBagConstraints constraints, final Component component) {
        constraints.gridy++;
        panel.add(component, constraints);
    }

    private static JLabel label(final String text, final Color color, final int size, final boolean bold) {
        final JLabel label = new JLabel(UpdateLanguage.getText(text));
        label.setForeground(color);
        label.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    private static JButton button(final String text) {
        return new JButton(UpdateLanguage.getText(text));
    }

    private static Image loadImage(final String resource) {
        if (resource == null) {
            return null;
        }
        final URL url = UpdateProgressBar.class.getClassLoader().getResource(resource);
        try {
            return url == null ? null : ImageIO.read(url);
        } catch (final IOException error) {
            return null;
        }
    }

    /** Each attempt starts from verified files, including retries from a fallback mirror. */
    void beginFile() {
        currentBytes = 0;
        if (startedAt == 0) {
            startedAt = System.nanoTime();
        }
        refresh(true, false);
    }

    @Override
    public void onDownloading(final int downloadedBytes) {
        currentBytes = Math.max(0, downloadedBytes);
        refresh(false, false);
    }

    @Override
    public void onDownloadCompleted(final int downloadedBytes) {
        currentBytes = Math.max(0, downloadedBytes);
        refresh(true, true);
    }

    /** Count a transfer once, only after its size and signature were accepted. */
    void fileVerified() {
        verifiedBytes += currentBytes;
        currentBytes = 0;
        refresh(true, false);
    }

    private void refresh(final boolean force, final boolean verifying) {
        final long now = System.nanoTime();
        if (!force && now - lastRefresh < 100_000_000L) {
            return;
        }
        lastRefresh = now;
        final long bytes = Math.min(totalBytes, verifiedBytes + currentBytes);
        final double elapsed = startedAt == 0 ? 0 : (now - startedAt) / 1_000_000_000.0;
        final double bytesPerSecond = elapsed >= 1 ? bytes / elapsed : 0;
        SwingUtilities.invokeLater(() -> {
            status.setText(UpdateLanguage.getText(verifying ? "Sprawdzanie pobranych plików" : "Pobieram aktualizacje..."));
            renderProgress(bytes, bytesPerSecond);
        });
    }

    private void renderProgress(final long bytes, final double bytesPerSecond) {
        progress.setValue(totalBytes == 0 ? 0 : (int) Math.min(100, bytes * 100 / totalBytes));
        percentage.setText(progress.getValue() + "%");
        transferred.setText(formatMegabytes(bytes) + " / " + formatMegabytes(totalBytes) + " MB");
        if (bytesPerSecond <= 0 || bytes >= totalBytes) {
            speed.setText(UpdateLanguage.getText("Proszę czekać…"));
        } else {
            final long seconds = Math.max(1, (long) Math.ceil((totalBytes - bytes) / bytesPerSecond));
            speed.setText(formatMegabytes(bytesPerSecond) + " MB/s · "
                    + UpdateLanguage.getText("pozostało około") + " " + seconds + " s");
        }
    }

    private static String formatMegabytes(final double bytes) {
        final NumberFormat number = NumberFormat.getNumberInstance(UpdateLanguage.getLocale());
        number.setMinimumFractionDigits(1);
        number.setMaximumFractionDigits(1);
        return number.format(bytes / (1024 * 1024));
    }

    /** Called on the bootstrap thread after signatures and classpath changes succeeded. */
    void awaitLaunch() {
        SwingUtilities.invokeLater(() -> {
            ready = true;
            progress.setValue(100);
            percentage.setText("100%");
            status.setText(UpdateLanguage.getText("Gra jest aktualna"));
            speed.setText(UpdateLanguage.getText("Aktualizacja zakończona"));
            play.setEnabled(true);
            cancel.setVisible(false);
            if (autoLaunch.isSelected()) {
                launch.countDown();
            }
        });
        try {
            launch.await();
        } catch (final InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Update launch interrupted", error);
        } finally {
            SwingUtilities.invokeLater(this::dispose);
        }
    }

    boolean isReady() {
        return ready;
    }

    private static final class ResourceSprite implements Sprite {
        private final Image image;
        ResourceSprite(final Image image) { this.image = image; }
        @Override public int getWidth() { return image.getWidth(null); }
        @Override public int getHeight() { return image.getHeight(null); }
        @Override public Object getReference() { return image; }
        @Override public boolean isConstant() { return true; }
        @Override public void draw(final Graphics graphics, final int x, final int y) {
            graphics.drawImage(image, x, y, null);
        }
        @Override public void draw(final Graphics graphics, final int destx, final int desty,
                final int x, final int y, final int width, final int height) {
            graphics.drawImage(image, destx, desty, destx + width, desty + height,
                    x, y, x + width, y + height, null);
        }
        @Override public Sprite createRegion(final int x, final int y, final int width, final int height, final Object ref) {
            final BufferedImage region = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics graphics = region.getGraphics();
            draw(graphics, 0, 0, x, y, width, height);
            graphics.dispose();
            return new ResourceSprite(region);
        }
    }

    private static final class ArtworkPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Image image;
        private final boolean tiled;

        ArtworkPanel(final Image image, final boolean tiled) {
            this.image = image;
            this.tiled = tiled;
            setBackground(new Color(65, 41, 25));
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            if (image == null) {
                return;
            }
            final Graphics2D drawing = (Graphics2D) graphics.create();
            try {
                if (tiled) {
                    for (int x = 0; x < getWidth(); x += image.getWidth(null)) {
                        for (int y = 0; y < getHeight(); y += image.getHeight(null)) {
                            drawing.drawImage(image, x, y, null);
                        }
                    }
                } else {
                    final double scale = Math.max(getWidth() / (double) image.getWidth(null), getHeight() / (double) image.getHeight(null));
                    final int width = (int) Math.ceil(image.getWidth(null) * scale);
                    final int height = (int) Math.ceil(image.getHeight(null) * scale);
                    drawing.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    drawing.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
                    drawing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
                    final String firstLine = UpdateLanguage.getText("Twoja przygoda");
                    final String secondLine = UpdateLanguage.getText("trwa dalej.");
                    drawing.setColor(new Color(0, 0, 0, 180));
                    drawing.drawString(firstLine, 19, getHeight() - 47);
                    drawing.drawString(secondLine, 19, getHeight() - 22);
                    drawing.setColor(TEXT);
                    drawing.drawString(firstLine, 18, getHeight() - 48);
                    drawing.drawString(secondLine, 18, getHeight() - 23);
                }
            } finally {
                drawing.dispose();
            }
        }
    }
}
