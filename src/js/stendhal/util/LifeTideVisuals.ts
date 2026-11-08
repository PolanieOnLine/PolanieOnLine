/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
import { RenderingContext2D } from "./Types";

export const LIFE_TIDE_COLOR = "#b67cf0";

export function lifeTideOpacity(now: number): number {
	return 0.65 - 0.18 * Math.cos(2 * Math.PI * (now % 2400) / 2400);
}

/** Surplus is measured against normal maximum HP, not the capacity of the affix. */
export function drawLifeTideOverlay(ctx: RenderingContext2D, x: number, y: number,
		width: number, height: number, surplusRatio: number, now?: number): void {
	if (!Number.isFinite(surplusRatio) || surplusRatio <= 0 || width <= 0 || height <= 0) {
		return;
	}
	ctx.save();
	try {
		ctx.globalAlpha *= lifeTideOpacity(now ?? Date.now());
		ctx.fillStyle = LIFE_TIDE_COLOR;
		const coveredWidth = Math.round(width * Math.min(1, surplusRatio));
		// Keep diagonal gaps untouched, revealing the original green health bar.
		for (let row = 0; row < height; row++) {
			for (let stripe = -row; stripe < coveredWidth; stripe += 8) {
				const left = Math.max(0, stripe);
				const right = Math.min(coveredWidth, stripe + 5);
				if (right > left) {
					ctx.fillRect(x + left, y + row, right - left, 1);
				}
			}
		}
	} finally {
		ctx.restore();
	}
}
