/***************************************************************************
 *                (C) Copyright 2024 - Faiumoni e. V.                      *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

/**
 * Small helper around the CacheStorage API that keeps binary asset
 * responses in memory, deduplicates concurrent fetches and optionally limits
 * the number of network requests in flight.  It is primarily intended for
 * sprite images and audio files that are requested in bursts when a zone
 * loads.
 */
export class BinaryAssetCache {

        private static instance: BinaryAssetCache | undefined;

        private readonly inflight = new Map<string, Promise<Blob>>();
        private readonly cachePromise: Promise<Cache | null>;

        private readonly queue: Array<() => void> = [];
        private activeFetches = 0;

        private readonly maxConcurrent: number;
        private constructor(namespace: string, maxConcurrent: number) {
                this.maxConcurrent = Math.max(1, maxConcurrent);
                if (typeof caches !== "undefined") {
                        this.cachePromise = caches.open(namespace).catch(() => null);
                } else {
                        this.cachePromise = Promise.resolve(null);
                }
        }

        static get(namespace = "stendhal-assets-v1", maxConcurrent = 6): BinaryAssetCache {
                if (!BinaryAssetCache.instance) {
                        BinaryAssetCache.instance = new BinaryAssetCache(namespace, maxConcurrent);
                }
                return BinaryAssetCache.instance;
        }

	load(url: string): Promise<Blob> {
		const absolute = this.toAbsoluteUrl(url);
		let promise = this.inflight.get(absolute);
		if (!promise) {
			const fetchPromise = this.fetchWithCaching(absolute);
			promise = fetchPromise.then((result) => {
				this.inflight.delete(absolute);
				return result;
			}, (error) => {
				this.inflight.delete(absolute);
				throw error;
			});
			this.inflight.set(absolute, promise);
		}
		return promise;
	}


        private toAbsoluteUrl(url: string): string {
                try {
                        return new URL(url, window.location.href).toString();
                } catch (error) {
                        return url;
                }
        }

        private async fetchWithCaching(url: string): Promise<Blob> {
                await this.acquire();
                try {
                        const request = new Request(url, { credentials: "same-origin" });
                        const cache = await this.cachePromise;

                        let response: Response | undefined = undefined;
                        if (cache) {
                                response = await cache.match(request) ?? undefined;
                        }

                        if (!response) {
                                const requestInit: RequestInit = { cache: "force-cache" };
                                response = await fetch(request, requestInit).catch((error) => {
                                        throw new Error(`Failed to fetch asset ${url}: ${error}`);
                                });
                                if (!response.ok) {
                                        throw new Error(`Failed to fetch asset ${url}: ${response.status}`);
                                }
                                if (cache) {
                                        try {
                                                await cache.put(request, response.clone());
                                        } catch (error) {
                                                // CacheStorage may not be available or quota can be exceeded.
                                        }
                                }
                        }

                        return await response.blob();
                } finally {
                        this.release();
                }
        }

        private acquire(): Promise<void> {
                if (this.activeFetches < this.maxConcurrent) {
                        this.activeFetches++;
                        return Promise.resolve();
                }
                return new Promise((resolve) => {
                        this.queue.push(() => {
                                this.activeFetches++;
                                resolve();
                        });
                });
        }

        private release(): void {
                this.activeFetches = Math.max(0, this.activeFetches - 1);
                const next = this.queue.shift();
                if (next) {
                        next();
                }
        }
}

