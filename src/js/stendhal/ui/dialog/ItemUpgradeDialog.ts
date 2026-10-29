/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/

import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { ItemRarity } from "../../data/ItemRarity";
import { Item } from "../../entity/Item";
import { singletons } from "../../SingletonRepo";
import { stendhal } from "../../stendhal";
import { ui } from "../UI";
import { ActionContextMenu } from "./ActionContextMenu";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { getItemSlotLayers } from "../component/ItemSlotPresentation";
import { GameLanguage } from "../../util/GameLanguage";
import { glyphColourFilter } from "../../data/GlyphAppearance";

type UpgradeEventData = {[key: string]: any};

/** Web client window backed entirely by the server preview DTO. */
export class ItemUpgradeDialog extends DialogContentComponent {
	private static active?: ItemUpgradeDialog;
    private static readonly openedWindows = new Set<string>();
    private static pendingOpenRequest?: string;
    private static nextOpenRequest = 0;

	private readonly icon = document.createElement("div");
	private readonly nameLabel = document.createElement("div");
	private readonly levelLabel = document.createElement("div");
	private readonly stats = document.createElement("div");
	private readonly chance = document.createElement("div");
	private readonly fee = document.createElement("div");
    private readonly glyphFee = document.createElement("div");
    private readonly materialHeading = document.createElement("h4");
	private readonly materials = document.createElement("div");
	private readonly status = document.createElement("div");
	private readonly refreshButton: HTMLButtonElement;
	private readonly upgradeButton: HTMLButtonElement;

	private npcId = 0;
	private glyphMode = false;
    private creatingGlyph = false;
    private selectedRecipe?: string;
    private readonly recipeCatalog = document.createElement("div");
    private readonly details = document.createElement("div");
    private glyphStatsSection?: HTMLFieldSetElement;
    private glyphStatsHeading?: HTMLLegendElement;
    private glyphMaterialsSection?: HTMLFieldSetElement;
    private itemHeading!: HTMLHeadingElement;
    private readonly categoryOpen = new Map<string, boolean>();
    private recipeButtons: HTMLButtonElement[] = [];
    private recipeCatalogKey = "";
    private readonly recipeRows = new Map<string, {button: HTMLButtonElement; label: HTMLElement}>();
    private readonly categoryLists = new Map<string, HTMLDivElement>();
	private selectedPath?: string;
	private requestToken?: string;
	private selectionCleared = false;
	private upgradeCandidatePaths = new Set<string>();
    private inventorySignature?: string;
    private catalogReceived = false;
    private inventoryRefresh?: ReturnType<typeof setTimeout>;
    private animationTimer?: ReturnType<typeof setInterval>;
    private readonly animatedIcons = new Map<HTMLElement, string>();

	private constructor() {
		super("empty-div-template");
		this.componentElement.classList.add("item-upgrade-dialog");
        const layout = document.createElement("div"); layout.className = "glyph-workbench-layout";
        this.recipeCatalog.className = "glyph-recipe-catalog"; this.recipeCatalog.hidden = true;
        this.details.className = "item-upgrade-details";
        layout.append(this.recipeCatalog, this.details); this.componentElement.appendChild(layout);

		const itemHeading = document.createElement("h4"); this.itemHeading = itemHeading;
		itemHeading.textContent = "Przedmiot do ulepszenia";
		this.details.appendChild(itemHeading);

		const identity = document.createElement("div");
		identity.className = "item-upgrade-identity";
		this.icon.className = "item-upgrade-slot";
		this.icon.title = this.selectionHint();
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
		this.icon.addEventListener("contextmenu", (event: MouseEvent) => {
			this.onIconContextMenu(event);
		});
		identity.appendChild(this.icon);
		const identityText = document.createElement("div");
		this.nameLabel.className = "item-upgrade-name";
		this.nameLabel.textContent = this.selectionPrompt();
		this.levelLabel.textContent = this.selectionInfo();
		identityText.append(this.nameLabel, this.levelLabel);
		identity.appendChild(identityText);
		this.details.appendChild(identity);

		this.stats.className = "item-upgrade-stats";
		this.details.append(this.stats, this.chance, this.fee);
		const materialHeading = this.materialHeading;
		materialHeading.textContent = "Materiały (posiadasz / wymagane)";
		this.glyphFee.className = "glyph-fee"; this.glyphFee.hidden = true;
        this.details.append(materialHeading, this.materials, this.glyphFee);
		this.status.className = "item-upgrade-status";
		this.details.appendChild(this.status);

		this.refreshButton = this.addButton("Odśwież", () => {
			if (this.selectedPath || this.selectedRecipe) {
				this.send("preview", this.selectedPath);
			} else {
				this.send("refresh");
			}
		});
		this.upgradeButton = this.addButton("Ulepsz", () => {
			if ((this.selectedPath || this.selectedRecipe) && this.requestToken) {
				this.upgradeButton.disabled = true;
				this.send("upgrade", this.selectedPath, this.requestToken);
			}
		});
	}

	public static show(data: UpgradeEventData): void {
        if (data.phase === "open") {
            if (data.window_request && data.window_request !== this.pendingOpenRequest) return;
            if (data.window_id) {
                if (this.openedWindows.has(data.window_id)) return;
                this.openedWindows.add(data.window_id);
                if (this.openedWindows.size > 64) this.openedWindows.delete(this.openedWindows.values().next().value!);
            }
            this.pendingOpenRequest = undefined;
        }
		if (!ItemUpgradeDialog.active) {
			if (data.phase !== "open") return;
			const dialog = new ItemUpgradeDialog();
			const frame = new FloatingWindow("Ulepszanie przedmiotu", dialog, 24, 24);
			frame.setId("item-upgrade");
			dialog.setFrame(frame);
			ItemUpgradeDialog.active = dialog;
		}
		ItemUpgradeDialog.active.apply(data);
        ItemUpgradeDialog.active.startAnimation();
	}

    public static glyphOpenAction(tableId: number): {[key: string]: any} {
        this.pendingOpenRequest = Date.now() + ":" + ++this.nextOpenRequest;
        return {type: "item_upgrade_action", service: "glyph", command: "open", npc_id: String(tableId), window_request: this.pendingOpenRequest};
    }

    public static onInventoryChanged(): void {
        const dialog = this.active;
        if (!dialog?.glyphMode || !marauroa.me) return;
        const parts: string[] = [];
        const append = (owner: any) => {
            for (const key of Object.keys(owner).sort()) {
                const slot = owner[key];
                if (!slot || typeof slot.count !== "function" || typeof slot.getByIndex !== "function") continue;
                parts.push("[", key);
                for (let i = 0; i < slot.count(); i++) {
                    const item = slot.getByIndex(i); if (!item) continue;
                    parts.push(JSON.stringify(["id", "name", "class", "subclass", "quantity", "glyph_level", "bound", "itemdata"].map(key => item[key])));
                    append(item);
                }
                parts.push("]");
            }
        };
        append(marauroa.me); const signature = parts.join("");
        const changed = dialog.inventorySignature !== undefined && signature !== dialog.inventorySignature;
        dialog.inventorySignature = signature;
        if (changed && !dialog.catalogReceived) {
            dialog.requestToken = undefined; dialog.upgradeButton.disabled = true; dialog.scheduleRefresh();
        }
        dialog.catalogReceived = false;
    }
    private scheduleRefresh(): void {
        if (this.inventoryRefresh !== undefined) clearTimeout(this.inventoryRefresh);
        this.inventoryRefresh = setTimeout(() => {
            this.inventoryRefresh = undefined;
            if (ItemUpgradeDialog.active === this && this.glyphMode) this.send(this.selectedRecipe ? "preview" : "refresh");
        }, 200);
    }
    private startAnimation(): void {
        if (this.animationTimer === undefined) this.animationTimer = setInterval(() => this.animateIcons(), 100);
        this.animateIcons();
    }
    private spriteIcon(itemClass: string, subclass: string, size = 32, glyphLevel = 0): HTMLElement {
        const clip = document.createElement("span"); clip.className = "workbench-item-icon";
        clip.style.width = clip.style.height = size + "px";
        const image = document.createElement("span"); image.className = "workbench-item-frame";
        if (size !== 32) image.style.transform = `scale(${size / 32})`;
        clip.appendChild(image); this.registerSprite(image, itemClass, subclass, glyphLevel); return clip;
    }
    private registerSprite(element: HTMLElement, itemClass: string, subclass: string, glyphLevel = 0): void {
        const filename = Paths.sprites + "/items/" + itemClass + "/" + subclass + ".png";
        element.style.backgroundImage = "url(" + singletons.getSpriteStore().checkPath(filename) + ")";
        element.style.backgroundPosition = "0px 0px";
        element.style.filter = itemClass === "glyph" ? glyphColourFilter(glyphLevel) : "none";
        this.animatedIcons.set(element, filename);
    }
    private animateIcons(now = Date.now()): void {
        for (const [element, filename] of this.animatedIcons) {
            if (element.isConnected === false) { this.animatedIcons.delete(element); continue; }
            const sheet = singletons.getSpriteStore().get(filename);
            const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
            element.style.backgroundPosition = `${-(Math.floor(now / 100) % frames) * 32}px 0px`;
        }
    }

	public static canSelectItem(item: Item): boolean {
		return !!ItemUpgradeDialog.active && !ItemUpgradeDialog.active.glyphMode
				&& ItemUpgradeDialog.active.upgradeCandidatePaths.has(
						ItemUpgradeDialog.active.normalizePath(item.getIdPath()));
	}

	public static selectItemForUpgrade(item: Item): void {
		if (ItemUpgradeDialog.canSelectItem(item)) {
			ItemUpgradeDialog.active!.selectItemPath(item.getIdPath());
		}
	}

	public override onParentClose(): void {
        ItemUpgradeDialog.pendingOpenRequest = undefined;
		this.send("close");
        if (this.inventoryRefresh !== undefined) clearTimeout(this.inventoryRefresh);
        if (this.animationTimer !== undefined) clearInterval(this.animationTimer);
        this.animatedIcons.clear();
		ItemUpgradeDialog.active = undefined;
	}

	private apply(data: UpgradeEventData): void {
        const responseGlyph = data.service === "glyph";
        if (data.phase !== "open" && (responseGlyph !== this.glyphMode
                || (data.npc_id !== undefined && this.npcId && Number(data.npc_id) !== this.npcId))) return;
        const responseRecipe = data.selected_recipe ? String(data.selected_recipe) : undefined;
        const responsePath = data.selected_path ? this.normalizePath(String(data.selected_path)) : undefined;
        if (data.phase !== "open") {
            if (this.selectionCleared && data.phase !== "refresh") return;
            if (responseRecipe && this.selectedRecipe && responseRecipe !== this.selectedRecipe) return;
            if (data.phase === "refresh" && (this.selectedPath || (this.glyphMode && this.selectedRecipe))) return;
            if (!(this.glyphMode && responseRecipe) && responsePath && this.selectedPath && responsePath !== this.selectedPath) return;
        }
        if (data.phase === "open") {
            this.glyphMode = responseGlyph;
            this.refreshButton.hidden = this.glyphMode; this.inventorySignature = undefined; this.catalogReceived = false;
            if (this.inventoryRefresh !== undefined) clearTimeout(this.inventoryRefresh);
            this.frame?.setTitle(this.glyphMode ? "Stół runiczny" : "Ulepszanie przedmiotu");
            this.componentElement.classList.toggle("glyph-workbench-dialog", this.glyphMode);
            this.setGlyphLayout(this.glyphMode);
            this.recipeCatalog.hidden = !this.glyphMode; this.selectedRecipe = undefined;
        }
        if (responseRecipe) this.selectedRecipe = responseRecipe;
        else if (!this.isInteractionStatus(String(data.status || ""))) this.selectedRecipe = undefined;
        this.creatingGlyph = this.glyphMode && data.operation === "create";
        this.icon.title = this.glyphMode ? "" : this.selectionHint();
        this.itemHeading.hidden = this.glyphMode;
        this.chance.hidden = this.glyphMode; this.fee.hidden = this.glyphMode;
        this.materialHeading.textContent = GameLanguage.getText(this.glyphMode ? "Składniki (masz / potrzeba)" : "Materiały (posiadasz / wymagane)");
        this.itemHeading.textContent = GameLanguage.getText(this.glyphMode ? (this.creatingGlyph ? "Tworzony glif" : "Wybrany glif") : "Przedmiot do ulepszenia");
        if (this.glyphMode && data.recipe_ids !== undefined) { this.fillRecipeCatalog(data); this.catalogReceived = true; }
        this.upgradeButton.textContent = GameLanguage.getText(this.glyphMode ? (this.creatingGlyph ? "Utwórz glif" : "Ulepsz glif") : "Ulepsz");
		if (data.phase === "open") this.selectionCleared = false;
		if (data.npc_id !== undefined) this.npcId = Number(data.npc_id || 0);
		if (data.candidate_paths !== undefined) {
			this.upgradeCandidatePaths = new Set(this.list(data.candidate_paths)
					.map(path => this.normalizePath(path)));
		}
		const state = String(data.status || "");
		const preservePreview = !data.name && this.isInteractionStatus(state)
				&& !!(this.selectedPath || this.selectedRecipe);
		if (data.selected_path) {
			this.selectedPath = String(data.selected_path);
		} else if (!preservePreview) {
			this.selectedPath = undefined;
		}
		this.requestToken = data.request_token || undefined;

		if (data.name) {
			this.icon.replaceChildren();
            this.icon.appendChild(this.spriteIcon(data["class"], data.subclass, 32, this.glyphMode ? Number(data.next_upgrade_level) : 0));
			const rarity = ItemRarity.fromId(data.rarity_id) || ItemRarity.COMMON;
			this.nameLabel.textContent = data.name;
			this.nameLabel.style.color = this.glyphMode ? "#ffcc70" : rarity.colorHex;
			this.levelLabel.textContent = this.glyphMode ? (this.creatingGlyph ? GameLanguage.getText("Tworzenie · poziom I") : GameLanguage.getText("Ulepszanie") + " · "
                    + ["I", "II", "III"][Number(data.upgrade_level) - 1] + " → " + ["I", "II", "III"][Number(data.next_upgrade_level) - 1]
                    + (data.source_location ? " (" + data.source_location + ")" : ""))
                    : "Poziom ulepszenia: +" + data.upgrade_level + " / +" + data.max_upgrade_level;
		} else if (!preservePreview) {
			this.icon.replaceChildren();
			this.nameLabel.textContent = state === "NO_UPGRADEABLE_ITEMS"
					? (this.glyphMode ? "Brak glifu do rozwinięcia" : "Brak przedmiotu do ulepszenia")
					: this.selectionPrompt();
			this.nameLabel.style.color = "";
			this.levelLabel.textContent = this.selectionInfo();
		}

		if (data.name || !preservePreview) {
			this.fillStats(data);
			this.chance.textContent = GameLanguage.getText(this.glyphMode ? "Gwarantowane powodzenie." : data.success_percent !== undefined
					? "Szansa powodzenia: " + data.success_percent + "%"
					: "Szansa powodzenia: —");
			this.fee.textContent = GameLanguage.getText(this.creatingGlyph ? "Koszt: wymagane składniki" : data.fee_text ? "Koszt: " + data.fee_text
					: "Koszt: —");
			this.fillMaterials(data);
		}

		const canUpgrade = Number(data.can_upgrade || 0) === 1 && !!this.requestToken;
		this.upgradeButton.disabled = !canUpgrade;
		this.refreshButton.disabled = !this.npcId;
        if (this.glyphMode && state === "STALE_PREVIEW") this.scheduleRefresh();
		this.status.hidden = this.glyphMode && ["READY", "MISSING_RESOURCES", "NOT_ENOUGH_MONEY"].includes(state);
        this.status.textContent = data.message || this.statusText(state);
        this.glyphFee.hidden = !this.glyphMode || this.creatingGlyph || !(Number(data.fee) > 0);
        if (!this.glyphFee.hidden) {
            const label = document.createElement("span"); label.textContent = GameLanguage.getText("Koszt ulepszenia");
            const value = document.createElement("span"); value.textContent = data.fee_text;
            value.className = Number(data.owned_money) >= Number(data.fee) ? "item-upgrade-owned" : "item-upgrade-missing";
            this.glyphFee.replaceChildren(label, value);
        }
		this.status.dataset.status = state.toLowerCase();
		this.upgradeButton.title = canUpgrade ? (this.glyphMode ? "Ulepsz glif" : "Wykonaj próbę ulepszenia")
				: this.statusText(state);
	}

	private fillStats(data: UpgradeEventData): void {
		this.stats.replaceChildren();
		if (this.glyphMode) {
            this.glyphStatsHeading!.textContent = GameLanguage.getText(this.creatingGlyph ? "Właściwości" : "Zmiana mocy");
            this.fillGlyphStats(data); return;
        }
		const heading = document.createElement("h4");
		heading.textContent = GameLanguage.getText("Statystyki po ulepszeniu");
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
			this.addStatRow(this.statLabel(name), this.creatingGlyph ? "" : current[index], upgraded[index]);
		});
		if (names.length === 0) {
			const row = document.createElement("div");
			row.textContent = this.previewHint();
			this.stats.appendChild(row);
		}
	}

    private setGlyphLayout(enabled: boolean): void {
        if (enabled && !this.glyphStatsSection) {
            this.glyphStatsSection = document.createElement("fieldset");
            this.glyphStatsSection.className = "glyph-detail-section";
            this.glyphStatsHeading = document.createElement("legend");
            this.details.insertBefore(this.glyphStatsSection, this.stats);
            this.glyphStatsSection.append(this.glyphStatsHeading, this.stats);

            this.glyphMaterialsSection = document.createElement("fieldset");
            this.glyphMaterialsSection.className = "glyph-detail-section glyph-material-section";
            const heading = document.createElement("legend");
            heading.textContent = GameLanguage.getText("Składniki (masz / potrzeba)");
            this.details.insertBefore(this.glyphMaterialsSection, this.materials);
            this.glyphMaterialsSection.append(heading, this.materials);
            this.details.appendChild(this.upgradeButton.parentElement!);
            this.icon.style.setProperty("--item-slot-texture", `url(${Paths.gui}/slot.png)`);
        } else if (!enabled && this.glyphStatsSection) {
            this.details.insertBefore(this.stats, this.glyphStatsSection);
            this.glyphStatsSection.remove(); this.glyphStatsSection = undefined;
            this.glyphStatsHeading = undefined;
            this.details.insertBefore(this.materials, this.glyphMaterialsSection!);
            this.glyphMaterialsSection!.remove(); this.glyphMaterialsSection = undefined;
            this.componentElement.appendChild(this.upgradeButton.parentElement!);
        }
        this.materialHeading.hidden = enabled;
    }

    private fillGlyphStats(data: UpgradeEventData): void {
        const names = this.list(data.stat_short_names ?? data.stat_names), keys = this.list(data.stat_keys);
        const current = this.list(data.current_stat_values), next = this.list(data.upgraded_stat_values);
        const notes = this.valuesByName(this.list(data.stat_note_keys), this.list(data.stat_note_values));
        const add = (name: string, before: string|undefined, after: string, heading = false) => {
            const row = document.createElement("div"); row.className = "glyph-stat-row" + (before === undefined ? "" : " glyph-stat-upgrade");
            const label = document.createElement("span"); label.textContent = name; row.appendChild(label);
            if (before !== undefined) { const value = document.createElement("span"); value.textContent = before; row.appendChild(value); }
            const value = document.createElement("span"); value.textContent = after; value.className = heading ? "glyph-stat-caption" : "item-upgrade-owned"; row.appendChild(value);
            this.stats.appendChild(row);
        };
        if (!this.creatingGlyph && names.length) add("", GameLanguage.getText("Teraz"), GameLanguage.getText("Po zmianie"), true);
        names.forEach((name, index) => {
            if (current[index] === undefined || next[index] === undefined) return;
            const unit = keys[index] === "out_of_combat_regen_cap" ? " " + GameLanguage.getText("PZ") : "";
            add(name, this.creatingGlyph ? undefined : current[index] + unit, next[index] + unit);
            if (notes[keys[index]]) {
                const note = document.createElement("div"); note.className = "glyph-stat-note"; note.textContent = notes[keys[index]]; this.stats.appendChild(note);
            }
        });
        if (!names.length) { const hint = document.createElement("div"); hint.textContent = this.previewHint(); this.stats.appendChild(hint); }
    }

    private fillRecipeCatalog(data: UpgradeEventData): void {
        const ids = this.list(data.recipe_ids), names = this.list(data.recipe_names), levels = this.list(data.recipe_levels);
        const ready = this.list(data.recipe_ready), sprites = this.list(data.recipe_subclasses);
        const catalogKey = JSON.stringify([ids, levels, sprites]);
        if (catalogKey === this.recipeCatalogKey && ids.length === this.recipeRows.size) {
            ids.forEach((id, index) => {
                const row = this.recipeRows.get(id)!;
                row.label.textContent = names[index] + " " + ["I", "II", "III"][Number(levels[index]) - 1];
                this.updateRecipeButton(row.button, id, ready[index] === "1");
            });
            return;
        }
        this.recipeCatalogKey = catalogKey;
        const positions = new Map(Array.from(this.categoryLists, ([mode, list]) => [mode, list.scrollTop]));
        this.recipeCatalog.replaceChildren(); this.recipeButtons = []; this.categoryLists.clear(); this.recipeRows.clear();
        for (const mode of ["create", "upgrade"]) {
            const category = document.createElement("section"); category.className = "glyph-catalog-group";
            const expanded = this.categoryOpen.get(mode) ?? mode === "create";
            const heading = document.createElement("button"); heading.type = "button"; heading.className = "glyph-catalog-heading";
            const title = GameLanguage.getText(mode === "create" ? "Tworzenie glifów" : "Ulepszanie");
            const rows = document.createElement("div"); rows.className = "glyph-catalog-rows";
            const toggle = (open: boolean) => {
                this.categoryOpen.set(mode, open); rows.hidden = !open;
                category.classList.toggle("glyph-catalog-open", open);
                heading.textContent = (open ? "▾ " : "▸ ") + title;
                heading.setAttribute("aria-expanded", String(open));
            };
            heading.onclick = () => toggle(!this.categoryOpen.get(mode)); toggle(expanded);
            category.append(heading, rows); this.categoryLists.set(mode, rows);
            ids.forEach((id, index) => {
                const level = Number(levels[index]);
                if ((level === 1) !== (mode === "create")) return;
                const button = document.createElement("button"); button.type = "button";
                button.className = "glyph-recipe-row";
                this.updateRecipeButton(button, id, ready[index] === "1");
                if (sprites[index]) {
                    button.appendChild(this.spriteIcon("glyph", sprites[index], 20, level));
                }
                const label = document.createElement("span"); label.textContent = names[index] + " " + ["I", "II", "III"][level - 1]; button.appendChild(label);
                button.onclick = () => {
                    this.selectedRecipe = id; this.selectedPath = undefined; this.requestToken = undefined; this.selectionCleared = false;
                    if (this.inventoryRefresh !== undefined) clearTimeout(this.inventoryRefresh); this.inventoryRefresh = undefined;
                    this.recipeButtons.forEach(row => row.classList.toggle("glyph-recipe-selected", row === button));
                    this.upgradeButton.disabled = true; this.status.textContent = GameLanguage.getText("Sprawdzanie wymagań...");
                    this.send("preview");
                };
                this.recipeButtons.push(button); this.recipeRows.set(id, {button, label}); rows.appendChild(button);
            });
            this.recipeCatalog.appendChild(category);
            rows.scrollTop = positions.get(mode) || 0;
        }
    }

    private updateRecipeButton(button: HTMLButtonElement, id: string, ready: boolean): void {
        button.classList.toggle("glyph-recipe-missing", !ready);
        button.classList.toggle("glyph-recipe-selected", id === this.selectedRecipe);
        button.title = GameLanguage.getText(ready ? "Wymagania są spełnione." : "Brakuje składników. Kliknij, aby sprawdzić wymagania.");
    }

	private fillMaterials(data: UpgradeEventData): void {
		this.materials.replaceChildren();
		const names = this.list(data.material_names);
        this.materials.style.gridTemplateColumns = this.glyphMode && names.length ? `repeat(${Math.min(5, names.length)}, minmax(0, 1fr))` : "";
		const classes = this.list(data.material_classes);
		const subclasses = this.list(data.material_subclasses);
		const required = this.list(data.material_values);
		const owned = this.list(data.owned_material_values);
        const glyphLevels = this.list(data.material_glyph_levels);
		this.materials.className = names.length > 0
				? "item-upgrade-materials" : "";
		names.forEach((name, index) => {
			const have = Number(owned[index] || 0);
			const need = Number(required[index] || 0);
			const available = have >= need;
			const card = document.createElement("div");
			card.className = "item-upgrade-material";
			card.title = GameLanguage.getText(name + ": posiadasz " + have + ", wymagane " + need);

			const slot = document.createElement("div");
			slot.className = "itemSlot item-upgrade-material-slot";
			const layers = getItemSlotLayers(slot);
			if (classes[index] && subclasses[index]) {
                this.registerSprite(layers.icon, classes[index], subclasses[index], Number(glyphLevels[index] || 0));
			}
			layers.quantityLabel.textContent = need > 1 ? String(need) : "";

			const label = document.createElement("div");
			label.textContent = name;
			const count = document.createElement("div");
			count.className = available ? "item-upgrade-owned"
					: "item-upgrade-missing";
			count.textContent = (this.glyphMode ? "" : available ? "✓ " : "✗ ") + have + " / " + need;
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
		this.selectItemPath(path);
	}

	private canAcceptHeldItem(): boolean {
        if (this.glyphMode) return false;
		if (!stendhal.ui.heldObject || !stendhal.ui.heldObject.path) return false;
		return this.isOwnedPath(stendhal.ui.heldObject.path);
	}

	private isOwnedPath(path: string): boolean {
		const parts = this.normalizePath(path).split("/");
		return parts.length >= 3 && parts[0] === String(marauroa.me["id"]);
	}

	private selectItemPath(path: string): void {
		const normalizedPath = this.normalizePath(path);
		if (!this.isOwnedPath(normalizedPath)) return;
		this.selectionCleared = false;
		this.selectedPath = normalizedPath;
		this.requestToken = undefined;
		this.upgradeButton.disabled = true;
		this.refreshButton.disabled = true;
		this.status.textContent = "Pobieranie podglądu z serwera…";
		this.status.dataset.status = "select_item";
		this.send("preview", normalizedPath);
	}

	private onIconContextMenu(event: MouseEvent): void {
		event.preventDefault();
		if (this.glyphMode || !this.selectedPath) return;
		stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow(
				"Czynności", new ActionContextMenu(undefined, [{
					title: "Wyczyść",
					action: () => this.clearSelection()
				}]), event.pageX - 50, event.pageY - 5));
	}

	private clearSelection(): void {
		this.send("clear");
        this.selectedRecipe = undefined;
		this.apply({status: "SELECT_ITEM", service: this.glyphMode ? "glyph" : undefined, npc_id: this.npcId});
		this.selectionCleared = true;
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
		row.append(label, current ? current + "  →  " : "", result);
		this.stats.appendChild(row);
	}

	private valuesByName(names: string[], values: string[]): {[key: string]: string} {
		const result: {[key: string]: string} = {};
		names.forEach((name, index) => {
			if (values[index] !== undefined) result[name] = values[index];
		});
		return result;
	}

	private send(command: string, path?: string, token?: string): void {
		if (!this.npcId) return;
		const action: {[key: string]: any} = {
			type: "item_upgrade_action",
			command,
			npc_id: String(this.npcId)
		};
		if (this.glyphMode) action.service = "glyph";
        if (this.glyphMode && this.selectedRecipe) action.glyph_recipe = this.selectedRecipe;
		if (path) {
			action.target_path = "[" + path.split("/").join("\t") + "]";
		}
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

    private previewHint(): string { return this.glyphMode ? "Po wybraniu glifu zobaczysz zmianę jego mocy." : "Po wybraniu przedmiotu zobaczysz zmianę statystyk."; }
	private selectionPrompt(): string { return this.glyphMode ? "Wybierz glif z listy" : "Przeciągnij tutaj przedmiot"; }
    private selectionInfo(): string { return this.glyphMode ? "Glif pozostanie w ekwipunku." : "Przedmiot pozostanie w ekwipunku."; }
    private selectionHint(): string { return this.glyphMode ? "Wybierz glif z listy po lewej stronie." : "Przeciągnij tutaj przedmiot z ekwipunku."; }
    private statusText(state: string): string {
        if (this.glyphMode) {
            if (state === "SELECT_ITEM") return this.selectionHint();
            if (state === "NO_UPGRADEABLE_ITEMS") return "Nie masz przy sobie glifu I lub II, który można rozwinąć.";
            if (state === "NPC_TOO_FAR") return "Podejdź do stołu runicznego, aby rozwijać glify.";
            if (state === "MAX_LEVEL") return "Glif osiągnął poziom III.";
            if (state === "INVALID_ITEM" || state === "NOT_UPGRADEABLE") return "Tego glifu nie można ulepszyć.";
        }
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
