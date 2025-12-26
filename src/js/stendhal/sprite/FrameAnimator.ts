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
 * Lightweight frame animator inspired by the Java client implementation.
 *
 * It tracks the active frame based on elapsed time that is supplied by the
 * caller (e.g. the game loop), so animations stay deterministic and in sync
 * with the rendering cadence instead of relying on `Date.now()` at draw time.
 */
export class FrameAnimator {

	private readonly delays: number[];
	private readonly delaySignature: string;
	private readonly loop: boolean;
	private readonly pingPong: boolean;
	private readonly idleFrame: number;

	private animating: boolean;
	private index: number;
	private direction = 1;
	private cycleTime = 0;
	private carryOver = 0;
	private static globalFpsCap?: number;

	/**
	 * @param frameCount
	 *   Number of frames available in the animation.
	 * @param delay
	 *   Either a single delay (ms) or an array of per-frame delays.
	 * @param idleFrame
	 *   Frame index to use when animation is stopped.
	 * @param loop
	 *   Whether the animation loops after the last frame.
	 * @param pingPong
	 *   When true, animation reverses at the end instead of jumping back.
	 * @param animating
	 *   Initial animation state.
	 */
	constructor(
		private readonly frameCount: number,
		delay: number | number[],
		idleFrame = 0,
		loop = true,
		pingPong = false,
		animating = true
	) {
		const resolvedDelays = Array.isArray(delay) ? delay.slice() : new Array(frameCount).fill(delay);
		this.delays = resolvedDelays.map((value) => Math.max(0, value));
		this.delaySignature = this.delays.join(",");
		this.loop = loop;
		this.pingPong = pingPong;
		this.idleFrame = Math.min(Math.max(idleFrame, 0), Math.max(frameCount - 1, 0));
		this.animating = animating && frameCount > 1;
		this.index = this.animating ? 0 : this.idleFrame;
	}

	advance(deltaMs: number): void {
		if (!Number.isFinite(deltaMs) || deltaMs <= 0) {
			return;
		}

		deltaMs = this.applyGlobalFpsCap(deltaMs);
		if (deltaMs <= 0) {
			return;
		}

		if (!this.animating || this.frameCount <= 1) {
			this.index = this.idleFrame;
			this.cycleTime = 0;
			return;
		}

		this.cycleTime += deltaMs;
		let currentDelay = this.getCurrentDelay();
		while (currentDelay > 0 && this.cycleTime >= currentDelay) {
			this.cycleTime -= currentDelay;
			if (!this.stepFrame()) {
				break;
			}
			currentDelay = this.getCurrentDelay();
		}
	}

	getFrame(): number {
		return this.index;
	}

	setAnimating(animating: boolean): void {
		this.animating = animating && this.frameCount > 1;
		if (!this.animating) {
			this.index = this.idleFrame;
			this.cycleTime = 0;
			this.direction = 1;
		}
	}

	reset(index = this.idleFrame): void {
		this.index = Math.min(Math.max(index, 0), Math.max(this.frameCount - 1, 0));
		this.cycleTime = 0;
		this.direction = 1;
	}

	getFrameCount(): number {
		return this.frameCount;
	}

	getDelaySignature(): string {
		return this.delaySignature;
	}

	getIdleFrame(): number {
		return this.idleFrame;
	}

	static setGlobalFpsCap(fps?: number): void {
		if (typeof fps === "number" && Number.isFinite(fps) && fps > 0) {
			FrameAnimator.globalFpsCap = fps;
		} else {
			FrameAnimator.globalFpsCap = undefined;
		}
	}

	private getCurrentDelay(): number {
		if (this.index < 0 || this.index >= this.delays.length) {
			return 0;
		}
		return this.delays[this.index];
	}

	/**
	 * Advances the frame index by one step.
	 *
	 * @return true when another step is allowed, false if animation stopped.
	 */
	private stepFrame(): boolean {
		if (this.frameCount <= 1) {
			return false;
		}

		this.index += this.direction;
		const lastIndex = this.frameCount - 1;

		if (this.index > lastIndex || this.index < 0) {
			if (!this.loop) {
				this.setAnimating(false);
				return false;
			}

			if (this.pingPong) {
				this.direction *= -1;
				this.index = Math.max(0, Math.min(lastIndex, this.index + 2 * this.direction));
			} else {
				this.index = (this.index + this.frameCount) % this.frameCount;
			}
		}

			return true;
		}

	/**
	 * Applies the global FPS cap (if configured) by accumulating elapsed time
	 * and only advancing when the minimum step duration is reached.
	 */
	private applyGlobalFpsCap(deltaMs: number): number {
		if (!FrameAnimator.globalFpsCap) {
			return deltaMs;
		}

		const minStep = 1000 / FrameAnimator.globalFpsCap;
		this.carryOver += deltaMs;
		if (this.carryOver < minStep) {
			return 0;
		}
		const effectiveDelta = this.carryOver;
		this.carryOver = 0;
		return effectiveDelta;
	}
}
