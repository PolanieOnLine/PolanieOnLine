import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";
import { GameLanguage } from "../../util/GameLanguage";
import { getItemSlotLayers } from "../component/ItemSlotPresentation";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

type CalendarData = {[key: string]: any};
const text = (value: string): string => GameLanguage.getText(value);

/** Opened locally; asynchronous replies can only update the matching live window. */
export class AdventCalendarDialog extends DialogContentComponent {
    private static active?: AdventCalendarDialog;
    private static openingSequence = 0;
    private readonly owner = marauroa.me;
    private readonly windowId = `${Date.now()}:${++AdventCalendarDialog.openingSequence}`;
    private readonly board = document.createElement("div");
    private readonly date = document.createElement("span");
    private readonly status = document.createElement("div");
    private readonly rewards = document.createElement("div");
    private readonly doors = new Map<number, HTMLButtonElement>();
    private readonly sprites = new Map<HTMLElement, string>();
    private readonly timer: ReturnType<typeof setInterval>;
    private data?: CalendarData;
    private sequence = 0;
    private requestId = "";
    private sentAt = 0;
    private waiting = false;

    private constructor() {
        super("empty-div-template");
        this.componentElement.classList.add("advent-calendar-dialog");
        const heading = document.createElement("div");
        heading.className = "advent-calendar-heading";
        const title = document.createElement("strong");
        title.textContent = text("Świąteczne podarunki");
        heading.append(title, this.date);
        this.board.className = "advent-calendar-board";
        this.board.setAttribute("aria-label", text("Kalendarz adwentowy"));
        this.rewards.className = "advent-calendar-rewards";
        this.status.className = "advent-calendar-status";
        this.status.setAttribute("aria-live", "polite");
        this.status.textContent = text("Wczytywanie kalendarza…");
        this.componentElement.append(heading, this.board, this.rewards, this.status);
        this.addCloseButton();
        this.timer = setInterval(() => {
            if (marauroa.me !== this.owner) {
                this.close();
                return;
            }
            const elapsed = Date.now() - this.sentAt;
            if (this.waiting && elapsed > 8000) {
                this.waiting = false;
                this.status.textContent = text("Nie udało się odświeżyć kalendarza. Spróbuj ponownie.");
                this.updateDoors();
            } else if (!this.waiting && elapsed > 30000) {
                this.send("refresh");
            }
            for (const [icon, filename] of this.sprites) {
                const sheet = singletons.getSpriteStore().get(filename);
                const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
                icon.style.backgroundPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
            }
        }, 100);
    }

    public static open(): void {
        if (!marauroa.me) return;
        if (this.active?.owner !== marauroa.me) this.active?.close();
        if (!this.active) {
            const dialog = new AdventCalendarDialog();
            const frame = new FloatingWindow(text("Kalendarz adwentowy"), dialog, 24, 24);
            frame.setId("advent-calendar");
            dialog.setFrame(frame);
            this.active = dialog;
        }
        this.active.send("open");
    }

    public static show(data: CalendarData): void {
        const dialog = this.active;
        if (!dialog || dialog.owner !== marauroa.me || data.request_id !== dialog.requestId) return;
        dialog.waiting = false;
        dialog.data = data;
        const date = new Date(`${data.date}T12:00:00`);
        dialog.date.textContent = Number.isFinite(date.getTime())
            ? new Intl.DateTimeFormat(GameLanguage.getLanguage(), {day: "numeric", month: "long", year: "numeric"}).format(date)
            : String(data.date || "");
        if (!dialog.doors.size) dialog.buildDoors(dialog.list(data.layout));
        dialog.status.textContent = text(data.message || "Otwórz dzisiejsze okienko lub odbierz zaległy prezent.");
        dialog.updateDoors();
        if (data.status === "inactive") {
            dialog.rewards.replaceChildren();
            dialog.sprites.clear();
        }
        if (Number(data.revealed_day) > 0 && data.reward_names) dialog.showRewards(data);
    }

    public override onParentClose(): void {
        clearInterval(this.timer);
        this.sprites.clear();
        if (AdventCalendarDialog.active === this) AdventCalendarDialog.active = undefined;
    }

    private buildDoors(layout: string[]): void {
        for (const value of layout) {
            const [day, column, row, width, height] = value.split(",").map(Number);
            if (![day, column, row, width, height].every(Number.isFinite) || day < 1 || day > 24) continue;
            const door = document.createElement("button");
            door.type = "button";
            door.className = "advent-calendar-door";
            door.style.gridColumn = `${column + 1} / span ${width}`;
            door.style.gridRow = `${row + 1} / span ${height}`;
            door.dataset.day = String(day);
            door.classList.toggle("tone-green", day % 3 === 0);
            const number = document.createElement("span");
            number.className = "advent-calendar-number";
            number.textContent = String(day);
            const received = document.createElement("span");
            received.className = "advent-calendar-received";
            received.textContent = "✓";
            received.setAttribute("aria-hidden", "true");
            door.append(number, received);
            door.addEventListener("click", () => {
                const state = this.list(this.data?.states)[day - 1];
                if (state === "available" || state === "claimed") this.send(state === "claimed" ? "view" : "claim", day);
            });
            this.doors.set(day, door);
            this.board.appendChild(door);
        }
    }

    private updateDoors(): void {
        const states = this.list(this.data?.states);
        for (const [day, door] of this.doors) {
            const state = states[day - 1] || "future";
            door.classList.toggle("is-today", day === Number(this.data?.today));
            door.classList.toggle("is-claimed", state === "claimed");
            door.classList.toggle("is-expired", state === "expired");
            door.disabled = this.waiting || (state !== "available" && state !== "claimed");
            const label = state === "claimed" ? "Prezent odebrany" : state === "available" ? "Odbierz prezent"
                : state === "expired" ? "Termin odbioru minął" : "Prezent jeszcze niedostępny";
            door.setAttribute("aria-label", `${day} ${text("grudnia")}: ${text(label)}`);
        }
    }

    private send(command: string, day = 0): void {
        if (this.waiting) return;
        this.requestId = `${this.windowId}:${++this.sequence}`;
        this.waiting = true;
        this.sentAt = Date.now();
        this.updateDoors();
        marauroa.clientFramework.sendAction({type: "advent_calendar_action", command, day: String(day), request_id: this.requestId});
    }

    private showRewards(data: CalendarData): void {
        this.rewards.replaceChildren();
        this.sprites.clear();
        const names = this.list(data.reward_names);
        const classes = this.list(data.reward_classes);
        const subclasses = this.list(data.reward_subclasses);
        const quantities = this.list(data.reward_quantities);
        names.forEach((name, index) => {
            const reward = document.createElement("div");
            reward.className = "advent-calendar-reward";
            const slot = document.createElement("div");
            slot.className = "itemSlot";
            const layers = getItemSlotLayers(slot);
            const filename = `${Paths.sprites}/items/${classes[index]}/${subclasses[index]}.png`;
            layers.icon.style.backgroundImage = `url(${singletons.getSpriteStore().checkPath(filename)})`;
            layers.quantityLabel.textContent = Number(quantities[index]) > 1 ? quantities[index] : "";
            this.sprites.set(layers.icon, filename);
            const label = document.createElement("span");
            label.textContent = text(name === "money" ? "monety" : name);
            reward.append(slot, label);
            this.rewards.appendChild(reward);
        });
    }

    private list(value: unknown): string[] {
        return Array.isArray(value) ? value.map(String)
            : typeof value === "string" ? value.substring(1, value.length - 1).split(/\t/).filter(Boolean) : [];
    }
}
