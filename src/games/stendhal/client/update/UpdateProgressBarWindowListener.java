/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.update;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

/**
 * Ask for confirmation and exists the JVM if the user closes the progress bar window.
 */
class UpdateProgressBarWindowListener implements WindowListener {

	@Override
	public void windowOpened(WindowEvent e) {
		// do nothing
	}

	/**
	 * Ask for confirmation and closes the JVM
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		String[] options = {UpdateLanguage.getText("Tak"), UpdateLanguage.getText("Nie")};
		int result = JOptionPane.showOptionDialog(e.getWindow(),
				UpdateLanguage.getText("Czy chcesz przerwać pobieranie?"), UpdateLanguage.getText("Potwierdzenie"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (result == JOptionPane.YES_OPTION) {
			e.getWindow().dispose();
			System.exit(1);
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// do nothing
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// do nothing
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// do nothing
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// do nothing
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// do nothing
	}
}
