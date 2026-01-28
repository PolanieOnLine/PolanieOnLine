/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.stendhal;

/**
 * Window displaying improver offer details.
 */
public class ImproverOfferWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 7685383217812195876L;

	private final JLabel itemLabel = new JLabel();
	private final JLabel chanceLabel = new JLabel();
	private final JLabel costLabel = new JLabel();
	private final JPanel resourcesPanel = new JPanel();
	private final List<JLabel> resourceLabels = new ArrayList<JLabel>();

	public ImproverOfferWindow(final String title) {
		super("improver_offer", title);

		setMinimizable(false);
		setCloseable(true);
		setHideOnClose(true);

		itemLabel.setHorizontalAlignment(SwingConstants.CENTER);
		chanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
		costLabel.setHorizontalAlignment(SwingConstants.CENTER);

		final JPanel layout = new JPanel();
		layout.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, 4));
		layout.setBorder(new EmptyBorder(6, 8, 8, 8));
		layout.setOpaque(false);
		layout.add(itemLabel);
		layout.add(chanceLabel);
		layout.add(costLabel);

		resourcesPanel.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, 2));
		resourcesPanel.setOpaque(false);
		layout.add(resourcesPanel);

		setContent(layout);
	}

	public void setOffer(final String item, final int level, final int chance, final String costText,
			final JSONArray resources) {
		final StringBuilder name = new StringBuilder();
		name.append(item);
		if (level > 0) {
			name.append(" +").append(level);
		}

		itemLabel.setText(name.toString());
		chanceLabel.setText("Szansa: " + chance + "%");
		costLabel.setText("Koszt: " + (costText != null ? costText : "-"));

		resourcesPanel.removeAll();
		resourceLabels.clear();
		if (resources != null && !resources.isEmpty()) {
			JLabel header = new JLabel("Surowce:");
			header.setAlignmentX(Component.CENTER_ALIGNMENT);
			resourcesPanel.add(header);
			for (Object entry : resources) {
				if (entry instanceof JSONObject) {
					JSONObject resource = (JSONObject) entry;
					String nameValue = (String) resource.get("name");
					Long owned = (Long) resource.get("owned");
					Long required = (Long) resource.get("required");
					if (nameValue != null && owned != null && required != null) {
						JLabel label = new JLabel(nameValue + ": " + owned + " / " + required);
						label.setAlignmentX(Component.CENTER_ALIGNMENT);
						resourceLabels.add(label);
						resourcesPanel.add(label);
					}
				}
			}
		}

		resourcesPanel.revalidate();
		resourcesPanel.repaint();
	}

	public void open() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				j2DClient.get().addWindow(ImproverOfferWindow.this);
				center();
				getParent().validate();
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferred = super.getPreferredSize();
		Dimension displaySize = stendhal.getDisplaySize();
		int maxWidth = (int) displaySize.getWidth() - 80;
		int maxHeight = (int) displaySize.getHeight() - 80;
		return new Dimension(Math.min(preferred.width, maxWidth), Math.min(preferred.height, maxHeight));
	}
}
