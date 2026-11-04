/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Four journal entries for one expedition, offered together by Drilenun. */
public final class RadanDungeonQuest extends TrailQuest {
    public RadanDungeonQuest(final Step step) {
        super(step);
        if (!TrailQuestLine.DUNGEON_CHAPTER.equals(step.chapter)) throw new IllegalArgumentException("Not a dungeon quest");
    }
    @Override protected void addDialogs() {
        if (step == Step.RADAN_PASSAGE) {
            offer("Zejdź do podziemi Semos. Od schodów idź na zachód, do bocznego chodnika. "
                    + "Leżą tam porzucone narzędzia. Najpierw sprawdź belkę przy wejściu. "
                    + "Jeśli uda ci się dostać do środka, rozejrzyj się za ludźmi Radana. Możesz wrócić po pomoc w każdej chwili.",
                    ConversationStates.QUEST_2_OFFERED);
            reply(step.npc, (player, sentence, speaker) -> {
                if (player.isQuestCompleted(step.slot)) {
                    speaker.say(player.isQuestCompleted(Step.RADAN_FINALE.slot)
                            ? "Straż zajęła już ten chodnik. Bartek jest bezpieczny, a towary wrócą do kupców."
                            : TrailQuestLine.nextObjective(player));
                } else request(player, speaker,
                        "Znaleźliśmy kryjówkę Radana pod miastem. Ma tam ludzi i skradzione zapasy. "
                        + "Nie chcę, żeby uciekli, zanim zbierzemy strażników. Zajrzysz do bocznego chodnika?", ConversationStates.QUEST_2_OFFERED);
            }, "chodnik", "kryjówka", "kryjowka");
        } else if (step == Step.RADAN_FINALE) {
            reply(step.npc, (player, sentence, speaker) -> {
                if (!player.isQuestInState(step.slot, "return")) { remind(player, speaker); return; }
                Item reward = SingletonRepository.getEntityManager().getItem("buty żelazne",
                        ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
                                .withQuestRarity(ItemRarity.RARE).generateAffixes(true).build());
                reward.setBoundTo(player.getName());
                if (!player.equip("bag", reward)) {
                    speaker.say("Mam dla ciebie buty żelazne. Zrób miejsce w plecaku i wróć po nagrodę."); return;
                }
                complete(player, speaker, "return");
            }, "Radan", "radan");
        }
    }
    @Override protected String completedText() {
        return "Dobrze. Księga i zeznanie Bartka wystarczą. Poślę strażników po Radana, "
                + "a kupcy dostaną swoje zapasy. Dziękuję, że pomogłeś to zakończyć. "
                + "Weź te buty i 150 monet. Przydadzą ci się w drodze.";
    }
}
