/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.status.ActiveMapEventStatus;
import games.stendhal.client.sprite.DataLoader;

/**
 * Collapsible overlay displaying all event secondary objectives.
 */
public class EventSecondaryObjectiveOverlay extends JPanel {
	private static final String COLLAPSED_CARD = "collapsed";
	private static final String EXPANDED_CARD = "expanded";
	private static final int PANEL_RADIUS = 9;
	private static final int EXPANDED_WIDTH = 320;
	private static final int COLLAPSED_WIDTH = 164;
	private static final int COLLAPSED_HEIGHT = 48;
	private static final int ENTRY_GAP = 8;
	private static final int MAX_EXPANDED_HEIGHT = 260;
	private static final int MIN_EXPANDED_HEIGHT = 116;
	private static final Color PANEL_BACKGROUND = new Color(22, 14, 10, 102);
	private static final Color PANEL_BORDER = new Color(168, 130, 92, 130);
	private static final Color HEADER_COLOR = new Color(246, 230, 200);
	private static final Color VALUE_COLOR = Color.WHITE;
	private static final Color MUTED_COLOR = new Color(220, 198, 166);
	private static final Color ACCENT_COLOR = new Color(244, 210, 132);
	private static final Color SUCCESS_COLOR = new Color(178, 225, 160);
	private static final Color FAILED_COLOR = new Color(222, 146, 146);
	private static final Color ENTRY_BORDER = new Color(168, 130, 92, 70);
	private static final ImageIcon SCROLL_ICON = createScrollIcon();

	private final CardLayout cards = new CardLayout();
	private final JPanel collapsedPanel = createOverlayPanel();
	private final JPanel expandedPanel = createOverlayPanel();
	private final JPanel objectivesContainer = createOverlayPanel();
	private final JScrollPane objectivesScrollPane = createScrollPane(objectivesContainer);
	private final JLabel collapsedIconLabel = new JLabel();
	private final JLabel collapsedTitleLabel = new JLabel("Cele poboczne");
	private final JLabel collapsedHintLabel = new JLabel("Kliknij, aby rozwinąć");
	private final JLabel expandedHeaderLabel = new JLabel("Cele poboczne");
	private final JLabel expandedHintLabel = new JLabel("Kliknij ponownie, aby zwinąć");
	private boolean expanded;
	private float overlayAlpha = 1.0f;
	private int expandedPreferredHeight = MIN_EXPANDED_HEIGHT;

	@Override
	public void updateUI() {
		setUI(new BasicPanelUI());
		updateLabelUi(collapsedIconLabel, collapsedTitleLabel, collapsedHintLabel,
				expandedHeaderLabel, expandedHintLabel);
	}

	public EventSecondaryObjectiveOverlay() {
		setUI(new BasicPanelUI());
		setOpaque(false);
		setLayout(cards);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setToolTipText("Kliknij, aby rozwinąć lub zwinąć cele poboczne.");

		buildCollapsedPanel();
		buildExpandedPanel();

		add(collapsedPanel, COLLAPSED_CARD);
		add(expandedPanel, EXPANDED_CARD);
		setExpanded(false);
		setVisible(false);
	}

	public void setOverlayAlpha(final float alpha) {
		overlayAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
		repaint();
	}

	public void updateObjectives(final List<ActiveMapEventStatus.SecondaryObjectiveStatus> objectives) {
		final List<ActiveMapEventStatus.SecondaryObjectiveStatus> safeObjectives =
				(objectives == null) ? Collections.<ActiveMapEventStatus.SecondaryObjectiveStatus>emptyList() : objectives;
		objectivesContainer.removeAll();
		if (safeObjectives.isEmpty()) {
			setExpanded(false);
			setVisible(false);
			expandedPreferredHeight = MIN_EXPANDED_HEIGHT;
			return;
		}

		for (int i = 0; i < safeObjectives.size(); i++) {
			if (i > 0) {
				objectivesContainer.add(createVerticalGap());
			}
			objectivesContainer.add(createObjectiveEntry(safeObjectives.get(i)), SLayout.EXPAND_X);
		}
		objectivesContainer.revalidate();
		objectivesContainer.doLayout();
		final int contentHeight = objectivesContainer.getPreferredSize().height + 54;
		expandedPreferredHeight = Math.max(MIN_EXPANDED_HEIGHT, Math.min(MAX_EXPANDED_HEIGHT, contentHeight));
		objectivesScrollPane.getVerticalScrollBar().setValue(0);
		setVisible(true);
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(final boolean value) {
		expanded = value;
		cards.show(this, expanded ? EXPANDED_CARD : COLLAPSED_CARD);
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return expanded
				? new Dimension(EXPANDED_WIDTH, expandedPreferredHeight)
				: new Dimension(COLLAPSED_WIDTH, COLLAPSED_HEIGHT);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (overlayAlpha <= 0.0f) {
			return;
		}
		final Graphics2D g2d = (Graphics2D) g.create();
		try {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final int width = getWidth();
			final int height = getHeight();
			if ((width > 1) && (height > 1)) {
				final int arc = PANEL_RADIUS * 2;
				g2d.setColor(PANEL_BACKGROUND);
				g2d.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);
				final Stroke originalStroke = g2d.getStroke();
				g2d.setStroke(new BasicStroke(0.8f));
				g2d.setColor(PANEL_BORDER);
				g2d.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
				g2d.setStroke(originalStroke);
			}
			super.paintComponent(g2d);
		} finally {
			g2d.dispose();
		}
	}

	private void buildCollapsedPanel() {
		collapsedPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		collapsedPanel.setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL));

		collapsedIconLabel.setUI(new BasicLabelUI());
		collapsedIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		collapsedIconLabel.setVerticalAlignment(SwingConstants.CENTER);
		collapsedIconLabel.setIcon(SCROLL_ICON);
		collapsedIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		collapsedPanel.add(collapsedIconLabel);

		final JPanel collapsedTextPanel = createOverlayPanel();
		collapsedTextPanel.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		collapsedTitleLabel.setUI(new BasicLabelUI());
		collapsedTitleLabel.setHorizontalAlignment(SwingConstants.LEFT);
		collapsedTitleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		collapsedTitleLabel.setForeground(HEADER_COLOR);
		collapsedTextPanel.add(collapsedTitleLabel, SLayout.EXPAND_X);

		collapsedHintLabel.setUI(new BasicLabelUI());
		collapsedHintLabel.setHorizontalAlignment(SwingConstants.LEFT);
		collapsedHintLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		collapsedHintLabel.setForeground(ACCENT_COLOR);
		collapsedTextPanel.add(collapsedHintLabel, SLayout.EXPAND_X);

		collapsedPanel.add(collapsedTextPanel, SLayout.EXPAND_X);
		installToggleHandler(collapsedPanel);
	}

	private void buildExpandedPanel() {
		expandedPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		expandedPanel.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

		expandedHeaderLabel.setUI(new BasicLabelUI());
		expandedHeaderLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		expandedHeaderLabel.setForeground(HEADER_COLOR);
		expandedPanel.add(expandedHeaderLabel, SLayout.EXPAND_X);

		expandedHintLabel.setUI(new BasicLabelUI());
		expandedHintLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		expandedHintLabel.setForeground(ACCENT_COLOR);
		expandedPanel.add(expandedHintLabel, SLayout.EXPAND_X);

		objectivesContainer.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));
		objectivesScrollPane.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		expandedPanel.add(objectivesScrollPane, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
		installToggleHandler(expandedHeaderLabel);
		installToggleHandler(expandedHintLabel);
	}

	private JPanel createObjectiveEntry(final ActiveMapEventStatus.SecondaryObjectiveStatus objective) {
		final JPanel entryPanel = createOverlayPanel();
		entryPanel.setLayout(new SBoxLayout(SBoxLayout.VERTICAL));
		entryPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, ENTRY_BORDER),
				BorderFactory.createEmptyBorder(0, 0, 6, 0)));

		final JLabel titleLabel = createTextLabel(objective.getTitle(), Font.BOLD, 12, resolveStateColor(objective));
		entryPanel.add(titleLabel, SLayout.EXPAND_X);

		final JLabel stateLabel = createTextLabel(resolveWaveLabel(objective), Font.PLAIN, 10, ACCENT_COLOR);
		entryPanel.add(stateLabel, SLayout.EXPAND_X);

		final JLabel detailsLabel = createHtmlLabel(objective.getDetails(), 250, MUTED_COLOR);
		entryPanel.add(detailsLabel, SLayout.EXPAND_X);

		if (!objective.getTrackedTargetLabels().isEmpty()) {
			final String targets = "Cele: " + joinValues(objective.getTrackedTargetLabels());
			entryPanel.add(createHtmlLabel(targets, 250, VALUE_COLOR), SLayout.EXPAND_X);
		}

		entryPanel.add(createTextLabel(resolveProgressLabel(objective), Font.PLAIN, 11, VALUE_COLOR), SLayout.EXPAND_X);

		if (objective.getRewardDescription() != null && !objective.getRewardDescription().trim().isEmpty()) {
			entryPanel.add(createHtmlLabel("Nagroda: " + objective.getRewardDescription(), 250, MUTED_COLOR), SLayout.EXPAND_X);
		}
		return entryPanel;
	}

	private JLabel createTextLabel(final String text, final int style, final int size, final Color color) {
		final JLabel label = new JLabel((text == null) ? "" : text);
		label.setUI(new BasicLabelUI());
		label.setFont(new Font(Font.DIALOG, style, size));
		label.setForeground(color);
		label.setOpaque(false);
		label.setBorder(BorderFactory.createEmptyBorder());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JLabel createHtmlLabel(final String text, final int widthPx, final Color color) {
		final String colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		final JLabel label = new JLabel("<html><div style='width:" + widthPx + "px;color:" + colorHex + ";'>"
				+ escapeHtml(text) + "</div></html>");
		label.setUI(new BasicLabelUI());
		label.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		label.setForeground(color);
		label.setOpaque(false);
		label.setBorder(BorderFactory.createEmptyBorder());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private Component createVerticalGap() {
		final JPanel spacer = createOverlayPanel();
		spacer.setPreferredSize(new Dimension(1, ENTRY_GAP));
		spacer.setMinimumSize(new Dimension(1, ENTRY_GAP));
		spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, ENTRY_GAP));
		return spacer;
	}

	private String resolveWaveLabel(final ActiveMapEventStatus.SecondaryObjectiveStatus objective) {
		final String state = normalizeState(objective.getState());
		if ("pending".equals(state)) {
			return "Od fali " + Math.max(1, objective.getStartWave());
		}
		if ("completed".equals(state)) {
			return "Cel wykonany";
		}
		if ("failed".equals(state)) {
			return "Cel nieudany";
		}
		if (objective.getStartWave() == objective.getEndWave()) {
			return "Fala " + Math.max(1, objective.getStartWave());
		}
		return "Fale " + Math.max(1, objective.getStartWave()) + "-" + Math.max(objective.getStartWave(), objective.getEndWave());
	}

	private Color resolveStateColor(final ActiveMapEventStatus.SecondaryObjectiveStatus objective) {
		final String state = normalizeState(objective.getState());
		if ("completed".equals(state)) {
			return SUCCESS_COLOR;
		}
		if ("failed".equals(state)) {
			return FAILED_COLOR;
		}
		if ("pending".equals(state)) {
			return HEADER_COLOR;
		}
		return VALUE_COLOR;
	}

	private String resolveProgressLabel(final ActiveMapEventStatus.SecondaryObjectiveStatus objective) {
		final int progress = Math.max(0, objective.getProgress());
		final int target = Math.max(0, objective.getTarget());
		if (target <= 0) {
			return "Postęp: " + progress;
		}
		return "Postęp: " + progress + " / " + target;
	}

	private String normalizeState(final String state) {
		return (state == null) ? "" : state.trim().toLowerCase();
	}

	private String joinValues(final List<String> values) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(values.get(i));
		}
		return builder.toString();
	}

	private void installToggleHandler(final Component component) {
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (!isVisible()) {
					return;
				}
				setExpanded(!expanded);
			}
		});
	}

	private void updateLabelUi(final JLabel... labels) {
		if (labels == null) {
			return;
		}
		for (JLabel label : labels) {
			if (label != null) {
				label.setUI(new BasicLabelUI());
			}
		}
	}

	private String escapeHtml(final String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private static JPanel createOverlayPanel() {
		final JPanel panel = new JPanel();
		panel.setUI(new BasicPanelUI());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createEmptyBorder());
		return panel;
	}

	private static JScrollPane createScrollPane(final JPanel content) {
		final JScrollPane scrollPane = new JScrollPane(content,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setUI(new BasicScrollPaneUI());
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUI(new OverlayScrollBarUI());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}

	private static ImageIcon createScrollIcon() {
		final java.net.URL resource = DataLoader.getResource("data/sprites/items/documents/scroll.png");
		if (resource == null) {
			return null;
		}
		final ImageIcon icon = new ImageIcon(resource);
		final Image image = icon.getImage();
		if (image == null) {
			return icon;
		}
		return new ImageIcon(image.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
	}

	private static final class OverlayScrollBarUI extends BasicScrollBarUI {
		private static final Color THUMB_COLOR = new Color(168, 130, 92, 150);
		private static final Color THUMB_BORDER = new Color(97, 72, 50, 190);

		@Override
		protected void configureScrollBarColors() {
			thumbColor = THUMB_COLOR;
			trackColor = new Color(0, 0, 0, 0);
		}

		@Override
		protected void paintTrack(final Graphics g, final JComponent c, final java.awt.Rectangle trackBounds) {
			// transparent track
		}

		@Override
		protected void paintThumb(final Graphics g, final JComponent c, final java.awt.Rectangle thumbBounds) {
			if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
				return;
			}
			final Graphics2D g2d = (Graphics2D) g.create();
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(THUMB_COLOR);
				g2d.fillRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 4, thumbBounds.height, 8, 8);
				g2d.setColor(THUMB_BORDER);
				g2d.drawRoundRect(thumbBounds.x + 2, thumbBounds.y, thumbBounds.width - 5, thumbBounds.height - 1, 8, 8);
			} finally {
				g2d.dispose();
			}
		}

	}
}
