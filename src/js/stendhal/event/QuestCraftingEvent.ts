/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { RPEvent } from "./RPEvent";

import { DialogContentComponent } from "../ui/toolkit/DialogContentComponent";
import { ui } from "../ui/UI";

declare var marauroa: any;
declare var stendhal: any;

interface CraftingItemInfo {
	name: string;
	quantity: number;
	clazz: string;
	subclass: string;
}

export class QuestCraftingEvent extends RPEvent {
	public execute(entity: any): void {
		if (!this.hasOwnProperty("product")) {
			console.error("QuestCraftingEvent missing product attribute");
			return;
		}

		const productParts = (this["product"] as string).split("|");
		if (productParts.length < 4) {
			console.error("QuestCraftingEvent product payload malformed", this["product"]);
			return;
		}

		const product: CraftingItemInfo = {
			name: productParts[0],
			quantity: parseInt(productParts[1], 10) || 1,
			clazz: productParts[2],
			subclass: productParts[3]
		};

		const requiredItems: CraftingItemInfo[] = [];
		if (this.hasOwnProperty("required") && this["required"]) {
			for (const entry of (this["required"] as string).split(";")) {
				if (!entry) {
					continue;
				}
				const parts = entry.split("|");
				if (parts.length < 4) {
					console.warn("QuestCraftingEvent required entry malformed", entry);
					continue;
				}
				requiredItems.push({
					name: parts[0],
					quantity: parseInt(parts[1], 10) || 1,
					clazz: parts[2],
					subclass: parts[3]
				});
			}
		}

		const content = new class extends DialogContentComponent {}("empty-div-template");
		content.setConfigId("quest_crafting");
		content.componentElement.classList.add("crafting-dialog");

		const columns = document.createElement("div");
		columns.className = "horizontalgroup crafting-columns";
		const leftColumn = document.createElement("div");
		leftColumn.className = "verticalgroup crafting-column";
		const rightColumn = document.createElement("div");
		rightColumn.className = "verticalgroup crafting-column";

		const leftTitle = document.createElement("div");
		leftTitle.className = "crafting-section-title";
		leftTitle.textContent = "Wymagane przedmioty";
		leftColumn.appendChild(leftTitle);

		if (requiredItems.length === 0) {
			const emptyInfo = document.createElement("div");
			emptyInfo.className = "crafting-empty";
			emptyInfo.textContent = "Brak danych o wymaganych przedmiotach.";
			leftColumn.appendChild(emptyInfo);
		} else {
			for (const item of requiredItems) {
				leftColumn.appendChild(this.createItemRow(item, true));
			}
		}

		const rightTitle = document.createElement("div");
		rightTitle.className = "crafting-section-title";
		rightTitle.textContent = "Rezultat";
		rightColumn.appendChild(rightTitle);
		rightColumn.appendChild(this.createItemRow(product, false));

		columns.appendChild(leftColumn);
		columns.appendChild(rightColumn);
		content.componentElement.appendChild(columns);

		const infoRow = document.createElement("div");
		infoRow.className = "horizontalgroup crafting-footer";

		const timeInfo = document.createElement("div");
		timeInfo.className = "crafting-info";
		if (this.hasOwnProperty("waiting_time")) {
			const wait = parseInt(this["waiting_time"], 10) || 0;
			if (wait > 0) {
				timeInfo.textContent = "Czas produkcji: " + wait + " min";
			}
		}
		infoRow.appendChild(timeInfo);

		const craftButton = document.createElement("button");
		craftButton.className = "dialogbutton";
		craftButton.textContent = this.hasOwnProperty("button") ? this["button"] : "Wytwórz";
		const canCraft = this.hasOwnProperty("can_craft");
		craftButton.disabled = !canCraft;
		craftButton.addEventListener("click", () => {
			const action: any = {
				type: "quest_craft",
				npc: this["npc"],
				quest: this["quest"],
				zone: marauroa.currentZoneName
			};
			marauroa.clientFramework.sendAction(action);
			content.componentElement.dispatchEvent(new Event("close"));
		});
		infoRow.appendChild(craftButton);
		content.componentElement.appendChild(infoRow);

		if (!canCraft) {
			const warning = document.createElement("div");
			warning.className = "crafting-warning";
			warning.textContent = "Brakuje wymaganych przedmiotów.";
			content.componentElement.appendChild(warning);
		}

		const title = this.hasOwnProperty("title") ? this["title"] : (product.name + " - wytwarzanie");
		stendhal.ui.globalInternalWindow.set(ui.createSingletonFloatingWindow(title, content, 20, 20));
	}

	private createItemRow(info: CraftingItemInfo, showQuantity: boolean): HTMLElement {
		const row = document.createElement("div");
		row.className = "crafting-entry";

		if (showQuantity) {
			const quantity = document.createElement("div");
			quantity.className = "crafting-quantity";
			quantity.textContent = info.quantity + "x";
			row.appendChild(quantity);
		}

		const iconWrapper = document.createElement("div");
		iconWrapper.className = "crafting-icon";
		iconWrapper.appendChild(stendhal.data.sprites.get(stendhal.paths.sprites
				+ "/items/" + info.clazz + "/" + info.subclass + ".png"));
		row.appendChild(iconWrapper);

		const name = document.createElement("div");
		name.className = "crafting-name";
		name.textContent = info.name;
		row.appendChild(name);

		return row;
	}
}
