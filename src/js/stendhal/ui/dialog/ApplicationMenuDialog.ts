/***************************************************************************
 *                (C) Copyright 2003-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;

import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { singletons } from "../../SingletonRepo";

import { Debug } from "../../util/Debug";
import { ScreenCapture } from "../../util/ScreenCapture";


interface MenuAction {
	title: string,
	action: string,
	alt?: string,
	condition?: Function
}

export class ApplicationMenuDialog extends DialogContentComponent {

	private actions = [
			{
				title: "Konto",
				children: [
					{
						title: "Wybierz postać",
						action: "characterselector"
					},
					{
						title: "Historia logowań",
						action: "loginhistory"
					},
					{
						title: "Zmień hasło",
						action: "changepassword"
					},
					{
						title: "Wyloguj",
						action: "logout"
					}
				] as MenuAction[]
			},
			{
				title: "Narzędzia",
				children: [
					{
						title: "Wykonaj zrzut ekranu",
						action: "screenshot",
					},
					/*
					{
						title: "Nagraj wideo",
						alt: "Zatrzymaj nagrywanie",
						condition: ScreenCapture.isActive,
						action: "screencap"
					},
					*/
					{
						title: "Ustawienia",
						action: "settings",
					}
				] as MenuAction[]
			},
			{
				title: "Komendy",
				children: [
					{
						title: "Atlas",
						action: "atlas",
					},
					{
						title: "Gracze online",
						action: "who",
					},
					{
						title: "Aleja sław",
						action: "halloffame",
					},
					{
						title: "Dziennik zadań",
						action: "progressstatus",
					}
				] as MenuAction[]
			},
			{
				title: "Pomoc",
				children: [
					{
						title: "Przewodnik",
						action: "manual",
					},
					{
						title: "FAQ",
						action: "faq",
					},
					{
						title: "Dla początkujących",
						action: "beginnersguide",
					},
					{
						title: "Polecenia",
						action: "help",
					},
					{
						title: "Regulamin",
						action: "rules",
					}
				] as MenuAction[]
			},
		]

	constructor() {
		super("applicationmenudialog-template");

		if (Debug.isActive("screencap")) {
			this.actions[1].children.push({
				title: "Nagraj wideo",
				alt: "Zatrzymaj nagrywanie",
				condition: ScreenCapture.isActive,
				action: "screencap"
			});
		}

		var content = "";
		for (var i = 0; i < this.actions.length; i++) {
			content += "<div class=\"inlineblock buttonColumn\"><h4 class=\"menugroup\">" + stendhal.ui.html.esc(this.actions[i].title) + "</h4>"
			for (var j = 0; j < this.actions[i].children.length; j++) {
				const action = this.actions[i].children[j];
				let title = action.title;
				if (action.alt && action.condition && action.condition()) {
					title = action.alt;
				}
				content += "<button id=\"menubutton." + action.action + "\" class=\"menubutton\">" + stendhal.ui.html.esc(title) + "</button><br>";
			}
			content += "</div>";
		}
		this.componentElement.innerHTML = content;

		this.componentElement.addEventListener("click", (event) => {
			this.onClick(event);
		});
	}

	public override getConfigId(): string {
		return "menu";
	}

	private onClick(event: Event) {
		var cmd = (event.target as HTMLInputElement).id?.substring(11);
		if (cmd) {
			singletons.getSlashActionRepo().execute("/" + cmd);
			this.componentElement.dispatchEvent(new Event("close"));
		}
		event.preventDefault();

	}
}
