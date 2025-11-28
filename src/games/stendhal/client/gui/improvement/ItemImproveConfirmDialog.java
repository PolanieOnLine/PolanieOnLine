/***************************************************************************
*                      (C) Copyright 2024 - PolanieOnLine                 *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui.improvement;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JDialog.ModalityType;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.stendhal.client.gui.WindowUtils;

/**
* Confirmation dialog for a single improvement attempt.
*/
public class ItemImproveConfirmDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	ItemImproveConfirmDialog(final Window owner, final String npcName, final ItemImprovementEntry entry) {
		super(owner, "Potwierdzenie", ModalityType.APPLICATION_MODAL);
		setLayout(new BorderLayout(10, 10));

		final JLabel message = new JLabel(
		"Czy na pewno chcesz ulepszyć " + entry.getName() + "?", SwingConstants.CENTER);
		final JLabel chance = new JLabel(
		"Szansa powodzenia: " + String.format("%.2f%%", entry.getChance()), SwingConstants.CENTER);

		add(message, BorderLayout.NORTH);
		add(chance, BorderLayout.CENTER);
		add(createButtons(npcName, entry), BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(owner);
		WindowUtils.closeOnEscape(this);
	}

private JPanel createButtons(final String npcName, final ItemImprovementEntry entry) {
	final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	final JButton yes = new JButton("Tak");
	final JButton no = new JButton("Nie");

	yes.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
			ItemImprovementController.performUpgrade(npcName, entry);
			dispose();
		}
});

no.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(final ActionEvent e) {
		dispose();
	}
});

panel.add(yes);
panel.add(no);
return panel;
}
}
