/***************************************************************************
 *                   (C) Copyright 2022-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { singletons } from "../SingletonRepo";

import { ui } from "../ui/UI";
import { UIComponentEnum } from "../ui/UIComponentEnum";
import { FloatingWindow } from "../ui/toolkit/FloatingWindow";

import { SlashAction } from "./SlashAction";
import { ShowFloatingWindowComponent } from "../ui/component/ShowFloatingWindowComponent";
import { Panel } from "ui/toolkit/Panel";

import { Chat } from "../util/Chat";
import { Debug } from "../util/Debug";

declare var marauroa: any;
declare var stendhal: any;

/**
 * performances debugging actions
 */
export class DebugAction extends SlashAction {
	readonly minParams = 0;
	readonly maxParams = 0;
	private uiPopped = false;


	execute(_type: string, params: string[], _remainder: string): boolean {
		if (params.length < 1) {
			Chat.logH("error", "Oczekiwane parametry")
			this.showUsage();
		} else if (["help", "?"].indexOf(params[0]) > -1) {
			this.showUsage();
		} else if (params[0] === "ui") {
			this.uiAction(params);
		} else if (params[0] === "weather") {
			this.debugWeather(params[1]);
		} else if (params[0] === "log") {
			Chat.debugLogEnabled = true;
		} else if (params[0] === "settings") {
			Chat.log("client", "Ustawienia eksperymentalne " + (Debug.toggle("settings") ? "włączone"
					: "wyłączone"));
		} else if (params[0] === "touch") {
			Chat.log("client", "Debugowanie dotyku " + (Debug.toggle("touch") ? "włączone" : "wyłączone"));
		} else if (params[0] === "screencap") {
			Debug.setActive("screencap", !Debug.isActive("screencap"));
			Chat.log("client", "Debugowanie przechwytywania ekranu " + (Debug.isActive("screencap") ? "włączone"
					: "wyłączone"));
		}
		return true;
	}

	showUsage() {
		const usage = [
			"Użycie:",
			"  /debug log",
			"  /debug ui [pop]",
			"  /debug weather [<nazwa>]",
			"  /debug settings",
			"  /debug touch"
		];
		Chat.log("client", usage);
	}

	uiAction(params: string[]) {
		console.log(UIComponentEnum);
		for (let i in UIComponentEnum) {
			console.log("in", i);
		}
		if (params[1] === "pop") {
			this.uiPop();
		}
	}

	uiPop() {
		this.uiFloatComponent(UIComponentEnum.LeftPanel, "Lewy panel", 10, 10);
		this.uiFloatComponent(UIComponentEnum.RightPanel, "Prawy panel", 500, 10);
		this.uiFloatComponent(UIComponentEnum.TopPanel, "Górny panel", 200, 10);
		this.uiFloatComponent(UIComponentEnum.BottomPanel, "Dolny panel", 10, 500);

		this.uiFloatComponent(UIComponentEnum.MiniMap, "Mapa", 10, 50);
		this.uiFloatComponent(UIComponentEnum.PlayerStats, "Statystyki", 10, 190);
		this.uiFloatComponent(UIComponentEnum.BuddyList, "Przyjaciele", 10, 250);

		this.uiFloatComponent(UIComponentEnum.PlayerEquipment, marauroa.me["_name"], 500, 50);
		this.uiFloatComponent(UIComponentEnum.Bag, "Plecak", 500, 200);
		this.uiFloatComponent(UIComponentEnum.Keyring, "Rzemyk", 500, 300);
		this.uiFloatComponent(UIComponentEnum.MagicBag, "Magiczna torba", 500, 400);
		this.uiFloatComponent(UIComponentEnum.ChatInput, "Czat", 100, 500);
		this.uiFloatComponent(UIComponentEnum.ChatLog, "Dziennik czatu", 100, 560);

		this.uiPopped = true;
	}


	uiFloatComponent(uiComponentEnum: UIComponentEnum, title: string, x: number, y: number) {
		let component = ui.get(uiComponentEnum);
		if (!component) {
			return;
		}
		component.componentElement.dispatchEvent(new Event("close"));
		component.componentElement.remove();
		new FloatingWindow(title, component, x, y);

		if (!this.uiPopped) {
			let bottomPannel = ui.get(UIComponentEnum.BottomPanel) as Panel;
			bottomPannel.add(new ShowFloatingWindowComponent(uiComponentEnum, title, x, y))
		}
	}

	/**
	 * Sets weather animation for debugging.
	 *
	 * @param weather
	 *     Name of weather to be loaded. <code>undefined</code> turns
	 *     weather animation off.
	 */
	private debugWeather(weather?: string) {
		const usage = ["Użycie:", "  /debug weather [<nazwa>]"];
		if (weather && ["help", "?"].indexOf(weather) > -1) {
			Chat.log("client", usage);
			return;
		}
		if (!stendhal.config.getBoolean("effect.weather")) {
			Chat.logH("warning", "Pogoda jest wyłączona.");
		}

		if (weather) {
			weather = weather.replace(/ /g, "_");
			const wfilename = stendhal.paths.weather + "/" + weather + ".png";
			if (!stendhal.data.sprites.getCached(wfilename)) {
				Chat.logH("error", "nieznana pogoda: " + wfilename);
				return;
			}
		}

		singletons.getWeatherRenderer().update(weather);
	}

};
