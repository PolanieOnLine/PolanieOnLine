/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;

/** Category navigation and persistent pages; switching pages never rebuilds controls. */
@SuppressWarnings("serial")
class SettingsPages extends JPanel {

	enum Category {
		CONTROLS("Sterowanie"), DISPLAY("Obraz"), APPEARANCE("Wygląd"),
		TEXT("Tekst i powiadomienia"), COMBAT("Walka i przedmioty"), AUDIO("Dźwięk");

		private final String label;
		Category(String label) { this.label = label; }
		String getLabel() { return label; }
	}

	private final Map<Category, JComponent> pages = new EnumMap<>(Category.class);
	private final Map<Category, JButton> buttons = new EnumMap<>(Category.class);
	private final Map<Category, Border> borders = new EnumMap<>(Category.class);
	private final Map<Category, Map<String, JComponent>> groups = new EnumMap<>(Category.class);
	private final JPanel cards = new JPanel(new CardLayout());
	private Category selected = Category.CONTROLS;

	SettingsPages() {
		super(new BorderLayout(12, 0));
		int pad = SBoxLayout.COMMON_PADDING;
		setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		cards.setBorder(BorderFactory.createEmptyBorder());
		JComponent navigation = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		navigation.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, pad));
		for (Category category : Category.values()) {
			JButton button = new JButton(category.getLabel());
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.addActionListener(event -> showCategory(category));
			buttons.put(category, button);
			borders.put(category, button.getBorder());
			navigation.add(button, SLayout.EXPAND_X);
			bindNavigation(button, category, KeyEvent.VK_UP, -1);
			bindNavigation(button, category, KeyEvent.VK_DOWN, 1);

			JComponent page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
			page.setBorder(BorderFactory.createEmptyBorder(0, 0, pad, pad));
			pages.put(category, page);
			groups.put(category, new java.util.LinkedHashMap<String, JComponent>());
			JScrollPane scroll = new JScrollPane(page) {
				@Override
				public Dimension getPreferredSize() {
					Dimension content = super.getPreferredSize();
					int rowHeight = getFontMetrics(getFont()).getHeight();
					return new Dimension(content.width + getVerticalScrollBar().getPreferredSize().width,
							Math.min(content.height, rowHeight * 25));
				}
			};
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.setOpaque(false);
			scroll.getViewport().setOpaque(false);
			scroll.getVerticalScrollBar().setUnitIncrement(16);
			cards.add(scroll, category.name());
		}
		SBoxLayout.addSpring(navigation);
		add(navigation, BorderLayout.WEST);
		add(cards, BorderLayout.CENTER);
		addGroup(Category.DISPLAY, "Ekran");
		addGroup(Category.DISPLAY, "Płynność");
		addGroup(Category.DISPLAY, "Rozmiar okna");
		addGroup(Category.TEXT, "Język");
		addGroup(Category.TEXT, "Czcionki");
		addGroup(Category.TEXT, "Dymki");
		addGroup(Category.TEXT, "Wiadomości prywatne");
		showCategory(selected);
	}

	private void bindNavigation(JButton button, Category category, int key, int offset) {
		String action = "category-" + key;
		button.getInputMap().put(KeyStroke.getKeyStroke(key, 0), action);
		button.getActionMap().put(action, new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				Category[] categories = Category.values();
				Category next = categories[(category.ordinal() + offset + categories.length) % categories.length];
				showCategory(next);
				buttons.get(next).requestFocusInWindow();
			}
		});
	}

	JComponent addGroup(Category category, String title) {
		JComponent existing = groups.get(category).get(title);
		if (existing != null) return existing;
		JComponent group = SettingsComponentFactory.createSettingsGroup(title);
		groups.get(category).put(title, group);
		pages.get(category).add(group, SLayout.EXPAND_X);
		return group;
	}

	void addComponent(Category category, JComponent component) {
		pages.get(category).add(component, SLayout.EXPAND_X);
	}

	JComponent getPage(Category category) { return pages.get(category); }
	Category getSelectedCategory() { return selected; }

	void showCategory(Category category) {
		selected = category;
		((CardLayout) cards.getLayout()).show(cards, category.name());
		Style style = StyleUtil.getStyle();
		for (Category entry : Category.values()) {
			JButton button = buttons.get(entry);
			boolean active = entry == category;
			button.setSelected(active);
			button.setFont(button.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
			Border border = active ? (style == null ? BorderFactory.createLoweredBevelBorder() : style.getBorderDown())
					: borders.get(entry);
			// A wrapper keeps the styled button delegate from resetting the selected border.
			button.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(1, 2, 1, 2)));
			button.getAccessibleContext().setAccessibleDescription(active ? "Wybrana kategoria" : entry.getLabel());
		}
	}
}
