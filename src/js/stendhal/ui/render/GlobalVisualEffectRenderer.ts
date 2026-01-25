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

import { RenderingContext2D } from "util/Types";

type GlobalVisualEffectName = "blacken" | "fog" | "lightning";

interface GlobalVisualEffectState {
	name: GlobalVisualEffectName;
	durationMs: number;
	strength?: number;
	startedAt: number;
	expires: boolean;
}

/**
 * Renderer for full-screen visual effects triggered by server events.
 */
export class GlobalVisualEffectRenderer {
	private static instance: GlobalVisualEffectRenderer;
	private effects: GlobalVisualEffectState[] = [];

	static get(): GlobalVisualEffectRenderer {
		if (!GlobalVisualEffectRenderer.instance) {
			GlobalVisualEffectRenderer.instance = new GlobalVisualEffectRenderer();
		}
		return GlobalVisualEffectRenderer.instance;
	}

	private constructor() {
		// singleton
	}

	addEffect(name: string, durationMs: number, strength?: number): void {
		const resolved = this.resolveName(name);
		if (!resolved) {
			console.warn(`Unknown global visual effect: ${name}`);
			return;
		}
		if (!Number.isFinite(durationMs) || durationMs <= 0) {
			console.warn(`Invalid duration for global visual effect: ${durationMs}`);
			return;
		}

		this.effects = this.effects.filter((effect) => effect.name !== resolved);
		this.effects.push({
			name: resolved,
			durationMs,
			strength,
			startedAt: performance.now(),
			expires: resolved !== "blacken",
		});
	}

	draw(ctx: RenderingContext2D, width: number, height: number): void {
		if (!this.effects.length) {
			return;
		}

		const now = performance.now();
		const remaining: GlobalVisualEffectState[] = [];

		for (const effect of this.effects) {
			const elapsed = now - effect.startedAt;
			if (effect.expires && elapsed >= effect.durationMs) {
				continue;
			}

			const alpha = this.resolveAlpha(effect, elapsed);
			if (alpha > 0) {
				ctx.save();
				ctx.globalAlpha = alpha;
				ctx.fillStyle = this.resolveColor(effect);
				ctx.fillRect(0, 0, width, height);
				ctx.restore();
			}

			remaining.push(effect);
		}

		this.effects = remaining;
	}

	private resolveName(name: string): GlobalVisualEffectName | null {
		if (!name) {
			return null;
		}
		switch (name.toLowerCase()) {
			case "blacken":
			case "fog":
			case "lightning":
				return name.toLowerCase() as GlobalVisualEffectName;
			default:
				return null;
		}
	}

	private resolveColor(effect: GlobalVisualEffectState): string {
		switch (effect.name) {
			case "fog":
				return "rgb(90, 90, 90)";
			case "lightning":
				return "rgb(255, 255, 255)";
			case "blacken":
			default:
				return "rgb(0, 0, 0)";
		}
	}

	private resolveAlpha(effect: GlobalVisualEffectState, elapsed: number): number {
		switch (effect.name) {
			case "blacken":
				return this.resolveBlackenAlpha(effect, elapsed);
			case "fog":
				return this.resolveFogAlpha(effect, elapsed);
			case "lightning":
				return this.resolveLightningAlpha(effect, elapsed);
			default:
				return 0;
		}
	}

	private resolveBlackenAlpha(effect: GlobalVisualEffectState, elapsed: number): number {
		const target = this.resolveStrength(effect, 0.75);
		const fadeDuration = Math.max(1, effect.durationMs);
		const progress = Math.min(1, elapsed / fadeDuration);
		return target * progress;
	}

	private resolveFogAlpha(effect: GlobalVisualEffectState, elapsed: number): number {
		const target = this.resolveStrength(effect, 0.35);
		const duration = Math.max(1, effect.durationMs);
		const progress = Math.min(1, elapsed / duration);
		const fadePortion = 0.2;
		const fadeIn = Math.min(1, progress / fadePortion);
		const fadeOut = Math.min(1, (1 - progress) / fadePortion);
		const intensity = Math.min(1, fadeIn, fadeOut);
		return target * intensity;
	}

	private resolveLightningAlpha(effect: GlobalVisualEffectState, elapsed: number): number {
		const target = this.resolveStrength(effect, 0.9);
		const duration = Math.max(1, effect.durationMs);
		const progress = Math.min(1, elapsed / duration);
		return target * (1 - progress);
	}

	private resolveStrength(effect: GlobalVisualEffectState, fallback: number): number {
		if (!Number.isFinite(effect.strength)) {
			return fallback;
		}
		const normalized = (effect.strength as number) / 100;
		return Math.max(0, Math.min(1, normalized || fallback));
	}
}
