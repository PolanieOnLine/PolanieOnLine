/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/

import { RPEvent } from "marauroa";
import { ItemUpgradeDialog } from "../ui/dialog/ItemUpgradeDialog";

/** Opens or refreshes the web Item Upgrades 2.0 dialog. */
export class ItemUpgradeEvent extends RPEvent {
	public execute(_entity: any): void {
		ItemUpgradeDialog.show(this as any);
	}
}
