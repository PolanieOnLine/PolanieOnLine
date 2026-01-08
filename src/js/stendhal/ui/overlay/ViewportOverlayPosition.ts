/***************************************************************************
 *                     Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

export interface ViewportOverlayPositionOptions {
	margin?: number;
	elementWidth?: number;
	elementHeight?: number;
	offsetLeft?: number;
	offsetTop?: number;
	offsetRight?: number;
	offsetBottom?: number;
}

export interface ViewportOverlayPosition {
	rect: DOMRect;
	scrollLeft: number;
	scrollTop: number;
	safeLeft: number;
	safeTop: number;
	safeRight: number;
	safeBottom: number;
	baseLeft: number;
	baseTop: number;
	baseRight: number;
	baseBottom: number;
}

export function getViewportOverlayPosition(
	options: ViewportOverlayPositionOptions = {}
): ViewportOverlayPosition | null {
	const viewport = document.getElementById("viewport");
	if (!viewport) {
		return null;
	}

	const rect = viewport.getBoundingClientRect();
	const scrollLeft = window.scrollX || document.documentElement.scrollLeft;
	const scrollTop = window.scrollY || document.documentElement.scrollTop;
	const margin = options.margin ?? 0;
	const elementWidth = options.elementWidth ?? 0;
	const elementHeight = options.elementHeight ?? 0;
	const offsetLeft = options.offsetLeft ?? 0;
	const offsetTop = options.offsetTop ?? 0;
	const offsetRight = options.offsetRight ?? 0;
	const offsetBottom = options.offsetBottom ?? 0;

	const baseLeft = rect.left + scrollLeft + margin + offsetLeft;
	const baseTop = rect.top + scrollTop + margin + offsetTop;
	const baseRight = rect.right + scrollLeft - elementWidth - margin - offsetRight;
	const baseBottom = rect.bottom + scrollTop - elementHeight - margin - offsetBottom;

	return {
		rect,
		scrollLeft,
		scrollTop,
		safeLeft: baseLeft,
		safeTop: baseTop,
		safeRight: baseRight,
		safeBottom: baseBottom,
		baseLeft,
		baseTop,
		baseRight,
		baseBottom
	};
}
