/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.stats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.settings.SettingsProperties;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.CombatStat;

/** Displays server-calculated percentages; settings never alter combat values. */
@SuppressWarnings("serial")
final class CombatStatsPanel extends JPanel {
	private final JToggleButton toggle = new JToggleButton();
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
	private Map<String, String> values = Collections.emptyMap();

	CombatStatsPanel() {
		super(new BorderLayout(0, 3));
		setOpaque(false);
		rows.setEditable(false);
		rows.setFocusable(false);
		rows.setOpaque(false);
		rows.setLineWrap(true);
		rows.setWrapStyleWord(true);
		rows.setFont(UIManager.getFont("Label.font"));
		Style style = StyleUtil.getStyle();
		if (style != null) {
			rows.setFont(style.getFont());
			rows.setForeground(style.getForeground());
		}
		UiTranslation.preserve(rows);
		UiTranslation.preserve(toggle);
		putClientProperty(UiTranslation.AFTER_TRANSLATION, (Runnable) this::render);
		toggle.addActionListener(event -> render());
		add(toggle, BorderLayout.NORTH);
		add(rows, BorderLayout.CENTER);
		WtWindowManager.getInstance().registerSettingChangeListener(SettingsProperties.SHOW_STAT_LIMITS_PROPERTY,
				value -> { if (SwingUtilities.isEventDispatchThread()) render();
					else SwingUtilities.invokeLater(this::render); });
		render();
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
		rows.setText(text.toString());
		rows.setVisible(toggle.isSelected() && text.length() > 0);
		setVisible(text.length() > 0);
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
