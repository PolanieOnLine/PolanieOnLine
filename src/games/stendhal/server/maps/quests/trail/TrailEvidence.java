/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;

/** Shared scenery; examining it changes only the examining player's persisted quest. */
public final class TrailEvidence extends AreaEntity implements UseListener {
    public enum Kind {
        SHIPMENT("skrzynia z dostawą", "item/pot/crate_small", 1, TrailQuestLine.Step.DELIVERY, "inspect", "archive",
                "Pod wiekiem skrzyni leży zamówienie z pieczęcią Zakopanego. W miejscu podpisu są trzy małe kółka. Sprawdź rejestr zamówień w ratuszu w Zakopanem S. Księga leży na stole po wschodniej stronie sali Gościrada."),
        REGISTRY("rejestr zamówień", "item/documents/book", 3, TrailQuestLine.Step.DELIVERY, "archive", "discrepancy",
                "Przeglądasz wpisy, ale takiego zamówienia tu nie ma. Strony są całe, a numery idą po kolei. Pod innymi zamówieniami podpisuje się Gościrad. Odcisk pieczęci też wygląda inaczej. Porozmawiaj z nim o zamówieniu."),
        TRACKS("rozbita skrzynka", "item/pot/crate_small", 0, TrailQuestLine.Step.GUEST, "tracks", "found",
                "Na deskach są trzy kółka, takie same jak na zamówieniu. Między nimi tkwi kawałek sznurka ubrudzonego woskiem. Ślady wozu prowadzą dalej, w głąb polany. Wróć do Brzezdoma i porozmawiaj o woźnicy."),
        DOCUMENTS("dokumenty ze składu", "item/documents/book", 2, TrailQuestLine.Step.SEAL, "documents", "recovered",
                "Przy workach leży spis: węgiel Staska, mąka do karczmy i inne zamówienia z Zakopanego. W liście ktoś każe wysłać następny transport do Semos. Zapisujesz najważniejsze informacje. Wróć do Brzezdoma i opowiedz mu o składzie.");
        final String title, tileset, before, after, text;
        final int tile;
        final TrailQuestLine.Step step;
        Kind(String title, String tileset, int tile, TrailQuestLine.Step step, String before, String after, String text) {
            this.title = title; this.tileset = tileset; this.tile = tile;
            this.step = step; this.before = before; this.after = after; this.text = text;
        }
    }
    private final Kind kind;

    public TrailEvidence(final Kind kind) {
        super(1, 1);
        this.kind = kind;
        setRPClass("block"); put("type", "block"); put("class", "questuseable"); put("name", kind.title);
        put("z", 8000); put("tileset", kind.tileset); put("tile_index", kind.tile); put("tileset_columns", 2);
        setCursor("ACTIVITY"); setMenu("Sprawdź|Użyj"); setResistance(0);
        setDescription("Oto " + kind.title + ". Możesz przyjrzeć się bliżej i poszukać wskazówek.");
    }
    @Override public boolean onUsed(final RPEntity user) {
        if (!(user instanceof Player) || getZone() == null || user.getZone() != getZone()) return false;
        final Player player = (Player) user;
        if (!player.nextTo(this)) { player.sendPrivateText("Musisz podejść bliżej."); return false; }
        if (!player.isQuestInState(kind.step.slot, kind.before)) {
            player.sendPrivateText("Nie znajdujesz tu nic nowego. Jeśli nie wiesz, gdzie szukać dalej, zajrzyj do dziennika.");
            return false;
        }
        player.setQuest(kind.step.slot, kind.after);
        player.sendPrivateText(kind.text);
        return true;
    }
    @Override public String getDescriptionName() { return kind.title; }
}
