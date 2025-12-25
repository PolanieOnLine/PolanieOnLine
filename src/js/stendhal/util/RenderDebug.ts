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

import { SpriteFrameCache, SpriteFrameCacheMetrics } from "./SpriteFrameCache";

interface RenderStats {
	drawCalls: number;
	frameMs: number;
	fps: number;
	cacheEntries: number;
	cachePixels: number;
	cacheHits: number;
	cacheMisses: number;
}

export class RenderDebug {

	private static instance: RenderDebug;

	private enabled = false;
	private label?: HTMLElement;
	private lastLog = 0;
	private readonly metricsScratch: SpriteFrameCacheMetrics = {
		entries: 0,
		pixels: 0,
		hits: 0,
		misses: 0
	};
	private readonly stats: RenderStats = {
		drawCalls: 0,
		frameMs: 0,
		fps: 0,
		cacheEntries: 0,
		cachePixels: 0,
		cacheHits: 0,
		cacheMisses: 0
	};

	static get(): RenderDebug {
		if (!RenderDebug.instance) {
			RenderDebug.instance = new RenderDebug();
		}
		return RenderDebug.instance;
	}

	init(config: any, parent?: HTMLElement) {
		if (this.enabled) {
			return;
		}
		if (!config || typeof config.getBoolean !== "function") {
			return;
		}
		this.enabled = !!config.getBoolean("debug.render_metrics");
		if (!this.enabled) {
			return;
		}
		this.label = this.createLabel(parent);
	}

	beginFrame() {
		if (!this.enabled) {
			return;
		}
		this.stats.drawCalls = 0;
	}

	recordDraw(count = 1) {
		if (!this.enabled) {
			return;
		}
		this.stats.drawCalls += count;
	}

	setFpsSample(fps: number) {
		if (!this.enabled) {
			return;
		}
		this.stats.fps = fps;
	}

	endFrame(durationMs: number) {
		if (!this.enabled) {
			return;
		}
		this.stats.frameMs = durationMs;
		const cacheMetrics = SpriteFrameCache.get().getMetrics(this.metricsScratch);
		this.stats.cacheEntries = cacheMetrics.entries;
		this.stats.cachePixels = cacheMetrics.pixels;
		this.stats.cacheHits = cacheMetrics.hits;
		this.stats.cacheMisses = cacheMetrics.misses;
		this.maybeReport();
	}

	private maybeReport() {
		const text = "draws:" + this.stats.drawCalls
			+ " cache:" + this.stats.cacheEntries
			+ " (" + this.stats.cacheHits + "/" + this.stats.cacheMisses + ")"
			+ " frame:" + this.stats.frameMs.toFixed(2) + "ms"
			+ " fps:" + (this.stats.fps || 0);
		if (this.label) {
			this.label.textContent = text;
			return;
		}
		const now = Date.now();
		if (now - this.lastLog > 2000) {
			console.debug("[render]", text);
			this.lastLog = now;
		}
	}

	private createLabel(parent?: HTMLElement): HTMLElement | undefined {
		if (!parent) {
			return undefined;
		}
		const label = document.createElement("div");
		label.style.position = "absolute";
		label.style.right = "6px";
		label.style.bottom = "6px";
		label.style.padding = "2px 6px";
		label.style.background = "rgba(0, 0, 0, 0.6)";
		label.style.color = "#0f0";
		label.style.fontFamily = "monospace";
		label.style.fontSize = "11px";
		label.style.pointerEvents = "none";
		parent.appendChild(label);
		return label;
	}
}
