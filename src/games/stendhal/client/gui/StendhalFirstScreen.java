/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.launcher.LauncherArtworkPanel;
import games.stendhal.client.gui.launcher.LauncherButton;
import games.stendhal.client.gui.launcher.LauncherButton.Style;
import games.stendhal.client.gui.launcher.LauncherFramePanel;
import games.stendhal.client.gui.launcher.LauncherTheme;
import games.stendhal.client.gui.launcher.LauncherTransparentPanel;
import games.stendhal.client.gui.login.CreateAccountDialog;
import games.stendhal.client.gui.login.LoginDialog;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.stendhal;
import games.stendhal.client.update.ClientGameConfiguration;

/**
 * Desktop launcher shown before the game login dialog.
 */
@SuppressWarnings("serial")
public class StendhalFirstScreen extends JFrame {
	private static final long serialVersionUID = -7825572598938892220L;

	private static final int WINDOW_WIDTH = 1200;
	private static final int WINDOW_HEIGHT = 660;
	private static final int MINIMUM_WIDTH = 1040;
	private static final int MINIMUM_HEIGHT = 600;
	private static final int SIDEBAR_WIDTH = 196;
	private static final int INFO_WIDTH = 238;
	private static final int OUTER_PADDING = 16;
	private static final int COLUMN_GAP = 12;

	private final StendhalClient client;
	private final List<JButton> actionButtons = new ArrayList<JButton>();
	private LauncherButton playButton;

	static {
		// This is the initial window, when loaded at all.
		Initializer.init();
	}

	/**
	 * Creates the first screen.
	 *
	 * @param client
	 *            StendhalClient
	 */
	public StendhalFirstScreen(final StendhalClient client) {
		super(detectScreen());
		setLocationByPlatform(true);
		WindowUtils.trackLocation(this, "main", true);
		this.client = client;
		client.setSplashScreen(this);

		initializeComponent();
		setVisible(true);
		playButton.requestFocusInWindow();
	}

	/**
	 * Detect the preferred screen by where the mouse is the moment the method
	 * is called. This is for multi-monitor support.
	 *
	 * @return GraphicsEnvironment of the current screen
	 */
	private static GraphicsConfiguration detectScreen() {
		final PointerInfo pointer = MouseInfo.getPointerInfo();
		if (pointer != null) {
			return pointer.getDevice().getDefaultConfiguration();
		}
		return null;
	}

	/** Setup the launcher contents without changing the existing login flow. */
	private void initializeComponent() {
		final String gameName = ClientGameConfiguration.get("GAME_NAME");
		final LauncherRootPanel root = new LauncherRootPanel();
		root.setBorder(BorderFactory.createEmptyBorder(OUTER_PADDING, OUTER_PADDING,
				OUTER_PADDING, OUTER_PADDING));
		root.setLayout(new BorderLayout(COLUMN_GAP, 0));
		setContentPane(root);

		final Action loginAction = createLoginAction(gameName);
		final Action createAccountAction = createAccountAction(gameName);
		final Action helpAction = createHelpAction();
		final Action creditsAction = createCreditsAction();

		root.add(createSidebar(gameName, createAccountAction, helpAction, creditsAction), BorderLayout.WEST);
		root.add(createCenterColumn(loginAction), BorderLayout.CENTER);
		root.add(createInfoColumn(), BorderLayout.EAST);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(gameName + " " + stendhal.VERSION + " - darmowa gra MMORPG - polanieonline.eu");
		setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

		final URL iconUrl = DataLoader.getResource(ClientGameConfiguration.get("GAME_ICON"));
		if (iconUrl != null) {
			setIconImage(new ImageIcon(iconUrl).getImage());
		}

		getRootPane().setDefaultButton(playButton);
		pack();
	}

	private Action createLoginAction(final String gameName) {
		final Action action = new AbstractAction("GRAJ") {
			@Override
			public void actionPerformed(final ActionEvent event) {
				new LoginDialog(StendhalFirstScreen.this, client).setVisible(true);
			}
		};
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		action.putValue(Action.SHORT_DESCRIPTION,
				"Zaloguj się i rozpocznij grę na serwerze " + gameName + ".");
		return action;
	}

	private Action createAccountAction(final String gameName) {
		final Action action = new AbstractAction("Utwórz konto") {
			@Override
			public void actionPerformed(final ActionEvent event) {
				new CreateAccountDialog(StendhalFirstScreen.this, client);
			}
		};
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		action.putValue(Action.SHORT_DESCRIPTION,
				"Utwórz nowe konto na serwerze " + gameName + ".");
		return action;
	}

	private Action createHelpAction() {
		final Action action = new AbstractAction("Pomoc i FAQ") {
			@Override
			public void actionPerformed(final ActionEvent event) {
				BareBonesBrowserLaunch.openURL(
						ClientGameConfiguration.get("DEFAULT_SERVER_WEB") + "/wprowadzenie.html");
			}
		};
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		return action;
	}

	private Action createCreditsAction() {
		final Action action = new AbstractAction("Wyróżnieni") {
			@Override
			public void actionPerformed(final ActionEvent event) {
				new CreditsDialog(StendhalFirstScreen.this);
			}
		};
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		return action;
	}

	private JComponent createSidebar(final String gameName, final Action createAccountAction,
			final Action helpAction, final Action creditsAction) {
		final LauncherFramePanel panel = new LauncherFramePanel();
		panel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));

		final JLabel icon = createGameIcon(48);
		icon.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(icon);
		panel.add(Box.createVerticalStrut(8));

		final JLabel title = new JLabel("POLANIE");
		title.setForeground(LauncherTheme.TEXT);
		title.setFont(LauncherTheme.displayFont(23));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(title);

		final JLabel subtitle = new JLabel("ONLINE");
		subtitle.setForeground(LauncherTheme.GOLD_BRIGHT);
		subtitle.setFont(LauncherTheme.bodyFont(Font.BOLD, 11));
		subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(subtitle);
		panel.add(Box.createVerticalStrut(20));
		panel.add(createDivider());
		panel.add(Box.createVerticalStrut(16));

		panel.add(createNavigationButton(createAccountAction));
		panel.add(Box.createVerticalStrut(9));
		panel.add(createNavigationButton(helpAction));
		panel.add(Box.createVerticalStrut(9));
		panel.add(createNavigationButton(creditsAction));
		panel.add(Box.createVerticalGlue());
		panel.add(createDivider());
		panel.add(Box.createVerticalStrut(12));

		final JLabel footer = new JLabel("Wersja " + stendhal.VERSION);
		footer.setForeground(LauncherTheme.TEXT_MUTED);
		footer.setFont(LauncherTheme.bodyFont(Font.PLAIN, 11));
		footer.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(footer);

		return panel;
	}

	private JComponent createCenterColumn(final Action loginAction) {
		final JPanel center = new LauncherTransparentPanel(new BorderLayout(0, 10));

		final LauncherArtworkPanel hero = new LauncherArtworkPanel();
		hero.setLayout(new BorderLayout());
		hero.setBorder(BorderFactory.createEmptyBorder(24, 30, 26, 30));

		final JPanel heroBottom = new LauncherTransparentPanel();
		heroBottom.setLayout(new BoxLayout(heroBottom, BoxLayout.Y_AXIS));

		final JLabel eyebrow = new JLabel("DARMOWA GRA MMORPG");
		eyebrow.setForeground(LauncherTheme.GOLD_BRIGHT);
		eyebrow.setFont(LauncherTheme.bodyFont(Font.BOLD, 11));
		eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);
		heroBottom.add(eyebrow);
		heroBottom.add(Box.createVerticalStrut(7));

		final JLabel description = new JLabel("Otwarty świat przygód, niebezpieczeństw i wspólnej historii.");
		description.setForeground(new Color(229, 226, 218));
		description.setFont(LauncherTheme.bodyFont(Font.PLAIN, 15));
		description.setAlignmentX(Component.CENTER_ALIGNMENT);
		heroBottom.add(description);
		heroBottom.add(Box.createVerticalStrut(20));

		playButton = new LauncherButton(loginAction, Style.PRIMARY);
		playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		actionButtons.add(playButton);
		heroBottom.add(playButton);

		hero.add(heroBottom, BorderLayout.SOUTH);
		center.add(hero, BorderLayout.CENTER);
		center.add(createReadyPanel(), BorderLayout.SOUTH);
		return center;
	}

	private JComponent createInfoColumn() {
		final LauncherFramePanel panel = new LauncherFramePanel();
		panel.setPreferredSize(new Dimension(INFO_WIDTH, 0));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(18, 16, 16, 16));

		panel.add(createSectionTitle("KLIENT"));
		panel.add(Box.createVerticalStrut(12));
		panel.add(createInfoRow("Wersja", stendhal.VERSION));
		panel.add(Box.createVerticalStrut(8));
		panel.add(createInfoRow("Gra", ClientGameConfiguration.get("GAME_NAME")));
		panel.add(Box.createVerticalStrut(18));
		panel.add(createDivider());
		panel.add(Box.createVerticalStrut(18));

		panel.add(createSectionTitle("SERWER"));
		panel.add(Box.createVerticalStrut(12));
		panel.add(createInfoRow("Adres", ClientGameConfiguration.get("DEFAULT_SERVER")));
		panel.add(Box.createVerticalStrut(8));
		panel.add(createInfoRow("Port", ClientGameConfiguration.get("DEFAULT_PORT")));
		panel.add(Box.createVerticalStrut(18));
		panel.add(createDivider());
		panel.add(Box.createVerticalStrut(18));

		panel.add(createSectionTitle("AKTUALIZACJE"));
		panel.add(Box.createVerticalStrut(12));
		final boolean autoUpdate = Boolean.parseBoolean(
				ClientGameConfiguration.get("UPDATE_ENABLE_AUTO_UPDATE"));
		panel.add(createStatusLabel(autoUpdate
				? "Włączone"
				: "Wyłączone",
				autoUpdate ? LauncherTheme.SUCCESS : LauncherTheme.TEXT_MUTED));
		panel.add(Box.createVerticalGlue());
		panel.add(createDivider());
		panel.add(Box.createVerticalStrut(12));
		final JLabel website = new JLabel("polanieonline.eu");
		website.setForeground(LauncherTheme.TEXT_MUTED);
		website.setFont(LauncherTheme.bodyFont(Font.PLAIN, 12));
		website.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(website);

		return panel;
	}

	private JComponent createReadyPanel() {
		final LauncherFramePanel panel = new LauncherFramePanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 12));
		panel.setPreferredSize(new Dimension(0, 50));

		final JLabel dot = new JLabel("●");
		dot.setForeground(LauncherTheme.SUCCESS);
		dot.setFont(LauncherTheme.bodyFont(Font.BOLD, 13));
		panel.add(dot);

		final JLabel status = new JLabel("Gotowy do logowania");
		status.setForeground(LauncherTheme.TEXT);
		status.setFont(LauncherTheme.bodyFont(Font.BOLD, 13));
		panel.add(status);

		final JLabel hint = new JLabel("Wybierz GRAJ, aby się zalogować.");
		hint.setForeground(LauncherTheme.TEXT_MUTED);
		hint.setFont(LauncherTheme.bodyFont(Font.PLAIN, 12));
		panel.add(hint);
		return panel;
	}

	private LauncherButton createNavigationButton(final Action action) {
		final LauncherButton button = new LauncherButton(action, Style.NAVIGATION);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		actionButtons.add(button);
		return button;
	}

	private JLabel createGameIcon(final int size) {
		final URL url = DataLoader.getResource(ClientGameConfiguration.get("GAME_ICON"));
		if (url == null) {
			return new JLabel("◆", SwingConstants.CENTER);
		}
		final Image source = new ImageIcon(url).getImage();
		return new JLabel(new ImageIcon(source.getScaledInstance(size, size, Image.SCALE_SMOOTH)));
	}

	private JComponent createSectionTitle(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(LauncherTheme.GOLD_BRIGHT);
		label.setFont(LauncherTheme.displayFont(18));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JComponent createInfoRow(final String name, final String value) {
		final JPanel row = new LauncherTransparentPanel(new BorderLayout(8, 0));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel nameLabel = new JLabel(name);
		nameLabel.setForeground(LauncherTheme.TEXT_MUTED);
		nameLabel.setFont(LauncherTheme.bodyFont(Font.PLAIN, 12));
		row.add(nameLabel, BorderLayout.WEST);

		final JLabel valueLabel = new JLabel(value == null ? "" : value);
		valueLabel.setForeground(LauncherTheme.TEXT);
		valueLabel.setFont(LauncherTheme.bodyFont(Font.BOLD, 12));
		row.add(valueLabel, BorderLayout.EAST);
		return row;
	}

	private JComponent createStatusLabel(final String text, final Color color) {
		final JLabel label = new JLabel("●  " + text);
		label.setForeground(color);
		label.setFont(LauncherTheme.bodyFont(Font.PLAIN, 12));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JComponent createDivider() {
		final JComponent divider = new JComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(final Graphics graphics) {
				graphics.setColor(LauncherTheme.DIVIDER);
				graphics.drawLine(0, 0, getWidth(), 0);
			}
		};
		divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		divider.setPreferredSize(new Dimension(1, 1));
		divider.setAlignmentX(Component.LEFT_ALIGNMENT);
		return divider;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		for (final JButton button : actionButtons) {
			button.setEnabled(enabled);
		}
	}

	/** Main launcher background with a restrained vignette-like gradient. */
	private static final class LauncherRootPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		LauncherRootPanel() {
			setOpaque(true);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			final Graphics2D g2 = (Graphics2D) graphics.create();
			LauncherTheme.configureGraphics(g2);
			g2.setPaint(new GradientPaint(0, 0, LauncherTheme.WINDOW_TOP,
					0, getHeight(), LauncherTheme.WINDOW_BOTTOM));
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setColor(new Color(163, 116, 54, 16));
			g2.fillOval(getWidth() / 4, -getHeight() / 2, getWidth(), getHeight());
			g2.dispose();
		}
	}

}
