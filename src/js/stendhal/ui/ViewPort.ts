/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var marauroa: any;
declare var stendhal: any;

import { HeldObject } from "./HeldObject";
import { ui } from "./UI";
import { UIComponentEnum } from "./UIComponentEnum";

import { PlayerEquipmentComponent } from "./component/PlayerEquipmentComponent";
import { MiniMapComponent } from "./component/MiniMapComponent";

import { ActionContextMenu } from "./dialog/ActionContextMenu";
import { DropQuantitySelectorDialog } from "./dialog/DropQuantitySelectorDialog";

import { Client } from "../Client";
import { singletons } from "../SingletonRepo";

import { AchievementBanner } from "../sprite/AchievementBanner";
import { EmojiSprite } from "../sprite/EmojiSprite";
import { NotificationBubble } from "../sprite/NotificationBubble";
import { SpeechBubble } from "../sprite/SpeechBubble";
import { TextBubble } from "../sprite/TextBubble";

import { Point } from "../util/Point";
import { GameLoop } from "../util/GameLoop";
import { SpringVector, Vector2 } from "../util/SpringVector";


/**
 * game window aka world view
 */
export class ViewPort {

	/** Horizontal screen offset in pixels. */
	private offsetX = 0;
	/** Vertical screen offset in pixels. */
	private offsetY = 0;
        /** Prevents adjusting offset based on player position. */
        private freeze = false;

        private loop?: GameLoop;
        private readonly cameraSpring = new SpringVector();
        private cameraTarget: Vector2 = {x: 0, y: 0};
        private cameraInitialized = false;
        private readonly renderOffset: Vector2 = {x: 0, y: 0};
        private readonly lastWorldSize: Vector2 = {x: 0, y: 0};
        private readonly entityStates = new WeakMap<any, {prevX: number; prevY: number; currX: number; currY: number}>();
        private fpsLabel?: HTMLElement;

	// dimensions
	// TODO: remove & use CSS style instead
	private readonly width: number;
	private readonly height: number;

	/** Drawing context. */
	private ctx: CanvasRenderingContext2D;
	/** Map tile pixel width. */
	private readonly targetTileWidth = 32;
	/** Map tile pixel height. */
	private readonly targetTileHeight = 32;
	private drawingError = false;

	/** Active speech bubbles to draw. */
	private textSprites: SpeechBubble[] = [];
	/** Active notification bubbles/achievement banners to draw. */
	private notifSprites: TextBubble[] = [];
	/** Active emoji sprites to draw. */
	private emojiSprites: EmojiSprite[] = [];
	/** Handles drawing weather in viewport. */
	private weatherRenderer = singletons.getWeatherRenderer();
	/** Coloring method of current zone. */
	private filter?: string; // deprecated, use `HSLFilter`
	/** Coloring filter of current zone. */
	private HSLFilter?: string;
	private colorMethod = "";
	private blendMethod = ""; // FIXME: unused

	/** Styles to be applied when chat panel is not floating. */
	private readonly initialStyle: {[prop: string]: string};

	/** Singleton instance. */
	private static instance: ViewPort;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): ViewPort {
		if (!ViewPort.instance) {
			ViewPort.instance = new ViewPort();
		}
		return ViewPort.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		const element = this.getElement() as HTMLCanvasElement;
		this.ctx = element.getContext("2d")!;
		this.width = element.width;
		this.height = element.height;

		this.initialStyle = {};
		//~ const stylesheet = getComputedStyle(element);
		// FIXME: how to get literal "calc()" instead of value of calc()?
		//~ this.initialStyle["max-width"] = stylesheet.getPropertyValue("max-width");
		//~ this.initialStyle["max-height"] = stylesheet.getPropertyValue("max-height");
		// NOTE: this doesn't work if properties set in css
                this.initialStyle["max-width"] = "calc((100dvh - 5em) * 640 / 480)";
                this.initialStyle["max-height"] = "calc(100dvh - 5em)";

                this.fpsLabel = this.createFpsLabel(element);
        }

	/**
	 * Retrieves the viewport element.
	 *
	 * @return {HTMLElement}
	 *   Viewport `HTMLElement`.
	 */
        public getElement(): HTMLElement {
                return document.getElementById("viewport")!;
        }

        private createFpsLabel(canvas: HTMLCanvasElement): HTMLElement {
                const label = document.createElement("div");
                label.className = "fps-counter";
                label.style.position = "absolute";
                label.style.top = "0";
                label.style.left = "0";
                label.style.padding = "2px 4px";
                label.style.background = "rgba(0, 0, 0, 0.35)";
                label.style.color = "#fff";
                label.style.fontFamily = "monospace";
                label.style.fontSize = "10px";
                label.style.pointerEvents = "none";
                label.style.userSelect = "none";
                label.textContent = "-- fps";

                const parent = canvas.parentElement;
                if (parent) {
                        if (!parent.style.position) {
                                parent.style.position = "relative";
                        }
                        parent.appendChild(label);
                }

                return label;
        }

        /**
         * Starts the render loop if it is not already active.
         */
        draw() {
                if (!this.loop) {
                        const prefer144 = stendhal.config.getBoolean("loop.prefer144hz");
                        const configuredLimit = stendhal.config.getFloat("loop.fps.limit");
                        const fpsLimit = this.normalizeFpsLimit(configuredLimit);
                        this.loop = new GameLoop(
                                (dt) => this.update(dt),
                                (alpha) => this.render(alpha),
                                {
                                        prefer144hz: prefer144,
                                        fpsLimit,
                                        onFpsSample: (fps) => this.updateFpsCounter(fps)
                                }
                        );
                }
                this.loop.start();
        }

        private update(dtMs: number) {
                if (!this.shouldRenderFrame()) {
                        return;
                }

                this.drawingError = false;
                this.updateCamera(dtMs);
                this.updateEntities(dtMs);
                this.advanceMiniMap(dtMs);
        }

        private render(alpha: number) {
                if (!this.shouldRenderFrame()) {
                        return;
                }

                this.ctx.globalAlpha = 1.0;
                this.ctx.setTransform(1, 0, 0, 1, 0, 0);
                this.ctx.imageSmoothingEnabled = false;

                const interpolated = this.cameraSpring.getInterpolated(alpha);
                this.renderOffset.x = interpolated.x;
                this.renderOffset.y = interpolated.y;

                const snappedX = this.snapToDevicePixel(interpolated.x);
                const snappedY = this.snapToDevicePixel(interpolated.y);

                this.offsetX = snappedX;
                this.offsetY = snappedY;

                this.ctx.fillStyle = "black";
                this.ctx.fillRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);

                this.ctx.translate(-snappedX, -snappedY);

                const tileOffsetX = Math.floor(snappedX / this.targetTileWidth);
                const tileOffsetY = Math.floor(snappedY / this.targetTileHeight);

                stendhal.data.map.parallax.draw(this.ctx, this.offsetX, this.offsetY);
                stendhal.data.map.strategy.render(
                        this.ctx.canvas,
                        this,
                        tileOffsetX,
                        tileOffsetY,
                        this.targetTileWidth,
                        this.targetTileHeight,
                        alpha
                );

                this.weatherRenderer.draw(this.ctx);
                this.applyHSLFilter();
                this.drawEntitiesTop(alpha);
                this.drawEmojiSprites();
                this.drawTextSprites();
                this.drawTextSprites(this.notifSprites);

                stendhal.ui.equip.update();
                (ui.get(UIComponentEnum.PlayerEquipment) as PlayerEquipmentComponent).update();

                this.renderMiniMap(alpha);
        }

        private shouldRenderFrame(): boolean {
                if (!marauroa.me || document.visibilityState !== "visible") {
                        return false;
                }
                if (marauroa.currentZoneName !== stendhal.data.map.currentZoneName
                                && stendhal.data.map.currentZoneName !== "int_vault"
                                && stendhal.data.map.currentZoneName !== "int_adventure_island"
                                && stendhal.data.map.currentZoneName !== "tutorial_island") {
                        return false;
                }
                return true;
        }

        private updateCamera(dtMs: number) {
                const canvas = this.ctx.canvas;
                const target = this.computeCameraTarget(canvas);

                const worldWidth = stendhal.data.map.zoneSizeX * this.targetTileWidth;
                const worldHeight = stendhal.data.map.zoneSizeY * this.targetTileHeight;

                if (this.lastWorldSize.x !== worldWidth || this.lastWorldSize.y !== worldHeight) {
                        this.lastWorldSize.x = worldWidth;
                        this.lastWorldSize.y = worldHeight;
                        this.cameraInitialized = false;
                }

                if (!this.cameraInitialized) {
                        this.cameraSpring.snap(target);
                        this.cameraInitialized = true;
                } else {
                        this.cameraSpring.step(target, dtMs);
                }

                const maxX = Math.max(0, worldWidth - canvas.width);
                const maxY = Math.max(0, worldHeight - canvas.height);
                this.cameraSpring.clamp(0, maxX, 0, maxY);
                this.cameraTarget = target;
        }

        private computeCameraTarget(canvas: HTMLCanvasElement): Vector2 {
                if (this.freeze) {
                        return this.cameraSpring.getPosition();
                }

                const playerX = (typeof(marauroa.me["_x"]) === "number") ? marauroa.me["_x"] : marauroa.me["x"];
                const playerY = (typeof(marauroa.me["_y"]) === "number") ? marauroa.me["_y"] : marauroa.me["y"];

                let centerX = playerX * this.targetTileWidth + this.targetTileWidth / 2 - canvas.width / 2;
                let centerY = playerY * this.targetTileHeight + this.targetTileHeight / 2 - canvas.height / 2;

                const worldWidth = stendhal.data.map.zoneSizeX * this.targetTileWidth;
                const worldHeight = stendhal.data.map.zoneSizeY * this.targetTileHeight;

                centerX = Math.min(centerX, worldWidth - canvas.width);
                centerX = Math.max(centerX, 0);

                centerY = Math.min(centerY, worldHeight - canvas.height);
                centerY = Math.max(centerY, 0);

                return {x: centerX, y: centerY};
        }

        private updateEntities(dtMs: number) {
                for (const key in stendhal.zone.entities) {
                        const entity = stendhal.zone.entities[key];
                        if (!entity) {
                                continue;
                        }

                        let state = this.entityStates.get(entity);
                        if (!state) {
                                const pos = this.getEntityPosition(entity);
                                state = {prevX: pos.x, prevY: pos.y, currX: pos.x, currY: pos.y};
                        }

                        state.prevX = state.currX;
                        state.prevY = state.currY;

                        entity.updatePosition(dtMs);

                        const currentPos = this.getEntityPosition(entity);
                        state.currX = currentPos.x;
                        state.currY = currentPos.y;
                        this.entityStates.set(entity, state);
                }
        }

        private getEntityPosition(entity: any): Vector2 {
                const x = (typeof(entity["_x"]) === "number") ? entity["_x"] : entity["x"];
                const y = (typeof(entity["_y"]) === "number") ? entity["_y"] : entity["y"];
                return {x: x || 0, y: y || 0};
        }

        private advanceMiniMap(dtMs: number) {
                const minimap = ui.get(UIComponentEnum.MiniMap) as MiniMapComponent | undefined;
                if (minimap && typeof(minimap.advance) === "function") {
                        minimap.advance(dtMs, this.cameraSpring.getPosition(), this.cameraTarget);
                }
        }

        private renderMiniMap(alpha: number) {
                const minimap = ui.get(UIComponentEnum.MiniMap) as MiniMapComponent | undefined;
                if (minimap && typeof(minimap.renderFrame) === "function") {
                        minimap.renderFrame(alpha);
                }
        }

        private updateFpsCounter(fps: number) {
                if (this.fpsLabel) {
                        this.fpsLabel.textContent = fps.toFixed(0) + " fps";
                }
        }

        private snapToDevicePixel(value: number): number {
                const ratio = window.devicePixelRatio || 1;
                return Math.round(value * ratio) / ratio;
        }

        public setFpsLimit(limit?: number) {
                const normalized = this.normalizeFpsLimit(limit);
                if (this.loop) {
                        this.loop.setFpsLimit(normalized);
                }
        }

        private normalizeFpsLimit(limit?: number): number|undefined {
                if (typeof(limit) === "number" && Number.isFinite(limit) && limit > 0) {
                        return limit;
                }
                return undefined;
        }

        /**
         * Adds map's coloring filter to viewport.
         *
         * FIXME:
	 * - colors are wrong
	 * - doesn't support "blend" layers
	 * - very slow
	 *
	 * @deprecated
	 */
	applyFilter() {
		if (this.filter && stendhal.config.getBoolean("effect.lighting")) {
			this.ctx.filter = this.filter;
		}
	}

	/**
	 * Removes map's coloring filter from viewport.
	 *
	 * @deprecated
	 */
	removeFilter() {
		this.ctx.filter = "none";
	}

	/**
	 * Add coloring filter to viewport.
	 */
	private applyHSLFilter() {
		if (!this.HSLFilter) {
			return;
		}
		this.ctx.save();
		// FIXME: is this the appropriate alpha level to use? "color_method" value from server doesn't
		//        appear to include alpha information
		this.ctx.globalAlpha = 0.75;
		this.ctx.globalCompositeOperation = (this.colorMethod || this.ctx.globalCompositeOperation) as GlobalCompositeOperation;
		this.ctx.fillStyle = this.HSLFilter;
		this.ctx.fillRect(this.offsetX, this.offsetY, this.ctx.canvas.width, this.ctx.canvas.height);
		this.ctx.restore();
	}

	/**
	 * Sets color method used for current zone.
	 *
	 * @param method {string}
	 *   Color method.
	 */
	setColorMethod(method: string) {
		switch(method) {
			case "softlight":
				method = "soft-light";
				break;
		}
		const known = [
			"source-over", "source-in", "source-out", "source-atop",
			"destination-over", "destination-in", "destination-out", "destination-atop",
			"lighter",
			"copy",
			"xor",
			"multiply",
			"screen",
			"overlay",
			"darken",
			"lighten",
			"color-dodge",
			"color-burn",
			"hard-light",
			"soft-light",
			"difference",
			"exclusion",
			"hue",
			"saturation",
			"color",
			"luminosity"
		];
		if (known.indexOf(method) < 0) {
			console.warn("Unknown color method:", method);
			return;
		}
		this.colorMethod = method;
	}

	/**
	 * Sets blend method used for current zone.
	 *
	 * @param method {string}
	 *   Blend method.
	 */
	setBlendMethod(method: string) {
		this.blendMethod = method;
	}

        /**
         * Draws overall entity sprites.
         */
        drawEntities(alpha: number) {
                for (const key in stendhal.zone.entities) {
                        const entity = stendhal.zone.entities[key];
                        if (!entity || typeof(entity.draw) === "undefined") {
                                continue;
                        }
                        this.drawEntityInterpolated(entity, alpha, () => entity.draw(this.ctx));
                }
        }

        /**
         * Draws titles & HP bars associated with entities.
         */
        drawEntitiesTop(alpha: number) {
                for (const key in stendhal.zone.entities) {
                        const entity = stendhal.zone.entities[key];
                        if (!entity) {
                                continue;
                        }
                        this.drawEntityInterpolated(entity, alpha, () => {
                                if (typeof(entity.setStatusBarOffset) !== "undefined") {
                                        entity.setStatusBarOffset();
                                }
                                if (typeof(entity.drawTop) !== "undefined") {
                                        entity.drawTop(this.ctx);
                                }
                        });
                }
        }

        private drawEntityInterpolated(entity: any, alpha: number, drawFn: () => void) {
                const state = this.entityStates.get(entity);
                if (!state) {
                        drawFn();
                        return;
                }

                const renderX = state.prevX + (state.currX - state.prevX) * alpha;
                const renderY = state.prevY + (state.currY - state.prevY) * alpha;

                const hadX = typeof(entity["_x"]) !== "undefined";
                const hadY = typeof(entity["_y"]) !== "undefined";
                const originalX = entity["_x"]; // may be undefined
                const originalY = entity["_y"];

                entity["_x"] = renderX;
                entity["_y"] = renderY;
                try {
                        drawFn();
                } finally {
                        if (hadX) {
                                entity["_x"] = originalX;
                        } else {
                                delete entity["_x"];
                        }
                        if (hadY) {
                                entity["_y"] = originalY;
                        } else {
                                delete entity["_y"];
                        }
                }
        }

	/**
	 * Draws active notifications or speech bubbles associated with characters, NPCs, & creatures.
	 *
	 * @param sgroup {sprite.TextBubble[]}
	 *   Sprite group to drawn, either speech bubbles or notifications/achievements (default: speech bubbles).
	 */
	drawTextSprites(sgroup: TextBubble[]=this.textSprites) {
		for (var i = 0; i < sgroup.length; i++) {
			var sprite = sgroup[i];
			var remove = sprite.draw(this.ctx);
			if (remove) {
				sgroup.splice(i, 1);
				sprite.onRemoved();
				i--;
			}
		}
	}

	/**
	 * Adds an emoji sprite to viewport.
	 *
	 * @param sprite {sprite.EmojiSprite}
	 *   Sprite definition.
	 */
	addEmojiSprite(sprite: EmojiSprite) {
		this.emojiSprites.push(sprite);
	}

	/**
	 * Draws active emoji sprites.
	 */
	drawEmojiSprites() {
		for (let i = 0; i < this.emojiSprites.length; i++) {
			const sprite = this.emojiSprites[i];
			const remove = sprite.draw(this.ctx);
			if (remove) {
				this.emojiSprites.splice(i, 1);
				i--;
			}
		}
	}

	/**
	 * Adds a speech bubble to viewport.
	 *
	 * @param sprite {sprite.SpeechBubble}
	 *   Sprite definition.
	 */
	addTextSprite(sprite: SpeechBubble) {
		this.textSprites.push(sprite);
		sprite.onAdded(this.ctx);
	}

	/**
	 * Adds a notification bubble to viewport.
	 *
	 * @param mtype {string}
	 *   Message type.
	 * @param text {string}
	 *   Text contents.
	 * @param profile {string}
	 *   Optional entity image filename to show as the speaker.
	 */
	addNotifSprite(mtype: string, text: string, profile?: string) {
		const bubble = new NotificationBubble(mtype, text, profile);
		this.notifSprites.push(bubble);
		bubble.onAdded(this.ctx);
	}

	/**
	 * Adds an achievement banner to viewport.
	 *
	 * @param cat {string}
	 *   Achievement categroy.
	 * @param title {string}
	 *   Achievement title.
	 * @param desc {string}
	 *   Achievement description.
	 */
	addAchievementNotif(cat: string, title: string, desc: string) {
		const banner = new AchievementBanner(cat, title, desc);
		this.notifSprites.push(banner);
		banner.onAdded(this.ctx);
	}

	/**
	 * Removes a speech bubble. Looks for topmost sprite at "x","y". Otherwise removes "sprite".
	 *
	 * @param sprite {sprite.TextBubble}
	 *   Sprite that is to be removed.
	 * @param x {number}
	 *   X coordinate to check for overlapping sprite.
	 * @param y {number}
	 *   Y coordinate to check for overlapping sprite.
	 */
	removeTextBubble(sprite: TextBubble, x: number, y: number) {
		for (let idx = this.notifSprites.length-1; idx >= 0; idx--) {
			const topSprite = this.notifSprites[idx];
			if (topSprite == sprite || topSprite.clipsPoint(x, y)) {
				this.notifSprites.splice(idx, 1);
				topSprite.onRemoved();
				return;
			}
		}

		for (let idx = this.textSprites.length-1; idx >= 0; idx--) {
			const topSprite = this.textSprites[idx];
			if (topSprite == sprite || topSprite.clipsPoint(x, y)) {
				this.textSprites.splice(idx, 1);
				topSprite.onRemoved();
				return;
			}
		}
	}

	/**
	 * Checks for an active speech bubble.
	 *
	 * @param x {number}
	 *   X coordinate to check.
	 * @param y {number}
	 *   Y coordinate to check.
	 * @return {boolean}
	 *   `true` if there is a text bubble at position.
	 */
	textBubbleAt(x: number, y: number): boolean {
		for (const sprite of this.notifSprites) {
			if (sprite.clipsPoint(x, y)) {
				return true;
			}
		}
		for (const sprite of this.textSprites) {
			if (sprite.clipsPoint(x, y)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called when `entity.User` instance exits a zone.
	 */
	onExitZone() {
		// clear speech bubbles & emojis so they don't appear on the new map
		for (const sgroup of [this.textSprites, this.emojiSprites]) {
			for (let idx = sgroup.length-1; idx >= 0; idx--) {
				const sprite = sgroup[idx];
				sgroup.splice(idx, 1);
				if (sprite instanceof SpeechBubble) {
					sprite.onRemoved();
				}
			}
		}
	}

	/**
	 *  Mouse click handling.
	 */
	onMouseDown = (function() {
		var entity: any;
		var startX: number;
		var startY: number;

		const mHandle: any = {};

		mHandle._onMouseDown = function(e: MouseEvent|TouchEvent) {
			var pos = stendhal.ui.html.extractPosition(e);
			if (stendhal.ui.touch.isTouchEvent(e)) {
				if (stendhal.ui.touch.holding()) {
					// prevent default viewport action when item is "held"
					return;
				}
				stendhal.ui.touch.onTouchStart(pos.pageX, pos.pageY);
			}
			if (stendhal.ui.globalpopup) {
				stendhal.ui.globalpopup.close();
			}

			startX = pos.canvasRelativeX;
			startY = pos.canvasRelativeY;

			var x = pos.canvasRelativeX + stendhal.ui.gamewindow.offsetX;
			var y = pos.canvasRelativeY + stendhal.ui.gamewindow.offsetY;

			// override ground/entity action if there is a text bubble
			if (stendhal.ui.gamewindow.textBubbleAt(x, y+15)) {
				return;
			}

			entity = stendhal.zone.entityAt(x, y);
			stendhal.ui.timestampMouseDown = +new Date();

			if (e.type !== "dblclick" && e.target) {
				e.target.addEventListener("mousemove", mHandle.onDrag);
				e.target.addEventListener("mouseup", mHandle.onMouseUp);
				e.target.addEventListener("touchmove", mHandle.onDrag);
				e.target.addEventListener("touchend", mHandle.onMouseUp);
			} else if (entity == stendhal.zone.ground) {
				entity.onclick(pos.canvasRelativeX, pos.canvasRelativeY, true);
			}
		}

		mHandle.isRightClick = function(e: MouseEvent) {
			if (+new Date() - stendhal.ui.timestampMouseDown > 300) {
				return true;
			}
			if (e.which) {
				return (e.which === 3);
			} else {
				return (e.button === 2);
			}
		}

		mHandle.onMouseUp = function(e: MouseEvent|TouchEvent) {
			const is_touch = stendhal.ui.touch.isTouchEvent(e);
			if (is_touch) {
				stendhal.ui.touch.onTouchEnd(e);
			}
			var pos = stendhal.ui.html.extractPosition(e);
			const long_touch = is_touch && stendhal.ui.touch.isLongTouch(e);
			if ((e instanceof MouseEvent && mHandle.isRightClick(e)) || long_touch) {
				if (entity != stendhal.zone.ground) {
					const append: any[] = [];
					/*
					if (long_touch) {
						// TODO: add option for "hold" to allow splitting item stacks
					}
					*/
					stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Action",
						new ActionContextMenu(entity, append), pos.pageX - 50, pos.pageY - 5));
				}
			} else {
				entity.onclick(pos.canvasRelativeX, pos.canvasRelativeY);
			}
			mHandle.cleanUp(pos);
			pos.target.focus();
			e.preventDefault();
		}

		mHandle.onDrag = function(e: MouseEvent) {
			if (stendhal.ui.touch.isTouchEvent(e)) {
				stendhal.ui.gamewindow.onDragStart(e);
			}

			var pos = stendhal.ui.html.extractPosition(e);
			var xDiff = startX - pos.offsetX;
			var yDiff = startY - pos.offsetY;
			// It's not really a click if the mouse has moved too much.
			if (xDiff * xDiff + yDiff * yDiff > 5) {
				mHandle.cleanUp(e);
			}
		}

		mHandle.cleanUp = function(e: Event) {
			entity = null;
			if (!e.target) {
				return;
			}
			e.target.removeEventListener("mouseup", mHandle.onMouseUp);
			e.target.removeEventListener("mousemove", mHandle.onDrag);
			e.target.removeEventListener("touchend", mHandle.onMouseUp);
			e.target.removeEventListener("touchmove", mHandle.onDrag);
		}

		return mHandle._onMouseDown;
	})()

	/**
	 * Updates cursor style when positioned over an element or entity.
	 */
	onMouseMove(e: MouseEvent) {
		var pos = stendhal.ui.html.extractPosition(e);
		var x = pos.canvasRelativeX + stendhal.ui.gamewindow.offsetX;
		var y = pos.canvasRelativeY + stendhal.ui.gamewindow.offsetY;
		var entity = stendhal.zone.entityAt(x, y);
		stendhal.ui.gamewindow.getElement().style.cursor = entity.getCursor(x, y);
	}

	/**
	 * Changes character facing direction dependent on direction of wheel scroll.
	 */
	onMouseWheel(e: WheelEvent) {
		if (marauroa.me) {
			e.preventDefault();

			// previous event may have changed type to string
			const currentDir = parseInt(marauroa.me["dir"], 10);
			let newDir = null;

			if (typeof(currentDir) === "number") {
				if (e.deltaY >= 100) {
					// clockwise
					newDir = currentDir + 1;
					if (newDir > 4) {
						newDir = 1;
					}
				} else if (e.deltaY <= -100) {
					// counter-clockwise
					newDir = currentDir - 1;
					if (newDir < 1) {
						newDir = 4;
					}
				}
			}

			if (newDir != null) {
				marauroa.clientFramework.sendAction({"type": "face", "dir": ""+newDir});
			}
		}
	}

	/**
	 * Handles engaging an item or corpse to be dragged.
	 */
	onDragStart(e: DragEvent) {
		var pos = stendhal.ui.html.extractPosition(e);
		let draggedEntity;
		for (const obj of stendhal.zone.getEntitiesAt(pos.canvasRelativeX + stendhal.ui.gamewindow.offsetX,
				pos.canvasRelativeY + stendhal.ui.gamewindow.offsetY)) {
			if (obj.isDraggable()) {
				draggedEntity = obj;
			}
		}

		var img = undefined;
		let heldObject: HeldObject;
		if (draggedEntity && draggedEntity.type === "item") {
			img = stendhal.data.sprites.getAreaOf(stendhal.data.sprites.get(draggedEntity.sprite.filename), 32, 32);
			heldObject = {
				path: draggedEntity.getIdPath(),
				zone: marauroa.currentZoneName,
				quantity: draggedEntity.hasOwnProperty("quantity") ? draggedEntity["quantity"] : 1
			}
		} else if (draggedEntity && draggedEntity.type === "corpse") {
			img = stendhal.data.sprites.get(draggedEntity.sprite.filename);
			heldObject = {
				path: draggedEntity.getIdPath(),
				zone: marauroa.currentZoneName,
				quantity: 1
			}
		} else {
			e.preventDefault();
			return;
		}

		if (stendhal.ui.touch.isTouchEvent(e)) {
			singletons.getHeldObjectManager().set(heldObject, img, new Point(pos.pageX, pos.pageY));
			stendhal.ui.touch.setHolding(true);
		} else {
			stendhal.ui.heldObject = heldObject;
		}

		if (e.dataTransfer) {
			window.event = e; // required by setDragImage polyfil
			e.dataTransfer.setDragImage(img, 0, 0);
		}
	}

	/**
	 * Displays a corpse or item sprite while dragging.
	 */
	onDragOver(e: DragEvent): boolean {
		e.preventDefault(); // Necessary. Allows us to drop.
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = "move";
		}
		return false;
	}

	/**
	 * Handles releasing an item or corpse from drag event.
	 */
	onDrop(e: DragEvent) {
		if (stendhal.ui.heldObject) {
			var pos = stendhal.ui.html.extractPosition(e);
			const targetSlot = stendhal.ui.html.parseSlotName((pos.target as HTMLElement).id);
			const action: any = {
				"zone": stendhal.ui.heldObject.zone
			};
			if (targetSlot === "viewport") {
				action.x = Math.floor((pos.canvasRelativeX + stendhal.ui.gamewindow.offsetX) / 32).toString();
				action.y = Math.floor((pos.canvasRelativeY + stendhal.ui.gamewindow.offsetY) / 32).toString();

				var id = stendhal.ui.heldObject.path.substr(1, stendhal.ui.heldObject.path.length - 2);
				var drop = /\t/.test(id);
				if (drop) {
					action["type"] = "drop";
					action["source_path"] = stendhal.ui.heldObject.path;
				} else {
					action["type"] = "displace";
					action["baseitem"] = id;
				}
			} else {
				let objectId = marauroa.me["id"];
				if (e.type === "touchend" && targetSlot === "content") {
					// find the actual target ID for touch events
					const container = stendhal.ui.equip.getByElement(stendhal.ui.html.extractTarget(event).parentElement!);
					if (container && container.object) {
						objectId = container.object.id;
					}
				}
				action["type"] = "equip";
				action["source_path"] = stendhal.ui.heldObject.path;
				action["target_path"] = "[" + objectId + "\t" + targetSlot + "]";
			}

			const quantity = stendhal.ui.heldObject.quantity;
			const sourceSlot = stendhal.ui.heldObject.slot || "viewport";
			// item was dropped
			stendhal.ui.heldObject = undefined;

			const touch_held = stendhal.ui.touch.holding() && quantity > 1;
			// if ctrl is pressed or holding stackable item from touch event, we ask for the quantity
			// NOTE: don't create selector if touch source is ground
			if (e.ctrlKey || (touch_held && sourceSlot !== targetSlot)) {
				ui.createSingletonFloatingWindow("Quantity", new DropQuantitySelectorDialog(action, touch_held), pos.pageX - 50, pos.pageY - 25);
			} else {
				singletons.getHeldObjectManager().onRelease();
				marauroa.clientFramework.sendAction(action);
			}
		}

		e.stopPropagation();
		e.preventDefault();
	}

	/**
	 * This is a workaround until it's figured out how to make it work using the same methods as mouse event.
	 */
	onTouchEnd(e: TouchEvent) {
		stendhal.ui.touch.onTouchEnd();
		stendhal.ui.gamewindow.onDrop(e);
		if (stendhal.ui.touch.holding()) {
			stendhal.ui.touch.setHolding(false);
			stendhal.ui.touch.unsetOrigin();
		}
		// execute here because "touchend" event propagation is cancelled on the veiwport
		Client.handleClickIndicator(e);
	}

	onContentMenu(e: MouseEvent) {
		e.preventDefault();
	}

	/**
	 * Updates viewport layout to compensate for chat panel style.
	 */
	public onChatPanelRefresh(floating: boolean) {
		const element = this.getElement();
		for (const prop of Object.keys(this.initialStyle)) {
			if (floating) {
				element.style.removeProperty(prop);
			} else {
				element.style.setProperty(prop, this.initialStyle[prop]);
			}
		}
	}
}
