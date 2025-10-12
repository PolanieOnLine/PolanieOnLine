/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.achievementlog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.achievementlog.AchievementLogComponents.FilterPanel;
import games.stendhal.client.gui.achievementlog.AchievementLogComponents.SummaryPanel;
import games.stendhal.client.gui.achievementlog.AchievementLogComponents.StatusFilter;
import games.stendhal.client.gui.achievementlog.AchievementLogRenderers.HeaderRenderer;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * @author KarajuSs
 */
class AchievementLog {
	private static final int TABLE_WIDTH = 720;
	private static final int TABLE_HEIGHT = 500;
	static final Color COMPLETED_BACKGROUND = new Color(0xF1, 0xF8, 0xE9);
	static final Color LOCKED_BACKGROUND = new Color(0xF9, 0xF9, 0xF9);
	static final String COMPLETED_ACCENT_HEX = "#2e7d32";
	static final String LOCKED_ACCENT_HEX = "#b71c1c";
	/** The enclosing window. */
	private JDialog window;
	private JComponent page;

	private final AchievementLogComponents component = new AchievementLogComponents();
	private final AchievementLogAdjusts adjust = new AchievementLogAdjusts();
	private AchievementLogRenderers renderer = AchievementLogRenderers.get();

	private final List<AchievementEntry> achievements;
	private AchievementTableModel tableModel;
	private JTable table;
	private SummaryPanel summaryPanel;

	public static final int PAD = 5;

	AchievementLog(String name) {
		window = new JDialog(j2DClient.get().getMainFrame(), name);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, PAD));
		window.setResizable(false);

		page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PAD);
		window.add(page);

		achievements = parseEntries(AchievementLogController.get().getList());

		summaryPanel = component.getSummaryPanel(TABLE_WIDTH, PAD);
		page.add(summaryPanel);

		table = createTable();

		FilterPanel filterPanel = component.getFilterPanel(getCategories(achievements), TABLE_WIDTH, PAD);
		filterPanel.setListener(new FilterPanel.Listener() {
			@Override
			public void onFilterChanged(String category, StatusFilter status) {
				applyFilter(category, status);
			}
		});
		page.add(filterPanel);

		page.add(component.getViewTable(table, TABLE_WIDTH, TABLE_HEIGHT, PAD));
		page.add(component.getButtons(window));

		WindowUtils.closeOnEscape(window);
		WindowUtils.watchFontSize(window);
		WindowUtils.trackLocation(window, "achievement_log", false);
		window.pack();
	}

	private JTable createTable() {
		tableModel = new AchievementTableModel(achievements);
		final JTable achievementTable = new JTable(tableModel);

		achievementTable.setEnabled(false);
		achievementTable.setFillsViewportHeight(true);
		achievementTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		achievementTable.setRowSelectionAllowed(false);
		achievementTable.setIntercellSpacing(new Dimension(0, 10));
		achievementTable.setShowGrid(false);
		achievementTable.setBackground(Color.WHITE);
		achievementTable.getTableHeader().setReorderingAllowed(false);

		TableColumn col = achievementTable.getColumnModel().getColumn(0);
		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(SwingConstants.CENTER);
		col.setCellRenderer(r);

		col = achievementTable.getColumnModel().getColumn(1);
		col.setCellRenderer(renderer.new SpriteCellRenderer());

		col = achievementTable.getColumnModel().getColumn(2);
		col.setCellRenderer(renderer.new DescriptionCellRenderer());

		HeaderRenderer hr = new HeaderRenderer();
		Enumeration<TableColumn> cols = achievementTable.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn c = cols.nextElement();
			c.setHeaderRenderer(hr);
		}

		return achievementTable;
	}

	private void applyTableAdjustments() {
		adjust.columnWidths(table);
		adjust.rowHeights(table);
	}

	private void applyFilter(String category, StatusFilter status) {
		tableModel.applyFilter(category, status);
		summaryPanel.update(achievements, tableModel.getVisibleEntries());
		applyTableAdjustments();
		table.revalidate();
		table.repaint();
	}

	private List<AchievementEntry> parseEntries(List<String> rawEntries) {
		List<AchievementEntry> list = new ArrayList<>();
		for (String entry : rawEntries) {
			String[] parts = entry.split(":");
			if (parts.length < 4) {
				continue;
			}
			boolean reached = Boolean.parseBoolean(parts[3]);
			list.add(new AchievementEntry(parts[0].trim(), parts[1].trim(), parts[2].trim(), reached));
		}
		return list;
	}

	private List<String> getCategories(List<AchievementEntry> entries) {
		Set<String> categories = new LinkedHashSet<>();
		for (AchievementEntry entry : entries) {
			categories.add(entry.category);
		}
		return new ArrayList<>(categories);
	}

	private String getAchievementDesc(AchievementEntry entry) {
		String category = escapeHtml(entry.category);
		String title = escapeHtml(entry.title);
		String desc = escapeHtml(entry.description);
		String statusColor = entry.reached ? COMPLETED_ACCENT_HEX : LOCKED_ACCENT_HEX;
		String statusText = entry.reached ? "Zdobyte osiągnięcie" : "Do zdobycia";

		StringBuilder builder = new StringBuilder();
		builder.append("<html><div style=\"padding:4px 0;\">");
		builder.append("<div style=\"font-size:11px; letter-spacing:0.06em; text-transform:uppercase; color:#757575;\">");
		builder.append(category);
		builder.append("</div>");
		builder.append("<div style=\"font-weight:bold; font-size:14px; color:#212121; margin-top:2px;\">");
		builder.append(title);
		builder.append("</div>");
		builder.append("<div style=\"font-size:12px; color:#444444; margin-top:4px;\">");
		builder.append(desc);
		builder.append("</div>");
		builder.append("<div style=\"margin-top:6px; font-size:11px; font-weight:bold; color:");
		builder.append(statusColor);
		builder.append(";\">");
		builder.append(statusText);
		builder.append("</div></div></html>");

		return builder.toString();
	}

	private String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	private Sprite getAchievementImage(AchievementEntry entry) {
		String imagePath = "/data/sprites/achievements/" + entry.category.toLowerCase() + ".png";

		Sprite sprite;
		if (entry.reached) {
			sprite = SpriteStore.get().getSprite(imagePath);
		} else {
			sprite = SpriteStore.get().getColoredSprite(imagePath, Color.LIGHT_GRAY);
		}

		if (sprite.getWidth() > sprite.getHeight()) {
			sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
		}

		return sprite;
	}

	Window getWindow() {
		return window;
	}

	private class AchievementTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final List<AchievementEntry> source;
		private List<AchievementEntry> visible;

		AchievementTableModel(List<AchievementEntry> entries) {
			source = new ArrayList<>(entries);
			visible = new ArrayList<>(entries);
		}

		@Override
		public int getRowCount() {
			return visible.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "#";
			case 1:
				return "Ikona";
			case 2:
				return "Opis";
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 1) {
				return Sprite.class;
			}
			return Object.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			AchievementEntry entry = visible.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return entry.reached ? "✔" : "";
			case 1:
				return getAchievementImage(entry);
			case 2:
				return getAchievementDesc(entry);
			default:
				return null;
			}
		}

		void applyFilter(String category, StatusFilter status) {
			visible = new ArrayList<>();
			for (AchievementEntry entry : source) {
				if (category != null && !category.equals(entry.category)) {
					continue;
				}
				if (status == StatusFilter.COMPLETED && !entry.reached) {
					continue;
				}
				if (status == StatusFilter.LOCKED && entry.reached) {
					continue;
				}
				visible.add(entry);
			}
			fireTableDataChanged();
		}

		List<AchievementEntry> getVisibleEntries() {
			return new ArrayList<>(visible);
		}
	}

	static class AchievementEntry {
		final String category;
		final String title;
		final String description;
		final boolean reached;

		AchievementEntry(String category, String title, String description, boolean reached) {
			this.category = category;
			this.title = title;
			this.description = description;
			this.reached = reached;
		}
	}
}
