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
	private readonly renderOffset: Vector2 = {x: 0, y: 0};
	private needsBackgroundRefresh = true;

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

	public advance(dtMs: number, viewportPosition?: Vector2, viewportTarget?: Vector2) {
		if (!marauroa.me) {
			return;
		}

		if (this.needsBackgroundRefresh) {
			this.zoneChange();
			this.createBackgroundImage();
			this.needsBackgroundRefresh = false;
		}

		const target = viewportTarget ? this.worldToMini(viewportTarget) : this.computePlayerTarget();
		const start = viewportPosition ? this.worldToMini(viewportPosition) : target;

		if (!this.cameraInitialized) {
			this.cameraSpring.snap(start);
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
		ctx.fillStyle = Color.DARK_GRAY;
		ctx.fillRect(0, 0, this.width, this.height);

		const interpolated = this.cameraSpring.getInterpolated(alpha);
		this.renderOffset.x = interpolated.x;
		this.renderOffset.y = interpolated.y;

		ctx.save();
		ctx.translate(-interpolated.x, -interpolated.y);
		this.drawBackground(ctx);
		this.drawEntities(ctx);
		ctx.restore();
	}

	private clampOffset(offset: Vector2): Vector2 {
		const maxX = Math.max(0, this.mapWidth * this.scale - this.width);
		const maxY = Math.max(0, this.mapHeight * this.scale - this.height);
		return {
			x: Math.min(Math.max(offset.x, 0), maxX),
			y: Math.min(Math.max(offset.y, 0), maxY)
		};
	}

	private worldToMini(position: Vector2): Vector2 {
		const tileWidth = stendhal.ui.gamewindow ? stendhal.ui.gamewindow.targetTileWidth : 32;
		const tileHeight = stendhal.ui.gamewindow ? stendhal.ui.gamewindow.targetTileHeight : 32;
		return this.clampOffset({
			x: (position.x / tileWidth) * this.scale,
			y: (position.y / tileHeight) * this.scale
		});
	}

	private computePlayerTarget(): Vector2 {
		const imageWidth = this.mapWidth * this.scale;
		const imageHeight = this.mapHeight * this.scale;
		const playerX = (typeof(marauroa.me["_x"]) === "number") ? marauroa.me["_x"] : marauroa.me["x"];
		const playerY = (typeof(marauroa.me["_y"]) === "number") ? marauroa.me["_y"] : marauroa.me["y"];

		let targetX = 0;
		let targetY = 0;

		if (imageWidth > this.width) {
			const rawX = Math.round((playerX * this.scale) + 0.5) - this.width / 2;
			if ((rawX + this.width) > imageWidth) {
				targetX = imageWidth - this.width;
			} else if (rawX > 0) {
				targetX = rawX;
			}
		}

		if (imageHeight > this.height) {
			const rawY = Math.round((playerY * this.scale) + 0.5) - this.height / 2;
			if ((rawY + this.height) > imageHeight) {
				targetY = imageHeight - this.height;
			} else if (rawY > 0) {
				targetY = rawY;
			}
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

	drawEntities(ctx: CanvasRenderingContext2D) {
		ctx.fillStyle = Color.RED;
		ctx.strokeStyle = Color.BLACK;
		let isAdmin = marauroa.me["adminlevel"] && marauroa.me["adminlevel"] >= 600;

		for (let i in marauroa.currentZone) {
			let o = marauroa.currentZone[i];
			if (typeof(o["x"]) != "undefined" && typeof(o["y"]) != "undefined" && (o.minimapShow || isAdmin)) {
				o.onMiniMapDraw();

				// this.ctx.fillText(o.id, o.x * this.scale, o.y * this.scale);
				if (o.minimapStyle) {
					ctx.strokeStyle = o.minimapStyle;
				} else {
					ctx.strokeStyle = Color.GRAY;
				}

				if (o instanceof Player) {
					let adj_scale = this.scale;
					if (adj_scale < 6) {
						// + is hard to see in wider views
						adj_scale = 6;
					}

					let ho = (o["width"] * adj_scale) / 2;
					let vo = (o["height"] * adj_scale) / 2;
					const hc = o["x"] * this.scale + ho;
					const vc = o["y"] * this.scale + vo;

					ctx.beginPath();
					ctx.moveTo(hc - ho, vc);
					ctx.lineTo(hc + ho, vc);
					ctx.moveTo(hc, vc - vo);
					ctx.lineTo(hc, vc + vo);
					ctx.stroke();
					ctx.closePath();
				} else {
					ctx.strokeRect(o["x"] * this.scale, o["y"] * this.scale, o["width"] * this.scale, o["height"] * this.scale);
				}
			}
		}
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
