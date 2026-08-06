/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

/**
 * Client presentation metadata for the stable rarity identifiers sent by
 * the server. Item statistics remain server-owned.
 */
export class ItemRarity {

	public static readonly COMMON = new ItemRarity("common", "#9e9e9e", "Zwykły", "Common");
	public static readonly RARE = new ItemRarity("rare", "#4a90e2", "Rzadki", "Rare");
	public static readonly EPIC = new ItemRarity("epic", "#9b59b6", "Epicki", "Epic");
	public static readonly LEGENDARY = new ItemRarity("legendary", "#ff8c00", "Legendarny", "Legendary");

	public static readonly VALUES: ReadonlyArray<ItemRarity> = [
		ItemRarity.COMMON,
		ItemRarity.RARE,
		ItemRarity.EPIC,
		ItemRarity.LEGENDARY
	];

	private constructor(
		public readonly id: string,
		public readonly colorHex: string,
		public readonly polishDisplayName: string,
		public readonly englishDisplayName: string
	) {
	}

	/**
	 * Resolves an exact wire identifier. Missing and unknown values are left
	 * undecorated so legacy or malformed objects never break the client.
	 */
	public static fromId(id: unknown): ItemRarity|undefined {
		if (typeof id !== "string") {
			return undefined;
		}
		return ItemRarity.VALUES.find((rarity) => rarity.id === id);
	}

	public get cssClass(): string {
		return "item-rarity-" + this.id;
	}
}
