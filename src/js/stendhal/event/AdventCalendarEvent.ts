import { RPEvent } from "marauroa";
import { AdventCalendarDialog } from "../ui/dialog/AdventCalendarDialog";

export class AdventCalendarEvent extends RPEvent {
    public execute(_entity: any): void {
        AdventCalendarDialog.show(this as any);
    }
}
