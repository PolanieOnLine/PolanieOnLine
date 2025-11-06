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

import { TextBubble, TextSegment } from "./TextBubble";

import { Color } from "../data/color/Color";

import { NotificationType } from "../util/NotificationType";
import { Speech } from "../util/Speech";

interface LayoutSegment {
	text: string;
	color: string;
	underline: boolean;
	italic: boolean;
	width: number;
	isWhitespace: boolean;
	forceBreak?: boolean;
}

interface FormattedLine {
	segments: LayoutSegment[];
	width: number;
}

export class NotificationBubble extends TextBubble {
	private readonly bubbleTextColor: string;
	private readonly borderColor: string;
	private segments: TextSegment[];
	private lines: FormattedLine[];
	private profile?: HTMLImageElement;
	private profileName?: string;
	private lmargin = 4;
	private readonly fontsize = 14;
	private readonly lineHeight = this.fontsize + 6;
	private readonly baseFont: string;
	private readonly italicFont: string;
	private static readonly BACKGROUND = "rgb(60, 30, 0)";

	constructor(mtype: string, text: string, profile?: string) {
		super(text);
		this.profileName = profile

		this.duration = Math.max(
			TextBubble.STANDARD_DUR,
			this.text.length * TextBubble.STANDARD_DUR / 50);

		this.bubbleTextColor = NotificationType[mtype] || Color.BLACK;
		this.borderColor = mtype === "privmsg"
			? Color.CHAT_PRIVATE
			: this.bubbleTextColor;
		this.segments = [];
		this.segregate(this.segments, this.bubbleTextColor);
		this.lines = [];
		this.baseFont = this.fontsize + "px sans-serif";
		this.italicFont = "italic " + this.fontsize + "px sans-serif";

		if (profile) {
			// FIXME: first drawing of profile may still be delayed on slower systems
			// cache profile image at construction
			this.profile = new Image();
			this.loadProfileSprite();
		}
	}

	override draw(ctx: CanvasRenderingContext2D): boolean {
		const screenTop = stendhal.ui.gamewindow.offsetY;
		const screenBottom = screenTop + ctx.canvas.height;
		const screenLeft = stendhal.ui.gamewindow.offsetX;
		const screenCenterX = screenLeft + (ctx.canvas.width / 2);

		// get width & height of text
		ctx.lineWidth = 2;
		ctx.font = this.baseFont;
		ctx.fillStyle = NotificationBubble.BACKGROUND;
		ctx.strokeStyle = this.borderColor;

		if ((this.width < 0 || this.height < 0) || this.lines.length === 0) {
			this.layout(ctx);
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

		ctx.save();
		let sy = this.y;
		for (const line of this.lines) {
			let sx = this.x + this.lmargin;
			for (const segment of line.segments) {
				ctx.font = segment.italic ? this.italicFont : this.baseFont;
				ctx.fillStyle = segment.color;
				ctx.fillText(segment.text, sx, sy);
				if (segment.underline && segment.width > 0) {
					ctx.save();
					ctx.strokeStyle = segment.color;
					ctx.lineWidth = 1;
					ctx.beginPath();
					ctx.moveTo(sx, sy + 1);
					ctx.lineTo(sx + segment.width, sy + 1);
					ctx.stroke();
					ctx.restore();
				}
				sx += segment.width;
			}
			sy += this.lineHeight;
		}
		ctx.restore();

		return this.expired();
	}

	private layout(ctx: CanvasRenderingContext2D) {
		const previousFont = ctx.font;
		ctx.font = this.baseFont;
		const maxLineWidth = ctx.measureText("M".repeat(30)).width;
		ctx.font = this.baseFont;
		const maxWordWidth = ctx.measureText("M".repeat(60)).width;

		const measure = (text: string, italic: boolean) => {
			ctx.font = italic ? this.italicFont : this.baseFont;
			return ctx.measureText(text).width;
		};

		const hyphenWidthCache = new Map<boolean, number>();
		const hyphenWidth = (italic: boolean) => {
			if (!hyphenWidthCache.has(italic)) {
				hyphenWidthCache.set(italic, measure("-", italic));
			}
			return hyphenWidthCache.get(italic)!;
		};

		const splitLongSegment = (segment: LayoutSegment): LayoutSegment[] => {
			const chars = Array.from(segment.text);
			const result: LayoutSegment[] = [];
			let chunk = "";
			let width = 0;
			const hyWidth = hyphenWidth(segment.italic);
			for (const ch of chars) {
				const charWidth = measure(ch, segment.italic);
				if (chunk && width + charWidth > maxWordWidth) {
					result.push({
						text: chunk + "-",
						color: segment.color,
						underline: segment.underline,
						italic: segment.italic,
						width: width + hyWidth,
						isWhitespace: false,
						forceBreak: true
					});
					chunk = "-" + ch;
					width = hyWidth + charWidth;
				} else {
					chunk += ch;
					width += charWidth;
				}
			}
			if (chunk) {
				result.push({
					text: chunk,
					color: segment.color,
					underline: segment.underline,
					italic: segment.italic,
					width,
					isWhitespace: false
				});
			}
			return result;
		};

		const rawSegments: LayoutSegment[] = [];
		for (const part of this.segments) {
			const replaced = part.text.replace(/\t/g, " ");
			const pieces = replaced.split(/(\s+)/);
			for (const piece of pieces) {
				if (!piece) {
					continue;
				}
				rawSegments.push({
					text: piece,
					color: part.color,
					underline: !!part.underline,
					italic: !!part.italic,
					width: 0,
					isWhitespace: /^\s+$/.test(piece)
				});
			}
		}

		const lines: FormattedLine[] = [];
		let currentSegments: LayoutSegment[] = [];
		let currentWidth = 0;

		const flushLine = () => {
			if (currentSegments.length === 0) {
				lines.push({ segments: [], width: 0 });
				return;
			}
			let trimmedWidth = currentWidth;
			const trimmedSegments = currentSegments.slice();
			while (trimmedSegments.length > 0 && trimmedSegments[trimmedSegments.length - 1].isWhitespace) {
				trimmedWidth -= trimmedSegments[trimmedSegments.length - 1].width;
				trimmedSegments.pop();
			}
			lines.push({ segments: trimmedSegments, width: trimmedWidth });
			currentSegments = [];
			currentWidth = 0;
		};

		const appendSegment = (segment: LayoutSegment) => {
			if (segment.isWhitespace && currentSegments.length === 0) {
				return;
			}
			if (segment.width === 0) {
				segment.width = measure(segment.text, segment.italic);
			}

			if (!segment.isWhitespace && segment.width > maxWordWidth) {
				const split = splitLongSegment(segment);
				for (const part of split) {
					appendSegment(part);
					if (part.forceBreak) {
						flushLine();
					}
				}
				return;
			}

			if (!segment.isWhitespace && currentSegments.length > 0
					&& currentWidth + segment.width > maxLineWidth) {
				flushLine();
			}

			currentSegments.push({ ...segment });
			currentWidth += segment.width;

			if (!segment.isWhitespace && currentWidth >= maxLineWidth && !segment.forceBreak) {
				flushLine();
			}
		};

		for (const segment of rawSegments) {
			appendSegment(segment);
		}

		if (currentSegments.length > 0) {
			flushLine();
		}

		if (lines.length === 0) {
			lines.push({ segments: [], width: 0 });
		}

		this.lines = lines;
		const maxWidth = Math.max(...lines.map((line) => line.width));
		this.width = Math.max(maxWidth, 0) + (this.lmargin * 2);
		this.height = lines.length * this.lineHeight;

		ctx.font = previousFont;
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
