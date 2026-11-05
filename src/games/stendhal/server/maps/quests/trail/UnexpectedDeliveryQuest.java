/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;

/** A forged order found in a delivery links Stasek to the town archive. */
public final class UnexpectedDeliveryQuest extends TrailQuest {
    private static final String OFFER = "W skrzyni z opałem znalazłem kwit z pieczęcią ratusza. "
            + "Jakiś woźnica odebrał na jego podstawie część moich zapasów. "
            + "Tylko że nikt z ratusza mi o tym nie mówił. Sprawdzisz u Gościrada, czy wie coś o tym zamówieniu?";

    public UnexpectedDeliveryQuest() { super(TrailQuestLine.Step.DELIVERY); }
    @Override protected void addDialogs() {
        offer("Zajrzyj do skrzyni w moim sklepie. Potem idź z tym do Gościrada, "
                + "do ratusza w centrum Zakopanego. On zapisuje wszystkie zamówienia w księdze. "
                + "Powinien wiedzieć, skąd wzięła się ta kartka.");
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
                speaker.say("Porównaj ten dokument z wpisami w mojej księdze. "
                        + "Leży na małym stoliku obok mnie. Sprawdź numer zamówienia i podpis. "
                        + "Pieczęć można podrobić, ale zamówienie powinno być zapisane w księdze.");
            } else {
                remind(player, speaker);
            }
        }, "zamówienie", "zamowienie", "pieczęć", "pieczec");
    }
    @Override protected String completedText() {
        return "No proszę. W księdze ani słowa, a pieczęć niby nasza. "
                + "Ktoś się pod nas podszywa i wywozi ludziom zapasy. "
                + "Trzeba odnaleźć tego #'woźnicę', zanim znów kogoś oszuka. Mam pomysł, gdzie zacząć.";
    }
}
