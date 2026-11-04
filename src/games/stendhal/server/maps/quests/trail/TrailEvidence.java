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
                "Pod wiekiem skrzyni znajduje się zamówienie z pieczęcią Zakopanego. Zamiast podpisu urzędnika widnieją trzy małe kółka. Sprawdź rejestr zamówień na stole we wschodniej części sali Gościrada w ratuszu Zakopanego S."),
        REGISTRY("rejestr zamówień", "item/documents/book", 3, TrailQuestLine.Step.DELIVERY, "archive", "discrepancy",
                "W księdze nie ma tej dostawy. Numery są ciągłe, nie wyrwano żadnej strony. Prawdziwe zamówienia podpisuje Gościrad, a pieczęć na twoim dokumencie ma odwrócony znak. Porozmawiaj z Gościradem o zamówieniu."),
        TRACKS("rozbita skrzynka", "item/pot/crate_small", 0, TrailQuestLine.Step.GUEST, "tracks", "found",
                "Na deskach widnieją trzy kółka. To ten sam znak co na zamówieniu. W szczelinie tkwi sznurek pobrudzony woskiem, a ślady ciężkiego ładunku prowadzą w głąb polany. Wróć do Brzezdoma i powiedz: woźnica."),
        DOCUMENTS("dokumenty ze składu", "item/documents/book", 2, TrailQuestLine.Step.SEAL, "documents", "recovered",
                "Przy workach leży spis towarów: opał Staska, mąka dla karczmy i kilka nieodebranych dostaw. Jest też list z poleceniem wysłania następnego transportu do Semos. Zapisujesz treść i znaki opakowań. Wróć do Brzezdoma i powiedz: skład.");
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
            player.sendPrivateText("Na razie nie znajdujesz tu nowych wskazówek. Zajrzyj do aktualnego celu zadania w dzienniku.");
            return false;
        }
        player.setQuest(kind.step.slot, kind.after);
        player.sendPrivateText(kind.text);
        return true;
    }
    @Override public String getDescriptionName() { return kind.title; }
}
