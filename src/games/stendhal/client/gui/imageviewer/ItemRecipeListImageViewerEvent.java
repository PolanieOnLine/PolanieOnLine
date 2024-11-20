package games.stendhal.client.gui.imageviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import games.stendhal.client.GameScreen;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.textformat.StringFormatter;
import games.stendhal.client.gui.textformat.TextAttributeSet;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public final class ItemRecipeListImageViewerEvent extends ViewPanel {
	private static final int PAD = 5;
	private static final long serialVersionUID = -6114543463410539585L;
	/** Formatter for item name underlining. */
	private final StringFormatter<Map<TextAttribute, Object>, TextAttributeSet> formatter
		= new StringFormatter<Map<TextAttribute, Object>, TextAttributeSet>();
	/** Default attributes for the item name formatter (empty). */
	private final TextAttributeSet defaultAttrs = new TextAttributeSet();

	private final RPEvent event;

	/**
	 * Creates a new ItemRecipeListImageViewerEvent.
	 *
	 * @param event event
	 */
	public ItemRecipeListImageViewerEvent(RPEvent event) {
		this.event = event;

		TextAttributeSet set = new TextAttributeSet();
		set.setAttribute(TextAttribute.UNDERLINE, "u");
		formatter.addStyle('§', set);
	}

	/**
	 * Shows the window.
	 */
	public void view() {
		new ImageViewWindow(event.get("title"), this);
	}

	@Override
	public void prepareView(final Dimension maxSize) {
		Dimension screenSize = GameScreen.get().getSize();
		int maxPreferredWidth = screenSize.width - 80;
		if (event.has("caption")) {
			JLabel caption = new JLabel("<html><div width=" + (maxPreferredWidth
					- 10) + ">" + event.get("caption") + "</div></html>");
			caption.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
			add(caption, BorderLayout.NORTH);
		}

		JTable table = createTable(maxPreferredWidth);
		// Prevents selection
		table.setEnabled(false);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setCellRenderer(new SpriteCellRenderer());

		col = table.getColumnModel().getColumn(1);
		col.setCellRenderer(new MultiSpritesCellRenderer());

		HeaderRenderer hr = new HeaderRenderer();
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn c = cols.nextElement();
			c.setHeaderRenderer(hr);
		}

		adjustColumnWidths(table);
		adjustRowHeights(table);

		ScrolledViewport viewPort = new ScrolledViewport(table);
		/*
		 * maxPreferredWidth is incorrect, but java does not seem to support
		 * max-width property for div's, so all the cells report the same
		 * preferred width anyway.
		 */
		viewPort.getComponent().setPreferredSize(new Dimension(maxPreferredWidth,
				Math.min(screenSize.height - 100, table.getPreferredSize().height
						+ hr.getPreferredSize().height + 4 * PAD)));
		viewPort.getComponent().setBackground(table.getBackground());
		add(viewPort.getComponent(), BorderLayout.CENTER);

		setVisible(true);
	}

	/**
	 * Adjust the column widths of a table based on the table contents.
	 *
	 * @param table adjusted table
	 */
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

	/**
	 * Adjust the row heights of a table based on the table contents.
	 *
	 * @param table adjusted table
	 */
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

	/**
	 * Renderer used for the header row items.
	 */
	public static class HeaderRenderer extends JPanel implements TableCellRenderer {
		private final JLabel header = new JLabel();

		/**
		 * Create a new HeaderRenderer.
		 */
		public HeaderRenderer() {
			add(header);
			header.setBorder(null);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			header.setText(value.toString());
			return this;
		}
	}

	/**
	 * Renderer used for the item description cells.
	 */
	private static class DescriptionCellRenderer extends DefaultTableCellRenderer {
		private final Border border = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			setBorder(border);
			return this;
		}
	}

	/**
	 * Renderer for the item sprite cells.
	 */
	private static class SpriteCellRenderer extends JComponent implements TableCellRenderer {
		private Sprite sprite;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color,
				boolean isSelected, boolean hasFocus, int row, int col) {
			Object obj = table.getValueAt(row, col);
			if (obj instanceof Sprite) {
				sprite = (Sprite) obj;
			} else {
				sprite = null;
			}
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = new Dimension();
			if (sprite != null) {
				d.width = sprite.getWidth() + 2 * PAD;
				d.height = sprite.getHeight() + 2 * PAD;
			}
			return d;
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (sprite != null) {
				sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
			}
		}
	}

	private static class MultiSpritesCellRenderer extends JComponent implements TableCellRenderer {
		private Sprite sprite;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color,
				boolean isSelected, boolean hasFocus, int row, int col) {
			Object obj = table.getValueAt(row, col);
			if (obj instanceof Sprite) {
				sprite = (Sprite) obj;
			} else {
				sprite = null;
			}
			return this;
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (sprite != null) {
				sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
			}
		}
	}

	/**
	 * Create a JTable based on the event contents.
	 *
	 * @param maxPreferredWidth maximum preferred width of the entire table
	 * @return new table
	 */
	private JTable createTable(int maxPreferredWidth) {
		String[] columnNames = { "Przedmiot", "Receptura" };
		Object[][] data = new Object[event.getSlot("content").size()][];

		RPSlot slot = event.getSlot("content");
		RPSlot recipeSlot = event.getSlot("content_recipe");

		int i = 0;
		for (RPObject item : slot) {
			List<RPObject> recipeItemsList = new ArrayList<>();
			for (RPObject recipeItem : recipeSlot) {
	            recipeItemsList.add(recipeItem);
	        }

			data[i] = createDataRow(item, recipeItemsList, maxPreferredWidth);
	        i++;
	    }
	    return new JTable(data, columnNames);
	}

	/**
	 * Create a table data row from item data.
	 *
	 * @param item item to use for the row contents
	 * @param maxPreferredWidth maximum preferred width of the entire table
	 * @return table content row
	 */
	private Object[] createDataRow(RPObject item, List<RPObject> recipeItems, int maxPreferredWidth) {
		Object[] rval = new Object[2];
		rval[0] = getItemSprite(item);
		//rval[1] = getItemSpritePanel(recipeItems);

		return rval;
	}

	/**
	 * Get a sprite for an item.
	 *
	 * @param item item to fetch sprite for
	 * @return item sprite
	 */
	private Sprite getItemSprite(RPObject item) {
		String itemSubclass = item.get("subclass");
		String itemName = item.get("class") + "/" + itemSubclass;
		String imagePath = "/data/sprites/items/" + itemName + ".png";

		Sprite sprite = SpriteStore.get().getSprite(imagePath);
		if (sprite.getWidth() > sprite.getHeight()) {
			sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
		}
		return sprite;
	}
}
