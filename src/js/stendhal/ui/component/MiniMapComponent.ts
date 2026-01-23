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
import { Canvas, RenderingContext2D } from "util/Types";


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
	private bgImage!: Canvas;
	private lastZone?: number[];

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
		this.componentElement.addEventListener("touchstart", (event) => {
			this.onTouchStart(event);
		}, { passive: false });
		this.componentElement.addEventListener("touchend", (event) => {
			this.onTouchEnd(event);
		}, { passive: false });
	}


	private zoneChange() {
		this.mapWidth = stendhal.data.map.zoneSizeX;
		this.mapHeight = stendhal.data.map.zoneSizeY;
		this.scale = Math.max(this.minimumScale, Math.min(this.height / this.mapHeight, this.width / this.mapWidth));
		this.createBackgroundImage();
	};

	private updateBasePosition() {
		if (!marauroa.me) {
			return;
		}

		const baseX = marauroa.me["_x"] ?? marauroa.me["x"];
		const baseY = marauroa.me["_y"] ?? marauroa.me["y"];

		this.xOffset = 0;
		this.yOffset = 0;

		let imageWidth = this.mapWidth * this.scale;
		let imageHeight = this.mapHeight * this.scale;

		let xpos = Math.round((baseX * this.scale) + 0.5) - this.width / 2;
		let ypos = Math.round((baseY * this.scale) + 0.5) - this.width / 2;

		if (imageWidth > this.width) {
			// need to pan width
			if ((xpos + this.width) > imageWidth) {
				// x is at the screen border
				this.xOffset = imageWidth - this.width;
			} else if (xpos > 0) {
				this.xOffset = xpos;
			}
		}

		if (imageHeight > this.height) {
			// need to pan height
			if ((ypos + this.height) > imageHeight) {
				// y is at the screen border
				this.yOffset = imageHeight - this.height;
			} else if (ypos > 0) {
				this.yOffset = ypos;
			}
		}
	}

	public draw() {
		if (marauroa.currentZoneName === stendhal.data.map.currentZoneName
			|| stendhal.data.map.currentZoneName === "int_vault"
			|| stendhal.data.map.currentZoneName === "int_adventure_island"
			|| stendhal.data.map.currentZoneName === "int_adventure_cave"
			|| stendhal.data.map.currentZoneName === "tutorial_island") {

			this.scale = 10;

			this.zoneChange();
			this.updateBasePosition();
			let ctx = (this.componentElement as HTMLCanvasElement).getContext("2d")!;

			// IE does not support ctx.resetTransform(), so use the following workaround:
			ctx.setTransform(1, 0, 0, 1, 0, 0);

			// The area outside of the map
			ctx.fillStyle = Color.DARK_GRAY;
			ctx.fillRect(0, 0, this.width, this.height);

			ctx.translate(Math.round(-this.xOffset), Math.round(-this.yOffset));
			this.drawBackground(ctx);
			this.drawEntities(ctx);
		}
	}

	drawBackground(ctx: RenderingContext2D) {
		ctx.save();
		ctx.imageSmoothingEnabled = false;

		ctx.scale(this.scale, this.scale);
		if (this.bgImage) {
			ctx.drawImage(this.bgImage, 0, 0);
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
			this.bgImage = document.createElement("canvas");
			let ctx = this.bgImage.getContext("2d")!;
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
			this.bgImage.width = width;
			this.bgImage.height = height;

			ctx.putImageData(imgData, 0, 0);
		}
	}

	drawEntities(ctx: RenderingContext2D) {
		ctx.fillStyle = Color.RED;
		ctx.strokeStyle = Color.BLACK;
		let isAdmin = marauroa.me["adminlevel"] && marauroa.me["adminlevel"] >= 600;

		for (let i in marauroa.currentZone) {
			let o = marauroa.currentZone[i];
			if (typeof (o["x"]) != "undefined" && typeof (o["y"]) != "undefined" && (o.minimapShow || isAdmin)) {
				o.onMiniMapDraw();
				const px = (o["_x"] ?? o["x"]);
				const py = (o["_y"] ?? o["y"]);
				const baseX = Math.floor(px * this.scale);
				const baseY = Math.floor(py * this.scale);

				// this.ctx.fillText(o.id, o.x * this.scale, o.y * this.scale);
				ctx.fillStyle = o.minimapStyle ?? Color.GRAY;

				if (o instanceof Player) {
					ctx.strokeStyle = ctx.fillStyle;
					let adj_scale = this.scale;
					if (adj_scale < 6) {
						// + is hard to see in wider views
						adj_scale = 6;
					}

					const ho = Math.floor((o["width"] * adj_scale) / 2);
					const vo = Math.floor((o["height"] * adj_scale) / 2);
					const hc = baseX + Math.floor(this.scale / 2);
					const vc = baseY + Math.floor(this.scale / 2);

					ctx.beginPath();
					ctx.moveTo(hc - ho, vc);
					ctx.lineTo(hc + ho, vc);
					ctx.moveTo(hc, vc - vo);
					ctx.lineTo(hc, vc + vo);
					ctx.stroke();
					ctx.closePath();
				} else {
					const rectWidth = Math.round(o["width"] * this.scale);
					const rectHeight = Math.round(o["height"] * this.scale);
					ctx.fillRect(baseX, baseY, rectWidth, rectHeight);
				}
			}
		}
	}

	onClick(event: MouseEvent) {
		if (!stendhal.config.getBoolean("pathfinding.minimap")) {
			return;
		}
		let pos = stendhal.ui.html.extractPosition(event);
		// canvasRelativeX/Y are CSS pixels; offsets and scale are in the same logical units.
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

	private onTouchStart(event: TouchEvent) {
		event.preventDefault();
	}

	private onTouchEnd(event: TouchEvent) {
		if (!stendhal.config.getBoolean("pathfinding.minimap")) {
			return;
		}
		event.preventDefault();
		const pos = stendhal.ui.html.extractPosition(event);
		const x = Math.floor((pos.canvasRelativeX + this.xOffset) / this.scale);
		const y = Math.floor((pos.canvasRelativeY + this.yOffset) / this.scale);
		if (!stendhal.data.map.collision(x, y)) {
			const action: any = {
				type: "moveto",
				x: x.toString(),
				y: y.toString()
			};

			const isDoubleTap = stendhal.ui.touch.registerTap(
				pos.pageX,
				pos.pageY,
				event.target,
				this.componentElement
			);
			if (isDoubleTap) {
				action["double_click"] = "";
			}

			marauroa.me.moveTo(action);
		}
	}

}
