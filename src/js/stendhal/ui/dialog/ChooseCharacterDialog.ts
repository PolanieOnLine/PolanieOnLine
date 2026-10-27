/***************************************************************************
 *                (C) Copyright 2015-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { DialogContentComponent } from "../toolkit/DialogContentComponent";

import { Client } from "../../Client";
import { SlashActionRepo } from "../../SlashActionRepo";
import { OutfitPreviewComponent } from "../component/OutfitPreviewComponent";
import { GameLanguage } from "../../util/GameLanguage";
import { createCharacterCard } from "./CharacterCard";


/**
 * a dialog to select your character from
 */
export class ChooseCharacterDialog extends DialogContentComponent {

	constructor(characters: any) {
		super("choose-character-template");
		this.componentElement.classList.add("character-selection");

		const characterList = this.child("#characters")!;
		for (var i in characters) {
			if (characters.hasOwnProperty(i)) {
				const attributes = characters[i]["a"];
				const preview = new OutfitPreviewComponent();
				preview.componentElement.removeAttribute("id");
				let outfit = attributes.outfit_ext_orig || attributes.outfit_ext;
				if (!outfit) {
					const legacy = Number(attributes.outfit) || 0;
					outfit = ["body", "dress", "head", "hair", "detail"]
						.map((part, index) => part + "=" + Math.floor(legacy / Math.pow(100, index)) % 100).join(",");
				}
				preview.setOutfit(outfit, attributes.outfit_colors);
				const button = createCharacterCard(attributes, preview.componentElement, name => {
					this.componentElement.dispatchEvent(new Event("close"));
					Client.get().chooseCharacter(name);
				}, text => GameLanguage.getText(text));
				characterList.append(button);
			}
		}

		this.child("#logout")!.addEventListener("click", (e: Event) => {
			this.componentElement.dispatchEvent(new Event("close"));
			this.onLogout();
		});
	}

	onLogout() {
		queueMicrotask(() => {
			// TODO: detect if not logged in via website & simply re-display login dialog
			SlashActionRepo.get().execute("/logout");
		});
	}
}

