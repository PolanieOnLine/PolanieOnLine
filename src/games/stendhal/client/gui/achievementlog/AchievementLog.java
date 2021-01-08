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
import java.awt.Window;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.j2DClient;
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
	/** The enclosing window. */
	private JDialog window;
	private JComponent page;

	private AchievementLogRenderers renderer = AchievementLogRenderers.get();

	public static final int PAD = 5;

	AchievementLog(String name) {
		window = new JDialog(j2DClient.get().getMainFrame(), name);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, PAD));
		window.setResizable(false);

		page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PAD);
		window.add(page);

		AchievementLogComponents component = new AchievementLogComponents();
		page.add(component.getHeaderText(TABLE_WIDTH, PAD));
		page.add(component.getViewTable(getTable(), TABLE_WIDTH, TABLE_HEIGHT, PAD));
		page.add(component.getButtons(window));

		WindowUtils.closeOnEscape(window);
		WindowUtils.watchFontSize(window);
		WindowUtils.trackLocation(window, "achievement_log", false);
		window.pack();
	}

	private JComponent getTable() {
		final JTable table = createTable();

		table.setEnabled(false);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		TableColumn col = table.getColumnModel().getColumn(0);
		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(SwingConstants.CENTER);
		col.setCellRenderer(r);

		col = table.getColumnModel().getColumn(1);
		col.setCellRenderer(renderer.new SpriteCellRenderer());

		col = table.getColumnModel().getColumn(2);
		col.setCellRenderer(renderer.new DescriptionCellRenderer());

		HeaderRenderer hr = new HeaderRenderer();
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn c = cols.nextElement();
			c.setHeaderRenderer(hr);
		}

		AchievementLogAdjusts adjust = new AchievementLogAdjusts();
		adjust.columnWidths(table);
		adjust.rowHeights(table);

		return table;
	}

	private JTable createTable() {
		final String[] columnNames = { "#", "Ikona", "Opis" };

		List<String> achievements = AchievementLogController.get().getList();
		final Object[][] data = new Object[achievements.size()][];
		int i = 0;
		for (final String a: achievements) {
			data[i] = createDataRow(a.split(":"));
			i++;
		}

		return new JTable(data, columnNames);
	}

	private Object[] createDataRow(final String[] achievements) {
		final Object[] rval = new Object[3];

		final String category = achievements[0];
		final String title = achievements[1];
		final String desc = achievements[2];

		boolean reached = achievements[3].equals("true");

		rval[0] = "";
		rval[1] = getAchievementImage(category, reached);
		rval[2] = getAchievementDesc(title, desc);

		if (reached) {
			rval[0] = "✔";
		}

		return rval;
	}

	private String getAchievementDesc(String title, String desc) {
		String description = "<html><span style=\"font-weight: bold;\">" + title + "</span><br/>"
				+ "<span style=\"font-weight: normal; font-style: italic;\">" + desc + ".</span></html>";

		return description;
	}

	private Sprite getAchievementImage(String category, boolean reached) {
		String imagePath = "/data/sprites/achievements/" + category.toLowerCase() + ".png";

		Sprite sprite;
		if (reached) {
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
}