/***************************************************************************
 *                   (C) Copyright 2024 - Stendhal                        *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { UIComponentEnum } from "../UIComponentEnum";
import { ui } from "../UI";

declare var stendhal: any;

interface ImproverResource {
	name: string;
	required: number;
	owned: number;
}

export interface ImproverOfferPayload {
	action?: string;
	item: string;
	level: number;
	chance: number;
	cost?: number;
	costText?: string;
	resources: ImproverResource[];
}

export class ImproverDialog extends DialogContentComponent {
	private static activeInstance?: ImproverDialog;

	private readonly itemName: HTMLElement;
	private readonly itemLevel: HTMLElement;
	private readonly chance: HTMLElement;
	private readonly cost: HTMLElement;
	private readonly resourcesList: HTMLElement;

	private constructor() {
		super("improverdialog-template");
		ui.registerComponent(UIComponentEnum.ImproverDialog, this);

		this.itemName = this.child(".improver-dialog__item-name") as HTMLElement;
		this.itemLevel = this.child(".improver-dialog__item-level") as HTMLElement;
		this.chance = this.child("[data-improver-chance]") as HTMLElement;
		this.cost = this.child("[data-improver-cost]") as HTMLElement;
		this.resourcesList = this.child(".improver-dialog__resources-list") as HTMLElement;

		this.addCloseButton("improver-close");
	}

	public setOffer(data: ImproverOfferPayload) {
		this.itemName.textContent = data.item || "-";
		this.itemLevel.textContent = data.level ? `+${data.level}` : "-";
		this.chance.textContent = Number.isFinite(data.chance) ? `${data.chance}%` : "-";

		if (data.costText) {
			this.cost.textContent = data.costText;
		} else if (typeof data.cost === "number") {
			this.cost.textContent = data.cost.toString();
		} else {
			this.cost.textContent = "-";
		}

		this.resourcesList.innerHTML = "";
		for (const resource of data.resources || []) {
			const row = document.createElement("div");
			row.classList.add("improver-dialog__resource-row");
			if (resource.owned >= resource.required) {
				row.classList.add("improver-dialog__resource--ok");
			} else {
				row.classList.add("improver-dialog__resource--missing");
			}

			const name = document.createElement("span");
			name.classList.add("improver-dialog__resource-name");
			name.textContent = resource.name;

			const amount = document.createElement("span");
			amount.classList.add("improver-dialog__resource-amount");
			amount.textContent = `${resource.owned} / ${resource.required}`;

			row.append(name, amount);
			this.resourcesList.appendChild(row);
		}
	}

	public override onParentClose() {
		ui.unregisterComponent(this);
		ImproverDialog.activeInstance = undefined;
	}

	public static showOffer(data: ImproverOfferPayload) {
		let dialog = ui.get(UIComponentEnum.ImproverDialog) as ImproverDialog;

		if (!dialog) {
			const dstate = stendhal.config.getWindowState("improver");
			dialog = new ImproverDialog();
			const frame = new FloatingWindow("Ulepszenia", dialog, dstate.x, dstate.y);
			frame.setId("improver");
			dialog.setFrame(frame);
			ImproverDialog.activeInstance = dialog;
		}

		dialog.setOffer(data);
	}

	public static closeActiveInstance() {
		if (ImproverDialog.activeInstance) {
			ImproverDialog.activeInstance.close();
		}
	}
}
