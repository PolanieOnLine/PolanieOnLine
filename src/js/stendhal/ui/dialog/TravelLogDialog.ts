/***************************************************************************
 *                (C) Copyright 2007-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Paths } from "../../data/Paths";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { ui } from "../UI";
import { UIComponentEnum } from "../UIComponentEnum";

declare var marauroa: any;
declare var stendhal: any;


/**
 * a dialog to display images
 */
export class TravelLogDialog extends DialogContentComponent {
	private currentProgressType = "";
	private repeatable: {[key: string]: boolean;} = {};
	private activeTab = "";
	private query = "";
	private activeFilters = new Set<string>();
	private sortMode = "recent";
	private selectedQuestId = "";
	private mobileView = false;
	private questsByTab: {[key: string]: QuestEntry[]} = {};
	private searchDebounceHandle?: number;

	constructor(dataItems?: string[]) {
		super("travellogdialog-template");
		ui.registerComponent(UIComponentEnum.TravelLogDialog, this);
		this.refresh();

		this.child(".travellogitems")!.innerHTML = "<option value=\"\">(ładowanie)</option>";
		this.mobileView = stendhal.session.touchOnly();
		if (dataItems) {
			this.setDataItems(dataItems);
		}
	}

	public setDataItems(dataItems: string[]) {
		this.createHtml(dataItems);
		this.activeTab = dataItems[0] ?? "";

		// trigger loading of content for first entry
		this.currentProgressType = this.activeTab;
		this.updateTabs();
		var action = {
			"type":           "progressstatus",
			"progress_type":  this.currentProgressType
		}
		marauroa.clientFramework.sendAction(action);
	}

	public override refresh() {
		this.componentElement.style.setProperty("font-family", stendhal.config.get("font.travel-log"));
		this.mobileView = stendhal.session.touchOnly();
	}

	public override getConfigId(): string {
		return "travel-log";
	}

	private createHtml(dataItems: string[]) {
		let buttons = "";
		for (var i = 0; i < dataItems.length; i++) {
			buttons = buttons + "<button id=\"" + stendhal.ui.html.esc(dataItems[i])
				+ "\" class=\"progressTypeButton\" role=\"tab\" aria-selected=\"false\""
				+ " aria-controls=\"travellog-tabpanel\" tabindex=\"-1\">"
				+ stendhal.ui.html.esc(dataItems[i]) + "</button>";
		}
		this.child(".tavellogtabpanel")!.innerHTML = buttons;

		this.componentElement.querySelectorAll(".progressTypeButton").forEach((button) => {
			button.addEventListener("click", (event) => {
				this.onProgressTypeButtonClick(event);
			});
		});

		this.child(".travellogitems")!.addEventListener("change", (event) => {
			this.onTravelLogItemsChange(event);
		});

		const searchInput = this.child(".travellogdialog__search-input") as HTMLInputElement | null;
		if (searchInput) {
			searchInput.addEventListener("input", () => {
				this.queueSearchUpdate(searchInput.value);
			});
		}

		this.componentElement.querySelectorAll(".travellogdialog__chip").forEach((chip) => {
			chip.addEventListener("click", () => {
				const label = chip.textContent?.trim();
				if (!label) {
					return;
				}
				if (this.activeFilters.has(label)) {
					this.activeFilters.delete(label);
					chip.classList.remove("active");
				} else {
					this.activeFilters.add(label);
					chip.classList.add("active");
				}
				this.renderQuestList();
			});
		});

		const sortSelect = this.child("#travellog-sort") as HTMLSelectElement | null;
		if (sortSelect) {
			sortSelect.addEventListener("change", () => {
				this.sortMode = sortSelect.value;
				this.renderQuestList();
			});
		}
	}

	public updateTabs() {
		let activeTabElement: HTMLElement | null = null;
		document.querySelectorAll(".progressTypeButton").forEach((tab) => {
			const element = document.getElementById(tab.id)! as HTMLElement;
			if (element.id == this.currentProgressType) {
				// highlight selected tab
				element.classList.add("active");
				element.setAttribute("aria-selected", "true");
				element.setAttribute("tabindex", "0");
				activeTabElement = element;
			} else {
				element.classList.remove("active");
				element.setAttribute("aria-selected", "false");
				element.setAttribute("tabindex", "-1");
			}
		});
		const tabpanel = this.child("#travellog-tabpanel");
		if (tabpanel && activeTabElement) {
			tabpanel.setAttribute("aria-labelledby", activeTabElement.id);
		}
	}

	private onProgressTypeButtonClick(event: Event) {
		// clear details when changing category
		this.refreshDetails();

		this.currentProgressType = (event.target as HTMLElement).id;
		this.activeTab = this.currentProgressType;
		this.updateTabs();
		var action = {
			"type":           "progressstatus",
			"progress_type":  this.currentProgressType
		};
		marauroa.clientFramework.sendAction(action);
		this.renderQuestList();

		// request repeatable quests
		if (this.currentProgressType === "Ukończone zadania") {
			marauroa.clientFramework.sendAction({
				"type": "progressstatus",
				"progress_type": "repeatable"
			});
		}
	}

	private onTravelLogItemsChange(event: Event) {
		const value = (event.target as HTMLInputElement).value;
		if (!value) {
			// ignore options without value
			return;
		}
		this.selectedQuestId = value;
		var action = {
			"type":           "progressstatus",
			"progress_type":  this.currentProgressType,
			"item":           value
		};
		marauroa.clientFramework.sendAction(action);
		this.updateListboxSelection(value);
	}


	public progressTypeData(progressType: string, dataItems: string[]) {
		if (progressType !== this.currentProgressType) {
			return;
		}
		if (dataItems.length == 1 && dataItems[0] === "") {
			// prevent infinitely re-sending request when list is technically empty
			dataItems = [];
		}
		// sort items alphabetically
		dataItems.sort();
		this.questsByTab[progressType] = dataItems.map((item, index) =>
			this.parseQuestEntry(item, index, progressType));
		this.renderQuestList();
	}


	public itemData(progressType: string, selectedItem: string, description: string, dataItems: string[]) {
		if (progressType !== this.currentProgressType) {
			return;
		}
		this.selectedQuestId = selectedItem;

		const detailsSpan = document.createElement("span");

		detailsSpan.innerHTML = "<h3>" + stendhal.ui.html.esc(selectedItem) + "</h3>";
		if (this.repeatable[selectedItem]) {
			detailsSpan.innerHTML += "<p id=\"travellogrepeatable\">"
				+ "<img src=\"" + Paths.gui + "/rp.png\" /> <em>Mogę wykonać to zadanie jeszcze raz.</em></p>";
		}

		detailsSpan.innerHTML += "<p id=\"travellogdescription\">"
				+ stendhal.ui.html.esc(description) + "</p>";

		const ul = document.createElement("ul");
		ul.className = "uniform";

		for (var i = 0; i < dataItems.length; i++) {
			let content = []
			let html = stendhal.ui.html.esc(dataItems[i], ["em", "tally"]);
			if (html.includes("<tally>") && html.includes("</tally>")) {
				content = stendhal.ui.html.formatTallyMarks(html);
			} else {
				content.push(html);
			}

			const li = document.createElement("li");
			li.className = "uniform";
			li.innerHTML = content[0];
			if (content[1]) {
				li.appendChild(content[1]);

				if (content[2]) {
					li.innerHTML += content[2];
				}
			}

			ul.appendChild(li);
		}

		detailsSpan.appendChild(ul);
		this.refreshDetails("", detailsSpan);
	}

	private refreshDetails(html: string="", newDetails?: HTMLElement) {
		const details = this.child(".travellogdetails")!;
		details.innerHTML = html;

		if (newDetails) {
			details.appendChild(newDetails);
		}
	}

	public override onParentClose() {
		ui.unregisterComponent(this);
	}

	public setRepeatable(dataItems: string[]) {
		const repeatable = {} as {[key: string]: boolean};
		for (const item of dataItems) {
			repeatable[item] = true;
		}

		this.repeatable = repeatable;
		this.renderQuestList();
	}

	private queueSearchUpdate(value: string) {
		if (this.searchDebounceHandle) {
			window.clearTimeout(this.searchDebounceHandle);
		}
		this.searchDebounceHandle = window.setTimeout(() => {
			this.query = value.trim();
			this.renderQuestList();
		}, 200);
	}

	private renderQuestList() {
		const quests = this.sortQuests(this.filterQuests(this.getQuestsByTab(this.activeTab)));
		const itemList = this.child(".travellogitems")! as HTMLSelectElement;
		let html = "";
		for (const quest of quests) {
			const label = this.formatQuestOption(quest);
			const badges = this.deriveBadges(quest);
			const badgeSuffix = badges.length > 0 ? ` (${badges.join(", ")})` : "";
			const isSelected = quest.id === this.selectedQuestId;
			html += "<option value=\"" + stendhal.ui.html.esc(quest.id)
				+ "\" role=\"option\" aria-selected=\"" + (isSelected ? "true" : "false") + "\">"
				+ stendhal.ui.html.esc(label + badgeSuffix) + "</option>";
		}
		itemList.innerHTML = html;

		if (quests.length > 0) {
			const selectedIndex = quests.findIndex((quest) => quest.id === this.selectedQuestId);
			itemList.selectedIndex = selectedIndex >= 0 ? selectedIndex : 0;
			if (selectedIndex < 0 || !this.selectedQuestId) {
				this.selectedQuestId = quests[itemList.selectedIndex].id;
				itemList.dispatchEvent(new Event("change"));
			} else {
				this.updateListboxSelection(this.selectedQuestId);
			}
		} else if (this.mobileView) {
			itemList.innerHTML = "<option value=\"\" role=\"option\" aria-selected=\"true\">(nic)</option>";
			itemList.selectedIndex = 0;
		}
	}

	private formatQuestOption(quest: QuestEntry): string {
		const npc = quest.npc || "brak NPC";
		const location = quest.location || "brak lokacji";
		const progress = quest.progress || "brak postępu";
		return `${quest.name} • ${npc} • ${location} • ${progress}`;
	}

	private parseQuestEntry(raw: string, index: number, progressType: string): QuestEntry {
		const trimmed = raw.trim();
		let name = trimmed || `Zadanie ${index + 1}`;
		let npc = "";
		let location = "";
		let progress = "";
		let metadata: string[] = [];
		if (trimmed.includes("|")) {
			metadata = trimmed.split("|").map((part) => part.trim());
		} else if (trimmed.includes(" - ")) {
			metadata = trimmed.split(" - ").map((part) => part.trim());
		}
		if (metadata.length > 0) {
			name = metadata[0] || name;
			npc = metadata[1] || "";
			location = metadata[2] || "";
			progress = metadata[3] || "";
		}
		const status = this.deriveStatus(progressType);
		const id = trimmed || name || `${progressType}-${index}`;
		return { id, name, npc, location, progress, status, order: index, raw: trimmed };
	}

	private deriveStatus(progressType: string): string {
		const normalized = progressType.toLowerCase();
		if (normalized.includes("ukończ")) {
			return "Ukończone";
		}
		if (normalized.includes("ukry")) {
			return "Ukryte";
		}
		return "Aktywne";
	}

	private getQuestsByTab(tab: string): QuestEntry[] {
		return this.questsByTab[tab] ?? [];
	}

	private filterQuests(quests: QuestEntry[]): QuestEntry[] {
		let filtered = quests;
		if (this.query) {
			const needle = this.query.toLowerCase();
			filtered = filtered.filter((quest) =>
				[quest.name, quest.npc, quest.location, quest.progress, quest.raw]
					.filter(Boolean)
					.some((field) => field!.toLowerCase().includes(needle))
			);
		}
		if (this.activeFilters.size > 0) {
			filtered = filtered.filter((quest) => this.activeFilters.has(quest.status));
		}
		return filtered;
	}

	private sortQuests(quests: QuestEntry[]): QuestEntry[] {
		const sorted = [...quests];
		if (this.sortMode === "name") {
			sorted.sort((a, b) => a.name.localeCompare(b.name));
			return sorted;
		}
		if (this.sortMode === "region") {
			sorted.sort((a, b) => a.location.localeCompare(b.location));
			return sorted;
		}
		sorted.sort((a, b) => a.order - b.order);
		return sorted;
	}

	private deriveBadges(quest: QuestEntry): string[] {
		const badges: string[] = [];
		if (quest.status && quest.status !== "Aktywne") {
			badges.push(quest.status);
		}
		if (this.repeatable[quest.id]) {
			badges.push("R");
		}
		return badges;
	}

	private updateListboxSelection(selectedId: string) {
		this.componentElement.querySelectorAll(".travellogitems option").forEach((option) => {
			const isSelected = (option as HTMLOptionElement).value === selectedId;
			option.setAttribute("aria-selected", isSelected ? "true" : "false");
		});
	}
}

type QuestEntry = {
	id: string;
	name: string;
	npc: string;
	location: string;
	progress: string;
	status: string;
	order: number;
	raw: string;
};
