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
package games.stendhal.client.actions;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;

/**
 * Display command usage. Eventually replace this with ChatCommand.usage().
 */
class HelpAction implements SlashAction {

	/**
	 * Execute a chat command.
	 *
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 *
	 * @return <code>true</code> if was handled.
	 */
	@Override
	public boolean execute(final String[] params, final String remainder) {
		final String[] lines = {
				"Aby dowiedzieć się więcej odwiedź #http://www.polskagra.net",
				"Oto najczęściej używane komendy:",
				"* ROZMOWA: ",
				"- /me <akcja> \t\tPokazuje wiadomość o tym co teraz robisz",
				"- /tell <wojownik> <wiadomość> \tWysyła prywatną wiadomość do <wojownik>",
				"- /answer <wiadomość> \t\tWysyła prywatną wiadomość do wojownika, który ostatnio przysłał Tobie wiadomość",
				"- // <wiadomość> \t\tWysyła wiadomość do wojownika któremu ostatnio wysłałeś wiadomość",
				"- /storemessage <wojownik> <wiadomość> Zostawia do dostarczenia prywatną wiadomość rozłączonym wojownikom",
				"- /who \t\tWyświetla listę wojowników którzy są aktualnie zalogowani",
				"- /where <wojownik> \t\tPodaje położenie <wojownik>",
				"- /sentence <tekst> \t\tNapisz zdanie, które pojawi się na stronie WWW.",
				"* SUPPORT:",
				"- /support <wiadomość> \tZapytaj administratora w celu uzyskania pomocy. UWAGA! Przed użyciem zapoznaj się z zasadami jej korzystania.",
				"- /faq \t\tOtwiera FAQ gry.",
				"* PRZEDMIOTY:",
				"- /drop [ilość] <przedmiot>\tOdkłada określoną ilość danego przedmiotu",
				"- /markscroll <tekst> \tZaznacz swój empty scroll i nadaj mu własną etykietę",
				"* PRZYJACIELE I WROGOWIE:",
				"- /add <wojownik> \t\tDodaje <wojownik> do twojej listy przyjaciół",
				"- /remove <wojownik> \tUsuwa <wojownik> z twojej listy przyjaciół",
				"- /ignore <wojownik> [<minuty>|*|- [<powód>]] \tDodaj <wojownik> do listy ignorowanych",
				"- /ignore \t\t Sprawdź kto jest na twojej liście ignorowanych",
				"- /unignore <wojownik> \tUsuwa <wojownik> z listy ignorowanych",
				"* STATUS:",
				"- /away <wiadomość> \tUstawia wiadomość dla statusu oddalony",
				"- /away \tUsuwa status oddalony",
				"- /grumpy <wiadomość> \t Ustawiasz wiadomość i ignorujesz wszystkich graczy spoza kręgu przyjaciół.",
				"- /grumpy \tUsuwa status niedostępności",
				"- /name <zwierzątko> <imię> \t\tNadaje imię twojemu zwierzątku",
				"- /profile [<nazwa>] \t Otwiera profil postaci",
				"* USTAWIENIA GRACZA:",
				"- /clickmode <0|1> \t\tWyłącza lub włącza możliwość podwójnego kliknięcia przy jednokrotnym naciśnięciu lewego przycisku myszy",
				"- /walk \tWłącza automatyczne chodzenie.",
				"- /stopwalk \tWyłącza automatyczne chodzenie.",
				"- /movecont \tWłącza lub wyłącza ciągły ruch (pozwala graczom kontynuować chodzenie po zmianie mapy lub przejścia przez portal bez puszczania klawisza kierunkowego).",
				"* USTAWIENIA DŹWIĘKU:",
				"- /mute \t\tUcisza lub przywraca dźwięki",
				"- /volume \t\tWylicza lub ustawia poziom głośności dla dźwięków i muzyki",
				"* RÓŻNE:",
				"- /info \t\tPokazuje aktualny czas z serwera",
				"- /clear \tWyczyść chat.",
				"- /help \tWyświetla podstawowe komendy."
		};

		for (final String line : lines) {
			ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(line, NotificationType.CLIENT));
		}

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMinimumParameters() {
		return 0;
	}
}
