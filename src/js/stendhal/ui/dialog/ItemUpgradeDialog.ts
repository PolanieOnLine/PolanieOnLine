/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/

import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { ItemRarity } from "../../data/ItemRarity";
import { singletons } from "../../SingletonRepo";
import { stendhal } from "../../stendhal";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

type UpgradeEventData = {[key: string]: any};

/** Web client window backed entirely by the server preview DTO. */
export class ItemUpgradeDialog extends DialogContentComponent {
	private static active?: ItemUpgradeDialog;

	private readonly icon = document.createElement("div");
	private readonly nameLabel = document.createElement("div");
	private readonly levelLabel = document.createElement("div");
	private readonly stats = document.createElement("div");
	private readonly chance = document.createElement("div");
	private readonly fee = document.createElement("div");
	private readonly materials = document.createElement("div");
	private readonly status = document.createElement("div");
	private readonly refreshButton: HTMLButtonElement;
	private readonly upgradeButton: HTMLButtonElement;

	private npcId = 0;
	private selectedPath?: string;
	private requestToken?: string;

	private constructor() {
		super("empty-div-template");
		this.componentElement.classList.add("item-upgrade-dialog");

		const itemHeading = document.createElement("h4");
		itemHeading.textContent = "Przedmiot do ulepszenia";
		this.componentElement.appendChild(itemHeading);

		const identity = document.createElement("div");
		identity.className = "item-upgrade-identity";
		this.icon.className = "item-upgrade-slot";
		this.icon.title = "Przeciągnij tutaj przedmiot z ekwipunku.";
		this.icon.addEventListener("dragover", (event: DragEvent) => {
			this.onDragOver(event);
		});
		this.icon.addEventListener("dragleave", () => {
			this.icon.classList.remove("item-upgrade-slot--accept");
		});
		this.icon.addEventListener("drop", (event: DragEvent) => {
			this.onDrop(event);
		});
		this.icon.addEventListener("touchend", (event: TouchEvent) => {
			this.onDrop(event);
		});
		identity.appendChild(this.icon);
		const identityText = document.createElement("div");
		this.nameLabel.className = "item-upgrade-name";
		this.nameLabel.textContent = "Przeciągnij tutaj przedmiot";
		this.levelLabel.textContent = "Przedmiot pozostanie w ekwipunku.";
		identityText.append(this.nameLabel, this.levelLabel);
		identity.appendChild(identityText);
		this.componentElement.appendChild(identity);

		this.stats.className = "item-upgrade-stats";
		this.componentElement.append(this.stats, this.chance, this.fee);
		const materialHeading = document.createElement("h4");
		materialHeading.textContent = "Materiały (posiadasz / wymagane)";
		this.componentElement.append(materialHeading, this.materials);
		this.status.className = "item-upgrade-status";
		this.componentElement.appendChild(this.status);

		this.refreshButton = this.addButton("Odśwież", () => {
			if (this.selectedPath) {
				this.send("preview", this.selectedPath);
			}
		});
		this.upgradeButton = this.addButton("Ulepsz", () => {
			if (this.selectedPath && this.requestToken) {
				this.upgradeButton.disabled = true;
				this.send("upgrade", this.selectedPath, this.requestToken);
			}
		});
	}

	public static show(data: UpgradeEventData): void {
		if (!ItemUpgradeDialog.active) {
			if (data.phase !== "open") return;
			const dialog = new ItemUpgradeDialog();
			const frame = new FloatingWindow("Ulepszanie przedmiotu", dialog, 24, 24);
			frame.setId("item-upgrade");
			dialog.setFrame(frame);
			ItemUpgradeDialog.active = dialog;
		}
		ItemUpgradeDialog.active.apply(data);
	}

	public override onParentClose(): void {
		ItemUpgradeDialog.active = undefined;
	}

	private apply(data: UpgradeEventData): void {
		if (data.npc_id !== undefined) this.npcId = Number(data.npc_id || 0);
		const state = String(data.status || "");
		const preservePreview = !data.name && this.isInteractionStatus(state)
				&& !!this.selectedPath;
		if (data.selected_path) {
			this.selectedPath = String(data.selected_path);
		} else if (!preservePreview) {
			this.selectedPath = undefined;
		}
		this.requestToken = data.request_token || undefined;

		if (data.name) {
			this.icon.replaceChildren();
			const sprite = singletons.getSpriteStore().get(Paths.sprites
					+ "/items/" + data["class"] + "/" + data.subclass + ".png");
			this.icon.appendChild(sprite.cloneNode());
			const rarity = ItemRarity.fromId(data.rarity_id) || ItemRarity.COMMON;
			this.nameLabel.textContent = data.name;
			this.nameLabel.style.color = rarity.colorHex;
			this.levelLabel.textContent = "Poziom ulepszenia: +" + data.upgrade_level
					+ " / +" + data.max_upgrade_level;
		} else if (!preservePreview) {
			this.icon.replaceChildren();
			this.nameLabel.textContent = state === "NO_UPGRADEABLE_ITEMS"
					? "Brak przedmiotu do ulepszenia"
					: "Przeciągnij tutaj przedmiot";
			this.nameLabel.style.color = "";
			this.levelLabel.textContent = "Przedmiot pozostanie w ekwipunku.";
		}

		if (data.name || !preservePreview) {
			this.fillStats(data);
			this.chance.textContent = data.success_percent !== undefined
					? "Szansa powodzenia: " + data.success_percent + "%"
					: "Szansa powodzenia: —";
			this.fee.textContent = data.fee_text ? "Koszt: " + data.fee_text
					: "Koszt: —";
			this.fillMaterials(data);
		}

		const canUpgrade = Number(data.can_upgrade || 0) === 1 && !!this.requestToken;
		this.upgradeButton.disabled = !canUpgrade;
		this.refreshButton.disabled = !this.selectedPath;
		this.status.textContent = data.message || this.statusText(state);
		this.status.dataset.status = state.toLowerCase();
		this.upgradeButton.title = canUpgrade ? "Wykonaj próbę ulepszenia"
				: this.statusText(state);
	}

	private fillStats(data: UpgradeEventData): void {
		this.stats.replaceChildren();
		const heading = document.createElement("h4");
		heading.textContent = "Statystyki po ulepszeniu";
		this.stats.appendChild(heading);
		const names = this.list(data.stat_names);
		const current = this.list(data.current_stat_values);
		const upgraded = this.list(data.upgraded_stat_values);
		const currentByName = this.valuesByName(names, current);
		const upgradedByName = this.valuesByName(names, upgraded);
		if (currentByName.damage_min !== undefined
				&& currentByName.damage_max !== undefined) {
			this.addStatRow("Obrażenia", currentByName.damage_min + "–"
					+ currentByName.damage_max, upgradedByName.damage_min + "–"
					+ upgradedByName.damage_max);
		}
		names.forEach((name, index) => {
			if (current[index] === undefined || upgraded[index] === undefined) return;
			if ((name === "damage_min" || name === "damage_max")
					&& currentByName.damage_min !== undefined
					&& currentByName.damage_max !== undefined) return;
			this.addStatRow(this.statLabel(name), current[index], upgraded[index]);
		});
		if (names.length === 0) {
			const row = document.createElement("div");
			row.textContent = "Po wybraniu przedmiotu zobaczysz zmianę statystyk.";
			this.stats.appendChild(row);
		}
	}

	private fillMaterials(data: UpgradeEventData): void {
		this.materials.replaceChildren();
		const names = this.list(data.material_names);
		const classes = this.list(data.material_classes);
		const subclasses = this.list(data.material_subclasses);
		const required = this.list(data.material_values);
		const owned = this.list(data.owned_material_values);
		this.materials.className = names.length > 0
				? "item-upgrade-materials" : "";
		names.forEach((name, index) => {
			const have = Number(owned[index] || 0);
			const need = Number(required[index] || 0);
			const available = have >= need;
			const card = document.createElement("div");
			card.className = "item-upgrade-material";
			card.title = name + ": posiadasz " + have + ", wymagane " + need;

			const slot = document.createElement("div");
			slot.className = "itemSlot item-upgrade-material-slot";
			if (classes[index] && subclasses[index]) {
				slot.style.backgroundImage = "url(" + singletons.getSpriteStore()
						.checkPath(Paths.sprites + "/items/" + classes[index]
								+ "/" + subclasses[index] + ".png") + ")";
				slot.style.backgroundPosition = "1px 1px";
			}
			slot.textContent = need > 1 ? String(need) : "";

			const label = document.createElement("div");
			label.textContent = name;
			const count = document.createElement("div");
			count.className = available ? "item-upgrade-owned"
					: "item-upgrade-missing";
			count.textContent = (available ? "✓ " : "✗ ") + have + " / " + need;
			card.append(slot, label, count);
			this.materials.appendChild(card);
		});
		if (names.length === 0) {
			this.materials.textContent = "Wymagania pojawią się po wybraniu przedmiotu.";
		}
	}

	private onDragOver(event: DragEvent): void {
		if (!stendhal.ui.heldObject) return;
		event.preventDefault();
		const accepted = this.canAcceptHeldItem();
		this.icon.classList.toggle("item-upgrade-slot--accept", accepted);
		if (event.dataTransfer) {
			event.dataTransfer.dropEffect = accepted ? "copy" : "none";
		}
	}

	private onDrop(event: DragEvent|TouchEvent): void {
		if (!stendhal.ui.heldObject) return;
		event.preventDefault();
		event.stopPropagation();
		const accepted = this.canAcceptHeldItem();
		const path = this.normalizePath(stendhal.ui.heldObject.path);
		stendhal.ui.heldObject = undefined;
		singletons.getHeldObjectManager().onRelease();
		this.icon.classList.remove("item-upgrade-slot--accept");
		if (!accepted) {
			this.status.textContent = "Tego przedmiotu nie można ulepszyć.";
			this.status.dataset.status = "invalid_item";
			return;
		}
		this.selectedPath = path;
		this.requestToken = undefined;
		this.upgradeButton.disabled = true;
		this.refreshButton.disabled = true;
		this.status.textContent = "Pobieranie podglądu z serwera…";
		this.status.dataset.status = "select_item";
		this.send("preview", path);
	}

	private canAcceptHeldItem(): boolean {
		if (!stendhal.ui.heldObject || !stendhal.ui.heldObject.path) return false;
		const path = this.normalizePath(stendhal.ui.heldObject.path).split("/");
		return path.length >= 3 && path[0] === String(marauroa.me["id"]);
	}

	private normalizePath(path: string): string {
		if (path.startsWith("[") && path.endsWith("]")) {
			return path.substring(1, path.length - 1).split("\t").join("/");
		}
		return path;
	}

	private addStatRow(name: string, current: string, upgraded: string): void {
		const row = document.createElement("div");
		const label = document.createElement("strong");
		label.textContent = name + ": ";
		const result = document.createElement("strong");
		result.className = "item-upgrade-owned";
		result.textContent = upgraded;
		row.append(label, current + "  →  ", result);
		this.stats.appendChild(row);
	}

	private valuesByName(names: string[], values: string[]): {[key: string]: string} {
		const result: {[key: string]: string} = {};
		names.forEach((name, index) => {
			if (values[index] !== undefined) result[name] = values[index];
		});
		return result;
	}

	private send(command: string, path: string, token?: string): void {
		if (!path || !this.npcId) return;
		const action: {[key: string]: any} = {
			type: "item_upgrade",
			command,
			npc_id: String(this.npcId),
			target_path: "[" + path.split("/").join("\t") + "]"
		};
		if (token) action.request_token = token;
		marauroa.clientFramework.sendAction(action);
	}

	private list(value: unknown): string[] {
		if (Array.isArray(value)) return value.map(String);
		if (typeof value !== "string" || value.length < 2) return [];
		return value.substring(1, value.length - 1).split(/\t/).filter(Boolean);
	}

	private statLabel(stat: string): string {
		return ({atk: "ATK", ratk: "RATK", def: "DEF", damage_min: "Obrażenia min.",
			damage_max: "Obrażenia max.", range: "Zasięg", rate: "Czas ataku"} as any)[stat] || stat;
	}

	private statusText(state: string): string {
		const messages: {[key: string]: string} = {
			SELECT_ITEM: "Przeciągnij przedmiot z ekwipunku do slotu.",
			NO_UPGRADEABLE_ITEMS: "Nie masz przedmiotu, który można ulepszyć.",
			READY: "Wymagania są spełnione.",
			NOT_ENOUGH_MONEY: "Brakuje pieniędzy.",
			MISSING_RESOURCES: "Brakuje materiałów.",
			INVALID_ITEM: "Tego przedmiotu nie można ulepszyć.",
			NOT_UPGRADEABLE: "Tego przedmiotu nie można ulepszyć.",
			MAX_LEVEL: "Osiągnięto maksymalny poziom.",
			STALE_PREVIEW: "Podgląd wygasł — stan został odświeżony.",
			NPC_TOO_FAR: "Musisz pozostać w pobliżu kowala.",
			FAILURE: "Próba ulepszenia nie powiodła się."
		};
		return messages[state] || "Nie można teraz ulepszyć przedmiotu.";
	}

	private isInteractionStatus(state: string): boolean {
		return state === "NPC_TOO_FAR";
	}
}
