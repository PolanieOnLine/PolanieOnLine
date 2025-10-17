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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public class GoldenCauldronWindow extends SlotWindow {
	private static final long serialVersionUID = 1668982658239759785L;

	private final JLabel statusLabel;
	private final JButton mixButton;

	private GoldenCauldron cauldron;
	private boolean closingFromServer;

	public GoldenCauldronWindow() {
		super("golden_cauldron", 3, 2);

		statusLabel = new JLabel("Ułóż składniki w sześciu miejscach i wybierz „Mieszaj”.");
		mixButton = new JButton("Mieszaj");
		mixButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				sendCommand("mix");
			}
		});
		mixButton.setEnabled(false);

		SlotGrid grid = getSlotGrid();
		Container parent = grid.getParent();
		if (parent != null) {
			parent.remove(grid);
		}

		JComponent content = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 4);
		content.add(statusLabel);
		content.add(grid);

		JComponent buttons = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, 4);
		buttons.add(mixButton);
		content.add(buttons);

		setContent(content);
		setAcceptedTypes(EntityMap.getClass("item", null, null));

		closingFromServer = false;
	}

	@Override
	public void setSlot(final IEntity parent, final String slot) {
		super.setSlot(parent, slot);
		if (parent instanceof GoldenCauldron) {
			cauldron = (GoldenCauldron) parent;
		}
		updateState();
	}

	public void setCauldron(final GoldenCauldron entity) {
		cauldron = entity;
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
			statusLabel.setText("Ułóż składniki w sześciu miejscach i wybierz „Mieszaj”.");
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
