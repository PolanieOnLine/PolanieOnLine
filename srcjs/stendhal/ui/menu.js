/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

"use strict";

window.marauroa = window.marauroa || {};
window.stendhal = window.stendhal || {};
stendhal.ui = stendhal.ui || {};

/**
 * game window aka world view
 */
stendhal.ui.menu = {

	onOpenAppMenu: function(e) {
		if (stendhal.ui.globalpopup) {
			stendhal.ui.globalpopup.popup.close();
		}

		var actions = [
			{
				title: "Konto",
				children: [
					{
						title: "Zmień Hasło",
						action: "changepassword",
					},
					{
						title: "Wybierz Postać",
						action: "characterselector",
					},
					{
						title: "Historia Logowania",
						action: "loginhistory",
					}
				]
			},
/*
			{
				title: "Tools",
				children: [
					{
						title: "Zrzut ekranu",
						action: "takescreenshot",
					},
					{
						title: "Ustawienia",
						action: "settings",
					}
				]
			},
*/
			{
				title: "Commands",
				children: [
					{
						title: "Atlas",
						action: "atlas",
					},
					{
						title: "Wojownicy Online",
						action: "who",
					},
					{
						title: "Aleja Sław",
						action: "halloffame",
					},
					{
						title: "Dziennik Zadań",
						action: "travellog",
					}
				]
			},
			{
				title: "Help",
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
						title: "Dla Początkujących",
						action: "beginnersguide",
					},
					{
						title: "List Komend",
						action: "help",
					},
					{
						title: "Regulamin",
						action: "rules",
					}
				]
			},
		]
		var that = this;
		
		var content = "<div class=\"actionmenu\">";
		for (var i = 0; i < actions.length; i++) {
			content += "<div class=\"inlineblock\"><h4 class=\"menugroup\">" + stendhal.ui.html.esc(actions[i].title) + "</h4>"
			for (var j = 0; j < actions[i].children.length; j++) {
				content += "<button id=\"menubutton." + actions[i].children[j].action + "\">" + stendhal.ui.html.esc(actions[i].children[j].title) + "</button><br>";
			}
			content += "</div>";
		}
		content += "</div>";
		this.popup = new stendhal.ui.Popup("Akcja", content, 150, e.pageY + 20);

		this.popup.popupdiv.addEventListener("click", function(e) {
			var cmd = e.target.id.substring(11);
			that.popup.close();
			console.log(cmd);
			stendhal.slashActionRepository.execute("/" + cmd);
		});

		this.close = function() {
			this.popup.close();
			stendhal.ui.globalpopup = null;
		}
		stendhal.ui.globalpopup = this;
	}

}