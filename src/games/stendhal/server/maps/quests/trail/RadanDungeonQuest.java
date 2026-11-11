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
		if (!TrailQuestLine.DUNGEON_CHAPTER.equals(step.chapter))
			throw new IllegalArgumentException("Not a dungeon quest");
	}

	@Override
	protected void addDialogs() {
		if (step == Step.RADAN_PASSAGE) {
			offer("Zejdź do podziemi Semos. Od schodów idź na zachód, do bocznego chodnika. "
					+ "Leżą tam porzucone narzędzia. Najpierw sprawdź belkę przy wejściu. "
					+ "Jeśli uda ci się dostać do środka, rozejrzyj się za ludźmi Radana. Możesz wrócić po pomoc w każdej chwili.",
					ConversationStates.QUEST_2_OFFERED);
			reply(step.npc, (player, sentence, speaker) -> {
				if (player.isQuestCompleted(step.slot)) {
					if (player.isQuestCompleted(Step.RADAN_FINALE.slot)) {
						speaker.say(
								"Bartek jest bezpieczny. Teraz straż zajmie się Radanem i zwrotem zapasów kupcom.");
					} else {
						for (Step current : new Step[] { Step.RADAN_RESCUE,
								Step.RADAN_TRANSPORT, Step.RADAN_FINALE }) {
							if (!player.isQuestCompleted(current.slot)) {
								speaker.say(TrailDialogue.reminder(player,
										current, TrailSceneRegistry.speakerName(speaker.getEntity())));
								break;
							}
						}
					}
				} else
					request(player, speaker,
							"Znaleźliśmy kryjówkę Radana pod Semos. Ma tam ludzi i skradzione zapasy. "
									+ "Nie chcę, żeby uciekli, zanim zbierzemy strażników. Zajrzysz do bocznego chodnika?",
							ConversationStates.QUEST_2_OFFERED);
			}, "chodnik", "kryjówka", "kryjowka");
		} else if (step == Step.RADAN_FINALE) {
			reply(step.npc, (player, sentence, speaker) -> {
				if (!player.isQuestInState(step.slot, "return")) {
					remind(player, speaker);
					return;
				}
				Item reward = SingletonRepository.getEntityManager().getItem(
						"buty żelazne",
						ItemCreationContext.builder(
								ItemCreationContext.Source.QUEST).withQuestRarity(
										ItemRarity.RARE).generateAffixes(
												true).build());
				reward.setBoundTo(player.getName());
				if (!player.equip("bag", reward)) {
					speaker.say(
							"Chcę ci podarować te żelazne buty, ale nie zmieszczą się w twoim plecaku. Zostaw gdzieś część rzeczy, a przechowam je dla ciebie.");
					return;
				}
				complete(player, speaker, "return");
			}, "Radan", "radan");
		}
	}

	@Override
	protected String completedText() {
		return "Mamy księgę, a Bartek może potwierdzić, co działo się pod Semos. Poślę strażników po Radana. "
				+ "Dopilnuję też, żeby zapasy wróciły do kupców. Dobra robota. Te buty i 150 monet należą się tobie. "
				+ "Nie podoba mi się jeszcze jedno: Radan zapisał dostawę dla kopalni, na #'nocną zmianę'. "
				+ "Przyjrzę się temu. Gdy odpoczniesz, zajrzyj do mnie.";
	}
}
