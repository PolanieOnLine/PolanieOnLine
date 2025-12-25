/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ImageWithDimensions } from "../data/ImageWithDimensions";

export interface CachedFrame {
	image: CanvasImageSource;
	width: number;
	height: number;
}

interface CacheEntry extends CachedFrame {
	lastUsed: number;
	cost: number;
}

export interface SpriteFrameCacheMetrics {
	entries: number;
	pixels: number;
	hits: number;
	misses: number;
}

export class SpriteFrameCache {

	private static instance: SpriteFrameCache;

	private cache = new Map<string, CacheEntry>();
	private totalPixels = 0;
	private readonly ttlMs = 45000;
	private readonly maxPixels = 16 * 1024 * 1024;
	private hits = 0;
	private misses = 0;
	private readonly metricsScratch: SpriteFrameCacheMetrics = {
		entries: 0,
		pixels: 0,
		hits: 0,
		misses: 0
	};

	static get(): SpriteFrameCache {
		if (!SpriteFrameCache.instance) {
			SpriteFrameCache.instance = new SpriteFrameCache();
		}
		return SpriteFrameCache.instance;
	}

	buildKey(
		spriteId: string,
		frameIndex: number,
		orientation: number,
		width: number,
		height: number,
		scale: number,
		sourceX: number,
		sourceY: number,
		palette?: string
	): string {
		return spriteId + "|" + frameIndex + "|" + orientation + "|" + width + "x" + height + "|" + scale
			+ "|" + sourceX + "|" + sourceY + "|" + (palette || "");
	}

	getFrame(
		key: string,
		image: CanvasImageSource & ImageWithDimensions,
		sourceX: number,
		sourceY: number,
		width: number,
		height: number,
		scale = 1
	): CachedFrame {
		if (!width || !height) {
			return { image, width, height };
		}
		const now = Date.now();
		const existing = this.cache.get(key);
		if (existing) {
			existing.lastUsed = now;
			this.hits++;
			return existing;
		}

		const destWidth = Math.max(1, Math.round(width * scale));
		const destHeight = Math.max(1, Math.round(height * scale));
		const canvas = this.createCanvas(destWidth, destHeight);
		const ctx = canvas.getContext("2d");
		if (!ctx) {
			return { image, width, height };
		}
		ctx.setTransform(1, 0, 0, 1, 0, 0);
		ctx.imageSmoothingEnabled = false;
		ctx.clearRect(0, 0, destWidth, destHeight);
		ctx.drawImage(image, sourceX, sourceY, width, height, 0, 0, destWidth, destHeight);

		const entry: CacheEntry = {
			image: canvas,
			width: destWidth,
			height: destHeight,
			lastUsed: now,
			cost: destWidth * destHeight
		};
		this.cache.set(key, entry);
		this.totalPixels += entry.cost;
		this.misses++;
		this.prune(now);
		return entry;
	}

	getMetrics(target?: SpriteFrameCacheMetrics): SpriteFrameCacheMetrics {
		const out = target || this.metricsScratch;
		out.entries = this.cache.size;
		out.pixels = this.totalPixels;
		out.hits = this.hits;
		out.misses = this.misses;
		return out;
	}

	private createCanvas(width: number, height: number): HTMLCanvasElement | OffscreenCanvas {
		if (typeof OffscreenCanvas !== "undefined") {
			return new OffscreenCanvas(width, height);
		}
		const canvas = document.createElement("canvas");
		canvas.width = width;
		canvas.height = height;
		return canvas;
	}

	private prune(now: number) {
		if (this.cache.size === 0) {
			return;
		}
		const ttl = this.ttlMs;
		for (const [key, value] of this.cache) {
			if (now - value.lastUsed > ttl) {
				this.cache.delete(key);
				this.totalPixels -= value.cost;
			}
		}
		if (this.totalPixels <= this.maxPixels && this.cache.size <= 256) {
			return;
		}
		while ((this.totalPixels > this.maxPixels || this.cache.size > 512) && this.cache.size > 0) {
			let oldestKey = "";
			let oldestTime = Number.MAX_SAFE_INTEGER;
			for (const [key, value] of this.cache) {
				if (value.lastUsed < oldestTime) {
					oldestTime = value.lastUsed;
					oldestKey = key;
				}
			}
			if (!oldestKey) {
				break;
			}
			const entry = this.cache.get(oldestKey);
			this.cache.delete(oldestKey);
			if (entry) {
				this.totalPixels -= entry.cost;
			}
		}
	}
}
