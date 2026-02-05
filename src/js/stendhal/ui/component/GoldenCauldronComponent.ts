/***************************************************************************
 *                   (C) Copyright 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Component } from "../toolkit/Component";
import { ItemInventoryComponent } from "./ItemInventoryComponent";

import { stendhal } from "../../stendhal";

const SECOND_MS = 1000;

export class GoldenCauldronComponent extends Component {
	private readonly statusLabel: HTMLDivElement;
	private readonly mixButton: HTMLButtonElement;
	private readonly inventory: ItemInventoryComponent;
	private baseStatus = "";
	private readyAt = 0;
	private countdownId?: number;
	private readonly onMix: () => void;
	private readonly onClosed: () => void;

	constructor(object: any, slot: string, onMix: () => void, onClosed: () => void) {
		const root = document.createElement("div");
		root.classList.add("golden-cauldron");
		super(root, true);

		this.onMix = onMix;
		this.onClosed = onClosed;

		this.statusLabel = document.createElement("div");
		this.statusLabel.classList.add("golden-cauldron__status");
		this.statusLabel.textContent = "Kocioł nie pracuje.";

		this.inventory = new ItemInventoryComponent(
			object,
			slot,
			4,
			2,
			stendhal.config.getBoolean("inventory.quick-pickup"),
			undefined,
		);
		this.inventory.componentElement.classList.add("golden-cauldron__inventory");

		this.mixButton = document.createElement("button");
		this.mixButton.type = "button";
		this.mixButton.classList.add("golden-cauldron__mix");
		this.mixButton.textContent = "Mieszaj";
		this.mixButton.disabled = true;
		this.mixButton.addEventListener("click", () => {
			if (!this.mixButton.disabled) {
				this.onMix();
			}
		});

		this.componentElement.append(this.statusLabel);
		this.componentElement.append(this.inventory.componentElement);
		this.componentElement.append(this.mixButton);
	}

	public setMixEnabled(enabled: boolean) {
		this.mixButton.disabled = !enabled;
	}

	public updateStatus(text: string, readyAt: number) {
		this.baseStatus = text || "";
		this.readyAt = readyAt;
		this.restartCountdown();
	}

	public markDirty() {
		this.inventory.markDirty();
	}

	public update() {
		this.inventory.update();
	}

	public override onParentClose() {
		this.stopCountdown();
		this.inventory.onParentClose();
		this.onClosed();
	}

	private restartCountdown() {
		this.stopCountdown();
		if (this.readyAt > Date.now()) {
			this.countdownId = window.setInterval(() => {
				this.refreshStatusLabel();
			}, SECOND_MS);
			this.refreshStatusLabel();
		} else {
			this.statusLabel.textContent = this.baseStatus;
		}
	}

	private refreshStatusLabel() {
		const now = Date.now();
		if (this.readyAt <= now) {
			this.stopCountdown();
			this.statusLabel.textContent = this.baseStatus;
			return;
		}

		const remainingMillis = this.readyAt - now;
		const totalSeconds = Math.max(0, Math.floor(remainingMillis / SECOND_MS));
		const minutes = Math.floor(totalSeconds / 60);
		const seconds = totalSeconds % 60;
		const parts: string[] = [];
		if (this.baseStatus) {
			parts.push(this.baseStatus);
		}
		if (minutes > 0) {
			const paddedMinutes = minutes < 10 ? `0${minutes}` : `${minutes}`;
			const paddedSeconds = seconds < 10 ? `0${seconds}` : `${seconds}`;
			parts.push(`Pozostało ${paddedMinutes}:${paddedSeconds}.`);
		} else {
			parts.push(`Pozostało ${seconds} s.`);
		}
		this.statusLabel.textContent = parts.join(" ");
	}

	private stopCountdown() {
		if (this.countdownId !== undefined) {
			window.clearInterval(this.countdownId);
			this.countdownId = undefined;
		}
	}
}
