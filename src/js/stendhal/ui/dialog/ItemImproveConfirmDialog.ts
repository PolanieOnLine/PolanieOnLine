/***************************************************************************
	*                      (C) Copyright 2024 - PolanieOnLine                 *
	***************************************************************************/
/***************************************************************************
	*                                                                         *
	*   This program is free software; you can redistribute it and/or modify  *
	*   it under the terms of the GNU Affero General Public License as        *
	*   published by the Free Software Foundation; either version 3 of the    *
	*   License, or (at your option) any later version.                       *
	*                                                                         *
	***************************************************************************/

import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

import { ItemImprovementEntry } from "./ItemImprovementTypes";

/**
	* Simple confirmation dialog before sending an improve request.
	*/
export class ItemImproveConfirmDialog extends DialogContentComponent {
constructor(entry: ItemImprovementEntry, npc: string, onConfirm: () => void) {
super("empty-div-template");
const frame = new FloatingWindow("Potwierdzenie", this, 0, 0);
this.setFrame(frame);

const body = document.createElement("div");
body.className = "improve-confirm";
const line1 = document.createElement("div");
line1.textContent = "Czy na pewno chcesz ulepszyć " + entry.name + " u " + npc + "?";
const line2 = document.createElement("div");
line2.textContent = "Szansa powodzenia: " + entry.chance.toFixed(2) + "%";
body.appendChild(line1);
body.appendChild(line2);
this.componentElement.appendChild(body);

const buttons = document.createElement("div");
buttons.className = "horizontalgroup dialogbuttons";
const yesButton = document.createElement("button");
yesButton.className = "dialogbutton";
yesButton.textContent = "Tak";
yesButton.addEventListener("click", () => {
onConfirm();
frame.close();
});
const noButton = document.createElement("button");
noButton.className = "dialogbutton";
noButton.textContent = "Nie";
noButton.addEventListener("click", () => {
frame.close();
});
buttons.appendChild(yesButton);
buttons.appendChild(noButton);
this.componentElement.appendChild(buttons);
}
}
