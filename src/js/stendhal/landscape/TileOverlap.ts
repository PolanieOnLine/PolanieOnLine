export const BASE_TILE_EDGE_TRIM = 0.02;

export interface TileOverlapMetrics {
	tileOverlap: number;
	overlapOffset: number;
	edgeTrim: number;
}

export function resolveTileScale(tileScale: number): number {
	return tileScale > 0 ? tileScale : 1;
}

export function getTileOverlapMetrics(tileScale: number, baseEdgeTrim = BASE_TILE_EDGE_TRIM): TileOverlapMetrics {
	const clampedScale = resolveTileScale(tileScale);
	const tileOverlap = clampedScale < 1 ? Math.max(1, Math.ceil(2 / clampedScale)) : 0;
	const overlapOffset = tileOverlap ? Math.floor(tileOverlap / 2) : 0;
	const edgeTrim = clampedScale < 1 ? Math.max(baseEdgeTrim, baseEdgeTrim / clampedScale) : baseEdgeTrim;

	return {
		tileOverlap,
		overlapOffset,
		edgeTrim
	};
}
