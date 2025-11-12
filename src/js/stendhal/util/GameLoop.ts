/***************************************************************************
 *                 Copyright © 2025 - Faiumoni e. V.                       *
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
        fpsLimit?: number;
        onFpsSample?: FpsSampleCallback;
}

interface NavigatorWithBattery extends Navigator {
        getBattery?: () => Promise<BatteryManager>;
}

interface BatteryManager extends EventTarget {
        charging: boolean;
        level: number;
        addEventListener(type: "chargingchange" | "levelchange", listener: EventListener): void;
        removeEventListener(type: "chargingchange" | "levelchange", listener: EventListener): void;
}

/**
 * Game loop implementation using requestAnimationFrame and a fixed update
 * timestep. Rendering receives an interpolation factor so moving objects can
 * be drawn smoothly even if the simulation falls slightly behind.
 */
export class GameLoop {

        private static readonly MAX_FRAME_TIME = 250; // ms
        private static readonly FIXED_UPDATE_HZ = 144;
        private static readonly BACKGROUND_THROTTLE_MS = 1000;
        private static readonly FPS_WINDOW_MS = 750;
        private static readonly FPS_HISTORY_SIZE = 240;

        private readonly fixedDt: number = 1000 / GameLoop.FIXED_UPDATE_HZ;

        private accumulator = 0;
        private lastNow = 0;
        private lastRenderNow = 0;
        private rafHandle = 0;
        private slowTimerHandle: number | null = null;
        private running = false;

        private targetFrameTime = 0;
        private userFrameTime = 0;
        private saveDataFrameTime = 0;
        private batteryFrameTime = 0;
        private nextFrameDeadline = 0;

        private readonly fpsTimestamps = new Float64Array(GameLoop.FPS_HISTORY_SIZE);
        private fpsHead = 0;
        private fpsCount = 0;

        private readonly visibilityListener: () => void;

        constructor(
                private readonly updateCb: UpdateCallback,
                private readonly renderCb: RenderCallback,
                private readonly options: GameLoopOptions = {}
        ) {
                this.visibilityListener = () => this.onVisibilityChanged();

                if (typeof document !== "undefined") {
                        document.addEventListener("visibilitychange", this.visibilityListener, {passive: true});
                }

                this.setupSaveDataCap();
                this.setupBatteryCap();

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
                this.nextFrameDeadline = 0;
                this.fpsHead = 0;
                this.fpsCount = 0;
                this.lastRenderNow = 0;
                if (this.slowTimerHandle !== null) {
                        clearTimeout(this.slowTimerHandle);
                        this.slowTimerHandle = null;
                }
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
                this.running = false;
                cancelAnimationFrame(this.rafHandle);
                if (this.slowTimerHandle !== null) {
                        clearTimeout(this.slowTimerHandle);
                        this.slowTimerHandle = null;
                }
        }

        setFpsLimit(limit?: number) {
                if (typeof(limit) === "number" && Number.isFinite(limit) && limit > 0) {
                        this.userFrameTime = 1000 / limit;
                } else {
                        this.userFrameTime = 0;
                }
                this.updateTargetFrameTime();
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

                const alpha = this.fixedDt > 0 ? (this.accumulator / this.fixedDt) : 0;

                const shouldRender = this.shouldRenderFrame(now);
                if (shouldRender) {
                        this.renderCb(alpha);
                        this.trackFps(now);
                }

                this.scheduleNextTick();
        }

        private shouldRenderFrame(now: number): boolean {
                if (this.targetFrameTime <= 0) {
                        return true;
                }

                if (this.nextFrameDeadline <= 0) {
                        this.nextFrameDeadline = now;
                }

                if (now < this.nextFrameDeadline) {
                        return false;
                }

                const behind = now - this.nextFrameDeadline;
                if (behind >= this.targetFrameTime) {
                        const intervalsMissed = Math.floor(behind / this.targetFrameTime) + 1;
                        this.nextFrameDeadline += intervalsMissed * this.targetFrameTime;
                } else {
                        this.nextFrameDeadline += this.targetFrameTime;
                }

                return true;
        }

        private scheduleNextTick() {
                if (!this.running) {
                        return;
                }

                if (typeof document !== "undefined" && document.visibilityState === "hidden") {
                        if (this.slowTimerHandle !== null) {
                                return;
                        }
                        this.slowTimerHandle = window.setTimeout(() => {
                                if (!this.running) {
                                        return;
                                }
                                this.slowTimerHandle = null;
                                this.lastNow = performance.now();
                                this.rafHandle = requestAnimationFrame((nextNow) => this.tick(nextNow));
                        }, GameLoop.BACKGROUND_THROTTLE_MS);
                        return;
                }

                if (this.slowTimerHandle !== null) {
                        clearTimeout(this.slowTimerHandle);
                        this.slowTimerHandle = null;
                }

                this.rafHandle = requestAnimationFrame((nextNow) => this.tick(nextNow));
        }

        private trackFps(now: number) {
                if (this.lastRenderNow === 0) {
                        this.lastRenderNow = now;
                }

                const delta = now - this.lastRenderNow;
                this.lastRenderNow = now;

                const idx = this.fpsHead;
                this.fpsTimestamps[idx] = now;
                this.fpsHead = (idx + 1) % this.fpsTimestamps.length;
                if (this.fpsCount < this.fpsTimestamps.length) {
                        this.fpsCount++;
                }

                const cutoff = now - GameLoop.FPS_WINDOW_MS;
                while (this.fpsCount > 1) {
                        const tailIndex = (this.fpsHead - this.fpsCount + this.fpsTimestamps.length) % this.fpsTimestamps.length;
                        if (this.fpsTimestamps[tailIndex] >= cutoff) {
                                break;
                        }
                        this.fpsCount--;
                }

                let fps = 0;
                if (this.fpsCount > 1) {
                        const tailIndex = (this.fpsHead - this.fpsCount + this.fpsTimestamps.length) % this.fpsTimestamps.length;
                        const elapsed = now - this.fpsTimestamps[tailIndex];
                        if (elapsed > 0) {
                                fps = (this.fpsCount - 1) / (elapsed / 1000);
                        }
                } else if (delta > 0) {
                        fps = 1000 / delta;
                }

                if (fps > 0 && this.options.onFpsSample) {
                        this.options.onFpsSample(fps);
                }
        }

        private updateTargetFrameTime() {
                let frameTime = this.userFrameTime;
                const userHasExplicitLimit = frameTime > 0;

                if (!userHasExplicitLimit) {
                        frameTime = this.applyFrameCap(frameTime, this.saveDataFrameTime);
                }
                frameTime = this.applyFrameCap(frameTime, this.batteryFrameTime);
                this.targetFrameTime = frameTime;
                this.nextFrameDeadline = 0;
        }

        private applyFrameCap(baseFrameTime: number, capFrameTime: number): number {
                if (capFrameTime <= 0) {
                        return baseFrameTime;
                }
                if (baseFrameTime <= 0) {
                        return capFrameTime;
                }
                return Math.max(baseFrameTime, capFrameTime);
        }

        private setupSaveDataCap() {
                if (typeof navigator === "undefined") {
                        return;
                }
                const navAny = navigator as Navigator & {connection?: any};
                const connection = navAny.connection;
                if (!connection) {
                        return;
                }
                const apply = () => {
                        const enabled = Boolean(connection.saveData);
                        this.saveDataFrameTime = enabled ? (1000 / 30) : 0;
                        this.updateTargetFrameTime();
                };
                apply();
                if (typeof connection.addEventListener === "function") {
                        connection.addEventListener("change", apply);
                } else if ("onchange" in connection) {
                        connection.onchange = apply;
                }
        }

        private setupBatteryCap() {
                if (typeof navigator === "undefined") {
                        return;
                }

                const navWithBattery = navigator as NavigatorWithBattery;
                if (typeof navWithBattery.getBattery !== "function") {
                        return;
                }

                navWithBattery.getBattery().then((battery) => {
                        const apply = () => {
                                const shouldCap = !battery.charging && battery.level <= 0.2;
                                this.batteryFrameTime = shouldCap ? (1000 / 30) : 0;
                                this.updateTargetFrameTime();
                        };
                        apply();
                        battery.addEventListener("chargingchange", apply);
                        battery.addEventListener("levelchange", apply);
                }).catch(() => {
                        // ignore lack of support
                });
        }

        private onVisibilityChanged() {
                if (!this.running) {
                        return;
                }

                if (typeof document === "undefined") {
                        return;
                }

                if (document.visibilityState === "visible") {
                        if (this.slowTimerHandle !== null) {
                                clearTimeout(this.slowTimerHandle);
                                this.slowTimerHandle = null;
                        }
                        this.nextFrameDeadline = 0;
                        this.lastNow = performance.now();
                        this.rafHandle = requestAnimationFrame((now) => this.tick(now));
                } else {
                        this.nextFrameDeadline = 0;
                }
        }
}
