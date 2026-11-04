/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** The level-thirty continuation of the delivery investigation, in existing mines. */
public final class MineTrailQuest extends TrailQuest {
    public MineTrailQuest(Step step) {
        super(step);
        if (!TrailQuestLine.MINE_CHAPTER.equals(step.chapter)) throw new IllegalArgumentException("Not a mine quest");
    }
    @Override protected void addDialogs() {
        switch (step) {
        case MINE_LEDGER:
            offer("Pokaż Barbarusowi ten wpis. Pracuje w zachodniej odnodze kopalni Semos, w górach na północ od miasta. "
                    + "Zapytaj go o #'nocną zmianę'. Nie wiemy jeszcze, kto odbierał zapasy.");
            reply(step.npc, (player, sentence, speaker) -> request(player, speaker,
                    "W księdze Radana został jeszcze jeden wpis. Ktoś zamawiał jedzenie i drewno dla nocnej zmiany w kopalni. "
                    + "Bez nazwiska, bez ilości wydobytego węgla. Sprawdzisz u Barbarusa, czy rzeczywiście pracowali tam nocą?"), "nocna zmiana", "nocną zmianę");
            reply("Barbarus", (player, sentence, speaker) -> {
                if (!MineTrailProgress.active(player, step)) { remind(player, speaker); return; }
                if (player.isQuestInState(step.slot, "barbarus")) {
                    player.setQuest(step.slot, "register");
                    speaker.say("Nocna zmiana? Ja zamykam robotę przed nocą. Ale nie musisz wierzyć mi na słowo. "
                            + "Sprawdź rejestr wydobycia po zachodniej stronie mojego stanowiska. Zapisuję każdy wózek.");
                } else if (player.isQuestInState(step.slot, "report")) complete(player, speaker, "report");
                else remind(player, speaker);
            }, "nocna zmiana", "nocną zmianę");
            break;
        case MINE_TRACKS:
            offer("Idź od mojego stanowiska na północny wschód. Na pierwszym dużym zakręcie leży skrzynka po racjach. "
                    + "Dalej na wschód poszukaj śladów butów. Sprawdź oba miejsca i wróć.");
            reply(step.npc, (player, sentence, speaker) -> {
                if (MineTrailProgress.active(player, step) && player.isQuestInState(step.slot, "report")) complete(player, speaker, "report");
                else request(player, speaker, "Przy zakręcie znalazłem resztki jedzenia. Moi ludzie jedzą tutaj, przy narzędziach. "
                        + "A dalej widziałem obce ślady. Sprawdzisz, kto mógł tędy chodzić? Niczego stamtąd nie zabieraj, tylko dobrze się przyjrzyj.");
            }, "ślady nocnej zmiany", "slady nocnej zmiany");
            break;
        case MINE_VOICES:
            offer("Szukaj starej obudowy szybu na północnym wschodzie, niedaleko Phalka. Nie przechodź przez jego blokadę. "
                    + "Przyłóż ucho do belek. Jeśli usłyszysz słowa, zapamiętaj je. Zynn w bibliotece zna stare dzieje Semos.");
            reply(step.npc, (player, sentence, speaker) -> request(player, speaker,
                    "Teraz te głosy martwią mnie jeszcze bardziej. Czasem słyszę krzyk, czasem równe kroki, a potem zapada cisza. "
                    + "Sprawdzisz, co dzieje się za starą obudową szybu?"), "głosy spod skały", "glosy spod skaly", "głosach spod skały", "glosach spod skaly");
            reply("Zynn Iwuhos", (player, sentence, speaker) -> {
                if (MineTrailProgress.active(player, step) && player.isQuestInState(step.slot, "history")) complete(player, speaker, "history");
                else remind(player, speaker);
            }, "rozkazy spod ziemi");
            break;
        case MINE_DISPATCH:
            offer("Łącznik jest przy zakręcie na południe ode mnie. Poznasz duergara po uzbrojeniu i pakietach. "
                    + "Pokonaj go, sprawdź pakiet przy zakręcie i pokaż mi rozkaz. Powiedz wtedy #'rozkaz dla straży'. "
                    + "Nie wchodź przez blokadę. Tam jest znacznie niebezpieczniej.");
            reply(step.npc, (player, sentence, speaker) -> request(player, speaker,
                    "Zynn dobrze cię przysłał. To nie nasi górnicy. Widziałem duergara, który nosi pakiety między korytarzami. "
                    + "Jeśli zdobędziemy jeden rozkaz, straż w Semos nam uwierzy. Podejmiesz się tego?"), "łącznik", "lacznik", "łącznika", "lacznika");
            reply(step.npc, (player, sentence, speaker) -> {
                if (!MineTrailProgress.active(player, step) || !player.isQuestInState(step.slot, "identified")) { remind(player, speaker); return; }
                player.setQuest(step.slot, "return");
                speaker.say("Znam ten znak. Duergarowie. Rozkaz mówi o zapasach i warcie przy starej bramie. "
                        + "Są bliżej naszych wyrobisk, niż sądziłem. Zanieś to Drilenunowi w ratuszu Semos. Powiedz #'druga zmiana'. Ja zostanę przy przejściu.");
            }, "rozkaz dla straży", "rozkaz dla strazy");
            reply("Lieutenant Drilenun", (player, sentence, speaker) -> {
                if (!MineTrailProgress.active(player, step) || !player.isQuestInState(step.slot, "return")) { remind(player, speaker); return; }
                complete(player, speaker, "return");
                new EquipItemAction("duży eliksir", 5).fire(player, sentence, speaker);
            }, "druga zmiana");
            break;
        default: throw new IllegalStateException("Not a mine quest");
        }
    }
    @Override protected String completedText() {
        switch (step) {
        case MINE_LEDGER: return "Zgadza się, żadnych nocnych wózków. Ktoś nas karmi bajką o drugiej zmianie. "
                + "Skoro nie kopali węgla, to po co schodzili pod ziemię? Zapytaj mnie o #'ślady nocnej zmiany'.";
        case MINE_TRACKS: return "Racje odliczone na porcje, a buty podkute jak do marszu. To nie moi ludzie. "
                + "Od dawna słyszę coś z tamtej strony, tylko brałem to za echo. Porozmawiajmy o #'głosach spod skały'.";
        case MINE_VOICES: return "Druga zmiana do bramy... To komenda przy zmianie warty. Górnicy tak do siebie nie mówią. "
                + "Jeszcze te równe kroki i sygnał na koniec. Ktoś tam pilnuje przejścia. Zapytaj Phalka o #łącznika. "
                + "Stoi przy zablokowanym wejściu w północno-wschodniej części kopalni. Nie musisz wchodzić za blokadę.";
        case MINE_DISPATCH: return "A więc zapasy Radana szły do duergarów. Dobrze, że mamy ich rozkaz, a nie same pogłoski. "
                + "Ostrzegę kupców i Barbarusa. Phalk niech pilnuje przejścia. Nie wiemy jeszcze, ilu ich tam jest. "
                + "Dziękuję za pomoc. Weź 200 monet i pięć dużych eliksirów. Przydadzą ci się po tej wyprawie.";
        default: throw new IllegalStateException("Not a mine quest");
        }
    }
}
