/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

type CachedCanvas = OffscreenCanvas | HTMLCanvasElement;

interface CacheEntry {
	canvas: CachedCanvas;
	lastUsed: number;
}

const TTL_MS = 30_000;
const CLEANUP_INTERVAL_MS = 5_000;

/**
 * Pre-samples animated item frames onto an OffscreenCanvas (when supported) and
 * keeps them cached only while the animation is active.
 */
export class AnimatedItemSampler {

	private readonly cache = new Map<string, CacheEntry>();
	private readonly sourceIds = new WeakMap<object, number>();
	private nextId = 0;
	private cleanupHandle?: ReturnType<typeof setInterval>;

	public getFrame(source: CanvasImageSource, sx: number, sy: number, width: number, height: number)
		: CanvasImageSource | undefined {
		if (!this.isSupported()) {
			return undefined;
		}
		const key = this.getKey(source, sx, sy, width, height);
		const now = Date.now();
		const cached = this.cache.get(key);
		if (cached) {
			cached.lastUsed = now;
			return cached.canvas;
		}

		const canvas = this.createCanvas(width, height);
		const ctx = canvas.getContext("2d");
		if (!this.is2DContext(ctx)) {
			return undefined;
		}
		ctx.clearRect(0, 0, width, height);
		ctx.drawImage(source, sx, sy, width, height, 0, 0, width, height);

		this.cache.set(key, { canvas, lastUsed: now });
		this.ensureCleanup();
		return canvas;
	}

	private isSupported(): boolean {
		return typeof OffscreenCanvas !== "undefined" || typeof document !== "undefined";
	}

	private createCanvas(width: number, height: number): CachedCanvas {
		if (typeof OffscreenCanvas !== "undefined") {
			return new OffscreenCanvas(width, height);
		}
		const canvas = document.createElement("canvas");
		canvas.width = width;
		canvas.height = height;
		return canvas;
	}

	private getKey(source: CanvasImageSource, sx: number, sy: number, width: number, height: number): string {
		const id = this.getSourceId(source);
		return `${id}:${sx},${sy},${width}x${height}`;
	}

	private getSourceId(source: CanvasImageSource): number {
		const key = source as object;
		let id = this.sourceIds.get(key);
		if (!id) {
			id = ++this.nextId;
			this.sourceIds.set(key, id);
		}
		return id;
	}

	private ensureCleanup(): void {
		if (this.cleanupHandle) {
			return;
		}
		this.cleanupHandle = setInterval(() => this.cleanup(), CLEANUP_INTERVAL_MS);
	}

	private cleanup(): void {
		const now = Date.now();
		for (const [key, entry] of this.cache.entries()) {
			if (now - entry.lastUsed > TTL_MS) {
				this.cache.delete(key);
			}
		}
		if (this.cache.size === 0 && this.cleanupHandle) {
			clearInterval(this.cleanupHandle);
			this.cleanupHandle = undefined;
		}
	}

	private is2DContext(ctx: RenderingContext | OffscreenCanvasRenderingContext2D | null): ctx is CanvasRenderingContext2D | OffscreenCanvasRenderingContext2D {
		return !!ctx && typeof (ctx as CanvasRenderingContext2D).drawImage === "function";
	}
}

export const animatedItemSampler = new AnimatedItemSampler();
