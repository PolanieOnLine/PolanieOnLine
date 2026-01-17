/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ConfigManager } from "../util/ConfigManager";

/**
 * Shared ticker for item sprite animations.
 */
export class ItemAnimationClock {
	private static readonly FRAME_DELAY = 100;
	private static readonly DEFAULT_MAX_VISIBLE_PER_FRAME = 1; // 240
	private static readonly DEFAULT_MAX_UI_PER_FRAME = 1; // 40
	private static readonly DEFAULT_MAX_OFFSCREEN_PER_FRAME = 1; // 40
	private static frameCounts = new Map<string, number>();
	private static frameIndexCache = new Map<string, number>();
	private static elapsed = 0;
	private static lastTick = performance.now();
	private static maxVisiblePerFrame = ItemAnimationClock.DEFAULT_MAX_VISIBLE_PER_FRAME;
	private static maxUiPerFrame = ItemAnimationClock.DEFAULT_MAX_UI_PER_FRAME;
	private static maxOffscreenPerFrame = ItemAnimationClock.DEFAULT_MAX_OFFSCREEN_PER_FRAME;
	private static usedVisible = 0;
	private static usedUi = 0;
	private static usedOffscreen = 0;

	static update(delta?: number) {
		const now = performance.now();
		const step = (typeof delta === "number" && Number.isFinite(delta))
			? Math.max(0, delta)
			: Math.max(0, now - this.lastTick);
		this.lastTick = (typeof delta === "number" && Number.isFinite(delta)) ? this.lastTick + step : now;
		this.resetFrameBudget();
		if (step === 0) {
			return;
		}

		this.elapsed += step;

		for (const [key, columns] of this.frameCounts) {
			const frameIndex = columns > 0 ? Math.floor((this.elapsed / ItemAnimationClock.FRAME_DELAY) % columns) : 0;
			this.frameIndexCache.set(key, frameIndex);
		}
	}

	static setFrameCount(key: string, columns: number) {
		const safeColumns = Math.max(1, columns);
		this.frameCounts.set(key, safeColumns);
		const currentIndex = Math.floor((this.elapsed / ItemAnimationClock.FRAME_DELAY) % safeColumns);
		this.frameIndexCache.set(key, currentIndex);
	}

	static getFrameIndex(key: string, columns: number, priority: ItemAnimationPriority = ItemAnimationPriority.Offscreen): number {
		this.setFrameCount(key, columns);
		if (!this.consumeBudget(priority)) {
			return Math.max(0, columns - 1);
		}
		return this.frameIndexCache.get(key) || 0;
	}

	private static resetFrameBudget() {
		const config = ConfigManager.get();
		this.maxVisiblePerFrame = Math.max(
			0,
			config.getInt("animation.item.visible.max-per-frame", ItemAnimationClock.DEFAULT_MAX_VISIBLE_PER_FRAME)
		);
		this.maxUiPerFrame = Math.max(
			0,
			config.getInt("animation.item.ui.max-per-frame", ItemAnimationClock.DEFAULT_MAX_UI_PER_FRAME)
		);
		this.maxOffscreenPerFrame = Math.max(
			0,
			config.getInt("animation.item.offscreen.max-per-frame", ItemAnimationClock.DEFAULT_MAX_OFFSCREEN_PER_FRAME)
		);
		this.usedVisible = 0;
		this.usedUi = 0;
		this.usedOffscreen = 0;
	}

	private static consumeBudget(priority: ItemAnimationPriority): boolean {
		switch (priority) {
			case ItemAnimationPriority.Visible:
				if (this.usedVisible >= this.maxVisiblePerFrame) {
					return false;
				}
				this.usedVisible += 1;
				return true;
			case ItemAnimationPriority.Ui:
				if (this.usedUi >= this.maxUiPerFrame) {
					return false;
				}
				this.usedUi += 1;
				return true;
			case ItemAnimationPriority.Offscreen:
			default:
				if (this.usedOffscreen >= this.maxOffscreenPerFrame) {
					return false;
				}
				this.usedOffscreen += 1;
				return true;
		}
	}
}

export enum ItemAnimationPriority {
	Visible = "visible",
	Ui = "ui",
	Offscreen = "offscreen",
}
