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

import { RPEvent } from "./RPEvent";
import { ImproverDialog, ImproverOfferPayload } from "../ui/dialog/ImproverDialog";

declare var marauroa: any;

export class ImproverOfferEvent extends RPEvent {
	public payload?: string;

	public execute(entity: any): void {
		if (entity !== marauroa.me) {
			return;
		}

		const payloadText = this["payload"];
		if (!payloadText) {
			return;
		}

		let payload: ImproverOfferPayload;
		try {
			payload = JSON.parse(payloadText);
		} catch (error) {
			console.warn("ImproverOfferEvent: invalid payload", error);
			return;
		}

		if (payload.action === "close") {
			ImproverDialog.closeActiveInstance();
			return;
		}

		if (!payload.item || !payload.resources) {
			return;
		}

		ImproverDialog.showOffer(payload);
	}
}
