package games.stendhal.client.actions;

import games.stendhal.client.gui.AdventCalendarDialog;

/** Opens the calendar without waiting for a server reply to create its dialog. */
final class AdventCalendarAction implements SlashAction {

	@Override
	public boolean execute(final String[] params, final String remainder) {
		AdventCalendarDialog.open();
		return true;
	}

	@Override
	public int getMaximumParameters() {
		return 0;
	}

	@Override
	public int getMinimumParameters() {
		return 0;
	}
}
