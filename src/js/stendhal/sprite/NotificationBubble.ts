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

		const linewrap = 30;
		const wordbreak = 60;
		this.lines = [];
		this.partsByLine = [];

		const formatted = TextBubble.buildFormattedParts(text, this.textColor);
		this.text = formatted.plainText;

		const words = this.text.split("\t").join(" ").split(" ");
		let nextlineParts: Pair<string, string>[] = [];
		let partIdx = 0;
		let partOffset = 0;
		let lastColor = this.textColor;

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
					lastColor = part.first;
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

		const totalLength = (parts: Pair<string, string>[]) => {
			return parts.reduce((len, p) => len + p.second.length, 0);
		};

		const splitParts = (parts: Pair<string, string>[], count: number)
				: { head: Pair<string, string>[]; tail: Pair<string, string>[] } => {
			const head: Pair<string, string>[] = [];
			const tail: Pair<string, string>[] = [];
			let remaining = count;
			for (const p of parts) {
				if (remaining <= 0) {
					tail.push(p);
					continue;
				}
				if (p.second.length <= remaining) {
					head.push(p);
					remaining -= p.second.length;
				} else {
					head.push(new Pair(p.first, p.second.substr(0, remaining)));
					tail.push(new Pair(p.first, p.second.substr(remaining)));
					remaining = 0;
				}
			}
			return { head, tail };
		};

		for (const w of words) {
			if (nextlineParts.length > 0) {
				nextlineParts.push(...consume(1));
			}
			nextlineParts.push(...consume(w.length));

			const currentLength = totalLength(nextlineParts);
			if (currentLength > wordbreak) {
				const { head, tail } = splitParts(nextlineParts, wordbreak);
				const color = head.length ? head[head.length - 1].first : lastColor;
				const headWithHyphen = [...head, new Pair(color, "-")];
				this.partsByLine.push(headWithHyphen);
				this.lines.push(headWithHyphen.map((p) => p.second).join(""));
				nextlineParts = [new Pair(color, "-")].concat(tail);
			} else if (currentLength >= linewrap) {
				this.partsByLine.push(nextlineParts);
				this.lines.push(nextlineParts.map((p) => p.second).join(""));
				nextlineParts = [];
			}
		}
		if (nextlineParts.length > 0) {
			this.partsByLine.push(nextlineParts);
			this.lines.push(nextlineParts.map((p) => p.second).join(""));
		}

		if (profile) {
			// FIXME: first drawing of profile may still be delayed on slower systems
			// cache profile image at construction
			this.profile = new Image();
			this.loadProfileSprite();
		}
	}

	override draw(ctx: RenderingContext2D): boolean {
		const screenTop = stendhal.ui.gamewindow.offsetY;
		const screenBottom = screenTop + ctx.canvas.height;
		const screenLeft = stendhal.ui.gamewindow.offsetX;
		const screenCenterX = screenLeft + (ctx.canvas.width / 2);


		// get width & height of text
		const fontsize = 14;
		const lheight = fontsize + 6;
		ctx.lineWidth = 2;
		ctx.font = fontsize + "px sans-serif";
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

		ctx.fillStyle = textColor;

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
