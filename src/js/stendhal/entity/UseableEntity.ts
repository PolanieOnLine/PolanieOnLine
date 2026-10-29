/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { stendhal } from "../stendhal";

import { Entity } from "./Entity";
import { MenuItem } from "../action/MenuItem";
import { Paths } from "../data/Paths";
import { ItemUpgradeDialog } from "../ui/dialog/ItemUpgradeDialog";
import { ProductionDialog } from "../ui/dialog/ProductionDialog";


export class UseableEntity extends Entity {
	override zIndex = 3000
	action = "use";

    override getDefaultAction(): any {
        if (this["_name"] === "charcoal_mound") return ProductionDialog.openAction(Number(this["id"]));
        return this["_name"] === "rune_workbench" ? ItemUpgradeDialog.glyphOpenAction(Number(this["id"])) : super.getDefaultAction();
    }

	constructor() {
		super();
		this.sprite = {
			height: 32,
			width: 32
		};
	}

	override set(key: string, value: any) {
		super.set(key, value);
		if (key === "class" || key === "name") {
			this.sprite.filename = Paths.sprites + "/"
				+ this["class"] + "/" + this["_name"] + ".png";
		}
        if (key === "state" || key === "width" || key === "height") {
            this.sprite.width = Number(this["width"] || 1) * 32;
            this.sprite.height = Number(this["height"] || 1) * 32;
            this.sprite.offsetY = Number(this["state"] || 0) * this.sprite.height;
        }
	}

	override isVisibleToAction(_filter: boolean) {
		return true;
	}

	override buildActions(list: MenuItem[]) {
		super.buildActions(list);
        if (this["_name"] === "rune_workbench" || this["_name"] === "charcoal_mound") {
            for (const entry of list) if (entry.type === "use") entry.action = () => this.onclick(0, 0);
        }
		// FIXME: Java client adds "look" to Entity super class. Should this do the same? If so, there
		//        is an issue where certain entities, such as signs, end up with two options, "read" &
		//        "look" which do the same thing.
		const lookItem = {title: "Zobacz", type: "look"} as MenuItem;
		if (list.indexOf(lookItem) < 0) {
			list.push(lookItem);
		}
	}
}

