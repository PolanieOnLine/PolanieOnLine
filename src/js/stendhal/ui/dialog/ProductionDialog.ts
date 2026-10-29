import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";
import { GameLanguage } from "../../util/GameLanguage";
import { getItemSlotLayers } from "../component/ItemSlotPresentation";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

type ProductionData = {[key: string]: any};
const text = (value: string): string => GameLanguage.getText(value);

/** Uses the producer's persisted order; the client clock only draws progress. */
export class ProductionDialog extends DialogContentComponent {
    private static active?: ProductionDialog;
    private static readonly seen = new Set<string>();
    private static pendingOpen?: string;
    private static pendingZone?: string;
    private static openSequence = 0;
    private readonly product = document.createElement("div");
    private readonly name = document.createElement("strong");
    private readonly recipes = document.createElement("select");
    private selectedRecipe?: string;
    private recipeCatalogKey = "";
    private readonly amount = document.createElement("input");
    private readonly materials = document.createElement("div");
    private readonly duration = document.createElement("div");
    private readonly orderLabel = document.createElement("div");
    private readonly remaining = document.createElement("div");
    private readonly progress = document.createElement("progress");
    private readonly status = document.createElement("div");
    private readonly start: HTMLButtonElement;
    private readonly collect: HTMLButtonElement;
    private readonly sprites = new Map<HTMLElement, string>();
    private readonly zone = marauroa.currentZoneName;
    private data?: ProductionData;
    private sequence = 0;
    private lastRequest = "";
    private busy = false;
    private readyAt = 0;
    private preview?: ReturnType<typeof setTimeout>;
    private polling?: ReturnType<typeof setInterval>;
    private animation?: ReturnType<typeof setInterval>;

    private constructor() {
        super("empty-div-template");
        this.componentElement.classList.add("production-dialog");
        const identity = document.createElement("div"); identity.className = "production-identity";
        this.product.className = "itemSlot";
        this.recipes.className = "production-recipes"; this.recipes.hidden = true;
        this.recipes.setAttribute("aria-label", text("Produkt"));
        identity.append(this.product, this.name, this.recipes); this.componentElement.appendChild(identity);

        const body = document.createElement("div"); body.className = "production-body";
        this.componentElement.appendChild(body);

        const controls = document.createElement("div");
        const quantities = document.createElement("div"); quantities.className = "production-quantity";
        const label = document.createElement("label"); label.textContent = text("Ilość:");
        this.amount.type = "number"; this.amount.min = "1"; this.amount.max = "1000"; this.amount.value = "1";
        this.amount.setAttribute("aria-label", text("Ilość:"));
        label.appendChild(this.amount); quantities.appendChild(label);
        for (const count of [1, 10, 100]) {
            const button = document.createElement("button"); button.textContent = String(count);
            button.addEventListener("click", () => {
                const step = Number(this.amount.step) || 1;
                this.amount.value = String(Math.max(step, Math.min(Math.ceil(count / step) * step, Number(this.amount.max))));
                this.quantityChanged();
            });
            quantities.appendChild(button);
        }
        controls.append(quantities, this.duration);
        body.appendChild(this.section("Zamówienie", controls));
        this.materials.className = "production-materials";
        body.appendChild(this.section("Składniki (masz / potrzeba)", this.materials));
        const order = document.createElement("div"); order.className = "production-order";
        this.progress.max = 1000; order.append(this.orderLabel, this.progress, this.remaining);
        body.appendChild(this.section("Bieżące zamówienie", order));
        this.status.className = "production-status"; this.status.setAttribute("aria-live", "polite");
        this.componentElement.appendChild(this.status);
        this.start = this.addButton(text("Zleć produkcję"), () => this.send("start"));
        this.collect = this.addButton(text("Odbierz zamówienie"), () => this.send("collect"));
        this.amount.addEventListener("input", () => this.quantityChanged());
        this.amount.addEventListener("change", () => this.quantityChanged());
        this.recipes.addEventListener("change", () => {
            if (!this.data || this.busy) return;
            this.selectedRecipe = this.recipes.value; this.lastRequest = "";
            if (this.preview !== undefined) clearTimeout(this.preview);
            this.start.disabled = true; this.collect.disabled = true; this.send("refresh");
        });
    }

    public static openAction(npcId: number): ProductionData {
        this.pendingOpen = Date.now() + ":" + ++this.openSequence;
        this.pendingZone = marauroa.currentZoneName;
        return {type: "production_action", command: "open", npc_id: String(npcId), request_id: this.pendingOpen};
    }

    public static show(data: ProductionData): void {
        const opening = data.phase === "open";
        if (opening) {
            if (data.request_id && data.request_id !== this.pendingOpen) return;
            if (this.seen.has(data.window_id)) return;
            this.seen.add(data.window_id);
            if (this.seen.size > 64) this.seen.delete(this.seen.values().next().value!);
            this.pendingOpen = undefined;
            this.active?.close();
            const dialog = new ProductionDialog();
            const frame = new FloatingWindow(text("Produkcja") + " · " + data.npc_name, dialog, 24, 24);
            frame.setId("production"); dialog.setFrame(frame); this.active = dialog;
        }
        const dialog = this.active;
        if (!dialog || (!opening && (data.window_id !== dialog.data?.window_id || data.request_id !== dialog.lastRequest))) return;
        if (data.status === "too_far") { dialog.close(); return; }
        dialog.apply(data);
        if (opening) {
            dialog.polling = setInterval(() => { if (!dialog.busy) dialog.send("refresh"); }, 2000);
            dialog.animation = setInterval(() => { dialog.animate(); dialog.updateClock(); }, 100);
        }
    }

    public static onZoneChanged(): void {
        if (!marauroa.me || this.pendingZone !== marauroa.currentZoneName) this.pendingOpen = undefined;
        if (this.active && (!marauroa.me || this.active.zone !== marauroa.currentZoneName)) this.active.close();
    }

    public override onParentClose(): void {
        ProductionDialog.pendingOpen = undefined;
        if (this.preview !== undefined) clearTimeout(this.preview);
        if (this.polling !== undefined) clearInterval(this.polling);
        if (this.animation !== undefined) clearInterval(this.animation);
        this.send("close"); this.sprites.clear(); ProductionDialog.active = undefined;
    }

    private apply(data: ProductionData): void {
        this.data = data; this.busy = false;
        this.name.textContent = text(data.name);
        this.selectedRecipe = data.selected_recipe;
        const ids = this.list(data.recipe_ids), names = this.list(data.recipe_names);
        const key = JSON.stringify([ids, names]);
        if (key !== this.recipeCatalogKey) {
            this.recipeCatalogKey = key; this.recipes.replaceChildren();
            ids.forEach((id, index) => { const option = document.createElement("option"); option.value = id; option.textContent = text(names[index]); this.recipes.appendChild(option); });
        }
        this.recipes.value = this.selectedRecipe || ""; this.recipes.hidden = ids.length <= 1;
        this.recipes.disabled = false; this.name.hidden = ids.length > 1;
        this.amount.min = this.amount.step = String(data.amount_step || 1);
        this.amount.max = String(data.max_amount); this.amount.value = String(data.amount);
        this.readyAt = Date.now() + Number(data.remaining) * 1000;
        this.updateRequirements(); this.updateClock();
        this.status.textContent = text(data.message || "");
        this.status.classList.toggle("production-missing", data.status !== "ok");
        this.start.disabled = !Number(data.can_start);
        this.collect.disabled = data.order_state !== "ready";
    }

    private quantityChanged(): void {
        if (!this.data || this.busy) return;
        this.lastRequest = "";
        this.start.disabled = true;
        this.updateRequirements();
        if (this.preview !== undefined) clearTimeout(this.preview);
        this.preview = setTimeout(() => { this.preview = undefined; this.send("refresh"); }, 180);
    }

    private updateRequirements(): void {
        const data = this.data!;
        const amount = Number(this.amount.value);
        const step = Number(data.amount_step) || 1;
        const valid = Number.isInteger(amount) && amount >= step && amount <= Number(data.max_amount) && amount % step === 0;
        const count = valid ? amount : 0;
        this.setSlot(this.product, data.class, data.subclass, count);
        this.duration.textContent = valid ? text("Czas produkcji:") + " " + this.formatTime(Number(data.duration) / Number(data.amount) * count)
                : text("Wybierz poprawną ilość.");
        const names = this.list(data.material_names), classes = this.list(data.material_classes), subclasses = this.list(data.material_subclasses);
        this.materials.style.gridTemplateColumns = `repeat(${Math.max(1, Math.min(3, names.length))}, minmax(0, 1fr))`;
        const required = this.list(data.material_values), owned = this.list(data.owned_material_values);
        this.materials.replaceChildren();
        names.forEach((name, index) => {
            const need = Math.round(Number(required[index]) / Number(data.amount) * count), have = Number(owned[index]);
            const card = document.createElement("div"); card.className = "production-material";
            const slot = document.createElement("div"); slot.className = "itemSlot";
            this.setSlot(slot, classes[index], subclasses[index], need);
            const label = document.createElement("div"); label.textContent = text(name);
            const values = document.createElement("div"); values.textContent = have + " / " + need;
            values.className = have >= need ? "production-available" : "production-missing";
            card.append(slot, label, values); this.materials.appendChild(card);
        });
        this.animate();
    }

    private updateClock(): void {
        if (!this.data) return;
        const active = this.data.order_state !== "idle";
        const left = Math.max(0, Math.ceil((this.readyAt - Date.now()) / 1000));
        this.orderLabel.textContent = active ? text(this.data.order_name || this.data.name) + ": " + this.data.order_amount : text("Brak aktywnego zamówienia.");
        this.remaining.textContent = !active ? text("Możesz odejść. Produkcja trwa również poza grą.")
                : left === 0 ? text("Gotowe do odbioru.") : text("Pozostało:") + " " + this.formatTime(left);
        this.progress.value = !active ? 0 : 1000 * Math.max(0, Number(this.data.order_duration) - left) / Math.max(1, Number(this.data.order_duration));
    }

    private setSlot(slot: HTMLElement, itemClass: string, subclass: string, count: number): void {
        const layers = getItemSlotLayers(slot);
        const filename = `${Paths.sprites}/items/${itemClass}/${subclass}.png`;
        layers.icon.style.backgroundImage = `url(${singletons.getSpriteStore().checkPath(filename)})`;
        layers.quantityLabel.textContent = count > 1 ? String(count) : "";
        this.sprites.set(layers.icon, filename);
    }

    private animate(): void {
        for (const [icon, filename] of this.sprites) {
            if (icon.isConnected === false) { this.sprites.delete(icon); continue; }
            const sheet = singletons.getSpriteStore().get(filename);
            const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
            icon.style.backgroundPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
        }
    }

    private send(command: string): void {
        if (!this.data) return;
        if (command === "start" || command === "collect") {
            this.busy = true; this.start.disabled = true; this.collect.disabled = true;
            this.recipes.disabled = true;
            if (this.preview !== undefined) clearTimeout(this.preview);
        }
        this.lastRequest = String(++this.sequence);
        marauroa.clientFramework.sendAction({type: "production_action", command, npc_id: String(this.data.npc_id),
            window_id: this.data.window_id, request_id: this.lastRequest, amount: this.amount.value,
            ...(this.selectedRecipe ? {recipe: this.selectedRecipe} : {})});
    }

    private section(title: string, body: HTMLElement): HTMLElement {
        const fieldset = document.createElement("fieldset"), legend = document.createElement("legend");
        legend.textContent = text(title); fieldset.append(legend, body); return fieldset;
    }

    private list(value: unknown): string[] {
        return Array.isArray(value) ? value.map(String) : typeof value === "string" ? value.substring(1, value.length - 1).split(/\t/).filter(Boolean) : [];
    }

    private formatTime(seconds: number): string {
        return seconds >= 3600 ? Math.floor(seconds / 3600) + " h " + Math.floor(seconds % 3600 / 60) + " min"
                : seconds >= 60 ? Math.floor(seconds / 60) + " min " + seconds % 60 + " s" : seconds + " s";
    }
}
