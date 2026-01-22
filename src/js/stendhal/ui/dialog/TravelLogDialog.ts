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
	private isMobile = false;
	private mobileView = false;
	private questsByTab: {[key: string]: QuestEntry[]} = {};
	private searchDebounceHandle?: number;

	constructor(dataItems?: string[]) {
		super("travellogdialog-template");
		ui.registerComponent(UIComponentEnum.TravelLogDialog, this);
		this.refresh();

		this.child(".travellogitems")!.innerHTML = "<option value=\"\">(ładowanie)</option>";
		this.isMobile = stendhal.session.touchOnly();
		this.mobileView = false;
		this.updateViewClasses();
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
		this.isMobile = stendhal.session.touchOnly();
		this.updateViewClasses();
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

		const backButton = this.child(".travellogdialog__back-button") as HTMLButtonElement | null;
		if (backButton) {
			backButton.addEventListener("click", () => {
				this.mobileView = false;
				this.updateViewClasses();
			});
		}

		this.componentElement.addEventListener("keydown", (event) => {
			this.onDialogKeyDown(event as KeyboardEvent);
		});
	}

	public updateTabs() {
		let activeTabId = "";
		document.querySelectorAll(".progressTypeButton").forEach((tab) => {
			const element = document.getElementById(tab.id)! as HTMLElement;
			if (element.id == this.currentProgressType) {
				// highlight selected tab
				element.classList.add("active");
				element.setAttribute("aria-selected", "true");
				element.setAttribute("tabindex", "0");
				activeTabId = element.id;
			} else {
				element.classList.remove("active");
				element.setAttribute("aria-selected", "false");
				element.setAttribute("tabindex", "-1");
			}
		});
		const tabpanel = this.child("#travellog-tabpanel");
		if (tabpanel && activeTabId) {
			tabpanel.setAttribute("aria-labelledby", activeTabId);
		}
	}

	private onProgressTypeButtonClick(event: Event) {
		// clear details when changing category
		this.refreshDetails();
		this.mobileView = false;
		this.updateViewClasses();

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
		if (this.isMobile) {
			this.mobileView = true;
			this.updateViewClasses();
		}
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

		const details = this.child(".travellogdetails")!;
		this.refreshDetails();
		const quest = this.findQuestById(selectedItem);
		const badges = quest ? this.deriveBadges(quest) : [];
		const miniProgress = quest ? this.extractMiniProgress(quest.progress) : null;

		const header = document.createElement("div");
		header.className = "travellogdialog__quest-header";

		const title = document.createElement("div");
		title.className = "travellogdialog__quest-title";
		title.textContent = selectedItem;
		header.appendChild(title);

		const meta = document.createElement("div");
		meta.className = "travellogdialog__quest-meta";

		if (badges.length > 0) {
			const badgeRow = document.createElement("div");
			badgeRow.className = "travellogdialog__quest-badges";
			for (const badge of badges) {
				const badgeEl = document.createElement("span");
				badgeEl.className = `quest-badge quest-badge--${badge.tone}`;
				badgeEl.textContent = badge.label;
				badgeRow.appendChild(badgeEl);
			}
			meta.appendChild(badgeRow);
		}

		if (miniProgress) {
			const progressEl = document.createElement("span");
			progressEl.className = "quest-mini-progress";
			progressEl.textContent = miniProgress;
			meta.appendChild(progressEl);
		}

		if (meta.childElementCount > 0) {
			header.appendChild(meta);
		}

		details.prepend(header);

		const sectionData = this.parseDetailSections(dataItems);
		const descriptionBody = details.querySelector(".travellogdialog__section--description .travellogdialog__section-body") as HTMLElement | null;
		const goalsBody = details.querySelector(".travellogdialog__section--goals .travellogdialog__section-body") as HTMLElement | null;
		const rewardsBody = details.querySelector(".travellogdialog__section--rewards .travellogdialog__section-body") as HTMLElement | null;
		const requirementsBody = details.querySelector(".travellogdialog__section--requirements .travellogdialog__section-body") as HTMLElement | null;

		this.fillTextSection(descriptionBody, description, "Brak opisu.");
		this.fillListSection(goalsBody, sectionData.goals, "Brak celów.");
		this.fillListSection(rewardsBody, sectionData.rewards, "Brak nagród.");
		this.fillListSection(requirementsBody, sectionData.requirements, "Brak wymagań lub kosztów.");
	}

	private refreshDetails() {
		const details = this.child(".travellogdetails")!;
		details.querySelectorAll(".travellogdialog__section-body").forEach((section) => {
			section.innerHTML = "";
			section.classList.remove("travellogdialog__section-body--empty");
		});
		details.querySelector(".travellogdialog__quest-header")?.remove();
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
			const badgeSuffix = badges.length > 0 ? ` (${badges.map((badge) => badge.label).join(", ")})` : "";
			const isSelected = quest.id === this.selectedQuestId;
			html += "<option value=\"" + stendhal.ui.html.esc(quest.id)
				+ "\" role=\"option\" aria-selected=\"" + (isSelected ? "true" : "false")
				+ "\" title=\"" + stendhal.ui.html.esc(label) + "\">"
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
		} else if (this.isMobile) {
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
			metadata = trimmed.split("|", 4).map((part) => part.trim());
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
		return { id, name, npc, location, progress, status, order: index, raw: trimmed, progressType };
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

	private deriveBadges(quest: QuestEntry): QuestBadge[] {
		const badges: QuestBadge[] = [];
		const normalized = quest.progressType.toLowerCase();
		if (normalized.includes("nowe")) {
			badges.push({ label: "NOWE", tone: "new" });
		} else if (normalized.includes("ukończ")) {
			badges.push({ label: "GOTOWE", tone: "done" });
		} else if (normalized.includes("ukry") || normalized.includes("anul")) {
			badges.push({ label: "ANULOWALNE", tone: "cancel" });
		} else if (normalized.includes("w trakcie") || normalized.includes("akty")) {
			badges.push({ label: "W TRAKCIE", tone: "progress" });
		}
		if (this.repeatable[quest.id]) {
			badges.push({ label: "POWTARZALNE", tone: "repeatable" });
		}
		return badges;
	}

	private extractMiniProgress(progress: string): string | null {
		const match = progress.match(/(\d+)\s*\/\s*(\d+)/);
		if (!match) {
			return null;
		}
		return `${match[1]}/${match[2]}`;
	}

	private findQuestById(id: string): QuestEntry | undefined {
		return this.getQuestsByTab(this.activeTab).find((quest) => quest.id === id);
	}

	private parseDetailSections(dataItems: string[]): QuestDetailSections {
		const sections: QuestDetailSections = { goals: [], rewards: [], requirements: [] };
		for (const item of dataItems) {
			const trimmed = item.trim();
			if (!trimmed) {
				continue;
			}
			const labelMatch = trimmed.match(/^([^:]+):\s*(.*)$/);
			const label = labelMatch ? labelMatch[1].trim().toLowerCase() : "";
			const remainder = labelMatch ? labelMatch[2].trim() : "";
			const payload = remainder || trimmed;
			if (label.startsWith("cel")) {
				sections.goals.push(payload);
			} else if (label.startsWith("nagrod")) {
				sections.rewards.push(payload);
			} else if (label.startsWith("wymagan") || label.startsWith("koszt")) {
				sections.requirements.push(payload);
			} else {
				sections.goals.push(trimmed);
			}
		}
		return sections;
	}

	private fillTextSection(section: HTMLElement | null, text: string, fallback: string) {
		if (!section) {
			return;
		}
		section.innerHTML = "";
		const trimmed = text.trim();
		if (!trimmed) {
			section.textContent = fallback;
			section.classList.add("travellogdialog__section-body--empty");
			return;
		}
		section.innerHTML = stendhal.ui.html.esc(trimmed);
	}

	private fillListSection(section: HTMLElement | null, items: string[], fallback: string) {
		if (!section) {
			return;
		}
		section.innerHTML = "";
		if (items.length === 0) {
			section.textContent = fallback;
			section.classList.add("travellogdialog__section-body--empty");
			return;
		}
		const ul = document.createElement("ul");
		ul.className = "uniform travellogdialog__list";
		for (const item of items) {
			ul.appendChild(this.buildDetailListItem(item));
		}
		section.appendChild(ul);
	}

	private buildDetailListItem(item: string): HTMLLIElement {
		const li = document.createElement("li");
		li.className = "uniform";
		let content: Array<string | HTMLElement> = [];
		const html = stendhal.ui.html.esc(item, ["em", "tally"]);
		if (html.includes("<tally>") && html.includes("</tally>")) {
			content = stendhal.ui.html.formatTallyMarks(html);
		} else {
			content = [html];
		}
		li.innerHTML = content[0] as string;
		if (content[1]) {
			li.appendChild(content[1] as HTMLElement);
			if (content[2]) {
				li.innerHTML += content[2] as string;
			}
		}
		return li;
	}

	private updateListboxSelection(selectedId: string) {
		this.componentElement.querySelectorAll(".travellogitems option").forEach((option) => {
			const isSelected = (option as HTMLOptionElement).value === selectedId;
			option.setAttribute("aria-selected", isSelected ? "true" : "false");
		});
	}

	private updateViewClasses() {
		this.componentElement.classList.toggle("is-mobile", this.isMobile);
		this.componentElement.classList.toggle("view-details", this.isMobile && this.mobileView);
		this.componentElement.classList.toggle("view-list", this.isMobile && !this.mobileView);
	}

	private onDialogKeyDown(event: KeyboardEvent) {
		if (event.key === "Escape") {
			event.preventDefault();
			this.close();
			return;
		}

		if (event.key !== "ArrowUp" && event.key !== "ArrowDown" && event.key !== "Enter") {
			return;
		}

		const target = event.target as HTMLElement | null;
		if (target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement || target?.isContentEditable) {
			return;
		}

		const itemList = this.child(".travellogitems") as HTMLSelectElement | null;
		if (!itemList || itemList.options.length === 0) {
			return;
		}

		event.preventDefault();

		let nextIndex = itemList.selectedIndex >= 0 ? itemList.selectedIndex : 0;
		if (event.key === "ArrowUp") {
			nextIndex = Math.max(0, nextIndex - 1);
		} else if (event.key === "ArrowDown") {
			nextIndex = Math.min(itemList.options.length - 1, nextIndex + 1);
		}

		itemList.selectedIndex = nextIndex;
		itemList.dispatchEvent(new Event("change"));
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
	progressType: string;
};

type QuestBadge = {
	label: string;
	tone: "new" | "progress" | "done" | "cancel" | "repeatable";
};

type QuestDetailSections = {
	goals: string[];
	rewards: string[];
	requirements: string[];
};
