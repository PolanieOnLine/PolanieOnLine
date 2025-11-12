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
 * Tileset image cache that keeps decoded sprites in memory and persists
 * responses through the CacheStorage API when it is available.  This avoids
 * repeated network round-trips for the same PNG files when players revisit
 * zones or when multiple combined tilesets request the same asset.
 */
export type CachedTilesetImage = {
        source: CanvasImageSource;
        width: number;
        height: number;
};

const CACHE_NAMESPACE = "stendhal-tilesets-v1";

export class TilesetImageCache {

        private static instance: TilesetImageCache | undefined;

        private readonly inflight = new Map<string, Promise<CachedTilesetImage>>();
        private readonly cachePromise: Promise<Cache | null>;

        private constructor() {
                if (typeof caches !== "undefined") {
                        this.cachePromise = caches.open(CACHE_NAMESPACE).catch(() => null);
                } else {
                        this.cachePromise = Promise.resolve(null);
                }
        }

        static get(): TilesetImageCache {
                if (!TilesetImageCache.instance) {
                        TilesetImageCache.instance = new TilesetImageCache();
                }
                return TilesetImageCache.instance;
        }

        /**
         * Warm the cache with a set of URLs.  The calls are intentionally fire
         * and forget – the returned promises are ignored so multiple callers
         * can safely prefetch without awaiting completion.
         */
        prefetch(urls: Iterable<string>): void {
                for (const url of urls) {
                        void this.load(url);
                }
        }

        load(url: string): Promise<CachedTilesetImage> {
                const absolute = this.toAbsoluteUrl(url);
                let promise = this.inflight.get(absolute);
                if (!promise) {
                        promise = this.fetchAndDecode(absolute);
                        this.inflight.set(absolute, promise);
                }
                return promise;
        }

        private toAbsoluteUrl(url: string): string {
                try {
                        return new URL(url, window.location.href).toString();
                } catch (e) {
                        return url;
                }
        }

        private async fetchAndDecode(url: string): Promise<CachedTilesetImage> {
                const request = new Request(url, { credentials: "same-origin" });
                const cache = await this.cachePromise;

                let response: Response | undefined = undefined;
                if (cache) {
                        response = await cache.match(request) ?? undefined;
                }

                if (!response) {
                        const requestInit: RequestInit = { cache: "force-cache" };
                        response = await fetch(request, requestInit).catch((error) => {
                                throw new Error(`Failed to fetch tileset ${url}: ${error}`);
                        });
                        if (!response.ok) {
                                throw new Error(`Failed to fetch tileset ${url}: ${response.status}`);
                        }
                        if (cache) {
                                try {
                                        await cache.put(request, response.clone());
                                } catch (error) {
                                        // Ignore CacheStorage failures (e.g. quota exceeded).
                                }
                        }
                }

                const blob = await response.blob();

                try {
                        if (typeof createImageBitmap === "function") {
                                const bitmap = await createImageBitmap(blob);
                                return { source: bitmap, width: bitmap.width, height: bitmap.height };
                        }
                } catch (error) {
                        // Fall back to HTMLImageElement decoding below.
                }

                const image = await this.decodeWithImageElement(blob);
                const width = image.naturalWidth || image.width;
                const height = image.naturalHeight || image.height;
                return { source: image, width, height };
        }

        private decodeWithImageElement(blob: Blob): Promise<HTMLImageElement> {
                return new Promise((resolve, reject) => {
                        const objectUrl = URL.createObjectURL(blob);
                        const img = new Image();
                        img.decoding = "async";
                        img.onload = () => {
                                URL.revokeObjectURL(objectUrl);
                                resolve(img);
                        };
                        img.onerror = (event) => {
                                URL.revokeObjectURL(objectUrl);
                                reject(event instanceof ErrorEvent ? event.error : event);
                        };
                        img.src = objectUrl;
                });
        }
}

