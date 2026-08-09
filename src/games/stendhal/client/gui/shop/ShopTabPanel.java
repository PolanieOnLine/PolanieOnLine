package games.stendhal.client.gui.shop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.common.grammar.Grammar;

class ShopTabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int MAX_STACK_AMOUNT = 999;

	enum ShopAction {
		BUY,
		SELL
	}

	private final ShopAction action;
	private final ShopTableModel model;
	private final JTable table;
	private final TableRowSorter<ShopTableModel> sorter;
	private final JCheckBox weaponFilter;
	private final JCheckBox elixirFilter;
	private final JSpinner amountSpinner;
	private final JButton actionButton;
	private final JTextArea descriptionArea;

	ShopTabPanel(ShopAction action, List<ShopItem> items) {
		super(new BorderLayout(8, 8));
		this.action = action;
		setOpaque(false);

		model = new ShopTableModel(items);
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(36);
		table.getTableHeader().setReorderingAllowed(false);

		sorter = new TableRowSorter<ShopTableModel>(model);
		sorter.toggleSortOrder(1);
		table.setRowSorter(sorter);

		TableColumn iconColumn = table.getColumnModel().getColumn(0);
		iconColumn.setMaxWidth(64);
		iconColumn.setMinWidth(48);
		iconColumn.setPreferredWidth(48);
		iconColumn.setCellRenderer(new SpriteCellRenderer());

		TableColumn nameColumn = table.getColumnModel().getColumn(1);
		nameColumn.setPreferredWidth(200);

		TableColumn priceColumn = table.getColumnModel().getColumn(2);
		priceColumn.setPreferredWidth(140);
		priceColumn.setCellRenderer(new PriceCellRenderer());

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateSelection();
				}
			}
		});

		JScrollPane tableScroll = new JScrollPane(table);
		tableScroll.setOpaque(false);
		tableScroll.getViewport().setOpaque(false);

		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setEditable(false);
		descriptionArea.setOpaque(false);
		descriptionArea.setBorder(BorderFactory.createTitledBorder("Opis"));
		descriptionArea.setRows(4);

		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setOpaque(false);
		descriptionScroll.getViewport().setOpaque(false);
		descriptionScroll.setBorder(BorderFactory.createEmptyBorder());

		JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
		centerPanel.setOpaque(false);
		centerPanel.add(tableScroll, BorderLayout.CENTER);
		centerPanel.add(descriptionScroll, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);

		weaponFilter = new JCheckBox("Pokaż tylko broń");
		weaponFilter.setOpaque(false);
		weaponFilter.addActionListener(e -> applyFilters());

		elixirFilter = new JCheckBox("Pokaż eliksiry");
		elixirFilter.setOpaque(false);
		elixirFilter.addActionListener(e -> applyFilters());

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
		filterPanel.setOpaque(false);
		filterPanel.add(weaponFilter);
		filterPanel.add(elixirFilter);
		add(filterPanel, BorderLayout.NORTH);

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, MAX_STACK_AMOUNT, 1);
		amountSpinner = new JSpinner(spinnerModel);
		JComponent spinnerEditor = amountSpinner.getEditor();
		if (spinnerEditor instanceof JSpinner.DefaultEditor) {
			((JSpinner.DefaultEditor) spinnerEditor).getTextField().setColumns(4);
		}

		actionButton = new JButton(action == ShopAction.BUY ? "Kup" : "Sprzedaj");
		actionButton.addActionListener(e -> performAction());

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		actionPanel.setOpaque(false);
		JLabel amountLabel = new JLabel("Ilość:");
		actionPanel.add(amountLabel);
		actionPanel.add(amountSpinner);
		actionPanel.add(actionButton);
		add(actionPanel, BorderLayout.SOUTH);

		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		} else {
			updateSelection();
		}
	}

	void updateItems(List<ShopItem> items) {
		model.setItems(items);
		applyFilters();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		} else {
			updateSelection();
		}
	}

	boolean hasItems() {
		return !model.isEmpty();
	}

	private void applyFilters() {
		if (!weaponFilter.isSelected() && !elixirFilter.isSelected()) {
			sorter.setRowFilter(null);
		} else {
			sorter.setRowFilter(new RowFilter<ShopTableModel, Integer>() {
				@Override
				public boolean include(Entry<? extends ShopTableModel, ? extends Integer> entry) {
					ShopItem item = model.getItemAt(entry.getIdentifier());
					if (item == null) {
							return false;
					}
					boolean showWeapons = weaponFilter.isSelected();
					boolean showElixirs = elixirFilter.isSelected();
					if (showWeapons && showElixirs) {
							return (item.getCategory() == ShopItem.Category.WEAPON)
								|| (item.getCategory() == ShopItem.Category.ELIXIR);
					}
					if (showWeapons) {
							return item.getCategory() == ShopItem.Category.WEAPON;
					}
					if (showElixirs) {
							return item.getCategory() == ShopItem.Category.ELIXIR;
					}
					return true;
				}
			});
		}
		if (table.getRowCount() > 0 && table.getSelectedRow() < 0) {
			table.setRowSelectionInterval(0, 0);
		}
		updateSelection();
	}

	private void updateSelection() {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			actionButton.setEnabled(false);
			amountSpinner.setEnabled(false);
			descriptionArea.setText("Brak dostępnych przedmiotów.");
			return;
		}

		int modelRow = table.convertRowIndexToModel(viewRow);
		ShopItem item = model.getItemAt(modelRow);
		if (item == null) {
			actionButton.setEnabled(false);
			amountSpinner.setEnabled(false);
			descriptionArea.setText("Brak dostępnych przedmiotów.");
			return;
		}

		StringBuilder text = new StringBuilder();
		if ((item.getDescription() != null) && !item.getDescription().isEmpty()) {
			text.append(item.getDescription());
		}
		if ((item.getFlavorText() != null) && !item.getFlavorText().isEmpty()) {
			if (text.length() > 0) {
				text.append("\n\n");
			}
			text.append(item.getFlavorText());
		}
		descriptionArea.setText(text.toString());
		descriptionArea.setCaretPosition(0);

		SpinnerNumberModel spinnerModel = (SpinnerNumberModel) amountSpinner.getModel();
		spinnerModel.setValue(Integer.valueOf(1));
		if (item.isStackable()) {
			amountSpinner.setEnabled(true);
			spinnerModel.setMaximum(Integer.valueOf(MAX_STACK_AMOUNT));
		} else {
			amountSpinner.setEnabled(false);
			spinnerModel.setMaximum(Integer.valueOf(1));
			spinnerModel.setValue(Integer.valueOf(1));
		}

		actionButton.setEnabled(true);
	}

	private void performAction() {
		ShopItem item = getSelectedItem();
		if (item == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int amount = ((Number) amountSpinner.getValue()).intValue();
		if (!item.isStackable()) {
			amount = 1;
		}

		StringBuilder command = new StringBuilder();
		if (action == ShopAction.BUY) {
			command.append("kup ");
		} else {
			command.append("sprzedaj ");
		}

		if (amount > 1) {
			command.append(amount).append(' ');
		}
		command.append(item.getName());

		boolean accepted = ChatLineParser.parseAndHandle(command.toString());
		if (!accepted) {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private ShopItem getSelectedItem() {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			return null;
		}
		int modelRow = table.convertRowIndexToModel(viewRow);
		return model.getItemAt(modelRow);
	}

	private static class SpriteCellRenderer extends JComponent implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private Sprite sprite;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof ShopItem) {
				sprite = ((ShopItem) value).getSprite();
			} else {
				sprite = null;
			}
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			if (sprite != null) {
				return new Dimension(sprite.getWidth() + 8, sprite.getHeight() + 8);
			}
			return new Dimension(36, 36);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (sprite != null) {
				int x = (getWidth() - sprite.getWidth()) / 2;
				int y = (getHeight() - sprite.getHeight()) / 2;
				sprite.draw(g, x, y);
			}
		}
	}

	private static class PriceCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		PriceCellRenderer() {
			setHorizontalAlignment(SwingConstants.LEFT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof Integer) {
				setText(formatPrice(((Integer) value).intValue()));
			} else {
				setText("");
			}
			return component;
		}

		private String formatPrice(int copper) {
			int dukaty = copper / 10000;
			int remaining = copper % 10000;
			int talary = remaining / 100;
			int miedziaki = remaining % 100;

			StringBuilder builder = new StringBuilder("<html>");
			boolean appended = false;
			appended = appendPart(builder, dukaty, "dukat", "#f4d742", appended);
			appended = appendPart(builder, talary, "talar", "#d0d0d0", appended);
			appended = appendPart(builder, miedziaki, "miedziak", "#d4843d", appended);
			if (!appended) {
				builder.append("<span style=\\"color:#d4843d\\">0 ")
					.append(Grammar.polishCoinName("miedziak", 0))
					.append("</span>");
			}
			builder.append("</html>");
			return builder.toString();
		}

		private boolean appendPart(StringBuilder builder, int amount, String coin, String color, boolean appended) {
			if (amount <= 0) {
				return appended;
			}
			if (appended) {
				builder.append(" ");
			}
			builder.append("<span style=\\"color:")
				.append(color)
				.append("">")
				.append(amount)
				.append(' ')
				.append(Grammar.polishCoinName(coin, amount))
				.append("</span>");
			return true;
		}
	}
}
