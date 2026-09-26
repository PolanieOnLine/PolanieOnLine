import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

type ShopEventData = {[key: string]: any};

interface ShopEntry {
    name: string;
    price: string;
    itemClass: string;
    subclass: string;
    stackable: boolean;
}

/** Okno zakupow i skupu, zasilane katalogiem wyslanym przez serwer. */
export class NpcShopDialog extends DialogContentComponent {
    private static active?: NpcShopDialog;

    private readonly heading = document.createElement("h3");
    private readonly wallet = document.createElement("div");
    private readonly search = document.createElement("input");
    private readonly buyTab = document.createElement("button");
    private readonly sellTab = document.createElement("button");
    private readonly listBox = document.createElement("div");
    private readonly detailIcon = document.createElement("img");
    private readonly detailName = document.createElement("h4");
    private readonly detailPrice = document.createElement("div");
    private readonly quantity = document.createElement("input");
    private readonly status = document.createElement("div");
    private readonly requestButton: HTMLButtonElement;
    private readonly confirmButton: HTMLButtonElement;
    private readonly cancelButton: HTMLButtonElement;

    private npcId = 0;
    private mode: "buy" | "sell" = "buy";
    private selling: ShopEntry[] = [];
    private buying: ShopEntry[] = [];
    private selected?: ShopEntry;
    private token?: string;
    private pendingMode?: string;
    private pendingItem?: string;
    private pendingAmount = 0;

    private constructor() {
        super("empty-div-template");
        this.componentElement.classList.add("npc-shop-dialog");
        const header = document.createElement("div");
        header.className = "npc-shop-header";
        header.append(this.heading, this.wallet);
        this.componentElement.appendChild(header);

        this.search.type = "search";
        this.search.placeholder = "Szukaj przedmiotu";
        this.search.setAttribute("aria-label", "Szukaj przedmiotu");
        this.search.addEventListener("input", () => this.drawList());
        this.componentElement.appendChild(this.search);

        const tabs = document.createElement("div");
        tabs.className = "npc-shop-tabs";
        this.buyTab.textContent = "Kup";
        this.sellTab.textContent = "Sprzedaj";
        this.buyTab.onclick = () => this.changeMode("buy");
        this.sellTab.onclick = () => this.changeMode("sell");
        tabs.append(this.buyTab, this.sellTab);
        this.componentElement.appendChild(tabs);

        const content = document.createElement("div");
        content.className = "npc-shop-layout";
        this.listBox.className = "npc-shop-list";
        this.listBox.setAttribute("role", "listbox");
        content.appendChild(this.listBox);

        const detail = document.createElement("div");
        detail.className = "npc-shop-detail";
        const imageHolder = document.createElement("div");
        imageHolder.className = "npc-shop-icon";
        this.detailIcon.alt = "";
        imageHolder.appendChild(this.detailIcon);
        this.detailName.textContent = "Wybierz przedmiot";
        this.quantity.type = "number";
        this.quantity.min = "1";
        this.quantity.max = "1000";
        this.quantity.value = "1";
        this.quantity.setAttribute("aria-label", "Ilość");
        const qtyLabel = document.createElement("label");
        qtyLabel.textContent = "Ilość";
        qtyLabel.appendChild(this.quantity);
        detail.append(imageHolder, this.detailName, this.detailPrice, qtyLabel);
        content.appendChild(detail);
        this.componentElement.appendChild(content);

        this.status.className = "npc-shop-status";
        this.status.setAttribute("aria-live", "polite");
        this.componentElement.appendChild(this.status);

        this.requestButton = this.addButton("Zapytaj o cenę", () => this.ask());
        this.confirmButton = this.addButton("Potwierdź", () => this.answer(true));
        this.cancelButton = this.addButton("Anuluj", () => this.answer(false));
        this.addButton("Odśwież", () => this.send("refresh"));
        this.addCloseButton();
        this.updateButtons();
    }

    public static show(data: ShopEventData): void {
        if (!NpcShopDialog.active) {
            if (data.phase !== "open") return;
            const dialog = new NpcShopDialog();
            const frame = new FloatingWindow("Sklep", dialog, 32, 32);
            frame.setId("npc-shop");
            dialog.setFrame(frame);
            NpcShopDialog.active = dialog;
        }
        const dialog = NpcShopDialog.active;
        if (dialog.npcId && data.npc_id !== undefined
                && dialog.npcId !== Number(data.npc_id)
                && data.phase !== "open") return;
        dialog.apply(data);
    }

    public override onParentClose(): void {
        this.send("close");
        NpcShopDialog.active = undefined;
    }

    private apply(data: ShopEventData): void {
        this.npcId = Number(data.npc_id || 0);
        this.heading.textContent = String(data.npc_name || "Sklep");
        this.wallet.textContent = "Posiadasz: " + String(data.owned_money_text || "0");
        this.selling = this.entries(data, "sell");
        this.buying = this.entries(data, "buy");
        this.buyTab.disabled = !this.selling.length;
        this.sellTab.disabled = !this.buying.length;
        if (this.mode === "buy" && !this.selling.length && this.buying.length) {
            this.mode = "sell";
        } else if (this.mode === "sell" && !this.buying.length && this.selling.length) {
            this.mode = "buy";
        }
        this.token = data.request_token ? String(data.request_token) : undefined;
        this.pendingMode = data.pending_mode ? String(data.pending_mode) : undefined;
        this.pendingItem = data.pending_item ? String(data.pending_item) : undefined;
        this.pendingAmount = Number(data.pending_amount || 0);
        const previouslySelected = this.selected?.name;
        this.selected = this.current().find(item => item.name === previouslySelected);
        this.drawList();
        if (data.phase === "offer" && this.token) {
            this.status.textContent = "Oferta: " + this.pendingAmount + " x "
                + this.pendingItem + ", " + String(data.pending_price_text)
                + ". Potwierdź lub anuluj.";
        } else {
            this.status.textContent = String(data.message
                    || "Wybierz przedmiot i zapytaj handlarza o cenę.");
        }
        this.updateButtons();
    }

    private entries(data: ShopEventData, prefix: string): ShopEntry[] {
        const names = this.asList(data[prefix + "_names"]);
        const prices = this.asList(data[prefix + "_prices"]);
        const classes = this.asList(data[prefix + "_classes"]);
        const subclasses = this.asList(data[prefix + "_subclasses"]);
        const stackable = this.asList(data.sell_stackable);
        return names.map((name, index) => ({
            name,
            price: prices[index] || "",
            itemClass: classes[index] || "",
            subclass: subclasses[index] || "",
            stackable: prefix === "buy" || stackable[index] === "1"
        }));
    }

    private asList(value: unknown): string[] {
        if (Array.isArray(value)) return value.map(String);
        if (typeof value !== "string" || value.length < 2) return [];
        return value.substring(1, value.length - 1).split(/\t/);
    }

    private current(): ShopEntry[] {
        return this.mode === "buy" ? this.selling : this.buying;
    }

    private changeMode(mode: "buy" | "sell"): void {
        this.mode = mode;
        this.selected = undefined;
        this.drawList();
    }

    private drawList(): void {
        this.buyTab.classList.toggle("active", this.mode === "buy");
        this.sellTab.classList.toggle("active", this.mode === "sell");
        const filter = this.search.value.trim().toLocaleLowerCase();
        this.listBox.replaceChildren();
        for (const entry of this.current()) {
            if (!entry.name.toLocaleLowerCase().includes(filter)) continue;
            const row = document.createElement("button");
            row.type = "button";
            row.className = "npc-shop-row";
            row.setAttribute("role", "option");
            row.setAttribute("aria-selected", String(entry === this.selected));
            if (entry === this.selected) row.classList.add("selected");
            if (entry.itemClass && entry.subclass) {
                const icon = document.createElement("img");
                icon.src = Paths.sprites + "/items/" + entry.itemClass
                        + "/" + entry.subclass + ".png";
                icon.alt = "";
                icon.onerror = () => { icon.style.visibility = "hidden"; };
                row.appendChild(icon);
            } else {
                const placeholder = document.createElement("span");
                placeholder.className = "npc-shop-icon-placeholder";
                row.appendChild(placeholder);
            }
            const description = document.createElement("span");
            const label = document.createElement("strong");
            label.textContent = entry.name;
            const price = document.createElement("small");
            price.textContent = entry.price;
            description.append(label, price);
            row.appendChild(description);
            row.onclick = () => {
                this.selected = entry;
                this.drawList();
            };
            this.listBox.appendChild(row);
        }
        this.drawDetails();
    }

    private drawDetails(): void {
        const entry = this.selected;
        this.detailName.textContent = entry?.name || "Wybierz przedmiot";
        this.detailPrice.textContent = entry
                ? (this.mode === "buy" ? "Cena: " : "Cena bazowa: ") + entry.price
                : "";
        this.detailIcon.style.visibility = entry?.itemClass && entry?.subclass
                ? "visible" : "hidden";
        if (entry?.itemClass && entry.subclass) {
            this.detailIcon.src = Paths.sprites + "/items/"
                    + entry.itemClass + "/" + entry.subclass + ".png";
        }
        this.quantity.max = entry && !entry.stackable ? "1" : "1000";
        if (entry && !entry.stackable) this.quantity.value = "1";
        this.updateButtons();
    }

    private updateButtons(): void {
        this.requestButton.disabled = !this.selected || !!this.token;
        this.confirmButton.disabled = !this.token;
        this.cancelButton.disabled = !this.token;
    }

    private ask(): void {
        if (!this.selected || this.token) return;
        const input = Number(this.quantity.value);
        const max = this.selected.stackable ? 1000 : 1;
        if (!Number.isInteger(input) || input < 1 || input > max) {
            this.status.textContent = "Wybierz poprawną ilość.";
            return;
        }
        this.requestButton.disabled = true;
        this.status.textContent = "Czekam na ofertę handlarza.";
        this.send("request", this.mode, this.selected.name, input);
    }

    private answer(confirm: boolean): void {
        if (!this.token) return;
        this.confirmButton.disabled = true;
        this.cancelButton.disabled = true;
        this.status.textContent = "Czekam na odpowiedź handlarza.";
        this.send(confirm ? "confirm" : "cancel",
                this.pendingMode, this.pendingItem, this.pendingAmount, this.token);
    }

    private send(command: string, mode?: string, item?: string,
            quantity?: number, token?: string): void {
        if (!this.npcId) return;
        const action: {[key: string]: any} = {
            type: "npc_shop_action",
            command,
            npc_id: String(this.npcId)
        };
        if (mode) action.mode = mode;
        if (item) {
            action.item = item;
            action.quantity = String(quantity || 1);
        }
        if (token) action.request_token = token;
        marauroa.clientFramework.sendAction(action);
    }
}
