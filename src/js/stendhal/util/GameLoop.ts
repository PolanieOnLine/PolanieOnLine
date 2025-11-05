/***************************************************************************
 *                 Copyright © 2024 - Faiumoni e. V.                       *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

export type UpdateCallback = (fixedDtMs: number) => void;
export type RenderCallback = (alpha: number) => void;
export type FpsSampleCallback = (fps: number) => void;

export interface GameLoopOptions {
        prefer144hz?: boolean;
        fpsLimit?: number;
        onFpsSample?: FpsSampleCallback;
}

/**
 * Game loop implementation using requestAnimationFrame and a fixed update
 * timestep. Rendering receives an interpolation factor so moving objects can
 * be drawn smoothly even if the simulation falls slightly behind.
 */
export class GameLoop {

        private static readonly MAX_FRAME_TIME = 250; // ms

        private readonly fixedDt: number;
        private minFrameInterval?: number;

        private accumulator = 0;
        private limiterAccumulator = 0;
        private lastNow = 0;
        private rafHandle = 0;
        private running = false;

        private frameCounter = 0;
        private frameCounterElapsed = 0;

        constructor(
                private readonly updateCb: UpdateCallback,
                private readonly renderCb: RenderCallback,
                private readonly options: GameLoopOptions = {}
        ) {
                const baseHz = options.prefer144hz ? 144 : 120;
                this.fixedDt = 1000 / baseHz;

                this.setFpsLimit(options.fpsLimit);
        }

        /**
         * Starts the loop if it is not running already.
         */
        start() {
                if (this.running) {
                        return;
                }
                this.running = true;
                this.accumulator = 0;
                this.limiterAccumulator = 0;
                this.lastNow = performance.now();
                this.rafHandle = requestAnimationFrame((now) => this.tick(now));
        }

        /**
         * Stops the loop.
         */
        stop() {
                if (!this.running) {
                        return;
                }
                cancelAnimationFrame(this.rafHandle);
                this.running = false;
        }

        private tick(now: number) {
                if (!this.running) {
                        return;
                }

                let frameTime = now - this.lastNow;
                this.lastNow = now;

                if (frameTime > GameLoop.MAX_FRAME_TIME) {
                        frameTime = GameLoop.MAX_FRAME_TIME;
                }

                this.accumulator += frameTime;

                while (this.accumulator >= this.fixedDt) {
                        this.updateCb(this.fixedDt);
                        this.accumulator -= this.fixedDt;
                }

                const alpha = this.accumulator / this.fixedDt;

                let shouldRender = true;
                if (this.minFrameInterval) {
                        this.limiterAccumulator += frameTime;
                        if (this.limiterAccumulator < this.minFrameInterval) {
                                shouldRender = false;
                        } else {
                                this.limiterAccumulator %= this.minFrameInterval;
                        }
                }

                if (shouldRender) {
                        this.renderCb(alpha);
                        this.trackFps(frameTime);
                }

                this.rafHandle = requestAnimationFrame((nextNow) => this.tick(nextNow));
        }

        setFpsLimit(limit?: number) {
                if (typeof(limit) === "number" && Number.isFinite(limit) && limit > 0) {
                        this.minFrameInterval = 1000 / limit;
                } else {
                        this.minFrameInterval = undefined;
                }
                this.limiterAccumulator = 0;
        }

        private trackFps(frameTime: number) {
                this.frameCounter++;
                this.frameCounterElapsed += frameTime;
                if (this.frameCounterElapsed < 1000) {
                        return;
                }

                const elapsedSeconds = this.frameCounterElapsed / 1000;
                const fps = this.frameCounter / elapsedSeconds;
                if (this.options.onFpsSample) {
                        this.options.onFpsSample(fps);
                }
                this.frameCounter = 0;
                this.frameCounterElapsed = 0;
        }
}

