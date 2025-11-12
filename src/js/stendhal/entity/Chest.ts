/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { PopupInventory } from "./PopupInventory";

import { MenuItem } from "../action/MenuItem";

import { Color } from "../data/color/Color";

import { FloatingWindow } from "../ui/toolkit/FloatingWindow";
import { ItemInventoryComponent } from "../ui/component/ItemInventoryComponent";

import { Chat } from "../util/Chat";

declare var marauroa: any;
declare var stendhal: any;
const DEFAULT_CHEST_SPRITE = "chest";
const BANK_SLOT_ATTRIBUTE = "bank_slot";
const SPRITE_EXTENSION = ".png";

const ZONE_SPRITES: { [key: string]: string } = {
	"0_semos_city": "chests/chest_public",
	"0_zakopane_s": "chests/chest_public_snow"
};

const BANK_SLOT_SPRITES: { [key: string]: string } = {
	"bank": "chests/chest_semos",
	"bank_ados": "chests/chest_ados",
	"bank_fado": "chests/chest_fado",
	"bank_kirdneh": "chests/chest_kirdneh",
	"bank_krakow": "chests/chest_krakow",
	"bank_nalwor": "chests/chest_nalwor"
};

const SPRITE_RELEVANT_KEYS = [BANK_SLOT_ATTRIBUTE, "type", "zoneid", "id"];

type ChestSpriteDescriptor = {
	filename: string;
	height: number;
	width: number;
	offsetY?: number;
};

type ChestSpritePair = {
	open: ChestSpriteDescriptor;
	closed: ChestSpriteDescriptor;
};

const SPRITE_CACHE: { [key: string]: ChestSpritePair } = {};

function translateSprite(name: string) {
	return stendhal.paths.sprites + "/" + name + SPRITE_EXTENSION;
}

function getSpritePair(name: string): ChestSpritePair {
	if (!SPRITE_CACHE[name]) {
		const filename = translateSprite(name);
		SPRITE_CACHE[name] = {
			open: {
				filename,
				height: 32,
				width: 32,
				offsetY: 32
			},
			closed: {
				filename,
				height: 32,
				width: 32
			}
		};
	}
	return SPRITE_CACHE[name];
}

function getZoneId(chest: Chest): string | undefined {
	const zoneId = chest["zoneid"];
	if (typeof zoneId === "string" && zoneId.length > 0) {
		return zoneId;
	}

	const identifier = chest["id"];
	if (identifier && typeof identifier.zoneid === "string" && identifier.zoneid.length > 0) {
		return identifier.zoneid;
	}

	if (typeof marauroa?.currentZoneName === "string" && marauroa.currentZoneName.length > 0) {
		return marauroa.currentZoneName;
	}

	return undefined;
}

function resolveSpriteName(chest: Chest): string {
	const bankSlotValue = chest[BANK_SLOT_ATTRIBUTE];
	if (typeof bankSlotValue === "string") {
		const bankSprite = BANK_SLOT_SPRITES[bankSlotValue];
		if (bankSprite) {
			return bankSprite;
		}
	}

	const zoneId = getZoneId(chest);
	if (zoneId) {
		const zoneSprite = ZONE_SPRITES[zoneId];
		if (zoneSprite) {
			return zoneSprite;
		}
	}

	const typeValue = chest["type"];
	if (typeof typeValue === "string" && typeValue.length > 0) {
		return typeValue;
	}

	return DEFAULT_CHEST_SPRITE;
}

export class Chest extends PopupInventory {
	override minimapShow = true;
	override minimapStyle = Color.CHEST;

	override zIndex = 5000;
	sprite = getSpritePair(DEFAULT_CHEST_SPRITE).closed;
	open = false;
	private spriteName = DEFAULT_CHEST_SPRITE;

	private refreshSprite() {
		const resolvedName = resolveSpriteName(this);
		if (this.spriteName !== resolvedName) {
			this.spriteName = resolvedName;
		}
		const spritePair = getSpritePair(this.spriteName);
		this.sprite = this.open ? spritePair.open : spritePair.closed;
	}

	override set(key: string, value: any) {
		super.set(key, value);
		if (key === "open") {
			this.open = true;
			this.refreshSprite();
		}
		if (SPRITE_RELEVANT_KEYS.indexOf(key) > -1) {
			this.refreshSprite();
		}
		if (this.isNextTo(marauroa.me)) {
			this.openInventoryWindow();
		}
	}

	override unset(key: string) {
		super.unset(key);
		if (key === "open") {
			this.open = false;
			this.refreshSprite();
			if (this.inventory && this.inventory.isOpen()) {
				this.inventory.close();
				this.inventory = undefined;
			}
		}
		if (SPRITE_RELEVANT_KEYS.indexOf(key) > -1) {
			this.refreshSprite();
		}
	}

	override buildActions(list: MenuItem[]) {
		super.buildActions(list);

		const that = this;
		if (this.open) {
			list.push({
				title: "Sprawdź",
				action: function(_entity: any) {
					that.checkOpenInventoryWindow();
				}
			});
		}
		list.push({
			title: that.open ? "Zamknij" : "Otwórz",
			action: function(_entity: any) {
				that.onUsed();
			}
		});
	}

	override isVisibleToAction(_filter: boolean) {
		return true;
	}

	override onclick(_x: number, _y: number) {
		if (marauroa.me.isNextTo(this)) {
			// If we are next to the chest, open it.
			if (!this.open) {
				this.onUsed();
			} else {
				this.openInventoryWindow();
			}
		} else {
			// We are far away, but if the chest is open, we can take a look
			if (this.open) {
				this.checkOpenInventoryWindow();
			}
		}
	}

	private onUsed() {
		var action = {
			"type": "use",
			"target": "#" + this["id"],
			"zone": marauroa.currentZoneName
		};
		marauroa.clientFramework.sendAction(action);
	}

	openInventoryWindow() {
                if (!this.inventory || !this.inventory.isOpen()) {
                        const invComponent = new ItemInventoryComponent(this,
                                        "content", 6, 6, stendhal.config.getBoolean("inventory.quick-pickup"), undefined);
                        // TODO: remove, deprecated
                        invComponent.setConfigId("chest");

                        const dstate = stendhal.config.getWindowState("chest");
                        const windowInstance = new FloatingWindow("Skrzynia", invComponent,
                                        dstate.x, dstate.y);
                        windowInstance.setId("chest");
                        windowInstance.enableMinimizeButton(true);
                        this.inventory = windowInstance;
                }
                (this.inventory as FloatingWindow).setMinimized(false);
        }

	/**
	 * Opens inventory window if player is within range.
	 */
	private checkOpenInventoryWindow() {
		if (this.canViewContents()) {
			this.openInventoryWindow();
		} else {
			Chat.log("client", "Skrzynia znajduje się zbyt daleko.");
		}
	}

	override closeInventoryWindow() {
		if (this.inventory && this.inventory.isOpen()) {
			this.inventory.close();
			this.inventory = undefined;
		}
	}

	override getCursor(_x: number, _y: number) {
		return "url(" + stendhal.paths.sprites + "/cursor/bag.png) 1 3, auto";
	}

}
