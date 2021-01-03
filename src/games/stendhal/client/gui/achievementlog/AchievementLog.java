package games.stendhal.client.gui.achievementlog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

class AchievementLog {
	/** The enclosing window. */
	private JDialog window;

	public static final int PAD = 5;

	AchievementLog(String name) {
		window = new JDialog(j2DClient.get().getMainFrame(), name);

		final JTable table = createTable();

		table.setEnabled(false);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		TableColumn col = table.getColumnModel().getColumn(0);
		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(SwingConstants.CENTER);
		col.setCellRenderer(r);

		col = table.getColumnModel().getColumn(1);
		col.setCellRenderer(new SpriteCellRenderer());

		col = table.getColumnModel().getColumn(2);
		col.setCellRenderer(new DescriptionCellRenderer());

		HeaderRenderer hr = new HeaderRenderer();
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn c = cols.nextElement();
			c.setHeaderRenderer(hr);
		}

		adjustColumnWidths(table);
		adjustRowHeights(table);

		JScrollPane scrollPane = new JScrollPane(table);

		WindowUtils.closeOnEscape(window);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.add(scrollPane);
		window.pack();
		WindowUtils.watchFontSize(window);
		WindowUtils.trackLocation(window, "achievement_log", true);
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

		final Boolean reached = achievements[3].equals("true");

		rval[0] = "";
		rval[1] = getAchievementImage(category, reached);
		rval[2] = getAchievementDesc(title, desc);

		if (reached) {
			rval[0] = "✔";
		}

		return rval;
	}

	private void adjustColumnWidths(JTable table) {
		TableColumnModel model = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			TableColumn tc = model.getColumn(column);
			int width = tc.getWidth();
			for (int row = 0; row < table.getRowCount(); row++) {
				Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			tc.setPreferredWidth(width);
		}
	}
	private void adjustRowHeights(JTable table) {
		for (int row = 0; row < table.getRowCount(); row++) {
			int rowHeight = table.getRowHeight();

			for (int column = 0; column < table.getColumnCount(); column++) {
				Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			table.setRowHeight(row, rowHeight);
		}
	}

	private String getAchievementDesc(String title, String desc) {
		String description = "<html><span style=\"font-weight: bold;\">" + title + "</span><br/>"
				+ "<span style=\"font-weight: normal; font-style: italic;\">" + desc + ".</span></html>";

		return description;
	}

	private Sprite getAchievementImage(String category, boolean reached) {
		String imagePath = "/data/sprites/achievements/" + category + ".png";

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
