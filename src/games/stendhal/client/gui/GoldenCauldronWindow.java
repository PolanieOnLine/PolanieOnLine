/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public class GoldenCauldronWindow extends SlotWindow {
	private static final long serialVersionUID = 1668982658239759785L;

	private final JLabel statusLabel;
	private final JButton mixButton;

	private GoldenCauldron cauldron;
	private boolean closingFromServer;
	private boolean slotBound;

	public GoldenCauldronWindow() {
		super("golden_cauldron", 4, 2);

                statusLabel = new JLabel("Ułóż składniki w ośmiu miejscach i wybierz \"Mieszaj\".");
                statusLabel.setHorizontalAlignment(JLabel.CENTER);
		mixButton = new JButton("Mieszaj");
		mixButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				sendCommand("mix");
			}
		});
		mixButton.setEnabled(false);

		final SlotGrid grid = getSlotGrid();
		grid.setOpaque(false);
		setSlotsLayout(4, 2);

		final JPanel gridWrapper = new JPanel(new BorderLayout());
		gridWrapper.setOpaque(false);
		gridWrapper.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));
		gridWrapper.add(statusLabel, BorderLayout.NORTH);
		gridWrapper.add(grid, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
		buttonPanel.add(mixButton, BorderLayout.CENTER);

		final JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(false);
		content.add(gridWrapper, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);
		content.setBorder(BorderFactory.createEmptyBorder(4, 4, 6, 4));

		mixButton.setPreferredSize(new Dimension(120, mixButton.getPreferredSize().height));
		setContent(content);
		setAcceptedTypes(EntityMap.getClass("item", null, null));

		closingFromServer = false;
		slotBound = false;
	}

	@Override
	protected void setSlotsLayout(final int width, final int height) {
		getSlotGrid().setSlotsLayout(width, height);
		revalidate();
		repaint();
	}

	@Override
	public void setSlot(final IEntity parent, final String slot) {
		if (parent instanceof GoldenCauldron) {
			cauldron = (GoldenCauldron) parent;
			slotBound = true;
			super.setSlot(parent, GoldenCauldron.CONTENT_SLOT);
		} else {
			slotBound = (slot != null);
			super.setSlot(parent, slot);
			cauldron = null;
		}
		updateState();
	}

	public void setCauldron(final GoldenCauldron entity) {
		cauldron = entity;
		if (!slotBound && entity != null) {
			super.setSlot(entity, GoldenCauldron.CONTENT_SLOT);
			slotBound = true;
		}
		updateState();
	}

	public void updateBrewer() {
		updateState();
	}

	public void closeFromServer() {
		closingFromServer = true;
		close();
	}

	@Override
	public void close() {
		if (!closingFromServer && cauldron != null && cauldron.isOpen()) {
			sendCommand("close");
		}
		super.close();
		cauldron = null;
		mixButton.setEnabled(false);
		closingFromServer = false;
		slotBound = false;
	}

	private void updateState() {
		if (cauldron == null || !cauldron.isOpen()) {
			statusLabel.setText("Kocioł jest nieaktywny.");
			mixButton.setEnabled(false);
			return;
		}

		final String brewer = cauldron.getBrewer();
		final String user = User.getCharacterName();

		if ((brewer != null) && (user == null || !brewer.equalsIgnoreCase(user))) {
			statusLabel.setText("Kocioł obsługuje obecnie: " + brewer + ".");
			mixButton.setEnabled(false);
		} else {
			statusLabel.setText("Ułóż składniki w ośmiu miejscach i wybierz \"Mieszaj\".");
			mixButton.setEnabled(true);
		}
	}

	private void sendCommand(final String command) {
		if (cauldron == null) {
			return;
		}

		final RPAction action = new RPAction();
		action.put("type", "golden_cauldron");
		action.put("action", command);

		final RPObject base = cauldron.getRPObject().getBaseContainer();
		if (base != null && base.has("zoneid")) {
			action.put("zone", base.get("zoneid"));
		}

		action.put(Actions.TARGET_PATH, cauldron.getPath());
		StendhalClient.get().send(action);
	}
}
