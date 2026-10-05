/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import type { Item } from "../entity/Item";
import type { ItemRarity } from "../data/ItemRarity";
import type { RenderingContext2D } from "../util/Types";
import { singletons } from "../SingletonRepo";

type Effect = "outline" | "ground";
type Pixels = { width: number; height: number; data: Uint8ClampedArray };
type Source = CanvasImageSource & { width: number; height: number; src?: string; currentSrc?: string; complete?: boolean };

/** Alpha-silhouette decorations matching the Java client's item presentation. */
export class ItemRarityEffects {

	private static readonly ALPHA_THRESHOLD = 16;
	private static readonly CACHE_BYTES = 8 * 1024 * 1024;
	private static readonly cache = new Map<string, HTMLCanvasElement>();
	private static readonly sources = new WeakMap<object, { id: number; signature: string; failed?: boolean }>();
	private static readonly slots = new WeakMap<HTMLElement, { canvas: HTMLCanvasElement; effect?: HTMLCanvasElement }>();
	private static nextSourceId = 0;
	private static cacheBytes = 0;
	private static scratch?: HTMLCanvasElement;

	/** Four-neighbor outline in slots, three-pixel fading rings on the ground. */
	public static createPixels(source: Pixels, rarity: ItemRarity, effect: Effect): Pixels {
		const radius = effect === "outline" ? 1 : 3;
		const width = source.width + radius * 2;
		const height = source.height + radius * 2;
		const data = new Uint8ClampedArray(width * height * 4);
		const color = Number.parseInt(rarity.colorHex.slice(1), 16);
		const strengths: Record<string, number> = effect === "outline"
			? { common: 1, rare: 0.5, epic: 0.7, legendary: 0.4 }
			: { common: 0.6, rare: 0.5, epic: 0.4, legendary: 0.6 };
		const strength = strengths[rarity.id] ?? (effect === "outline" ? 1 : 0.6);
		const alphaAt = (x: number, y: number) => x < 0 || y < 0 || x >= source.width || y >= source.height
			? 0 : source.data[(y * source.width + x) * 4 + 3];
		const neighbors: Array<{ dx: number; dy: number; falloff: number }> = [];
		for (let dy = -radius; dy <= radius; dy++) {
			for (let dx = -radius; dx <= radius; dx++) {
				const distance = Math.hypot(dx, dy);
				if (distance && distance <= radius) {
					neighbors.push({ dx, dy, falloff: effect === "outline" ? 1 : (radius + 1 - distance) / radius });
				}
			}
		}
		for (let y = 0; y < source.height; y++) {
			for (let x = 0; x < source.width; x++) {
				const sourceAlpha = alphaAt(x, y);
				if (sourceAlpha <= this.ALPHA_THRESHOLD) {
					continue;
				}
				for (const { dx, dy, falloff } of neighbors) {
					if (alphaAt(x + dx, y + dy) > this.ALPHA_THRESHOLD) {
						continue;
					}
					const alpha = Math.round(255 * strength * falloff * (effect === "outline" ? 1 : sourceAlpha / 255));
					const index = ((y + dy + radius) * width + x + dx + radius) * 4;
					if (alpha > data[index + 3]) {
						data[index] = color >> 16 & 255;
						data[index + 1] = color >> 8 & 255;
						data[index + 2] = color & 255;
						data[index + 3] = alpha;
					}
				}
			}
		}
		return { width, height, data };
	}

	/** Cache small effect frames, never a decorated full sprite sheet per slot. */
	public static getFrame(image: Source, x: number, y: number, rarity: ItemRarity, effect: Effect): HTMLCanvasElement|undefined {
		if (image.complete === false || x < 0 || y < 0 || x + 32 > image.width || y + 32 > image.height) {
			return undefined;
		}
		const signature = `${image.currentSrc || image.src || ""}/${image.width}/${image.height}`;
		let source = this.sources.get(image);
		if (!source || source.signature !== signature) {
			source = { id: ++this.nextSourceId, signature };
			this.sources.set(image, source);
		}
		if (source.failed) {
			return undefined;
		}
		const key = `${source.id}/${x}/${y}/${rarity.id}/${effect}`;
		const cached = this.cache.get(key);
		if (cached) {
			this.cache.delete(key);
			this.cache.set(key, cached);
			return cached;
		}
		try {
			const scratch = this.scratch ||= document.createElement("canvas");
			scratch.width = scratch.height = 32;
			const context = scratch.getContext("2d", { willReadFrequently: true });
			if (!context) {
				return undefined;
			}
			context.drawImage(image, x, y, 32, 32, 0, 0, 32, 32);
			const pixels = this.createPixels(context.getImageData(0, 0, 32, 32), rarity, effect);
			const canvas = document.createElement("canvas");
			canvas.width = pixels.width;
			canvas.height = pixels.height;
			const output = canvas.getContext("2d");
			if (!output) {
				return undefined;
			}
			const result = output.createImageData(pixels.width, pixels.height);
			result.data.set(pixels.data);
			output.putImageData(result, 0, 0);
			this.cache.set(key, canvas);
			this.cacheBytes += canvas.width * canvas.height * 4;
			while (this.cacheBytes > this.CACHE_BYTES) {
				const oldest = this.cache.entries().next().value!;
				this.cacheBytes -= oldest[1].width * oldest[1].height * 4;
				this.cache.delete(oldest[0]);
			}
			return canvas;
		} catch {
			// Unreadable image data must not prevent normal item rendering.
			source.failed = true;
			return undefined;
		}
	}

	public static updateSlot(target: HTMLElement, item?: Item) {
		const rarity = item?.getRarity();
		const image = rarity && item ? singletons.getSpriteStore().get(item.sprite.filename) : undefined;
		const effect = image && item && rarity ? this.getFrame(image,
			item.getXFrameIndex() * 32, item.getYFrameIndex() * 32, rarity, "outline") : undefined;
		let slot = this.slots.get(target);
		if (!effect) {
			slot?.canvas.remove();
			this.slots.delete(target);
			return;
		}
		if (!slot) {
			const canvas = document.createElement("canvas");
			canvas.className = "item-rarity-outline";
			canvas.width = canvas.height = 34;
			canvas.setAttribute("aria-hidden", "true");
			slot = { canvas };
			this.slots.set(target, slot);
		}
		if (slot.effect !== effect) {
			const context = slot.canvas.getContext("2d");
			context?.clearRect(0, 0, 34, 34);
			context?.drawImage(effect, 0, 0);
			slot.effect = effect;
		}
		// Quantity text updates replace children; keep and reattach the small canvas.
		if (slot.canvas.parentElement !== target) {
			target.appendChild(slot.canvas);
		}
	}

	public static drawGround(context: RenderingContext2D, item: Item, x: number, y: number) {
		const rarity = item.getRarity();
		if (!rarity || item["_parent"]) {
			return;
		}
		const image = singletons.getSpriteStore().get(item.sprite.filename);
		const effect = this.getFrame(image, item.sprite.offsetX || 0, item.sprite.offsetY || 0, rarity, "ground");
		if (effect) {
			context.drawImage(effect,
				x + Math.floor((item.getWidth() * 32 - 32) / 2) - 3,
				y + Math.floor((item.getHeight() * 32 - 32) / 2) - 3);
		}
	}
}
