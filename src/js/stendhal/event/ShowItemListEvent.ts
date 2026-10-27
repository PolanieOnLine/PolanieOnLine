/***************************************************************************
 *                 Copyright © 2024 - 2025  Faiumoni e. V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { RPEvent } from "marauroa"

import { ui } from "../ui/UI";
import { DialogContentComponent } from "../ui/toolkit/DialogContentComponent";
import { Chat } from "../util/Chat";
import { Paths } from "../data/Paths";
import { ItemRarity } from "../data/ItemRarity";
import { singletons } from "../SingletonRepo";
import { GameLanguage } from "../util/GameLanguage";

import { stendhal } from "../stendhal";

export class ShowItemListEvent extends RPEvent {
	/** Labels emitted by Item.describe; other text and numeric values stay intact. */
	private static readonly statLabels = new Set([
		"ATK", "SIŁA-ATAKU", "BONUS-ATAKU", "OBR", "BONUS-OBRONY", "STR", "WAGA",
		"ZMNIEJSZA-WAGĘ-BRONI", "PZ", "CYKL", "REGEN", "ZASIĘG", "KRADZIEŻ-ŻYCIA",
		"ŚWIATŁO", "MROK", "OGIEŃ", "LÓD", "NATURA", "WODA", "BONUS-PRECYZJI",
		"MIN-UŻYCIE", "MIN-POZIOM", "PODTRZYMUJĄCE-ŻYCIE", "SZANSA-NA-ATAK-KRYTYCZNY",
		"SILNIEJSZE-ATAKI-KRYTYCZNE", "ZWIĘKSZONA-KRADZIEŻ-ŻYCIA", "ZDROWIE", "CIĘŻAR",
		"Value", "Wartość"
	]);

	title!: string;
	caption!: string;
	content!: any;

	execute(_entity: any) {
		let title = "Przedmioty";
		let caption = "";
		let items = [];

		if (this.hasOwnProperty("title")) {
			title = GameLanguage.getText(this["title"]);
		}
		if (this.hasOwnProperty("caption")) {
			caption = GameLanguage.getText(this["caption"]);
		}
		if (this.hasOwnProperty("content")) {
			for (let obj in this["content"]) {
				if (this["content"].hasOwnProperty(obj)) {
					let data = this["content"][obj]["a"];
					let item = {
						name: data["name"],
						clazz: data["class"],
						subclass: data["subclass"],
						img: data["class"] + "/" + data["subclass"] + ".png",
						price: data["price"],
						desc: data["description_info"],
						rarity: ItemRarity.fromId(data["rarity_id"])
					}

					// seller shops prefix prices with "-"
					if (item.price.startsWith("-")) {
						item.price = item.price.substr(1);
					}
					items.push(item);
				}
			}
		}

		const content = new class extends DialogContentComponent { }("empty-div-template");
		content.componentElement.classList.add("shopsign");
		// Create table
		const table = document.createElement("table");
		table.className = "shoptable";
		content.componentElement.appendChild(table);

		// Caption
		const tableCaption = document.createElement("caption");
		tableCaption.className = "shopcaption";
		tableCaption.textContent = caption;
		table.appendChild(tableCaption);

		// Header
		const thead = document.createElement("thead");
		const headerRow = document.createElement("tr");

		const headers = ["Przedmiot", "Cena", "Opis"].map(text => GameLanguage.getText(text));
		for (const text of headers) {
			const th = document.createElement("th");
			th.textContent = text;
			headerRow.appendChild(th);
		}

		thead.appendChild(headerRow);
		table.appendChild(thead);

		// Body
		const tbody = document.createElement("tbody");

		for (const i of items) {
			const row = document.createElement("tr");
			if (i.rarity) {
				row.classList.add("item-rarity", i.rarity.cssClass);
			}

			// Item (image)
			const itemCell = document.createElement("td");
			itemCell.appendChild(
				singletons.getSpriteStore().get(
					Paths.sprites + "/items/" + i.img
				)
			);
			row.appendChild(itemCell);

			// Price
			const priceCell = document.createElement("td");
			priceCell.className = "number";
			priceCell.textContent = i.price;
			row.appendChild(priceCell);

			// Description
			const descCell = document.createElement("td");
			descCell.innerHTML = this.formatDescription(i.desc, i.name);
			row.appendChild(descCell);

			tbody.appendChild(row);
		}

		table.appendChild(tbody);

		stendhal.ui.globalInternalWindow.set(
			ui.createSingletonFloatingWindow(title, content, 20, 20));
	}

	private formatDescription(text: string, canonicalName?: string): string {
		if (canonicalName) {
			// Item.describe marks the name before the authored sentence is translated.
			text = text.split("§'" + canonicalName + "'").join(canonicalName);
		}
		// Statistics are appended by the server, outside the authored description.
		const statsStart = text.lastIndexOf(" Parametry (");
		text = statsStart < 0 ? GameLanguage.getText(text)
			: GameLanguage.getText(text.substring(0, statsStart)) + " "
				+ this.translateStatistics(text.substring(statsStart + 1));
		if (!text.includes("§") && canonicalName) {
			const name = GameLanguage.getText(canonicalName);
			const nameStart = name ? text.indexOf(name) : -1;
			if (nameStart >= 0) {
				const escapedName = name.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
				return Chat.formatLogEntry(text.substring(0, nameStart))
					+ '<span class="logi">' + escapedName + "</span>"
					+ Chat.formatLogEntry(text.substring(nameStart + name.length));
			}
		}
		return Chat.formatLogEntry(text);
	}

	private translateStatistics(text: string): string {
		const translated = GameLanguage.getText(text);
		if (translated !== text || !text.startsWith("Parametry (") || !text.endsWith(").")) return translated;
		const body = text.substring("Parametry (".length, text.length - 2).replace(
			/(?<!\S)([\p{L}-]+)(?=:)|(?<!\S)(ZMNIEJSZA-WAGĘ-BRONI)(?= |$)/gu,
			label => ShowItemListEvent.statLabels.has(label) ? GameLanguage.getText(label) : label);
		return GameLanguage.getText("Parametry") + " (" + body + ").";
	}
}

