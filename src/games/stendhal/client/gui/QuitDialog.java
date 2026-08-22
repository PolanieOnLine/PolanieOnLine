/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;


import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.entity.User;

@SuppressWarnings("serial") class QuitDialog {
	private static final int PADDING = 10;
	/** Quit dialog window. */
	private InternalManagedWindow quitDialog;
	private JButton settingsButton;
	private JButton changeCharacterButton;
	private JButton quitButton;

	/**
	 * Get the dialog component.
	 *
	 * @return quit dialog component
	 */
	Component getQuitDialog() {
		return quitDialog;
	}

	/**
	 * Create a new QuitDialog.
	 */
	QuitDialog() {
		quitDialog = buildQuitDialog();
		quitDialog.setVisible(false);
		quitDialog.addHierarchyBoundsListener(new ParentResizeListener());
	}

	/**
	 * Build the in-window quit dialog.
	 *
	 * @return the quit dialog
	 */
	private InternalManagedWindow buildQuitDialog() {
		// dialog contents
		JComponent content = new JComponent() { };
		content.setLayout(new GridLayout(3, 1, PADDING, PADDING));
		content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		// Limit keyboard focus handling to the dialog until the user makes some
		// decision
		content.setFocusCycleRoot(true);
		content.setFocusTraversalPolicy(new LimitingFocusTraversalPolicy());

		settingsButton = new JButton("Ustawienia");
		settingsButton.setMnemonic(KeyEvent.VK_U);
		settingsButton.addActionListener(new SettingsCB());
		content.add(settingsButton);

		changeCharacterButton = new JButton("Zmień postać");
		changeCharacterButton.setMnemonic(KeyEvent.VK_Z);
		changeCharacterButton.addActionListener(new ChangeCharacterCB());
		content.add(changeCharacterButton);

		quitButton = new JButton("Wyjdź z gry");
		quitButton.setMnemonic(KeyEvent.VK_W);
		quitButton.addActionListener(new QuitConfirmCB());
		content.add(quitButton);

		// Pack the whole thing in a managed window
		InternalManagedWindow window = new InternalManagedWindow("quit", "Sesja");
		window.setContent(content);
		window.setMinimizable(false);
		window.setHideOnClose(true);
		window.setMovable(false);

		return window;
	}

	private class ChangeCharacterCB implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent ev) {
			quitDialog.setVisible(false);
			j2DClient.get().requestCharacterChange();
		}
	}

	private class SettingsCB implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent ev) {
			quitDialog.setVisible(false);
			SlashActionRepository.get("settings").execute(null, null);
		}
	}

	private static class QuitConfirmCB implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent ev) {
			j2DClient.get().shutdown();
		}
	}

	/**
	 * Show the session dialog. Changing character is offered only when the
	 * connected server negotiated protocol support for character-session leave.
	 */
	void requestQuit(final User user) {
		/*
		 * Stop the player
		 */
		if (user != null && !user.stopped()) {
			/* User.stopMovement() executes an AutoWalkAction which will stop
			 * the character's movement and remove auto-walk setting.
			 */
			user.stopMovement();
		}

		StendhalClient client = StendhalClient.get();
		boolean canChangeCharacter = client != null && client.supportsCharacterSessionLeave();
		changeCharacterButton.setEnabled(canChangeCharacter);
		changeCharacterButton.setToolTipText(canChangeCharacter ? null
				: "Serwer nie obsługuje zmiany postaci bez ponownego logowania.");

		quitDialog.center();
		quitDialog.setVisible(true);
		settingsButton.requestFocusInWindow();
	}

	/**
	 * @return whether the session menu is currently open
	 */
	boolean isVisible() {
		return quitDialog.isVisible();
	}

	/**
	 * Hide the session menu.
	 */
	void hide() {
		quitDialog.setVisible(false);
	}

	/**
	 * For keeping the dialog centered on game screen resizes.
	 */
	private class ParentResizeListener implements HierarchyBoundsListener {
		@Override
		public void ancestorMoved(HierarchyEvent e) {
			// ignore
		}

		@Override
		public void ancestorResized(HierarchyEvent e) {
			if (quitDialog.isVisible()) {
				if (e.getChanged().equals(quitDialog.getParent())) {
					quitDialog.center();
				}
			}
		}
	}

	/**
	 * A FocusTraversalPolicy that keeps the keyboard focus within the
	 * container, instead of passing it to parent once the last component has
	 * been reached.
	 */
	private static class LimitingFocusTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
		@Override
		public Component getFirstComponent(Container container) {
			// By default we'd get the container itself.
			Component[] components = container.getComponents();
			if (components.length > 0) {
				return components[0];
			}
			return null;
		}

		@Override
		public Component getComponentBefore(Container container,
                Component component) {
			/*
			 * Jump to the actual last component instead of returning the
			 * container itself when cycling backwards from the first component.
			 */
			Component before = super.getComponentBefore(container, component);
			if (before == container) {
				before = super.getLastComponent(container);
			}
			return before;
		}
	}
}
