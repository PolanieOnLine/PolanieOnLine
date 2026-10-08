import { AbstractSettingsTab } from "./AbstractSettingsTab";
import { AdminDiagnostics, DiagnosticSwitch } from "../../../util/AdminDiagnostics";
import { stendhal } from "../../../stendhal";
import { copyTextToClipboard } from "../../../util/Clipboard";

/** Controls only this client. No configuration storage or server actions. */
export class AdminDiagnosticsTab extends AbstractSettingsTab {
	constructor() {
		const element = document.createElement("div");
		element.className = "admin-diagnostic-settings pad-contents";
		super(element);
		const intro = document.createElement("p");
		intro.textContent = "Testy działają wyłącznie u Ciebie, do wyłączenia diagnostyki, zmiany postaci lub odświeżenia klienta. Nie zmieniają świata gry ani ustawień innych graczy.";
		element.appendChild(intro);
		const controls = new Map<DiagnosticSwitch, HTMLInputElement>();
		const refresh = () => {
			const allowed = AdminDiagnostics.isAdministrator();
			for (const [key, input] of controls) {
				input.checked = AdminDiagnostics.get(key);
				input.disabled = !allowed || (key !== "enabled" && !AdminDiagnostics.get("enabled"));
			}
		};
		const add = (key: DiagnosticSwitch, text: string) => {
			const label = document.createElement("label");
			const input = document.createElement("input");
			input.type = "checkbox";
			input.addEventListener("change", () => {
				AdminDiagnostics.set(key, input.checked);
				refresh();
			});
			label.append(input, document.createTextNode(text));
			element.appendChild(label);
			controls.set(key, input);
		};
		add("enabled", " Włącz lokalną diagnostykę i licznik płynności");
		add("freezeIcons", " Zatrzymaj animacje ikon w ekwipunku (nie w świecie gry)");
		add("simpleSlots", " Wyłącz maski, gradientowe ramki i cienie slotów");
		add("hideRarity", " Wyłącz obwódki rzadkości w ekwipunku");
		const hint = document.createElement("p");
		hint.textContent = "Porównaj po 10–15 sekund ruchu z zamkniętym i otwartym plecakiem. Zmieniaj jeden przełącznik naraz; każda zmiana zeruje pomiar. p95 to czas, którego nie przekracza 95% klatek — mniej znaczy płynniej.";
		element.appendChild(hint);
		const sampleLabel = document.createElement("input");
		sampleLabel.type = "text";
		sampleLabel.maxLength = 80;
		sampleLabel.placeholder = "Opis próbki, np. otwarty plecak, animacje wyłączone";
		sampleLabel.setAttribute("aria-label", "Opis próbki diagnostycznej");
		element.appendChild(sampleLabel);
		const output = document.createElement("textarea");
		output.readOnly = true;
		output.rows = 8;
		output.value = AdminDiagnostics.getReports();
		output.setAttribute("aria-label", "Zapisane próbki diagnostyczne");
		const copyStatus = document.createElement("p");
		copyStatus.setAttribute("role", "status");
		copyStatus.setAttribute("aria-live", "polite");
		const save = document.createElement("button");
		save.textContent = "Zapisz próbkę do raportu";
		save.addEventListener("click", () => {
			if (!AdminDiagnostics.get("enabled")) { refresh(); return; }
			AdminDiagnostics.saveReport(sampleLabel.value, stendhal.data.build);
			output.value = AdminDiagnostics.getReports();
			output.scrollTop = output.scrollHeight;
			copyStatus.textContent = "";
		});
		const clear = document.createElement("button");
		clear.textContent = "Zeruj pomiar";
		clear.addEventListener("click", () => AdminDiagnostics.clearMeasurements());
		const reset = document.createElement("button");
		reset.textContent = "Wyłącz testy i przywróć wygląd";
		reset.addEventListener("click", () => { AdminDiagnostics.reset(); refresh(); });
		const copy = document.createElement("button");
		copy.textContent = "Kopiuj raport";
		copy.addEventListener("click", async () => {
			const report = AdminDiagnostics.getReports();
			if (!report) {
				copyStatus.textContent = "Brak zapisanych próbek. Najpierw zapisz próbkę do raportu.";
				return;
			}
			copy.disabled = true;
			try {
				const copied = await copyTextToClipboard(report);
				copyStatus.textContent = copied ? "Raport skopiowany do schowka. Możesz go teraz wkleić."
					: "Przeglądarka zablokowała kopiowanie. Spróbuj w Chrome lub zaznacz raport i użyj menu kopiowania.";
			} finally { copy.disabled = false; }
		});
		const select = document.createElement("button");
		select.textContent = "Zaznacz raport do skopiowania";
		select.addEventListener("click", () => { output.focus(); output.select(); });
		for (const button of [save, clear, reset, copy, select]) { button.className = "dialogbutton"; }
		element.append(save, clear, reset, copy, select, copyStatus, output);
		refresh();
	}
}
