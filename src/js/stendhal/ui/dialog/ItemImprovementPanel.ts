/***************************************************************************
	*                      (C) Copyright 2024 - PolanieOnLine                 *
	***************************************************************************/
/***************************************************************************
	*                                                                         *
	*   This program is free software; you can redistribute it and/or modify  *
	*   it under the terms of the GNU Affero General Public License as        *
	*   published by the Free Software Foundation; either version 3 of the    *
	*   License, or (at your option) any later version.                       *
	*                                                                         *
	***************************************************************************/

import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { ui } from "../UI";
import { UIComponentEnum } from "../UIComponentEnum";

import { ItemImprovementEntry } from "./ItemImprovementTypes";
import { ItemImprovementController } from "./ItemImprovementController";
import { ItemImproveConfirmDialog } from "./ItemImproveConfirmDialog";

declare var stendhal: any;

/**
	* Dialog showing improvable items with icons rendered via sprite images.
	*/
export class ItemImprovementPanel extends DialogContentComponent {
	private entries: ItemImprovementEntry[] = [];
	private scrollContainer: HTMLElement;
	private npcName: string;

	constructor(npcName: string) {
	super("empty-div-template");
	ui.registerComponent(UIComponentEnum.ItemImprovementDialog, this);
	this.npcName = npcName;
	this.componentElement.classList.add("pad-contents");
	this.componentElement.classList.add("improve-panel");
	this.scrollContainer = document.createElement("div");
	this.scrollContainer.className = "improve-scroll";
	this.buildHeader();
	this.componentElement.appendChild(this.scrollContainer);
	}

	public override getConfigId(): string {
	return "item-improvement";
	}

	public setNpcName(npc: string) {
	this.npcName = npc;
	}

	public matchesNpc(npc: string): boolean {
	return this.npcName === npc;
	}

	public setEntries(entries: ItemImprovementEntry[]) {
	this.entries = entries;
	this.renderRows();
	}

	public reload() {
	ItemImprovementController.requestList(this.npcName);
	}

	private buildHeader() {
	const header = document.createElement("div");
	header.className = "improve-row improve-header";
	header.appendChild(this.headerCell("Ikona"));
	header.appendChild(this.headerCell("Przedmiot"));
	header.appendChild(this.headerCell("Materiały"));
	header.appendChild(this.headerCell("Koszt"));
	header.appendChild(this.headerCell("Akcja"));
	this.componentElement.appendChild(header);
	}

	private headerCell(text: string): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell";
	cell.textContent = text;
	return cell;
	}

	private renderRows() {
	this.scrollContainer.innerHTML = "";
	for (const entry of this.entries) {
	this.scrollContainer.appendChild(this.buildRow(entry));
	}
	}

	private buildRow(entry: ItemImprovementEntry): HTMLElement {
	const row = document.createElement("div");
	row.className = "improve-row";

	row.appendChild(this.iconCell(entry));
	row.appendChild(this.nameCell(entry));
	row.appendChild(this.requirementsCell(entry));
	row.appendChild(this.costCell(entry));
	row.appendChild(this.actionCell(entry));

	return row;
	}

	private iconCell(entry: ItemImprovementEntry): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell improve-icon";
	if (entry.icon) {
	const img = document.createElement("img");
	img.src = this.iconPath(entry.icon);
	img.width = 32;
	img.height = 32;
	cell.appendChild(img);
	}
	return cell;
	}

	private iconPath(icon: string): string {
	const trimmed = icon.endsWith(".png") ? icon : icon + ".png";
	return stendhal.paths.sprites + "/" + trimmed;
	}

	private nameCell(entry: ItemImprovementEntry): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell";
	cell.textContent = entry.name + this.levelSuffix(entry);
	return cell;
	}

	private levelSuffix(entry: ItemImprovementEntry): string {
	if (entry.improve <= 0) {
	return "";
	}
	return " (+" + entry.improve + ")";
	}

	private requirementsCell(entry: ItemImprovementEntry): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell";
	const parts: string[] = [];
	for (const key of Object.keys(entry.requirements)) {
	parts.push(entry.requirements[key] + " x " + key);
	}
	cell.textContent = parts.join(", ");
	return cell;
	}

	private costCell(entry: ItemImprovementEntry): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell improve-cost";
	cell.textContent = entry.cost.toString();
	return cell;
	}

	private actionCell(entry: ItemImprovementEntry): HTMLElement {
	const cell = document.createElement("div");
	cell.className = "improve-cell";
	const button = document.createElement("button");
	button.className = "dialogbutton";
	button.textContent = "Ulepsz";
	button.addEventListener("click", () => {
	new ItemImproveConfirmDialog(entry, this.npcName, () => {
	ItemImprovementController.requestUpgrade(this.npcName, entry);
	});
	});
	cell.appendChild(button);
	return cell;
	}
	}
