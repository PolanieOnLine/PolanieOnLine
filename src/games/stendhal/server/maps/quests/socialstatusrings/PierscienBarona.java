/***************************************************************************
 *                   (C) Copyright 2010-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

public class PierscienBarona extends AbstractQuest {
	private static final String QUEST_SLOT = "pierscien_barona";
	private final SpeakerNPC npc = npcs.get("eDragon");

	private static final String HUNGRY_JOSHUA_QUEST_SLOT = "hungry_joshua"; 
	private static final String FISHERMANS_LICENSE2_QUEST_SLOT = "fishermans_license2";
	private static final String OBSIDIAN_KNIFE_QUEST_SLOT = "obsidian_knife";
	private static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak";
	private static final String CIUPAGA_DWA_WASY_QUEST_SLOT = "ciupaga_dwa_wasy";

	private static Logger logger = Logger.getLogger(PierscienBarona.class);

	private AndCondition hasRequiredItemsCondition() {
		return new AndCondition(
			new PlayerHasItemWithHimCondition("pierścień rycerza", 1),
			new PlayerHasItemWithHimCondition("ruda żelaza", 100),
			new PlayerHasItemWithHimCondition("bryłka złota", 150),
			new PlayerHasItemWithHimCondition("polano", 200),
			new PlayerHasItemWithHimCondition("węgiel", 100),
			new PlayerHasItemWithHimCondition("siarka", 100),
			new PlayerHasItemWithHimCondition("sól", 100)
		);
	}

	private String requiredItemsList() {
		return "Przynieś mi:\n"
			+ "#'1 pierścień rycerza'\n"
			+ "#'100 rudy żelaza'\n"
			+ "#'150 bryłek złota'\n"
			+ "#'200 polan'\n"
			+ "#'100 węgla'\n"
			+ "#'100 siarki'\n"
			+ "#'100 soli'\n";
	}

	private void checkLevelHelm() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()) { 
						raiser.say("Krew rycerza plami twoje ręce, a mroczne piętno na twej duszy czyni cię niegodnym. Oczyść się, nim staniesz przed moim obliczem!");
					} else {
						if (player.isQuestCompleted(CIUPAGA_DWA_WASY_QUEST_SLOT)) {
							if (player.getLevel() >= 350) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Czy jesteś gotów, by wkroczyć na ścieżkę barona i zdobyć pierścień, który kryje w sobie moc pradawnych przodków?");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Już wcześniej obdarzyłem cię pierścieniem barona. Strzeż go dobrze.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twoja pozycja wśród plemion jest zbyt niska. Wróć, gdy osiągniesz poziom 350 i zyskasz szacunek starszyzny.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Nie widzę przy tobie złotej ciupagi z dwoma wąsami. Bez niej nie możesz podjąć tego wyzwania.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Najpierw upewnijmy się, że spełniasz wszystkie wymogi. To wyzwanie nie jest dla #nieprzygotowanych.");
					player.addKarma(10);
				}
		});

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przygotowania", "nieprzygotowany", "wyzwanie"),
			null, ConversationStates.ATTENDING, 
			"Musisz zebrać specjalne dary dla mnie, które uświadczą o Twoim statusie społecznym wśród ludu. Dlatego też będzie potrzebna #lista przedmiotów.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Rozumiem, może innym razem twoje serce zapłonie odwagą.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void checkCollectingQuests() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj, wędrowcze. Czy szukasz mocy ukrytej w pierścieniu barona?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "lista", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(HUNGRY_JOSHUA_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Najpierw nakarm głodnego kowala. Dopiero potem porozmawiamy o pierścieniu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "lista", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(FISHERMANS_LICENSE2_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Zdobycie licencji rybaka to twój następny krok. Udowodnij swoją wytrwałość.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "lista", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Nie posiadasz obsydianowego noża, który jest kluczem do dalszej drogi.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "lista", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Bez płaszcza z mithrilu twoja podróż nie będzie bezpieczna. Zdobądź go najpierw.",
			null);
	}

	private void requestItem() {
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pierścień rycerza", 1));
		reward.add(new DropItemAction("ruda żelaza", 100));
		reward.add(new DropItemAction("bryłka złota", 150));
		reward.add(new DropItemAction("polano", 200));
		reward.add(new DropItemAction("węgiel", 100));
		reward.add(new DropItemAction("siarka", 100));
		reward.add(new DropItemAction("sól", 100));
		reward.add(new EquipItemAction("pierścień barona", 1, true));
		reward.add(new IncreaseXPAction(100000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("listę", "lista", "pierścień"),
				new AndCondition( 
						new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(HUNGRY_JOSHUA_QUEST_SLOT),
						new QuestCompletedCondition(FISHERMANS_LICENSE2_QUEST_SLOT),
						new QuestCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT)),
				ConversationStates.ATTENDING, "Widzę, że masz za sobą wiele wyzwań. Teraz czas przygotować potrzebne #dary.",
				new SetQuestAction(QUEST_SLOT, "lista"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmiotów", "przedmioty", "dary"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "lista"),
					hasRequiredItemsCondition()),
				ConversationStates.ATTENDING, "Doskonale, masz wszystko, czego potrzeba. Oto twój pierścień barona. Niech jego moc ci służy.",
				new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmiotów", "przedmioty", "dary"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "lista"),
					new NotCondition(hasRequiredItemsCondition())),
				ConversationStates.ATTENDING,
				requiredItemsList(),
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Status Społeczny: Baron",
				"Podążaj ścieżką wyznaczoną przez pradawnego smoka i zdobądź pierścień barona.",
				true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);

		res.add(player.getGenderVerb("Spotkałem") + " starożytnego smoka eDragon, strażnika dawnej hierarchii i honoru.");
		res.add("Powiedział mi, że mogę zdobyć pierścień barona – symbol władzy i zaszczytów – jeśli dowiodę swojej wartości jako rycerz.");

		if ("rejected".equals(questState)) {
			res.add("Uznałem, że nie jestem jeszcze gotowy na wyzwanie, które może zmienić mój status społeczny.");
			return res;
		}
		if ("start".equals(questState)) {
			return res;
		}

		res.add("Smok wyjawił mi, że aby wejść w stan baronii, muszę dostarczyć:");
		res.add("- Pierścień rycerza, symbol mojej obecnej przynależności i lojalności wobec rycerstwa.");
		res.add("- 100 rud żelaza, wydobytych z ziem matki Mokoszy.");
		res.add("- 150 bryłek złota, będących darem boga Welesa, patrona bogactwa.");
		res.add("- 200 polan, symbolizujących dar dla Swaroga, boga ognia.");
		res.add("- 100 jednostek węgla, potrzebnych do wykucia symbolu mojego nowego statusu.");
		res.add("- 100 jednostek siarki, oczyszczającej mnie z dawnych zobowiązań.");
		res.add("- 100 jednostek soli, będącej przypieczętowaniem nowej przysięgi.");
		res.add("Mam mu powiedzieć 'przedmioty', gdy zgromadzę wszystko, co potrzebne do ceremonii baronii.");

		if ("lista".equals(questState)) {
			return res;
		}

		res.add("Po dostarczeniu wymaganych darów smok eDragon przyjął mnie w poczet baronów. Mój status społeczny zmienił się, a " + player.getGenderVerb("otrzymałem") + " pierścień barona jako dowód mojego nowego miejsca w hierarchii.");
		res.add(player.getGenderVerb("Zostałem") + " uznany za barona, co otwiera przede mną nowe możliwości i przywileje w świecie.");

		if (isCompleted(player)) {
			return res;
		}

		// Debugging info for unexpected quest state
		final List<String> debug = new ArrayList<>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu zadania: " + questState);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Barona";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
