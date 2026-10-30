package games.stendhal.server.core.rule.quest;

import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.player.Player;

/** Read-only journal objectives for the existing Zakopane starter quest states. */
public final class StarterQuestLine {
    public static final String CHAPTER = "Zaufanie Zakopanego";
    public static final String PIETREK = "Porozmawiaj z Pietrkiem w domku bohaterów w centrum Zakopanego.";
    public static final String ADAS = "Porozmawiaj z Adasiem w domku bohaterów w centrum Zakopanego. Zapytaj o zadanie.";
    public static final String FRYDERYK = "Odwiedź Fryderyka w Zakopanem S i zapytaj o zadanie. Potrzebuje kosy do żniw.";
    public static final String STASEK = "Odwiedź Staska w sklepie obok mostu w Zakopanem C. Zapytaj o zlecenie.";

    private StarterQuestLine() { }

    public static String objective(final Player player, final String slot) {
        final String state = player.getQuest(slot, 0);
        if ("meet_pietrek".equals(slot)) {
            if (state == null || "rejected".equals(state)) { return PIETREK; }
            if ("start".equals(state)) {
                return new KilledForQuestCondition(slot, 1).fire(player, null, null)
                        ? "Wróć do Pietrka w domku bohaterów i powiedz mu, że pokonałeś szczura."
                        : "Pokonaj jednego szczura w pobliżu domku bohaterów, a potem wróć do Pietrka.";
            }
            return "Dokończ rozmowę z Pietrkiem w domku bohaterów. Zapytaj o pomoc i wysłuchaj kolejnych wskazówek.";
        }
        if ("pomoc_adasiowi".equals(slot)) {
            if (state == null || "rejected".equals(state)) { return ADAS; }
            return new KilledForQuestCondition(slot, 1).fire(player, null, null)
                    ? "Wróć do Adasia w domku bohaterów po nagrodę."
                    : "Zejdź do piwnicy domku bohaterów. Pokonaj szczura, szczura jaskiniowego i węża, a potem wróć do Adasia.";
        }
        if ("scythe_fryderyk".equals(slot)) {
            if (state == null || "rejected".equals(state)) { return FRYDERYK; }
            return player.isEquipped("kosa")
                    ? "Zanieś kosę Fryderykowi w Zakopanem S."
                    : "Zdobądź kosę dla Fryderyka w Zakopanem S. Możesz sprawdzić ofertę Bogusia w szpitalu w Zakopanem C.";
        }
        if ("wegiel_na_opal".equals(slot)) {
            if (!player.isQuestCompleted("pomoc_adasiowi")) { return ADAS; }
            if (!player.isQuestCompleted("scythe_fryderyk")) { return FRYDERYK; }
            if (state == null || "rejected".equals(state)) { return STASEK; }
            final int required = player.getRequiredItemQuantity(slot, 0);
            final int owned = player.getNumberOfSubmittableEquipped("węgiel");
            return owned >= required
                    ? "Zanieś węgiel Staskowi w sklepie obok mostu w Zakopanem C, aby odblokować handel."
                    : "Zbierz węgiel dla Staska: " + Math.min(owned, required) + "/" + required
                            + ". Następnie wróć do jego sklepu w Zakopanem C.";
        }
        return null;
    }
}
