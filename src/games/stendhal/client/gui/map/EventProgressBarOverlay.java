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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Overlay used to display active map event progress.
 */
public class EventProgressBarOverlay extends JPanel {
    private static final int BAR_WIDTH = 260;
    private static final int BAR_HEIGHT = 14;

    private final JLabel eventTitle;
    private final JProgressBar progressBar;
    private final JLabel valueLabel;
    private String eventId;

    public EventProgressBarOverlay() {
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 185));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220, 180), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        setAlignmentX(JComponent.CENTER_ALIGNMENT);
        setAlignmentY(JComponent.TOP_ALIGNMENT);
        setLayout(new SBoxLayout(SBoxLayout.VERTICAL));

        eventTitle = new JLabel();
        eventTitle.setForeground(Color.WHITE);
        eventTitle.setFont(eventTitle.getFont().deriveFont(Font.BOLD));
        eventTitle.setAlignmentX(CENTER_ALIGNMENT);
        add(eventTitle, SLayout.EXPAND_X);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
        progressBar.setMinimumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
        progressBar.setMaximumSize(new Dimension(BAR_WIDTH, BAR_HEIGHT));
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        add(progressBar, SLayout.EXPAND_X);

        valueLabel = new JLabel();
        valueLabel.setForeground(new Color(235, 235, 235));
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(valueLabel, SLayout.EXPAND_X);

        setVisible(false);
    }

    public void showOverlay(final String newEventId, final String title, final int progressPercent, final String value) {
        eventId = newEventId;
        applyValues(title, progressPercent, value);
        setVisible(true);
    }

    public void updateOverlay(final String newEventId, final String title, final int progressPercent, final String value) {
        eventId = newEventId;
        applyValues(title, progressPercent, value);
        if (!isVisible()) {
            setVisible(true);
        }
    }

    public void hideOverlay() {
        eventId = null;
        setVisible(false);
    }

    public boolean isShowingEvent(final String checkedEventId) {
        return (eventId != null) && eventId.equals(checkedEventId);
    }

    private void applyValues(final String title, final int progressPercent, final String value) {
        eventTitle.setText((title == null || title.trim().isEmpty()) ? "Wydarzenie" : title);
        progressBar.setValue(Math.max(0, Math.min(100, progressPercent)));
        valueLabel.setText(value == null ? "" : value);
    }
}
