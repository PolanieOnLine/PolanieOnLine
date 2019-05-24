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
package games.stendhal.server.core.events;

/**
 * Event types used in the new Zone notifier.
 *
 * @author kymara (based on TutorialEventType by hendrik)
 */
public enum ZoneEventType {

	VISIT_SUB1_SEMOS_CATACOMBS(
			"Wrzaski i płacz czuć w tych strasznych katakumbach ..."),
	VISIT_SUB2_SEMOS_CATACOMBS(
			"Gdy zagłebiasz się w głąb katakumb twoje złe przeczucia narastają.Dostrzegając śmiertelne kolce przysięgasz sobie, że będziesz bardziej z nimi ostrożny."),
	VISIT_KIKAREUKIN_CAVE(
			"Po wyjściu z portalu twoja głowa wiruje. Zbijając się wysoko i mijając chmury i ptaki zostałeś wessany na latające wyspy połączone mostami. Brnąc przez warstwy skał ostatecznie wylądąwałeś w sieci jaskiń."),
	VISIT_KANMARARN_PRISON(
			"UCIECZKA Z WIĘZIENIA! Natknąłeś się na włamanie. Wygląda na to, że duergary włamały się tutaj, aby uwolnić liderów i bohaterów z więzeinia krasnali."),
	VISIT_IMPERIAL_CAVES(
			"Z daleka słychać komendy i rozkazy. Możesz sobie tylko wyobrazić, że zbliża się wojsko lub coś w tym rodzaju. Obawiając się słyszysz jakieś bardzo, bardzo ciężkie kroki."),
	VISIT_MAGIC_CITY_N(
			"Zwiedzając dalej przechodzą Cię ciarki. Zdecydowanie znajduje się tutaj magia."),
	VISIT_MAGIC_CITY(
			"Wyczuwasz silną obecność magi. Czy to możliwe, że w pobliżu są czarodzieje lub jakiś silny czar?"),
	VISIT_SEMOS_CAVES(
			"Ziemia w tej pieczarze jest pełna śladów GIGANTÓW! Słabsi podróżnicy nie mają tutaj przyszłości. Zawróć i uciekaj!"),
	VISIT_ADOS_CASTLE(
			"Wyczuwasz wielką tragedię, która miała tutaj miejsce. Zamek musi być opanowany przez złe potwory skoro odgłosy ich ostatnich ofiar dzwonią w twoich uszach. Mądrze byłoby trzymać się stąd z daleka.");

	private String message;

	/**
	 * create a new ZoneEventType.
	 *
	 * @param message
	 *            human readable message
	 */
	private ZoneEventType(final String message) {
		this.message = message;
	}

	/**
	 * get the descriptive message.
	 *
	 * @return message
	 */
	String getMessage() {
		return message;
	}
}
