/***************************************************************************
 *                    Copyright © 2025 - Faiumoni e. V.                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ConfigManager } from "../util/ConfigManager";

export interface GameScreenSize {
	width: number;
	height: number;
	renderWidth: number;
	renderHeight: number;
	pixelRatio: number;
	isMobile: boolean;
}

export interface GameScreenSizeParams {
	availableWidth: number;
	availableHeight: number;
	aspectRatio: number;
	minWidth: number;
	minHeight: number;
	mobileMinWidth?: number;
	mobileMinHeight?: number;
	baseWidth: number;
	baseHeight: number;
	config: ConfigManager;
	maxPixelRatio?: number;
}

const MOBILE_QUERY = "(max-width: 900px)";

function detectMobile(): boolean {
	if (typeof window === "undefined") {
		return false;
	}
	const media = window.matchMedia ? window.matchMedia(MOBILE_QUERY).matches : false;
	const ua = (navigator && navigator.userAgent || "").toLowerCase();
	const touch = typeof navigator !== "undefined" && (navigator.maxTouchPoints || 0) > 1;
	return media || touch || ua.indexOf("mobile") >= 0 || ua.indexOf("android") >= 0;
}

function fitToArea(aspect: number, maxWidth: number, maxHeight: number): {width: number; height: number;} {
	let width = Math.floor(maxWidth);
	let height = Math.floor(width / aspect);
	if (height > maxHeight) {
		height = Math.floor(maxHeight);
		width = Math.floor(height * aspect);
	}
	return {
		width: Math.max(1, width),
		height: Math.max(1, height)
	};
}

function parsePreferredResolution(value: string | null | undefined): {width: number; height: number;} | null {
	if (!value) {
		return null;
	}
	const trimmed = value.trim().toLowerCase();
	if (trimmed === "auto" || !trimmed) {
		return null;
	}
	const match = trimmed.match(/^(\d+)\s*x\s*(\d+)$/);
	if (!match) {
		return null;
	}
	const width = parseInt(match[1], 10);
	const height = parseInt(match[2], 10);
	if (!Number.isFinite(width) || !Number.isFinite(height) || width <= 0 || height <= 0) {
		return null;
	}
	return { width, height };
}

export function computeGameScreenSize(params: GameScreenSizeParams): GameScreenSize {
	const isMobile = detectMobile();
	const minWidth = isMobile ? (params.mobileMinWidth || params.minWidth) : params.minWidth;
	const minHeight = isMobile ? (params.mobileMinHeight || params.minHeight) : params.minHeight;
	const preferred = isMobile ? null : parsePreferredResolution(params.config.get("display.resolution.desktop"));
	const maxPixelRatio = params.maxPixelRatio || 2.5;
	const pixelRatio = Math.min(maxPixelRatio, Math.max(1, window.devicePixelRatio || 1));

	const aspect = params.aspectRatio > 0 ? params.aspectRatio : 4 / 3;
	const maxDisplay = fitToArea(aspect, params.availableWidth, params.availableHeight);

	let targetWidth = preferred ? preferred.width : Math.max(minWidth, params.baseWidth);
	let targetHeight = preferred ? preferred.height : Math.max(minHeight, params.baseHeight);
	let targetAspect = targetWidth > 0 && targetHeight > 0 ? targetWidth / targetHeight : aspect;
	if (!Number.isFinite(targetAspect) || targetAspect <= 0) {
		targetAspect = aspect;
	}

	let displayWidth = maxDisplay.width;
	let displayHeight = Math.round(displayWidth / targetAspect);
	if (displayHeight > maxDisplay.height) {
		displayHeight = maxDisplay.height;
		displayWidth = Math.round(displayHeight * targetAspect);
	}

	if (preferred) {
		displayWidth = Math.min(maxDisplay.width, preferred.width);
		displayHeight = Math.round(displayWidth / targetAspect);
		if (displayHeight > maxDisplay.height) {
			displayHeight = Math.min(maxDisplay.height, preferred.height);
			displayWidth = Math.round(displayHeight * targetAspect);
		}
	}

	if (!preferred && !isMobile) {
		displayWidth = Math.min(maxDisplay.width, Math.max(minWidth, targetWidth));
		displayHeight = Math.round(displayWidth / targetAspect);
		if (displayHeight > maxDisplay.height) {
			displayHeight = maxDisplay.height;
			displayWidth = Math.round(displayHeight * targetAspect);
		}
	}

	if (displayWidth < minWidth) {
		displayWidth = Math.min(maxDisplay.width, minWidth);
		displayHeight = Math.round(displayWidth / targetAspect);
	}
	if (displayHeight < minHeight) {
		displayHeight = Math.min(maxDisplay.height, minHeight);
		displayWidth = Math.round(displayHeight * targetAspect);
	}

	displayWidth = Math.min(maxDisplay.width, Math.max(1, Math.round(displayWidth)));
	displayHeight = Math.min(maxDisplay.height, Math.max(1, Math.round(displayHeight)));

	const renderWidth = Math.max(minWidth, Math.round(displayWidth * pixelRatio));
	const renderHeight = Math.max(minHeight, Math.round(displayHeight * pixelRatio));

	return {
		width: displayWidth,
		height: displayHeight,
		renderWidth,
		renderHeight,
		pixelRatio,
		isMobile
	};
}
