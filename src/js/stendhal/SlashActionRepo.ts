/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var marauroa: any;
declare var stendhal: any;

import { singletons } from "./SingletonRepo";

import { AboutAction } from "./action/AboutAction";
import { DebugAction } from "./action/DebugAction";
import { OpenWebsiteAction } from "./action/OpenWebsiteAction";
import { ProgressStatusAction } from "./action/ProgressStatusAction";
import { ReTellAction } from "./action/ReTellAction";
import { ScreenCaptureAction } from "./action/ScreenCaptureAction";
import { SettingsAction } from "./action/SettingsAction";
import { SlashActionImpl } from "./action/SlashAction";
import { TellAction } from "./action/TellAction";

import { ui } from "./ui/UI";
import { UIComponentEnum } from "./ui/UIComponentEnum";

import { ChatLogComponent } from "./ui/component/ChatLogComponent";

import { Chat } from "./util/Chat";
import { Debug } from "./util/Debug";


/**
 * Action type representation.
 */
interface Action {
	[key: string]: string;
	type: string;
}

/**
 * Registered slash actions.
 */
export class SlashActionRepo {
	[index: string]: any;

	/** Singleton instance. */
	private static instance: SlashActionRepo;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): SlashActionRepo {
		if (!SlashActionRepo.instance) {
			SlashActionRepo.instance = new SlashActionRepo();
		}
		return SlashActionRepo.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Forwards action information to server.
	 *
	 * @param action {SlashActionRepo.Action}
	 *   Action object.
	 */
	private sendAction(action: Action) {
		marauroa.clientFramework.sendAction(action);
	}

	/**
	 * Retrieves registered types excluding generic defaults & aliases.
	 *
	 * FIXME: not detecting some property names such as "manual"
	 *
	 * @return {string[]}
	 *   List of unique type names.
	 */
	private getTypes(): string[] {
		// type names
		const types: string[] = [];
		// excludes including alias duplicates
		const actions: SlashActionImpl[] = [this["_default"], this["debug"]];
		for (const t of Object.keys(this)) {
			const action = this[t];
			if (actions.indexOf(action) > -1) {
				continue;
			}
			actions.push(action);
			types.push(t);
		}
		return types;
	}

	/**
	 * Retrieves help data for standard user actions.
	 *
	 * @return {any}
	 */
	private getUserHelpData(): any {
		const grouping: {[index: string]: any} = {
			"CHATTING": [
				"chat",
				"me",
				"tell",
				"answer",
				"/",
				"p",
				"storemessage",
				{
					type: "who",
					getHelp: function(): string[] {
						return ["", "Lista wszystkich graczy aktualnie online."];
					}
				},
				{
					type: "where",
					getHelp: function(): string[] {
						return ["[<gracz>]", "Pokaż bieżącą lokalizację #gracza."];
					}
				},
				"sentence"
			],
			"TOOLS": [
				"progressstatus",
				"screenshot",
				//"screencap",
				"atlas",
				"beginnersguide"
			],
			"SUPPORT": [
				"support",
				{
					type: "faq",
					getHelp: function(): string[] {
						return ["", "Otwórz stronę FAQ wiki Stendhal w przeglądarce."];
					}
				}
			],
			"ITEM MANIPULATION": [
				"drop",
				{
					type: "markscroll",
					getHelp: function(): string[] {
						return ["<tekst>", "Oznacz swój pusty zwój i dodaj etykietę #tekstu."];
					}
				}
			],
			"BUDDIES AND ENEMIES": [
				"add",
				"remove",
				{type: "ignore", sparams: "<gracz> [minuty|*|- [powód...]]"},
				"ignore",
				{
					type: "unignore",
					getHelp: function(): string[] {
						return ["<gracz>", "Usuń #gracza z twojej listy ignorów."];
					}
				}
			],
			"STATUS": [
				{type: "away", sparams: "<wiadomość>"},
				"away",
				{type: "grumpy", sparams: "<wiadomość>"},
				"grumpy",
				{
					type: "name",
					getHelp: function(): string[] {
						return ["<zwierzak> <nazwa>", "Nadaj imię twojemu zwierzakowi."];
					}
				},
				"profile"
			],
			"PLAYER CONTROL": [
				//"clickmode", // Switches between single click mode and double click mode.
				"walk",
				"stopwalk",
				"movecont"
			],
			"CLIENT SETTINGS": [
				"settings",
				"mute",
				{type: "volume", sparams: "<warstwa> <wartość>"},
				"volume"
			],
			"MISC": [
				"about",
				{
					type: "info",
					getHelp: function(): string[] {
						return ["", "Sprawdź, która jest aktualnie godzina na serwerze."];
					}
				},
				"clear",
				"help",
				{
					type: "removedetail",
					getHelp: function(): string[] {
						return [
							"",
							"Usuwa warstwę detaliczną (t.j. balonik, parasol, itd.) z postaci. #OSTRZEŻENIE:"
									+ " Nie może zostać przywrócone."
						];
					}
				},
				"emojilist",
				{type: "group", sparams: "invite <gracz>"},
				{type: "group", sparams: "join <gracz"},
				{type: "group", sparams: "leader <gracz>"},
				{type: "group", sparams: "lootmode single|shared"},
				{type: "group", sparams: "kick <gracz>"},
				{type: "group", sparams: "part"},
				{type: "group", sparams: "status"}
			]
		};

		if (Debug.isActive("screencap")) {
			grouping["TOOLS"].push("screencap");
		}

		return {
			info: [
				"Aby uzyskać szczegółowe informacje, odwiedź #https://s1.polanieonline.eu/wiki/PolanieOnLine_Przewodnik",
				"Oto najczęściej używane polecenia:"
			],
			grouping: grouping
		}
	}

	/**
	 * Retrieves help data for game master actions.
	 *
	 * @return {any}
	 */
	private getGMHelpData(): any {
		const grouping: {[index: string]: any} = {
			"GENERAL": [
				{type: "gmhelp", sparams: "[alter|script|support]"},
				"gmhelp",
				"adminnote",
				{
					type: "inspect",
					getHelp: function(): string[] {
						return ["<gracz>", "Pokaż pełne szczegóły dotyczące #gracza."];
					}
				},
				"inspectkill",
				"inspectquest",
				{
					type: "script",
					getHelp: function(): string[] {
						return [
							"<scriptname>",
							"Załaduj (lub przeładuj) skrypt na serwerze. Szczegóły znajdziesz w #/gmhelp #script."
						];
					}
				}
			],
			"CHATTING": [
				"supportanswer",
				"tellall"
			],
			"PLAYER CONTROL": [
				"teleportto",
				{
					type: "teleclickmode",
					getHelp: function(): string[] {
						return ["", "Umożliwia teleportację do miejsca, w które kliknięto dwukrotnie."];
					}
				},
				{
					type: "ghostmode",
					getHelp: function(): string[] {
						return ["", "Sprawia, że ​​stajesz się niewidzialny i nieuchwytny."];
					}
				},
				{
					type: "invisible",
					getHelp: function(): string[] {
						return ["", "Włącza lub wyłącza niewidzialność dla stworzeń."];
					}
				}
			],
			"ENTITY MANIPULATION": [
				"adminlevel",
				"jail",
				"gag",
				"ban",
				"teleport",
				"alter",
				"altercreature",
				"alterkill",
				"alterquest",
				{type: "summon", sparams: "<stwór>|<przedmiot> [<x> <y>]"},
				{type: "summon", sparams: "<przedmiot> [<x> <y>] [ilość]"},
				"summonat",
				{
					type: "destroy",
					getHelp: function(): string[] {
						return ["<byt>", "Niszczy byt całkowicie."];
					}
				}
			],
			"MISC": [
				{
					type: "jailreport",
					getHelp: function(): string[] {
						return ["[<gracz>]", "Wymień uwięzionych graczy i ich wyroki."];
					}
				}
			]
		};
		return {
			info: [
				"Aby uzyskać szczegółowe informacje, odwiedź #https://s1.polanieonline.eu/wiki/PolanieOnLine:Administrowanie",
				"Oto najczęściej używane polecenia GM:"
			],
			grouping: grouping
		};
	}

	/**
	 * Compiles help information for registered actions.
	 *
	 * TODO: cache compiled help info
	 *
	 * @param gm {boolean}
	 *   Pull info for GM help commands instead of standard user help (default: `false`).
	 * @return {string[]}
	 *   Help info string array.
	 */
	private getHelp(gm=false): string[] {
		let help: any;
		let stripHelp: any;
		if (gm) {
			help = this.getGMHelpData();
			stripHelp = this.getUserHelpData();
		} else {
			help = this.getUserHelpData();
			stripHelp = this.getGMHelpData();
		}

		const types = this.getTypes();
		// remove types not available for this help set
		for (const gname in stripHelp.grouping) {
			for (let t of stripHelp.grouping[gname]) {
				if (typeof(t) !== "string") {
					t = t.type;
				}
				types.splice(types.indexOf(t), 1);
			}
		}

		const unavailable: string[] = [];
		for (const gname in help.grouping) {
			// add spacing for clarity
			help.info.push("&nbsp;");
			help.info.push(gname + ":");
			for (let t of help.grouping[gname]) {
				// underscore command line
				let actionHelp = ["- §'/"];
				let action: any;
				if (typeof(t) === "string") {
					actionHelp[0] += t;
					action = this[t];
				} else {
					actionHelp[0] += t.type;
					action = t || this[t.type];
					action.getHelp = action.getHelp || this[t.type].getHelp;
					t = t.type;
				}

				const typeIndex = types.indexOf(t);
				if (!action || !action.getHelp) {
					unavailable.push(t);
					actionHelp[0] += "'";
					help.info.push(actionHelp[0]);
					if (typeIndex > -1) {
						types.splice(typeIndex, 1);
					}
					continue;
				}
				const helpTemp = action.getHelp(action.sparams);
				if (helpTemp && helpTemp.length) {
					if (helpTemp[0]) {
						actionHelp[0] += " " + helpTemp[0];
					}
					helpTemp.splice(0, 1);
					for (const li of helpTemp) {
						actionHelp.push("&nbsp;&nbsp;" + li);
					}
				} else {
					unavailable.push(t);
				}
				if (action.aliases) {
					actionHelp.push("&nbsp;&nbsp;Aliases: " + action.aliases.join(", "));
				}
				actionHelp[0] += "'";
				help.info = [...help.info, ...actionHelp];
				if (typeIndex > -1) {
					types.splice(typeIndex, 1);
				}
			}
		}
		for (const t of types) {
			if (unavailable.indexOf(t) < 0) {
				unavailable.push(t);
			}
		}
		if (unavailable.length) {
			console.warn("help information is unavailable for actions:", unavailable.join(", "));
		}
		return help.info;
	}

	"help": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const msg = this.getHelp();
			for (var i = 0; i < msg.length; i++) {
				Chat.log("info", msg[i]);
			}
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Pokaż komunikat pomocy."];
		}
	};

	"gmhelp": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			var msg = null;
			if (params[0] == null) {
				msg = this.getHelp(true);
			} else if ((params.length == 1) && (params[0] != null)) {
				if ("alter" == params[0]) {
					msg = [
						"/alter <gracz> <atrybut> <tryb> <wartość>",
						"\t\tZmień statystykę <atrybut> gracza <gracz> o podaną wartość; <tryb> może być ADD, SUB, SET lub UNSET.",
						"\t\t- Przykłady atrybutów: atk, def, base_hp, hp, atk_xp, def_xp, xp, outfit",
						"\t\t- Modyfikując 'outfit', należy użyć trybu SET i podać 8-cyfrową liczbę; pierwsze 2 cyfry oznaczają 'włosy', następnie 'głowę', 'strój' i 'ciało'.",
						"\t\t  Przykład: #'/alter testplayer outfit set 12109901'",
						"\t\t  Sprawi to, że <testplayer> będzie wyglądać jak danter"
					];
				} else if ("script" == params[0]) {
					msg = [
						"Użycie: /script [-list|-load|-unload|-execute] [parametry]",
						"\t-list : pokazuje dostępne skrypty. W tym trybie można podać opcjonalny parametr do filtrowania nazw plików, używając znanych symboli wieloznacznych ('*' i '?', np. \"*.class\" dla skryptów Javy).",
						"\t-load : ładuje skrypt o nazwie podanej jako pierwszy parametr.",
						"\t-unload : usuwa z serwera skrypt o nazwie z pierwszego parametru.",
						"\t-execute : uruchamia wybrany skrypt.",
						"",
						"Wszystkie skrypty uruchamia się komendą: /script nazwaskryptu [parametry]. Po uruchomieniu można usunąć jego ślady poleceniem /script -unload nazwaskryptu, co np. usunie przywołane stworzenia. To dobra praktyka po użyciu skryptów do tworzenia rajdów.",
						"#/script #AdminMaker.class : Tylko dla serwerów testowych — przywołuje NPC adminmakera do testów.",
						"#/script #AdminSign.class #strefa #x #y #tekst : Tworzy znak administracyjny (AdminSign) w strefie podanych współrzędnych z tekstem. Aby postawić go obok siebie, wpisz: /script AdminSign.class - - - tekst.",
						"#/script #AlterQuest.class #gracz #nazwa_questa #stan : Ustawia stan questa u gracza. Pominięcie #stan usuwa questa.",
						"#/script #DeepInspect.class #gracz : Szczegółowo analizuje gracza i wszystkie jego przedmioty.",
						"#/script #DropPlayerItems.class #gracz #[ilość] #przedmiot : Upuszcza określoną ilość przedmiotów z ekwipunku gracza (jeśli są w torbie lub założone).",
						"#/script #EntitySearch.class #nonrespawn : Pokazuje lokalizacje wszystkich stworzeń, które się nie odradzają (np. przywołane przez GM-a, z Deathmatcha itp.).",
						"#/script #FixDM.class #gracz : Ustawia slot DeathMatch gracza na status zwycięstwa.",
						"#/script #ListNPCs.class : Wypisuje listę wszystkich NPC i ich pozycje.",
						"#/script #LogoutPlayer.class #gracz : Wyrzuca gracza z gry.",
						"#/script #NPCShout.class #npc #tekst : NPC wykrzykuje wiadomość.",
						"#/script #NPCShoutZone.class #npc #strefa #tekst : NPC wykrzykuje wiadomość do graczy w podanej strefie. Użyj '-' zamiast nazwy strefy, aby była to Twoja aktualna strefa.",
						"#/script #Plague.class #1 #stworzenie : Przywołuje plagę stworzeń rajdowych wokół Ciebie.",
						"#/script #WhereWho.class : Pokazuje, gdzie znajdują się wszyscy zalogowani gracze.",
						"#/script #Maria.class : Przywołuje NPC Marię, sprzedającą jedzenie i napoje. Po zakończeniu użycia nie zapomnij jej usunąć komendą -unload.",
						"#/script #ServerReset.class : Używać tylko w nagłych wypadkach — natychmiast wyłącza serwer. Jeśli to możliwe, ostrzeż wcześniej graczy i daj im czas na wylogowanie. To twarde zatrzymanie serwera.",
						"#/script #ResetSlot.class #gracz #slot : Resetuje wskazany slot, np. !kills lub !quests. Przydatne przy debugowaniu."
					];
				/* TODO:
				} else if ("support" == params[0]) {
					msg = buildHelpSupportResponse();
				*/
				} else {
					return false;
				}
			} else {
				return false;
			}

			for (var i = 0; i < msg.length; i++) {
				Chat.log("info", msg[i]);
			}

			return true;
		},
		minParams: 0,
		maxParams: 1,
		getHelp: function(sparams?: string): string[] {
			if (sparams) {
				return [sparams, "Aby uzyskać więcej informacji o alter, script lub skrótach supportanswer."];
			}
			return ["", "Wyświetl ten komunikat pomocy."];
		}
	};

	"about" = new AboutAction();

	"add": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			if (params == null) {
				return false;
			};

			const action: Action = {
				"type": "addbuddy",
				"target": params[0]
			};
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["<gracz>", "Dodaj #gracza do swojej listy znajomych."];
		}
	};

	"adminnote": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": type,
				"target": params[0],
				"note": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["<gracz> <notatka>", "Zapisuje notatkę o #graczu."];
		}
	};

	"adminlevel": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": type,
				"target": params[0],
			};
			if (params.length >= 2) {
				action["newlevel"] = params[1];
			}
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 2,
		getHelp: function(): string[] {
			return ["<gracz> [<nowypoziom>]", "Wyświetla lub ustawia poziom administratora wskazanego #gracza."];
		}
	};

	"alter": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": type,
				"target": params[0],
				"stat": params[1],
				"mode": params[2],
				"value": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 3,
		maxParams: 3,
		getHelp: function(): string[] {
			return [
				"<gracz> <atrybut> <tryb> <wartość>",
				"Zmień statystykę #atrybut #gracza o podaną wartość; #tryb może przyjmować wartości ADD, SUB, SET lub UNSET."
						+ " Zobacz #/gmhelp #alter po szczegóły."
			];
		}
	};

	"altercreature": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "altercreature",
				"target": params[0],
				"text": params[1]
			};

			this.sendAction(action);
			return true;
		},
		minParams: 2,
		maxParams: 2,
		getHelp: function(): string[] {
			return [
				"<id> set|unset|add|sub <atrybut> [<wartość>]",
				"Zmień wartości stwora. Użyj #- jako symbolu zastępczego, aby zachować domyślną wartość. Przydatne w"
						+ " rajdach."
			];
		}
	};

	"alterkill": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const target = params[0];
			const killtype = params[1];
			const count = params[2];
			var creature = null;

			if (remainder != null && remainder != "") {
				// NOTE: unlike Java client, Javascript client automatically trims whitespace in "remainder" parameter
				creature = remainder;
			}

			const action: Action = {
				"type": "alterkill",
				"target": target,
				"killtype": killtype,
				"count": count
			};
			if (creature != null) {
				action["creature"] = creature;
			}

			this.sendAction(action);
			return true;
		},
		minParams: 3,
		maxParams: 3,
		getHelp: function(): string[] {
			return [
				"<gracz> <typ> <liczba> <stwór>",
				"Zmień liczbę #creature zabitych w trybie #type (\"solo\" lub \"shared\") na #count dla #player."
			];
		}
	};

	"alterquest": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "alterquest",
				"target": params[0],
				"name": params[1]
			};

			if (params[2] != null) {
				action["state"] = this.checkQuoted(params[2], remainder);
			}

			this.sendAction(action);
			return true;
		},
		minParams: 2,
		maxParams: 3,
		getHelp: function(): string[] {
			return ["<gracz> <gniazdo_zadania> <wartość>", "Zaktualizuj #questslot dla #player do wartości #value."];
		}
	};

	"answer": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			if (remainder == null || remainder == "") {
				return false;
			};

			const action: Action = {
				"type": "answer",
				"text": remainder
			};

			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<wiadomość>", "Wyślij prywatną wiadomość do ostatniego gracza, z którym pisałeś."];
		}
	};

	"away": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "away",
			};
			if (remainder.length != 0) {
				action["message"] = remainder;
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(sparams: string): string[] {
			if (sparams === "<wiadomość>") {
				return [sparams,  "Ustaw wiadomość o nieobecności."];
			}
			return ["", "Usuń status nieobecności."];
		}
	};

	"ban": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "ban",
				"target": params[0],
				"hours": params[1],
				"reason": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 2,
		maxParams: 2,
		getHelp: function(): string[] {
			return [
					"<postać> <godziny> <powód>",
					"Banuje konto postaci i uniemożliwia logowanie na serwer gry lub stronę internetową przez"
							+ " określoną liczbę godzin (-1 oznacza do końca świata)."
			];
		}
	};

	"chat": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": type,
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return [
				"<wiadomość>",
				"Wyślij publiczną wiadomość na czacie (tak samo jak wpisanie tekstu bez poprzedzającej komendy)."
			];
		}
	};

	"clear": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			(ui.get(UIComponentEnum.ChatLog) as ChatLogComponent).clear();
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Wyczyść dziennik czatu."];
		}
	};

	/*
	"clickmode": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const newMode = !stendhal.config.getBoolean("input.doubleclick");
			stendhal.config.set("input.doubleclick", newMode);
			stendhal.ui.gamewindow.updateClickMode();

			if (newMode) {
				Chat.log("info", "Tryb kliknięcia ustawiono na podwójne kliknięcie.");
			} else {
				Chat.log("info", "Tryb kliknięcia ustawiono na pojedyncze kliknięcie.");
			}
			return true;
		},
		minParams: 0,
		maxParams: 0
	};
	*/

	"debug" = new DebugAction();

	"drop": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			console.log(type, params, remainder);
			let name = remainder;
			let quantity = parseInt(params[0], 10);
			console.log(name, quantity);
			if (isNaN(quantity)) {
				name = (params[0] + " " + remainder).trim();
				quantity = 0;
			}
			console.log(name, quantity);
			const action: Action = {
				"type": "drop",
				"source_name": name,
				"quantity": "" + quantity,
				"x": "" + marauroa.me.x,
				"y": "" + marauroa.me.y
			};
			console.log(action);
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["[<ilość>] <nazwa_przedmiotu>", "Upuszcza przedmioty z ekwipunku w miejscu, w którym stoisz."];
		}
	};

	"emojilist": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const emojilist = singletons.getEmojiStore().getEmojiList().sort();
			for (const idx in emojilist) {
				emojilist[idx] = "&nbsp;&nbsp;- :" + emojilist[idx] + ":";
			}
			emojilist.splice(0, 0, emojilist.length + " emotikon dostępnych:");
			Chat.log("client", emojilist);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Wyświetl dostępne emotikony."];
		}
	};

	"gag": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "gag",
				"target": params[0],
				"minutes": params[1],
				"reason": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 2,
		maxParams: 2,
		getHelp: function(): string[] {
			return [
				"<gracz> <minuty> <powód>",
				"Wycisza #gracza na określony czas (gracz nie może wysyłać wiadomości do nikogo)."
			];
		}
	};

	"group": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "group_management",
				"action": params[0],
				"params": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(sparams: string): string[] {
			const desc: string[] = [];
			if (sparams === "invite <player>") {
				desc.push("Zaproś gracza do swojej grupy.");
			} else if (sparams === "join <player") {
				desc.push("Przyjmij zaproszenie do grupy #gracza.");
			} else if (sparams === "leader <player>") {
				desc.push("Uczyń #gracza liderem grupy.");
			} else if (sparams === "lootmode single|shared") {
				desc.push("Ustaw tryb zdobywania łupów dla grupy.");
				desc.push("&nbsp;&nbsp;single: Tylko lider grupy może zbierać łupy.");
				desc.push("&nbsp;&nbsp;shared: Każdy członek grupy może zbierać łupy.");
			} else if (sparams === "kick <player>") {
				desc.push("Wyrzuć #gracza z grupy.");
			} else if (sparams === "part") {
				desc.push("Opuść grupę.");
			} else if (sparams === "status") {
				desc.push("Nie działa?");
			}
			return [sparams, ...desc];
		}
	};

	"grumpy": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "grumpy",
			};
			if (remainder.length != 0) {
				action["reason"] = remainder;
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(sparams: string): string[] {
			if (sparams === "<wiadomość>") {
				return [sparams,  "Ustaw wiadomość, aby ignorować wszystkich spoza listy znajomych."];
			}
			return ["", "Usuń status zgryźliwego."];
		}
	};

	"ignore": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
					"type": "ignore"
			};

			if (params[0] == null) {
				action["list"] = "1";
			} else {
				action["target"] = params[0];
				var duration = params[1];

				if (duration != null) {
					/*
					 * Ignore "forever" values
					 */
					if (duration != "*" && duration != "-") {
						/*
						 * Validate it's a number
						 */
						if (isNaN(parseInt(duration, 10))) {
							return false;
						}

						action["duration"] = duration;
					}
				}

				if (remainder.length != 0) {
					action["reason"] = remainder;
				}
			}

			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 2,
		getHelp: function(sparams?: string): string[] {
			if (sparams) {
				return [sparams, "Dodaj #gracza do swojej listy ignorowanych."];
			}
			return ["", "Zobacz, kto znajduje się na twojej liście ignorowanych."];
		}
	};

	"inspectkill": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const target = params[0];
			var creature = null;
			if (remainder != null && remainder != "") {
				// NOTE: unlike Java client, Javascript client automatically trims whitespace in "remainder" parameter
				creature = remainder;
			}

			const action: Action = {
				"type": "inspectkill",
				"target": target
			};
			if (creature != null) {
				action["creature"] = creature;
			}

			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["<gracz> <stwór>", "Pokaż liczbę zabójstw #creature dokonanych przez #player."];
		}
	};

	"inspectquest": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
					"type": "inspectquest",
					"target": params[0]
			};
			if (params.length > 1) {
					action["quest_slot"] = params[1];
			}
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 2,
		getHelp: function(): string[] {
			return ["<gracz> [<gniazdo_zadania>]", "Pokaż stan zadania dla #player."];
		}
	};


	"jail": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "jail",
				"target": params[0],
				"minutes": params[1],
				"reason": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 2,
		maxParams: 2,
		getHelp: function(): string[] {
			return ["<gracz> <minuty> <powód>", "Wtrąca #gracza do więzienia na określony czas."];
		}
	};

	"me": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "emote",
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<akcja>", "Wyświetl wiadomość opisującą, co robisz."];
		}
	};

	"movecont": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const enable = !stendhal.config.getBoolean("move.cont");
			const action: Action = {
				"type": "move.continuous",
			};
			if (enable) {
				action["move.continuous"] = "";
			}
			this.sendAction(action);
			// update config
		stendhal.config.set("move.cont", enable);
		Chat.log("info", "Ciągły ruch "
				+ (enable ? "włączony" : "wyłączony") + ".");
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return [
				"",
				"Przełącz tryb ciągłego ruchu (pozwala kontynuować chodzenie po zmianie mapy lub"
						+ " teleportacji bez puszczania klawisza kierunku)."
			];
		}
	};

	"tell" = new TellAction();
	"msg" = this["tell"];
	"/" = new ReTellAction();

	"mute": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			stendhal.sound.toggleSound();
			if (stendhal.config.getBoolean("sound")) {
				Chat.log("info", "Dźwięki są teraz włączone.");
			} else {
				Chat.log("info", "Dźwięki są teraz wyłączone.");
			}
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Wycisz lub włącz dźwięki."];
		}
	};

	"p": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "group_message",
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<wiadomość>", "Wyślij wiadomość do członków grupy."];
		}
	};

	"progressstatus" = new ProgressStatusAction();

	"remove": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			if (params == null) {
				return false;
			}

			const action: Action = {
				"type": "removebuddy",
				"target": params[0]
			};

			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["<gracz>", "Usuń #gracza ze swojej listy znajomych."];
		}
	};

	"screenshot": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			return singletons.getDownloadUtil().buildScreenshot().execute();
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Wykonaj zrzut ekranu obszaru widocznego w oknie."];
		}
	};

	"screencap" = new ScreenCaptureAction();

	"sentence": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			if (params == null) {
				return false;
			};

			const action: Action = {
				"type": "sentence",
				"value": remainder
			};

			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return [
				"<tekst>",
				"Ustaw wiadomość na stronie profilu stendhalgame.org oraz tekst wyświetlany przy użyciu komendy #Look."
			];
		}
	};

	"settings" = new SettingsAction();

	"stopwalk": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "walk",
				"mode": "stop"
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Wyłącza automatyczne chodzenie."];
		}
	};

	"volume": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const layername = params[0];
			let vol = params[1];
			if (typeof(layername) === "undefined") {
				const layers = ["master", ...stendhal.sound.getLayerNames()];
				Chat.log("info", "Użyj /volume <warstwa> <wartość>, aby dostosować głośność.");
				Chat.log("client", "<warstwa> to jedna z \"" + layers.join("\", \"") + "\"");
				Chat.log("client", "<wartość> to liczba z zakresu od 0 do 100.");
				Chat.log("client", "Aktualne poziomy głośności:");
				for (const l of layers) {
					Chat.log("client", "&nbsp;&nbsp;- " + l + " -> " + stendhal.sound.getVolume(l) * 100);
				}
			} else if (typeof(vol) !== "undefined") {
				if (!/^\d+$/.test(vol)) {
					Chat.log("error", "Wartość musi być liczbą.");
					return true;
				}
				if (stendhal.sound.setVolume(layername, parseInt(vol, 10) / 100)) {
					Chat.log("client", "Kanał \"" + layername + "\" ustawiono na "
							+ (stendhal.sound.getVolume(layername) * 100) + ".");
				} else {
					Chat.log("error", "Nieznana warstwa \"" + layername + "\".");
				}
			} else {
				Chat.log("error", "Użyj /volume, aby uzyskać pomoc.");
			}

			return true;
		},
		minParams: 0,
		maxParams: 2,
		getHelp: function(sparams?: string): string[] {
			if (sparams) {
				return [sparams, "Wyświetla lub ustawia głośność dźwięków i muzyki."];
			}
			return ["", "Pokazuje bieżące poziomy głośności."];
		}
	};

	"summon": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			var x = null;
			var y = null;
			var quantity = null;

			var nameBuilder = [];
			for (var idx = 0; idx < params.length; idx++) {
				const str = params[idx];
				if (str.match("[0-9].*")) {
					if (x == null) {
						x = str;
					} else if (y == null) {
						y = str;
					} else if (quantity == null) {
						quantity = str;
					} else {
						nameBuilder.push(str);
					}
				} else {
					nameBuilder.push(str);
				}
			}

			// use x value as quantity if y was not specified
			if (quantity == null && y == null && x != null) {
				quantity = x;
				x = null;
			}

			var creature = nameBuilder.join(" ");
			if (x == null || y == null) {
				// for some reason, the action does not accept the x,y coordinates if they are not a string
				x = marauroa.me.x.toString();
				y = marauroa.me.y.toString();
			}

			const action: Action = {
				"type": type,
				"creature": creature,
				"x": x,
				"y": y
			};
			if (quantity != null) {
				action["quantity"] = quantity;
			}
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: -1, // XXX: is this the proper way to allow an unlimited number of arguments?
		getHelp: function(sparams: string): string[] {
			let desc: any;
			if (sparams === "<creature>|<item> [<x> <y>]") {
				desc = "Przyzwij stwora.";
			} else if (sparams === "<stackable_item> [<x> <y>] [quantity]") {
				desc = "Przyzwij wskazany przedmiot lub stwora na współrzędnych #x, #y w bieżącej strefie.";
			}
			return [sparams, desc];
		}
	};

	"summonat": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			var amount = params[2];
			// don't require first parameter to be integer amount
			if (isNaN(parseInt(amount, 10))) {
				if (remainder) {
					remainder = amount + " " + remainder;
				} else {
					remainder = amount;
				}
				amount = "1";
			}

			const action: Action = {
				"type": type,
				"target": params[0],
				"slot": params[1],
				"amount": amount,
				"item": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 3,
		maxParams: 3,
		getHelp: function(): string[] {
			return [
				"<gracz> <slot> [ilość] <przedmiot>",
				"Przyzwij wskazany przedmiot do określonego slotu #player; <ilość> domyślnie wynosi 1, jeśli"
						+ " nie zostanie podana."
			];
		}
	};

	"support": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "support",
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<wiadomość>", "Poproś administratora o pomoc."];
		}
	};

	"supportanswer": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "supportanswer",
				"target": params[0],
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		aliases: ["supporta"],
		getHelp: function(): string[] {
			return [
				"<gracz> <wiadomość>",
				"Odpowiada na zgłoszenie wsparcia. Zastąp #message skrótami $faq, $faqsocial, $ignore, $faqpvp,"
						+ " $wiki, $knownbug, $bugstracker, $rules, $notsupport lub $spam, jeśli chcesz użyć skrótów."
			];
		}
	};
	"supporta": SlashActionImpl = this["supportanswer"];

	"teleport": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "teleport",
				"target": params[0],
				"zone": params[1],
				"x": params[2],
				"y": params[3]
			};
			this.sendAction(action);
			return true;
		},
		minParams: 4,
		maxParams: 4,
		getHelp: function(): string[] {
			return ["<gracz> <strefa> <x> <y>", "Teleportuj #gracza w podane miejsce."];
		}
	};

	"teleportto": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "teleportto",
				"target": remainder,
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<nazwa>", "Teleportuj się w pobliże wskazanego gracza lub NPC."];
		}
	};

	"tellall": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "tellall",
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["<wiadomość>", "Wyślij prywatną wiadomość do wszystkich zalogowanych graczy."];
		}
	};

	"walk": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "walk"
			};
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Przełącza automatyczne chodzenie."];
		}
	};

	"atlas": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			window.location.href = "https://stendhalgame.org/world/atlas.html?me="
				+ marauroa.currentZoneName + "." + marauroa.me.x + "." + marauroa.me.y;
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Otwiera stronę atlasu na stendhalgame.org."];
		}
	};

	"beginnersguide": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			window.location.href = "https://stendhalgame.org/wiki/Stendhal_Beginner's_Guide";
			return true;
		},
		minParams: 0,
		maxParams: 0,
		getHelp: function(): string[] {
			return ["", "Otwiera stronę wiki z poradnikiem dla początkujących na stendhalgame.org."];
		}
	};

	"characterselector" = new OpenWebsiteAction("https://stendhalgame.org/account/mycharacters.html");

	"faq" = new OpenWebsiteAction("https://stendhalgame.org/wiki/Stendhal_FAQ");

	"manual" = new OpenWebsiteAction("https://stendhalgame.org/wiki/Stendhal_Manual/Controls_and_Game_Settings");

	"profile": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			var url = "https://stendhalgame.org/character/";
			var name = marauroa.me["_name"] || singletons.getSessionManager().getCharName();

			if (params.length > 0 && params[0] != null) {
				name = params[0];
			}
			if (!name) {
				console.warn("failed to get default character name!");
				return true;
			}

			url += name + ".html";
			Chat.log("info", "Próba otwarcia #" + url + " w przeglądarce.");
			window.location.href = url;
			return true;
		},
		minParams: 0,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["[<nazwa>]", "Otwiera stronę profilu gracza na stendhalgame.org."];
		}
	};

	"rules" = new OpenWebsiteAction("https://stendhalgame.org/wiki/Stendhal_Rules");

	"changepassword" = new OpenWebsiteAction("https://stendhalgame.org/account/change-password.html");

	"loginhistory" = new OpenWebsiteAction("https://stendhalgame.org/account/history.html");

	"logout" = new OpenWebsiteAction("/account/logout.html");

	"halloffame" = new OpenWebsiteAction("https://stendhalgame.org/world/hall-of-fame/active_overview.html");

	"storemessage": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": "storemessage",
				"target": params[0],
				"text": remainder
			};
			this.sendAction(action);
			return true;
		},
		minParams: 1,
		maxParams: 1,
		getHelp: function(): string[] {
			return ["<gracz> <wiadomość>", "Zapisz prywatną wiadomość do dostarczenia nieobecnemu #graczowi."];
		}
	};

	/** Default action executed if a type is not registered. */
	"_default": SlashActionImpl = {
		execute: (type: string, params: string[], remainder: string): boolean => {
			const action: Action = {
				"type": type
			};
			if (typeof(params[0] != "undefined")) {
				action["target"] = params[0];
				if (remainder != "") {
					action["args"] = remainder;
				}
			}
			this.sendAction(action);
			return true;
		},
		minParams: 0,
		maxParams: 1
	};

	/**
	 * Parses a slash action formatted string & executes the registered action.
	 *
	 * @param line {string}
	 *   Complete slash action line including parameters.
	 * @return {boolean}
	 *   `true` to represent successful execution.
	 */
	execute(line: string): boolean {
		line = line.trim();

		// double slash is a special command, that should work without
		// entering a space to separate it from the arguments.
		if (line.startsWith("//") && !line.startsWith("// ")) {
			line = "// " + line.substring(2);
		}

		var array = line.split(" ");

		// clean whitespace
		for (var el in array) {
			array[el] = array[el].trim();
		}
		array = array.filter(Boolean);
		if (array.length == 0) {
			return false;
		}

		var name = array[0];
		if (name[0] != "/") {
			name = "/chat";
		} else {
			array.shift();
		}
		name = name.substr(1);
		var action: SlashActionImpl;
		if (typeof(this[name]) == "undefined") {
			action = this["_default"];
		} else {
			action = this[name];
		}

		// use executing character if name parameter not supplied
		if (name == "where" && array.length == 0) {
			array[0] = marauroa.me["_name"];
		}

		if (action.minParams <= array.length) {
			var remainder = "";
			for (var i = action.maxParams; i < array.length; i++) {
				remainder = remainder + array[i] + " ";
			}
			array.slice(action.maxParams);
			return action.execute(name, array, remainder.trim());
		} else {
			Chat.log("error", "Brak argumentów. Spróbuj /help");
			return false;
		}
		return true;
	}

	/**
	 * Checks for quoted whitepace to be included in parameter.
	 *
	 * @param p {string}
	 *   The parameter to be amended.
	 * @param remainder {string}
	 *   String to be checked for quoted whitespace.
	 * @return {string}
	 *   Amended parameter.
	 */
	checkQuoted(p: string, remainder: string): string {
		if (p.includes("\"") && remainder.includes("\"")) {
			let endQuote = false;
			let paramEnd = 0;
			const arr = Array.from(remainder);
			for (const c of arr) {
				if (c === " " && endQuote) {
					break;
				} else if (c === "\"") {
					endQuote = !endQuote;
				}
				paramEnd++;
			}
			p = (p + " " + remainder.substring(0, paramEnd+1)).replace(/\"/g, "").trim();
		}
		return p;
	}
}
