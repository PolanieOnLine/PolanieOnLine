/***************************************************************************
 *                (C) Copyright 2003-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare let marauroa: any;
declare let stendhal: any;

import { Component } from "../toolkit/Component";

import { Player } from "../../entity/Player";

import { Color } from "../../data/color/Color";
import { SpringVector, Vector2 } from "../../util/SpringVector";

type EntityState = {prevX: number; prevY: number; currX: number; currY: number};


/**
 * mini map
 */
export class MiniMapComponent extends Component {

	private width = 128;
	private height = 128;
	private minimumScale = 2;
	private xOffset = 1;
	private yOffset = 1;
	private mapWidth = 1;
	private mapHeight = 1;
	private scale = 1;
	private bgImage?: HTMLCanvasElement|OffscreenCanvas;
	private lastZone?: number[];

	private readonly cameraSpring = new SpringVector(600);
	private cameraTarget: Vector2 = {x: 0, y: 0};
	private cameraInitialized = false;
	private needsBackgroundRefresh = true;
	private entityStates = new WeakMap<any, EntityState>();

	// ground/collision colors
	private static readonly COLOR_COLLISION = Color.parseRGB(Color.COLLISION); // red
	private static readonly COLOR_GROUND = Color.parseRGB(Color.BACKGROUND); // light gray
	private static readonly COLOR_PROTECTION = Color.parseRGB(Color.PROTECTION); // green

	constructor() {
		super("minimap");
		this.componentElement.addEventListener("click", (event) => {
			this.onClick(event);
		});
		this.componentElement.addEventListener("dblclick", (event) => {
			this.onClick(event);
		});
	}

	private zoneChange() {
		this.mapWidth = stendhal.data.map.zoneSizeX;
		this.mapHeight = stendhal.data.map.zoneSizeY;
		const fitScale = Math.min(this.height / this.mapHeight, this.width / this.mapWidth);
		const normalizedFit = Number.isFinite(fitScale) ? fitScale : this.minimumScale;
		const quantizedFit = normalizedFit >= this.minimumScale ? Math.round(normalizedFit) : this.minimumScale;
		this.scale = Math.max(this.minimumScale, quantizedFit);
		this.cameraInitialized = false;
		this.needsBackgroundRefresh = true;
		this.entityStates = new WeakMap<any, EntityState>();
	};

	public draw() {
		if (marauroa.currentZoneName === stendhal.data.map.currentZoneName
			|| stendhal.data.map.currentZoneName === "int_vault"
			|| stendhal.data.map.currentZoneName === "int_adventure_island"
			|| stendhal.data.map.currentZoneName === "tutorial_island") {

			this.scale = 10;
			this.zoneChange();
			this.needsBackgroundRefresh = !this.createBackgroundImage();
		}
	}

	public advance(dtMs: number, _viewportPosition?: Vector2, _viewportTarget?: Vector2) {
		if (!marauroa.me) {
			return;
		}

		if (this.needsBackgroundRefresh) {
			this.zoneChange();
			this.needsBackgroundRefresh = !this.createBackgroundImage();
		}

		this.updateEntities();

		const target = this.computePlayerTarget();

		if (!this.cameraInitialized) {
			this.cameraSpring.snap(target);
			this.cameraInitialized = true;
		}

		this.cameraSpring.step(target, dtMs);
		const maxX = Math.max(0, this.mapWidth * this.scale - this.width);
		const maxY = Math.max(0, this.mapHeight * this.scale - this.height);
		this.cameraSpring.clamp(0, maxX, 0, maxY);

		const current = this.cameraSpring.getPosition();
		this.cameraTarget = target;
		this.xOffset = current.x;
		this.yOffset = current.y;
	}

	public renderFrame(alpha: number) {
		if (!marauroa.me) {
			return;
		}

		if (this.needsBackgroundRefresh) {
			this.needsBackgroundRefresh = !this.createBackgroundImage();
		}

		const ctx = (this.componentElement as HTMLCanvasElement).getContext("2d")!;

		ctx.setTransform(1, 0, 0, 1, 0, 0);
		ctx.imageSmoothingEnabled = false;
		ctx.fillStyle = Color.BLACK;
		ctx.fillRect(0, 0, this.width, this.height);

		const interpolated = this.cameraSpring.getInterpolated(alpha);
		const renderX = Math.round(interpolated.x);
		const renderY = Math.round(interpolated.y);
		this.xOffset = renderX;
		this.yOffset = renderY;
		ctx.save();
		ctx.translate(-renderX, -renderY);
		this.drawBackground(ctx);
		this.drawEntities(ctx, alpha);
		ctx.restore();
	}

	private computePlayerTarget(): Vector2 {
		const imageWidth = this.mapWidth * this.scale;
		const imageHeight = this.mapHeight * this.scale;
		const playerX = (typeof(marauroa.me["_x"]) === "number") ? marauroa.me["_x"] : marauroa.me["x"];
		const playerY = (typeof(marauroa.me["_y"]) === "number") ? marauroa.me["_y"] : marauroa.me["y"];
		const playerWidth = typeof(marauroa.me["width"]) === "number" ? marauroa.me["width"] : 1;
		const playerHeight = typeof(marauroa.me["height"]) === "number" ? marauroa.me["height"] : 1;

		const halfViewportWidth = this.width / 2;
		const halfViewportHeight = this.height / 2;

		let targetX = (playerX + playerWidth / 2) * this.scale - halfViewportWidth;
		let targetY = (playerY + playerHeight / 2) * this.scale - halfViewportHeight;

		const maxX = Math.max(0, imageWidth - this.width);
		const maxY = Math.max(0, imageHeight - this.height);

		if (!Number.isFinite(targetX)) {
			targetX = 0;
		}
		if (!Number.isFinite(targetY)) {
			targetY = 0;
		}

		if (targetX < 0) {
			targetX = 0;
		} else if (targetX > maxX) {
			targetX = maxX;
		}

		if (targetY < 0) {
			targetY = 0;
		} else if (targetY > maxY) {
			targetY = maxY;
		}

		return {x: targetX, y: targetY};
	}

	drawBackground(ctx: CanvasRenderingContext2D) {
		ctx.save();
		ctx.imageSmoothingEnabled = false;

		ctx.scale(this.scale, this.scale);
		if (this.bgImage) {
			ctx.drawImage(this.bgImage as CanvasImageSource, 0, 0);
		}
		ctx.restore();
	}

	createBackgroundImage(): boolean {
		const width = this.mapWidth;
		const height = this.mapHeight;
		if (width <= 0 || height <= 0) {
			return false;
		}

		if (stendhal.data.map.collisionData === this.lastZone && this.bgImage) {
			return true;
		}

		this.lastZone = stendhal.data.map.collisionData;
		let buffer: HTMLCanvasElement|OffscreenCanvas;
		if (typeof OffscreenCanvas !== "undefined") {
			buffer = new OffscreenCanvas(width, height);
		} else {
			buffer = document.createElement("canvas");
		}
		buffer.width = width;
		buffer.height = height;

		const ctx = buffer.getContext("2d")!;
		const imgData = ctx.createImageData(width, height);

		for (let y = 0; y < height; y++) {
			for (let x = 0; x < width; x++) {
				let color = MiniMapComponent.COLOR_GROUND;
				// RGBA array. Find the actual position
				const pos = 4 * (y * width + x);
				if (stendhal.data.map.collision(x, y)) {
					// red collision
					color = MiniMapComponent.COLOR_COLLISION;
				} else if (stendhal.data.map.isProtected(x, y)) {
					// light green for protection
					color = MiniMapComponent.COLOR_PROTECTION;
				}
				imgData.data[pos] = color.R;
				imgData.data[pos + 1] = color.G;
				imgData.data[pos + 2] = color.B;
				imgData.data[pos + 3] = 255; // opacity
			}
		}
		ctx.putImageData(imgData, 0, 0);
		this.bgImage = buffer;
		return true;
	}

	drawEntities(ctx: CanvasRenderingContext2D, alpha: number) {
		ctx.fillStyle = Color.RED;
		ctx.strokeStyle = Color.BLACK;
		ctx.lineWidth = 1;
		ctx.lineCap = "butt";
		ctx.lineJoin = "miter";
		ctx.imageSmoothingEnabled = false;
		let isAdmin = marauroa.me["adminlevel"] && marauroa.me["adminlevel"] >= 600;

		for (let i in marauroa.currentZone) {
			let o = marauroa.currentZone[i];
			if (this.hasRenderablePosition(o) && (o.minimapShow || isAdmin)) {
				o.onMiniMapDraw();

				// this.ctx.fillText(o.id, o.x * this.scale, o.y * this.scale);
				if (o.minimapStyle) {
					ctx.strokeStyle = o.minimapStyle;
				} else {
					ctx.strokeStyle = Color.GRAY;
				}

				const pos = this.getEntityInterpolatedPosition(o, alpha);
				const width = this.getEntityDimension(o, "width");
				const height = this.getEntityDimension(o, "height");
				const drawX = Math.round(pos.x * this.scale);
				const drawY = Math.round(pos.y * this.scale);
				const scaledWidth = Math.max(1, Math.round(width * this.scale));
				const scaledHeight = Math.max(1, Math.round(height * this.scale));

				if (o instanceof Player) {
					const centerX = Math.round((pos.x + width / 2) * this.scale);
					const centerY = Math.round((pos.y + height / 2) * this.scale);
					const armLength = Math.max(3, Math.round(Math.max(scaledWidth, scaledHeight) / 2));
					const crossSpan = armLength * 2 + 1;
					const originalFill: CanvasGradient|CanvasPattern|string = ctx.fillStyle;
					ctx.fillStyle = ctx.strokeStyle as typeof ctx.fillStyle;
					ctx.fillRect(centerX, centerY - armLength, 1, crossSpan);
					ctx.fillRect(centerX - armLength, centerY, crossSpan, 1);
					ctx.fillStyle = originalFill;
				} else {
					const rectX = drawX + 0.5;
					const rectY = drawY + 0.5;
					const rectWidth = Math.max(0, scaledWidth - 1);
					const rectHeight = Math.max(0, scaledHeight - 1);
					if (rectWidth === 0 && rectHeight === 0) {
						const originalFill: CanvasGradient|CanvasPattern|string = ctx.fillStyle;
						ctx.fillStyle = ctx.strokeStyle as typeof ctx.fillStyle;
						ctx.fillRect(drawX, drawY, 1, 1);
						ctx.fillStyle = originalFill;
					} else {
						ctx.strokeRect(rectX, rectY, rectWidth, rectHeight);
					}
				}
			}
		}
	}

	private updateEntities() {
		for (const key in marauroa.currentZone) {
			const obj = marauroa.currentZone[key];
			if (!this.hasRenderablePosition(obj)) {
				continue;
			}

			const pos = this.getEntityPosition(obj);
			let state = this.entityStates.get(obj);
			if (!state) {
				state = {prevX: pos.x, prevY: pos.y, currX: pos.x, currY: pos.y};
			} else {
				state.prevX = state.currX;
				state.prevY = state.currY;
				state.currX = pos.x;
				state.currY = pos.y;
			}
			this.entityStates.set(obj, state);
		}
	}

	private hasRenderablePosition(obj: any): boolean {
		if (!obj) {
			return false;
		}
		const hasX = typeof(obj["x"]) !== "undefined" || typeof(obj["_x"]) === "number";
		const hasY = typeof(obj["y"]) !== "undefined" || typeof(obj["_y"]) === "number";
		return hasX && hasY;
	}

	private getEntityPosition(obj: any): Vector2 {
		const x = (typeof(obj["_x"]) === "number") ? obj["_x"] : obj["x"];
		const y = (typeof(obj["_y"]) === "number") ? obj["_y"] : obj["y"];
		return {x: x || 0, y: y || 0};
	}

	private getEntityInterpolatedPosition(obj: any, alpha: number): Vector2 {
		const state = this.entityStates.get(obj);
		if (!state) {
			return this.getEntityPosition(obj);
		}

		const lerp = (a: number, b: number) => a + (b - a) * alpha;
		return {
			x: lerp(state.prevX, state.currX),
			y: lerp(state.prevY, state.currY)
		};
	}

	private getEntityDimension(obj: any, key: "width" | "height"): number {
		const value = typeof(obj[key]) === "number" ? obj[key] : 1;
		return value || 1;
	}

	onClick(event: MouseEvent) {
		if (!stendhal.config.getBoolean("pathfinding.minimap")) {
			return;
		}
		let pos = stendhal.ui.html.extractPosition(event);
		let x = Math.floor((pos.canvasRelativeX + this.xOffset) / this.scale);
		let y = Math.floor((pos.canvasRelativeY + this.yOffset) / this.scale);
		if (!stendhal.data.map.collision(x, y)) {
			let action: any = {
					type: "moveto",
					x: x.toString(),
					y: y.toString()
			};

			if ("type" in event && event["type"] === "dblclick") {
				action["double_click"] = "";
			}

			marauroa.me.moveTo(action);
		}
	}
}
