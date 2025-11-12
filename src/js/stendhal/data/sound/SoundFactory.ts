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
                        const fallbackSrc = absolute;
                        let resolvedSrc = fallbackSrc;

                        sound.src = fallbackSrc;
                        sound.load();

                        const ready = SoundFactory.getObjectUrl(absolute).then((objectUrl) => {
                                resolvedSrc = objectUrl;
                                if (sound.paused && sound.currentSrc !== objectUrl) {
                                        sound.src = objectUrl;
                                        sound.load();
                                }
                                return objectUrl;
                        }).catch((error) => {
                                console.warn("Falling back to direct audio src", absolute, error);
                                resolvedSrc = fallbackSrc;
                                return fallbackSrc;
                        });

                        const originalClone = sound.cloneNode.bind(sound);
                        sound.cloneNode = ((deep?: boolean) => {
                                const clone = originalClone(deep) as SoundObject;
                                clone.basevolume = sound.basevolume;
                                clone.preload = sound.preload;
                                clone.src = resolvedSrc;
                                if (clone.preload !== "none") {
                                        clone.load();
                                }
                                if (resolvedSrc === fallbackSrc) {
                                        ready.then((finalSrc) => {
                                                if (finalSrc !== clone.currentSrc && clone.paused) {
                                                        clone.src = finalSrc;
                                                        if (clone.preload !== "none") {
                                                                clone.load();
                                                        }
                                                }
                                        }).catch(() => {
                                                // ignore, clone already has fallback src
                                        });
                                }
                                return clone;
                        }) as typeof sound.cloneNode;
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
