/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.settings;

import static games.stendhal.client.gui.settings.SettingsProperties.BUBBLES_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.DISPLAY_SIZE_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.HP_BAR_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.REBORN_BADGES_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.ITEM_TOOLTIP_COMPARISON_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.OVERRIDE_AA;
import static games.stendhal.client.gui.settings.SettingsProperties.FPS_COUNTER_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.FPS_LIMIT_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.SCALE_SCREEN_PROPERTY;
import static games.stendhal.client.gui.settings.SettingsProperties.SCALE_SCREEN_DEFAULT;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.stendhal;
import games.stendhal.client.UiRenderingMethod;
import games.stendhal.client.gui.WindowMode;
import games.stendhal.client.gui.WindowModeController;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;

/**
 * Page for style settings.
 */
class VisualSettings {
	private static final String STYLE_PROPERTY = "ui.style";
	private static final String DEFAULT_STYLE = "Wood (domyślny)";
	private static final String TRANSPARENCY_PROPERTY = "ui.transparency";

	private static final int[] FPS_OPTIONS = new int[] { 30, 60, 90, 120, 144, 165, 240 };

	/** Default decorative font. */
	private static final String DEFAULT_FONT = "AntykwaTorunska";
	/** Default font size. */
	private static final int DEFAULT_FONT_SIZE = 12;
	/** Smallest size in the font size selector. */
	private static final int FONT_MIN_SIZE = 8;
	/** Largest size in the font size selector. */
	private static final int FONT_MAX_SIZE = 20;
	/** Property used for the decorative font. */
	private static final String FONT_PROPERTY = "ui.logfont";
	/** Property used for the decorative font. */
	private static final String FONT_SIZE_PROPERTY = "ui.font_size";

	private static final String GAMESCREEN_CREATURESPEECH = "gamescreen.creaturespeech";
	private static final String GAMESCREEN_BLOOD = "gamescreen.blood";
	private static final String GAMESCREEN_NONUDE = "gamescreen.nonude";
	private static final String GAMESCREEN_CURSORCLASSIC = "gamescreen.cursorclassic";

	/** Property used for toggling map coloring on. */
	private static final String MAP_COLOR_PROPERTY = "ui.colormaps";

	/**
	 * Create new StyleSettings.
	 */
	VisualSettings(SettingsPages pages) {
		JComponent screen = pages.addGroup(SettingsPages.Category.DISPLAY, "Ekran");
		JComponent performance = pages.addGroup(SettingsPages.Category.DISPLAY, "Płynność");
		JComponent style = pages.addGroup(SettingsPages.Category.APPEARANCE, "Styl");
		JComponent world = pages.addGroup(SettingsPages.Category.APPEARANCE, "Świat gry");
		JComponent characters = pages.addGroup(SettingsPages.Category.APPEARANCE, "Postacie i kursor");
		JComponent fonts = pages.addGroup(SettingsPages.Category.TEXT, "Czcionki");
		JComponent bubbles = pages.addGroup(SettingsPages.Category.TEXT, "Dymki");
		JComponent items = pages.addGroup(SettingsPages.Category.COMBAT, "Przedmioty");

		style.add(createStyleTypeSelector(), SLayout.EXPAND_X);
		JComponent advanced = SettingsComponentFactory.createSettingsGroup("Wyświetlanie interfejsu");
		advanced.add(createRenderingSelector(), SLayout.EXPAND_X);
		advanced.add(createTransparencySelector(), SLayout.EXPAND_X);
		pages.addComponent(SettingsPages.Category.DISPLAY, SettingsComponentFactory.createAdvancedSection(advanced));
		screen.add(createWindowModeSelector(), SLayout.EXPAND_X);
		screen.add(createDisplaySizeSelector(), SLayout.EXPAND_X);
		performance.add(createFpsSelector(), SLayout.EXPAND_X);

		final JCheckBox fpsCounterToggle = SettingsComponentFactory.createSettingsToggle(FPS_COUNTER_PROPERTY, false,
				"Pokaż licznik FPS", "Wyświetla aktualny licznik klatek na sekundę na ekranie gry.");
		performance.add(fpsCounterToggle);

		// Lighting effects
		JCheckBox mapColoring = SettingsComponentFactory.createSettingsToggle(MAP_COLOR_PROPERTY, true,
				"Efekty świetlne", "Pokaż nocne światła i inne kolorowe efekty");
		world.add(mapColoring);
		// Coloring setting needs a map change to take an effect, so we need to
		// inform the player about the delayed effect.
		mapColoring.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Efekty świetlne są teraz " + tmp
						+ ". Możesz zmienić mapę lub przelogować się, aby zmiana zadziałała.";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		JCheckBox weather = SettingsComponentFactory.createSettingsToggle("ui.draw_weather", true, "Pokaż pogodę",
				"Pokazuje efekty pogodowe.");
		world.add(weather);
		weather.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Efekty pogodowe zostały " + tmp + ".";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// shadows
		JCheckBox shadows = SettingsComponentFactory.createSettingsToggle("gamescreen.shadows", true, "Pokaż cienie",
				"Pokazuje cienie pod różnymi obiektami.");
		world.add(shadows);
		shadows.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Cienie zostały " + tmp
						+ ". Niektóre zmiany zaczną działać dopiero po zmianie mapy lub przelogowaniu się.";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// blood
		JCheckBox showBloodToggle = SettingsComponentFactory.createSettingsToggle(GAMESCREEN_BLOOD, true,
				"Pokaż krew i zwłoki", "Pokazuje plamy krwi podczas uderzenia w walce i zwłoki.");
		world.add(showBloodToggle);
		// Inform players that some images won-t update until after client is restarted.
		// FIXME: Can't images be updated via map change?
		showBloodToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Krew i zwłoki zostały " + tmp
						+ ". Niektóre zmiany zaczną działać dopiero po ponownym uruchomieniu klienta.";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// no nude
		JCheckBox noNudeToggle = SettingsComponentFactory.createSettingsToggle(GAMESCREEN_NONUDE, true,
				"Pokaż bieliznę", "\"Nagie\" postacie zostaną pokryte bielizną.");
		characters.add(noNudeToggle);
		noNudeToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączona" : "wyłączona";
				String msg = "Bielizna została " + tmp
						+ ". Niektóre zmiany zaczną działać dopiero po zmianie mapy lub przelogowaniu się.";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// classic cursor
		JCheckBox cursorClassicToggle = SettingsComponentFactory.createSettingsToggle(GAMESCREEN_CURSORCLASSIC, false,
				"Pokaż klasyczny kursor", "Klasyczny wygląd kursora.");
		characters.add(cursorClassicToggle);
		cursorClassicToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączony" : "wyłączony";
				String msg = "Klasyczny kursor został " + tmp
						+ ". Zmiana wyglądu kursora zacznie działać dopiero po przelogowaniu się.";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// show creature speech bubbles
		JCheckBox showCreatureSpeechToggle = SettingsComponentFactory.createSettingsToggle(GAMESCREEN_CREATURESPEECH,
				true, "Pokaż dymki potworów", "Pokazuje dymki z tekstem potworów w ekranie klienta");
		bubbles.add(showCreatureSpeechToggle);
		showCreatureSpeechToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Dymki potworów zostały " + tmp + ".";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		final JCheckBox scaleScreenToggle = SettingsComponentFactory.createSettingsToggle(SCALE_SCREEN_PROPERTY, SCALE_SCREEN_DEFAULT,
				"Dopasuj widok gry do okna",
				"Skaluje widok gry do dostępnego miejsca. Wyłącz, aby zachować oryginalny rozmiar grafiki.");
		screen.add(scaleScreenToggle);
		scaleScreenToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Skalowanie widoku gry zostało " + tmp + ".";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});

		// controller for setting visibility of HP br
		final JCheckBox showHPBarToggle = SettingsComponentFactory.createSettingsToggle(HP_BAR_PROPERTY, true,
				"Pokaż pasek zdrowia", "Pokazuje pasek reprezentujący aktualne zdrowie.");
		showHPBarToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				StatsPanelController.get().toggleHPBar(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		characters.add(showHPBarToggle);

		final JCheckBox showRebornBadgesToggle = SettingsComponentFactory.createSettingsToggle(
				REBORN_BADGES_PROPERTY, true, "Pokaż odznaki odrodzeń",
				"Pokazuje tarczę z liczbą odrodzeń przy nazwach wszystkich graczy.");
		characters.add(showRebornBadgesToggle);
		showRebornBadgesToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				final boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
				final String state = enabled ? "włączone" : "wyłączone";
				ClientSingletonRepository.getUserInterface().addEventLine(new EventLine("",
						"Odznaki odrodzeń zostały " + state + ".", NotificationType.CLIENT));
			}
		});

		// system font antialiasing
		final JCheckBox overrideSystemFontAA = SettingsComponentFactory.createSettingsToggle(OVERRIDE_AA, false,
				"Wymuś wygładzanie czcionki",
				"Włącz tę opcję, jeśli klient nie rozpoznaje konfiguracji antyaliasingu czcionek na pulpicie.");
		overrideSystemFontAA.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				ClientSingletonRepository.getUserInterface().addEventLine(new EventLine("",
						"Zmiany zaczną działać po ponownym uruchomieniu klienta.", NotificationType.CLIENT));
			}
		});
		fonts.add(createFontSizeSelector(), SLayout.EXPAND_X);
		fonts.add(overrideSystemFontAA);
		fonts.add(createFontSelector(), SLayout.EXPAND_X);

		final JCheckBox chatBubblesToggle = SettingsComponentFactory.createSettingsToggle(BUBBLES_PROPERTY, true,
				"Ruchome dymki czatu", "Dymki czatu podążają za graczem i innymi obiektami.");
		chatBubblesToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				String tmp = enabled ? "włączone" : "wyłączone";
				String msg = "Ruchome dymki chatu zostały " + tmp + ".";
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("", msg, NotificationType.CLIENT));
			}
		});
		bubbles.add(chatBubblesToggle);

		final JCheckBox itemTooltipComparisonToggle = SettingsComponentFactory.createSettingsToggle(
				ITEM_TOOLTIP_COMPARISON_PROPERTY, true,
				"Porównuj statystyki przedmiotów",
				"Pokazuje w opisie różnice względem aktualnie założonego przedmiotu.");
		items.add(itemTooltipComparisonToggle);
		JComponent combatStats = pages.addGroup(SettingsPages.Category.COMBAT, "Statystyki postaci");
		combatStats.add(SettingsComponentFactory.createSettingsToggle(SettingsProperties.SHOW_COMBAT_STATS_PROPERTY,
				false, "Pokazuj statystyki procentowe",
				"Udostępnia przycisk otwierający okno statystyk procentowych obok widoku gry."));
		combatStats.add(SettingsComponentFactory.createSettingsToggle(SettingsProperties.SHOW_STAT_LIMITS_PROPERTY,
				false, "Pokazuj limity statystyk",
				"Pokazuje limity obok wartości statystyk."));
	}

	/**
	 * Create a selector for styles.
	 *
	 * @return combo box with style options
	 */
	private JComponent createStyleSelector() {
		final JComboBox<String> selector = new JComboBox<>();

		// Fill with available styles
		for (String s : StyleFactory.getAvailableStyles()) {
			selector.addItem(s);
		}

		final WtWindowManager wm = WtWindowManager.getInstance();
		String currentStyle = wm.getProperty(STYLE_PROPERTY, DEFAULT_STYLE);
		selector.setSelectedItem(currentStyle);

		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = selector.getSelectedItem();
				wm.setProperty(STYLE_PROPERTY, selected.toString());
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("",
								"Nowy styl będzie wykorzystywany przy kolejnym uruchomieniu klienta gry.",
								NotificationType.CLIENT));
			}
		});

		return selector;
	}

	/**
	 * Create the transparency mode selector row.
	 *
	 * @return component holding the selector and the related label
	 */
	private JComponent createTransparencySelector() {
		final JComboBox<String> selector = new JComboBox<>();
		final String[][] data = {
				{ "Automatyczny (domyślny)", "auto",
						"Odpowiedni tryb jest wybierany automatycznie w zależności od szybkości systemu." },
				{ "Pełna przezroczystość", "translucent",
						"Będzie używał półprzezroczystych obrazów o ile to możliwe. Może to spowolnić system." },
				{ "Prosta przezroczystość", "bitmask",
						"Użyje prostej przezroczystości tam gdzie obraz jest w pełni przezroczysty lub całkowicie nieprzezroczysty. <P>Użyj tego ustawienia na starszych komputerach jeśli gra nie odpowiada." } };

		// Convenience mapping for getting the data rows from either short or
		// long names
		final Map<String, String[]> desc2data = new HashMap<String, String[]>();
		Map<String, String[]> key2data = new HashMap<String, String[]>();

		for (String[] s : data) {
			// fill the selector...
			selector.addItem(s[0]);
			// ...and prepare the convenience mappings in the same step
			desc2data.put(s[0], s);
			key2data.put(s[1], s);
		}

		// Find out the current option
		final WtWindowManager wm = WtWindowManager.getInstance();
		String currentKey = wm.getProperty(TRANSPARENCY_PROPERTY, "auto");
		String[] currentData = key2data.get(currentKey);
		if (currentData == null) {
			// invalid value; force the default
			currentData = key2data.get("auto");
		}
		selector.setSelectedItem(currentData[0]);
		selector.setRenderer(new TooltippedRenderer(data));

		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = selector.getSelectedItem();
				String[] selectedData = desc2data.get(selected);
				wm.setProperty(TRANSPARENCY_PROPERTY, selectedData[1]);
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("",
								"Nowy tryb przeźroczystości będzie działał przy następnym uruchomieniu klienta gry.",
								NotificationType.CLIENT));
			}
		});

		StringBuilder toolTip = new StringBuilder("<html>Tryb przeźroczystości użyty dla grafiki. Dostępne opcje:<dl>");
		for (String[] optionData : data) {
			toolTip.append("<dt><b>");
			toolTip.append(optionData[0]);
			toolTip.append("</b></dt>");
			toolTip.append("<dd>");
			toolTip.append(optionData[2]);
			toolTip.append("</dd>");
		}
		toolTip.append("</dl></html>");
		selector.setToolTipText(toolTip.toString());

		return SettingsComponentFactory.createSettingsRow("Przezroczystość:", selector);
	}

	/**
	 * Create selector for choosing between defined and custom styles.
	 *
	 * @return layout for styles widgets
	 */
	private JComponent createDisplaySizeSelector() {
		final JComboBox<DisplaySizeOption> combo = new JComboBox<DisplaySizeOption>();
		final List<Dimension> sizes = stendhal.getAvailableDisplaySizes();
		int maxIndex = Math.max(0, sizes.size() - 1);
		int currentIndex = MathHelper.clamp(
				WtWindowManager.getInstance().getPropertyInt(DISPLAY_SIZE_PROPERTY, stendhal.getDisplaySizeIndex()), 0,
				maxIndex);
		for (int i = 0; i < sizes.size(); i++) {
			combo.addItem(new DisplaySizeOption(i, sizes.get(i)));
		}
		combo.setSelectedIndex(MathHelper.clamp(currentIndex, 0, combo.getItemCount() - 1));
		combo.setToolTipText("Określ szerokość i wysokość pola gry w pikselach.");
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DisplaySizeOption selected = (DisplaySizeOption) combo.getSelectedItem();
				if (selected != null) {
					WtWindowManager.getInstance().setProperty(DISPLAY_SIZE_PROPERTY,
							Integer.toString(selected.getIndex()));
				}
			}
		});
		return SettingsComponentFactory.createSettingsRow("Obszar gry:", combo);
	}

	/**
	 * Create the selector controlling the window that contains the game.
	 *
	 * @return row containing the client window mode selector
	 */
	private JComponent createWindowModeSelector() {
		final JComboBox<WindowMode> combo = new JComboBox<WindowMode>(WindowMode.values());
		combo.setSelectedItem(WindowModeController.getConfiguredMode());
		combo.setToolTipText("Wybierz okno, pełny ekran w oknie lub pełny ekran");
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowMode selected = (WindowMode) combo.getSelectedItem();
				if (selected != null) {
					Frame mainFrame = j2DClient.get().getMainFrame();
					if (mainFrame instanceof JFrame) {
						WindowModeController.select((JFrame) mainFrame, selected);
					} else {
						WindowModeController.select(selected);
					}
					ClientSingletonRepository.getUserInterface().addEventLine(new EventLine("",
							"Tryb wyświetlania klienta został zmieniony.",
							NotificationType.CLIENT));
				}
			}
		});
		return SettingsComponentFactory.createSettingsRow("Tryb wyświetlania:", combo);
	}

	private static final class DisplaySizeOption {
		private final int index;
		private final Dimension size;

		DisplaySizeOption(int index, Dimension size) {
			this.index = index;
			this.size = size;
		}

		int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return size.width + " × " + size.height;
		}
	}

	private JComponent createFpsSelector() {
		final JComboBox<Integer> combo = new JComboBox<Integer>();
		int configured = Math.max(1,
				WtWindowManager.getInstance().getPropertyInt(FPS_LIMIT_PROPERTY, stendhal.getFpsLimit()));
		boolean match = false;
		for (int option : FPS_OPTIONS) {
			combo.addItem(Integer.valueOf(option));
			if (option == configured) {
				match = true;
			}
		}
		if (!match) {
			combo.addItem(Integer.valueOf(configured));
		}
		combo.setSelectedItem(Integer.valueOf(configured));
		combo.setToolTipText("Wybierz maksymalną liczbę klatek na sekundę dla klienta.");
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Integer selected = (Integer) combo.getSelectedItem();
				if (selected != null) {
					WtWindowManager.getInstance().setProperty(FPS_LIMIT_PROPERTY,
							Integer.toString(selected.intValue()));
				}
			}
		});
		return SettingsComponentFactory.createSettingsRow("Limit klatek na sekundę:", combo);
	}

	private JComponent createStyleTypeSelector() {
		JComponent selector = createStyleSelector();
		selector.setToolTipText("Zmienia wygląd okien i przycisków, bez zmiany działania gry.");
		return SettingsComponentFactory.createSettingsRow("Wygląd klienta:", selector);
	}

	/**
	 * Create selector for choosing GUI rendering method.
	 *
	 * @return component
	 */
	private JComponent createRenderingSelector() {
		final JComboBox<RenderingMethod> selector = new JComboBox<>();
		selector.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((RenderingMethod) value).name, index, isSelected,
						cellHasFocus);
			}
		});

		final WtWindowManager wm = WtWindowManager.getInstance();
		String currentMethod = wm.getProperty(SettingsProperties.UI_RENDERING, "");

		// Fill with available methods
		for (UiRenderingMethod method : UiRenderingMethod.getAvailableMethods()) {
			RenderingMethod item = new RenderingMethod();
			item.method = method;
			switch (method) {
			case DEFAULT: {
				item.name = "Domyślny (system)";
				break;
			}
			case SOFTWARE: {
				if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
					item.name = "Windows API/GDI Renderowanie Systemowe";
				} else {
					item.name = "Renderowanie Systemowe";
				}
				break;
			}
			case DIRECT_DRAW: {
				item.name = "DirectDraw Only";
				break;
			}
			case DDRAW_HWSCALE: {
				item.name = "Skalowanie Direct3D HW";
				break;
			}
			case OPEN_GL: {
				item.name = "Open GL";
				break;
			}
			case XRENDER: {
				item.name = "XRender";
				break;
			}
			case METAL: {
				item.name = "Metal Framework";
				break;
			}
			}
			selector.addItem(item);
			if (method.getPropertyValue().equals(currentMethod))
				selector.setSelectedItem(item);
		}

		selector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RenderingMethod selected = (RenderingMethod) selector.getSelectedItem();
				wm.setProperty(SettingsProperties.UI_RENDERING, selected.method.getPropertyValue());
				ClientSingletonRepository.getUserInterface()
						.addEventLine(new EventLine("",
								"Nowe renderowanie zostanie użyte przy następnym uruchomieniu klienta gry.",
								NotificationType.CLIENT));
			}
		});

		selector.setToolTipText("Sposób rysowania okien i przycisków klienta gry.");
		return SettingsComponentFactory.createSettingsRow("Rysowanie interfejsu:", selector);
	}

	/**
	 * Create selector for the default font size.
	 *
	 * @return component containing the selector
	 */
	private JComponent createFontSizeSelector() {
		final JComboBox<Object> selector = new JComboBox<>();

		// Fill the selector, and set current size as the selection
		int current = WtWindowManager.getInstance().getPropertyInt(FONT_SIZE_PROPERTY, DEFAULT_FONT_SIZE);
		selector.addItem("domyślny (12)");
		for (int size = FONT_MIN_SIZE; size <= FONT_MAX_SIZE; size += 2) {
			Integer obj = size;
			selector.addItem(obj);
			if ((size == current) && (size != DEFAULT_FONT_SIZE)) {
				selector.setSelectedItem(obj);
			}
		}

		selector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selected = selector.getSelectedItem();
				if ("domyślny (12)".equals(selected)) {
					selected = "12";
				}
				WtWindowManager.getInstance().setProperty(FONT_SIZE_PROPERTY, selected.toString());

				LookAndFeel look = UIManager.getLookAndFeel();
				if (look instanceof StyledLookAndFeel) {
					int size = MathHelper.parseIntDefault(selected.toString(), DEFAULT_FONT_SIZE);
					((StyledLookAndFeel) look).setDefaultFontSize(size);
				}
			}
		});
		selector.setToolTipText("Rozmiar czcionki w oknach klienta.");
		return SettingsComponentFactory.createSettingsRow("Rozmiar czcionki:", selector);
	}

	/**
	 * Create selector for the font used in the quest log and achievements.
	 *
	 * @return component for specifying a font
	 */
	private JComponent createFontSelector() {
		int pad = SBoxLayout.COMMON_PADDING;
		JComponent fontBox = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		fontBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(pad, pad, pad, pad)));

		// There seems to be no good way to change the default background color
		// of all components. The color is needed for making the etched border.
		Style style = StyleUtil.getStyle();
		if (style != null) {
			fontBox.setBackground(style.getPlainColor());
		}

		JCheckBox fontToggle = new JCheckBox("Czcionka dekoracyjna");
		fontToggle.setToolTipText("Ustawia niestandardową czcionkę dla dziennika podróży i osiągnięć");
		fontBox.add(fontToggle);

		final JComboBox<String> fontList = new JComboBox<>();
		JComponent fontRow = SettingsComponentFactory.createSettingsRow("Czcionka:", fontList);
		final JLabel label = (JLabel) fontRow.getComponent(0);
		fontBox.add(fontRow, SLayout.EXPAND_X);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (String font : ge.getAvailableFontFamilyNames()) {
			fontList.addItem(font);
		}
		// Show the user what's in use at the moment
		String font = WtWindowManager.getInstance().getProperty(FONT_PROPERTY, DEFAULT_FONT);
		fontList.setSelectedItem(font);

		// Detect if the font property had been changed from the default.
		boolean changed = fontChanged();
		fontToggle.setSelected(changed);
		fontList.setEnabled(changed);
		label.setEnabled(changed);

		// Bind the toggle button to enabling and disabling the selector
		fontToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				if (enabled) {
					String selected = fontList.getSelectedItem().toString();
					WtWindowManager.getInstance().setProperty(FONT_PROPERTY, selected);
				} else {
					WtWindowManager.getInstance().setProperty(FONT_PROPERTY, DEFAULT_FONT);
				}
				fontList.setEnabled(enabled);
				label.setEnabled(enabled);
			}
		});

		// Bind changing the selection to changing the font. The selector is
		// enabled only when font changing is enabled, so this should be safe
		fontList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = fontList.getSelectedItem().toString();
				WtWindowManager.getInstance().setProperty(FONT_PROPERTY, selected);
			}
		});

		return fontBox;
	}

	/**
	 * Check if a custom font is in use.
	 *
	 * @return <code>true</code> if the user has changed the font from the default,
	 *         <code>false</code> otherwise
	 */
	private boolean fontChanged() {
		String currentSetting = WtWindowManager.getInstance().getProperty(FONT_PROPERTY, DEFAULT_FONT);
		return !currentSetting.equals(DEFAULT_FONT);
	}

	/*
	 * private String selectBGImage() { JFileChooser bgSelector = new
	 * JFileChooser(); bgSelector.setDialogType(JFileChooser.OPEN_DIALOG |
	 * JFileChooser.FILES_ONLY);
	 * bgSelector.setDialogTitle("Select an image to use for the client background"
	 * ); //bgSelector.createDialog(this.page);
	 * 
	 * // Returning null until I figure out how to use JFileChooser return null; }
	 */

	/**
	 * A cell renderer for combo boxes that can show different tooltips for the
	 * options. The implementation is currently dependent on the data being in the
	 * format used in {@link #createTransparencySelector()}, so if this is needed
	 * elsewhere it needs to be generalized first.
	 */
	private static class TooltippedRenderer extends DefaultListCellRenderer {
		private final String[][] data;

		TooltippedRenderer(String[][] data) {
			this.data = data;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if ((index > -1) && (value != null)) {
				list.setToolTipText("<html>" + data[index][2] + "</html>");
			}
			return comp;
		}
	}

	private static class RenderingMethod {
		UiRenderingMethod method;
		String name;
	}
}
