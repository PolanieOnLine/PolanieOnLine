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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;
import games.stendhal.common.messages.SupportMessageTemplatesFactory;

/**
 * Display command usage. Eventually replace this with ChatCommand.usage().
 */
class GMHelpAction implements SlashAction {

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
	    List<String> lines;
		if (params[0] == null) {
			lines = Arrays.asList(
				"Aby dowiedzieć się więcej odwiedź #http://www.polskagra.net",
				"Oto najczęściej używane komendy przez GA-ów i GM-ów:",
				"- /gmhelp [alter|script|support]  \t więcej informacji o alter, script lub skrótach supportanswer", 
				"- /supportanswer <wojownik> <wiadomość> \t Wysyła odpowiedź na pytanie. Zastąp <wiadomość> skrótami $faq, $faqpvp, $knownbug, $bugstracker, $rules i $abuse jeżeli utworzone.",
				"- /adminnote <wojownik> <notatka> \t\tUmożliwia aktualizację powodu zamknięcia wybranego wojownika",
				"- /adminlevel <wojownik> [<nowypoziom>] \t\tWyświetla lub ustawia adminlevel dla <wojownik>",
				"- /tellall <wiadomość> \t\tWysyła prywatną wiadomośść do wszystkich zalogowanych wojowników",
				"- /jail <wojownik> <minuty> <powód>\t\tWsadza wojownika na określoną ilość czasu",
				"- /jailreport [<wojownik>]\t\tWyśwetla listę aresztowanych wojowników i powódy zatrzymiania",
				"- /gag <wojownik> <minuty> <powód>\t\tUcisza wojownika na określony czas (wojownik do nikogo nie może wysłać wiadomości)",
				"- /ban <wojownik> <godziny> <powód> \t\tWojownik ma całkowity zakaz logowania się na serwer gry lub stronę na określoną ilość godzin (-1 dożywotnia blokada).",
				"- /script <nazwaskryptu> \t\tŁaduje (lub przeładowuje) skrypt na serwerze",
				"- /teleport <wojownik> <obszar> <x> <y> \tTeleportuje określonego <wojownik> do podanego miejsca",
				"- /teleportto <wojownik> \t\tTeleportujesz siebie w pobliże określonego wojownika",
				"- /teleclickmode \t\t\t Teleportuje Ciebie do miejsca gdzie podwójnie nacisnąłeś lewy przycisk myszy",
				"- /ghostmode \t\t\t Powoduje, że stajesz się niewidzialny i nietykalny",
				"- /alter <wojownik> <atrybut> <tryb> <ilość> \tZmienia bezzwłocznie <atrybut> dla <wojownik> w podanej ilości. <tryb> może być ADD, SUB, SET lub UNSET. Dowiedz się więcej wpisując /gmhelp_alter",
				"- /altercreature <id> name;atk;def;hp;xp \tZmienia potworowi wartości. Użyj, aby zastąpić domyślne wartości. Użyteczne w plagach",
				"- /alterquest <wojownik> <nazwa zadania> <stan> \tAktualizuje <nazwa zadania> dla <wojownik> nadając <stan>",
				"- /summon <potwór|przedmiot> <x> <y> \tPrzywołuje określony przedmiot lub potwora w podanym miejscu <x>, <y> w aktualnej strefie",
				"- /summonat <wojownik> <slot> <ilość> <przedmiot> Przywołuje określony przedmiot do określonego slota u <wojownik>. <ilość> domyślnie wynosi 1 jeżeli nie została określona",
				"- /invisible \t\t\tWłącza lub wyłącza niewidzialność dla potworów",
				"- /inspect <wojownik> \t\t\tPokazuje kompletne informacje o <wojownik>",
				"- /destroy <obiekt> \t\t\tNiszczy obiekt");
		} else if ((params.length == 1) && (params[0] != null)) {
			if ("alter".equals(params[0])) {
				lines = Arrays.asList(
					"/alter <wojownik> <atrybut> <tryb> <wartość> \tStatystyki altera <atrybut> <wojownik> przez podaną ilość; <tryb> może być ADD, SUB, SET lub UNSET",
					"Przykłady <atrybut>: atk, def, base_hp, hp, atk_xp, def_xp, xp, outfit",
					"Gdy zmieniamy 'outfit' to powinieneś użyć trybu SET i wprowadzić 10-cyfrowy numer; pierwsze 3 miejsca to ustawienia 'włosów' pożniej 2 kolejne miejsca to 'głowa', następne 3 miejsca to 'wygląd', a ostatnie 2 miejsca to 'ciało'",
					"Na przykład: #'/alter testplayer outfit set 151010301'",
					"<testplayer> będzie wyglądał jak danter" );
			} else if ("script".equals(params[0])) {
				lines = Arrays.asList(
					"użyj: /script [-list|-load|-unload|-execute] [parametry]",
					"-list : pokazuje dostępne skrypty. W tym trybie mogą być dostępne dodatkowe tryby dla filtrowania plików przy użyciu ('*' i '?', na przykład \"*.class\" tylko dla skryptów java).",
					"-load : ładuje skrypt z pierwszymi parametrami nazwy pliku.",
					"-unload : rozładowuje skrypt z pierwszymi parametrami nazwy pliku dla serwera",
					"-execute : wykonuje wybrany skrypt.",
					"",	
					"Wszystkie skrypty uruchamia się przez użycie: /script nazwaskryptu [parametry]. Po użycie skryptu możesz usunąć jego ślady przy pomocy /script -unload nazwaskryptu na przykład usunie to wszystkie przywołane potwory. Najlepszą ćwiczeniem jest używanie jej za każdym razem, gdy już nie chcemy korzystać ze skryptu.",
					"#/script #AdminMaker.class : Tylko dla serwerów testowych. Przywołuje administratora do testowania.",
					"#/script #AdminSign.class #obszar #x #y #tekst : Tworzy AdminSign na podanym obszarze obszarze w miejscu (x,y) z napisem. Aby postawić znak obok siebie należy wpisać /script AdminSign.class - - - tekst.",
					"#/script #AlterQuest.class #wojownik #nazwazadania #stan : Zmienia zadanie wojownikowi na wybrany stan. Zignoruj #stan, aby usunąć zadanie.",
					"#/script #DeepInspect.class #wojownik : Szczegółowsze sprawdzanie wojownika i wszystkie jego/jej przedmioty.",
					"#/script #DropPlayerItems.class #wojownik #[ilość] #przedmiot : Usuwa określoną ilość przedmiotów wojownikowi o ile ma w plecaku lub na sobie.",
					"#/script #EntitySearch.class #nieodnawiające : Pokazuje lokalizacje wszystkich potworów, które zostały przywołane przez GM lub GA, potwory z deathmatch itd.",
					"#/script #FixDM.class #wojownik : ustawia wojownikowi slot DeathMatch na stan victory.",
					"#/script #ListNPCs.class : wyświetla listę wszystkich npców i ich pozycje.",
					"#/script #LogoutAllPlayers.class : wylogowuje wszystkich wojowników z gry prócz osoby używającej tego skryptu oraz postmana.",
					"#/script #LogoutPlayer.class #wojownik : wylogowuje wojownika z gry.",
					"#/script #NPCShout.class #npc #tekst : NPC krzyczy podany tekst.",
					"#/script #NPCShoutZone.class #npc #obszar #tekst : NPC krzyczy  podany tekst wojownikom w podanym obszarze. Użyj - na swoim obszarze, aby zadziałało w obszarze na którym aktualnie się znajdujesz.",
					"#/script #Plague.class #1 #potwór : przywołuje plagę potworów wokół Ciebie.",
					"#/script #WhereWho.class : Lista pozycji wojowników, którzy są w grze",
					"#/script #Maria.class : Przywołuje Marię, która sprzedaje food&drinks. Nie zapomnij o -unload, gdy już skończysz.",
					"#/script #ServerReset.class : używaj tylko w nagłych przypadkach, aby zamknąć serwer. Jeżeli to możliwe to ostrzeż wojowników, aby się wylogowali i daj im trochę czasu.",
					"#/script #ResetSlot.class #wojownik #slot : Resetuje wybrany slottaki jak !kills czy !quests. Używaj tylko do debugowania."
					);

			} else if ("support".equals(params[0])) {
				lines = buildHelpSupportResponse();
			} else {
				return false;
			}
		} else {
			return false;
		}
		for (final String line : lines) {
			ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(line, NotificationType.CLIENT));
		}

		return true;
	}

	private List<String> buildHelpSupportResponse() {
		List<String> lines = new LinkedList<String>();
		Map<String, String> templates = new SupportMessageTemplatesFactory().getTemplates();
		for (Entry<String, String> template : templates.entrySet()) {
			lines.add(template.getKey() + " - " + template.getValue());
		}
		return lines;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMaximumParameters() {
		return 1;
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
