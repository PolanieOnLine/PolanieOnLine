/***************************************************************************
 *                (C) Copyright 2022-2023 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { CombinedTileset } from "./CombinedTileset";

declare var stendhal: any;

const TILE_EPSILON = 0.5;

export class LandscapeRenderer {

        drawLayer(
                        canvas: HTMLCanvasElement,
                        combinedTileset: CombinedTileset, layerNo: number,
                        tileOffsetX: number, tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {
                if (!combinedTileset) {
                        return;
                }
                let ctx = canvas.getContext("2d")!;

                const layer = combinedTileset.combinedLayers[layerNo];
                const yMax = Math.min(tileOffsetY + canvas.height / targetTileHeight + 1, stendhal.data.map.zoneSizeY);
                const xMax = Math.min(tileOffsetX + canvas.width / targetTileWidth + 1, stendhal.data.map.zoneSizeX);

                const previousTransform = ctx.getTransform();
                const cameraOffsetX = -previousTransform.e;
                const cameraOffsetY = -previousTransform.f;
                ctx.setTransform(1, 0, 0, 1, 0, 0);

                try {
                        for (let y = tileOffsetY; y < yMax; y++) {
                                for (let x = tileOffsetX; x < xMax; x++) {
                                        let index = layer[y * stendhal.data.map.zoneSizeX + x];
                                        if (index > -1) {

                                                try {
                                                        const screenX = Math.round(x * targetTileWidth - cameraOffsetX);
                                                        const screenY = Math.round(y * targetTileHeight - cameraOffsetY);

                                                        ctx.drawImage(combinedTileset.canvas,

                                                                (index % combinedTileset.tilesPerRow) * stendhal.data.map.tileWidth,
                                                                Math.floor(index / combinedTileset.tilesPerRow) * stendhal.data.map.tileHeight,

                                                                stendhal.data.map.tileWidth, stendhal.data.map.tileHeight,
                                                                screenX - TILE_EPSILON, screenY - TILE_EPSILON,
                                                                targetTileWidth + (TILE_EPSILON * 2), targetTileHeight + (TILE_EPSILON * 2));
                                                } catch (e) {
                                                        console.error(e);
                                                }
                                        }
                                }
                        }
                } finally {
                        ctx.setTransform(previousTransform);
                }
        }

}
