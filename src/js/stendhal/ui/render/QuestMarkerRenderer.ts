/*
 * Copyright (C) 2026 - PolanieOnLine
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
import { RenderingContext2D } from "util/Types";

export type QuestMarker = "available" | "repeatable" | "ready" | "progress";
export const QUEST_MARKER_WIDTH = 12;
export const QUEST_MARKER_HEIGHT = 16;
const EXCLAMATION = ["00100", "01110", "01110", "00100", "00100", "00000", "00100"];
const QUESTION = ["01110", "10001", "00001", "00110", "00100", "00000", "00100"];
const COLORS: Record<QuestMarker, string> = {
	available: "#f4d06f", repeatable: "#79b9eb", ready: "#82d878", progress: "#a4a49b"
};
const IMAGES = new Map<QuestMarker, HTMLCanvasElement>();

/** Ignore previous-zone snapshots, unknown states and another player's state. */
export function readQuestMarker(markers: unknown, zone: string | undefined,
		id: string | number | undefined): QuestMarker | undefined {
	if (!markers || typeof markers !== "object" || !zone || id == null) {
		return undefined;
	}
	const data = markers as Record<string, unknown>;
	if (data["_zone"] !== zone) {
		return undefined;
	}
	const state = data[String(id)];
	return typeof state === "string" && Object.prototype.hasOwnProperty.call(COLORS, state)
		? state as QuestMarker : undefined;
}

/** A cached 12x16 pixel glyph, with no animation or per-frame image generation. */
export function drawQuestMarker(ctx: RenderingContext2D, marker: QuestMarker,
		x: number, y: number) {
	let image = IMAGES.get(marker);
	if (!image) {
		image = document.createElement("canvas");
		image.width = QUEST_MARKER_WIDTH;
		image.height = QUEST_MARKER_HEIGHT;
		const graphics = image.getContext("2d");
		if (!graphics) {
			return;
		}
		const rows = marker === "available" || marker === "repeatable" ? EXCLAMATION : QUESTION;
		graphics.fillStyle = "#211b16";
		for (let row = 0; row < rows.length; row++) {
			for (let column = 0; column < rows[row].length; column++) {
				if (rows[row][column] === "1") {
					graphics.fillRect(2 * column, 2 * row, 4, 4);
				}
			}
		}
		graphics.fillStyle = COLORS[marker];
		for (let row = 0; row < rows.length; row++) {
			for (let column = 0; column < rows[row].length; column++) {
				if (rows[row][column] === "1") {
					graphics.fillRect(1 + 2 * column, 1 + 2 * row, 2, 2);
				}
			}
		}
		IMAGES.set(marker, image);
	}
	ctx.drawImage(image, Math.round(x), Math.round(y));
}
