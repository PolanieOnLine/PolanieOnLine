/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { AbstractSettingsTab } from "./AbstractSettingsTab";

import { SettingsDialog } from "../SettingsDialog";

import { ui } from "../../UI";
import { UIComponentEnum } from "../../UIComponentEnum";

import { QuickMenuButton } from "../../quickmenu/QuickMenuButton";

import { singletons } from "../../../SingletonRepo";

export class InputTab extends AbstractSettingsTab {

	constructor(parent: SettingsDialog, element: HTMLElement) {
		super(element);


		/* *** pathfinding *** */

		parent.createCheckBox("chk_pathfinding", "pathfinding",
				"Kliknij/dotknij podłoża, aby chodzić", "Chodzenie po kliknięciu/dotknięcia podłoża wyłączone",
				function(e: Event) {
					// update quick menu button image
					(ui.get(UIComponentEnum.QMPathFinding) as QuickMenuButton).update();
				});

		parent.createCheckBox("chk_pathfindingmm", "pathfinding.minimap",
				"Kliknij/dotknij minimapy, aby chodzić", "Chodzenie po kliknięciu/dotknięcia minimapy wyłączone");


		/* *** joystick interface *** */

		// on-screen joystick
		const sel_joystick = parent.createSelectFromConfig("seljoystick", "joystick.style",
				undefined,
				function(e: Event) {
					singletons.getJoystickController().update();
				});
		const chk_joystick = parent.createCheckBox("chk_joystick", "joystick",
				undefined, undefined,
				function(e: Event) {
					sel_joystick.disabled = !chk_joystick.checked;
					singletons.getJoystickController().update();
				})!;
		chk_joystick.checked = singletons.getSessionManager().joystickEnabled();
		sel_joystick.disabled = !chk_joystick.checked;

		// attack button overlay
		const chk_attack_button = parent.createCheckBox("chk_attack_button", "attack.button",
				"Pokazuj przycisk ataku",
				"Ukryj przycisk ataku",
				function(_e: Event) {
					singletons.getAttackButtonController().update();
				});
		if (chk_attack_button) {
			chk_attack_button.checked = singletons.getSessionManager().attackButtonEnabled();
		}

		// loot button overlay
		const chk_loot_button = parent.createCheckBox("chk_loot_button", "loot.button",
				"Pokazuj przycisk łupów",
				"Ukryj przycisk łupów",
				function(_e: Event) {
					singletons.getLootButtonController().update();
				});
		if (chk_loot_button) {
			chk_loot_button.checked = singletons.getSessionManager().lootButtonEnabled();
		}

		parent.createCheckBox("chk_attack_players", "attack.target.players",
			"Zezwól na wybór graczy jako celów przy automatycznym ataku",
			"Wyklucz graczy z automatycznego wyboru celu");

		// joystck positioning
		const joystickInputs: HTMLInputElement[] = [];
		for (const o of ["x", "y"]) {
			const orienter = parent.createNumberInput("txtjoystick" + o,
				parseInt(parent.storedStates["txtjoystick" + o], 10),
				"Pozycja joysticka na osi " + o.toUpperCase());
			joystickInputs.push(orienter);
			orienter.addEventListener("input", (e) => {
				// update configuration
				singletons.getConfigManager().set("joystick.center." + o, orienter.value || 0);
				// update on-screen joystick position
				singletons.getJoystickController().update();
			});
		}
		const updateJoystickInputsState = (autoEnabled: boolean) => {
			for (const input of joystickInputs) {
				input.disabled = autoEnabled;
			}
		};
		const chk_autoposition = parent.createCheckBox("chk_joystick_auto", "joystick.autoposition",
			"Automatyczne dopasowanie joysticka do ekranu gry",
			"Ręczna konfiguracja pozycji joysticka",
			() => {
				updateJoystickInputsState(chk_autoposition!.checked);
				singletons.getJoystickController().update();
			});
		if (chk_autoposition) {
			updateJoystickInputsState(chk_autoposition.checked);
		}
	}
}
