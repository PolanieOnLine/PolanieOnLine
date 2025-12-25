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
import { Entity } from "../entity/Entity";
import { RPEntity } from "../entity/RPEntity";

import { Point } from "../util/Point";
import { GameLoop } from "../util/GameLoop";
import { SpringVector, Vector2 } from "../util/SpringVector";
import { TilemapRenderer } from "./render/TilemapRenderer";


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
	private requestedFpsLimit?: number;
	private readonly cameraSpring = new SpringVector();
	private cameraTarget: Vector2 = { x: 0, y: 0 };
	private cameraInitialized = false;
	private readonly renderOffset: Vector2 = { x: 0, y: 0 };
	private readonly lastWorldSize: Vector2 = { x: 0, y: 0 };
	private entityRefs: Entity[] = [];
	private entityCount = 0;
	private entityPrevPositions = new Float32Array(0);
	private entityCurrPositions = new Float32Array(0);
	private entityLastPositions = new Float32Array(0);
	private entityLastCount = 0;
	private entityIndexLookup: WeakMap<Entity, number> = new WeakMap();
	private fpsLabel?: HTMLElement;

	/** Drawing context. */
	private ctx: CanvasRenderingContext2D;
	/** Map tile pixel width. */
	private readonly targetTileWidth = 32;
	/** Map tile pixel height. */
	private readonly targetTileHeight = 32;
	private drawingError = false;

	/** Active speech bubbles to draw. */
	private textSprites: (SpeechBubble | null)[] = [];
	private textSpriteFree: number[] = [];
	private speechBubblePool: SpeechBubble[] = [];
	private readonly speechScratch: SpeechBubble[] = [];
	/** Active notification bubbles/achievement banners to draw. */
	private notifSprites: (TextBubble | null)[] = [];
	private notifSpriteFree: number[] = [];
	private notifBubblePool: NotificationBubble[] = [];
	private achievementPool: AchievementBanner[] = [];
	/** Active emoji sprites to draw. */
	private emojiSprites: EmojiSprite[] = [];
	/** Handles drawing weather in viewport. */
	private readonly weatherRenderer = singletons.getWeatherRenderer();
	/** Batched tilemap renderer. */
	private readonly tileRenderer = new TilemapRenderer();
	/** Coloring method of current zone. */
	private filter?: string; // deprecated, use `HSLFilter`
	/** Coloring filter of current zone. */
	private HSLFilter?: string;
	private colorMethod = "";
	private blendMethod = ""; // FIXME: unused

	/** Styles to be applied when chat panel is not floating. */
	private readonly initialStyle: { [prop: string]: string };
	private readonly baseRenderWidth: number;
	private readonly baseRenderHeight: number;
	private readonly fallbackAspectRatio: number;
	/**
	 * Default resolutions used to derive targetRatio:
	 * - desktop: wide (16:9) or classic (4:3)
	 * - mobile: compact 4:3
	 * Adjust these values to change the default rendering resolution.
	 */
	private readonly targetDesktopResolution: Vector2;
	private readonly targetDesktopFallbackResolution: Vector2;
	private readonly targetMobileResolution: Vector2;
	private readonly desktopBreakpointPx = 900;
	private readonly povScaleFactor = 0.9;
	private readonly minCanvasWidth: number;
	private readonly minCanvasHeight: number;
	private parentResizeObserver?: ResizeObserver;
	private readonly handleWindowResize: () => void;

	/** Singleton instance. */
	private static instance: ViewPort;
	private static readonly FPS_LIMITS = [60, 90, 120, 144];

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
		this.baseRenderWidth = element.width || 800;
		this.baseRenderHeight = element.height || 600;
		this.fallbackAspectRatio = this.baseRenderWidth && this.baseRenderHeight
			? this.baseRenderWidth / this.baseRenderHeight
			: (4 / 3);
		this.targetDesktopResolution = { x: 1280, y: 720 };
		this.targetDesktopFallbackResolution = { x: 1024, y: 768 };
		this.targetMobileResolution = { x: 844, y: 633 };

		this.initialStyle = {};
		const styles = getComputedStyle(element);
		this.captureInitialStyles(element, styles);
		const minSize = this.computeMinimumCanvasSize(styles);
		this.minCanvasWidth = minSize.x;
		this.minCanvasHeight = minSize.y;
		this.handleWindowResize = () => this.updateCanvasBounds();
		this.observeParent(element);
		this.updateCanvasBounds();
		window.addEventListener("resize", this.handleWindowResize, { passive: true });

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

	private captureInitialStyles(element: HTMLElement, styles: CSSStyleDeclaration) {
		this.assignInitialStyleFrom(styles.getPropertyValue("--viewport-max-width"), "max-width");
		this.assignInitialStyleFrom(styles.getPropertyValue("--viewport-max-height"), "max-height");
		this.assignInitialStyleFrom(element.style.getPropertyValue("max-width"), "max-width");
		this.assignInitialStyleFrom(element.style.getPropertyValue("max-height"), "max-height");
		if (!this.initialStyle["max-width"]) {
			this.initialStyle["max-width"] = "calc((100dvh - 5em) * 800 / 600)";
		}
		if (!this.initialStyle["max-height"]) {
			this.initialStyle["max-height"] = "calc(100dvh - 5em)";
		}
	}

	private computeMinimumCanvasSize(styles: CSSStyleDeclaration): Vector2 {
		let minWidth = Math.max(1, Math.round(this.baseRenderWidth));
		let minHeight = Math.max(1, Math.round(this.baseRenderHeight));
		const minWidthSetting = this.parseCssLength(styles.getPropertyValue("--viewport-min-render-width"));
		const minHeightSetting = this.parseCssLength(styles.getPropertyValue("--viewport-min-render-height"));
		if (minWidthSetting && minWidthSetting > 0) {
			minWidth = Math.max(minWidth, Math.round(minWidthSetting));
		}
		if (minHeightSetting && minHeightSetting > 0) {
			minHeight = Math.max(minHeight, Math.round(minHeightSetting));
		}
		const aspect = this.getTargetAspectRatio(window.innerWidth || minWidth, window.innerHeight || minHeight);
		const widthFromHeight = Math.max(1, Math.round(minHeight * aspect));
		const heightFromWidth = Math.max(1, Math.round(minWidth / aspect));
		if (widthFromHeight > minWidth) {
			minWidth = widthFromHeight;
		}
		if (heightFromWidth > minHeight) {
			minHeight = heightFromWidth;
		}
		return { x: minWidth, y: minHeight };
	}

	private parseCssLength(value: string | null | undefined): number | null {
		if (!value) {
			return null;
		}
		const trimmed = value.trim();
		if (!trimmed) {
			return null;
		}
		const parsed = parseFloat(trimmed);
		if (!Number.isFinite(parsed)) {
			return null;
		}
		return parsed;
	}

	private observeParent(canvas: HTMLCanvasElement) {
		const parent = canvas.parentElement as HTMLElement | null;
		if (!parent || typeof ResizeObserver !== "function") {
			return;
		}
		this.parentResizeObserver = new ResizeObserver(() => {
			this.updateCanvasBounds();
		});
		this.parentResizeObserver.observe(parent);
	}

	private updateCanvasBounds() {
		const canvas = this.ctx?.canvas as HTMLCanvasElement | undefined;
		if (!canvas || !canvas.isConnected) {
			return;
		}
		const parent = canvas.parentElement as HTMLElement | null;
		if (!parent) {
			return;
		}

		const parentStyle = getComputedStyle(parent);
		const paddingLeft = parseFloat(parentStyle.paddingLeft) || 0;
		const paddingRight = parseFloat(parentStyle.paddingRight) || 0;
		const paddingTop = parseFloat(parentStyle.paddingTop) || 0;
		const paddingBottom = parseFloat(parentStyle.paddingBottom) || 0;
		let availableWidth = Math.floor(parent.clientWidth - paddingLeft - paddingRight);
		let availableHeight = Math.floor(parent.clientHeight - paddingTop - paddingBottom);
		const parentRect = parent.getBoundingClientRect();
		if (parentRect && Number.isFinite(parentRect.width)) {
			const rectWidth = Math.floor(parentRect.width - paddingLeft - paddingRight);
			if (rectWidth > 0) {
				availableWidth = rectWidth;
			}
		}
		if (parentRect && Number.isFinite(parentRect.height)) {
			const rectHeight = Math.floor(parentRect.height - paddingTop - paddingBottom);
			if (rectHeight > 0) {
				availableHeight = rectHeight;
			}
		}
		if (!Number.isFinite(availableWidth) || !Number.isFinite(availableHeight)) {
			return;
		}
		if (availableWidth <= 0 || availableHeight <= 0) {
			return;
		}

		let reservedHeight = 0;
		for (const child of Array.from(parent.children)) {
			if (!(child instanceof HTMLElement) || child === canvas) {
				continue;
			}
			const childStyle = getComputedStyle(child);
			if (childStyle.display === "none" || childStyle.position === "absolute" || childStyle.position === "fixed") {
				continue;
			}
			const flexGrow = parseFloat(childStyle.flexGrow || "0");
			if (flexGrow > 0) {
				continue;
			}
			reservedHeight += child.getBoundingClientRect().height;
			reservedHeight += (parseFloat(childStyle.marginTop) || 0) + (parseFloat(childStyle.marginBottom) || 0);
		}

		const usableHeight = Math.floor(Math.max(1, availableHeight - reservedHeight));
		if (!Number.isFinite(usableHeight) || usableHeight <= 0) {
			return;
		}

		const targetResolution = this.getTargetResolution(availableWidth, usableHeight);
		const targetRatio = this.safeAspect(targetResolution.x, targetResolution.y);
		if (!Number.isFinite(targetRatio) || targetRatio <= 0) {
			return;
		}

		const displaySize = this.getContainedSize(availableWidth, usableHeight, targetRatio);
		const deviceScale = this.getDevicePixelRatio();
		const maxScaleFromResolution = Math.min(
			targetResolution.x > 0 ? targetResolution.x / displaySize.x : Number.POSITIVE_INFINITY,
			targetResolution.y > 0 ? targetResolution.y / displaySize.y : Number.POSITIVE_INFINITY
		);
		const cappedScale = Math.min(deviceScale, maxScaleFromResolution);
		const minScale = Math.max(
			1,
			this.minCanvasWidth / displaySize.x,
			this.minCanvasHeight / displaySize.y
		);
		const povScale = Math.max(0.5, this.povScaleFactor);
		const limitedScale = Math.max(1, cappedScale * povScale);
		const renderScale = Math.max(limitedScale, minScale);
		const displayWidth = Math.max(1, Math.floor(displaySize.x));
		const displayHeight = Math.max(1, Math.floor(displaySize.y));
		const renderWidth = Math.max(1, Math.round(displayWidth * renderScale));
		const renderHeight = Math.max(1, Math.round(displayHeight * renderScale));

		if (canvas.width === renderWidth && canvas.height === renderHeight
			&& canvas.style.width === `${displayWidth}px` && canvas.style.height === `${displayHeight}px`) {
			return;
		}

		canvas.width = renderWidth;
		canvas.height = renderHeight;
		canvas.style.width = `${displayWidth}px`;
		canvas.style.height = `${displayHeight}px`;
	}

	public refreshBounds() {
		this.updateCanvasBounds();
	}

	private getTargetAspectRatio(containerWidth: number, containerHeight: number): number {
		const targetResolution = this.getTargetResolution(containerWidth, containerHeight);
		if (targetResolution.x > 0 && targetResolution.y > 0) {
			return targetResolution.x / targetResolution.y;
		}
		return this.fallbackAspectRatio || (4 / 3);
	}

	private getTargetResolution(containerWidth: number, containerHeight: number): Vector2 {
		const isDesktop = this.matchesDesktopBreakpoint(containerWidth);
		if (isDesktop) {
			const availableRatio = this.safeAspect(containerWidth, containerHeight);
			const wideRatio = this.safeAspect(this.targetDesktopResolution.x, this.targetDesktopResolution.y);
			const classicRatio = this.safeAspect(
				this.targetDesktopFallbackResolution.x,
				this.targetDesktopFallbackResolution.y
			);
			const useClassic = Math.abs(availableRatio - classicRatio) < Math.abs(availableRatio - wideRatio);
			return useClassic ? this.targetDesktopFallbackResolution : this.targetDesktopResolution;
		}
		return this.targetMobileResolution;
	}

	private matchesDesktopBreakpoint(containerWidth: number): boolean {
		if (typeof matchMedia === "function") {
			const media = matchMedia(`(min-width: ${this.desktopBreakpointPx}px)`);
			if (typeof media.matches === "boolean") {
				return media.matches;
			}
		}
		return containerWidth >= this.desktopBreakpointPx;
	}

	private safeAspect(width: number, height: number): number {
		if (!Number.isFinite(width) || !Number.isFinite(height) || height <= 0) {
			return 1;
		}
		return width / height;
	}

	private getContainedSize(maxWidth: number, maxHeight: number, aspect: number): Vector2 {
		let width = Math.floor(maxWidth);
		let height = Math.floor(width / aspect);
		if (height > maxHeight) {
			height = Math.floor(maxHeight);
			width = Math.floor(height * aspect);
		}
		return { x: Math.max(1, width), y: Math.max(1, height) };
	}

	private getDevicePixelRatio(): number {
		const dpr = typeof window !== "undefined" && Number.isFinite(window.devicePixelRatio)
			? window.devicePixelRatio
			: 1;
		return Math.max(1, dpr);
	}

	private assignInitialStyleFrom(value: string | null | undefined, prop: string) {
		if (!value || this.initialStyle[prop]) {
			return;
		}
		const trimmed = value.trim();
		if (trimmed) {
			this.initialStyle[prop] = trimmed;
		}
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
			const configuredLimit = stendhal.config.getFloat("loop.fps.limit");
			const initialLimit = this.requestedFpsLimit ?? this.normalizeFpsLimit(configuredLimit);
			this.loop = new GameLoop(
				(dt) => this.update(dt),
				(alpha) => this.render(alpha),
				{
					fpsLimit: initialLimit,
					onFpsSample: (fps) => this.updateFpsCounter(fps),
					networkTaskBudget: 6,
					networkTimeBudgetMs: 5
				}
			);
			this.requestedFpsLimit = initialLimit;
		}
		this.loop.start();
	}

	queueNetworkTask(task: () => void) {
		if (typeof task !== "function") {
			return;
		}
		try {
			if (this.loop && this.loop.isRunning()) {
				this.loop.enqueueNetworkTask(task);
				return;
			}
			task();
		} catch (error) {
			if (typeof console !== "undefined" && console.error) {
				console.error("ViewPort network task failed", error);
			}
		}
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

		this.tileRenderer.configure(stendhal.data.map, this.targetTileWidth, this.targetTileHeight);
		const parallaxImage = stendhal.data.map.parallax.getImageElement();
		this.tileRenderer.updateParallax(parallaxImage);
		this.tileRenderer.prepareFrame(snappedX, snappedY, this.ctx.canvas.width, this.ctx.canvas.height);

		this.tileRenderer.drawBaseLayer(this.ctx, snappedX, snappedY, this.ctx.canvas.width, this.ctx.canvas.height);

		const blendComposite = this.getBlendCompositeOperation();
		this.tileRenderer.drawBlendLayer(
			"blend_ground",
			this.ctx,
			tileOffsetX,
			tileOffsetY,
			blendComposite ? { composite: blendComposite } : undefined
		);

		this.drawEntities(alpha);

		this.tileRenderer.drawRoofLayer(this.ctx, snappedX, snappedY, this.ctx.canvas.width, this.ctx.canvas.height);
		this.tileRenderer.drawBlendLayer(
			"blend_roof",
			this.ctx,
			tileOffsetX,
			tileOffsetY,
			blendComposite ? { composite: blendComposite } : undefined
		);

		this.weatherRenderer.draw(this.ctx);
		this.applyHSLFilter();
		this.drawEntitiesTop(alpha);
		this.drawEmojiSprites();
		this.drawSpeechBubbles();
		this.drawNotificationSprites();

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

		const playerX = (typeof (marauroa.me["_x"]) === "number") ? marauroa.me["_x"] : marauroa.me["x"];
		const playerY = (typeof (marauroa.me["_y"]) === "number") ? marauroa.me["_y"] : marauroa.me["y"];

		let centerX = playerX * this.targetTileWidth + this.targetTileWidth / 2 - canvas.width / 2;
		let centerY = playerY * this.targetTileHeight + this.targetTileHeight / 2 - canvas.height / 2;

		const worldWidth = stendhal.data.map.zoneSizeX * this.targetTileWidth;
		const worldHeight = stendhal.data.map.zoneSizeY * this.targetTileHeight;

		centerX = Math.min(centerX, worldWidth - canvas.width);
		centerX = Math.max(centerX, 0);

		centerY = Math.min(centerY, worldHeight - canvas.height);
		centerY = Math.max(centerY, 0);

		return { x: centerX, y: centerY };
	}

	private ensureEntityCapacity(expectedEntities: number) {
		const required = Math.max(expectedEntities, this.entityCount, this.entityLastCount) * 2;
		if (required <= this.entityPrevPositions.length) {
			return;
		}
		let newSize = this.entityPrevPositions.length;
		if (newSize === 0) {
			newSize = 32;
		}
		while (newSize < required) {
			newSize *= 2;
		}
		const newPrev = new Float32Array(newSize);
		if (this.entityCount > 0) {
			newPrev.set(this.entityPrevPositions.subarray(0, this.entityCount * 2));
		}
		const newCurr = new Float32Array(newSize);
		if (this.entityCount > 0) {
			newCurr.set(this.entityCurrPositions.subarray(0, this.entityCount * 2));
		}
		const newLast = new Float32Array(newSize);
		if (this.entityLastCount > 0) {
			newLast.set(this.entityLastPositions.subarray(0, this.entityLastCount * 2));
		}
		this.entityPrevPositions = newPrev;
		this.entityCurrPositions = newCurr;
		this.entityLastPositions = newLast;
	}

	private swapEntityHistoryBuffers() {
		const temp = this.entityLastPositions;
		this.entityLastPositions = this.entityCurrPositions;
		this.entityCurrPositions = temp;
		this.entityLastCount = this.entityCount;
	}

	private resolveEntityTileX(entity: any): number {
		const override = entity["_x"];
		if (typeof override === "number" && Number.isFinite(override)) {
			return override;
		}
		const base = entity["x"];
		if (typeof base === "number" && Number.isFinite(base)) {
			return base;
		}
		return 0;
	}

	private resolveEntityTileY(entity: any): number {
		const override = entity["_y"];
		if (typeof override === "number" && Number.isFinite(override)) {
			return override;
		}
		const base = entity["y"];
		if (typeof base === "number" && Number.isFinite(base)) {
			return base;
		}
		return 0;
	}

	private withEntityRenderPosition(entity: any, tileX: number, tileY: number, drawFn: () => void) {
		if (typeof entity.pushRenderOverride === "function" && typeof entity.popRenderOverride === "function") {
			entity.pushRenderOverride(tileX, tileY);
			try {
				drawFn();
			} finally {
				entity.popRenderOverride();
			}
		} else {
			drawFn();
		}
	}

	private updateEntities(dtMs: number) {
		const zone = stendhal.zone;
		const entities: Entity[] = (zone && Array.isArray(zone.entities)) ? zone.entities as Entity[] : [];

		if (!entities.length) {
			this.entityCount = 0;
			this.entityRefs.length = 0;
			this.entityIndexLookup = new WeakMap();
			this.entityLastCount = 0;
			return;
		}

		this.ensureEntityCapacity(entities.length);
		this.swapEntityHistoryBuffers();

		const lastPositions = this.entityLastPositions;
		const currPositions = this.entityCurrPositions;
		const prevPositions = this.entityPrevPositions;
		const previousIndexLookup = this.entityIndexLookup;
		const nextIndexLookup = new WeakMap<Entity, number>();

		let count = 0;
		for (let i = 0; i < entities.length; i++) {
			const entity = entities[i];
			if (!entity) {
				continue;
			}
			const base = count * 2;
			let prevX: number;
			let prevY: number;
			const prevIndex = previousIndexLookup.get(entity);
			if (typeof prevIndex === "number" && prevIndex >= 0 && prevIndex < this.entityLastCount) {
				const prevBase = prevIndex * 2;
				prevX = lastPositions[prevBase];
				prevY = lastPositions[prevBase + 1];
			} else {
				prevX = this.resolveEntityTileX(entity);
				prevY = this.resolveEntityTileY(entity);
			}

			entity.updatePosition(dtMs);

			const currX = this.resolveEntityTileX(entity);
			const currY = this.resolveEntityTileY(entity);

			prevPositions[base] = prevX;
			prevPositions[base + 1] = prevY;
			currPositions[base] = currX;
			currPositions[base + 1] = currY;
			this.entityRefs[count] = entity;
			nextIndexLookup.set(entity, count);
			count++;
		}

		this.entityCount = count;
		this.entityRefs.length = count;
		this.entityIndexLookup = nextIndexLookup;
	}

	private advanceMiniMap(dtMs: number) {
		const minimap = ui.get(UIComponentEnum.MiniMap) as MiniMapComponent | undefined;
		if (minimap && typeof (minimap.advance) === "function") {
			minimap.advance(dtMs, this.cameraSpring.getPosition(), this.cameraTarget);
		}
	}

	private renderMiniMap(alpha: number) {
		const minimap = ui.get(UIComponentEnum.MiniMap) as MiniMapComponent | undefined;
		if (minimap && typeof (minimap.renderFrame) === "function") {
			minimap.renderFrame(alpha);
		}
	}

	private updateFpsCounter(fps: number) {
		if (this.fpsLabel) {
			const rounded = Math.max(0, Math.round(fps));
			this.fpsLabel.textContent = (rounded > 0 ? rounded.toString() : "--") + " fps";
		}
	}

	private snapToDevicePixel(value: number): number {
		const ratio = window.devicePixelRatio || 1;
		return Math.round(value * ratio) / ratio;
	}

	public setFpsLimit(limit?: number) {
		const normalized = this.normalizeFpsLimit(limit);
		this.requestedFpsLimit = normalized;
		if (this.loop) {
			this.loop.setFpsLimit(normalized);
		}
	}

	private normalizeFpsLimit(limit?: number): number | undefined {
		if (typeof (limit) !== "number" || !Number.isFinite(limit) || limit <= 0) {
			return undefined;
		}
		const rounded = Math.round(limit);
		let closest = ViewPort.FPS_LIMITS[0];
		let bestDelta = Math.abs(rounded - closest);
		for (let i = 1; i < ViewPort.FPS_LIMITS.length; i++) {
			const candidate = ViewPort.FPS_LIMITS[i];
			const delta = Math.abs(rounded - candidate);
			if (delta < bestDelta) {
				bestDelta = delta;
				closest = candidate;
			}
		}
		return closest;
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
		//	appear to include alpha information
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

	getBlendCompositeOperation(): GlobalCompositeOperation | undefined {
		if (!this.blendMethod) {
			return undefined;
		}
		return this.blendMethod as GlobalCompositeOperation;
	}

	/**
	 * Draws overall entity sprites.
	 */
	drawEntities(alpha: number) {
		const count = this.entityCount;
		if (count === 0) {
			return;
		}
		const prevPositions = this.entityPrevPositions;
		const currPositions = this.entityCurrPositions;
		for (let index = 0; index < count; index++) {
			const entity = this.entityRefs[index];
			if (!entity || typeof entity.draw !== "function") {
				continue;
			}
			const base = index * 2;
			const prevX = prevPositions[base];
			const prevY = prevPositions[base + 1];
			const currX = currPositions[base];
			const currY = currPositions[base + 1];
			const renderX = prevX + (currX - prevX) * alpha;
			const renderY = prevY + (currY - prevY) * alpha;
			this.withEntityRenderPosition(entity, renderX, renderY, () => entity.draw(this.ctx, renderX, renderY));
		}
	}

	/**
	 * Draws titles & HP bars associated with entities.
	 */
	drawEntitiesTop(alpha: number) {
		const count = this.entityCount;
		if (count === 0) {
			return;
		}
		const prevPositions = this.entityPrevPositions;
		const currPositions = this.entityCurrPositions;
		for (let index = 0; index < count; index++) {
			const entity = this.entityRefs[index];
			if (!entity) {
				continue;
			}
			const base = index * 2;
			const prevX = prevPositions[base];
			const prevY = prevPositions[base + 1];
			const currX = currPositions[base];
			const currY = currPositions[base + 1];
			const renderX = prevX + (currX - prevX) * alpha;
			const renderY = prevY + (currY - prevY) * alpha;
			this.withEntityRenderPosition(entity, renderX, renderY, () => {
				if (typeof entity.setStatusBarOffset === "function") {
					entity.setStatusBarOffset();
				}
				if (typeof entity.drawTop === "function") {
					entity.drawTop(this.ctx, renderX, renderY);
				}
			});
		}
	}

	private drawSpeechBubbles() {
		for (let index = 0; index < this.textSprites.length; index++) {
			const bubble = this.textSprites[index];
			if (!bubble) {
				continue;
			}
			const remove = bubble.draw(this.ctx);
			if (remove) {
				bubble.onRemoved();
				this.textSprites[index] = null;
				this.textSpriteFree.push(index);
				this.speechBubblePool.push(bubble);
			}
		}
	}

	private drawNotificationSprites() {
		for (let index = 0; index < this.notifSprites.length; index++) {
			const sprite = this.notifSprites[index];
			if (!sprite) {
				continue;
			}
			const remove = sprite.draw(this.ctx);
			if (remove) {
				sprite.onRemoved();
				this.notifSprites[index] = null;
				this.notifSpriteFree.push(index);
				if (sprite instanceof NotificationBubble) {
					this.notifBubblePool.push(sprite);
				} else if (sprite instanceof AchievementBanner) {
					this.achievementPool.push(sprite);
				}
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

	private acquireSpeechBubble(): SpeechBubble {
		return this.speechBubblePool.pop() || new SpeechBubble();
	}

	private activateSpeechBubble(bubble: SpeechBubble) {
		const index = this.textSpriteFree.length > 0 ? this.textSpriteFree.pop()! : this.textSprites.length;
		if (index === this.textSprites.length) {
			this.textSprites.push(bubble);
		} else {
			this.textSprites[index] = bubble;
		}
		bubble.onAdded(this.ctx);
	}

	private collectActiveSpeechBubbles(): SpeechBubble[] {
		const scratch = this.speechScratch;
		scratch.length = 0;
		for (const bubble of this.textSprites) {
			if (bubble) {
				scratch.push(bubble);
			}
		}
		return scratch;
	}

	/**
	 * Adds a speech bubble to viewport.
	 */
	showSpeechBubble(text: string, entity: RPEntity) {
		const bubble = this.acquireSpeechBubble();
		const siblings = this.collectActiveSpeechBubbles();
		bubble.configure(text, entity, siblings);
		this.speechScratch.length = 0;
		this.activateSpeechBubble(bubble);
	}

	private acquireNotificationBubble(): NotificationBubble {
		return this.notifBubblePool.pop() || new NotificationBubble();
	}

	private acquireAchievementBanner(): AchievementBanner {
		return this.achievementPool.pop() || new AchievementBanner();
	}

	private activateNotificationSprite(sprite: TextBubble) {
		const index = this.notifSpriteFree.length > 0 ? this.notifSpriteFree.pop()! : this.notifSprites.length;
		if (index === this.notifSprites.length) {
			this.notifSprites.push(sprite);
		} else {
			this.notifSprites[index] = sprite;
		}
		sprite.onAdded(this.ctx);
	}

	/**
	 * Adds a notification bubble to viewport.
	 */
	addNotifSprite(mtype: string, text: string, profile?: string) {
		const bubble = this.acquireNotificationBubble();
		bubble.configure(mtype, text, profile);
		this.activateNotificationSprite(bubble);
	}

	/**
	 * Adds an achievement banner to viewport.
	 */
	addAchievementNotif(cat: string, title: string, desc: string) {
		const banner = this.acquireAchievementBanner();
		banner.configure(cat, title, desc);
		this.activateNotificationSprite(banner);
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
			if (!topSprite) {
				continue;
			}
			if (topSprite === sprite || topSprite.clipsPoint(x, y)) {
				topSprite.onRemoved();
				this.notifSprites[idx] = null;
				this.notifSpriteFree.push(idx);
				if (topSprite instanceof NotificationBubble) {
					this.notifBubblePool.push(topSprite);
				} else if (topSprite instanceof AchievementBanner) {
					this.achievementPool.push(topSprite);
				}
				return;
			}
		}

		for (let idx = this.textSprites.length - 1; idx >= 0; idx--) {
			const bubble = this.textSprites[idx];
			if (!bubble) {
				continue;
			}
			if (bubble === sprite || bubble.clipsPoint(x, y)) {
				bubble.onRemoved();
				this.textSprites[idx] = null;
				this.textSpriteFree.push(idx);
				this.speechBubblePool.push(bubble);
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
			if (sprite && sprite.clipsPoint(x, y)) {
				return true;
			}
		}
		for (const sprite of this.textSprites) {
			if (sprite && sprite.clipsPoint(x, y)) {
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
		for (let idx = 0; idx < this.textSprites.length; idx++) {
			const bubble = this.textSprites[idx];
			if (!bubble) {
				continue;
			}
			bubble.onRemoved();
			this.textSprites[idx] = null;
			this.textSpriteFree.push(idx);
			this.speechBubblePool.push(bubble);
		}

		for (let idx = this.emojiSprites.length - 1; idx >= 0; idx--) {
			this.emojiSprites.splice(idx, 1);
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

		mHandle.onMouseUp = function(e: MouseEvent | TouchEvent) {
			const is_touch = stendhal.ui.touch.isTouchEvent(e);
			const viewport = stendhal.ui.gamewindow as ViewPort;
			if (is_touch) {
				stendhal.ui.touch.onTouchEnd(e as TouchEvent);
			}
			var pos = stendhal.ui.html.extractPosition(e);
			const long_touch = is_touch && stendhal.ui.touch.isLongTouch(e);
			let isDoubleTap = false;
			let handledTeleclick = false;
			if (is_touch && !long_touch) {
				isDoubleTap = stendhal.ui.touch.registerTap(pos.pageX, pos.pageY, pos.target, viewport.getElement());
				if (isDoubleTap) {
					handledTeleclick = viewport.handleTeleclickDoubleTap(pos);
				}
			}
			if (handledTeleclick) {
				mHandle.cleanUp(pos);
				if (pos.target instanceof HTMLElement) {
					pos.target.focus();
				}
				e.preventDefault();
				return;
			}
			if ((e instanceof MouseEvent && mHandle.isRightClick(e)) || long_touch) {
				if (entity != stendhal.zone.ground) {
					const append: any[] = [];
					if (long_touch) {
						if (viewport.canHoldEntityForTouch(entity)) {
							append.push({
								title: "Przytrzymaj (podziel stos)",
								action: () => {
									viewport.tryHoldEntityForTouch(entity, pos.pageX, pos.pageY);
								}
							});
						}
					}
					stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Czynności",
						new ActionContextMenu(entity, append), pos.pageX - 50, pos.pageY - 5));
				}
			} else {
				const sendDoubleClick = isDoubleTap && entity === stendhal.zone.ground
					&& viewport.isTeleclickEnabled();
				if (sendDoubleClick) {
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

	private isTeleclickEnabled(): boolean {
		const player = marauroa && marauroa.me;
		if (!player) {
			return false;
		}
		return Object.prototype.hasOwnProperty.call(player, "teleclickmode");
	}

	private handleTeleclickDoubleTap(pos: any): boolean {
		if (!this.isTeleclickEnabled()) {
			return false;
		}
		const target = pos && pos.target;
		const viewportElement = this.getElement();
		let isViewportTarget = false;
		if (target instanceof HTMLElement) {
			if (target === viewportElement) {
				isViewportTarget = true;
			} else if (typeof target.closest === "function") {
				isViewportTarget = !!target.closest("#viewport");
			}
		}
		if (!isViewportTarget) {
			return false;
		}

		const ground = stendhal.zone && stendhal.zone.ground;
		if (!ground || typeof ground.onclick !== "function") {
			return false;
		}

		const localX = typeof pos.canvasRelativeX === "number" ? pos.canvasRelativeX : 0;
		const localY = typeof pos.canvasRelativeY === "number" ? pos.canvasRelativeY : 0;

		ground.onclick(localX, localY, true);
		return true;
	}

	private canHoldEntityForTouch(entity: any): boolean {
		if (!entity || entity.type !== "item") {
			return false;
		}
		const quantity = entity.hasOwnProperty("quantity") ? Number(entity["quantity"]) : 1;
		if (!quantity || quantity <= 1) {
			return false;
		}
		return !!(entity.sprite && entity.sprite.filename);
	}

	private tryHoldEntityForTouch(entity: any, pageX: number, pageY: number): boolean {
		if (!this.canHoldEntityForTouch(entity)) {
			return false;
		}
		const spriteFilename = entity.sprite.filename;
		const sprite = stendhal.data.sprites.getAreaOf(stendhal.data.sprites.get(spriteFilename), 32, 32);
		if (!sprite) {
			return false;
		}
		const position = new Point(pageX, pageY);
		const quantity = entity.hasOwnProperty("quantity") ? Number(entity["quantity"]) : 1;
		const held: HeldObject = {
			path: entity.getIdPath(),
			zone: marauroa.currentZoneName,
			quantity,
			origin: position
		};
		singletons.getHeldObjectManager().set(held, sprite, position);
		stendhal.ui.touch.setHolding(true);
		return true;
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
		this.updateCanvasBounds();
	}
}
