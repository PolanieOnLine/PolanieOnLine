/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
import { RenderingContext2D } from "util/Types";
import { Paths } from "../../data/Paths";

export interface NavigationTarget {
	id: string; x: number; y: number; main: boolean; label: string;
}

export function parseNavigationTargets(snapshot: unknown, zone: string): NavigationTarget[] {
	if (!snapshot || typeof snapshot !== "object" || Array.isArray(snapshot)) return [];
	const map = snapshot as Record<string, unknown>;
	if (map["_zone"] !== zone) return [];
	const targets: NavigationTarget[] = [];
	for (const [id, value] of Object.entries(map)) {
		if (id === "_zone" || !id || typeof value !== "string") continue;
		const fields = value.split("\t");
		if (fields.length !== 4 || !fields[0].trim() || !fields[1].trim() || !fields[3].trim()
				|| (fields[2] !== "main" && fields[2] !== "side")) continue;
		const x = Number(fields[0]), y = Number(fields[1]);
		if (Number.isFinite(x) && Number.isFinite(y)) {
			targets.push({id, x, y, main: fields[2] === "main", label: fields[3]});
		}
	}
	return targets;
}

export function compassPosition(target: NavigationTarget, x: number, y: number): number {
	if (target.x === x && target.y === y) return 0.5;
	return 0.5 + Math.atan2(target.x - x, y - target.y) / (2 * Math.PI);
}

export function compassBounds(viewportWidth: number): {left: number; width: number} {
	const width = Math.max(0, Math.min(560, viewportWidth - 24));
	return {left: (viewportWidth - width) / 2, width};
}

const INK = "#fff3d2", MAIN = "#f27766", SIDE = "#d9c697";
let wood: HTMLImageElement | undefined;

/** Screen-space overlay, using only our own private perception snapshot. */
export function drawNavigationCompass(ctx: RenderingContext2D, player: any, zone: string,
		viewportWidth: number, pixelRatio = 1): void {
	if (!player || Number(player["compass_unlocked"]) !== 1) return;
	const x = Number(player["_x"] ?? player["x"]), y = Number(player["_y"] ?? player["y"]);
	if (!Number.isFinite(x) || !Number.isFinite(y)) return;
	const {left, width} = compassBounds(viewportWidth);
	if (width < 80) return;
	const targets = parseNavigationTargets(player["compass_targets"], zone)
		.map(target => ({...target, distance: Math.hypot(target.x - x, target.y - y)}))
		.filter(target => Number.isFinite(target.distance))
		.sort((a, b) => a.distance - b.distance || a.id.localeCompare(b.id));
	ctx.save();
	try {
		ctx.setTransform(pixelRatio, 0, 0, pixelRatio, left * pixelRatio, 12 * pixelRatio);
		ctx.beginPath(); ctx.rect(0, 0, width, 38); ctx.clip();
		ctx.globalAlpha = 0.65;
		if (!wood) { wood = new Image(); wood.src = Paths.gui + "/panel_wood.jpg"; }
		ctx.fillStyle = "#382818";
		if (wood.complete && wood.naturalWidth > 0) {
			const pattern = ctx.createPattern(wood, "repeat");
			if (pattern) ctx.fillStyle = pattern;
		}
		ctx.fillRect(0, 0, width, 38);
		ctx.strokeStyle = "#ae8a5b"; ctx.lineWidth = 1; ctx.strokeRect(.5, .5, width - 1, 37);
		ctx.globalAlpha = 1;
		ctx.font = "bold 11px Arial, sans-serif"; ctx.textAlign = "center";
		ctx.textBaseline = "alphabetic";
		ctx.shadowColor = "#000"; ctx.shadowOffsetX = 1; ctx.shadowOffsetY = 1; ctx.shadowBlur = 0;
		const directions = ["S", "W", "N", "E", "S"];
		for (let tick = 0; tick <= 16; tick++) {
			const tx = 8 + Math.round((width - 16) * tick / 16), cardinal = tick % 4 === 0;
			ctx.fillStyle = INK;
			ctx.fillRect(tx - (cardinal ? 1 : 0), 14, cardinal ? 2 : 1, cardinal ? 7 : 4);
			if (cardinal) ctx.fillText(directions[tick / 4], tx, 12);
		}
		ctx.font = "11px Arial, sans-serif";
		const occupied: number[] = [];
		for (const target of targets) {
			const tx = 8 + Math.round((width - 16) * compassPosition(target, x, y));
			if (occupied.some(previous => Math.abs(previous - tx) < 45)) continue;
			occupied.push(tx);
			ctx.fillStyle = target.main ? MAIN : SIDE;
			ctx.beginPath(); ctx.moveTo(tx, 18); ctx.lineTo(tx + 4, 22);
			ctx.lineTo(tx, 26); ctx.lineTo(tx - 4, 22); ctx.closePath(); ctx.fill();
			ctx.fillStyle = INK; ctx.textAlign = "left";
			const distance = String(Math.round(target.distance));
			ctx.fillText(distance, Math.min(width - ctx.measureText(distance).width - 3, tx + 6), 26);
			if (occupied.length === 4) break;
		}
		if (targets.length) {
			let caption = targets[0].label;
			while (caption && ctx.measureText(caption).width > width - 20) caption = caption.slice(0, -1);
			ctx.textAlign = "center"; ctx.fillStyle = targets[0].main ? MAIN : SIDE;
			ctx.fillText(caption, width / 2, 36);
		}
	} finally { ctx.restore(); }
}
