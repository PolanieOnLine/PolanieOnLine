/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.stats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.settings.SettingsProperties;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.CombatStat;

/** Displays server-calculated percentages; settings never alter combat values. */
@SuppressWarnings("serial")
final class CombatStatsPanel extends JPanel {
	private final JButton toggle = new JButton();
	private final JTextArea rows = new JTextArea() {
		@Override public void setBounds(int x, int y, int width, int height) {
			boolean widthChanged = width != getWidth();
			super.setBounds(x, y, width, height);
			// Wrapped height is known only after layout has assigned the new width.
			if (widthChanged) SwingUtilities.invokeLater(this::revalidate);
		}

		@Override public Dimension getMinimumSize() {
			Dimension size = super.getMinimumSize();
			size.width = getInsets().left + getInsets().right;
			return size;
		}
	};
	private final JScrollPane scroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private Map<String, String> values = Collections.emptyMap();
	private final InternalManagedWindow window = new InternalManagedWindow("combat_stats", "Statystyki procentowe");
	private Container windowHost;
	private final ComponentAdapter hostResize = new ComponentAdapter() {
		@Override public void componentResized(ComponentEvent event) {
			if (window.isVisible()) positionWindow();
		}
	};

	CombatStatsPanel() {
		super(new BorderLayout(0, 3));
		setOpaque(false);
		rows.setEditable(false);
		rows.setFocusable(false);
		rows.setOpaque(false);
		rows.setLineWrap(true);
		rows.setWrapStyleWord(true);
		rows.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		rows.setFont(UIManager.getFont("Label.font"));
		Style style = StyleUtil.getStyle();
		if (style != null) {
			rows.setFont(style.getFont());
			rows.setForeground(style.getForeground());
		}
		UiTranslation.preserve(rows);
		UiTranslation.preserve(toggle);
		putClientProperty(UiTranslation.AFTER_TRANSLATION, (Runnable) this::render);
		toggle.setFocusable(false);
		toggle.addActionListener(event -> toggleWindow());
		add(toggle, BorderLayout.NORTH);
		scroll.setBorder(null);
		scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
		scroll.setPreferredSize(new Dimension(330, 300));
		window.setContent(scroll);
		window.setMinimizable(false);
		window.setMovable(false);
		window.setHideOnClose(true);
		window.setVisible(false);
		for (String property : new String[] {SettingsProperties.SHOW_STAT_LIMITS_PROPERTY, SettingsProperties.SHOW_COMBAT_STATS_PROPERTY}) {
			WtWindowManager.getInstance().registerSettingChangeListener(property,
					value -> { if (SwingUtilities.isEventDispatchThread()) render();
						else SwingUtilities.invokeLater(this::render); });
		}
		render();
	}

	private void toggleWindow() {
		if (window.isVisible()) { window.setVisible(false); return; }
		if (!isVisible()) return;
		if (window.getParent() == null) {
			if (j2DClient.get() == null) return;
			j2DClient.get().addWindow(window);
		}
		if (window.getParent() == null) return;
		if (windowHost != window.getParent()) {
			if (windowHost != null) windowHost.removeComponentListener(hostResize);
			windowHost = window.getParent(); windowHost.addComponentListener(hostResize);
		}
		positionWindow();
		window.setVisible(true);
		window.raise();
	}

	private void positionWindow() {
		Container host = window.getParent();
		if (host == null) return;
		Point point = SwingUtilities.convertPoint(this, getWidth(), 0, host);
		Dimension size = window.getPreferredSize();
		size.width = Math.min(size.width, host.getWidth());
		size.height = Math.min(size.height, host.getHeight());
		int x = Math.max(0, Math.min(point.x, host.getWidth() - size.width));
		int y = Math.max(0, Math.min(point.y, host.getHeight() - size.height));
		window.setBounds(x, y, size.width, size.height);
	}

	void setSnapshot(Map<String, String> snapshot) {
		if (values.equals(snapshot)) return;
		values = new LinkedHashMap<String, String>(snapshot);
		render();
	}

	private void render() {
		boolean showLimits = WtWindowManager.getInstance().getPropertyBoolean(
				SettingsProperties.SHOW_STAT_LIMITS_PROPERTY, false);
		StringBuilder text = new StringBuilder();
		DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(
				"en".equals(ClientLanguage.getLanguage()) ? Locale.ENGLISH : new Locale("pl", "PL")));
		for (CombatStat stat : CombatStat.values()) {
			Double value = number(values.get(stat.getKey()));
			if (value == null) continue;
			if (text.length() > 0) text.append('\n');
			text.append(ClientLanguage.getText(stat.getLabel())).append(": ").append(format.format(value)).append('%');
			Double limit = number(values.get(stat.getLimitKey()));
			// Keep the value and its limit together when a long label wraps in a narrow sidebar.
			if (showLimits && limit != null) text.append("\u00a0/\u00a0").append(format.format(limit)).append('%');
		}
		toggle.setText(ClientLanguage.getText("Statystyki procentowe"));
		window.setTitle("Statystyki procentowe");
		rows.setText(text.toString());
		scroll.setPreferredSize(new Dimension(330, Math.min(300, Math.max(32, rows.getPreferredSize().height))));
		boolean available = text.length() > 0 && WtWindowManager.getInstance().getPropertyBoolean(
				SettingsProperties.SHOW_COMBAT_STATS_PROPERTY, false);
		setVisible(available);
		if (!available) window.setVisible(false);
		else if (window.isVisible()) positionWindow();
		setToolTipText(ClientLanguage.getText("Aktualne wartości po uwzględnieniu limitów. Bonusy warunkowe zależą od bieżącej sytuacji w walce."));
		revalidate();
		repaint();
	}

	private static Double number(String value) {
		if (value == null) return null;
		try {
			double number = Double.parseDouble(value);
			return Double.isFinite(number) && number >= 0.0 ? number : null;
		} catch (NumberFormatException ignored) { return null; }
	}
}
