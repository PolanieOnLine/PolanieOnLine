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

/**
 * Shared ticker for item sprite animations.
 */
export class ItemAnimationClock {
	private static readonly FRAME_DELAY = 100;
	private static readonly MAX_ANIMATIONS_PER_FRAME = 200;
	private static frameCounts = new Map<string, number>();
	private static frameIndexCache = new Map<string, number>();
	private static elapsed = 0;
	private static lastTick = performance.now();
	private static frameBudget = ItemAnimationClock.MAX_ANIMATIONS_PER_FRAME;
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
		this.frameBudget = ItemAnimationClock.MAX_ANIMATIONS_PER_FRAME;
		this.usedVisible = 0;
		this.usedUi = 0;
		this.usedOffscreen = 0;
	}

	private static consumeBudget(priority: ItemAnimationPriority): boolean {
		if (this.frameBudget <= 0) {
			return false;
		}

		switch (priority) {
			case ItemAnimationPriority.Visible:
				this.usedVisible += 1;
				break;
			case ItemAnimationPriority.Ui:
				this.usedUi += 1;
				break;
			case ItemAnimationPriority.Offscreen:
			default:
				this.usedOffscreen += 1;
				break;
		}

		this.frameBudget -= 1;
		return true;
	}
}

export enum ItemAnimationPriority {
	Visible = "visible",
	Ui = "ui",
	Offscreen = "offscreen",
}
