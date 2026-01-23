
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
import { Canvas, RenderingContext2D } from "util/Types";
import { Debug } from "../util/Debug";
import { UiStateStore } from "./mobile/UiStateStore";

/**
 * game window aka world view
 */
export class ViewPort {

	/** Horizontal screen offset in pixels. */
	private offsetX = 0;
	/** Vertical screen offset in pixels. */
	private offsetY = 0;
	/** Smoothed horizontal camera position in pixels. */
	private cameraX = this.offsetX;
	/** Smoothed vertical camera position in pixels. */
	private cameraY = this.offsetY;
	/** Prevents adjusting offset based on player position. */
	private freeze = false;
	/** Time of most recent frame, in milliseconds. */
	private lastFrameTime?: number;
	/** Most recent frame delta, clamped, in milliseconds. */
	private lastDeltaMs = 0;
	/** Maximal delta time passed to updates to avoid huge jumps. */
	private readonly maxDeltaMs = 250;
	/** Current device pixel ratio. */
	private devicePixelRatio = 1;
	/** Logical viewport width in CSS pixels. */
	private logicalWidth = 0;
	/** Logical viewport height in CSS pixels. */
	private logicalHeight = 0;
	/** Scale factor applied to tiles (mobile only). */
	private tileScale = 1;

	/**
	 * Current canvas buffer width.
	 */
	get width(): number {
		return this.ctx.canvas.width;
	}

	/**
	 * Current canvas buffer height.
	 */
	get height(): number {
		return this.ctx.canvas.height;
	}

	/**
	 * Provides logical input scale based on viewport size and device pixel ratio.
	 */
	public getInputScale(): { devicePixelRatio: number; rectWidth: number; rectHeight: number } {
		const canvas = this.getElement() as HTMLCanvasElement;
		const rect = canvas.getBoundingClientRect();
		return {
			devicePixelRatio: (this.devicePixelRatio || window.devicePixelRatio || 1) * this.tileScale,
			rectWidth: rect.width,
			rectHeight: rect.height,
		};
	}

	public getTileScale(): number {
		return this.tileScale;
	}

	public getWorldViewportSize(): { width: number; height: number } {
		const canvas = this.getElement() as HTMLCanvasElement;
		const devicePixelRatio = this.devicePixelRatio || window.devicePixelRatio || 1;
		const scale = devicePixelRatio * this.tileScale;
		return {
			width: canvas.width / scale,
			height: canvas.height / scale,
		};
	}

	/** Drawing context. */
	private ctx: RenderingContext2D;
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
	private readonly initialStyle: { [prop: string]: string };
	private readonly initialMaxHeight?: number;

	private preserveHeightForNextResize = false;
	private resizeScheduled = false;
	private lastCssHeight?: number;
	private lastViewportClientWidth?: number;
	private lastViewportClientHeight?: number;
	private heightNeedsMeasurement = true;
	private unsubscribeUiState?: () => void;

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

		const { styles, maxHeight } = this.resolveInitialStyles(element);
		this.initialStyle = styles;
		this.initialMaxHeight = maxHeight;

		this.subscribeToUiState();
		this.updateCanvasSize();
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

	/**
	 * Draws terrain tiles & entity sprites in the viewport.
	 */
	draw() {
		const resized = this.updateCanvasSize();
		if (resized) {
			this.lastFrameTime = undefined;
			this.lastDeltaMs = 0;
			requestAnimationFrame(() => {
				stendhal.ui.gamewindow.draw();
			});
			return;
		}

		const now = performance.now();
		if (this.lastFrameTime === undefined) {
			this.lastFrameTime = now;
		}
		const rawDelta = now - this.lastFrameTime;
		this.lastDeltaMs = Math.min(Math.max(rawDelta, 0), this.maxDeltaMs);
		this.lastFrameTime = now;

		if (marauroa.me && document.visibilityState === "visible") {
			if (marauroa.currentZoneName === stendhal.data.map.currentZoneName
				|| stendhal.data.map.currentZoneName === "int_vault"
				|| stendhal.data.map.currentZoneName === "int_adventure_island"
				|| stendhal.data.map.currentZoneName === "tutorial_island") {
				this.drawingError = false;

				this.ctx.globalAlpha = 1.0;
				this.clearViewportCanvas();
				this.adjustView(this.ctx.canvas);

				var tileOffsetX = Math.floor(this.offsetX / this.targetTileWidth);
				var tileOffsetY = Math.floor(this.offsetY / this.targetTileHeight);

				// FIXME: filter should not be applied to "blend" layers
				//this.applyFilter();
				stendhal.data.map.parallax.draw(this.ctx, this.offsetX, this.offsetY);
				stendhal.data.map.strategy.render(this.ctx.canvas, this, tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight);

				this.weatherRenderer.draw(this.ctx);
				//this.removeFilter();
				if (!Debug.isActive("light")) {
					this.applyHSLFilter();
				}
				this.drawEntitiesTop();
				(ui.get(UIComponentEnum.MiniMap) as MiniMapComponent).draw();
				this.drawEmojiSprites();
				this.drawTextSprites();
				this.drawTextSprites(this.notifSprites);

				// redraw inventory sprites
				stendhal.ui.equip.update();
				(ui.get(UIComponentEnum.PlayerEquipment) as PlayerEquipmentComponent).update();
			}
		}
		requestAnimationFrame(() => {
			stendhal.ui.gamewindow.draw();
		});
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
	 * Syncs canvas backing dimensions with its rendered size to avoid scaling artifacts.
	 */
	public updateCanvasSize(options?: { preserveHeight?: boolean }): boolean {
		const preserveHeight = options?.preserveHeight ?? this.preserveHeightForNextResize;
		if (options?.preserveHeight !== undefined) {
			this.preserveHeightForNextResize = false;
		}

		this.tileScale = this.resolveTileScale();
		this.applyResponsiveCanvasSize(preserveHeight);

		const canvas = this.getElement() as HTMLCanvasElement;
		const rect = canvas.getBoundingClientRect();
		if (rect.width === 0 || rect.height === 0) {
			return false;
		}
		this.devicePixelRatio = window.devicePixelRatio || 1;
		this.logicalWidth = rect.width;
		this.logicalHeight = rect.height;
		const targetWidth = Math.round(rect.width * this.devicePixelRatio);
		const targetHeight = Math.round(rect.height * this.devicePixelRatio);
		if (canvas.width === targetWidth && canvas.height === targetHeight) {
			return false;
		}
		canvas.width = targetWidth;
		canvas.height = targetHeight;
		this.ctx = canvas.getContext("2d")!;
		this.ctx.imageSmoothingEnabled = false;
		return true;
	}

	public scheduleResize(preserveHeight = false) {
		this.preserveHeightForNextResize = preserveHeight;
		if (!preserveHeight) {
			this.heightNeedsMeasurement = true;
		}
		if (this.resizeScheduled) {
			return;
		}
		this.resizeScheduled = true;
		requestAnimationFrame(() => {
			this.resizeScheduled = false;
			this.updateCanvasSize({ preserveHeight });
		});
	}

	private subscribeToUiState() {
		if (this.unsubscribeUiState) {
			return;
		}
		this.unsubscribeUiState = UiStateStore.get().subscribe(({ rightPanelExpanded }) => {
			if (rightPanelExpanded !== undefined) {
				this.logViewportSize(`right panel ${rightPanelExpanded ? "expanded" : "collapsed"}`, "before");
				requestAnimationFrame(() => {
					this.scheduleResize(true);
					this.logViewportSize(`right panel ${rightPanelExpanded ? "expanded" : "collapsed"}`, "during");
					requestAnimationFrame(() => this.logViewportSize(`right panel ${rightPanelExpanded ? "expanded" : "collapsed"}`, "after"));
				});
			}
		});
	}

	private applyResponsiveCanvasSize(preserveHeight: boolean) {
		const clientRoot = document.getElementById("client");
		const viewport = this.getViewportDimensions();
		const canvas = this.getElement() as HTMLCanvasElement;
		const mobileFloating = this.isMobileFloatingUi();
		if (!clientRoot || !mobileFloating) {
			this.captureCurrentHeight();
			this.lastViewportClientWidth = viewport.width;
			this.lastViewportClientHeight = viewport.height;
			return;
		}

		const middleColumn = document.getElementById("middleColumn");
		if (!middleColumn) {
			this.lastViewportClientWidth = viewport.width;
			this.lastViewportClientHeight = viewport.height;
			return;
		}

		this.applyResponsiveWidth(canvas, middleColumn, viewport.width);

		if (preserveHeight) {
			this.reapplyCachedHeight(canvas);
		} else if (this.shouldRecalculateHeight(preserveHeight, viewport)) {
			this.applyResponsiveHeight(canvas);
		} else {
			this.captureCurrentHeight();
		}

		this.lastViewportClientWidth = viewport.width;
		this.lastViewportClientHeight = viewport.height;
	}

	private resolveInitialStyles(element: HTMLElement): { styles: { [prop: string]: string }; maxHeight?: number } {
		const resolved: { [prop: string]: string } = {};
		const stylesheet = getComputedStyle(element);
		const defaults = this.getViewportStyleDefaults();

		const resolvedMaxWidth = this.resolveStyleValue(
			stylesheet,
			"max-width",
			"--viewport-max-width",
			defaults.maxWidth,
		);
		resolved["max-width"] = resolvedMaxWidth;

		const resolvedMaxHeight = this.resolveStyleValue(stylesheet, "max-height", "--viewport-max-height", defaults.maxHeight);
		resolved["max-height"] = resolvedMaxHeight;

		const numericMaxHeight = this.resolveInitialMaxHeight(stylesheet);
		return { styles: resolved, maxHeight: numericMaxHeight };
	}

	private resolveStyleValue(
		stylesheet: CSSStyleDeclaration,
		property: string,
		variable: string,
		defaultValue: string,
	): string {
		const propertyValue = this.normalizeCssValue(stylesheet.getPropertyValue(property));
		if (propertyValue) {
			return propertyValue;
		}
		const variableValue = this.normalizeCssValue(stylesheet.getPropertyValue(variable));
		if (variableValue) {
			return variableValue;
		}
		return defaultValue;
	}

	private resolveInitialMaxHeight(stylesheet: CSSStyleDeclaration): number | undefined {
		const computedMaxHeight = this.parseCssPixels(stylesheet.getPropertyValue("max-height"));
		if (computedMaxHeight > 0) {
			return computedMaxHeight;
		}
		const variableMaxHeight = this.parseCssPixels(stylesheet.getPropertyValue("--viewport-max-height"));
		if (variableMaxHeight > 0) {
			return variableMaxHeight;
		}
		const fallbackMaxHeight = this.getViewportMaxHeightFallback();
		if (fallbackMaxHeight > 0) {
			return fallbackMaxHeight;
		}
		return undefined;
	}

	private getViewportDimensions(): { width: number; height: number } {
		const width = Math.max(0, document.documentElement?.clientWidth || window.innerWidth || 0);
		const height = Math.max(0, document.documentElement?.clientHeight || window.innerHeight || 0);
		return { width, height };
	}

	private resolveTileScale(): number {
		if (!this.isMobileFloatingUi()) {
			return 1;
		}
		const rawValue = singletons.getConfigManager().get("viewport.tileScale.mobile");
		if (!rawValue) {
			return 1;
		}
		const parsed = parseFloat(rawValue);
		if (!Number.isFinite(parsed) || parsed <= 0) {
			return 1;
		}
		return parsed;
	}

	private applyResponsiveWidth(canvas: HTMLCanvasElement, middleColumn: HTMLElement, viewportWidth: number) {
		const middleRect = middleColumn.getBoundingClientRect();
		const middleStyles = getComputedStyle(middleColumn);
		const paddingX = this.parseCssPixels(middleStyles.paddingLeft) + this.parseCssPixels(middleStyles.paddingRight);
		const marginX = this.parseCssPixels(middleStyles.marginLeft) + this.parseCssPixels(middleStyles.marginRight);
		const contentBoxWidth = Math.max(0, middleRect.width - paddingX - marginX);
		const clampedWidth = Math.max(0, Math.min(contentBoxWidth, viewportWidth - marginX));
		if (clampedWidth > 0) {
			canvas.style.width = `${clampedWidth}px`;
		}
	}

	private shouldRecalculateHeight(preserveHeight: boolean, viewport: { width: number; height: number }): boolean {
		if (preserveHeight) {
			return false;
		}
		if (this.heightNeedsMeasurement) {
			return true;
		}
		return viewport.width !== this.lastViewportClientWidth || viewport.height !== this.lastViewportClientHeight;
	}

	private applyResponsiveHeight(canvas: HTMLCanvasElement) {
		const canvasStyles = getComputedStyle(canvas);
		const maxHeight = this.resolveMaxHeight(canvasStyles);
		const cssHeight = this.resolveCssHeight(canvasStyles, maxHeight);
		const measuredHeight = cssHeight ?? this.lastCssHeight ?? canvas.getBoundingClientRect().height;
		const targetHeight = this.clampHeight(measuredHeight, maxHeight);
		if (targetHeight > 0) {
			canvas.style.height = `${targetHeight}px`;
			this.lastCssHeight = targetHeight;
			this.heightNeedsMeasurement = false;
		}
	}

	private reapplyCachedHeight(canvas: HTMLCanvasElement) {
		if (!this.lastCssHeight) {
			this.captureCurrentHeight();
		}
		if (this.lastCssHeight && this.lastCssHeight > 0) {
			canvas.style.height = `${this.lastCssHeight}px`;
		}
	}

	private resolveCssHeight(styles: CSSStyleDeclaration, maxHeight: number): number | undefined {
		const cssHeight = this.parseCssPixels(styles.height);
		if (cssHeight > 0) {
			return this.clampHeight(cssHeight, maxHeight);
		}
		if (maxHeight > 0) {
			return maxHeight;
		}
		return undefined;
	}

	private resolveMaxHeight(styles: CSSStyleDeclaration): number {
		const maxHeight = this.parseCssPixels(styles.maxHeight);
		if (maxHeight > 0) {
			return maxHeight;
		}
		const viewportMaxHeight = this.parseCssPixels(styles.getPropertyValue("--viewport-max-height"));
		if (viewportMaxHeight > 0) {
			return viewportMaxHeight;
		}
		const fallbackMaxHeight = this.getViewportMaxHeightFallback();
		if (fallbackMaxHeight > 0) {
			return fallbackMaxHeight;
		}
		return Math.max(0, this.initialMaxHeight ?? 0);
	}

	private clampHeight(height: number | undefined, maxHeight: number): number {
		if (!height || height <= 0) {
			return 0;
		}
		if (maxHeight > 0) {
			return Math.min(height, maxHeight);
		}
		return height;
	}

	private captureCurrentHeight() {
		if (this.lastCssHeight) {
			return;
		}
		const height = this.getElement().getBoundingClientRect().height;
		if (height > 0) {
			this.lastCssHeight = height;
		}
	}

	private normalizeCssValue(value: string): string | undefined {
		const trimmed = value?.trim();
		if (!trimmed || trimmed === "none") {
			return undefined;
		}
		return trimmed;
	}

	private clearViewportCanvas() {
		this.ctx.setTransform(1, 0, 0, 1, 0, 0);
		this.ctx.imageSmoothingEnabled = false;
		this.ctx.fillStyle = "black";
		this.ctx.fillRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
	}

	private parseCssPixels(value: string): number {
		const parsed = parseFloat(value);
		return Number.isNaN(parsed) ? 0 : parsed;
	}

	private getViewportStyleDefaults(): { maxHeight: string; maxWidth: string } {
		if (this.isMobileFloatingUi()) {
			return {
				maxHeight: "calc(100dvh - 1em)",
				maxWidth: "min(100vw, calc((100dvh - 1em) * 640 / 480))",
			};
		}
		return {
			maxHeight: "calc(100dvh - 5em)",
			maxWidth: "calc((100dvh - 5em) * 640 / 480)",
		};
	}

	private getViewportMaxHeightFallback(): number {
		const viewport = this.getViewportDimensions();
		if (viewport.height <= 0) {
			return 0;
		}
		const fontSize = this.parseCssPixels(getComputedStyle(document.documentElement).fontSize);
		if (fontSize <= 0) {
			return Math.max(0, this.initialMaxHeight ?? 0);
		}
		const marginEm = this.isMobileFloatingUi() ? 1 : 5;
		const marginPx = marginEm * fontSize;
		if (marginPx <= 0) {
			return Math.max(0, this.initialMaxHeight ?? 0);
		}
		return Math.max(0, viewport.height - marginPx);
	}

	private isMobileFloatingUi(): boolean {
		const clientRoot = document.getElementById("client");
		return Boolean(
			clientRoot?.classList.contains("mobile-floating-ui")
				|| document.body?.classList.contains("mobile-floating-ui"),
		);
	}

	public logViewportSize(reason: string, phase: "before" | "after" | "during" = "during") {
		const canvas = this.getElement() as HTMLCanvasElement;
		const rect = canvas.getBoundingClientRect();
		if (!rect.width || !rect.height) {
			return;
		}
		console.info(
			`[viewport] ${reason} (${phase}) -> css ${Math.round(rect.width)}x${Math.round(rect.height)}, buffer ${canvas.width}x${canvas.height}`,
		);
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
		switch (method) {
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
	drawEntities(deltaMs: number = this.lastDeltaMs) {
		const time = Math.min(Math.max(deltaMs, 0), this.maxDeltaMs);
		for (var i in stendhal.zone.entities) {
			var entity = stendhal.zone.entities[i];
			if (typeof (entity.draw) != "undefined") {
				entity.updatePosition(time);
				entity.draw(this.ctx);
			}
		}
	}

	/**
	 * Draws titles & HP bars associated with entities.
	 */
	drawEntitiesTop() {
		var i;
		for (i in stendhal.zone.entities) {
			const entity = stendhal.zone.entities[i];
			if (typeof (entity.setStatusBarOffset) !== "undefined") {
				entity.setStatusBarOffset();
			}
			if (typeof (entity.drawTop) != "undefined") {
				entity.drawTop(this.ctx);
			}
		}
	}

	/**
	 * Draws active notifications or speech bubbles associated with characters, NPCs, & creatures.
	 *
	 * @param sgroup {sprite.TextBubble[]}
	 *   Sprite group to drawn, either speech bubbles or notifications/achievements (default: speech bubbles).
	 */
	drawTextSprites(sgroup: TextBubble[] = this.textSprites) {
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
	 * Updates viewport drawing position of map based on player position.
	 *
	 * @param canvas {Canvas}
	 *   Viewport canvas element.
	 */
	adjustView(canvas: Canvas) {
		const { targetX, targetY } = this.computeCameraTargets(canvas);

		if (this.freeze) {
			this.applyCameraPosition(targetX, targetY);
			return;
		}

		const smoothingFactor = Math.min(1, (this.lastDeltaMs / 1000) * 10);
		this.cameraX += (targetX - this.cameraX) * smoothingFactor;
		this.cameraY += (targetY - this.cameraY) * smoothingFactor;

		this.applyCameraPosition(this.cameraX, this.cameraY);
	}

	private computeCameraTargets(canvas: Canvas): { targetX: number; targetY: number } {
		const halfTileWidth = this.targetTileWidth / 2;
		const halfTileHeight = this.targetTileHeight / 2;
		const mapWidth = stendhal.data.map.zoneSizeX * this.targetTileWidth;
		const mapHeight = stendhal.data.map.zoneSizeY * this.targetTileHeight;
		const playerX = marauroa.me["_x"] * this.targetTileWidth + halfTileWidth;
		const playerY = marauroa.me["_y"] * this.targetTileHeight + halfTileHeight;

		const viewportWidth = (this.logicalWidth || canvas.width / this.devicePixelRatio) / this.tileScale;
		const viewportHeight = (this.logicalHeight || canvas.height / this.devicePixelRatio) / this.tileScale;
		const targetX = this.freeze ? this.offsetX : playerX - viewportWidth / 2;
		const targetY = this.freeze ? this.offsetY : playerY - viewportHeight / 2;

		const maxX = mapWidth - viewportWidth;
		const maxY = mapHeight - viewportHeight;
		const clampedX = maxX < 0 ? maxX / 2 : Math.min(Math.max(targetX, 0), maxX);
		const clampedY = maxY < 0 ? maxY / 2 : Math.min(Math.max(targetY, 0), maxY);

		return { targetX: clampedX, targetY: clampedY };
	}

	private applyCameraPosition(targetX: number, targetY: number) {
		this.cameraX = targetX;
		this.cameraY = targetY;
		this.offsetX = Math.round(this.cameraX);
		this.offsetY = Math.round(this.cameraY);
		const scaledPixelRatio = this.devicePixelRatio * this.tileScale;
		const translateX = Math.round(this.offsetX * scaledPixelRatio);
		const translateY = Math.round(this.offsetY * scaledPixelRatio);
		this.ctx.setTransform(
			scaledPixelRatio,
			0,
			0,
			scaledPixelRatio,
			-translateX,
			-translateY,
		);
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
		for (let idx = this.notifSprites.length - 1; idx >= 0; idx--) {
			const topSprite = this.notifSprites[idx];
			if (topSprite == sprite || topSprite.clipsPoint(x, y)) {
				this.notifSprites.splice(idx, 1);
				topSprite.onRemoved();
				return;
			}
		}

		for (let idx = this.textSprites.length - 1; idx >= 0; idx--) {
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
			for (let idx = sgroup.length - 1; idx >= 0; idx--) {
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

		mHandle._onMouseDown = function(e: MouseEvent | TouchEvent) {
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
			if (stendhal.ui.gamewindow.textBubbleAt(x, y + 15)) {
				return;
			}

			entity = stendhal.zone.entityAt(x, y);
			stendhal.ui.timestampMouseDown = +new Date();

			if (e.type !== "dblclick" && e.target) {
				e.target.addEventListener("mousemove", mHandle.onDrag, { passive: true });
				e.target.addEventListener("mouseup", mHandle.onMouseUp);
				e.target.addEventListener("touchmove", mHandle.onDrag, { passive: false });
				e.target.addEventListener("touchend", mHandle.onMouseUp, { passive: false });
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

		mHandle.onMouseUp = function(e: MouseEvent | TouchEvent) {
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
					stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Czynności",
						new ActionContextMenu(entity, append), pos.pageX - 50, pos.pageY - 5));
				}
			} else {
				const isDoubleTap = is_touch
					&& stendhal.ui.touch.registerTap(
						pos.pageX,
						pos.pageY,
						e.target,
						stendhal.ui.gamewindow.getElement()
					);
				if (isDoubleTap) {
					entity.onclick(pos.canvasRelativeX, pos.canvasRelativeY, true);
				} else {
					entity.onclick(pos.canvasRelativeX, pos.canvasRelativeY);
				}
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
			var xDiff = startX - pos.canvasRelativeX;
			var yDiff = startY - pos.canvasRelativeY;
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

			if (typeof (currentDir) === "number") {
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
				marauroa.clientFramework.sendAction({ "type": "face", "dir": "" + newDir });
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
			img = singletons.getSpriteStore().getAreaOf(singletons.getSpriteStore().get(draggedEntity.sprite.filename), 32, 32);
			heldObject = {
				path: draggedEntity.getIdPath(),
				zone: marauroa.currentZoneName,
				quantity: draggedEntity.hasOwnProperty("quantity") ? draggedEntity["quantity"] : 1
			}
		} else if (draggedEntity && draggedEntity.type === "corpse") {
			img = singletons.getSpriteStore().get(draggedEntity.sprite.filename);
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
				ui.createSingletonFloatingWindow("Ilość", new DropQuantitySelectorDialog(action, touch_held), pos.pageX - 50, pos.pageY - 25);
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
