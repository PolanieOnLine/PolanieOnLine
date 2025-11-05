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
	private bgImage!: HTMLCanvasElement|OffscreenCanvas;
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
		this.scale = Math.max(this.minimumScale, Math.min(this.height / this.mapHeight, this.width / this.mapWidth));
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
			this.createBackgroundImage();
			this.needsBackgroundRefresh = false;
		}
	}

	public advance(dtMs: number, _viewportPosition?: Vector2, _viewportTarget?: Vector2) {
		if (!marauroa.me) {
			return;
		}

		if (this.needsBackgroundRefresh) {
			this.zoneChange();
			this.createBackgroundImage();
			this.needsBackgroundRefresh = false;
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
			this.createBackgroundImage();
			this.needsBackgroundRefresh = false;
		}

		const ctx = (this.componentElement as HTMLCanvasElement).getContext("2d")!;

		ctx.setTransform(1, 0, 0, 1, 0, 0);
		ctx.imageSmoothingEnabled = false;
		ctx.fillStyle = Color.DARK_GRAY;
		ctx.fillRect(0, 0, this.width, this.height);

		const interpolated = this.cameraSpring.getInterpolated(alpha);
		ctx.save();
		ctx.translate(-interpolated.x, -interpolated.y);
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

	createBackgroundImage() {
		let width = this.mapWidth;
		let height = this.mapHeight;
		if (width <= 0 || height <= 0) {
			return;
		}

		if (stendhal.data.map.collisionData !== this.lastZone) {
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
			let imgData = ctx.createImageData(width, height);

			for (let y = 0; y < height; y++) {
				for (let x = 0; x < width; x++) {
					let color = MiniMapComponent.COLOR_GROUND;
					// RGBA array. Find the actual position
					let pos = 4 * (y * width + x);
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
			this.bgImage = buffer;
			ctx.putImageData(imgData, 0, 0);
		}
	}

	drawEntities(ctx: CanvasRenderingContext2D, alpha: number) {
		ctx.fillStyle = Color.RED;
		ctx.strokeStyle = Color.BLACK;
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

				if (o instanceof Player) {
					let adj_scale = this.scale;
					if (adj_scale < 6) {
						// + is hard to see in wider views
						adj_scale = 6;
					}

					let ho = (width * adj_scale) / 2;
					let vo = (height * adj_scale) / 2;
					const hc = pos.x * this.scale + ho;
					const vc = pos.y * this.scale + vo;

					ctx.beginPath();
					ctx.moveTo(hc - ho, vc);
					ctx.lineTo(hc + ho, vc);
					ctx.moveTo(hc, vc - vo);
					ctx.lineTo(hc, vc + vo);
					ctx.stroke();
					ctx.closePath();
				} else {
					ctx.strokeRect(pos.x * this.scale, pos.y * this.scale, width * this.scale, height * this.scale);
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
