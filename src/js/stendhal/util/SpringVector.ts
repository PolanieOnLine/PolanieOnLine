/***************************************************************************
 *                 Copyright © 2024 - Faiumoni e. V.                       *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

export interface Vector2 {
        x: number;
        y: number;
}

/**
 * Critically damped spring that keeps track of the previous position so
 * interpolation between updates is possible. The implementation operates in
 * milliseconds but physics calculations use seconds internally.
 */
export class SpringVector {

        private readonly stiffness: number;
        private readonly damping: number;

        private readonly position: Vector2 = {x: 0, y: 0};
        private readonly previous: Vector2 = {x: 0, y: 0};
        private readonly velocity: Vector2 = {x: 0, y: 0};

        constructor(stiffness = 600, damping?: number) {
                this.stiffness = stiffness;
                this.damping = damping ?? 2 * Math.sqrt(stiffness);
        }

        /**
         * Immediately sets the spring to a given position without applying any
         * velocity. Useful when switching zones where starting from the origin
         * would look odd.
         */
        snap(position: Vector2) {
                this.position.x = position.x;
                this.position.y = position.y;
                this.previous.x = position.x;
                this.previous.y = position.y;
                this.velocity.x = 0;
                this.velocity.y = 0;
        }

        /**
         * Integrates the spring over the provided delta time, converging towards
         * the requested target.
         */
        step(target: Vector2, dtMs: number) {
                const dt = dtMs / 1000;

                this.previous.x = this.position.x;
                this.previous.y = this.position.y;

                const ax = this.stiffness * (target.x - this.position.x) - this.damping * this.velocity.x;
                const ay = this.stiffness * (target.y - this.position.y) - this.damping * this.velocity.y;

                this.velocity.x += ax * dt;
                this.velocity.y += ay * dt;

                this.position.x += this.velocity.x * dt;
                this.position.y += this.velocity.y * dt;
        }

        getPosition(): Vector2 {
                return {x: this.position.x, y: this.position.y};
        }

        getInterpolated(alpha: number): Vector2 {
                const lerp = (a: number, b: number) => a + (b - a) * alpha;
                return {
                        x: lerp(this.previous.x, this.position.x),
                        y: lerp(this.previous.y, this.position.y)
                };
        }

        clamp(minX: number, maxX: number, minY: number, maxY: number) {
                if (this.position.x < minX) {
                        this.position.x = minX;
                        this.velocity.x = 0;
                } else if (this.position.x > maxX) {
                        this.position.x = maxX;
                        this.velocity.x = 0;
                }

                if (this.position.y < minY) {
                        this.position.y = minY;
                        this.velocity.y = 0;
                } else if (this.position.y > maxY) {
                        this.position.y = maxY;
                        this.velocity.y = 0;
                }

                if (this.previous.x < minX || this.previous.x > maxX
                                || this.previous.y < minY || this.previous.y > maxY) {
                        this.previous.x = this.position.x;
                        this.previous.y = this.position.y;
                }
        }
}

