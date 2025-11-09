/***************************************************************************
 *                 Copyright © 2023-2024 - Faiumoni e. V.                  *
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

import { PerceptionListener } from "./PerceptionListener";
import { singletons } from "./SingletonRepo";

import { Paths } from "./data/Paths";

import { Color } from "./data/color/Color";

import { Ground } from "./entity/Ground";
import { RPObject } from "./entity/RPObject";

import { ui } from "./ui/UI";
import { UIComponentEnum } from "./ui/UIComponentEnum";

import { BuddyListComponent } from "./ui/component/BuddyListComponent";
import { MiniMapComponent } from "./ui/component/MiniMapComponent";
import { PlayerEquipmentComponent } from "./ui/component/PlayerEquipmentComponent";
import { ZoneInfoComponent } from "./ui/component/ZoneInfoComponent";

import { ChooseCharacterDialog } from "./ui/dialog/ChooseCharacterDialog";
import { LoginDialog } from "./ui/dialog/LoginDialog";

import { DesktopUserInterfaceFactory } from "./ui/factory/DesktopUserInterfaceFactory";

import { SingletonFloatingWindow } from "./ui/toolkit/SingletonFloatingWindow";

import { Chat } from "./util/Chat";
import { DialogHandler } from "./util/DialogHandler";
import { Globals } from "./util/Globals";

/**
 * Main class representing client.
 */
export class Client {

	/** Property set to prevent re-initialization. */
	private initialized = false;
	private errorCounter = 0;
	private unloading = false;
	/** User's character name.
	 *
	 * NOTE: can we replace references to this with value now stored in `util.SessionManager`?
	 */
	public username?: string;

	/** ID for vetoing click indicator timeout (experimental setting not enabled/visible by default). */
	private static click_indicator_id: number|undefined = undefined;

	/** Singleton instance. */
	private static instance: Client;

	/** Cached parser used to decode HTML entities embedded in character names. */
	private static htmlEntityParser?: HTMLTextAreaElement;

	/** Cached UTF-8 decoder for converting Latin-1 encoded character names. */
	private static utf8Decoder?: TextDecoder;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): Client {
		if (!Client.instance) {
			Client.instance = new Client();
		}
		return Client.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Initializations to be called before main startup calls.
	 */
	init() {
		if (this.initialized) {
			console.warn("tried to re-initialize client");
			return;
		}
		this.initialized = true;

		// add version & build info to DOM for retrieval by browser
		document.documentElement.setAttribute("data-build-version", stendhal.data.build.version);
		document.documentElement.setAttribute("data-build-build", stendhal.data.build.build);

		stendhal.paths = singletons.getPaths();
		stendhal.config = singletons.getConfigManager();
		stendhal.session = singletons.getSessionManager();
		stendhal.actions = singletons.getSlashActionRepo();

		this.initData();
		this.initSound();
		this.initUI();
		this.initZone();
	}

	/**
	 * Initializes sprite resources & other data management.
	 */
	private initData() {
		// build info is stored in build/js/build.js
		stendhal.data = stendhal.data || {};
		stendhal.data.cache = singletons.getCacheManager();
		stendhal.data.cache.init();
		stendhal.data.cstatus = singletons.getCStatus();
		stendhal.data.cstatus.init();
		stendhal.data.group = singletons.getGroupManager();
		stendhal.data.outfit = singletons.getOutfitStore();
		stendhal.data.sprites = singletons.getSpriteStore();
		stendhal.data.map = singletons.getMap();
		// online players
		stendhal.players = [];
	}

	/**
	 * Initializes sound manager.
	 *
	 * Should be called after config is initialized.
	 */
	private initSound() {
		stendhal.sound = singletons.getSoundManager();
		// update sound levels from config at startup
		stendhal.sound.onConfigUpdate();
	}

	/**
	 * Initializes GUI elements, input management, sound management, & other interface tools.
	 */
	private initUI() {
		stendhal.ui = stendhal.ui || {};
		stendhal.ui.equip = singletons.getInventory();
		stendhal.ui.html = singletons.getHTMLManager();
		stendhal.ui.touch = singletons.getTouchHandler();
		stendhal.ui.viewport = singletons.getViewPort();
		// alias for backward-compat until changed in all source
		stendhal.ui.gamewindow = stendhal.ui.viewport;

		stendhal.ui.getMenuStyle = Globals.getMenuStyle;
	}

	/**
	 * Builds initial zone for user to enter world.
	 */
	private initZone() {
		stendhal.zone = singletons.getZone();
		stendhal.zone.ground = new Ground();
	}

	/**
	 * Main startup routines.
	 */
	startup() {
		this.devWarning();

		// initialize configuration & session managers
		const sparams = new URL(document.URL).searchParams;
		stendhal.config.init(sparams);
		stendhal.session.init(sparams);

		// update user interface after config is loaded
		stendhal.config.refreshTheme();
		document.getElementById("body")!.style.setProperty("font-family", stendhal.config.get("font.body"));

		// initialize events
		singletons.getEventRegistry().init();

		// initialize tileset animation data
		singletons.getTileStore().init();
		// initialize emoji data
		singletons.getEmojiStore().init();
		// initialize outfit data
		stendhal.data.outfit.init();

		new DesktopUserInterfaceFactory().create();

		Chat.log("client", "Klient załadowany. Łączenie...");

		this.registerMarauroaEventHandlers();
		this.registerBrowserEventHandlers();

		let ws = Paths.ws.substring(1);
		if (stendhal.session.isTestClient() && !stendhal.session.isServerDefault()) {
			ws = ws.replace(/t/, "s");
			// disclaimer for using the test client on the main server
			Chat.log("warning", "OSTRZEŻENIE: Łączysz się z serwerem przy użyciu deweloperskiej wersji testowego"
					+ " klienta, która może zawierać błędy lub nie działać zgodnie z przeznaczeniem. Postępuj ostrożnie.");
		}
		marauroa.clientFramework.connect(null, null, ws);

		stendhal.ui.actionContextMenu = new DialogHandler();
		stendhal.ui.globalInternalWindow = new DialogHandler();

		// pre-cache images & sounds
		stendhal.data.sprites.startupCache();
		stendhal.sound.startupCache();

		if (document.getElementById("viewport")) {
			stendhal.ui.gamewindow.draw.apply(stendhal.ui.gamewindow, arguments);
		}
	}

	/**
	 * Prints standard warning message to development tools console.
	 */
	devWarning() {
		console.log("%c ", "padding: 30px; background: url(" + window.location.protocol + "://" + window.location.host + "/images/buttons/devtools-warning.png) no-repeat; color: #AF0");
		console.log("%cJeśli ktoś kazał ci coś skopiować i wkleić tutaj, to oszustwo i da im dostęp do twojego konta.", "color:#A00; background-color:#FFF; font-size:150%");
		console.log("Jeśli jesteś deweloperem i ciekawi cię PolanieOnLine, zajrzyj na https://s1.polanieonline.eu/dla-projektantow.html, aby pobrać kod źródłowy. Może nawet dodasz nową funkcję lub naprawisz błąd! ");
		console.log(" ");
		console.log(" ");
		window["eval"] = function() {};
	}

	/**
	 * Reports errors emitted by web client to server.
	 */
	onError(error: ErrorEvent): boolean|undefined {
		this.errorCounter++;
		if (this.errorCounter > 5) {
			console.log("Zbyt wiele błędów, raportowanie zatrzymane.");
			return;
		}
		var text = error.message + "\r\n";
		text += error.filename + ":" + error.lineno;
		if (error.colno) {
			text += ":" + error.colno;
		}
		if (error.error) {
			text += "\r\n" + error.error.stack;
		}
		text += "\r\n" + window.navigator.userAgent;
		try {
			console.log(text);
			var action = {
				"type": "report_error",
				"text": text,
			};
			marauroa.clientFramework.sendAction(action);
		} catch (e) {
			// ignore
		}
		return true;
	}

	/**
	 * Registers Marauroa event handlers.
	 */
	registerMarauroaEventHandlers() {
		marauroa.clientFramework.onDisconnect = function(_reason: string, _error: string) {
			if (!Client.instance.unloading) {
				Chat.logH("error", "Odłączono od serwera.");
			}
		};

		marauroa.clientFramework.onLoginRequired = function(config: Record<string, string>) {
			if (config["client_login_url"]) {
				Client.instance.unloading = true;
				let currentUrl = encodeURI(window.location.pathname + window.location.hash);
				let url = config["client_login_url"].replace("[url]", currentUrl);
				window.location.href = url;
				return;
			}

			let body = document.getElementById("body")!;
			body.style.cursor = "auto";
			document.getElementById("loginpopup")!.style.display = "none";
			ui.createSingletonFloatingWindow(
				"Logowanie",
				new LoginDialog(),
				100, 50).enableCloseButton(false);
		};

		marauroa.clientFramework.onCreateAccountAck = function(username: string) {
			// TODO: We should login automatically
			alert("Konto zostało pomyślnie utworzone, proszę się zalogować.");
			window.location.reload();
		};

		marauroa.clientFramework.onCreateCharacterAck = function(charname: string, _template: any) {
			// Client.get().chooseCharacter(charname);
		};

		marauroa.clientFramework.onLoginFailed = function(_reason: string, _text: string) {
			alert("Logowanie nie powiodło się. " + _text);
			// TODO: Server closes the connection, so we need to open a new one
			window.location.reload();
		};

		marauroa.clientFramework.onAvailableCharacterDetails = function(characters: {[key: string]: RPObject}) {
			SingletonFloatingWindow.closeAll();
			const rawKeys = Object.keys(characters);
			const storedBeforeHash = stendhal.session.getCharName();
			Client.debugCharacterNameFlow("available_characters_received", {
				rawKeys,
				storedBeforeHash,
				rawHash: window.location.hash || undefined
			});
			if (!rawKeys.length && this.username) {
				marauroa.clientFramework.createCharacter(this.username, {});
				return;
			}
			if (window.location.hash) {
				const hashValue = window.location.hash.substring(1);
				Client.debugCharacterNameFlow("hash_candidate_detected", {hashValue});
				const resolved = Client.resolveCharacterNameFromHash(hashValue, characters);
				Client.debugCharacterNameFlow("hash_candidate_processed", {hashValue, resolved});
				if (resolved) {
					stendhal.session.setCharName(resolved);
					Client.debugCharacterNameFlow("hash_selection_stored", {resolved});
				}
			}

			const storedAfterHash = stendhal.session.getCharName();
			let name = storedAfterHash;
			if (name) {
				const resolvedStored = Client.findMatchingCharacterName(name, characters);
				Client.debugCharacterNameFlow("stored_selection_checked", {candidate: name, resolvedStored});
				if (resolvedStored) {
					Client.debugCharacterNameFlow("stored_selection_accepted", {candidate: name, resolvedStored});
					Client.get().chooseCharacter(resolvedStored);
					return;
				}
			}
			Client.debugCharacterNameFlow("displaying_character_dialog", {
				storedAfterHash,
				rawKeys
			});
			let body = document.getElementById("body")!;
			body.style.cursor = "auto";
			document.getElementById("loginpopup")!.style.display = "none";
			ui.createSingletonFloatingWindow(
				"Wybierz postać.",
				new ChooseCharacterDialog(characters),
				100, 50).enableCloseButton(false);
		};

		marauroa.clientFramework.onTransferREQ = function(items: any) {
			for (var i in items) {
				if (typeof(items[i]["name"]) == "undefined") {
					continue;
				}
				items[i]["ack"] = true;
			}
		};

		marauroa.clientFramework.onTransfer = function(items: any) {
			var data = {} as any;
			var zoneName = ""
			for (var i in items) {
				var name = items[i]["name"];
				zoneName = name.substring(0, name.indexOf("."));
				name = name.substring(name.indexOf(".") + 1);
				data[name] = items[i]["data"];
				if (name === "data_map") {
					this.onDataMap(items[i]["data"]);
				}
			}
			stendhal.data.map.onTransfer(zoneName, data);
		};

		// update user interface on perceptions
		if (document.getElementById("viewport")) {
			// override perception listener
			marauroa.perceptionListener = new PerceptionListener(marauroa.perceptionListener);
			marauroa.perceptionListener.onPerceptionEnd = function(_type: Int8Array, _timestamp: number) {
				stendhal.zone.sortEntities();
				(ui.get(UIComponentEnum.MiniMap) as MiniMapComponent).draw();
				(ui.get(UIComponentEnum.BuddyList) as BuddyListComponent).update();
				stendhal.ui.equip.update();
				(ui.get(UIComponentEnum.PlayerEquipment) as PlayerEquipmentComponent).update();
				if (!this.loaded) {
					this.loaded = true;
					// delay visibile change of client a little to allow for initialisation in the background for a smoother experience
					window.setTimeout(function() {
						let body = document.getElementById("body")!;
						body.style.cursor = "auto";
						document.getElementById("client")!.style.display = "block";
						document.getElementById("loginpopup")!.style.display = "none";

						// initialize observer after UI is ready
						singletons.getUIUpdateObserver().init();
						ui.onDisplayReady();
					}, 300);
				}
			}
		}
	}

	/**
	 * Creates a character selection dialog window.
	 */
	chooseCharacter(name: string) {
		Client.debugCharacterNameFlow("choose_character_request", {selected: name});
		stendhal.session.setCharName(name);
		marauroa.clientFramework.chooseCharacter(name);
		Chat.log("client", "Ładowanie świata...");

		// play login sound for this user
		stendhal.sound.playGlobalizedEffect("ui/login");
	}

	/**
	 * Sets the clients unloading state property.
	 */
	onBeforeUnload() {
		Client.instance.unloading = true;
	}

	/**
	 * Registers global browser event handlers.
	 */
	registerBrowserEventHandlers() {
		const keyHandler = singletons.getKeyHandler();
		document.addEventListener("keydown", keyHandler.onKeyDown);
		document.addEventListener("keyup", keyHandler.onKeyUp);
		document.addEventListener("contextmenu", stendhal.main.preventContextMenu);

		// handles closing the context menu
		// FIXME: does not work for "touchstart" as it prevents actions on the context menu
		document.addEventListener("mousedown", function(e) {
			if (stendhal.ui.actionContextMenu.isOpen()) {
				stendhal.ui.actionContextMenu.close(true);
				e.preventDefault();
				e.stopPropagation();
			}
		});

		window.addEventListener("beforeunload", () => {
			this.onBeforeUnload();
		})

		document.getElementById("body")!.addEventListener("mouseenter", stendhal.main.onMouseEnter);

		var gamewindow = document.getElementById("viewport")!;
		gamewindow.setAttribute("draggable", "true");
		gamewindow.addEventListener("mousedown", stendhal.ui.gamewindow.onMouseDown);
		gamewindow.addEventListener("dblclick", stendhal.ui.gamewindow.onMouseDown);
		gamewindow.addEventListener("dragstart", stendhal.ui.gamewindow.onDragStart);
		gamewindow.addEventListener("mousemove", stendhal.ui.gamewindow.onMouseMove);
		gamewindow.addEventListener("touchstart", stendhal.ui.gamewindow.onMouseDown);
		gamewindow.addEventListener("touchend", stendhal.ui.gamewindow.onTouchEnd);
		gamewindow.addEventListener("dragover", stendhal.ui.gamewindow.onDragOver);
		gamewindow.addEventListener("drop", stendhal.ui.gamewindow.onDrop);
		gamewindow.addEventListener("contextmenu", stendhal.ui.gamewindow.onContentMenu);
		gamewindow.addEventListener("wheel", stendhal.ui.gamewindow.onMouseWheel);

		singletons.getJoystickController().registerGlobalEventHandlers();

		// main menu button
		const menubutton = document.getElementById("menubutton")!;
		menubutton.addEventListener("click", function(e: Event) {
			ui.showApplicationMenu();
		});

		// main sound button
		const soundButton = document.getElementById("soundbutton")!;
		soundButton.addEventListener("click", function(e: Event) {
			stendhal.sound.toggleSound();
		});

		// click/touch indicator
		// TODO:
		//   - animate
		//   - would work better if displayed upon mousedown/touchstart then position updated & timer
		//     started upon release
		const click_indicator = document.getElementById("click-indicator")! as HTMLImageElement;
		click_indicator.onload = () => {
			click_indicator.onload = null;
			// FIXME: some event handlers cancel propagation
			document.addEventListener("click", Client.handleClickIndicator);
			document.addEventListener("touchend", Client.handleClickIndicator);
		};
		click_indicator.src = stendhal.paths.gui + "/click_indicator.png";
	}

	/**
	 * Reads zone's map data.
	 *
	 * @param data {any}
	 *   Information about map.
	 */
	onDataMap(data: any) {
		var zoneinfo = {} as {[key: string]: string};
		var deserializer = marauroa.Deserializer.fromBase64(data);
		deserializer.readAttributes(zoneinfo);
		(ui.get(UIComponentEnum.ZoneInfo) as ZoneInfoComponent).zoneChange(zoneinfo);

		// global zone music
		const musicVolume = parseFloat(zoneinfo["music_volume"]);
		stendhal.sound.playSingleGlobalizedMusic(zoneinfo["music"],
				!Number.isNaN(musicVolume) ? musicVolume : 1.0);

		// parallax background
		if (stendhal.config.getBoolean("effect.parallax")) {
			stendhal.data.map.setParallax(zoneinfo["parallax"]);
			stendhal.data.map.setIgnoredTiles(zoneinfo["parallax_ignore_tiles"]);
		}

		// coloring information
		if (zoneinfo["color"] && stendhal.config.getBoolean("effect.lighting")) {
			if (zoneinfo["color_method"]) {
				stendhal.ui.gamewindow.setColorMethod(zoneinfo["color_method"]);
			}
			if (zoneinfo["blend_method"]) {
				stendhal.ui.gamewindow.setBlendMethod(zoneinfo["blend_method"]);
			}
			const hsl = Color.numToHSL(Number(zoneinfo["color"]));
			stendhal.ui.gamewindow.HSLFilter = hsl.toString();
			// deprecated
			stendhal.ui.gamewindow.filter = "hue-rotate(" + hsl.H + "deg) saturate(" + hsl.S
					+ ") brightness(" + hsl.L + ")";
		} else {
			stendhal.ui.gamewindow.HSLFilter = undefined;
			stendhal.ui.gamewindow.filter = undefined;
		}

		singletons.getWeatherRenderer().update(zoneinfo["weather"]);
	}

	/**
	 * Event handler to suppress browser's default context menu.
	 */
	preventContextMenu(e: Event) {
		e.preventDefault();
	}

	/**
	 * Sets the default cursor for the entire page.
	 */
	onMouseEnter(e: MouseEvent) {
		// use Stendhal's built-in cursor for entire page
		(e.target as HTMLElement).style.cursor = "url(" + stendhal.paths.sprites + "/cursor/normal.png) 1 3, auto";
	}

	/**
	 * Draws indicator on screen to click/touch events when enabled.
	 *
	 * Experimental feature disabled & hidden from settings dialog by default.
	 */
	static handleClickIndicator(e: Event) {
		if (!stendhal.config.getBoolean("click-indicator")) {
			return;
		}
		if (Client.click_indicator_id !== undefined) {
			window.clearTimeout(Client.click_indicator_id);
			Client.click_indicator_id = undefined;
		}
		const pos = stendhal.data.html.extractPosition(e);
		const click_indicator = document.getElementById("click-indicator")! as HTMLImageElement;
		click_indicator.style["left"] = (pos.pageX - (click_indicator.width / 2)) + "px";
		click_indicator.style["top"] = (pos.pageY - (click_indicator.height / 2)) + "px";
		click_indicator.style["display"] = "inline";
		Client.click_indicator_id = window.setTimeout(function() {
			click_indicator.style["display"] = "none";
		}, 300);
	}

	private static debugCharacterNameFlow(stage: string, payload: Record<string, unknown>) {
		if (typeof console === "undefined") {
			return;
		}
		const message = "[CharacterName] " + stage;
		if (typeof console.debug === "function") {
			console.debug(message, payload);
			return;
		}
		if (typeof console.log === "function") {
			console.log(message, payload);
		}
	}

	private static decodeCharacterNameFromHash(rawName: string): string|undefined {
		if (!Client.isNonEmptyString(rawName)) {
			Client.debugCharacterNameFlow("decode_hash_rejected", {rawName, reason: "empty"});
			return undefined;
		}
		let candidate = rawName.trim();
		if (!candidate.length) {
			Client.debugCharacterNameFlow("decode_hash_rejected", {rawName, reason: "whitespace_only"});
			return undefined;
		}
		candidate = candidate.replace(/_/g, " ");
		candidate = candidate.replace(/\+/g, " ");
		candidate = Client.decodeUnicodeEscapes(candidate);
		const percentDecoded = Client.tryDecodeURIComponent(candidate);
		if (percentDecoded !== undefined) {
			candidate = percentDecoded;
		}
		candidate = Client.decodePossibleLatin1(candidate);
		candidate = candidate.trim();
		const result = Client.isValidCharacterName(candidate) ? candidate : undefined;
		Client.debugCharacterNameFlow("decode_hash_complete", {rawName, candidate, result});
		return result;
	}

	private static decodeUnicodeEscapes(value: string): string {
		return value.replace(/%u([0-9a-fA-F]{4})|\\u([0-9a-fA-F]{4})/g, function(_match, percentHex, slashHex) {
			const hex = percentHex || slashHex;
			return String.fromCharCode(parseInt(hex, 16));
		});
	}

	private static tryDecodeURIComponent(value: string): string|undefined {
		try {
			return decodeURIComponent(value);
		} catch (_e) {
			return undefined;
		}
	}
	private static sanitizeCharacterName(value: unknown): string|undefined {
		if (!Client.isNonEmptyString(value)) {
			Client.debugCharacterNameFlow("sanitize_rejected", {value, reason: "empty"});
			return undefined;
		}
		let candidate = value.trim();
		if (!candidate.length) {
			Client.debugCharacterNameFlow("sanitize_rejected", {value, reason: "whitespace_only"});
			return undefined;
		}
		candidate = Client.decodeUnicodeEscapes(candidate);
		const percentDecoded = Client.tryDecodeURIComponent(candidate);
		if (percentDecoded !== undefined) {
			candidate = percentDecoded;
		}
		candidate = Client.decodeHtmlEntities(candidate);
		candidate = Client.decodePossibleLatin1(candidate);
		candidate = candidate.trim();
		const result = candidate.length ? candidate : undefined;
		Client.debugCharacterNameFlow("sanitize_complete", {value, candidate, result});
		return result;
	}

	private static decodeHtmlEntities(value: string): string {
		if (value.indexOf("&") === -1 || typeof(document) === "undefined") {
			return value;
		}
		if (!Client.htmlEntityParser) {
			Client.htmlEntityParser = document.createElement("textarea");
		}
		Client.htmlEntityParser.innerHTML = value;
		return Client.htmlEntityParser.value;
	}

	private static decodePossibleLatin1(value: string): string {
		let needsConversion = false;
		for (let index = 0; index < value.length; index++) {
			const code = value.charCodeAt(index);
			if (code > 0xFF) {
				return value;
			}
			if (code >= 0x80) {
				needsConversion = true;
				break;
			}
		}
		if (!needsConversion) {
			return value;
		}
		const bytes = new Uint8Array(value.length);
		for (let index = 0; index < value.length; index++) {
			bytes[index] = value.charCodeAt(index) & 0xFF;
		}
		return Client.decodeUtf8Bytes(bytes);
	}

	private static decodeUtf8Bytes(bytes: Uint8Array): string {
		if (typeof(TextDecoder) !== "undefined") {
			try {
				if (!Client.utf8Decoder) {
					Client.utf8Decoder = new TextDecoder("utf-8", {fatal: false});
				}
				return Client.utf8Decoder.decode(bytes);
			} catch (_e) {
				// fall through to manual decoder
			}
		}
		let result = "";
		for (let index = 0; index < bytes.length; index++) {
			const byte1 = bytes[index];
			if (byte1 < 0x80) {
				result += String.fromCharCode(byte1);
				continue;
			}
			if (byte1 >= 0xC0 && byte1 < 0xE0 && index + 1 < bytes.length) {
				const byte2 = bytes[++index];
				result += String.fromCharCode(((byte1 & 0x1F) << 6) | (byte2 & 0x3F));
				continue;
			}
			if (byte1 >= 0xE0 && byte1 < 0xF0 && index + 2 < bytes.length) {
				const byte2 = bytes[++index];
				const byte3 = bytes[++index];
				result += String.fromCharCode(((byte1 & 0x0F) << 12) | ((byte2 & 0x3F) << 6) | (byte3 & 0x3F));
				continue;
			}
			if (byte1 >= 0xF0 && byte1 < 0xF8 && index + 3 < bytes.length) {
				const byte2 = bytes[++index];
				const byte3 = bytes[++index];
				const byte4 = bytes[++index];
				const codepoint = ((byte1 & 0x07) << 18) | ((byte2 & 0x3F) << 12) | ((byte3 & 0x3F) << 6) | (byte4 & 0x3F);
				result += String.fromCodePoint(codepoint);
				continue;
			}
			return Array.from(bytes).map((byte) => String.fromCharCode(byte)).join("");
		}
		return result;
	}


	private static resolveCharacterNameFromHash(rawName: string, characters: {[key: string]: RPObject}): string|undefined {
		Client.debugCharacterNameFlow("resolve_from_hash_start", {rawName});
		const decoded = Client.decodeCharacterNameFromHash(rawName);
		if (decoded) {
			Client.debugCharacterNameFlow("resolve_from_hash_decoded", {rawName, decoded});
			const matchFromDecoded = Client.findMatchingCharacterName(decoded, characters);
			if (matchFromDecoded) {
				Client.debugCharacterNameFlow("resolve_from_hash_match", {rawName, decoded, matchFromDecoded});
				return matchFromDecoded;
			}
		}
		const fallback = Client.findMatchingCharacterName(rawName, characters);
		Client.debugCharacterNameFlow("resolve_from_hash_fallback", {rawName, fallback});
		return fallback;
	}

	public static getCharacterDisplayName(key: string, entry: RPObject|undefined): string|undefined {
		let attributeName: string|undefined;
		let directName: string|undefined;
		let decodedKey: string|undefined;
		let sanitizedKey: string|undefined;
		let resolved: string|undefined;
		if (entry && typeof(entry) === "object") {
			const record = entry as any;
			attributeName = Client.sanitizeCharacterName(record?.a?.name);
			if (attributeName && Client.isValidCharacterName(attributeName)) {
				resolved = attributeName;
			}
			if (!resolved) {
				directName = Client.sanitizeCharacterName(record?.name);
				if (directName && Client.isValidCharacterName(directName)) {
					resolved = directName;
				}
			}
		}
		if (!resolved) {
			decodedKey = Client.decodeCharacterNameFromHash(key);
			if (decodedKey) {
				resolved = decodedKey;
			}
		}
		if (!resolved) {
			sanitizedKey = Client.sanitizeCharacterName(key);
			if (sanitizedKey && Client.isValidCharacterName(sanitizedKey)) {
				resolved = sanitizedKey;
			}
		}
		Client.debugCharacterNameFlow("display_name_resolved", {key, attributeName, directName, decodedKey, sanitizedKey, resolved});
		return resolved;
	}

	private static findMatchingCharacterName(candidate: string|undefined, characters: {[key: string]: RPObject}): string|undefined {
		Client.debugCharacterNameFlow("match_candidate_received", {candidate});
		const sanitizedCandidate = Client.sanitizeCharacterName(candidate);
		if (!sanitizedCandidate) {
			Client.debugCharacterNameFlow("match_candidate_rejected", {candidate, reason: "sanitize_failed"});
			return undefined;
		}
		const normalizedCandidate = Client.normalizeCharacterName(sanitizedCandidate);
		Client.debugCharacterNameFlow("match_candidate_normalized", {candidate, sanitizedCandidate, normalizedCandidate});
		if (!normalizedCandidate.length) {
			Client.debugCharacterNameFlow("match_candidate_rejected", {candidate, reason: "normalize_empty"});
			return undefined;
		}
		for (const key in characters) {
			if (!Object.prototype.hasOwnProperty.call(characters, key)) {
				continue;
			}
			const entry = characters[key];
			const displayName = Client.getCharacterDisplayName(key, entry);
			if (displayName && Client.normalizeCharacterName(displayName) === normalizedCandidate) {
				Client.debugCharacterNameFlow("match_candidate_success", {candidate, key, displayName});
				return displayName;
			}
			const sanitizedKey = Client.sanitizeCharacterName(key);
			if (sanitizedKey && Client.normalizeCharacterName(sanitizedKey) === normalizedCandidate) {
				Client.debugCharacterNameFlow("match_candidate_success", {candidate, key, displayName: sanitizedKey});
				return sanitizedKey;
			}
		}
		Client.debugCharacterNameFlow("match_candidate_not_found", {candidate, normalizedCandidate});
		return undefined;
	}

	private static normalizeCharacterName(value: string): string {
		const sanitized = Client.sanitizeCharacterName(value);
		if (!sanitized) {
			return "";
		}
		let normalized = sanitized;
		try {
			normalized = normalized.normalize("NFC");
		} catch (_e) {
			// ignore browsers without Unicode normalization support
		}
		return normalized.toLocaleLowerCase("pl-PL");
	}

	private static isNonEmptyString(value: unknown): value is string {
		return typeof value === "string" && value.trim().length > 0;
	}

	private static isValidCharacterName(value: string): boolean {
		if (!value.length) {
			return false;
		}
		for (let index = value.length - 1; index >= 0; index--) {
			const chr = value.charAt(index);
			if (!Client.isAllowedCharacter(chr)) {
				return false;
			}
		}
		const first = value.charAt(0);
		return Client.isAllowedFirstCharacter(first);
	}

	private static isAllowedCharacter(chr: string): boolean {
		const code = chr.charCodeAt(0);
		if ((code >= 0x30 && code <= 0x39) || (code >= 0x41 && code <= 0x5A) || (code >= 0x61 && code <= 0x7A)) {
			return true;
		}
		switch (chr) {
			case "ą":
			case "ć":
			case "ę":
			case "ł":
			case "ń":
			case "ó":
			case "ś":
			case "ź":
			case "ż":
			case "Ą":
			case "Ć":
			case "Ę":
			case "Ł":
			case "Ń":
			case "Ó":
			case "Ś":
			case "Ź":
			case "Ż":
			case "-":
			case "_":
			case ".":
			case " ":
			case "!":
			case "$":
			case "%":
			case "^":
			case "&":
			case "*":
			case "(":
			case ")":
			case "+":
			case "=":
			case "?":
			case "{":
			case "}":
				return true;
		}
		return false;
	}

	private static isAllowedFirstCharacter(chr: string): boolean {
		const code = chr.charCodeAt(0);
		if ((code >= 0x41 && code <= 0x5A) || (code >= 0x61 && code <= 0x7A)) {
			return true;
		}
		switch (chr) {
			case "ć":
			case "ł":
			case "ś":
			case "ź":
			case "ż":
			case "Ć":
			case "Ł":
			case "Ś":
			case "Ź":
			case "Ż":
				return true;
		}
		return false;
	}
}
