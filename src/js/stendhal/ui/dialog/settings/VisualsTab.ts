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

import { SettingsComponent } from "../../toolkit/SettingsComponent";
import { WidgetType } from "../../../data/enum/WidgetType";

import { singletons } from "../../../SingletonRepo";

import { StandardMessages } from "../../../util/StandardMessages";

import { ViewPort } from "../../ViewPort";


export class VisualsTab extends AbstractSettingsTab {

	constructor(parent: SettingsDialog, element: HTMLElement) {
		super(element);
		const config = singletons.getConfigManager();

		const col1 = this.child("#col1")!;

		parent.createCheckBox("chk_light", "effect.lighting",
				"Efekty świetlne są włączone", "Efekty świetlne są wyłączone", function() {
					StandardMessages.changeNeedsRefresh();
				});

		parent.createCheckBox("chk_weather", "effect.weather",
				"Efekty pogodowe są włączone", "Efekty pogodowe są wyłączone", function() {
					StandardMessages.changeNeedsRefresh();
				})!;

		parent.createCheckBox("chk_blood", "effect.blood",
				"Efekty krwi są włączone", "Efekty krwi są wyłączone");

		parent.createCheckBox("chk_nonude", "effect.no-nude",
				"Nadzy bohaterowie mają bieliznę", "Nadzy bohaterowie nie są zakryci");

		parent.createCheckBox("chk_shadows", "effect.shadows",
				"Cienie są włączone", "Cienie są wyłączone");

		parent.createCheckBox("chk_clickindicator", "click-indicator",
				"Wyświetlanie kliknięć", "Brak wyświetlania kliknięć");

		const chkAnimate = new SettingsComponent("chk_animate", "Animacja wskaźnika aktywności");
		chkAnimate.setConfigId("activity-indicator.animate");
		chkAnimate.setEnabled(config.getBoolean("activity-indicator"));
		chkAnimate.addListener((evt: Event) => {
			StandardMessages.changeNeedsRefresh();
			parent.refresh();
		});

		const chkActivityInd = new SettingsComponent("chk_activityindicator", "Wskaźnik aktywności obiektów");
		chkActivityInd.setConfigId("activity-indicator");
		chkActivityInd.setTooltip("Wyświetlaj wskaźnik nad interaktywnymi obiektami i ciałami,"
				+ " które nie są puste");
		chkActivityInd.addListener((evt: Event) => {
			chkAnimate.setEnabled(chkActivityInd.getValue() as boolean);
			StandardMessages.changeNeedsRefresh();
			parent.refresh();
		});
		chkActivityInd.addTo(col1);
		chkAnimate.addTo(col1);
		chkAnimate.componentElement.classList.add("indented");

		const chkParallax = new SettingsComponent("chk_parallax", "Paralaksa tła");
		chkParallax.setTooltip("Przewijanie paralaksy włączone", "Przewijanie paralaksy wyłączone");
		chkParallax.setConfigId("effect.parallax");
		chkParallax.addListener((evt: Event) => {
			StandardMessages.changeNeedsRefresh();
			parent.refresh();
		});
		chkParallax.addTo(col1);

		const chkEntityOverlay = new SettingsComponent("chk_entity_overlay",
				"Efekty nakładek na jednostkach");
		chkEntityOverlay.setValue(config.getBoolean("effect.entity-overlay"));
		chkEntityOverlay.addListener((evt: Event) => {
			config.set("effect.entity-overlay", chkEntityOverlay.getValue());
			StandardMessages.changeNeedsRefresh();
			parent.refresh();
		});
		chkEntityOverlay.addTo(col1);

		const fpsOptions = [
			{label: "Nieograniczone", value: 0},
			{label: "60 FPS", value: 60},
			{label: "90 FPS", value: 90},
			{label: "120 FPS", value: 120},
			{label: "144 FPS", value: 144}
		];
		const fpsSelect = new SettingsComponent("sel_fps_limit", "Liczba klatek na sekundę", WidgetType.SELECT);
		for (const option of fpsOptions) {
			fpsSelect.addOption(option.label, option.value.toString());
		}
		const currentLimit = Math.trunc(config.getFloat("loop.fps.limit", 0));
		let selectedIndex = fpsOptions.findIndex((opt) => opt.value === currentLimit);
		if (selectedIndex < 0) {
			selectedIndex = 0;
		}
		fpsSelect.setValue(selectedIndex);
		fpsSelect.setTooltip("Ogranicz renderowanie do określonego limitu klatek na sekundę", "Renderuj z pełną częstotliwością odświeżania");
		fpsSelect.addListener(() => {
			const idx = fpsSelect.getValue() as number;
			const choice = fpsOptions[idx] || fpsOptions[0];
			config.set("loop.fps.limit", choice.value.toString());
			ViewPort.get().setFpsLimit(choice.value);
			parent.refresh();
		});
		fpsSelect.addTo(col1);
	}
}
