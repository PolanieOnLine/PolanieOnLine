/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.settings;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.wt.core.WtWindowManager;

class SettingsComponentFactory {
	static JComponent createSettingsGroup(String title) {
		int pad = SBoxLayout.COMMON_PADDING;
		JComponent group = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
		Style style = StyleUtil.getStyle();
		if (style != null) {
			group.setBackground(style.getPlainColor());
			border.setTitleColor(style.getForeground());
		}
		group.setBorder(BorderFactory.createCompoundBorder(border,
				BorderFactory.createEmptyBorder(pad, pad, pad, pad)));
		return group;
	}

	/** Keep long device/font choices from dictating the width of the entire dialog. */
	@SuppressWarnings("serial")
	static JComponent createSettingsRow(String title, JComponent control) {
		JLabel label = new JLabel(title);
		label.setLabelFor(control);
		JPanel row = new JPanel(new BorderLayout(10, 0)) {
			@Override public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				int selectorWidth = getFontMetrics(getFont()).charWidth('0') * 28;
				size.width = label.getPreferredSize().width + 10 + selectorWidth;
				return size;
			}
			@Override public Dimension getMinimumSize() { return getPreferredSize(); }
		};
		row.add(label, BorderLayout.WEST);
		row.add(control, BorderLayout.CENTER);
		row.setBorder(BorderFactory.createEmptyBorder());
		return row;
	}

	static JComponent createAdvancedSection(JComponent content) {
		JComponent section = SBoxLayout.createContainer(SBoxLayout.VERTICAL, SBoxLayout.COMMON_PADDING);
		JButton toggle = new JButton("Zaawansowane +");
		content.setVisible(false);
		toggle.addActionListener(event -> {
			boolean expanded = !content.isVisible();
			content.setVisible(expanded);
			toggle.setText(expanded ? "Zaawansowane -" : "Zaawansowane +");
			SettingsTranslation.apply(section);
			section.revalidate();
			section.repaint();
		});
		section.add(toggle);
		section.add(content, games.stendhal.client.gui.layout.SLayout.EXPAND_X);
		return section;
	}

	static JCheckBox createSettingsToggle(final String parameter, boolean defaultValue, String label, String tooltip) {
		boolean selected = false;
		JCheckBox toggle = new JCheckBox(label);
		toggle.setToolTipText(tooltip);
		selected = WtWindowManager.getInstance().getPropertyBoolean(parameter, defaultValue);
		toggle.setSelected(selected);

		toggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
				WtWindowManager.getInstance().setProperty(parameter, Boolean.toString(enabled));
				WtWindowManager.getInstance().save();
			}
		});
		return toggle;
	}

}
