import { SlashAction } from "./SlashAction";
import { AdventCalendarDialog } from "../ui/dialog/AdventCalendarDialog";

export class AdventCalendarAction extends SlashAction {
    readonly minParams = 0;
    readonly maxParams = 0;
    override readonly desc = "Otwiera kalendarz adwentowy.";

    execute(_type: string, _params: string[], _remainder: string): boolean {
        AdventCalendarDialog.open();
        return true;
    }
}
