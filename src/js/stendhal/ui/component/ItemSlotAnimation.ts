/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
import type { Item } from "../../entity/Item";
import { singletons } from "../../SingletonRepo";
import { ItemRarityEffects } from "../../sprite/ItemRarityEffects";
import { AdminDiagnostics } from "../../util/AdminDiagnostics";
import { glyphColourFilter } from "../../data/GlyphAppearance";

interface Atlas {
	key: string;
	url: string;
	width: number;
	bytes: number;
	image: HTMLImageElement;
	frames?: Array<{url: string; image: HTMLImageElement}>;
}
interface Layer {
	clip: HTMLElement;
	sheet: HTMLElement;
	atlas: Atlas;
	frame: number;
	frames?: HTMLElement[];
}

/** Shared prepainted 34px frames: a strip on desktop, local bitmaps on touch.
 * Static slots, quantities and input targets are unchanged. Unsupported or
 * unreadable images keep the ordinary renderer; world sprites are unaffected.
 */
export class ItemSlotAnimation {
	private static readonly MAX_BYTES = 8 * 1024 * 1024;
	private static readonly atlases = new Map<string, Atlas|null>();
	private static readonly sources = new WeakMap<object, number>();
	private static readonly layers = new WeakMap<HTMLElement, Layer>();
	private static nextSource = 0;
	private static bytes = 0;

	public static update(target: HTMLElement, icon: HTMLElement, item: Item): boolean {
		const fixedFrames = typeof window !== "undefined"
				&& typeof window.matchMedia === "function" && window.matchMedia("(pointer: coarse)").matches;
		const atlas = this.atlas(item, this.layers.get(target)?.atlas, fixedFrames);
		if (!atlas || !atlas.image.complete || !atlas.image.naturalWidth
				|| atlas.frames?.some(frame => !frame.image.complete || !frame.image.naturalWidth)) {
			this.clear(target, icon);
			return false;
		}
		let layer = this.layers.get(target);
		// Mobile frames have independent local bitmaps. A fixed element with a
		// negative strip offset still leaves frame-dependent raster sampling.
		const frameCount = atlas.width / 34;
		if (layer && (Boolean(layer.frames) !== fixedFrames
				|| (layer.frames && layer.frames.length !== frameCount))) {
			this.clear(target, icon);
			layer = undefined;
		}
		if (!layer) {
			const clip = document.createElement("span");
			clip.className = "item-slot-animation";
			clip.setAttribute("aria-hidden", "true");
			const sheet = document.createElement("span");
			sheet.className = fixedFrames ? "item-slot-animation__frames" : "item-slot-animation__sheet";
			clip.appendChild(sheet);
			target.appendChild(clip);
			layer = {clip, sheet, atlas, frame: -1};
			if (fixedFrames) {
				layer.frames = Array.from({length: frameCount}, () => {
					const image = document.createElement("span");
					image.className = "item-slot-animation__frame";
					image.style.backgroundPosition = "0px 0px";
					sheet.appendChild(image);
					return image;
				});
			}
			this.layers.set(target, layer);
			this.setAtlas(layer, atlas);
		}
		if (layer.atlas !== atlas) {
			layer.atlas = atlas;
			layer.frame = -1;
			this.setAtlas(layer, atlas);
		}
		if (layer.clip.parentElement !== target) target.appendChild(layer.clip);
        const filter = item["class"] === "glyph" ? glyphColourFilter(Number(item["glyph_level"] || 0)) : "";
        if (layer.clip.style.filter !== filter) layer.clip.style.filter = filter;
		const frame = item.getXFrameIndex();
		if (frame < 0 || (frame + 1) * 34 > atlas.width) {
			this.clear(target, icon);
			return false;
		}
		if (layer.frame !== frame) {
			if (layer.frames) {
				// Only two writes per tick, not one per frame in a long animation.
				if (layer.frame >= 0) layer.frames[layer.frame].style.opacity = "0";
				layer.frames[frame].style.opacity = "1";
			} else {
				layer.sheet.style.transform = `translate3d(${-frame * 34}px, 0, 0)`;
			}
			layer.frame = frame;
		}
		if (icon.style.visibility !== "hidden") icon.style.visibility = "hidden";
		return true;
	}

	private static setAtlas(layer: Layer, atlas: Atlas) {
		if (layer.frames) {
			for (const [index, frame] of layer.frames.entries()) {
				frame.style.backgroundImage = `url(${atlas.frames![index].url})`;
				frame.style.opacity = "0";
			}
		} else {
			layer.sheet.style.backgroundImage = `url(${atlas.url})`;
			layer.sheet.style.width = atlas.width + "px";
		}
	}

	public static clear(target: HTMLElement, icon: HTMLElement) {
		const layer = this.layers.get(target);
		if (layer) {
			layer.clip.remove();
			this.layers.delete(target);
			icon.style.visibility = "";
		}
	}

	private static atlas(item: Item, active?: Atlas, fixedFrames = false): Atlas|undefined {
		const source = singletons.getSpriteStore().get(item.sprite.filename) as HTMLImageElement;
		if (!source || source.complete === false || source.width <= 32 || !source.height
				|| source.width % 32 !== 0 || source.width / 32 * 34 > 4096) return;
		const row = item.getYFrameIndex();
		if (row < 0 || (row + 1) * 32 > source.height) return;
		const rarity = AdminDiagnostics.get("hideRarity") ? undefined : item.getRarity();
		let id = this.sources.get(source);
		if (id === undefined) { id = ++this.nextSource; this.sources.set(source, id); }
		const key = `${id}/${source.currentSrc || source.src}/${source.width}/${source.height}/${row}/${rarity?.id || "plain"}/${fixedFrames ? "local" : "strip"}`;
		// An active slot already owns this atlas, even if the shared LRU evicted it.
		// Large inventories must not encode the same live sheet every 100 ms.
		if (active?.key === key) return active;
		if (this.atlases.has(key)) {
			const cached = this.atlases.get(key);
			// Touch the shared atlas without rebuilding or reading its pixels.
			this.atlases.delete(key); this.atlases.set(key, cached || null);
			return cached || undefined;
		}
		try {
			const canvas = document.createElement("canvas");
			canvas.width = source.width / 32 * 34;
			canvas.height = 34;
			const context = canvas.getContext("2d");
			if (!context) { this.remember(key, null); return; }
			for (let x = 0; x < source.width / 32; x++) {
				context.drawImage(source, x * 32, row * 32, 32, 32, x * 34 + 1, 1, 32, 32);
				if (rarity) {
					const outline = ItemRarityEffects.getFrame(source, x * 32, row * 32, rarity);
					if (!outline) { this.remember(key, null); return; }
					context.drawImage(outline, x * 34, 0);
				}
			}
			const url = canvas.toDataURL();
			const image = document.createElement("img");
			image.src = url;
			const atlas: Atlas = {key, url, width: canvas.width, bytes: canvas.width * 34 * 4, image};
			if (fixedFrames) {
				atlas.frames = [];
				for (let x = 0; x < source.width / 32; x++) {
					const frame = document.createElement("canvas");
					frame.width = frame.height = 34;
					const frameContext = frame.getContext("2d");
					if (!frameContext) { this.remember(key, null); return; }
					frameContext.drawImage(canvas, x * 34, 0, 34, 34, 0, 0, 34, 34);
					const frameUrl = frame.toDataURL();
					const frameImage = document.createElement("img");
					frameImage.src = frameUrl;
					atlas.frames.push({url: frameUrl, image: frameImage});
				}
				// Account for both the shared strip and decoded local frame images.
				atlas.bytes *= 2;
			}
			this.remember(key, atlas);
			return atlas;
		} catch {
			// Cache failures too, so a CORS/tainted image is not retried every tick.
			this.remember(key, null);
			return;
		}
	}

	private static remember(key: string, atlas: Atlas|null) {
		this.atlases.set(key, atlas);
		this.bytes += atlas?.bytes || 0;
		// Bound both decoded pixels and failed/pending entries on long sessions.
		while (this.bytes > this.MAX_BYTES || this.atlases.size > 256) {
			const [oldestKey, oldest] = this.atlases.entries().next().value!;
			this.bytes -= oldest?.bytes || 0;
			this.atlases.delete(oldestKey);
		}
	}
}
