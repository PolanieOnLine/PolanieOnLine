/***************************************************************************
 *                (C) Copyright 2003-2022 - Faiumoni e.V.                  *
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

import static games.stendhal.client.gui.settings.SettingsProperties.DISPLAY_SIZE_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.EVENT_HUD_MODE_COMPACT;
import static games.stendhal.client.gui.settings.SettingsProperties.EVENT_HUD_MODE_FULL;
import static games.stendhal.client.gui.settings.SettingsProperties.EVENT_HUD_MODE_HIDDEN;
import static games.stendhal.client.gui.settings.SettingsProperties.EVENT_HUD_MODE_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.EVENT_HUD_OPACITY_PROPERTY;
import static games.stendhal.common.constants.Actions.COND_STOP;
import static games.stendhal.common.constants.Actions.TYPE;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.TabbedPaneUI;

import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.GameLoop;
import games.stendhal.client.GameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.StendhalClient.ZoneChangeListener;
import games.stendhal.client.UserContext;
import games.stendhal.client.WeatherSoundManager;
import games.stendhal.client.World;
import games.stendhal.client.Zone;
import games.stendhal.client.stendhal;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.buddies.BuddyPanelController;
import games.stendhal.client.gui.chattext.CharacterMap;
import games.stendhal.client.gui.chattext.ChatCompletionHelper;
import games.stendhal.client.gui.chattext.ChatTextController;
import games.stendhal.client.gui.group.GroupPanelController;
import games.stendhal.client.gui.layout.FreePlacementLayout;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.map.EventActivityLeaderboardOverlay;
import games.stendhal.client.gui.map.EventProgressBarOverlay;
import games.stendhal.client.gui.map.MapPanelController;
import games.stendhal.client.gui.settings.SettingsProperties;
import games.stendhal.client.gui.spells.Spells;
import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.gui.status.ActiveMapEventStatus;
import games.stendhal.client.gui.status.MapEventStatusStore;
import games.stendhal.client.gui.styled.StyledTabbedPaneUI;
import games.stendhal.client.gui.wt.core.SettingChangeListener;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.listener.FeatureChangeListener;
import games.stendhal.client.listener.PositionChangeListener;
import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

class SwingClientGUI implements J2DClientGUI {
	/** Scrolling speed when using the mouse wheel. */
	private static final int SCROLLING_SPEED = 8;
	private static final int EVENT_OVERLAY_TOP_MARGIN = 18;
	private static final int EVENT_OVERLAY_SAFE_GAP = 8;
	private static final int EVENT_ACTIVITY_OVERLAY_LEFT_MARGIN = 12;
	private static final int EVENT_ACTIVITY_OVERLAY_TOP_MARGIN = 14;
	private static final String KOSCIELISKO_ESCORT_EVENT_ID = "koscielisko_giant_escort";
	private static final int EVENT_REFRESH_INTERVAL_MILLIS = 100;
	private static final int EVENT_OVERLAY_DEBOUNCE_MILLIS = 300;
	private static final int EVENT_OVERLAY_FADE_DURATION_MILLIS = 220;
	private static final int EVENT_END_STATE_DURATION_MILLIS = 1200;
	private static final int EVENT_HUD_OPACITY_MIN = 40;
	private static final int EVENT_HUD_OPACITY_MAX = 90;
	private static final int EVENT_HUD_OPACITY_DEFAULT = 70;
	/** Property name used to determine if scaling is wanted. */
	private static final String SCALE_PREFERENCE_PROPERTY = "ui.scale_screen";
	private static final Logger logger = Logger.getLogger(SwingClientGUI.class);

	private final JLayeredPane pane;
	private final GameScreen screen;
	private final EventProgressBarOverlay eventProgressOverlay;
	private final EventActivityLeaderboardOverlay eventActivityOverlay;
	private final Timer eventProgressRefreshTimer;
	private final Timer eventOverlayDebounceTimer;
	private final Timer eventOverlayFadeTimer;
	private final Timer eventOverlayEndStateTimer;
	private ActiveMapEventStatus lastShownEventStatus;
	private long fadeStartMillis;
	private long overlayRefreshSuppressedUntilMillis;
	private String eventHudMode = EVENT_HUD_MODE_FULL;
	private float eventHudOpacity = EVENT_HUD_OPACITY_DEFAULT / 100.0f;
	private String eventHudModePropertyKey;
	private String eventHudOpacityPropertyKey;
	private final ScreenController screenController;
	private final ContainerPanel containerPanel;
	private final QuitDialog quitDialog;
	private final UserContext userContext;
	private final ChatTextController chatText = ChatTextController.get();
	private MapPanelController minimap;
	private JSplitPane verticalSplit;
	private final JFrame frame;
	private final JComponent chatLogArea;
	private final JComponent leftColumn;
	private JSplitPane horizontalSplit;
	private final Dimension frameDefaultSize;

	/** the Character panel. */
	private Character character;
	/** the inventory. */
	private Bag inventory;
	/** the Key ring panel. */
	private KeyRing keyring;
	private MagicBag magicbag;
	private RunicAltar runicAltar;
	// private Portfolio portfolio;
	private Spells spells;
	private boolean offline;
	private int paintCounter;
	private User user;
	private GameKeyHandler gameKeyHandler;
	private OutfitDialog outfitDialog;
	private FocusAdapter chatFocusRedirector;
	private boolean chatFocusRedirectInstalled;
	private volatile String currentZoneName;

	public SwingClientGUI(StendhalClient client, UserContext context, NotificationChannelManager channelManager,
			JFrame splash) {
		this.userContext = context;
		setupInternalWindowProperties();
		/*
		 * Add a layered pane for the game area, so that we can have windows on top of
		 * it
		 */
		pane = new JLayeredPane();
		pane.setLayout(new FreePlacementLayout());

		// Create the main game screen
		screen = GameScreen.get(client);
		GameScreen.setDefaultScreen(screen);
		// initialize the screen controller
		screenController = ScreenController.get(screen);
		pane.addComponentListener(new GameScreenResizer(screen));
		pane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				repositionEventProgressOverlay();
				repositionEventActivityOverlay();
			}
		});
		// ... and put it on the ground layer of the pane
		pane.add(screen, Component.LEFT_ALIGNMENT, JLayeredPane.DEFAULT_LAYER);
		eventProgressOverlay = new EventProgressBarOverlay();
		pane.add(eventProgressOverlay, JLayeredPane.PALETTE_LAYER);
		eventActivityOverlay = new EventActivityLeaderboardOverlay();
		pane.add(eventActivityOverlay, JLayeredPane.PALETTE_LAYER);
		initEventHudSettings();
		eventProgressRefreshTimer = new Timer(EVENT_REFRESH_INTERVAL_MILLIS, new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				refreshEventProgressOverlay();
			}
		});
		eventProgressRefreshTimer.start();

		eventOverlayDebounceTimer = new Timer(EVENT_OVERLAY_DEBOUNCE_MILLIS, new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				refreshEventProgressOverlay();
			}
		});
		eventOverlayDebounceTimer.setRepeats(false);

		eventOverlayFadeTimer = new Timer(40, new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateOverlayFade();
			}
		});

		eventOverlayEndStateTimer = new Timer(EVENT_END_STATE_DURATION_MILLIS, new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startOverlayFadeOut();
			}
		});
		eventOverlayEndStateTimer.setRepeats(false);

		runicAltar = new RunicAltar();
		pane.add(runicAltar.getRunicAltar(), JLayeredPane.MODAL_LAYER);

		quitDialog = new QuitDialog();
		pane.add(quitDialog.getQuitDialog(), JLayeredPane.MODAL_LAYER);

		setupChatEntry();
		chatLogArea = createChatLog(channelManager);
		containerPanel = createContainerPanel();
		leftColumn = createLeftPanel(client);
		frame = prepareMainWindow(splash);

		setupChatText();

		setupZoneChangeListeners(client);
		setupOverallLayout();

		int divWidth = verticalSplit.getDividerSize();
		WtWindowManager wm = WtWindowManager.getInstance();
		wm.registerSettingChangeListener(SCALE_PREFERENCE_PROPERTY, new ScalingSettingChangeListener(divWidth));
		wm.registerSettingChangeListener(DISPLAY_SIZE_PROPERTY, new DisplaySizeChangeListener());

		setInitialWindowStates();
		frame.setVisible(true);
		repositionEventProgressOverlay();
		repositionEventActivityOverlay();

		/*
		 * Used by settings dialog to restore the client's dimensions back to the
		 * original width and height. Needs to be called after frame.setSize().
		 */
		frameDefaultSize = frame.getSize();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				requestQuit(client);
			}
		});

		setupKeyHandling(client);

		locationHacksAndBugWorkaround();
		WindowUtils.restoreSize(frame);
	}

	private void setupInternalWindowProperties() {
		WtWindowManager windowManager = WtWindowManager.getInstance();
		windowManager.setDefaultProperties("corpse", false, 0, 190);
		windowManager.setDefaultProperties("chest", false, 100, 190);
	}

	private void setupChatEntry() {
		final KeyListener tabcompletion = new ChatCompletionHelper(chatText, World.get().getPlayerList().getNamesList(),
				SlashActionRepository.getCommandNames());
		chatText.addKeyListener(tabcompletion);

		chatFocusRedirector = new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				chatText.getPlayerChatText().requestFocus();
			}
		};

		final WtWindowManager windowManager = WtWindowManager.getInstance();
		updateChatFocusRedirector(windowManager.getProperty(SettingsProperties.MOVE_KEY_SCHEME_PROPERTY,
				SettingsProperties.MOVE_KEY_SCHEME_ARROWS));
		windowManager.registerSettingChangeListener(SettingsProperties.MOVE_KEY_SCHEME_PROPERTY,
				new SettingChangeListener() {
					@Override
					public void changed(String newValue) {
						updateChatFocusRedirector(newValue);
					}
				});
	}

	private void updateChatFocusRedirector(final String propertyValue) {
		final boolean shouldRedirect = !SettingsProperties.MOVE_KEY_SCHEME_WASD.equalsIgnoreCase(propertyValue);

		if (shouldRedirect) {
			if (!chatFocusRedirectInstalled) {
				screen.addFocusListener(chatFocusRedirector);
				chatFocusRedirectInstalled = true;
				chatText.getPlayerChatText().requestFocus();
			}
		} else if (chatFocusRedirectInstalled) {
			screen.removeFocusListener(chatFocusRedirector);
			chatFocusRedirectInstalled = false;
			screen.requestFocusInWindow();
		}
	}

	private void setupKeyHandling(StendhalClient client) {
		gameKeyHandler = new GameKeyHandler(client, screen);
		chatText.addKeyListener(gameKeyHandler);
		screen.addKeyListener(gameKeyHandler);
	}

	private JComponent createChatLog(NotificationChannelManager channelManager) {
		JComponent chatLogArea = new ChatLogArea(channelManager).getComponent();
		chatLogArea.setPreferredSize(new Dimension(screen.getWidth(), 171));
		// Bind the tab changing keys of the chat log to global key map
		InputMap input = chatLogArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		input.put(KeyStroke.getKeyStroke("control PAGE_UP"), "navigatePrevious");
		input.put(KeyStroke.getKeyStroke("control PAGE_DOWN"), "navigateNext");

		return chatLogArea;
	}

	/**
	 * Create the container panel (right side panel), and its child components.
	 *
	 * @return container panel
	 */
	private ContainerPanel createContainerPanel() {
		ContainerPanel containerPanel = new ContainerPanel();
		containerPanel.setAnimated(false);
		containerPanel.setMinimumSize(new Dimension(0, 0));

		/*
		 * Contents of the containerPanel
		 */
		// Character window
		character = new Character();
		containerPanel.addRepaintable(character);

		// Create the bag window
		inventory = new Bag();
		inventory.setAcceptedTypes(EntityMap.getClass("item", null, null));
		containerPanel.addRepaintable(inventory);
		userContext.addFeatureChangeListener(inventory);

		keyring = new KeyRing();
		// keyring's types are more limited, but it's simpler to let the server
		// handle those
		keyring.setAcceptedTypes(EntityMap.getClass("item", null, null));
		containerPanel.addRepaintable(keyring);
		userContext.addFeatureChangeListener(keyring);

		magicbag = new MagicBag();
		magicbag.setAcceptedTypes(EntityMap.getClass("item", null, null));
		containerPanel.addRepaintable(magicbag);
		userContext.addFeatureChangeListener(magicbag);

		/*
		 * portfolio = new Portfolio();
		 * portfolio.setAcceptedTypes(EntityMap.getClass("item", null, null));
		 * containerPanel.addRepaintable(portfolio);
		 * userContext.addFeatureChangeListener(portfolio);
		 */

		spells = new Spells();
		spells.setAcceptedTypes(EntityMap.getClass("spell", null, null));
		containerPanel.addRepaintable(spells);
		userContext.addFeatureChangeListener(spells);

		for (final FeatureChangeListener listener : character.getFeatureChangeListeners()) {
			userContext.addFeatureChangeListener(listener);
		}
		for (final ComponentListener listener : character.getComponentListeners()) {
			containerPanel.addComponentListener(listener);
		}

		return containerPanel;
	}

	/**
	 * Create the left side panel of the client.
	 *
	 * @return A component containing the components left of the game screen
	 */
	private JComponent createLeftPanel(StendhalClient client) {
		minimap = new MapPanelController(client);
		final StatsPanelController stats = StatsPanelController.get();
		final BuddyPanelController buddies = BuddyPanelController.get();
		ScrolledViewport buddyScroll = new ScrolledViewport((JComponent) buddies.getComponent());
		buddyScroll.setScrollingSpeed(SCROLLING_SPEED);
		final JComponent buddyPane = buddyScroll.getComponent();
		buddyPane.setBorder(null);

		final JComponent leftColumn = SBoxLayout.createContainer(SBoxLayout.VERTICAL);
		leftColumn.add(minimap.getComponent(), SLayout.EXPAND_X);
		leftColumn.add(stats.getComponent(), SLayout.EXPAND_X);

		// Add a background for the tabs. The column itself has none.
		JPanel tabBackground = new JPanel();
		tabBackground.setBorder(null);
		tabBackground.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));
		JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM);
		// Adjust the Tab Width, if we can. The default is pretty if there's
		// space, but in the column there are no pixels to waste.
		TabbedPaneUI ui = tabs.getUI();
		if (ui instanceof StyledTabbedPaneUI) {
			((StyledTabbedPaneUI) ui).setTabLabelMargins(1);
		}
		tabs.setFocusable(false);
		tabs.add("Przyjaciele", buddyPane);

		tabs.add("Grupa", GroupPanelController.get().getComponent());

		tabBackground.add(tabs, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		leftColumn.add(tabBackground, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		return leftColumn;
	}

	private JFrame prepareMainWindow(JFrame splash) {
		JFrame frame = MainFrame.prepare(splash);
		JComponent glassPane = DragLayer.get();
		frame.setGlassPane(glassPane);
		glassPane.setVisible(true);
		setupWindowWideListeners(frame);
		WindowUtils.watchFontSize(frame);

		return frame;
	}

	private void setupChatText() {
		Dimension displaySize = stendhal.getDisplaySize();
		chatText.getPlayerChatText().setMaximumSize(new Dimension(displaySize.width, Integer.MAX_VALUE));
		GameLoop.get().runAtQuit(chatText::saveCache);
	}

	@Override
	public void requestQuit(StendhalClient client) {
		if (client.getConnectionState() || !offline) {
			quitDialog.requestQuit(user);
		} else {
			System.exit(0);
		}
	}

	@Override
	public void getVisibleRunicAltar() {
		runicAltar.getVisibleRunicAltar();
	}

	@Override
	public void setOffline(final boolean offline) {
		screenController.setOffline(offline);
		this.offline = offline;
	}

	/**
	 * Requests repaint at the window areas that are painted according to the game
	 * loop frame rate.
	 */
	@Override
	public void triggerPainting() {
		if (frame.getState() != Frame.ICONIFIED) {
			paintCounter++;
			if (frame.isActive() || "false".equals(System.getProperty("stendhal.skip.inactive", "false"))
					|| paintCounter >= 20) {
				paintCounter = 0;
				logger.debug("Draw screen");
				minimap.refresh();
				containerPanel.repaintChildren();
				screen.repaint();
			}
		}
	}

	private void locationHacksAndBugWorkaround() {
		/*
		 * On some systems the window may end up occasionally unresponsive to keyboard
		 * use unless these are delayed.
		 */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				/*
				 * A massive kludge to ensure that the window position is treated properly.
				 * Without this popup menus can be misplaced and unusable until the user moves
				 * the game window. This can happen with certain window managers if the window
				 * manager moves the window as a result of resizing the window. "ui.dimensions"
				 * Description of the bug: https://bugzilla.redhat.com/show_bug.cgi?id=698295
				 *
				 * As of 2013-09-07 it is reproducible at least when using Mate desktop's marco
				 * window manager. Metacity and mutter have a workaround for the same issue in
				 * AWT.
				 */
				Point location = frame.getLocation();
				frame.setLocation(location.x + 1, location.y);
				frame.setLocation(location.x, location.y);

				// The keyboard fix mentioned above
				frame.setEnabled(true);
				chatText.getPlayerChatText().requestFocus();
			}
		});
	}

	/**
	 * For small screens. Setting the maximum window size does not help - pack()
	 * happily ignores it.
	 */
	private void smallScreenHacks() {
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Dimension current = frame.getSize();
		frame.setSize(Math.min(current.width, maxBounds.width), Math.min(current.height, maxBounds.height));
		/*
		 * Needed for small screens; Sometimes the divider is placed incorrectly unless
		 * we explicitly set it. Try to fit it on the screen and show a bit of the chat.
		 */
		verticalSplit.setDividerLocation(Math.min(stendhal.getDisplaySize().height, maxBounds.height - 80));
	}

	/**
	 * Modify the states of the on screen windows. The window manager normally
	 * restores the state of the window as it was on the previous session. For some
	 * windows this is not desirable.
	 * <p>
	 * <em>Note:</em> This need to be called from the event dispatch thread.
	 */
	private void setInitialWindowStates() {
		/*
		 * Window manager may try to restore the visibility of the dialog when it's
		 * added to the pane.
		 */
		quitDialog.getQuitDialog().setVisible(false);
		// Windows may have been closed in old clients
		character.setVisible(true);
		inventory.setVisible(true);
		/*
		 * Keyring and spells, on the other hand, *should* be hidden until revealed by
		 * feature change
		 */
		keyring.setVisible(false);
		magicbag.setVisible(false);
		runicAltar.getRunicAltar().setVisible(false);
		// portfolio.setVisible(false);
		spells.setVisible(false);
	}

	private void setupWindowWideListeners(JFrame frame) {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}

			@Override
			public void windowActivated(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}

			@Override
			public void windowGainedFocus(final WindowEvent ev) {
				chatText.getPlayerChatText().requestFocus();
			}
		});
		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				/*
				 * Stops player movement via keypress when focus is lost.
				 *
				 * FIXME: When focus is regained, direction key must be pressed twice to resume
				 * walking. Key states not flushed correctly?
				 */
				if (StendhalClient.serverVersionAtLeast("0.02")) {
					final RPAction stop = new RPAction();
					stop.put(TYPE, COND_STOP);
					ClientSingletonRepository.getClientFramework().send(stop);
					// Clear any direction keypresses
					gameKeyHandler.flushDirectionKeys();
				}
			}
		});
	}

	private void setupOverallLayout() {
		Dimension displaySize = stendhal.getDisplaySize();
		Container windowContent = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL);
		frame.setContentPane(windowContent);

		// Set maximum size to prevent the entry requesting massive widths, but
		// force expand if there's extra space anyway
		chatText.getPlayerChatText().setMaximumSize(new Dimension(displaySize.width, Integer.MAX_VALUE));
		JComponent chatEntryBox = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL);
		chatEntryBox.add(chatText.getPlayerChatText(), SLayout.EXPAND_X);

		chatEntryBox.add(new CharacterMap());
		final JComponent chatBox = new JPanel();
		chatBox.setBorder(null);
		chatBox.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));
		chatBox.add(chatEntryBox, SLayout.EXPAND_X);
		chatBox.add(chatLogArea, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane, chatBox);
		verticalSplit.setBorder(null);

		/*
		 * Fix the container panel size, so that it is always visible
		 */
		containerPanel.setMinimumSize(containerPanel.getPreferredSize());

		leftColumn.setMinimumSize(new Dimension());
		// Splitter between the left column and game screen
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, verticalSplit);
		// Ensure that the limits are obeyed even when the component is resized
		split.addComponentListener(new HorizontalSplitListener(split));

		horizontalSplit = split;
		int divWidth = verticalSplit.getDividerSize();
		pane.setPreferredSize(new Dimension(displaySize.width + divWidth, displaySize.height));
		horizontalSplit.setBorder(null);
		windowContent.add(horizontalSplit, SBoxLayout.constraint(SLayout.EXPAND_Y, SLayout.EXPAND_X));

		JComponent rightSidePanel = SBoxLayout.createContainer(SBoxLayout.VERTICAL);
		JComponent settings = new SettingsPanel();
		rightSidePanel.add(settings, SLayout.EXPAND_X);
		rightSidePanel.add(containerPanel, SBoxLayout.constraint(SLayout.EXPAND_Y, SLayout.EXPAND_X));
		windowContent.add(rightSidePanel, SLayout.EXPAND_Y);

		frame.pack();
		horizontalSplit.setDividerLocation(leftColumn.getPreferredSize().width);

		smallScreenHacks();
	}

	private void setupZoneChangeListeners(StendhalClient client) {
		client.addZoneChangeListener(screen);
		client.addZoneChangeListener(minimap);
		client.addZoneChangeListener(new WeatherSoundManager());
		client.addZoneChangeListener(new ZoneChangeListener() {
			@Override
			public void onZoneUpdate(final Zone zone) {
				currentZoneName = User.isNull() ? null : User.get().getZoneName();
				scheduleOverlayRefreshDebounced();
			}

			@Override
			public void onZoneChangeCompleted(final Zone zone) {
				currentZoneName = User.isNull() ? null : User.get().getZoneName();
				repositionEventProgressOverlay();
				repositionEventActivityOverlay();
				MapEventStatusStore.get().requestSnapshotRefresh();
				scheduleOverlayRefreshDebounced();
			}

			@Override
			public void onZoneChange(final Zone zone) {
				scheduleOverlayRefreshDebounced();
			}
		});
		// Disable side panel animation while changing zone
		client.addZoneChangeListener(new ZoneChangeListener() {
			@Override
			public void onZoneUpdate(Zone zone) {
			}

			@Override
			public void onZoneChangeCompleted(Zone zone) {
				containerPanel.setAnimated(true);
			}

			@Override
			public void onZoneChange(Zone zone) {
				containerPanel.setAnimated(false);
			}
		});
	}

	private void initEventHudSettings() {
		eventHudModePropertyKey = toUserScopedSettingsKey(EVENT_HUD_MODE_PROPERTY);
		eventHudOpacityPropertyKey = toUserScopedSettingsKey(EVENT_HUD_OPACITY_PROPERTY);

		final WtWindowManager wm = WtWindowManager.getInstance();
		applyEventHudMode(wm.getProperty(eventHudModePropertyKey, EVENT_HUD_MODE_FULL));
		applyEventHudOpacity(wm.getPropertyInt(eventHudOpacityPropertyKey, EVENT_HUD_OPACITY_DEFAULT));

		wm.registerSettingChangeListener(eventHudModePropertyKey, new SettingChangeListener() {
			@Override
			public void changed(final String newValue) {
				applyEventHudMode(newValue);
				repositionEventProgressOverlay();
				repositionEventActivityOverlay();
				scheduleOverlayRefreshDebounced();
			}
		});
		wm.registerSettingChangeListener(eventHudOpacityPropertyKey, new SettingChangeListener() {
			@Override
			public void changed(final String newValue) {
				applyEventHudOpacity(MathHelper.parseIntDefault(newValue, EVENT_HUD_OPACITY_DEFAULT));
			}
		});
	}

	private String toUserScopedSettingsKey(final String baseKey) {
		final String userName = userContext.getName();
		if ((userName == null) || userName.trim().isEmpty()) {
			return baseKey;
		}
		return baseKey + "." + userName.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
	}

	private void applyEventHudMode(final String mode) {
		if (EVENT_HUD_MODE_COMPACT.equals(mode)) {
			eventHudMode = EVENT_HUD_MODE_COMPACT;
			eventProgressOverlay.setCompactMode(true);
			return;
		}
		if (EVENT_HUD_MODE_HIDDEN.equals(mode)) {
			eventHudMode = EVENT_HUD_MODE_HIDDEN;
			eventProgressOverlay.setCompactMode(false);
			eventProgressOverlay.hideOverlay();
			eventActivityOverlay.setVisible(false);
			return;
		}
		eventHudMode = EVENT_HUD_MODE_FULL;
		eventProgressOverlay.setCompactMode(false);
	}

	private void applyEventHudOpacity(final int percent) {
		final int clamped = MathHelper.clamp(percent, EVENT_HUD_OPACITY_MIN, EVENT_HUD_OPACITY_MAX);
		eventHudOpacity = clamped / 100.0f;
		if (eventProgressOverlay.isVisible()) {
			eventProgressOverlay.repaint();
		}
		eventActivityOverlay.setOverlayAlpha(eventHudOpacity);
	}

	private void refreshEventProgressOverlay() {
		if (!pane.isShowing()) {
			return;
		}
		final ActiveMapEventStatus visibleStatus = MapEventStatusStore.get()
				.getVisibleStatusForZone(resolveCurrentZoneName());
		if (minimap != null) {
			minimap.setActiveMapEventStatus(visibleStatus);
		}
		if (System.currentTimeMillis() < overlayRefreshSuppressedUntilMillis) {
			return;
		}
		if (EVENT_HUD_MODE_HIDDEN.equals(eventHudMode)) {
			eventProgressOverlay.hideOverlay();
			eventActivityOverlay.setVisible(false);
			if (minimap != null) {
				minimap.setActiveMapEventStatus(null);
			}
			return;
		}
		if (visibleStatus == null) {
			if ((lastShownEventStatus != null) && (lastShownEventStatus.getRemainingSeconds() <= 0)) {
				showEventEndedState(lastShownEventStatus);
				return;
			}
			startOverlayFadeOut();
			eventActivityOverlay.setVisible(false);
			return;
		}

		lastShownEventStatus = visibleStatus;
		stopOverlayFade();
		eventOverlayEndStateTimer.stop();

		final String remaining = formatRemaining(visibleStatus.getRemainingSeconds());
		final boolean koscieliskoEscort = KOSCIELISKO_ESCORT_EVENT_ID.equals(visibleStatus.getEventId());
		final HudDisplayData hudData = buildHudDisplayData(visibleStatus, remaining, koscieliskoEscort);
		if (eventProgressOverlay.isShowingEvent(visibleStatus.getEventId())) {
			eventProgressOverlay.updateOverlay(visibleStatus.getEventId(), visibleStatus.getEventName(), hudData.details,
					hudData.progressPercent, hudData.value);
		} else {
			eventProgressOverlay.showOverlay(visibleStatus.getEventId(), visibleStatus.getEventName(), hudData.details,
					hudData.progressPercent, hudData.value);
		}
		eventProgressOverlay.setOverlayAlpha(eventHudOpacity);
		repositionEventProgressOverlay();
		eventActivityOverlay.setOverlayAlpha(eventHudOpacity);
		updateEventActivityOverlay(visibleStatus);
	}

	private void scheduleOverlayRefreshDebounced() {
		overlayRefreshSuppressedUntilMillis = System.currentTimeMillis() + EVENT_OVERLAY_DEBOUNCE_MILLIS;
		eventOverlayDebounceTimer.restart();
	}

	private void showEventEndedState(final ActiveMapEventStatus endedStatus) {
		if (eventOverlayEndStateTimer.isRunning()) {
			return;
		}
		stopOverlayFade();
		eventProgressOverlay.showTerminalState(endedStatus.getEventName(), "Finał wydarzenia", "Zdarzenie zakończone");
		eventActivityOverlay.setVisible(false);
		eventProgressOverlay.setOverlayAlpha(eventHudOpacity);
		eventActivityOverlay.setOverlayAlpha(eventHudOpacity);
		eventOverlayEndStateTimer.restart();
		lastShownEventStatus = null;
	}

	private void startOverlayFadeOut() {
		if (!eventProgressOverlay.isVisible() || eventOverlayFadeTimer.isRunning()) {
			return;
		}
		eventOverlayEndStateTimer.stop();
		fadeStartMillis = System.currentTimeMillis();
		eventOverlayFadeTimer.start();
	}

	private void stopOverlayFade() {
		eventOverlayFadeTimer.stop();
		eventProgressOverlay.setOverlayAlpha(eventHudOpacity);
	}

	private void updateOverlayFade() {
		final long elapsed = Math.max(0L, System.currentTimeMillis() - fadeStartMillis);
		final float fadeAlpha = Math.max(0.0f, 1.0f - ((float) elapsed / (float) EVENT_OVERLAY_FADE_DURATION_MILLIS));
		eventProgressOverlay.setOverlayAlpha(fadeAlpha * eventHudOpacity);
		eventActivityOverlay.setOverlayAlpha(fadeAlpha * eventHudOpacity);
		final float alpha = fadeAlpha;
		if (alpha <= 0.0f) {
			eventOverlayFadeTimer.stop();
			eventProgressOverlay.hideOverlay();
			eventActivityOverlay.setVisible(false);
		}
	}

	private String resolveCurrentZoneName() {
		if (!User.isNull()) {
			final String zoneName = User.get().getZoneName();
			if ((zoneName != null) && !zoneName.isEmpty()) {
				currentZoneName = zoneName;
			}
		}
		return currentZoneName;
	}

	private String formatRemaining(final int secondsTotal) {
		final int bounded = Math.max(0, secondsTotal);
		final int minutes = bounded / 60;
		final int seconds = bounded % 60;
		return minutes + ":" + ((seconds < 10) ? "0" + seconds : String.valueOf(seconds));
	}

	private String formatWaveLabel(final ActiveMapEventStatus status) {
		if (status.getTotalWaves() <= 0) {
			return "Fala -/-";
		}
		final int current = Math.max(1, Math.min(status.getCurrentWave(), status.getTotalWaves()));
		return "Fala " + current + "/" + status.getTotalWaves();
	}

	private HudDisplayData buildHudDisplayData(final ActiveMapEventStatus status, final String remaining,
			final boolean koscieliskoEscort) {
		final ActiveMapEventStatus.CapturePointStatus activePoint = resolveActiveCapturePoint(status);
		if (activePoint == null) {
			return buildLegacyHudDisplayData(status, remaining, koscieliskoEscort);
		}

		final String nearestLabel = formatCapturePointLabel(activePoint);
		final int nearestPercent = activePoint.getProgressPercent();
		if (EVENT_HUD_MODE_COMPACT.equals(eventHudMode)) {
			return new HudDisplayData("", nearestPercent,
					"Czas: " + remaining + " • " + nearestLabel + " " + nearestPercent + "%");
		}

		final String details = "Czas do końca: " + remaining;
		return new HudDisplayData(details, nearestPercent,
				"Aktywny punkt: " + nearestLabel + " • " + nearestPercent + "%");
	}

	private HudDisplayData buildLegacyHudDisplayData(final ActiveMapEventStatus status, final String remaining,
			final boolean koscieliskoEscort) {
		final String waveLabel = formatWaveLabel(status);
		final String details = koscieliskoEscort
				? "Czas do końca: " + remaining
				: waveLabel + " • Czas do końca: " + remaining;
		final String defenseStatus = status.getDefenseStatus();
		final String defeatProgress = status.getEventDefeatPercent() + "% wybitych"
				+ " (" + status.getEventDefeatedCreatures() + "/"
				+ status.getEventTotalSpawnedCreatures() + ")";
		final int progressPercent = koscieliskoEscort ? status.getProgressPercent() : status.getEventDefeatPercent();
		final String value;
		if (koscieliskoEscort) {
			value = remaining;
		} else if (EVENT_HUD_MODE_COMPACT.equals(eventHudMode)) {
			value = waveLabel + " • " + remaining;
		} else {
			value = defeatProgress + ((defenseStatus == null || defenseStatus.trim().isEmpty()) ? "" : " • " + defenseStatus);
		}
		return new HudDisplayData(details, progressPercent, value);
	}

	private ActiveMapEventStatus.CapturePointStatus resolveActiveCapturePoint(final ActiveMapEventStatus status) {
		if (User.isNull()) {
			return null;
		}
		final User player = User.get();
		for (ActiveMapEventStatus.CapturePointStatus capturePoint : status.getCapturePoints()) {
			if (!ActiveMapEventStatus.isInsideCapturePoint(player.getZoneName(), player.getX(), player.getY(), capturePoint)) {
				continue;
			}
			if (!isCapturePointInProgress(capturePoint)) {
				continue;
			}
			return capturePoint;
		}
		return null;
	}

	private boolean isCapturePointInProgress(final ActiveMapEventStatus.CapturePointStatus capturePoint) {
		final int progress = capturePoint.getProgressPercent();
		return progress > 0 && progress < 100;
	}

	private String formatCapturePointLabel(final ActiveMapEventStatus.CapturePointStatus capturePoint) {
		if (capturePoint == null || capturePoint.getPointId() == null || capturePoint.getPointId().trim().isEmpty()) {
			return "Punkt";
		}
		return capturePoint.getPointId();
	}

	private static final class HudDisplayData {
		private final String details;
		private final int progressPercent;
		private final String value;

		private HudDisplayData(final String details, final int progressPercent, final String value) {
			this.details = details;
			this.progressPercent = progressPercent;
			this.value = value;
		}
	}

	private void updateEventActivityOverlay(final ActiveMapEventStatus status) {
		final List<String> rows = mapActivityRows(status.getActivityTop());
		eventActivityOverlay.updateRows(rows);
		repositionEventActivityOverlay();
	}

	private List<String> mapActivityRows(final List<String> rawRows) {
		final List<String> mapped = new ArrayList<String>();
		for (String row : rawRows) {
			if (row == null || row.trim().isEmpty()) {
				continue;
			}
			final int separator = row.lastIndexOf("::");
			if (separator <= 0 || separator >= (row.length() - 2)) {
				mapped.add(row);
				continue;
			}
			mapped.add(row.substring(0, separator) + " — " + row.substring(separator + 2) + " pkt");
		}
		return mapped;
	}

	private void repositionEventActivityOverlay() {
		final Dimension preferred = eventActivityOverlay.getPreferredSize();
		eventActivityOverlay.setBounds(EVENT_ACTIVITY_OVERLAY_LEFT_MARGIN, EVENT_ACTIVITY_OVERLAY_TOP_MARGIN,
				preferred.width, preferred.height);
	}

	private void repositionEventProgressOverlay() {
		final Dimension screenSize = screen.getSize();
		final Dimension preferred = eventProgressOverlay.getPreferredSize();
		final int width = preferred.width;
		final int height = preferred.height;
		final int x = Math.max(0, (screenSize.width - width) / 2);

		int y = EVENT_OVERLAY_TOP_MARGIN;
		final JComponent minimapComponent = (minimap == null) ? null : minimap.getComponent();
		if ((minimapComponent != null) && minimapComponent.isShowing()) {
			final Rectangle minimapBounds = SwingUtilities.convertRectangle(
					minimapComponent.getParent(), minimapComponent.getBounds(), pane);
			final Rectangle overlayBounds = new Rectangle(x, y, width, height);
			if (overlayBounds.intersects(minimapBounds)) {
				y = minimapBounds.y + minimapBounds.height + EVENT_OVERLAY_SAFE_GAP;
			}
		}

		eventProgressOverlay.setBounds(x, y, width, height);
	}

	@Override
	public void updateUser(User user) {
		this.user = user;
		character.setPlayer(user);
		keyring.setSlot(user, "keyring");
		magicbag.setSlot(user, "magicbag");
		// portfolio.setSlot(user, "portfolio");
		spells.setSlot(user, "spells");
		inventory.setSlot(user, "bag");
		runicAltar.setPlayer(user);
	}

	@Override
	public JFrame getFrame() {
		return frame;
	}

	@Override
	public void resetClientDimensions() {
		int frameState = frame.getExtendedState();

		/*
		 * Do not attempt to reset client dimensions if window is maximized. Prevents
		 * resizing errors for child components.
		 */
		if (frameState != Frame.MAXIMIZED_BOTH) {
			frame.setSize(frameDefaultSize);
		}
	}

	@Override
	public Collection<PositionChangeListener> getPositionChangeListeners() {
		return Arrays.asList(screenController, minimap);
	}

	@Override
	public void setChatLine(String text) {
		chatText.setChatLine(text);
	}

	@Override
	public void afterPainting() {
		gameKeyHandler.processDelayedDirectionRelease();
	}

	@Override
	public void beforePainting() {
		screen.nextFrame();
	}

	@Override
	public void addDialog(Component dialog) {
		pane.add(dialog, JLayeredPane.PALETTE_LAYER);
	}

	@Override
	public boolean isOffline() {
		return offline;
	}

	@Override
	public void addAchievementBox(String title, String description, String category) {
		screen.addAchievementBox(title, description, category);
	}

	@Deprecated
	@Override
	public void addGameScreenText(double x, double y, String text, NotificationType type, boolean isTalking) {
		screenController.addText(x, y, text, type, isTalking);
	}

	@Deprecated
	@Override
	public void addGameScreenText(final Entity entity, final String text, final NotificationType type,
			final boolean isTalking) {
		screenController.addText(entity, text, type, isTalking);
	}

	@Override
	public void switchToSpellState(RPObject spell) {
		screen.switchToSpellCastingState(spell);
	}

	@Override
	public void chooseOutfit() {
		final RPObject player = userContext.getPlayer();

		if (!player.has("outfit_ext")) {
			final int code;

			if (player.has("outfit_org")) {
				code = player.getInt("outfit_org");
			} else {
				code = player.getInt("outfit");
			}

			final int body = code % 100;
			final int dress = code / 100 % 100;
			final int head = (int) (code / Math.pow(100, 2) % 100);
			final int hair = (int) (code / Math.pow(100, 3) % 100);
			final int detail = (int) (code / Math.pow(100, 4) % 100);

			final StringBuilder sb = new StringBuilder();
			sb.append("body=" + body);
			sb.append(",dress=" + dress);
			sb.append(",head=" + head);
			sb.append(",hair=" + hair);
			sb.append(",detail=" + detail);

			if (outfitDialog == null) {
				// Here we actually want to call new OutfitColor(). Modifying
				// OutfitColor.PLAIN would be a bad thing.
				outfitDialog = new OutfitDialog(frame, "PolanieOnLine - Zmień wygląd postaci", sb.toString(),
						new OutfitColor(player));

				outfitDialog.setVisible(true);
			} else {
				// XXX: (AntumDeluge) why does this use "OutfitColor.get" but above uses "new
				// OutfitColor"???
				outfitDialog.setState(sb.toString(), OutfitColor.get(player));

				outfitDialog.setVisible(true);
				outfitDialog.toFront();
			}
		} else {
			final String stroutfit;

			if (player.has("outfit_ext_orig")) {
				stroutfit = player.get("outfit_ext_orig");
			} else {
				stroutfit = player.get("outfit_ext");
			}

			if (outfitDialog == null) {
				// Here we actually want to call new OutfitColor(). Modifying
				// OutfitColor.PLAIN would be a bad thing.
				outfitDialog = new OutfitDialog(frame, "PolanieOnLine - Zmień wygląd postaci", stroutfit,
						new OutfitColor(player));
				outfitDialog.setVisible(true);
			} else {
				outfitDialog.setState(stroutfit, OutfitColor.get(player));

				outfitDialog.setVisible(true);
				outfitDialog.toFront();
			}
		}
	}

	private final class HorizontalSplitListener extends ComponentAdapter {
		private final JSplitPane split;
		// Start with a large value, so that the divider is placed as left
		// as possible
		private int oldWidth = Integer.MAX_VALUE;

		HorizontalSplitListener(JSplitPane split) {
			this.split = split;
		}

		@Override
		public void componentResized(ComponentEvent e) {
			Dimension displaySize = stendhal.getDisplaySize();
			if (screen.isScaled()) {
				/*
				 * Default behavior is otherwise reasonable, except the user will likely want to
				 * use the vertical space for the game screen.
				 *
				 * Try to keep the aspect ratio near the optimum; the sizes have not changed
				 * when this gets called, so push it to the EDT.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						double hScale = screen.getWidth() / (double) displaySize.width;
						int preferredLocation = (int) (hScale * displaySize.height);
						verticalSplit.setDividerLocation(preferredLocation);
					}
				});
			} else {
				int position = split.getDividerLocation();
				/*
				 * The trouble: the size of the game screen is likely the one that the player
				 * wants to preserve when making the window smaller. Swing provides no default
				 * way to the old component size, so we stash the interesting dimension in
				 * oldWidth.
				 */
				int width = split.getWidth();
				int oldRightDiff = oldWidth - position;
				int widthChange = width - oldWidth;
				int underflow = widthChange + position;
				if (underflow < 0) {
					/*
					 * Extreme size reduction. The divider location would have changed as the
					 * result. Use the previous location instead of the current.
					 */
					oldRightDiff = oldWidth - split.getLastDividerLocation();
				}
				position = MathHelper.clamp(width - oldRightDiff, split.getMinimumDividerLocation(),
						split.getMaximumDividerLocation());

				split.setDividerLocation(position);
				oldWidth = split.getWidth();
			}
		}
	}

	private class ScalingSettingChangeListener implements SettingChangeListener {
		private final int divWidth;

		ScalingSettingChangeListener(int divWidth) {
			this.divWidth = divWidth;
			changed(WtWindowManager.getInstance().getProperty(SCALE_PREFERENCE_PROPERTY, "true"));
		}

		@Override
		public final void changed(String newValue) {
			boolean scale = Boolean.parseBoolean(newValue);
			screen.setUseScaling(scale);
			if (scale) {
				// Clear the resize limits
				verticalSplit.setMaximumSize(null);
				pane.setMaximumSize(null);
			} else {
				Dimension displaySize = stendhal.getDisplaySize();

				// Set the limits
				verticalSplit.setMaximumSize(new Dimension(displaySize.width + divWidth, Integer.MAX_VALUE));
				pane.setMaximumSize(displaySize);
				// The user may have resized the screen outside allowed
				// parameters
				int overflow = horizontalSplit.getWidth() - horizontalSplit.getDividerLocation() - displaySize.width
						- divWidth;
				if (overflow > 0) {
					horizontalSplit.setDividerLocation(horizontalSplit.getDividerLocation() + overflow);
				}
				if (verticalSplit.getDividerLocation() > displaySize.height) {
					verticalSplit.setDividerLocation(displaySize.height);
				}
			}
		}
	}

	private class DisplaySizeChangeListener implements SettingChangeListener {
		DisplaySizeChangeListener() {
			String stored = WtWindowManager.getInstance().getProperty(DISPLAY_SIZE_PROPERTY,
					Integer.toString(stendhal.getDisplaySizeIndex()));
			changed(stored);
		}

		@Override
		public void changed(String newValue) {
			int maxIndex = Math.max(0, stendhal.getAvailableDisplaySizes().size() - 1);
			int parsed = MathHelper.parseIntDefault(newValue, stendhal.getDisplaySizeIndex());
			int clamped = MathHelper.clamp(parsed, 0, maxIndex);
			if (clamped != parsed) {
				WtWindowManager.getInstance().setProperty(DISPLAY_SIZE_PROPERTY, Integer.toString(clamped));
			}
			stendhal.setDisplaySizeIndex(clamped);
			applyDisplaySize(stendhal.getDisplaySize());
		}

		private void applyDisplaySize(final Dimension displaySize) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					int divWidth = verticalSplit.getDividerSize();
					pane.setPreferredSize(new Dimension(displaySize.width + divWidth, displaySize.height));
					pane.setSize(pane.getPreferredSize());
					screen.setSize(displaySize);
					screen.setPreferredSize(displaySize);
					screen.revalidate();
					pane.revalidate();
					if (screen.isScaled()) {
						verticalSplit.setMaximumSize(null);
						pane.setMaximumSize(null);
					} else {
						verticalSplit.setMaximumSize(new Dimension(displaySize.width + divWidth, Integer.MAX_VALUE));
						pane.setMaximumSize(displaySize);
						int overflow = horizontalSplit.getWidth() - horizontalSplit.getDividerLocation()
								- displaySize.width - divWidth;
						if (overflow > 0) {
							horizontalSplit.setDividerLocation(horizontalSplit.getDividerLocation() + overflow);
						}
						if (verticalSplit.getDividerLocation() > displaySize.height) {
							verticalSplit.setDividerLocation(displaySize.height);
						}
					}
					chatText.getPlayerChatText().setMaximumSize(new Dimension(displaySize.width, Integer.MAX_VALUE));
					frame.pack();
					horizontalSplit.setDividerLocation(leftColumn.getPreferredSize().width);
				}
			});
		}
	}

	/**
	 * The layered pane where the game screen is does not automatically resize the
	 * game screen. This handler is needed to do that work.
	 */
	private static class GameScreenResizer extends ComponentAdapter {
		private final Component child;

		/**
		 * Create a SplitPaneResizeListener.
		 *
		 * @param child the component that needs to be resized
		 */
		GameScreenResizer(Component child) {
			this.child = child;
		}

		@Override
		public void componentResized(ComponentEvent e) {
			// Pass on resize event
			child.setSize(e.getComponent().getSize());
		}
	}
}
