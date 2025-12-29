/***************************************************************************
 *                 Copyright © 2003-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;

import { TextBubble } from "./TextBubble";

import { Color } from "../data/color/Color";

import { NotificationType } from "../util/NotificationType";
import { Pair } from "../util/Pair";
import { Speech } from "../util/Speech";
import { RenderingContext2D } from "util/Types";


export class NotificationBubble extends TextBubble {
	private static readonly FONT_SIZE = 14;
	private static readonly LINE_HEIGHT = NotificationBubble.FONT_SIZE + 6;
	private static readonly FONT = NotificationBubble.FONT_SIZE + "px sans-serif";
	private static readonly MAX_TEXT_WIDTH = 340;

	private mtype: string;
	private lines: string[];
	private partsByLine: Pair<string, string>[][];
	private readonly textColor: string;
	private profile?: HTMLImageElement;
	private profileName?: string;
	private lmargin = 4;

	constructor(mtype: string, text: string, profile?: string) {
		super(text);
		this.mtype = mtype;
		this.profileName = profile;
		this.textColor = NotificationType[this.mtype] || Color.BLACK;

		this.duration = Math.max(
			TextBubble.STANDARD_DUR,
			this.text.length * TextBubble.STANDARD_DUR / 50);

		this.lines = [];
		this.partsByLine = [];

		const formatted = TextBubble.buildFormattedParts(text, this.textColor);
		this.text = formatted.plainText;

		const measurementCtx = NotificationBubble.createMeasurementContext();
		measurementCtx.font = NotificationBubble.FONT;

		const words = this.text.split("\t").join(" ").split(" ");
		let nextlineParts: Pair<string, string>[] = [];
		let partIdx = 0;
		let partOffset = 0;
		let lastColor = this.textColor;
		let longestWidth = 0;

		const measurePartsWidth = (parts: Pair<string, string>[]) =>
			measurementCtx.measureText(parts.map((p) => p.second).join(""))
				.width;

		const consume = (count: number) => {
			const consumed: Pair<string, string>[] = [];
			let remaining = count;
			while (remaining > 0 && partIdx < formatted.parts.length) {
				const part = formatted.parts[partIdx];
				const available = part.second.length - partOffset;
				const take = Math.min(remaining, available);
				const segment = part.second.substr(partOffset, take);
				if (segment.length > 0) {
					consumed.push(new Pair(part.first, segment));
					lastColor = part.first || lastColor;
				}
				remaining -= take;
				partOffset += take;
				if (partOffset >= part.second.length) {
					partIdx++;
					partOffset = 0;
				}
			}
			return consumed;
		};

		const collectLine = (parts: Pair<string, string>[]) => {
			this.partsByLine.push(parts);
			const textLine = parts.map((p) => p.second).join("");
			this.lines.push(textLine);
			const lineWidth = measurePartsWidth(parts);
			if (lineWidth > longestWidth) {
				longestWidth = lineWidth;
			}
		};

		const collapseCharsToParts = (chars: { color: string; char: string }[]) => {
			const collapsed: Pair<string, string>[] = [];
			for (const piece of chars) {
				const last = collapsed[collapsed.length - 1];
				if (last && last.first === piece.color) {
					last.second += piece.char;
				} else {
					collapsed.push(new Pair(piece.color, piece.char));
				}
			}
			return collapsed;
		};

		const splitLongWord = (parts: Pair<string, string>[]) => {
			const chars: { color: string; char: string }[] = [];
			for (const p of parts) {
				for (const char of p.second) {
					chars.push({ color: p.first || this.textColor, char });
				}
				}
			let splitIndex = 1;
			for (let idx = 1; idx <= chars.length; idx++) {
				const preview = chars.slice(0, idx).map((c) => c.char).join("") + "-";
				if (measurementCtx.measureText(preview).width <= NotificationBubble.MAX_TEXT_WIDTH) {
					splitIndex = idx;
				} else {
					break;
				}
			}
			const headChars = chars.slice(0, splitIndex);
			const tailChars = chars.slice(splitIndex);
			const hyphenColor = headChars.length
				? headChars[headChars.length - 1].color
				: (parts[parts.length - 1]?.first || this.textColor);
			const head = collapseCharsToParts(headChars);
			head.push(new Pair(hyphenColor, "-"));
			const tail = collapseCharsToParts(tailChars);
			tail.unshift(new Pair(hyphenColor, "-"));
			return { head, tail };
		};

		for (const w of words) {
			const lineHasContent = nextlineParts.length > 0;
			const prefixParts = lineHasContent ? consume(1) : [];
			const wordParts = consume(w.length);
			const candidateParts = nextlineParts.concat(prefixParts, wordParts);
			const candidateWidth = measurePartsWidth(candidateParts);

			if (candidateWidth <= NotificationBubble.MAX_TEXT_WIDTH) {
				nextlineParts = candidateParts;
				continue;
			}
			if (nextlineParts.length > 0) {
				collectLine(nextlineParts);
				nextlineParts = [];
				if (measurePartsWidth(wordParts) <= NotificationBubble.MAX_TEXT_WIDTH) {
					nextlineParts = wordParts;
					continue;
				}
			}

			let remainingParts = wordParts;
			while (measurePartsWidth(remainingParts) > NotificationBubble.MAX_TEXT_WIDTH) {
				const { head, tail } = splitLongWord(remainingParts);
				collectLine(head);
				remainingParts = tail;
			}

			nextlineParts = remainingParts;
		}
		if (nextlineParts.length > 0) {
			collectLine(nextlineParts);
		}

		this.width = longestWidth + (this.lmargin * 2);
		this.height = this.lines.length * NotificationBubble.LINE_HEIGHT;

		if (profile) {
			// FIXME: first drawing of profile may still be delayed on slower systems
			// cache profile image at construction
			this.profile = new Image();
			this.loadProfileSprite();
		}
	}

	private static createMeasurementContext(): CanvasRenderingContext2D {
		const canvas = (typeof OffscreenCanvas !== "undefined")
			? new OffscreenCanvas(1, 1)
			: document.createElement("canvas");
		const ctx = canvas.getContext("2d") as CanvasRenderingContext2D | null;
		if (!ctx) {
			throw new Error("Unable to create measurement context for NotificationBubble");
		}
		return ctx;
	}

	override draw(ctx: RenderingContext2D): boolean {
		const screenTop = stendhal.ui.gamewindow.offsetY;
		const screenBottom = screenTop + ctx.canvas.height;
		const screenLeft = stendhal.ui.gamewindow.offsetX;
		const screenCenterX = screenLeft + (ctx.canvas.width / 2);

		// get width & height of text
		const fontsize = NotificationBubble.FONT_SIZE;
		const lheight = NotificationBubble.LINE_HEIGHT;
		ctx.lineWidth = 2;
		ctx.font = NotificationBubble.FONT;
		ctx.fillStyle = "rgb(60, 30, 0)";
		ctx.strokeStyle = this.textColor;

		const lcount = this.lines.length;
		if (this.width < 0 || this.height < 0) {
			let longest = 0;
			for (let li = 0; li < lcount; li++) {
				let measurement = ctx.measureText(this.lines[li]);
				if (measurement.width > longest) {
					longest = measurement.width;
				}
			}

			this.width = longest + (this.lmargin * 2);
			this.height = lcount * lheight;
		}
		this.x = screenCenterX - (this.width / 2);
		// Note: border is 1 pixel
		this.y = screenBottom - this.height + TextBubble.adjustY - 1;

		if (this.profile) {
			if (!this.profile.complete || !this.profile.height) {
				this.loadProfileSprite();
			}
			if (this.profile.complete && this.profile.height) {
				ctx.drawImage(this.profile, this.x - 48, this.y - 16);
			}
			Speech.drawBubbleRounded(ctx, this.x, this.y - 15,
				this.width, this.height);
		} else {
			Speech.drawBubble(ctx, this.x, this.y, this.width,
				this.height);
		}

		ctx.fillStyle = this.textColor;

		let sy = this.y;
		for (let li = 0; li < lcount; li++) {
			let sx = this.x + this.lmargin;
			const parts = this.partsByLine[li];
			for (const part of parts) {
				ctx.fillStyle = part.first || this.textColor;
				ctx.fillText(part.second, sx, sy);
				sx += ctx.measureText(part.second).width;
			}
			sy += lheight;
		}

		return this.expired();
	}

	/**
	 * Loads a profile image to be drawn with text.
	 */
	private loadProfileSprite() {
		const img = stendhal.data.sprites.get(stendhal.paths.sprites
			+ "/npc/" + this.profileName + ".png");
		if (img.complete && img.height) {
			this.profile = stendhal.data.sprites.getAreaOf(img, 48, 48, 48, 128);
		}
	}
}
