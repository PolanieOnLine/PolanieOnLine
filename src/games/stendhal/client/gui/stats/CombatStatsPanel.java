/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.stats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Point;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
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
import javax.swing.JLabel;
import javax.swing.SwingConstants;
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
	private final JPanel rows = new JPanel(new GridBagLayout());
	private final JScrollPane scroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private Map<String, String> values = Collections.emptyMap();
	private final InternalManagedWindow window = new InternalManagedWindow("combat_stats", "Statystyki");
	private Container windowHost;
	private final ComponentAdapter hostResize = new ComponentAdapter() {
		@Override public void componentResized(ComponentEvent event) {
			if (window.isVisible()) positionWindow();
		}
	};

	CombatStatsPanel() {
		super(new BorderLayout(0, 3));
		setOpaque(false);
		setBorder(null);
		rows.setOpaque(false);
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
		toggle.setMargin(new Insets(0, 4, 0, 4));
		toggle.addActionListener(event -> toggleWindow());
		add(toggle, BorderLayout.SOUTH);
		scroll.setBorder(null);
		scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
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
		Point point = SwingUtilities.convertPoint(this, getWidth(), toggle.getY(), host);
		// Let the scroll pane include its border and viewport in the content size.
		Dimension size = window.getPreferredSize();
		if (size.width > host.getWidth()) size.height += scroll.getHorizontalScrollBar().getPreferredSize().height;
		if (size.height > host.getHeight()) size.width += scroll.getVerticalScrollBar().getPreferredSize().width;
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
		rows.removeAll();
		int row = 0;
		DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(
				Locale.forLanguageTag(ClientLanguage.getLanguage())));
		for (CombatStat stat : CombatStat.values()) {
			Double value = number(values.get(stat.getKey()), stat.getNature() != null);
			if (value == null) continue;
			addCell(ClientLanguage.getText(stat.getLabel()) + ":", row, 0, SwingConstants.LEFT);
			addCell(format.format(value) + "%", row, 1, SwingConstants.RIGHT);
			Double limit = number(values.get(stat.getLimitKey()), false);
			if (showLimits && limit != null) addCell("/ " + format.format(limit) + "%", row, 2, SwingConstants.RIGHT);
			row++;
		}
		toggle.setText(ClientLanguage.getText("Statystyki"));
		window.setTitle("Statystyki");
		rows.revalidate();
		boolean available = row > 0 && WtWindowManager.getInstance().getPropertyBoolean(
				SettingsProperties.SHOW_COMBAT_STATS_PROPERTY, false);
		setVisible(available);
		if (!available) window.setVisible(false);
		else if (window.isVisible()) positionWindow();
		setToolTipText(ClientLanguage.getText("Aktualne wartości po uwzględnieniu limitów. Bonusy warunkowe zależą od bieżącej sytuacji w walce."));
		revalidate();
		repaint();
	}

	private void addCell(String text, int row, int column, int alignment) {
		JLabel label = new JLabel(text, alignment);
		label.setFont(rows.getFont());
		label.setForeground(rows.getForeground());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column; constraints.gridy = row;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = column == 0 ? 1.0 : 0.0;
		constraints.insets = new Insets(1, column == 2 ? 5 : 0, 1, column == 0 ? 12 : 0);
		rows.add(label, constraints);
	}

	private static Double number(String value, boolean allowNegative) {
		if (value == null) return null;
		try {
			double number = Double.parseDouble(value);
			return Double.isFinite(number) && (allowNegative || number >= 0.0) ? number : null;
		} catch (NumberFormatException ignored) { return null; }
	}
}
