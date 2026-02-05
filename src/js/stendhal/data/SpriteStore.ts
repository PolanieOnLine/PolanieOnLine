/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { stendhal } from "../stendhal";

import { Paths } from "./Paths";
import { ImageFilter } from "../sprite/image/ImageFilter";


export class SpriteImage extends Image {
	// number of times the image has been accessed after initial creation
	counter = 0;
	bitmap?: ImageBitmap;
	bitmapWidth?: number;
	bitmapHeight?: number;
}

type IconAtlasRequest = {
	id: string;
	filename: string;
	sourceX: number;
	sourceY: number;
	sourceWidth: number;
	sourceHeight: number;
};

type IconAtlas = {
	key: string;
	dataUrl?: string;
	positions: Map<string, { x: number; y: number }>;
	ready: boolean;
};

export class SpriteStore {

	private knownBrokenUrls: { [url: string]: boolean } = {};
	private images: { [filename: string]: SpriteImage } = {};
	private itemIconAtlas: IconAtlas | undefined;

	private knownShadows: { [key: string]: boolean } = {
		"24x32": true,
		"32x32": true,
		"32x48": true,
		"32x48_long": true,
		"48x64": true,
		"48x64_float": true,
		"64x48": true,
		"64x64": true,
		"64x85": true,
		"64x96": true,
		"76x64": true,
		"81x96": true,
		"96x96": true,
		"96x128": true,
		"128x96": true,
		"128x170": true,
		"144x128": true,
		"168x224": true,
		"192x192": true,
		"192x192_float": true,
		"192x256": true,
		"320x440": true,
		"ent": true
	};

	// alternatives for known images that may be considered violent or mature
	private knownSafeSprites: { [filename: string]: boolean } = {
		[Paths.sprites + "/monsters/huge_animal/thing"]: true,
		[Paths.sprites + "/monsters/mutant/imperial_mutant"]: true,
		[Paths.sprites + "/monsters/undead/bloody_zombie"]: true,
		[Paths.sprites + "/npc/deadmannpc"]: true
	};

	// TODO: move to animation.json
	animations: { [key: string]: any } = {
		idea: {
			"love": { delay: 100, offsetX: 24, offsetY: -8 }
		}
	};


	/**
	 * Hidden singleton constructor.
	 */
	protected constructor() {
		// do nothing
	}

	private supportsImageBitmap(): boolean {
		return typeof createImageBitmap === "function";
	}

	private createImage(filename?: string): SpriteImage {
		const temp = new Image() as SpriteImage;
		temp.counter = 0;
		temp.onerror = ((t: SpriteImage, store: SpriteStore) => {
			return () => {
				if (t.src && !store.knownBrokenUrls[t.src]) {
					console.log("Broken image path:", t.src, new Error());
					store.knownBrokenUrls[t.src] = true;
				}
				const failsafe = store.getFailsafeImage();
				if (failsafe.src && t.src !== failsafe.src) {
					t.src = failsafe.src;
				}
			};
		})(temp, this);
		if (filename) {
			temp.src = filename;
		}
		if (this.supportsImageBitmap()) {
			temp.addEventListener("load", () => this.ensureBitmap(temp), { once: true });
		}
		return temp;
	}

	private ensureBitmap(image: SpriteImage): Promise<void> {
		if (image.bitmap || !this.supportsImageBitmap()) {
			if (!image.bitmapWidth && image.complete) {
				image.bitmapWidth = image.width;
				image.bitmapHeight = image.height;
			}
			return Promise.resolve();
		}
		if (!image.complete) {
			return Promise.resolve();
		}
		return createImageBitmap(image).then((bmp) => {
			image.bitmap = bmp;
			image.bitmapWidth = bmp.width;
			image.bitmapHeight = bmp.height;
		}).catch(() => {
			if (image.complete) {
				image.bitmapWidth = image.width;
				image.bitmapHeight = image.height;
			}
		});
	}

	private getBestSource(image: SpriteImage): SpriteImage {
		if (image.bitmap) {
			if (!image.bitmapWidth) {
				image.bitmapWidth = image.bitmap.width;
				image.bitmapHeight = image.bitmap.height;
			}
			if (image.bitmapWidth) {
				image.width = image.bitmapWidth;
				image.height = image.bitmapHeight || image.height;
			}
		} else if (!image.bitmapWidth && image.complete) {
			image.bitmapWidth = image.width;
			image.bitmapHeight = image.height;
		}
		return image;
	}

	private getFailsafeImage(): SpriteImage {
		const filename = Paths.sprites + "/failsafe.png";
		let failsafe = this.images[filename];
		if (failsafe) {
			failsafe.counter++;
		} else {
			failsafe = this.createImage(filename);
			this.images[filename] = failsafe;
		}
		return failsafe;
	}

	get(filename: string): any {
		if (!filename) {
			return {};
		}
		if (filename.indexOf("undefined") > -1) {
			if (!this.knownBrokenUrls[filename]) {
				console.log("Broken image path: ", filename, new Error());
			}
			this.knownBrokenUrls[filename] = true;
			return {};
		}
		if (this.images[filename]) {
			this.images[filename].counter++;
			const cached = this.images[filename];
			if (cached.complete && !cached.bitmap) {
				this.ensureBitmap(cached);
			}
			return this.getBestSource(cached);
		}
		const temp = this.createImage(filename);
		this.images[filename] = temp;
		return this.getBestSource(temp);
	}

	getWithPromise(filename: string): any {
		return new Promise((resolve) => {
			if (typeof (this.images[filename]) != "undefined") {
				const cached = this.images[filename];
				cached.counter++;
				const resolver = () => this.ensureBitmap(cached)
					.then(() => resolve(this.getBestSource(cached)))
					.catch(() => resolve(this.getBestSource(cached)));
				if (cached.complete || cached.bitmap) {
					resolver();
				} else {
					cached.addEventListener("load", resolver, { once: true });
				}
				return;
			}

			const image = this.createImage(filename);
			this.images[filename] = image;
			image.onload = () => this.ensureBitmap(image)
				.then(() => resolve(this.getBestSource(image)))
				.catch(() => resolve(this.getBestSource(image)));
		});
	}

	/**
	 * Rotates an image.
	 *
	 * @param img
	 *   Image to be rotated.
	 * @param angle
	 *   Angle of rotation.
	 */
	private rotate(img: HTMLImageElement, angle: number) {
		const canvas = document.getElementById("drawing-stage")! as HTMLCanvasElement;
		const ctx = canvas.getContext("2d")!;
		// make sure working with blank canvas
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		canvas.width = img.width;
		canvas.height = img.height;

		ctx.translate(canvas.width / 2, canvas.height / 2);
		ctx.rotate(angle * Math.PI / 180);
		ctx.translate(-canvas.width / 2, -canvas.height / 2);
		ctx.drawImage(img, 0, 0);

		img.src = canvas.toDataURL("image/png");
	}

	/**
	 * Retrieves a rotated image.
	 *
	 * @param filename
	 *   Path to target image file.
	 * @param angle
	 *   Angle of rotation.
	 * @return
	 *   HTMLImageElement.
	 */
	getRotated(filename: string, angle: number): any {
		if (angle == 0) {
			return this.get(filename);
		}
		const id = filename + "-rot" + angle;
		if (this.images[id]) {
			return this.get(id);
		}
		// NOTE: cannot use HTMLImageElement.cloneNode here, must get base image then transfer
		//       `src` property when ready
		const img = this.createImage();
		img.onload = () => {
			img.onload = null;
			this.rotate(img, angle);
			this.ensureBitmap(img);
		}
		this.get(filename);
		const baseImg = this.images[filename];
		if (baseImg) {
			if (baseImg.complete) {
				img.src = baseImg.src;
			} else {
				baseImg.addEventListener("load", () => {
					img.src = baseImg.src;
				}, { once: true });
			}
		}
		this.images[id] = img;
		return this.getBestSource(img);
	}

	/**
	 * Adds an image to cache.
	 *
	 * @param {string} id
	 *   Cache identifier.
	 * @param {SpriteImage}
	 *   Image to be cached.
	 */
	cache(id: string, image: SpriteImage) {
		image.counter = 0;
		this.images[id] = image;
	}

	/**
	 * Used when we only want an image if it was previously cached.
	 *
	 * @param filename
	 *     Full file path.
	 * @return
	 *     HTMLImageElement or undefined.
	 */
	getCached(filename: string): any {
		return this.images[filename];
	}

	/**
	 * Retrieves the failsafe sprite.
	 *
	 * @return
	 *     HTMLImageElement with failsafe image data.
	 */
	getFailsafe(): HTMLImageElement {
		const failsafe = this.getFailsafeImage();
		return this.getBestSource(failsafe);
	}

	getItemIconAtlas(requests: IconAtlasRequest[], tileSize: number): { dataUrl: string; positions: Map<string, { x: number; y: number }> } | undefined {
		const uniqueRequests = this.getUniqueAtlasRequests(requests);
		if (uniqueRequests.length === 0) {
			return undefined;
		}
		const key = this.buildAtlasKey(uniqueRequests, tileSize);
		if (!this.itemIconAtlas || this.itemIconAtlas.key !== key) {
			this.itemIconAtlas = {
				key,
				positions: new Map(),
				ready: false
			};
			this.buildItemIconAtlas(this.itemIconAtlas, uniqueRequests, tileSize);
		}
		if (!this.itemIconAtlas.ready || !this.itemIconAtlas.dataUrl) {
			return undefined;
		}
		return {
			dataUrl: this.itemIconAtlas.dataUrl,
			positions: this.itemIconAtlas.positions
		};
	}

	/**
	 * Checks cached images for a valid filename.
	 *
	 * @param filename
	 *     Image filename to be checked.
	 * @return
	 *     Path to image or failsafe image file.
	 */
	checkPath(filename: string): string {
		this.get(filename);
		const cached = this.images[filename];
		return cached ? cached.src : "";
	}

	private getUniqueAtlasRequests(requests: IconAtlasRequest[]): IconAtlasRequest[] {
		const unique = new Map<string, IconAtlasRequest>();
		for (const request of requests) {
			unique.set(request.id, request);
		}
		return Array.from(unique.values()).sort((a, b) => a.id.localeCompare(b.id));
	}

	private buildAtlasKey(requests: IconAtlasRequest[], tileSize: number): string {
		return `${tileSize}:${requests.map((request) => request.id).join("|")}`;
	}

	private buildItemIconAtlas(atlas: IconAtlas, requests: IconAtlasRequest[], tileSize: number) {
		const key = atlas.key;
		const loadPromise = Promise.all(requests.map((request) => this.getWithPromise(request.filename)))
			.then((images) => {
				if (!this.itemIconAtlas || this.itemIconAtlas.key !== key) {
					return;
				}
				const count = requests.length;
				const columns = Math.ceil(Math.sqrt(count));
				const rows = Math.ceil(count / columns);
				const canvas = document.createElement("canvas") as HTMLCanvasElement;
				canvas.width = columns * tileSize;
				canvas.height = rows * tileSize;
				const ctx = canvas.getContext("2d")!;
				atlas.positions.clear();
				for (let i = 0; i < requests.length; i++) {
					const request = requests[i];
					const image = images[i] as SpriteImage;
					const source = image.bitmap ?? image;
					const x = (i % columns) * tileSize;
					const y = Math.floor(i / columns) * tileSize;
					ctx.drawImage(
						source,
						request.sourceX,
						request.sourceY,
						request.sourceWidth,
						request.sourceHeight,
						x,
						y,
						tileSize,
						tileSize
					);
					atlas.positions.set(request.id, { x, y });
				}
				atlas.dataUrl = canvas.toDataURL("image/png");
				atlas.ready = true;
			})
			.catch(() => {
				if (this.itemIconAtlas && this.itemIconAtlas.key === key) {
					this.itemIconAtlas.ready = false;
				}
			});
		void loadPromise;
	}

	/** deletes all objects that have not been accessed since this method was called last time */
	// TODO: call clean on map change
	clean() {
		for (var i in this.images) {
			console.log(typeof (this.images[i]));
			if (this.images[i] instanceof SpriteImage) {
				if (this.images[i].counter > 0) {
					this.images[i].counter--;
				} else {
					delete (this.images[i]);
				}
			}
		}
	}

	/**
	 * Get an image element whose image data is an area of a specified image.
	 * If the area matches the original image, the image itself is returned.
	 * Otherwise <em>a copy</em> of the image data is returned. This is meant
	 * to be used for obtaining the drag image for drag and drop.
	 *
	 * @param image original image
	 * @param width width of the area
	 * @param height height of the area
	 * @param {number=} offsetX optional. left x coordinate of the area
	 * @param {number=} offsetY optional. top y coordinate of the area
	 */
	getAreaOf(image: HTMLImageElement, width: number, height: number,
		offsetX?: number, offsetY?: number): any {
		try {
			offsetX = offsetX || 0;
			offsetY = offsetY || 0;
			if ((image.width === width) && (image.height === height)
				&& (offsetX === 0) && (offsetY === 0)) {
				return image;
			}
			var canvas = document.createElement("canvas") as HTMLCanvasElement;
			canvas.width = width;
			canvas.height = height;
			var ctx = canvas.getContext("2d")!;
			ctx.drawImage(image, offsetX, offsetY, width, height, 0, 0, width, height);
			// Firefox would be able to use the canvas directly as a drag image, but
			// Chrome does not. This should work in any standards compliant browser.
			// TODO: Check if that is still true
			var newImage = new Image();
			newImage.src = canvas.toDataURL("image/png");
			return newImage;
		} catch (err) {
			if (err instanceof DOMException) {
				return this.getFailsafe();
			} else {
				// don't ignore other errors
				throw err;
			}
		}

		return {};
	}

	/**
	 * @param {string} fileName
	 * @param {string} filter
	 * @param {number=} param
	 */
	getFiltered(fileName: string, filter: string, param?: number) {
		const img = this.get(fileName);
		if (!img.complete || img.width === 0 || img.height === 0) {
			return img;
		}
		const filteredName = fileName + " " + filter + " " + param;
		let filtered: SpriteImage = this.images[filteredName];
		if (typeof (filtered) === "undefined") {
			const canvas = document.createElement("canvas") as any;
			canvas.width = img.width;
			canvas.height = img.height;
			const ctx = canvas.getContext("2d")!;
			ctx.drawImage(img, 0, 0);
			const imgData = ctx.getImageData(0, 0, img.width, img.height);
			new ImageFilter().filter(imgData, filter, param);
			ctx.putImageData(imgData, 0, 0);
			canvas.complete = true;
			this.images[filteredName] = filtered = canvas as SpriteImage;
		}

		return filtered;
	}

	/**
	 * @param {string} fileName
	 * @param {string} filter
	 * @param {number=} param
	 */
	getFilteredWithPromise(fileName: string, filter: string, param?: number) {
		const imgPromise = this.getWithPromise(fileName);
		return imgPromise.then(function(img: HTMLImageElement) {
			if (!img.complete || img.width === 0 || img.height === 0) {
				return img;
			}
			const filteredName = fileName + " " + filter + " " + param;
			let filtered: SpriteImage = stendhal.data.sprites.images[filteredName];
			if (typeof (filtered) === "undefined") {
				const canvas = document.createElement("canvas") as any;
				canvas.width = img.width;
				canvas.height = img.height;
				const ctx = canvas.getContext("2d")!;
				ctx.drawImage(img, 0, 0);
				const imgData = ctx.getImageData(0, 0, img.width, img.height);
				new ImageFilter().filter(imgData, filter, param);
				ctx.putImageData(imgData, 0, 0);
				canvas.complete = true;
				stendhal.data.sprites.images[filteredName] = filtered = canvas as SpriteImage;
			}

			return filtered;
		});
	}

	/**
	 * Retrieves a shadow sprite if the style is available.
	 *
	 * @param shadowStyle
	 *     Style of shadow to get from cache.
	 * @return
	 *     Image sprite or <code>undefined</code>.
	 */
	getShadow(shadowStyle: string): any {
		if (this.knownShadows[shadowStyle]) {
			const filename = Paths.sprites + "/shadow/" + shadowStyle + ".png";
			return this.get(filename);
		}
		return undefined;
	}

	/**
	 * Checks if there is a "safe" image available for sprite.
	 *
	 * @param filename
	 *     The sprite image base file path.
	 * @return
	 *     <code>true</code> if a known safe image is available.
	 */
	hasSafeImage(filename: string): boolean {
		return this.knownSafeSprites[filename] == true;
	}

	/**
	 * Called at startup to pre-cache certain images.
	 */
	startupCache() {
		// failsafe image
		this.getFailsafe();
		// tutorial profile
		this.get(Paths.sprites + "/npc/floattingladynpc.png");
		// achievement assets
		this.get(Paths.gui + "/banner_background.png");
		for (const cat of ["commerce", "deathmatch", "experience", "fighting", "friend",
			"interior_zone", "item", "obtain", "outside_zone", "production", "quest",
			"quest_ados_items", "quest_kill_blordroughs", "quest_kirdneh_item",
			"quest_mithrilbourgh_enemy_army", "quest_semos_monster", "special",
			"underground_zone"]) {
			this.get(Paths.achievements + "/" + cat + ".png");
		}
		// weather
		for (const weather of ["clouds", "fog", "fog_heavy", "rain", "rain_heavy",
			"rain_light", "snow", "snow_heavy", "snow_light", "wave"]) {
			this.get(Paths.weather + "/" + weather + ".png");
		}
	}
}

/**
 * Hidden class to create the singleton instance internally.
 */
class SpriteStoreInternal extends SpriteStore {

	/** Singleton instance. */
	private static instance: SpriteStore;

	/**
	 * Retrieves singleton instance.
	 */
	static get(): SpriteStore {
		if (!SpriteStoreInternal.instance) {
			SpriteStoreInternal.instance = new SpriteStore();
		}
		return SpriteStoreInternal.instance;
	}
}

// SpriteStore singleton instance
export const store = SpriteStoreInternal.get();
