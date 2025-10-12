/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.shop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.grammar.Grammar;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public final class NpcShopWindowManager {
	private static final Logger logger = Logger.getLogger(NpcShopWindowManager.class);

	private static final NpcShopWindowManager INSTANCE = new NpcShopWindowManager();

	private final Map<String, NpcShopWindow> openWindows = new HashMap<String, NpcShopWindow>();

	private NpcShopWindowManager() {
	}

	public static NpcShopWindowManager get() {
		return INSTANCE;
	}

	public void handle(final RPEvent event) {
		if (event == null) {
			return;
		}

		if (!event.has("npc") || !event.has("action")) {
			logger.warn("Ignoring NPC shop event without required attributes: " + event);
			return;
		}

		final String npc = event.get("npc");
		final String action = event.get("action");

		if ("open".equals(action)) {
			openWindow(npc, event);
		} else if ("close".equals(action)) {
			closeWindow(npc);
		} else {
			logger.warn("Unknown NPC shop action '" + action + "' for NPC " + npc);
		}
	}

	private void openWindow(final String npc, final RPEvent event) {
		NpcShopWindow window = openWindows.get(npc);
		if (window == null) {
			window = new NpcShopWindow(npc);
			final NpcShopWindow trackedWindow = window;
			window.setOnClose(new Runnable() {
				@Override
				public void run() {
					openWindows.remove(npc, trackedWindow);
				}
			});
			openWindows.put(npc, window);
		}

		final String title = event.has("title") ? event.get("title") : npc + " - Sklep";
		window.setTitleText(title);
		window.setBackgroundTexture(event.has("background") ? event.get("background") : null);

		window.setOffers(parseOffers(event));
		window.setVisible(true);
		window.toFront();
	}

	private void closeWindow(final String npc) {
		final NpcShopWindow window = openWindows.remove(npc);
		if (window != null) {
			window.disposeFromManager();
		}
	}

	private List<Offer> parseOffers(final RPEvent event) {
		final List<Offer> offers = new ArrayList<Offer>();
		if (!event.hasSlot("offers")) {
			return offers;
		}

		try {
			final RPSlot slot = event.getSlot("offers");
			for (final RPObject object : slot) {
				try {
					offers.add(parseOffer(object));
				} catch (final RuntimeException e) {
					logger.warn("Unable to parse shop offer: " + object, e);
				}
			}
		} catch (final RuntimeException e) {
			logger.error("Failed to parse NPC shop offers", e);
		}

		return offers;
	}

	private Offer parseOffer(final RPObject object) {
		final int price = object.has("price") ? object.getInt("price") : 0;
		final String commandKey = object.has("shop_item_key") ? object.get("shop_item_key") : object.get("name");
		final String displayName = object.has("name") ? object.get("name") : commandKey;
		final String description = object.has("description_info") ? object.get("description_info") : "";
		final String flavor = object.has("shop_flavor") ? object.get("shop_flavor") : "";
		final Sprite sprite = loadSprite(object);

		return new Offer(commandKey, displayName, description, flavor, price, sprite);
	}

	private Sprite loadSprite(final RPObject object) {
		try {
			final String clazz = object.has("class") ? object.get("class") : null;
			final String subclass = object.has("subclass") ? object.get("subclass") : null;
			if ((clazz == null) || (subclass == null)) {
				return SpriteStore.get().getFailsafe();
			}

			final String path = "/data/sprites/items/" + clazz + "/" + subclass + ".png";
			return SpriteStore.get().getSprite(path);
		} catch (final RuntimeException e) {
			logger.warn("Falling back to failsafe sprite for NPC shop item", e);
			return SpriteStore.get().getFailsafe();
		}
	}

	private static String formatPriceColored(final int copper) {
		final int dukaty = copper / 10000;
		final int afterDukaty = copper % 10000;
		final int talary = afterDukaty / 100;
		final int miedziaki = afterDukaty % 100;

		final StringBuilder builder = new StringBuilder("<html>");
		boolean appended = false;

		appended = appendPricePart(builder, dukaty, "#f5c542", "dukat", appended);
		appended = appendPricePart(builder, talary, "#d0d0d0", "talar", appended);
		appended = appendPricePart(builder, miedziaki, "#d87f33", "miedziak", appended || (dukaty > 0) || (talary > 0));

		if (!appended) {
			appendPricePart(builder, 0, "#d87f33", "miedziak", false);
		}

		builder.append("</html>");
		return builder.toString();
	}

	private static boolean appendPricePart(final StringBuilder builder, final int amount, final String color, final String coinName, final boolean appended) {
		if (amount <= 0) {
			return appended;
		}

		if (builder.length() > "<html>".length()) {
			builder.append(' ');
		}
		builder.append("<span style=\"color:");
		builder.append(color);
		builder.append("; font-weight:bold;\">");
		builder.append(amount);
		builder.append(' ');
		builder.append(Grammar.polishCoinName(coinName, amount));
		builder.append("</span>");
		return true;
	}

	private static String formatPricePlain(final int copper) {
		final int dukaty = copper / 10000;
		final int afterDukaty = copper % 10000;
		final int talary = afterDukaty / 100;
		final int miedziaki = afterDukaty % 100;

		final StringBuilder builder = new StringBuilder();
		boolean appended = false;

		appended = appendPlainPart(builder, dukaty, "dukat", appended);
		appended = appendPlainPart(builder, talary, "talar", appended);
		appended = appendPlainPart(builder, miedziaki, "miedziak", appended);

		if (!appended) {
			builder.append('0');
			builder.append(' ');
			builder.append(Grammar.polishCoinName("miedziak", 0));
		}

		builder.append(" (");
		builder.append(copper);
		builder.append(" miedziaków)");
		return builder.toString();
	}

	private static boolean appendPlainPart(final StringBuilder builder, final int amount, final String coinName, final boolean appended) {
		if (amount <= 0) {
			return appended;
		}
		if (appended) {
			builder.append(", ");
		}
		builder.append(amount);
		builder.append(' ');
		builder.append(Grammar.polishCoinName(coinName, amount));
		return true;
	}

	private static String escapeHtml(final String value) {
		if (value == null) {
			return "";
		}

		final StringBuilder builder = new StringBuilder(value.length());
		for (final char ch : value.toCharArray()) {
			switch (ch) {
			case '&':
				builder.append("&amp;");
				break;
			case '<':
				builder.append("&lt;");
				break;
			case '>':
				builder.append("&gt;");
				break;
			case '\"':
				builder.append("&quot;");
				break;
			default:
				builder.append(ch);
				break;
			}
		}
		return builder.toString();
	}

	private static final class NpcShopWindow extends JFrame {
		private static final long serialVersionUID = 1L;

		private final List<Offer> allOffers = new ArrayList<Offer>();
		private final OfferTableModel tableModel = new OfferTableModel();
		private final JTable table = new JTable(tableModel);
		private final JTextArea descriptionArea = new JTextArea();
		private final JLabel flavorLabel = new JLabel();
		private final JLabel unitPriceValue = new JLabel("-");
		private final JLabel totalPriceValue = new JLabel("-");
		private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1000), Integer.valueOf(1)));
		private final JButton buyButton = new JButton("Kup");
		private final JButton sellButton = new JButton("Sprzedaj");
		private final BackgroundPanel backgroundPanel = new BackgroundPanel();

		private Runnable onClose;
		private boolean disposingFromManager;

		NpcShopWindow(final String npc) {
			super(npc + " - Sklep");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setMinimumSize(new Dimension(560, 460));
			setLocationByPlatform(true);

			backgroundPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			backgroundPanel.setLayout(new BorderLayout(12, 12));
			setContentPane(backgroundPanel);

			final JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
			headerPanel.setOpaque(false);

			final JLabel titleLabel = new JLabel(getTitle());
			final Font baseFont = titleLabel.getFont();
			titleLabel.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, baseFont.getSize() + 2.0f));
			headerPanel.add(titleLabel, BorderLayout.CENTER);


			backgroundPanel.add(headerPanel, BorderLayout.NORTH);

			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setRowHeight(40);
			table.setFillsViewportHeight(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			table.setOpaque(false);
			table.setDefaultRenderer(Sprite.class, new SpriteCellRenderer());
			final DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer();
			nameRenderer.setOpaque(false);
			table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
			final PriceRenderer priceRenderer = new PriceRenderer();
			table.getColumnModel().getColumn(2).setCellRenderer(priceRenderer);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(final ListSelectionEvent event) {
					if (!event.getValueIsAdjusting()) {
						updateSelection();
					}
				}
			});

			final JScrollPane tableScroll = new JScrollPane(table);
			tableScroll.setOpaque(false);
			tableScroll.getViewport().setOpaque(false);
			tableScroll.setBorder(BorderFactory.createTitledBorder("Przedmioty"));
			configureTableColumns(table.getColumnModel());
			backgroundPanel.add(tableScroll, BorderLayout.CENTER);

			descriptionArea.setEditable(false);
			descriptionArea.setLineWrap(true);
			descriptionArea.setWrapStyleWord(true);
			descriptionArea.setOpaque(false);
			descriptionArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			final JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
			descriptionScroll.setBorder(BorderFactory.createTitledBorder("Opis przedmiotu"));
			descriptionScroll.setOpaque(false);
			descriptionScroll.getViewport().setOpaque(false);

			flavorLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
			flavorLabel.setForeground(new Color(230, 218, 184));

			final JPanel infoPanel = new JPanel();
			infoPanel.setOpaque(false);
			infoPanel.setLayout(new BorderLayout(8, 8));
			infoPanel.add(descriptionScroll, BorderLayout.CENTER);
			infoPanel.add(flavorLabel, BorderLayout.SOUTH);

			final JPanel totalsPanel = new JPanel();
			totalsPanel.setOpaque(false);
			totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));

			final JLabel unitLabel = new JLabel("Cena za sztukę:");
			unitLabel.setAlignmentX(LEFT_ALIGNMENT);
			unitPriceValue.setAlignmentX(LEFT_ALIGNMENT);
			final JLabel totalLabel = new JLabel("Łącznie:");
			totalLabel.setAlignmentX(LEFT_ALIGNMENT);
			totalPriceValue.setAlignmentX(LEFT_ALIGNMENT);

			totalsPanel.add(unitLabel);
			totalsPanel.add(unitPriceValue);
			totalsPanel.add(Box.createVerticalStrut(6));
			totalsPanel.add(totalLabel);
			totalsPanel.add(totalPriceValue);

			final JPanel actionPanel = new JPanel();
			actionPanel.setOpaque(false);
			actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
			actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			actionPanel.add(new JLabel("Ilość:"));
			actionPanel.add(Box.createHorizontalStrut(6));
			actionPanel.add(quantitySpinner);
			actionPanel.add(Box.createHorizontalStrut(12));
			actionPanel.add(buyButton);
			actionPanel.add(Box.createHorizontalStrut(6));
			actionPanel.add(sellButton);

			final JPanel bottomPanel = new JPanel();
			bottomPanel.setOpaque(false);
			bottomPanel.setLayout(new BorderLayout(12, 12));
			bottomPanel.add(infoPanel, BorderLayout.CENTER);
			bottomPanel.add(totalsPanel, BorderLayout.EAST);
			bottomPanel.add(actionPanel, BorderLayout.SOUTH);

			backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

			quantitySpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent event) {
					updateTotalPrice();
				}
			});

			buyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					sendCommand(true);
				}
			});

			sellButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					sendCommand(false);
				}
			});

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(final WindowEvent event) {
					if (!disposingFromManager && (onClose != null)) {
						onClose.run();
					}
				}
			});

			updateSelection();
		}

		void setOffers(final List<Offer> offers) {
			final String selected = getSelectedOfferKey();
			allOffers.clear();
			if (offers != null) {
				allOffers.addAll(offers);
			}

			tableModel.setOffers(allOffers);

			int preferredIndex = -1;
			if ((selected != null) && !selected.isEmpty()) {
				preferredIndex = tableModel.indexOf(selected);
			}

			if (preferredIndex >= 0) {
				table.getSelectionModel().setSelectionInterval(preferredIndex, preferredIndex);
			} else if (tableModel.getRowCount() > 0) {
				table.getSelectionModel().setSelectionInterval(0, 0);
			} else {
				table.clearSelection();
				updateSelection();
			}
		}

		private void configureTableColumns(final TableColumnModel model) {
			final TableColumn iconColumn = model.getColumn(0);
			iconColumn.setMinWidth(48);
			iconColumn.setMaxWidth(64);
			iconColumn.setPreferredWidth(56);

			final TableColumn nameColumn = model.getColumn(1);
			nameColumn.setPreferredWidth(240);

			final TableColumn priceColumn = model.getColumn(2);
			priceColumn.setPreferredWidth(140);
		}

		private void updateSelection() {
			final Offer offer = getSelectedOffer();

			if (offer == null) {
				descriptionArea.setText("");
				flavorLabel.setText("");
				unitPriceValue.setText("-");
				unitPriceValue.setToolTipText(null);
				totalPriceValue.setText("-");
				totalPriceValue.setToolTipText(null);
				buyButton.setEnabled(false);
				sellButton.setEnabled(false);
				return;
			}

			descriptionArea.setText(offer.description);
			descriptionArea.setCaretPosition(0);
			if ((offer.flavor != null) && !offer.flavor.isEmpty()) {
				flavorLabel.setText("<html><i>" + escapeHtml(offer.flavor) + "</i></html>");
			} else {
				flavorLabel.setText("");
			}

			unitPriceValue.setText(formatPriceColored(offer.price));
			unitPriceValue.setToolTipText(formatPricePlain(offer.price));
			buyButton.setEnabled(true);
			sellButton.setEnabled(true);

			if (((Integer) quantitySpinner.getValue()).intValue() < 1) {
				quantitySpinner.setValue(Integer.valueOf(1));
			}

			updateTotalPrice();
		}

		private void updateTotalPrice() {
			final Offer offer = getSelectedOffer();
			if (offer == null) {
				totalPriceValue.setText("-");
				totalPriceValue.setToolTipText(null);
				return;
			}

			final int quantity = ((Integer) quantitySpinner.getValue()).intValue();
			final long total = (long) offer.price * quantity;
			final int capped = total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;

			totalPriceValue.setText(formatPriceColored(capped));
			totalPriceValue.setToolTipText(formatPricePlain(capped));
		}

		private Offer getSelectedOffer() {
			final int selectedRow = table.getSelectedRow();
			if (selectedRow < 0) {
				return null;
			}
			return tableModel.getOffer(table.convertRowIndexToModel(selectedRow));
		}

		private String getSelectedOfferKey() {
			final Offer offer = getSelectedOffer();
			return offer != null ? offer.commandKey : null;
		}

		private void sendCommand(final boolean buy) {
			final Offer offer = getSelectedOffer();
			if (offer == null) {
				return;
			}

			final int quantity = ((Integer) quantitySpinner.getValue()).intValue();
			final StringBuilder builder = new StringBuilder();
			builder.append(buy ? "kup " : "sprzedaj ");
			builder.append(quantity);
			builder.append(' ');
			builder.append(offer.commandKey);

			ChatLineParser.parseAndHandle(builder.toString());
		}

		void setOnClose(final Runnable onClose) {
			this.onClose = onClose;
		}

		void disposeFromManager() {
			disposingFromManager = true;
			dispose();
		}

		void setTitleText(final String title) {
			setTitle(title);
		}

		void setBackgroundTexture(final String path) {
			backgroundPanel.setBackgroundTexture(path);
			backgroundPanel.repaint();
		}
	}

	private static final class OfferTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private static final String[] COLUMNS = new String[] {"", "Przedmiot", "Cena"};

		private final List<Offer> offers = new ArrayList<Offer>();

		void setOffers(final List<Offer> newOffers) {
			offers.clear();
			if (newOffers != null) {
				offers.addAll(newOffers);
			}
			fireTableDataChanged();
		}

		Offer getOffer(final int row) {
			if ((row < 0) || (row >= offers.size())) {
				return null;
			}
			return offers.get(row);
		}

		int indexOf(final String commandKey) {
			if (commandKey == null) {
				return -1;
			}
			for (int i = 0; i < offers.size(); i++) {
				if (commandKey.equals(offers.get(i).commandKey)) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getRowCount() {
			return offers.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(final int column) {
			return COLUMNS[column];
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			if (columnIndex == 0) {
				return Sprite.class;
			}
			return String.class;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			final Offer offer = offers.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return offer.sprite;
			case 1:
				return offer.displayName;
			case 2:
				return formatPriceColored(offer.price);
			default:
				return null;
			}
		}
	}

	private static final class PriceRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		PriceRenderer() {
			setHorizontalAlignment(SwingConstants.RIGHT);
			setOpaque(false);
		}

		@Override
		protected void setValue(final Object value) {
			setText(value == null ? "" : value.toString());
		}
	}

	private static final class SpriteCellRenderer extends JComponent implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		private Sprite sprite;
		private boolean selected;
		private Color selectionColor;

		@Override
		public JComponent getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			sprite = value instanceof Sprite ? (Sprite) value : null;
			selected = isSelected;
			selectionColor = table.getSelectionBackground();
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(48, 48);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			if (selected) {
				g.setColor(selectionColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			if (sprite != null) {
				final int x = (getWidth() - sprite.getWidth()) / 2;
				final int y = (getHeight() - sprite.getHeight()) / 2;
				sprite.draw(g, x, y);
			}
		}
	}


	private static final class BackgroundPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private transient BufferedImage backgroundImage;

		void setBackgroundTexture(final String path) {
			if ((path == null) || path.trim().isEmpty()) {
				backgroundImage = null;
				return;
			}

			try {
				final Sprite sprite = SpriteStore.get().getSprite(path);
				if (sprite == null) {
					backgroundImage = null;
					return;
				}
				backgroundImage = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
				sprite.draw(backgroundImage.getGraphics(), 0, 0);
			} catch (final RuntimeException e) {
				logger.warn("Failed to load NPC shop background texture: " + path, e);
				backgroundImage = null;
			}
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			if (backgroundImage == null) {
				return;
			}

			final int width = backgroundImage.getWidth();
			final int height = backgroundImage.getHeight();

			for (int x = 0; x < getWidth(); x += width) {
				for (int y = 0; y < getHeight(); y += height) {
					g.drawImage(backgroundImage, x, y, null);
				}
			}
		}
	}

	private static final class Offer {
		private final String commandKey;
		private final String displayName;
		private final String description;
		private final String flavor;
		private final int price;
		private final Sprite sprite;

		Offer(final String commandKey, final String displayName, final String description, final String flavor, final int price, final Sprite sprite) {
			this.commandKey = commandKey;
			this.displayName = displayName;
			this.description = description != null ? description : "";
			this.flavor = flavor != null ? flavor : "";
			this.price = price;
			this.sprite = sprite != null ? sprite : SpriteStore.get().getFailsafe();
		}
	}


}
