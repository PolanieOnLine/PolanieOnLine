/***************************************************************************
 *                 (C) Copyright 2024 - PolanieOnLine                      *
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import games.stendhal.client.ChatOptions;
import games.stendhal.client.ChatOptions.Option;
import games.stendhal.client.gui.chattext.ChatTextController;
import games.stendhal.client.gui.layout.SBoxLayout;

class ChatOptionsDialog {
	private static final int PADDING = 2;
	private static final Insets BUTTON_MARGIN = new Insets(1, 4, 1, 4);
	private static final Dimension MIN_SIZE = new Dimension(180, 40);

	private final InternalManagedWindow dialog;
	private final JComponent buttonContainer;

	ChatOptionsDialog() {
		dialog = buildDialog();
		buttonContainer = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, PADDING);
		dialog.setContent(buildContent());
		dialog.setVisible(false);
	}

	Component getChatOptionsDialog() {
		return dialog;
	}

	void showDialog() {
		if (ChatOptions.getOptions().isEmpty()) {
			return;
		}
		refreshOptions();
		dialog.center();
		dialog.setVisible(true);
	}

	void closeDialog() {
		dialog.setVisible(false);
	}

	boolean isVisible() {
		return dialog.isVisible();
	}

	void refreshOptions() {
		buttonContainer.removeAll();
		List<Option> options = ChatOptions.getOptions();
		if (options.isEmpty()) {
			dialog.setVisible(false);
			return;
		}
		for (Option option : options) {
			buttonContainer.add(createButton(option));
		}
		updateTitle();
		buttonContainer.revalidate();
		buttonContainer.repaint();
	}

	private JComponent buildContent() {
		JComponent content = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, PADDING);
		content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		content.add(buttonContainer);
		return content;
	}

	private JButton createButton(final Option option) {
		JButton button = new JButton(option.getLabel());
		button.setMargin(BUTTON_MARGIN);
		button.setFocusable(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ev) {
				handleOption(option);
			}
		});
		return button;
	}

	private void handleOption(final Option option) {
		if (option == null) {
			return;
		}
		String label = option.getLabel();
		String trigger = option.getTrigger();
		if (label != null && label.endsWith("...")) {
			if (trigger != null && !trigger.isEmpty()) {
				ChatTextController.get().setChatLine(trigger + " ");
			}
			return;
		}
		ChatOptions.sendKeyword(trigger);
	}

	private void updateTitle() {
		String attending = ChatOptions.getAttendingNPC();
		if (attending == null || attending.isEmpty()) {
			dialog.setTitle("Opcje rozmowy");
		} else {
			dialog.setTitle("Opcje rozmowy: " + attending);
		}
	}

	private InternalManagedWindow buildDialog() {
		InternalManagedWindow window = new InternalManagedWindow("chatoptions", "Opcje rozmowy");
		window.setHideOnClose(true);
		window.setMinimizable(true);
		window.setMovable(true);
		window.setMinimumSize(MIN_SIZE);
		return window;
	}
}
