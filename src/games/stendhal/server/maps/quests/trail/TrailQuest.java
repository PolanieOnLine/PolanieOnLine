/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.QuestMarker;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.maps.quests.QuestRewards;
import games.stendhal.common.constants.ItemRarity;

/** Common journal metadata, consent and one-time rewards for the chapter. */
abstract class TrailQuest extends AbstractQuest {
	protected final TrailQuestLine.Step step;

	TrailQuest(final TrailQuestLine.Step step) {
		this.step = step;
	}

	@Override
	public final void addToWorld() {
		fillQuestInfo(step.title, step.description, false);
		questInfo.setMainQuest(step.chapter, step.order, step.hint);
		QuestRewards rewards = new QuestRewards(step.xp, step.coins);
		switch (step) {
		case RADAN_FINALE:
			rewards.item("buty żelazne", 1, ItemRarity.RARE, true);
			break;
		case MINE_DISPATCH:
			rewards.item("duży eliksir", 5, null, false);
			break;
		case MOUNTAIN_RETURN:
			rewards.item("cuha góralska", 1, ItemRarity.RARE, true);
			break;
		case KEEP_RETURN:
			rewards.item("hełm chaosu", 1, ItemRarity.RARE, true);
			break;
		case OATH_BURIAL:
			rewards.item("magiczna tarcza płytowa", 1, ItemRarity.RARE, true);
			break;
		case WARSAW_TRUCE:
			rewards.item("magiczna zbroja płytowa", 1, ItemRarity.RARE, true);
			break;
		case COAST_RETURN:
			rewards.item("płaszcz chaosu", 1, ItemRarity.RARE, true);
			break;
		case SALT_RETURN:
			rewards.item("hełm wampirzy", 1, ItemRarity.RARE, true);
			break;
		case BELL_ESCAPE:
		case EMBER_RETURN:
			rewards.item("złota skrzynia", 1, null, true);
			break;
		default:
			break;
		}
		if (step == TrailQuestLine.Step.BELL_ESCAPE) {
			rewards.note("Osiągnięcie: Milczący dzwon.");
		}
		questInfo.setRewards(rewards.build());
		addDialogs();
	}

	protected abstract void addDialogs();

	protected void reply(String npc, ChatAction action, String... triggers) {
		npcs.get(npc).add(ConversationStates.ATTENDING, Arrays.asList(triggers),
				null, ConversationStates.ATTENDING, null, action);
	}

	protected void offer(final String accepted) {
		offer(accepted, ConversationStates.QUEST_3_OFFERED);
	}

	protected void offer(final String accepted,
			final ConversationStates state) {
		final SpeakerNPC npc = npcs.get(step.npc);
		// The ordinary QUEST_OFFERED state already belongs to Brzezdom's
		// riddles.
		final ChatCondition awaitingOffer = (player, sentence,
				speaker) -> step.available(player)
						&& !player.hasQuest(step.slot);
		npc.add(state, ConversationPhrases.YES_MESSAGES, awaitingOffer,
				ConversationStates.ATTENDING, null,
				(player, sentence, speaker) -> {
					if (!step.available(player) || player.hasQuest(step.slot)) {
						remind(player, speaker);
						return;
					}
					player.setQuest(step.slot, step.initial);
					speaker.say(accepted);
				});
		npc.add(state, ConversationPhrases.NO_MESSAGES, awaitingOffer,
				ConversationStates.ATTENDING,
				"Dobrze. Jeśli zmienisz zdanie, wiesz, gdzie mnie szukać.",
				null);
	}

	protected void request(final Player player, final EventRaiser speaker,
			final String question) {
		request(player, speaker, question, ConversationStates.QUEST_3_OFFERED);
	}

	protected void request(final Player player, final EventRaiser speaker,
			final String question, final ConversationStates state) {
		if (!step.available(player) || player.hasQuest(step.slot)) {
			remind(player, speaker);
			return;
		}
		speaker.say(question);
		speaker.setCurrentState(state);
	}

	protected void remind(final Player player, final EventRaiser speaker) {
		speaker.say(TrailDialogue.reminder(player, step, speaker.getName()));
	}

	protected abstract String completedText();

	protected void complete(final Player player, final EventRaiser speaker,
			final String requiredState) {
		if (!player.isQuestInState(step.slot, requiredState)) {
			remind(player, speaker);
			return;
		}
		player.setQuest(step.slot, "done");
		player.addXP(step.xp);
		player.addKarma(5);
		new EquipItemAction("money", step.coins).fire(player, null, speaker);
		speaker.say(completedText());
	}

	@Override
	public final String getSlotName() {
		return step.slot;
	}

	@Override
	public final String getName() {
		return step.title;
	}

	@Override
	public final String getNPCName() {
		return step.npc;
	}

	@Override
	public final int getMinLevel() {
		return step.minimumLevel;
	}

	@Override
	public final String getRegion() {
		return step.region;
	}

	@Override
	public final boolean isVisibleOnQuestStatus(final Player player) {
		return step.available(player) || player.hasQuest(step.slot);
	}

	@Override
	public QuestMarker getNPCQuestMarker(final Player player, final SpeakerNPC npc) {
		if (player.hasQuest(step.slot)) {
			return null;
		}
		// These checkpoints start automatically during the Radan expedition.
		if (step == TrailQuestLine.Step.RADAN_RESCUE
				|| step == TrailQuestLine.Step.RADAN_TRANSPORT
				|| step == TrailQuestLine.Step.RADAN_FINALE) {
			return QuestMarker.NONE;
		}
		return step.available(player) && step.npc.equals(npc.getName())
				? QuestMarker.AVAILABLE : QuestMarker.NONE;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return TrailQuestLine.history(player, step);
	}
}
