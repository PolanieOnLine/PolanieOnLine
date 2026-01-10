/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { InteractionButtonBase } from "./InteractionButtonBase";
import { UiStateStore } from "../mobile/UiStateStore";
import { ElementClickListener } from "../../util/ElementClickListener";
import { getMobileRightPanelCollapsedInset, getViewportOverlayPosition } from "../overlay/ViewportOverlayPosition";


/**
 * Toggle button for collapsing or expanding the right panel on touch devices.
 */
export class RightPanelToggleButton extends InteractionButtonBase {

	constructor() {
		const element = InteractionButtonBase.createButton({
			id: "right-panel-toggle",
			ariaLabel: "Przełącz prawy panel",
			title: "Przełącz prawy panel",
			ariaPressed: "false"
		});

		super(element);

		const listener = new ElementClickListener(this.componentElement);
		listener.onClick = () => UiStateStore.get().toggleRightPanel();

	}

	public setExpanded(expanded: boolean) {
		this.componentElement.classList.toggle("right-panel-toggle--active", expanded);
		this.componentElement.setAttribute("aria-pressed", expanded.toString());
	}

	/**
	 * Positions the toggle near the attack button when possible.
	 */
	public update(): void {
		const attackButton = document.getElementById("attack-button");
		const margin = 16;
		const separation = 12;
		const width = this.componentElement.offsetWidth || 48;
		const height = this.componentElement.offsetHeight || 48;
		const bounds = getViewportOverlayPosition({
			margin,
			elementWidth: width,
			elementHeight: height,
			offsetBottom: separation - margin,
			offsetRight: getMobileRightPanelCollapsedInset()
		});
		if (!bounds) {
			return;
		}

		let left = bounds.baseRight;
		let top = bounds.baseBottom;

		if (attackButton) {
			const attackRect = attackButton.getBoundingClientRect();
			left = Math.max(attackRect.left + bounds.scrollLeft, bounds.baseRight);
			top = attackRect.top + bounds.scrollTop - height - separation;

			const minTop = bounds.rect.top + bounds.scrollTop + margin;
			if (top < minTop) {
				top = bounds.baseBottom;
			}
		}

		const clampedLeft = Math.min(
			Math.max(left, bounds.safeLeft),
			bounds.safeRight < bounds.safeLeft ? bounds.safeLeft : bounds.safeRight
		);
		const clampedTop = Math.min(
			Math.max(top, bounds.safeTop),
			bounds.safeBottom < bounds.safeTop ? bounds.safeTop : bounds.safeBottom
		);

		this.componentElement.style.position = "absolute";
		this.componentElement.style.left = clampedLeft + "px";
		this.componentElement.style.top = clampedTop + "px";
	}
}
