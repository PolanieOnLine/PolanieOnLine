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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/**
 * Container window dedicated to Draconia's golden cauldron.
 */
public class GoldenCauldronWindow extends SlotWindow {
	private static final long serialVersionUID = 920640738155432528L;

	private final JLabel statusLabel;
	private final JButton mixButton;

	private GoldenCauldron cauldron;
	private boolean closingFromServer;

	/**
	 * Create the cauldron window with an eight slot grid and a visible mix button.
	 */
	public GoldenCauldronWindow() {
		super("golden_cauldron", 4, 2);

		statusLabel = new JLabel("Przygotuj osiem składników i wybierz \"Mieszaj\".");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

		mixButton = new JButton("Mieszaj");
		mixButton.setPreferredSize(new Dimension(120, mixButton.getPreferredSize().height));
		mixButton.setEnabled(false);
		mixButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				sendCommand("mix");
			}
		});

		final SlotGrid grid = getSlotGrid();
		grid.setOpaque(false);

		final JPanel gridPanel = new JPanel(new BorderLayout());
		gridPanel.setOpaque(false);
		gridPanel.add(statusLabel, BorderLayout.NORTH);
		gridPanel.add(grid, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		buttonPanel.add(mixButton);

		final JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(false);
		content.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
		content.add(gridPanel, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);

		setContent(content);
		setAcceptedTypes(EntityMap.getClass("item", null, null));

		closingFromServer = false;
	}

	@Override
	public void setSlot(final IEntity parent, final String slot) {
		final String targetSlot;
		if (parent instanceof GoldenCauldron) {
			targetSlot = GoldenCauldron.CONTENT_SLOT;
			cauldron = (GoldenCauldron) parent;
		} else {
			targetSlot = slot;
			cauldron = null;
		}
		super.setSlot(parent, targetSlot);
		updateState();
	}

	/**
	 * Bind the window to the provided cauldron entity.
	 *
	 * @param entity cauldron entity displayed by this window
	 */
	public void setCauldron(final GoldenCauldron entity) {
		cauldron = entity;
		if (entity != null) {
			final IEntity parent = entity;
			super.setSlot(parent, GoldenCauldron.CONTENT_SLOT);
		}
		updateState();
	}

	/**
	 * Refresh the button state after the brewer property changes.
	 */
	public void updateBrewer() {
		updateState();
	}

	/**
	 * Close the window because the server has closed the cauldron.
	 */
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
		mixButton.setEnabled(false);
		cauldron = null;
		closingFromServer = false;
	}

	private void updateState() {
		if (cauldron == null || !cauldron.isOpen()) {
			statusLabel.setText("Kocioł jest zamknięty.");
			mixButton.setEnabled(false);
			return;
		}

		final String brewer = cauldron.getBrewer();
		final String user = User.getCharacterName();

		if (user == null) {
			statusLabel.setText("Nie znam twojego imienia – zaloguj się ponownie.");
			mixButton.setEnabled(false);
		} else if (brewer != null && !brewer.equalsIgnoreCase(user)) {
			statusLabel.setText("Kocioł obsługuje teraz: " + brewer + ".");
			mixButton.setEnabled(false);
		} else {
			statusLabel.setText("Przygotuj osiem składników i wybierz \"Mieszaj\".");
			mixButton.setEnabled(true);
		}
	}

	private void sendCommand(final String actionCommand) {
		if (cauldron == null) {
			return;
		}

		final RPAction action = new RPAction();
		action.put("type", "golden_cauldron");
		action.put("action", actionCommand);
		action.put(Actions.TARGET_PATH, cauldron.getPath());

		final RPObject base = cauldron.getRPObject().getBaseContainer();
		if (base != null && base.has("zoneid")) {
			action.put("zone", base.get("zoneid"));
		}

		StendhalClient.get().send(action);
	}
}
