/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

/** A personal decoy and spoken pickup phrase expose the carrier's signed receipt. */
public final class FalsePickupQuest extends TrailQuest {
    private static final String OFFER = "Widziałam, jak tragarz czekał przy skrzyniach obok gospody. "
            + "Możemy podstawić mu skrzynię z polanami zamiast prawdziwego towaru. "
            + "Skoro znasz hasło, może pokaże ci, kto go przysłał. Spróbujemy?";
    public FalsePickupQuest() { super(TrailQuestLine.Step.SEMOS_PICKUP); }
    @Override protected void addDialogs() {
        offer("Przynieś mi dwa #'polana'. Włożę je do skrzyni i przykryję płótnem. "
                + "Gdy będziesz je mieć, powiedz #'przygotuj skrzynię'. Olek czeka na zewnątrz, po prawej stronie wejścia.");
        reply("Margaret", (player, sentence, speaker) -> request(player, speaker, OFFER), "odbiór", "odbior");
        reply("Margaret", (player, sentence, speaker) -> {
            if (!player.isQuestInState(step.slot, "wood")) { remind(player, speaker); return; }
            if (!player.dropSubmittable("polano", 2)) {
                speaker.say("Potrzebuję dwóch polan. Sprawdź, czy masz je przy sobie. "
                        + "Drewna możesz poszukać na polanach poza miastem. Potem powiedz #'przygotuj skrzynię'.");
                return;
            }
            player.setQuest(step.slot, "crate");
            speaker.say("Gotowe. Skrzynia stoi na zewnątrz, po prawej stronie wejścia do gospody. "
                    + "Sprawdź ją, a potem podejdź do Olka. Wiesz już, jak potwierdzić #'suchy ładunek'.");
        }, "przygotuj skrzynię", "przygotuj skrzynie", "polana");
        reply("Tragarz Olek", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "contact")) {
                player.setQuest(step.slot, "question");
                speaker.say("No, nareszcie ktoś z towarem. Wszystko suche? Dobrze. "
                        + "Mam odebrać jedną skrzynię. Jeśli chcesz, mogę pokazać #'kwit', zanim ją zabiorę.");
            } else if (player.isQuestInState(step.slot, "question")) {
                speaker.say("Tak, hasło się zgadza. Chcesz obejrzeć #'kwit'?");
            } else if (player.isQuestInState(step.slot, "return")) {
                speaker.say("Masz już zapisane, kto zamówił odbiór. Pokaż to porucznikowi w ratuszu.");
            } else if (player.isQuestCompleted(step.slot)) {
                speaker.say("Nie odbieram już tych przesyłek. Wystarczy mi kłopotów.");
            } else {
                speaker.say("Najpierw musi być gotowa skrzynia. Na razie nie mam czego odbierać.");
            }
        }, "suchy ładunek", "suchy ladunek");
        reply("Tragarz Olek", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "return") || player.isQuestCompleted(step.slot)) {
                speaker.say("Na kwicie jest podpis Radana. Wszystko masz już zapisane."); return;
            }
            if (!player.isQuestInState(step.slot, "question")) {
                speaker.say("Najpierw muszę wiedzieć, czy to ty masz mi wydać przesyłkę."); return;
            }
            player.setQuest(step.slot, "return");
            speaker.say("Proszę. Podpisano: Radan. Dał mi pieniądze i kazał odebrać skrzynię po umówionym haśle. "
                    + "Nie wiem, co miało być w środku. Jeśli coś się nie zgadza, pokaż ten kwit porucznikowi w ratuszu. "
                    + "Zaczekam, aż to wyjaśnicie.");
        }, "kwit", "dla kogo");
        reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
            if (player.isQuestInState(step.slot, "return")) complete(player, speaker, "return");
            else remind(player, speaker);
        }, "tragarz", "Olek");
    }
    @Override protected String completedText() {
        return "Radan. Wreszcie mamy nazwisko i podpis. Dagobert zatrzyma pieniądze, "
                + "a ja poślę ludzi, żeby sprawdzili zleceniodawcę Olka. "
                + "Dobrze, że udało się to załatwić bez kolejnej kradzieży. "
                + "Sam napiszę do Wojtka, że dostawa do Semos została wstrzymana. Olek nie zabierze tej skrzyni. Jeśli chcesz pomóc straży znaleźć Radana, zapytaj mnie o #chodnik.";
    }
}
