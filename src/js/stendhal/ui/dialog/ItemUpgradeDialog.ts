/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/

import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { ItemRarity } from "../../data/ItemRarity";
import { singletons } from "../../SingletonRepo";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

type UpgradeEventData = {[key: string]: any};

/** Web client window backed entirely by the server preview DTO. */
export class ItemUpgradeDialog extends DialogContentComponent {
	private static active?: ItemUpgradeDialog;

	private readonly select = document.createElement("select");
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

	private applying = false;
	private npcId = 0;
	private selectedPath?: string;
	private requestToken?: string;

	private constructor() {
		super("empty-div-template");
		this.componentElement.classList.add("item-upgrade-dialog");

		const selectorLabel = document.createElement("label");
		selectorLabel.textContent = "Przedmiot do ulepszenia";
		selectorLabel.appendChild(this.select);
		this.componentElement.appendChild(selectorLabel);
		this.select.addEventListener("change", () => {
			if (!this.applying) {
				this.send("preview", this.select.value);
			}
		});

		const identity = document.createElement("div");
		identity.className = "item-upgrade-identity";
		this.icon.className = "item-upgrade-slot";
		identity.appendChild(this.icon);
		const identityText = document.createElement("div");
		this.nameLabel.className = "item-upgrade-name";
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
		this.upgradeButton = this.addButton("ULEPSZ", () => {
			if (this.selectedPath && this.requestToken) {
				this.upgradeButton.disabled = true;
				this.send("upgrade", this.selectedPath, this.requestToken);
			}
		});
	}

	public static show(data: UpgradeEventData): void {
		if (!ItemUpgradeDialog.active) {
			const dialog = new ItemUpgradeDialog();
			const frame = new FloatingWindow("ULEPSZANIE PRZEDMIOTU", dialog, 24, 24);
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
		this.applying = true;
		this.npcId = Number(data.npc_id || 0);
		this.selectedPath = data.selected_path || undefined;
		this.requestToken = data.request_token || undefined;

		const paths = this.list(data.candidate_paths);
		const names = this.list(data.candidate_names);
		this.select.replaceChildren();
		paths.forEach((path, index) => {
			const option = document.createElement("option");
			option.value = path;
			option.textContent = names[index] || path;
			option.selected = path === this.selectedPath;
			this.select.appendChild(option);
		});
		this.select.disabled = paths.length === 0;

		this.icon.replaceChildren();
		if (data.name) {
			const sprite = singletons.getSpriteStore().get(Paths.sprites
					+ "/items/" + data["class"] + "/" + data.subclass + ".png");
			this.icon.appendChild(sprite.cloneNode());
			const rarity = ItemRarity.fromId(data.rarity_id) || ItemRarity.COMMON;
			this.nameLabel.textContent = data.name;
			this.nameLabel.style.color = rarity.colorHex;
			this.levelLabel.textContent = "Poziom ulepszenia: +" + data.upgrade_level
					+ " / +" + data.max_upgrade_level;
		} else {
			this.nameLabel.textContent = "Brak przedmiotu do ulepszenia";
			this.levelLabel.textContent = "";
		}

		this.fillStats(data);
		this.chance.textContent = data.success_percent !== undefined
				? "Szansa powodzenia: " + data.success_percent + "%" : "";
		this.fee.textContent = data.fee_text ? "Koszt: " + data.fee_text : "";
		this.fillMaterials(data);

		const canUpgrade = Number(data.can_upgrade || 0) === 1 && !!this.requestToken;
		this.upgradeButton.disabled = !canUpgrade;
		this.refreshButton.disabled = !this.selectedPath;
		const state = String(data.status || "");
		this.status.textContent = data.message || this.statusText(state);
		this.status.dataset.status = state.toLowerCase();
		this.upgradeButton.title = canUpgrade ? "Wykonaj próbę ulepszenia"
				: this.statusText(state);
		this.applying = false;
	}

	private fillStats(data: UpgradeEventData): void {
		this.stats.replaceChildren();
		const heading = document.createElement("h4");
		heading.textContent = "Statystyki po ulepszeniu";
		this.stats.appendChild(heading);
		const names = this.list(data.stat_names);
		const current = this.list(data.current_stat_values);
		const upgraded = this.list(data.upgraded_stat_values);
		names.forEach((name, index) => {
			if (current[index] === undefined || upgraded[index] === undefined) return;
			const row = document.createElement("div");
			row.textContent = this.statLabel(name) + ": " + current[index]
					+ "  →  " + upgraded[index];
			this.stats.appendChild(row);
		});
	}

	private fillMaterials(data: UpgradeEventData): void {
		this.materials.replaceChildren();
		const names = this.list(data.material_names);
		const required = this.list(data.material_values);
		const owned = this.list(data.owned_material_values);
		names.forEach((name, index) => {
			const have = Number(owned[index] || 0);
			const need = Number(required[index] || 0);
			const row = document.createElement("div");
			row.className = have >= need ? "item-upgrade-owned" : "item-upgrade-missing";
			row.textContent = (have >= need ? "✓ " : "✗ ") + name + ": "
					+ have + " / " + need;
			this.materials.appendChild(row);
		});
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
			READY: "Wymagania są spełnione.",
			NOT_ENOUGH_MONEY: "Brakuje pieniędzy.",
			MISSING_RESOURCES: "Brakuje materiałów.",
			MAX_LEVEL: "Osiągnięto maksymalny poziom.",
			STALE_PREVIEW: "Podgląd wygasł — stan został odświeżony.",
			NPC_TOO_FAR: "Musisz pozostać przy kowalu.",
			FAILURE: "Próba ulepszenia nie powiodła się."
		};
		return messages[state] || "Nie można teraz ulepszyć przedmiotu.";
	}
}
