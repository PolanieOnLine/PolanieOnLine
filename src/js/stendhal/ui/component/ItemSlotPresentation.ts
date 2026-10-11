/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/

import { Paths } from "../../data/Paths";

export interface ItemSlotLayers {
	icon: HTMLElement;
	quantityLabel: HTMLElement;
}

const slots = new WeakMap<HTMLElement, ItemSlotLayers>();

/** Keep the shared, static slot texture separate from animated sprite sheets. */
export function getItemSlotLayers(element: HTMLElement): ItemSlotLayers {
	const previous = slots.get(element);
	if (previous?.icon.parentElement === element && previous.quantityLabel.parentElement === element) {
		return previous;
	}
	const icon = document.createElement("span");
	icon.className = "item-slot-icon";
	icon.setAttribute("aria-hidden", "true");
	const quantity = document.createElement("span");
	quantity.className = "item-slot-quantity";
	element.replaceChildren(icon, quantity);
	element.style.setProperty("--item-slot-texture", `url(${Paths.gui}/slot.png)`);
	const layers = { icon, quantityLabel: quantity };
	slots.set(element, layers);
	return layers;
}
