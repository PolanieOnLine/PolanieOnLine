/*******************************************************************************
 *                   (C) Copyright 2024 - Polanie Online                    *
 *******************************************************************************
 *                                                                            *
 *   This program is free software; you can redistribute it and/or modify     *
 *   it under the terms of the GNU Affero General Public License as           *
 *   published by the Free Software Foundation; either version 3 of the       *
 *   License, or (at your option) any later version.                          *
 *                                                                            *
 *******************************************************************************/

declare var marauroa: any;
declare var stendhal: any;

import { MenuItem } from "../action/MenuItem";

import { GoldenCauldronDialog } from "../ui/dialog/GoldenCauldronDialog";
import { FloatingWindow } from "../ui/toolkit/FloatingWindow";
import { Chat } from "../util/Chat";

import { UseableEntity } from "./UseableEntity";

export class GoldenCauldron extends UseableEntity {
	private maxDistToView = 4;
	private window?: FloatingWindow;
	private dialog?: GoldenCauldronDialog;

	public override buildActions(list: MenuItem[]) {
		super.buildActions(list);
		const that = this;
		if (this["open"]) {
			list.push({
				title: "Pokaż składniki",
				action: function (_entity: any) {
					that.checkOpenInventoryWindow();
				}
			});
			list.push({
				title: "Zamknij",
				action: function (_entity: any) {
					that.sendUseAction();
				}
			});
		} else {
			list.push({
				title: "Otwórz",
				action: function (_entity: any) {
					that.sendUseAction();
				}
			});
		}
	}

	public override draw(ctx: CanvasRenderingContext2D) {
		super.draw(ctx);
		if (this.window && this.window.isOpen() && !this.canViewContents()) {
			this.closeInventoryWindow();
		}
	}

	private canViewContents(): boolean {
		if (!marauroa.me) {
			return false;
		}
		return this.getDistanceTo(marauroa.me) <= this.maxDistToView;
	}

	public override set(key: string, value: any) {
		super.set(key, value);
		if (key === "open" && value !== undefined) {
			this.openInventoryWindow();
		}
	}

	public override unset(key: string) {
		super.unset(key);
		if (key === "open") {
			this.closeInventoryWindow();
		}
	}

	public override destroy(parent: any) {
		this.closeInventoryWindow();
		super.destroy(parent);
	}

	public override onclick(_x: number, _y: number) {
		if (marauroa.me && marauroa.me.isNextTo(this)) {
			this.sendUseAction();
		} else if (this["open"]) {
			this.checkOpenInventoryWindow();
		}
	}

	public requestMix() {
		const action = {
			"type": "golden_cauldron",
			"action": "mix",
			"zone": marauroa.currentZoneName,
			"target_path": this.getIdPath()
		};
		marauroa.clientFramework.sendAction(action);
	}

	public onDialogClosed() {
		if (this["open"] !== undefined) {
			const action = {
				"type": "golden_cauldron",
				"action": "close",
				"zone": marauroa.currentZoneName,
				"target_path": this.getIdPath()
			};
			marauroa.clientFramework.sendAction(action);
		}
		if (this.window && this.window.isOpen()) {
			this.window.close();
		}
		this.window = undefined;
		this.dialog = undefined;
	}

	private sendUseAction() {
		const action = {
			"type": "use",
			"target_path": this.getIdPath(),
			"zone": marauroa.currentZoneName
		};
		marauroa.clientFramework.sendAction(action);
	}

	private openInventoryWindow() {
		if (!this.window || !this.window.isOpen()) {
			this.dialog = new GoldenCauldronDialog(this);
			this.dialog.setHint("Przeciągnij składniki do slotów i naciśnij 'Mieszaj'.");
			const dstate = stendhal.config.getWindowState("golden-cauldron");
			this.window = new FloatingWindow("Kocioł Draconii", this.dialog, dstate.x, dstate.y);
			this.window.setId("golden-cauldron");
		}
	}

	private checkOpenInventoryWindow() {
		if (this.canViewContents()) {
			this.openInventoryWindow();
		} else {
			Chat.log("client", "Kocioł jest za daleko.");
		}
	}

	private closeInventoryWindow() {
		if (this.window && this.window.isOpen()) {
			this.window.close();
		}
		this.window = undefined;
		this.dialog = undefined;
	}
}
