/*******************************************************************************
 *                (C) Copyright 2024 - Polanie Online                         *
 *******************************************************************************
 *                                                                            *
 *   This program is free software; you can redistribute it and/or modify     *
 *   it under the terms of the GNU Affero General Public License as           *
 *   published by the Free Software Foundation; either version 3 of the       *
 *   License, or (at your option) any later version.                          *
 *                                                                            *
 *******************************************************************************/

import { GoldenCauldron } from "../../entity/GoldenCauldron";

import { ItemInventoryComponent } from "../component/ItemInventoryComponent";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

export class GoldenCauldronDialog extends DialogContentComponent {
	private mixComponent: ItemInventoryComponent;

	constructor(private cauldron: GoldenCauldron) {
		super("golden-cauldron-dialog-template");

		this.mixComponent = new ItemInventoryComponent(cauldron, "mix", 3, 2, false, undefined);
		this.child("#cauldron-slots")!.append(this.mixComponent.componentElement);
		this.child("#cauldron-mix")!.addEventListener("click", () => {
			this.cauldron.requestMix();
		});
	}

	public setHint(text: string) {
		const hint = this.child("#cauldron-status");
		if (hint) {
			hint.textContent = text;
		}
	}

	public override onParentClose() {
		this.mixComponent.onParentClose();
		this.cauldron.onDialogClosed();
	}
}
