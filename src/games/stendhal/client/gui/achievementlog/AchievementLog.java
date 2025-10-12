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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
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
	private static final int COLUMNS_PER_PAGE = 2;
	private static final int ROWS_PER_PAGE = 4;
	private static final int CARD_WIDTH = 320;
	private static final int CARD_HEIGHT = 110;
	private static final int CARD_ICON_SIZE = 64;
	private static final int CARD_ICON_AREA = CARD_ICON_SIZE + (PAD * 6);
	private static final int DESCRIPTION_WIDTH = CARD_WIDTH - CARD_ICON_AREA;
	private static final int FILTER_TILE_COLUMNS = 3;
	private static final int FILTER_TILE_ROWS = 2;
	private static final int FILTER_TILE_PER_PAGE = FILTER_TILE_COLUMNS * FILTER_TILE_ROWS;
	private static final int FILTER_ICON_SIZE = 36;
	private static final int BOOK_SIDE_BORDER = 16 + 30;
	private static final int BOOK_TOP_BORDER = 12 + 20;
	private static final int BOOK_WIDTH = (COLUMNS_PER_PAGE * CARD_WIDTH)
	+ ((COLUMNS_PER_PAGE - 1) * (PAD * 2))
	+ (BOOK_SIDE_BORDER * 2);
	private static final int BOOK_HEIGHT = (ROWS_PER_PAGE * CARD_HEIGHT)
	+ ((ROWS_PER_PAGE - 1) * (PAD * 2))
	+ (BOOK_TOP_BORDER * 2);
	private static final int CONTENT_WIDTH = BOOK_WIDTH + (PAD * 2);
	private static final int ACHIEVEMENTS_PER_PAGE = COLUMNS_PER_PAGE * ROWS_PER_PAGE;
	private static final Color BOOK_BACKGROUND = new Color(248, 243, 229);
	private static final Color BOOK_BORDER = new Color(164, 136, 98);
	private static final Color BOOK_SHADOW = new Color(210, 190, 160, 180);
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
	private final CardLayout filterDeckLayout = new CardLayout();
	private final JPanel filterDeck = new JPanel(filterDeckLayout);
	private final List<JToggleButton> filterToggles = new ArrayList<>();
	private int filterPageIndex;
	private int filterPageCount;
	private JButton filterPrevButton;
	private JButton filterNextButton;
	private boolean updatingFilterSelection;

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
		Dimension cardSize = new Dimension(CARD_WIDTH, CARD_HEIGHT);
		placeholder.setPreferredSize(cardSize);
		placeholder.setMinimumSize(cardSize);
		placeholder.setMaximumSize(cardSize);
		placeholder.setOpaque(false);
		return placeholder;
	}

	private JPanel createEmptyPage() {
		JPanel emptyPage = new JPanel(new BorderLayout());
		emptyPage.setOpaque(false);
		JLabel label = new JLabel("<html><div style=\"text-align: center; color: #777777;\">Brak osiągnięć w tej kategorii.</div></html>", SwingConstants.CENTER);
		emptyPage.add(label, BorderLayout.CENTER);
		return emptyPage;
	}

	private JComponent createBookWrapper() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

		Dimension bookSize = new Dimension(BOOK_WIDTH, BOOK_HEIGHT);
		Dimension pagesSize = new Dimension((COLUMNS_PER_PAGE * CARD_WIDTH)
		+ ((COLUMNS_PER_PAGE - 1) * (PAD * 2)),
		(ROWS_PER_PAGE * CARD_HEIGHT) + ((ROWS_PER_PAGE - 1) * (PAD * 2)));
		JPanel book = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				int width = getWidth();
				int height = getHeight();
				g2d.setColor(BOOK_SHADOW);
				int seamX = width / 2;
				g2d.fillRect(seamX - 2, 0, 4, height);
				g2d.setColor(new Color(255, 255, 255, 120));
				g2d.drawLine(seamX - 6, 8, seamX - 6, height - 8);
				g2d.drawLine(seamX + 6, 8, seamX + 6, height - 8);
				g2d.dispose();
			}
		};
		book.setOpaque(true);
		book.setBackground(BOOK_BACKGROUND);
		book.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(12, 16, 12, 16, BOOK_BORDER),
		BorderFactory.createEmptyBorder(20, 30, 20, 30)));
		book.setPreferredSize(bookSize);
		book.setMinimumSize(bookSize);
		book.setMaximumSize(bookSize);
		bookPages.setOpaque(false);
		bookPages.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		bookPages.setPreferredSize(pagesSize);
		bookPages.setMinimumSize(pagesSize);
		bookPages.setMaximumSize(pagesSize);
		book.add(bookPages, BorderLayout.CENTER);

		Dimension wrapperSize = new Dimension(CONTENT_WIDTH, bookSize.height + (PAD * 2));
		wrapper.setPreferredSize(wrapperSize);
		wrapper.setMinimumSize(wrapperSize);
		wrapper.setMaximumSize(wrapperSize);
		wrapper.add(book, BorderLayout.CENTER);
		return wrapper;
	}

	private JComponent createNavigationPanel() {
		JPanel nav = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, PAD * 4, PAD));
		nav.setOpaque(false);
		nav.setAlignmentX(Component.CENTER_ALIGNMENT);
		nav.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));

		previousButton = new JButton("← Poprzednia strona");
		previousButton.setFocusPainted(false);
		previousButton.addActionListener(e -> showPage(currentPage - 1));

		nextButton = new JButton("Następna strona →");
		nextButton.setFocusPainted(false);
		nextButton.addActionListener(e -> showPage(currentPage + 1));

		int buttonHeight = previousButton.getPreferredSize().height;
		int buttonWidth = Math.max(previousButton.getPreferredSize().width, nextButton.getPreferredSize().width);
		Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);
		previousButton.setPreferredSize(buttonSize);
		nextButton.setPreferredSize(buttonSize);

		pageIndicator.setPreferredSize(new Dimension(140, buttonHeight));
		pageIndicator.setHorizontalAlignment(SwingConstants.CENTER);

		Dimension navSize = new Dimension(CONTENT_WIDTH, buttonHeight + (PAD * 4));
		nav.setPreferredSize(navSize);
		nav.setMinimumSize(navSize);
		nav.setMaximumSize(navSize);

		nav.add(previousButton);
		nav.add(pageIndicator);
		nav.add(nextButton);

		return nav;
	}

	private JComponent createFilterPanel() {
		for (JToggleButton toggle : new ArrayList<>(filterToggles)) {
			filterGroup.remove(toggle);
		}
		filterToggles.clear();
		filterDeck.removeAll();
		filterGroup.clearSelection();
		JPanel container = new JPanel(new BorderLayout(PAD * 2, 0));
		container.setOpaque(false);
		container.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		container.setAlignmentX(Component.CENTER_ALIGNMENT);
		container.setPreferredSize(new Dimension(CONTENT_WIDTH, (FILTER_ICON_SIZE + (PAD * 10)) * FILTER_TILE_ROWS));
		container.setMaximumSize(new Dimension(CONTENT_WIDTH, Integer.MAX_VALUE));

		List<CategoryInfo> categories = new ArrayList<>();
		ImageIcon allIcon = loadAllIcon();
		categories.add(new CategoryInfo(null, "Wszystkie", allIcon));
		categories.addAll(collectCategories().values());

		List<JToggleButton> toggles = new ArrayList<>(categories.size());
		for (CategoryInfo info : categories) {
			ImageIcon icon = info.icon != null ? info.icon : allIcon;
			toggles.add(createFilterToggle(icon, info.displayName, info.key, info.key == null));
		}

		filterToggles.addAll(toggles);
		filterPageCount = (int) Math.ceil((double) toggles.size() / FILTER_TILE_PER_PAGE);
		if (filterPageCount <= 0) {
			filterPageCount = 1;
		}

		for (int pageIndex = 0; pageIndex < filterPageCount; pageIndex++) {
			JPanel page = new JPanel(new GridLayout(FILTER_TILE_ROWS, FILTER_TILE_COLUMNS, PAD, PAD));
			page.setOpaque(false);
			int start = pageIndex * FILTER_TILE_PER_PAGE;
			int end = Math.min(start + FILTER_TILE_PER_PAGE, toggles.size());
			for (int i = start; i < end; i++) {
				page.add(toggles.get(i));
			}
			while (page.getComponentCount() < FILTER_TILE_PER_PAGE) {
				page.add(createFilterPlaceholder());
			}
			filterDeck.add(page, "filter-page-" + pageIndex);
		}
		filterDeck.setOpaque(false);

		filterPrevButton = createFilterPagerButton(true);
		filterNextButton = createFilterPagerButton(false);
		container.add(filterPrevButton, BorderLayout.WEST);
		container.add(filterDeck, BorderLayout.CENTER);
		container.add(filterNextButton, BorderLayout.EAST);

		showFilterPage(0);
		return container;
	}

	private Component createFilterPlaceholder() {
		JPanel placeholder = new JPanel();
		placeholder.setOpaque(false);
		return placeholder;
	}

	private JButton createFilterPagerButton(boolean previous) {
		String label = previous ? "◀" : "▶";
		JButton button = new JButton(label);
		button.setOpaque(false);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		button.setContentAreaFilled(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setForeground(new Color(90, 70, 40));
		button.setFont(button.getFont().deriveFont(Font.BOLD, button.getFont().getSize2D() + 2f));
		button.setPreferredSize(new Dimension(36, 36));
		button.addActionListener(e -> {
			if (previous) {
				showFilterPage(filterPageIndex - 1);
			} else {
				showFilterPage(filterPageIndex + 1);
			}
		});
		return button;
	}

	private void showFilterPage(int index) {
		if (filterPageCount <= 1) {
			filterPageIndex = 0;
			filterDeckLayout.show(filterDeck, "filter-page-0");
			updateFilterPagerState();
			return;
		}
		if (index < 0) {
			index = 0;
		} else if (index >= filterPageCount) {
			index = filterPageCount - 1;
		}
		filterPageIndex = index;
		filterDeckLayout.show(filterDeck, "filter-page-" + filterPageIndex);
		updateFilterPagerState();
	}

	private void updateFilterPagerState() {
		if (filterPrevButton != null) {
			filterPrevButton.setEnabled(filterPageIndex > 0);
		}
		if (filterNextButton != null) {
			filterNextButton.setEnabled(filterPageIndex < filterPageCount - 1);
		}
	}

	private JToggleButton createFilterToggle(ImageIcon icon, String displayName, String categoryKey, boolean selected) {
		JToggleButton toggle = new JToggleButton();
		ImageIcon usedIcon = icon;
		if ((usedIcon == null) || (usedIcon.getIconWidth() == 0) || (usedIcon.getIconHeight() == 0)) {
			usedIcon = loadAllIcon();
		}
		ImageIcon scaled = scaleIcon(usedIcon, FILTER_ICON_SIZE);
		toggle.setIcon(scaled);
		toggle.setText(displayName);
		toggle.setHorizontalTextPosition(SwingConstants.CENTER);
		toggle.setVerticalTextPosition(SwingConstants.BOTTOM);
		toggle.setMargin(new Insets(PAD * 2, PAD * 2, PAD * 2, PAD * 2));
		toggle.setFocusPainted(false);
		toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		toggle.setOpaque(true);
		toggle.setBackground(selected ? CARD_HOVER_BACKGROUND : BOOK_BACKGROUND);
		Border defaultBorder = BorderFactory.createCompoundBorder(new LineBorder(CARD_BORDER, 1, true),
		BorderFactory.createEmptyBorder(8, 8, 8, 8));
		toggle.setBorder(defaultBorder);
		toggle.getAccessibleContext().setAccessibleName(displayName);
		toggle.putClientProperty("categoryKey", categoryKey);
		toggle.addChangeListener(e -> {
			if (toggle.isSelected()) {
				toggle.setBackground(CARD_HOVER_BACKGROUND);
				toggle.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CARD_HOVER_BORDER, 2, true),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));
			} else {
				toggle.setBackground(BOOK_BACKGROUND);
				toggle.setBorder(defaultBorder);
			}
		});
		toggle.addActionListener(e -> {
			if (!updatingFilterSelection) {
				applyFilter(categoryKey);
			}
		});
		toggle.setSelected(selected);
		filterGroup.add(toggle);
		return toggle;
	}

	private Map<String, CategoryInfo> collectCategories() {
		Map<String, CategoryInfo> categories = new LinkedHashMap<>();
		for (AchievementEntry entry : allAchievements) {
			if (!categories.containsKey(entry.categoryKey)) {
				String display = entry.category == null || entry.category.isEmpty() ? "Inne" : entry.category;
				categories.put(entry.categoryKey, new CategoryInfo(entry.categoryKey, display, getFilterIcon(entry.categoryKey)));
			}
		}
		return categories;
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
		updateFilterSelection();
	}

	private void updateFilterSelection() {
		updatingFilterSelection = true;
		try {
			int selectedIndex = -1;
			for (int i = 0; i < filterToggles.size(); i++) {
				JToggleButton toggle = filterToggles.get(i);
				String key = (String) toggle.getClientProperty("categoryKey");
				boolean shouldSelect = (key == null && activeCategory == null)
				|| (key != null && key.equals(activeCategory));
				if (shouldSelect) {
					filterGroup.setSelected(toggle.getModel(), true);
					selectedIndex = i;
				} else {
					toggle.setSelected(false);
				}
			}
			if (selectedIndex >= 0) {
				int pageIndex = selectedIndex / FILTER_TILE_PER_PAGE;
				showFilterPage(pageIndex);
			}
		} finally {
			updatingFilterSelection = false;
		}
	}

	private JComponent createAchievementCard(AchievementEntry entry) {
		JPanel card = new JPanel(new BorderLayout(PAD * 2, 0));
		Color baseBackground = entry.reached ? CARD_BACKGROUND : LOCKED_CARD_BACKGROUND;
		card.setOpaque(true);
		card.setBackground(baseBackground);
		Border defaultBorder = BorderFactory.createCompoundBorder(new LineBorder(CARD_BORDER, 1, true),
		BorderFactory.createEmptyBorder(12, 14, 12, 14));
		card.setBorder(defaultBorder);
		card.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Dimension cardSize = new Dimension(CARD_WIDTH, CARD_HEIGHT);
		card.setPreferredSize(cardSize);
		card.setMinimumSize(cardSize);
		card.setMaximumSize(cardSize);
		card.setDoubleBuffered(true);

		JLabel iconLabel = new JLabel(scaleIcon(entry.icon, CARD_ICON_SIZE));
		Dimension iconArea = new Dimension(CARD_ICON_AREA, CARD_HEIGHT - (PAD * 4));
		iconLabel.setPreferredSize(iconArea);
		iconLabel.setMinimumSize(iconArea);
		iconLabel.setMaximumSize(iconArea);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		card.add(iconLabel, BorderLayout.WEST);

		JComponent textPanel = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PAD);
		textPanel.setOpaque(false);

		JLabel titleLabel = new JLabel(entry.title);
		Font baseFont = titleLabel.getFont();
		titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize2D() + 1f));
		titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		titleLabel.setForeground(entry.reached ? Color.DARK_GRAY : LOCKED_TEXT_COLOR);
		textPanel.add(titleLabel);

		JLabel descLabel = new JLabel(createDescriptionHtml(entry.description), SwingConstants.LEFT);
		Color descColor = entry.reached ? new Color(80, 80, 80) : LOCKED_TEXT_COLOR;
		descLabel.setForeground(descColor);
		descLabel.setVerticalAlignment(SwingConstants.TOP);
		textPanel.add(descLabel);

		card.add(textPanel, BorderLayout.CENTER);

		card.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				card.setBackground(CARD_HOVER_BACKGROUND);
				card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CARD_HOVER_BORDER, 2, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
				card.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				card.setBackground(baseBackground);
				card.setBorder(defaultBorder);
				card.repaint();
			}
		});

		return card;
	}

	private String createDescriptionHtml(String desc) {
		String text = desc == null ? "" : desc.trim();
		if (!text.isEmpty()) {
			char last = text.charAt(text.length() - 1);
			if (last != '.' && last != '!' && last != '?') {
				text = text + '.';
			}
		}
		return "<html><div style=\"width: " + Math.max(120, DESCRIPTION_WIDTH) + "px; text-align: left; font-size: 12px;\">"
		+ text + "</div></html>";
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
			String categoryKey = category == null ? "" : category.toLowerCase(Locale.ROOT);
			if (categoryKey.endsWith("ratk")) {
				continue;
			}
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
		String key = category == null ? "" : category.toLowerCase(Locale.ROOT);
		String imagePath = "/data/sprites/achievements/" + key + ".png";
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

	private ImageIcon getFilterIcon(String categoryKey) {
		if (categoryKey == null || categoryKey.isEmpty()) {
			return loadAllIcon();
		}
		Sprite sprite = SpriteStore.get().getSprite("/data/sprites/achievements/" + categoryKey + ".png");
		if (sprite == null) {
			return loadAllIcon();
		}
		return createIcon(sprite);
	}

	private ImageIcon loadAllIcon() {
		Sprite sprite = SpriteStore.get().getSprite("/data/sprites/achievements/special.png");
		ImageIcon icon = createIcon(sprite);
		if (icon == null || icon.getIconWidth() == 0) {
			BufferedImage placeholder = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
			return new ImageIcon(placeholder);
		}
		return icon;
	}

	private ImageIcon scaleIcon(ImageIcon icon, int size) {
		if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
			return icon;
		}
		if (icon.getIconWidth() == size && icon.getIconHeight() == size) {
			return icon;
		}
		BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = scaled.createGraphics();
		g2d.drawImage(icon.getImage(), 0, 0, size, size, null);
		g2d.dispose();
		return new ImageIcon(scaled);
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

	private static class CategoryInfo {
		private final String key;
		private final String displayName;
		private final ImageIcon icon;

		CategoryInfo(String key, String displayName, ImageIcon icon) {
			this.key = key;
			this.displayName = displayName;
			this.icon = icon;
		}
	}
}
