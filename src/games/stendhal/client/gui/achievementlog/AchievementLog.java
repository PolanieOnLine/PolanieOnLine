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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

class AchievementLog {
	public static final int PAD = 5;
	private static final int BOOK_WIDTH = 720;
	private static final int BOOK_HEIGHT = 500;
	private static final int COLUMNS_PER_PAGE = 2;
	private static final int ROWS_PER_PAGE = 4;
	private static final int ACHIEVEMENTS_PER_PAGE = COLUMNS_PER_PAGE * ROWS_PER_PAGE;
	private static final Color BOOK_BACKGROUND = new Color(248, 243, 229);
	private static final Color BOOK_BORDER = new Color(164, 136, 98);
	private static final Color CARD_BACKGROUND = new Color(255, 252, 244);
	private static final Color LOCKED_CARD_BACKGROUND = new Color(245, 241, 234);
	private static final Color CARD_HOVER_BACKGROUND = new Color(255, 246, 220);
	private static final Color CARD_BORDER = new Color(171, 134, 89);
	private static final Color CARD_HOVER_BORDER = new Color(191, 154, 109);
	private static final Color LOCKED_TEXT_COLOR = new Color(120, 120, 120);

	private final JDialog window;
	private final JComponent page;

	private final List<AchievementEntry> allAchievements;
	private final List<AchievementEntry> visibleAchievements = new ArrayList<>();
	private final CardLayout bookLayout = new CardLayout();
	private final JPanel bookPages = new JPanel(bookLayout);
	private final JLabel pageIndicator = new JLabel("", SwingConstants.CENTER);
	private final ButtonGroup filterGroup = new ButtonGroup();

	private JButton previousButton;
	private JButton nextButton;
	private String activeCategory;
	private int currentPage;
	private int totalPages = 1;

	AchievementLog(String name) {
		window = new JDialog(j2DClient.get().getMainFrame(), name);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, PAD));
		window.setResizable(false);

		page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PAD);
		window.add(page);

		AchievementLogComponents component = new AchievementLogComponents();
		page.add(component.getHeaderText(BOOK_WIDTH, PAD));

		allAchievements = loadAchievements();
		activeCategory = null;

		page.add(createFilterPanel());
		page.add(createBookWrapper());
		page.add(createNavigationPanel());
		page.add(component.getButtons(window));

		WindowUtils.closeOnEscape(window);
		WindowUtils.watchFontSize(window);
		WindowUtils.trackLocation(window, "achievement_log", false);

		updateVisibleAchievements();
		updatePages();
		window.pack();
	}

	private void updateVisibleAchievements() {
		visibleAchievements.clear();
		for (AchievementEntry entry : allAchievements) {
			if (activeCategory == null || entry.categoryKey.equals(activeCategory)) {
				visibleAchievements.add(entry);
			}
		}
	}

	private void updatePages() {
		bookPages.removeAll();
		if (visibleAchievements.isEmpty()) {
			totalPages = 1;
			currentPage = 0;
			bookPages.add(createEmptyPage(), createPageName(0));
		} else {
			totalPages = (int) Math.ceil((double) visibleAchievements.size() / ACHIEVEMENTS_PER_PAGE);
			if (currentPage >= totalPages) {
				currentPage = Math.max(0, totalPages - 1);
			}
			for (int i = 0; i < totalPages; i++) {
				bookPages.add(createPagePanel(i), createPageName(i));
			}
		}
		bookPages.revalidate();
		bookPages.repaint();
		showPage(currentPage);
	}

	private void showPage(int index) {
		if (totalPages == 0) {
			return;
		}
		if (index < 0) {
			index = 0;
		} else if (index >= totalPages) {
			index = totalPages - 1;
		}
		currentPage = index;
		bookLayout.show(bookPages, createPageName(currentPage));
		updateNavigationState();
	}

	private void updateNavigationState() {
		String indicator = "Strona " + (currentPage + 1) + " z " + totalPages;
		pageIndicator.setText(indicator);
		if (previousButton != null) {
			previousButton.setEnabled(totalPages > 1 && currentPage > 0);
		}
		if (nextButton != null) {
			nextButton.setEnabled(totalPages > 1 && currentPage < totalPages - 1);
		}
	}

	private String createPageName(int index) {
		return "page-" + index;
	}

	private JPanel createPagePanel(int pageIndex) {
		JPanel pagePanel = new JPanel(new GridLayout(ROWS_PER_PAGE, COLUMNS_PER_PAGE, PAD * 2, PAD * 2));
		pagePanel.setOpaque(false);
		int start = pageIndex * ACHIEVEMENTS_PER_PAGE;
		int end = Math.min(start + ACHIEVEMENTS_PER_PAGE, visibleAchievements.size());
		for (int i = start; i < end; i++) {
			pagePanel.add(createAchievementCard(visibleAchievements.get(i)));
		}
		while (pagePanel.getComponentCount() < ACHIEVEMENTS_PER_PAGE) {
			pagePanel.add(createPlaceholderCard());
		}
		return pagePanel;
	}

	private JComponent createPlaceholderCard() {
		JPanel placeholder = new JPanel();
		placeholder.setOpaque(false);
		return placeholder;
	}

	private JPanel createEmptyPage() {
		JPanel emptyPage = new JPanel(new BorderLayout());
		emptyPage.setOpaque(false);
		JLabel label = new JLabel("<html><div style="text-align: center; color: #777777;">Brak osiągnięć w tej kategorii.</div></html>", SwingConstants.CENTER);
		emptyPage.add(label, BorderLayout.CENTER);
		return emptyPage;
	}

	private JComponent createBookWrapper() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel book = new JPanel(new BorderLayout());
		book.setOpaque(true);
		book.setBackground(BOOK_BACKGROUND);
		book.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(12, 16, 12, 16, BOOK_BORDER),
			BorderFactory.createEmptyBorder(20, 30, 20, 30)));
		book.setPreferredSize(new Dimension(BOOK_WIDTH, BOOK_HEIGHT));
		bookPages.setOpaque(false);
		bookPages.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		book.add(bookPages, BorderLayout.CENTER);

		wrapper.add(book, BorderLayout.CENTER);
		return wrapper;
	}

	private JComponent createNavigationPanel() {
		JComponent nav = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, PAD);
		nav.setAlignmentX(Component.CENTER_ALIGNMENT);
		nav.setBorder(BorderFactory.createEmptyBorder(0, PAD, PAD, PAD));

		previousButton = new JButton("← Poprzednia strona");
		previousButton.setFocusPainted(false);
		previousButton.addActionListener(e -> showPage(currentPage - 1));

		nextButton = new JButton("Następna strona →");
		nextButton.setFocusPainted(false);
		nextButton.addActionListener(e -> showPage(currentPage + 1));

		pageIndicator.setPreferredSize(new Dimension(160, previousButton.getPreferredSize().height));
		pageIndicator.setHorizontalAlignment(SwingConstants.CENTER);

		nav.add(previousButton);
		nav.add(pageIndicator);
		nav.add(nextButton);

		return nav;
	}

	private JPanel createFilterPanel() {
		JPanel filters = new JPanel();
		filters.setOpaque(false);
		filters.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, PAD, PAD / 2));
		filters.setBorder(BorderFactory.createEmptyBorder(0, PAD, 0, PAD));
		filters.setAlignmentX(Component.LEFT_ALIGNMENT);

		filters.add(createFilterToggle("Wszystkie", null, true));

		Map<String, String> categories = new LinkedHashMap<>();
		for (AchievementEntry entry : allAchievements) {
			if (!categories.containsKey(entry.categoryKey)) {
				categories.put(entry.categoryKey, entry.category);
			}
		}
		for (Map.Entry<String, String> entry : categories.entrySet()) {
			filters.add(createFilterToggle(formatCategoryLabel(entry.getValue()), entry.getKey(), false));
		}

		return filters;
	}

	private String formatCategoryLabel(String category) {
		if (category == null || category.isEmpty()) {
			return "Inne";
		}
		return category.toUpperCase(Locale.ROOT);
	}

	private JToggleButton createFilterToggle(String label, String categoryKey, boolean selected) {
		JToggleButton toggle = new JToggleButton(label);
		toggle.setFocusPainted(false);
		toggle.setSelected(selected);
		toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Border defaultBorder = BorderFactory.createCompoundBorder(new LineBorder(CARD_BORDER, 1, true),
			BorderFactory.createEmptyBorder(6, 16, 6, 16));
		toggle.setBorder(defaultBorder);
		toggle.setOpaque(true);
		toggle.setBackground(selected ? CARD_HOVER_BACKGROUND : BOOK_BACKGROUND);
		toggle.addChangeListener(e -> {
			if (toggle.isSelected()) {
				toggle.setBackground(CARD_HOVER_BACKGROUND);
				toggle.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CARD_HOVER_BORDER, 2, true),
					BorderFactory.createEmptyBorder(4, 14, 4, 14)));
			} else {
				toggle.setBackground(BOOK_BACKGROUND);
				toggle.setBorder(defaultBorder);
			}
		});
		toggle.addActionListener(e -> applyFilter(categoryKey));
		filterGroup.add(toggle);
		return toggle;
	}

	private void applyFilter(String categoryKey) {
		if ((categoryKey == null && activeCategory == null)
			|| (categoryKey != null && categoryKey.equals(activeCategory))) {
			return;
		}
		activeCategory = categoryKey;
		currentPage = 0;
		updateVisibleAchievements();
		updatePages();
	}

	private JComponent createAchievementCard(AchievementEntry entry) {
		JComponent card = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PAD / 2);
		Color baseBackground = entry.reached ? CARD_BACKGROUND : LOCKED_CARD_BACKGROUND;
		card.setOpaque(true);
		card.setBackground(baseBackground);
		Border defaultBorder = BorderFactory.createCompoundBorder(new LineBorder(CARD_BORDER, 1, true),
			BorderFactory.createEmptyBorder(12, 12, 12, 12));
		card.setBorder(defaultBorder);
		card.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JLabel iconLabel = new JLabel(entry.icon);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel titleLabel = new JLabel(entry.title, SwingConstants.CENTER);
		Font baseFont = titleLabel.getFont();
		titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize2D() + 1f));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		if (!entry.reached) {
			titleLabel.setForeground(LOCKED_TEXT_COLOR);
		}

		JLabel descLabel = new JLabel(createDescriptionHtml(entry.description), SwingConstants.CENTER);
		Color descColor = entry.reached ? Color.DARK_GRAY : LOCKED_TEXT_COLOR;
		descLabel.setForeground(descColor);
		descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		card.add(iconLabel);
		card.add(javax.swing.Box.createVerticalStrut(PAD / 2));
		card.add(titleLabel);
		card.add(javax.swing.Box.createVerticalStrut(PAD / 3));
		card.add(descLabel);

		card.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				card.setBackground(CARD_HOVER_BACKGROUND);
				card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CARD_HOVER_BORDER, 2, true),
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				card.setBackground(baseBackground);
				card.setBorder(defaultBorder);
			}
		});

		return card;
	}

	private String createDescriptionHtml(String desc) {
		return "<html><div style=\"text-align: center; font-size: 11px;\">" + desc + ".</div></html>";
	}

	private List<AchievementEntry> loadAchievements() {
		List<String> achievements = AchievementLogController.get().getList();
		List<AchievementEntry> entries = new ArrayList<>(achievements.size());
		for (String raw : achievements) {
			String[] parts = raw.split(":", 4);
			if (parts.length < 4) {
				continue;
			}
			String category = parts[0];
			String title = parts[1];
			String description = parts[2];
			boolean reached = Boolean.parseBoolean(parts[3]);
			Sprite sprite = getAchievementImage(category, reached);
			ImageIcon icon = createIcon(sprite);
			entries.add(new AchievementEntry(category, title, description, reached, icon));
		}
		return entries;
	}

	private ImageIcon createIcon(Sprite sprite) {
		if (sprite == null) {
			return new ImageIcon();
		}
		int width = Math.max(1, sprite.getWidth());
		int height = Math.max(1, sprite.getHeight());
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		sprite.draw(g2d, 0, 0);
		g2d.dispose();
		return new ImageIcon(image);
	}

	private Sprite getAchievementImage(String category, boolean reached) {
		String imagePath = "/data/sprites/achievements/" + category.toLowerCase(Locale.ROOT) + ".png";
		Sprite sprite;
		if (reached) {
			sprite = SpriteStore.get().getSprite(imagePath);
		} else {
			sprite = SpriteStore.get().getColoredSprite(imagePath, Color.LIGHT_GRAY);
		}
		if ((sprite != null) && (sprite.getWidth() > sprite.getHeight())) {
			sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
		}
		return sprite;
	}

	Window getWindow() {
		return window;
	}

	private static class AchievementEntry {
		private final String category;
		private final String categoryKey;
		private final String title;
		private final String description;
		private final boolean reached;
		private final ImageIcon icon;

		AchievementEntry(String category, String title, String description, boolean reached, ImageIcon icon) {
			this.category = category;
			this.categoryKey = category == null ? "" : category.toLowerCase(Locale.ROOT);
			this.title = title;
			this.description = description;
			this.reached = reached;
			this.icon = icon;
		}
	}
}
