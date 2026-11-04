/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Arrays;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.AbstractQuest;

/** Common journal metadata, consent and one-time rewards for the chapter. */
abstract class TrailQuest extends AbstractQuest {
    protected final TrailQuestLine.Step step;
    TrailQuest(final TrailQuestLine.Step step) { this.step = step; }

    @Override public final void addToWorld() {
        fillQuestInfo(step.title, step.description, false);
        questInfo.setMainQuest(TrailQuestLine.CHAPTER, step.ordinal() + 1, step.hint);
        addDialogs();
    }
    protected abstract void addDialogs();

    protected void reply(String npc, ChatAction action, String... triggers) {
        npcs.get(npc).add(ConversationStates.ATTENDING, Arrays.asList(triggers), null,
                ConversationStates.ATTENDING, null, action);
    }

    protected void offer(final String accepted) {
        final SpeakerNPC npc = npcs.get(step.npc);
        // The ordinary QUEST_OFFERED state already belongs to Brzezdom's riddles.
        npc.add(ConversationStates.QUEST_3_OFFERED, ConversationPhrases.YES_MESSAGES, null,
                ConversationStates.ATTENDING, null, (player, sentence, speaker) -> {
            if (!step.available(player) || player.hasQuest(step.slot)) { remind(player, speaker); return; }
            player.setQuest(step.slot, step.initial);
            speaker.say(accepted);
        });
        npc.add(ConversationStates.QUEST_3_OFFERED, ConversationPhrases.NO_MESSAGES, null,
                ConversationStates.ATTENDING, "Dobrze. Jeśli zmienisz zdanie, wiesz, gdzie mnie szukać.", null);
    }

    protected void request(final Player player, final EventRaiser speaker, final String question) {
        if (!step.available(player) || player.hasQuest(step.slot)) { remind(player, speaker); return; }
        speaker.say(question);
        speaker.setCurrentState(ConversationStates.QUEST_3_OFFERED);
    }

    protected void remind(final Player player, final EventRaiser speaker) {
        speaker.say(player.isQuestCompleted(step.slot) ? completedText()
                : TrailQuestLine.objective(player, step.slot));
    }
    protected abstract String completedText();

    protected void complete(final Player player, final EventRaiser speaker, final String requiredState) {
        if (!player.isQuestInState(step.slot, requiredState)) { remind(player, speaker); return; }
        player.setQuest(step.slot, "done");
        player.addXP(step.xp);
        player.addKarma(5);
        new EquipItemAction("money", step.coins).fire(player, null, speaker);
        speaker.say(completedText());
    }

    @Override public final String getSlotName() { return step.slot; }
    @Override public final String getName() { return step.title; }
    @Override public final String getNPCName() { return step.npc; }
    @Override public final String getRegion() { return Region.ZAKOPANE_CITY; }
    @Override public final boolean isVisibleOnQuestStatus(final Player player) {
        return step.available(player) || player.hasQuest(step.slot);
    }
    @Override public List<String> getHistory(final Player player) {
        return TrailQuestLine.history(player, step);
    }
}
