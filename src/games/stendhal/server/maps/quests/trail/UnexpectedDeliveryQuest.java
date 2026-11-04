/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;

/** A forged order found in a delivery links Stasek to the town archive. */
public final class UnexpectedDeliveryQuest extends TrailQuest {
    private static final String OFFER = "Wśród opału znalazłem zamówienie z pieczęcią ratusza. "
            + "Miałem oddać część zapasów człowiekowi, którego nigdy wcześniej nie widziałem. "
            + "Po twojej pomocy wiem, że mogę ci zaufać. Sprawdzisz, czy ratusz naprawdę wysłał tę dostawę?";

    public UnexpectedDeliveryQuest() { super(TrailQuestLine.Step.DELIVERY); }
    @Override protected void addDialogs() {
        offer("Skrzynia z dostawą stoi w moim sklepie. Sprawdź ją, a z tym, co znajdziesz, "
                + "udaj się do Gościrada w ratuszu Zakopanego S. Prowadzi rejestr miejskich zamówień. "
                + "Nie oskarżajmy nikogo, zanim zobaczymy zapis w księdze.");
        final ChatAction requestDelivery = (player, sentence, speaker) -> request(player, speaker, OFFER);
        reply("Stasek", requestDelivery, "dostawa", "podejrzana dostawa");
        // Stasek's original help still directs players to ask about a task.
        // Keep that path useful after his coal commission is complete.
        npcs.get("Stasek").add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
                (player, sentence, npc) -> step.available(player), ConversationStates.ATTENDING, null, requestDelivery);
        reply("Gościrad", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "discrepancy")) {
                complete(player, speaker, "discrepancy");
            } else if (player.isQuestInState(step.slot, "archive")) {
                speaker.say("Połóż dokument obok mojego rejestru zamówień i przyjrzyj się wpisom. "
                        + "Księga leży na stole we wschodniej części tej sali. Zwróć uwagę na numer dostawy i podpis urzędnika. "
                        + "Sama pieczęć nie wystarczy, by uznać zamówienie za prawdziwe.");
            } else {
                remind(player, speaker);
            }
        }, "zamówienie", "zamowienie", "pieczęć", "pieczec");
    }
    @Override protected String completedText() {
        return "Dobrze, że sprawdziliśmy księgę. Nikt z ratusza nie wystawił tego zamówienia. "
                + "Ktoś odbiera zapasy, posługując się naszą pieczęcią. Nie chcę wywoływać paniki, "
                + "zanim dowiemy się czegoś więcej. Zapytaj mnie o #'woźnicę', a ustalimy, dokąd pojechał.";
    }
}
