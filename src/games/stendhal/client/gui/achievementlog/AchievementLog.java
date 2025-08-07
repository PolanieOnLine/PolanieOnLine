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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * @author KarajuSs
 */
public class AchievementLog {
    private static final int TABLE_WIDTH = 720;
    private static final int TABLE_HEIGHT = 500;
    private static final int CARD_WIDTH = 200;
    public static final int PAD = 5;

    private JDialog window;
    private JPanel mainPanel;
    private JPanel achievementPanel;
    private JList<String> categoryList;
    private DefaultListModel<String> listModel;

    private AchievementLogRenderers renderer = AchievementLogRenderers.get();

    private JPanel expandedDescriptionPanel;
    private Timer animationTimer;
    private int expandedHeight = 0;
    private final int maxDescriptionHeight = 100;

    private int expandedRowIndex = -1;

    AchievementLog(String name) {
        window = new JDialog(j2DClient.get().getMainFrame(), name);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setLayout(new BorderLayout(PAD, PAD));
        window.setResizable(false);

        mainPanel = new JPanel(new BorderLayout(PAD, PAD));
        achievementPanel = new JPanel();
        achievementPanel.setLayout(new GridBagLayout()); // Using GridBagLayout for more control

        expandedDescriptionPanel = new JPanel();
        expandedDescriptionPanel.setLayout(new BorderLayout());
        expandedDescriptionPanel.setVisible(false);

        listModel = new DefaultListModel<>();
        List<String> categories = AchievementLogController.get().getCategories();
        for (String category : categories) {
            listModel.addElement(category);
        }

        categoryList = new JList<>(listModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setSelectedIndex(0);
        categoryList.addListSelectionListener(new CategorySelectionListener());

        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setPreferredSize(new Dimension(150, TABLE_HEIGHT));

        mainPanel.add(categoryScrollPane, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(achievementPanel), BorderLayout.CENTER);

        window.add(mainPanel);
        loadAchievements(categories.get(0));

        WindowUtils.closeOnEscape(window);
        WindowUtils.watchFontSize(window);
        WindowUtils.trackLocation(window, "achievement_log", false);
        window.pack();
    }

    private void loadAchievements(String category) {
    	achievementPanel.removeAll();
        expandedDescriptionPanel.setVisible(false);
        expandedRowIndex = -1;
        List<String> achievements = AchievementLogController.get().getAchievementsByCategory(category);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(PAD, PAD, PAD, PAD);
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        int colCount = TABLE_WIDTH / CARD_WIDTH;
        int currentCol = 0;

        for (String achievement : achievements) {
        	JPanel card = createAchievementCard(achievement.split(":"));
            achievementPanel.add(card, constraints);
            currentCol++;
            if (currentCol == colCount) {
                constraints.gridy++;
                currentCol = 0;
            }
            constraints.gridx = currentCol;
        }

        achievementPanel.revalidate();
        achievementPanel.repaint();
    }

    private JPanel createAchievementCard(final String[] achievementData) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        cardPanel.setPreferredSize(new Dimension(CARD_WIDTH, 100));

        final String title = achievementData[1];
        final String desc = achievementData[2];
        boolean reached = achievementData[3].equals("true");

        JLabel iconLabel = new JLabel(new ImageIcon(convertToImage(getAchievementImage(achievementData[0], reached))));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(iconLabel, BorderLayout.CENTER);
        headerPanel.add(titleLabel, BorderLayout.SOUTH);

        cardPanel.add(headerPanel, BorderLayout.NORTH);

        cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
            	toggleExpandedDescription(title, desc, cardPanel);
            }
        });

        return cardPanel;
    }

    private void toggleExpandedDescription(String title, String desc, JPanel cardPanel) {
        int cardIndex = achievementPanel.getComponentZOrder(cardPanel);
        int colCount = TABLE_WIDTH / CARD_WIDTH;
        int rowIndex = cardIndex / colCount;

        if (expandedRowIndex == rowIndex) {
            achievementPanel.remove(expandedDescriptionPanel);
            expandedRowIndex = -1;
            achievementPanel.revalidate();
            achievementPanel.repaint();
            return;
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = rowIndex + 1;
        constraints.gridwidth = colCount;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        expandedDescriptionPanel.removeAll();

        JLabel arrowLabel = new JLabel("▼", SwingConstants.CENTER);
        arrowLabel.setFont(arrowLabel.getFont().deriveFont(16f));
        arrowLabel.setPreferredSize(new Dimension(20, 20));

        JPanel arrowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, PAD, PAD));
        arrowPanel.add(arrowLabel);

        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(descArea.getFont().deriveFont(Font.ITALIC));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);

        expandedDescriptionPanel.add(arrowPanel, BorderLayout.NORTH);
        expandedDescriptionPanel.add(descArea, BorderLayout.CENTER);

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        expandedHeight = 0;
        expandedDescriptionPanel.setPreferredSize(new Dimension(TABLE_WIDTH, 0));
        achievementPanel.add(expandedDescriptionPanel, constraints);
        expandedRowIndex = rowIndex;

        animationTimer = new Timer(10, e -> {
            expandedHeight += 5;
            expandedDescriptionPanel.setPreferredSize(new Dimension(TABLE_WIDTH, expandedHeight));
            achievementPanel.revalidate();
            if (expandedHeight >= maxDescriptionHeight) {
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer.start();

        expandedDescriptionPanel.setVisible(true);

        // Adjust arrow position dynamically
        int arrowX = cardPanel.getX() + cardPanel.getWidth() / 2 - arrowLabel.getWidth() / 2;
        arrowLabel.setLocation(arrowX, arrowLabel.getY());

        achievementPanel.revalidate();
        achievementPanel.repaint();
    }

    private Image convertToImage(Sprite sprite) {
        if (sprite instanceof ImageSprite) {
            return ((ImageSprite) sprite).getImage();
        }
        throw new IllegalArgumentException("Unsupported sprite type: " + sprite.getClass().getName());
    }

    private Sprite getAchievementImage(String category, boolean reached) {
        String imagePath = "/data/sprites/achievements/" + category.toLowerCase() + ".png";
        Sprite sprite = reached ? SpriteStore.get().getSprite(imagePath) : SpriteStore.get().getColoredSprite(imagePath, Color.LIGHT_GRAY);
        return sprite;
    }

    private class CategorySelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                loadAchievements(selectedCategory);
            }
        }
    }

    Window getWindow() {
        return window;
    }
}
