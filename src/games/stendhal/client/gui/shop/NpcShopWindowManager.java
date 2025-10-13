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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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

import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.InternalWindow;
import games.stendhal.client.gui.InternalWindow.CloseListener;
import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.client.gui.j2DClient;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public final class NpcShopWindowManager {
	private static final Logger logger = Logger.getLogger(NpcShopWindowManager.class);

	private static final NpcShopWindowManager INSTANCE = new NpcShopWindowManager();

	private static final String MODE_BUY = "buy";
	private static final String MODE_SELL = "sell";
	private static final String MODE_BOTH = "buy_sell";
	private static final String OFFER_TYPE_BUY = "buy";
	private static final String OFFER_TYPE_SELL = "sell";
	private static final String ATTR_MODE = "shop_mode";
	private static final String ATTR_OFFER_TYPE = "shop_offer_type";

	private enum TransactionType {
		BUY,
		SELL
	}

	private enum ShopMode {
		BUY(true, false),
		SELL(false, true),
		BOTH(true, true);

		private final boolean canBuy;
		private final boolean canSell;

		ShopMode(final boolean canBuy, final boolean canSell) {
			this.canBuy = canBuy;
			this.canSell = canSell;
		}

		boolean allows(final TransactionType type) {
			if (type == TransactionType.BUY) {
				return canBuy;
			}
			if (type == TransactionType.SELL) {
				return canSell;
			}
			return false;
		}

		boolean allowsBuy() {
			return canBuy;
		}

		boolean allowsSell() {
			return canSell;
		}
	}

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
			j2DClient.get().addWindow(window);
		}

		final String title = event.has("title") ? event.get("title") : npc + " - Sklep";
		window.setTitleText(title);
		window.setBackgroundTexture(event.has("background") ? event.get("background") : null);

		window.setOffers(parseOffers(event));
		window.setShopMode(parseShopMode(event));
		window.setMinimized(false);
		window.setVisible(true);
	}

	private void closeWindow(final String npc) {
		final NpcShopWindow window = openWindows.remove(npc);
		if (window != null) {
			window.disposeFromManager();
		}
	}

	private ShopMode parseShopMode(final RPEvent event) {
		if (!event.has(ATTR_MODE)) {
			return ShopMode.BUY;
		}

		final String mode = event.get(ATTR_MODE);
		if (mode == null) {
			return ShopMode.BUY;
		}
		if (MODE_BOTH.equalsIgnoreCase(mode)) {
			return ShopMode.BOTH;
		}
		if (MODE_SELL.equalsIgnoreCase(mode)) {
			return ShopMode.BUY;
		}
		if (MODE_BUY.equalsIgnoreCase(mode)) {
			return ShopMode.SELL;
		}
		if ("trade".equalsIgnoreCase(mode)) {
			return ShopMode.BUY;
		}
		return ShopMode.BUY;
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
		final String rawType = object.has(ATTR_OFFER_TYPE) ? object.get(ATTR_OFFER_TYPE) : null;
		final TransactionType type;
		if (OFFER_TYPE_SELL.equalsIgnoreCase(rawType)) {
			type = TransactionType.BUY;
		} else if (OFFER_TYPE_BUY.equalsIgnoreCase(rawType)) {
			type = TransactionType.SELL;
		} else {
			type = TransactionType.BUY;
		}

		return new Offer(commandKey, displayName, description, flavor, price, sprite, type);
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

private static final class NpcShopWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;

	private static final Color PRIMARY_TEXT = new Color(246, 236, 220);
	private static final Color SECONDARY_TEXT = new Color(220, 204, 184);
	private static final Color MUTED_TEXT = new Color(202, 182, 158);
	private static final Color SELECTION_BG = new Color(123, 88, 55, 200);
	private static final Color SELECTION_FG = new Color(255, 249, 240);
	private static final Color GRID_COLOR = new Color(104, 80, 58, 160);
	private static final Color HEADER_BG = new Color(90, 62, 38, 230);
	private static final Color HEADER_FG = new Color(255, 246, 232);
	private static final Color PANEL_TINT = new Color(34, 24, 16, 215);
	private static final Color BORDER_COLOR = new Color(117, 89, 63, 210);
	private static final Color BUTTON_BG = new Color(198, 156, 94);
	private static final Color BUTTON_BORDER = new Color(128, 94, 54);

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
	private final JPanel actionPanel = new JPanel();
	private final BackgroundPanel backgroundPanel = new BackgroundPanel();
	private final TintedPanel contentPanel = new TintedPanel();
	private final JLabel itemsLabel = createSectionLabel("Przedmioty");
	private final JLabel descriptionLabel = createSectionLabel("Opis przedmiotu");
	private final JLabel totalsHeading = createSectionLabel("Podsumowanie");
	private ShopMode shopMode = ShopMode.BUY;

	private Runnable onClose;
	private boolean disposingFromManager;

	NpcShopWindow(final String npc) {
		super(buildHandle(npc), npc + " - Sklep");
		setCloseable(true);
		setHideOnClose(true);
		setMinimizable(true);
		setMovable(true);
		setPreferredSize(new Dimension(640, 520));

		backgroundPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		backgroundPanel.setLayout(new BorderLayout());
		backgroundPanel.setOpaque(false);

		contentPanel.setTint(PANEL_TINT);
		contentPanel.setLayout(new BorderLayout(16, 16));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		backgroundPanel.add(contentPanel, BorderLayout.CENTER);
		setContent(backgroundPanel);

		configureTable();
		final JScrollPane tableScroll = createTableScroll();

		final JPanel tableSection = new JPanel(new BorderLayout(0, 8));
		tableSection.setOpaque(false);
		itemsLabel.setLabelFor(table);
		tableSection.add(itemsLabel, BorderLayout.NORTH);
		tableSection.add(tableScroll, BorderLayout.CENTER);

		final JPanel centerWrapper = new JPanel(new BorderLayout(0, 12));
		centerWrapper.setOpaque(false);
		centerWrapper.add(tableSection, BorderLayout.CENTER);
		centerWrapper.add(createSeparator(), BorderLayout.SOUTH);

		contentPanel.add(centerWrapper, BorderLayout.CENTER);

		final JScrollPane descriptionScroll = configureDescription();
		final JPanel infoPanel = new JPanel(new BorderLayout(0, 8));
		infoPanel.setOpaque(false);
		descriptionLabel.setLabelFor(descriptionArea);
		infoPanel.add(descriptionLabel, BorderLayout.NORTH);
		infoPanel.add(descriptionScroll, BorderLayout.CENTER);
		infoPanel.add(flavorLabel, BorderLayout.SOUTH);

		final JPanel totalsPanel = createTotalsPanel();

		final JPanel infoRow = new JPanel(new BorderLayout(12, 0));
		infoRow.setOpaque(false);
		infoRow.add(infoPanel, BorderLayout.CENTER);
		infoRow.add(totalsPanel, BorderLayout.EAST);

		configureActionPanel();
		final JPanel actionRow = new JPanel();
		actionRow.setOpaque(false);
		actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
		actionRow.add(Box.createHorizontalGlue());
		actionRow.add(actionPanel);

		final JPanel bottomWrapper = new JPanel(new BorderLayout(0, 12));
		bottomWrapper.setOpaque(false);
		bottomWrapper.add(infoRow, BorderLayout.CENTER);
		bottomWrapper.add(actionRow, BorderLayout.SOUTH);

		contentPanel.add(bottomWrapper, BorderLayout.SOUTH);

		quantitySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent event) {
				updateTotalPrice();
			}
		});

		buyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				initiateTransaction(TransactionType.BUY);
			}
		});

		sellButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				initiateTransaction(TransactionType.SELL);
			}
		});

		addCloseListener(new CloseListener() {
			@Override
			public void windowClosed(final InternalWindow window) {
				if (!disposingFromManager && (onClose != null)) {
					onClose.run();
				}
			}
		});

		updateSelection();
	}

	private static String buildHandle(final String npc) {
		String base = (npc == null) ? "npc" : npc.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("_+", "_");
		base = base.replaceAll("^_", "").replaceAll("_$", "");
		if (base.isEmpty()) {
			base = "npc";
		}
		return "npcshop." + base;
	}

	private void configureTable() {
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(44);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setOpaque(false);
		table.setForeground(PRIMARY_TEXT);
		table.setGridColor(GRID_COLOR);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		table.setSelectionBackground(SELECTION_BG);
		table.setSelectionForeground(SELECTION_FG);
		table.setDefaultRenderer(Sprite.class, new SpriteCellRenderer());
		final DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer();
		nameRenderer.setOpaque(false);
		nameRenderer.setForeground(PRIMARY_TEXT);
		table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
		final PriceRenderer priceRenderer = new PriceRenderer();
		priceRenderer.setForeground(PRIMARY_TEXT);
		table.getColumnModel().getColumn(2).setCellRenderer(priceRenderer);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					updateSelection();
				}
			}
		});
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setForeground(HEADER_FG);
		table.getTableHeader().setBackground(HEADER_BG);
	}

	private JScrollPane createTableScroll() {
		final JScrollPane tableScroll = new JScrollPane(table);
		tableScroll.setOpaque(false);
		tableScroll.getViewport().setOpaque(false);
		tableScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		configureTableColumns(table.getColumnModel());
		return tableScroll;
	}

	private JScrollPane configureDescription() {
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setOpaque(false);
		descriptionArea.setForeground(PRIMARY_TEXT);
		descriptionArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		flavorLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
		flavorLabel.setForeground(MUTED_TEXT);
		flavorLabel.setVerticalAlignment(SwingConstants.TOP);

		final JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true), BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		descriptionScroll.setOpaque(false);
		descriptionScroll.getViewport().setOpaque(false);
		return descriptionScroll;
	}

	private JPanel createTotalsPanel() {
		final JPanel totalsPanel = new JPanel();
		totalsPanel.setOpaque(false);
		totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
		totalsHeading.setAlignmentX(LEFT_ALIGNMENT);
		totalsPanel.add(totalsHeading);
		totalsPanel.add(Box.createVerticalStrut(4));

		final JLabel unitLabel = new JLabel("Cena za sztukę:");
		unitLabel.setAlignmentX(LEFT_ALIGNMENT);
		unitLabel.setForeground(SECONDARY_TEXT);
		unitPriceValue.setAlignmentX(LEFT_ALIGNMENT);
		unitPriceValue.setForeground(PRIMARY_TEXT);

		final JLabel totalLabel = new JLabel("Łącznie:");
		totalLabel.setAlignmentX(LEFT_ALIGNMENT);
		totalLabel.setForeground(SECONDARY_TEXT);
		totalPriceValue.setAlignmentX(LEFT_ALIGNMENT);
		totalPriceValue.setForeground(PRIMARY_TEXT);

		totalsPanel.add(unitLabel);
		totalsPanel.add(unitPriceValue);
		totalsPanel.add(Box.createVerticalStrut(8));
		totalsPanel.add(totalLabel);
		totalsPanel.add(totalPriceValue);

		return totalsPanel;
	}

	private void configureActionPanel() {
		actionPanel.setOpaque(false);
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.setBorder(BorderFactory.createEmptyBorder());

		final JLabel quantityLabel = new JLabel("Ilość:");
		quantityLabel.setForeground(SECONDARY_TEXT);
		actionPanel.add(quantityLabel);
		actionPanel.add(Box.createHorizontalStrut(8));
		quantitySpinner.setMaximumSize(new Dimension(80, quantitySpinner.getPreferredSize().height));
		if (quantitySpinner.getEditor() instanceof JSpinner.DefaultEditor) {
			final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) quantitySpinner.getEditor();
			editor.getTextField().setColumns(3);
			editor.getTextField().setBackground(new Color(255, 248, 236));
			editor.getTextField().setForeground(new Color(30, 20, 12));
		}
		actionPanel.add(quantitySpinner);
		actionPanel.add(Box.createHorizontalStrut(16));

		styleButton(buyButton);
		actionPanel.add(buyButton);
		actionPanel.add(Box.createHorizontalStrut(8));
		styleButton(sellButton);
		actionPanel.add(sellButton);
	}

	private void styleButton(final JButton button) {
		button.setBackground(BUTTON_BG);
		button.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER));
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.setForeground(new Color(30, 20, 12));
		button.setFont(button.getFont().deriveFont(Font.BOLD));
	}

	private JSeparator createSeparator() {
		final JSeparator separator = new JSeparator();
		separator.setForeground(BORDER_COLOR);
		separator.setOpaque(false);
		return separator;
	}

	private JLabel createSectionLabel(final String text) {
		final JLabel label = new JLabel(text);
		final Font baseFont = label.getFont();
		label.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, baseFont.getSize() + 1.0f));
		label.setForeground(PRIMARY_TEXT);
		return label;
	}

	void setOffers(final List<Offer> offers) {
		final String selectedId = getSelectedOfferId();
		allOffers.clear();
		if (offers != null) {
			allOffers.addAll(offers);
		}

		tableModel.setOffers(allOffers);

		int preferredIndex = -1;
		if ((selectedId != null) && !selectedId.isEmpty()) {
			preferredIndex = tableModel.indexOf(selectedId);
			if ((preferredIndex >= 0) && !isRowCompatibleWithMode(preferredIndex)) {
				preferredIndex = -1;
			}
		}

		if (preferredIndex < 0) {
			preferredIndex = findFirstRowForMode();
		}

		if (preferredIndex >= 0) {
			table.getSelectionModel().setSelectionInterval(preferredIndex, preferredIndex);
		} else {
			table.clearSelection();
		}

		updateSelection();
	}

	void setShopMode(final ShopMode mode) {
		shopMode = (mode != null) ? mode : ShopMode.BUY;
		buyButton.setVisible(shopMode.allowsBuy());
		sellButton.setVisible(shopMode.allowsSell());
		actionPanel.revalidate();
		actionPanel.repaint();
		ensureSelectionForMode();
		updateSelection();
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
		buyButton.setEnabled(isActionAvailable(TransactionType.BUY, offer));
		sellButton.setEnabled(isActionAvailable(TransactionType.SELL, offer));

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

	private String getSelectedOfferId() {
		final Offer offer = getSelectedOffer();
		return (offer != null) ? offer.selectionId : null;
	}

	private boolean isRowCompatibleWithMode(final int row) {
		final Offer candidate = tableModel.getOffer(row);
		return (candidate != null) && shopMode.allows(candidate.type);
	}

	private int findFirstRowForMode() {
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (isRowCompatibleWithMode(i)) {
				return i;
			}
		}
		return -1;
	}

	private void ensureSelectionForMode() {
		final Offer offer = getSelectedOffer();
		if ((offer != null) && shopMode.allows(offer.type)) {
			return;
		}
		final int row = findFirstRowForMode();
		if (row >= 0) {
			table.getSelectionModel().setSelectionInterval(row, row);
		} else {
			table.clearSelection();
		}
	}

	private boolean isActionAvailable(final TransactionType type, final Offer offer) {
		return (offer != null) && shopMode.allows(type) && (offer.type == type);
	}

	private void initiateTransaction(final TransactionType type) {
		final Offer offer = getSelectedOffer();
		if (!isActionAvailable(type, offer)) {
			return;
		}

		int quantity = ((Integer) quantitySpinner.getValue()).intValue();
		if (quantity < 1) {
			quantity = 1;
			quantitySpinner.setValue(Integer.valueOf(1));
		}

		final long total = (long) offer.price * quantity;
		final int capped = total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;

		ChatLineParser.parseAndHandle(buildCommand(type, quantity, offer.commandKey));

		final String actionWord = (type == TransactionType.BUY) ? "zakup" : "sprzedaż";
		final String dialogTitle = (type == TransactionType.BUY) ? "Potwierdź zakup" : "Potwierdź sprzedaż";
		final String message = new StringBuilder()
		.append("Czy potwierdzasz ")
		.append(actionWord)
		.append(' ')
		.append(quantity)
		.append(" × ")
		.append(offer.displayName)
		.append(" za ")
		.append(formatPricePlain(capped))
		.append('?')
		.toString();

		final int result = JOptionPane.showConfirmDialog(this, message, dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			ChatLineParser.parseAndHandle("tak");
		} else {
			ChatLineParser.parseAndHandle("nie");
		}
	}

	private String buildCommand(final TransactionType type, final int quantity, final String commandKey) {
		final StringBuilder builder = new StringBuilder();
		builder.append(type == TransactionType.BUY ? "kup " : "sprzedaj ");
		builder.append(quantity);
		builder.append(' ');
		builder.append(commandKey);
		return builder.toString();
	}

	void setOnClose(final Runnable onClose) {
		this.onClose = onClose;
	}

	void disposeFromManager() {
		disposingFromManager = true;
		close();
		disposingFromManager = false;
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

		int indexOf(final String selectionId) {
			if ((selectionId == null) || selectionId.isEmpty()) {
				return -1;
			}
			for (int i = 0; i < offers.size(); i++) {
				final Offer offer = offers.get(i);
				if (selectionId.equals(offer.selectionId)) {
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
			final Offer offer = getOffer(rowIndex);
			if (offer == null) {
				return null;
			}
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

	private static final class TintedPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Color tint = new Color(0, 0, 0, 160);

	TintedPanel() {
		setOpaque(false);
	}

	void setTint(final Color tint) {
		if (tint != null) {
			this.tint = tint;
			repaint();
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if ((tint == null) || (tint.getAlpha() == 0)) {
			return;
		}

		final Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(tint);
		g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
		g2.dispose();
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
		private final TransactionType type;
		private final String selectionId;

		Offer(final String commandKey, final String displayName, final String description, final String flavor, final int price, final Sprite sprite, final TransactionType type) {
			this.commandKey = commandKey;
			this.displayName = displayName;
			this.description = (description != null) ? description : "";
			this.flavor = (flavor != null) ? flavor : "";
			this.price = price;
			this.sprite = (sprite != null) ? sprite : SpriteStore.get().getFailsafe();
			this.type = (type != null) ? type : TransactionType.BUY;
			this.selectionId = this.commandKey + ":" + this.type.name();
		}
	}
}
