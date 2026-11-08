/***************************************************************************
 *                    Copyright © 2003-2023 - Stendhal                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Paths } from "./Paths";
import { JSONLoader } from "../util/JSONLoader";

export interface TileAnimation {
	frames: number[];
	delays: number[];
	duration: number;
}

export type AnimationMap = {[filename: string]: {[index: number]: TileAnimation}};


export class TileStore {

	private readonly DEFAULT_DELAY = 500;
	private readonly emptyMap: AnimationMap = {};
	private readonly started = Date.now();

	private landscapeMap?: AnimationMap;
	private weatherMap?: AnimationMap;

	/** Singleton instance. */
	private static instance: TileStore;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): TileStore {
		if (!this.instance) {
			this.instance = new TileStore();
		}
		return this.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	getLandscapeMap(): AnimationMap {
		return this.landscapeMap || this.emptyMap;
	}

	getWeatherMap(): AnimationMap {
		return this.weatherMap || this.emptyMap;
	}

	/** Resolve a tile using the same frame sequence and durations as the Java client. */
	getLandscapeFrame(filename: string, index: number, elapsed = Date.now() - this.started): number {
		const animation = this.landscapeMap?.[filename]?.[index];
		if (!animation || animation.duration <= 0) {
			return index;
		}
		let remaining = Math.max(0, elapsed) % animation.duration;
		for (let frame = 0; frame < animation.frames.length; frame++) {
			if (remaining < animation.delays[frame]) {
				return animation.frames[frame];
			}
			remaining -= animation.delays[frame];
		}
		return index;
	}

	/**
	 * Initializes landscape & weather tile animations.
	 */
	init() {
		if (this.landscapeMap && this.weatherMap) {
			console.warn("tried to re-initialize tile animations");
			return;
		}

		const loader = new JSONLoader();
		loader.onDataReady = () => {
			this.landscapeMap = this.loadAnimations(
					loader.data["landscape"], Paths.tileset + "/");
			this.weatherMap = this.loadAnimations(
					loader.data["weather"], Paths.weather + "/");
		};
		loader.load(Paths.tileset + "/animation.json");
	}

	/**
	 * Animations are stored using the tileset name as key with value
	 * being a map indexed by initial frames. Frame map contains two
	 * lists: <code>frames</code> (a list of frames used in animation)
	 * & <code>delays</code> (a list of delay values for the
	 * corresponding frame of each index).
	 *
	 * ani = animationMap[tileset_name];
	 * frames = ani[0].frames
	 * delays = ani[0].delays
	 *
	 * @param def
	 *     Unformatted animations lists indexed by tileset name.
	 *     Example: {ts1: [ani1, ani2, ...], ts2: [ani1, ani2, ...], ...}
	 * @param prefix
	 *     Parent directory containing the target tileset images.
	 * @return
	 *     Map of animations.
	 */
	private loadAnimations(def: any, prefix: string): AnimationMap {
		const ani: AnimationMap = {};
		for (const tsname of Object.keys(def)) {
			const entry: {[index: number]: TileAnimation} = {};
			for (const line of def[tsname]) {
				const [idToken, frameTokens] = line.trim().split(/\s+/);
				if (!idToken || !frameTokens) {
					continue;
				}
				const [id, delayToken] = idToken.split("@");
				const defaultDelay = delayToken === undefined ? this.DEFAULT_DELAY : Number(delayToken);
				const frames: number[] = [];
				const delays: number[] = [];
				for (const token of frameTokens.split(":")) {
					const [frame, customDelay] = token.split("@");
					frames.push(Number(frame));
					delays.push(customDelay === undefined ? defaultDelay : Number(customDelay));
				}
				if (frames.some(frame => !Number.isInteger(frame) || frame < 0)
						|| delays.some(delay => !Number.isFinite(delay) || delay <= 0)) {
					continue;
				}
				const duration = delays.reduce((sum, delay) => sum + delay, 0);
				if (id === "*") {
					// Each placed frame starts its own rotated sequence, just like TilesetAnimationMap.
					for (let start = 0; start < frames.length; start++) {
						entry[frames[start]] = {
							frames: frames.slice(start).concat(frames.slice(0, start)),
							delays: delays.slice(start).concat(delays.slice(0, start)),
							duration
						};
					}
				} else if (Number.isInteger(Number(id)) && Number(id) >= 0) {
					entry[Number(id)] = {frames, delays, duration};
				}
			}
			ani[prefix + tsname + ".png"] = entry;
		}
		return ani;
	}
}
