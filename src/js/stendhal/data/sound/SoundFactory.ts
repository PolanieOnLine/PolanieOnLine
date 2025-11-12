/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { BinaryAssetCache } from "../../util/BinaryAssetCache";

/**
 * A playable sound.
 */
export interface SoundObject extends HTMLAudioElement {
	/** Base volume level unique to this sound. */
	basevolume: number;
	/** Distance at which sound can be heard. */
	radius?: number;
	/** Coordinate of sound entity on X axis. */
	x?: number;
	/** Coordinate of sound entity on Y axis. */
	y?: number;
	/** String identifier. */
	basename?: string;
}

/**
 * Factory for creating sound objects.
 */
export class SoundFactory {

	private static readonly binaryCache = BinaryAssetCache.get();
	private static readonly objectUrlCache = new Map<string, Promise<string>>();

	/**
	 * Hidden constructor (use `SoundFactory.create`).
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Creates a new sound object.
	 *
	 * @param src {string}
	 *   Sound filename path (default: `undefined`).
	 */
	static create(src?: string): SoundObject {
		const sound = new Audio() as SoundObject;
		sound.basevolume = sound.volume;
		sound.preload = "auto";
		if (src) {
			const absolute = SoundFactory.toAbsoluteUrl(src);
			const ready = SoundFactory.getObjectUrl(absolute).then((objectUrl) => {
				sound.src = objectUrl;
				sound.load();
			}).catch((error) => {
				console.warn("Falling back to direct audio src", absolute, error);
				sound.src = absolute;
				sound.load();
			});
			const originalPlay = sound.play.bind(sound);
			sound.play = () => ready.then(() => originalPlay());
		}
		return sound;
	}

	private static getObjectUrl(src: string): Promise<string> {
		let existing = SoundFactory.objectUrlCache.get(src);
		if (!existing) {
			existing = SoundFactory.binaryCache.load(src).then((blob) => {
				return URL.createObjectURL(blob);
			});
			SoundFactory.objectUrlCache.set(src, existing.then((url) => {
				SoundFactory.objectUrlCache.set(src, Promise.resolve(url));
				return url;
			}).catch((error) => {
				SoundFactory.objectUrlCache.delete(src);
				throw error;
			}));
		}
		return existing;
	}

	private static toAbsoluteUrl(src: string): string {
		try {
			return new URL(src, window.location.href).toString();
		} catch (error) {
			return src;
		}
	}
}
