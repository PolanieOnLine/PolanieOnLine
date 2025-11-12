/***************************************************************************
 *                 Copyright © 2003-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { TextBubble } from "./TextBubble";
import { BackgroundPainter } from "../util/BackgroundPainter";

declare const stendhal: any;


export class AchievementBanner extends TextBubble {

        private title = "";
        private readonly banner: BackgroundPainter;
        private readonly bannerHeight: number;
        private icon?: HTMLImageElement;

        private font = "";
        private fontT = "";

        private innerWidth = 0;
        private innerHeight = 0;
        private readonly padding = 32;


        constructor() {
                super("");
                const bg = stendhal.data.sprites.get(stendhal.paths.gui + "/banner_background.png");
                this.banner = new BackgroundPainter(bg);
                this.bannerHeight = bg.height || 0;
        }

        configure(cat: string, title: string, desc: string) {
                this.resetBubble(desc, TextBubble.STANDARD_DUR * 4);
                this.title = title;
                this.font = "normal 14px " + stendhal.config.get("font.travel-log");
                this.fontT = "normal 20px " + stendhal.config.get("font.travel-log");
                this.icon = stendhal.data.sprites.get(stendhal.paths.achievements
                                + "/" + cat.toLowerCase() + ".png");

                const gamewindow = document.getElementById("viewport") as HTMLCanvasElement | null;
                const ctx = gamewindow ? gamewindow.getContext("2d") : null;
                if (ctx && gamewindow) {
                        const td = this.getTextDimensions(ctx);
                        this.innerWidth = td.width + this.padding; // add padding between icon & text
                        this.innerHeight = td.height;
                        this.width = this.innerWidth + (this.padding * 2); // add left & right padding
                        this.height = this.bannerHeight || this.innerHeight;
                        this.x = (gamewindow.width / 2) - (this.width / 2);
                        this.y = gamewindow.height - this.height;
                } else {
                        const fallbackSize = this.icon ? this.icon.height : 96;
                        this.innerWidth = (this.icon ? this.icon.width : 128) + this.padding;
                        this.innerHeight = fallbackSize;
                        this.width = this.innerWidth + (this.padding * 2);
                        this.height = this.bannerHeight || this.innerHeight;
                        this.x = 0;
                        this.y = 0;
                }
        }

        override draw(ctx: CanvasRenderingContext2D): boolean {
                const targetX = stendhal.ui.gamewindow.offsetX + this.x;
                const targetY = stendhal.ui.gamewindow.offsetY + this.y;

                const iconX = targetX + (this.width / 2) - (this.innerWidth / 2);
                const iconHeight = this.icon ? this.icon.height : 0;
                const iconWidth = this.icon ? this.icon.width : 0;
                const iconY = targetY + (this.height / 2) - (iconHeight * 0.75);
                const textX = iconX + iconWidth + this.padding;
                // TODO: user inner height (text height) for centering vertically
                const textY = targetY + (this.height / 2) + 10;

                this.banner.paint(ctx, targetX, targetY, this.width,
                                this.height);
                if (this.icon && this.icon.height && this.icon.complete) {
                        ctx.drawImage(this.icon, 0, 0, this.icon.width,
                                        this.icon.height, iconX, iconY, this.icon.width,
                                        this.icon.height);
                }
                ctx.fillStyle = "#000000";
                ctx.font = this.fontT;
                ctx.fillText(this.title, textX, textY-25);
                ctx.font = this.font;
		ctx.fillText(this.text, textX, textY);

		return this.expired();
	}

        private getTextDimensions(ctx: CanvasRenderingContext2D): any {
                const ret = {} as any;
                ctx.font = this.font;
                let m = ctx.measureText(this.text);
                ret.width = m.width;
                ctx.font = this.fontT;
                m = ctx.measureText(this.title);
		ret.width = Math.max(ret.width, m.width);
		// FIXME: how to find text height
		ret.height = 96;
		return ret;
	}

        override getX(): number {
                return stendhal.ui.gamewindow.offsetX + this.x;
        }

	override getY(): number {
		return stendhal.ui.gamewindow.offsetY + this.y;
	}
}
