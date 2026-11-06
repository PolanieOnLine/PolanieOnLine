/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
import { Item } from "../../entity/Item";
import { ItemContainerImplementation } from "./ItemContainerImplementation";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";
import { GameLanguage } from "../../util/GameLanguage";
import { getItemSlotLayers } from "./ItemSlotPresentation";

/** Optional fields appended to progress-status events by newer servers. */
export interface QuestRewardData {
	reward_xp?: number | string;
	reward_coins?: number | string;
	reward_completed?: number | string;
	reward_names?: string[] | string;
	reward_classes?: string[] | string;
	reward_subclasses?: string[] | string;
	reward_amounts?: string[] | string;
	reward_rarities?: string[] | string;
	reward_descriptions?: string[] | string;
	reward_bindings?: string[] | string;
	reward_notes?: string[] | string;
	reward_stats?: string[] | string;
}

const rarities: {[id: string]: [string, string]} = {
	common: ["Zwykły", "#9e9e9e"],
	rare: ["Rzadki", "#4a90e2"],
	epic: ["Epicki", "#9b59b6"],
	legendary: ["Legendarny", "#ff8c00"]
};

function list(value: string[] | string | undefined): string[] {
	if (Array.isArray(value)) return value.map(String);
	if (!value || value === "[]") return [];
	return value.substring(1, value.length - 1).split("\t");
}

/** No live item entities, drag actions or guessed affixes in a reward preview. */
export function createQuestRewards(data?: QuestRewardData): {
	element: HTMLElement;
	dispose: () => void;
} | undefined {
	if (data?.reward_xp === undefined) return undefined;
	const text = (value: string) => GameLanguage.getText(value);
	const number = (value: number | string | undefined) => Number(value || 0).toLocaleString("pl-PL");
	const root = document.createElement("section");
	root.className = "quest-rewards";
	const heading = document.createElement("strong");
	heading.textContent = text(Number(data.reward_completed) ? "Otrzymane nagrody" : "Nagrody");
	root.appendChild(heading);
	const amounts = document.createElement("div");
	amounts.className = "quest-reward-amounts";
	for (const [value, label] of [[data.reward_xp, "PD"], [data.reward_coins, "monet"]] as const) {
		if (Number(value) > 0) {
			const amount = document.createElement("span");
			amount.textContent = number(value) + " " + text(label);
			amounts.appendChild(amount);
		}
	}
	root.appendChild(amounts);
	const names = list(data.reward_names);
	const classes = list(data.reward_classes);
	const subclasses = list(data.reward_subclasses);
	const quantities = list(data.reward_amounts);
	const tiers = list(data.reward_rarities);
	const stats = list(data.reward_stats);
	const items = document.createElement("div");
	items.className = "quest-reward-items";
	const sprites = new Map<HTMLElement, string>();
	const tooltips: Array<() => void> = [];
	names.forEach((name, index) => {
		const row = document.createElement("div");
		row.className = "quest-reward-item";
		const slot = document.createElement("div");
		slot.className = "itemSlot";
		const layers = getItemSlotLayers(slot);
		const filename = `${Paths.sprites}/items/${classes[index]}/${subclasses[index]}.png`;
		layers.icon.style.backgroundImage = `url(${singletons.getSpriteStore().checkPath(filename)})`;
		layers.quantityLabel.textContent = Number(quantities[index]) > 1 ? quantities[index] : "";
		sprites.set(layers.icon, filename);
		const caption = document.createElement("div");
		const itemName = document.createElement("span");
		itemName.textContent = text(name);
		caption.appendChild(itemName);
		const rarity = rarities[tiers[index]];
		if (rarity) {
			const label = document.createElement("small");
			label.textContent = text(rarity[0]);
			label.style.color = rarity[1];
			caption.appendChild(label);
		}
		const item = new Item();
		Object.assign(item, {
			name, class: classes[index], subclass: subclasses[index],
			quantity: quantities[index], rarity_id: tiers[index]
		});
		try {
			const parsed = JSON.parse(stats[index] || "{}");
			if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
				item["tooltip_stats"] = parsed;
			}
		} catch {
			// Older servers may omit the optional tooltip data.
		}
		tooltips.push(ItemContainerImplementation.bindToolTip(row, item));
		row.tabIndex = 0;
		row.append(slot, caption);
		items.appendChild(row);
	});
	if (names.length) root.appendChild(items);
	for (const note of list(data.reward_notes)) {
		const label = document.createElement("div");
		label.textContent = text(note);
		root.appendChild(label);
	}
	const animate = () => {
		for (const [icon, filename] of sprites) {
			const sheet = singletons.getSpriteStore().get(filename);
			const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
			icon.style.backgroundPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
		}
	};
	animate();
	const timer = sprites.size ? setInterval(animate, 100) : undefined;
	return {
		element: root,
		dispose: () => {
			if (timer !== undefined) clearInterval(timer);
			sprites.clear();
			tooltips.forEach(dispose => dispose());
		}
	};
}
