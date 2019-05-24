/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: The Jailed Barbarian
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Lorenz, the jailed barbarian in a hut on Amazon Island</li>
 * <li>Esclara the Amazon Princess</li>
 * <li>Ylflia the Princess of Kalavan</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>1. Lorenz ask you for a scythe to bring him</li>
 * <li>2. You have to ask Princess Esclara for a 'reason'</li>
 * <li>3. You have to bring him an egg</li>
 * <li>4. You have to inform Princess Ylflia</li>
 * <li>5. You have to bring him a barbarian armor</li>
 * <li>6. You get a reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>You get 20 gold bars</li>
 * <li>Karma: 15</li>
 * <li>You get 52,000 experience points in all</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */
 
 public class JailedBarbarian extends AbstractQuest {
 	private static final String QUEST_SLOT = "jailedbarb";
 	
	private static Logger logger = Logger.getLogger(JailedBarbarian.class);

 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void step1() {
		final SpeakerNPC npc = npcs.get("Lorenz");	
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Potrzebuję pomocy przy ucieczce z tego więzienia. Te wstrętne Amazonki! Czy możesz mi pomóc?",
				null);
							
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! Teraz mogę uciec!",
				null);
		

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dziękuję! Po pierwsze potrzebuję #kosy do wycięcia tych okropnych kwiatków. Tylko nie przynoś mi starej! Daj znać, gdy będziesz ją miał!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Odejdź ktoś inny mi pomoże!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}
	
	private void step2() {
	final SpeakerNPC npc = npcs.get("Lorenz");	
	
	    final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kosa"));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "capture"));
		reward.add(new IncreaseKarmaAction(10));
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("kosa","kosy"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition("kosa")),
				ConversationStates.ATTENDING, 
				"Dziękuję!! Pierwszą część mamy za sobą! Teraz mogę ściąć wszystkie te kwiatki! Zapytaj Princess Esclara dlaczego tutaj jestem! Sądzę, że coś powie, gdy podasz jej moje imię...",
				new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("kosa","kosy"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("kosa"))),
			ConversationStates.ATTENDING,
			"Nie masz jeszcze kosy! Idź i zdobądź jakąś dla mnie. Pospiesz się!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"Już się Ciebie pytałem o przyniesienie mi #kosy do ścięcia tych kwiatków!",
				null);
	}
	
	private void step3() {
	final SpeakerNPC npc = npcs.get("Princess Esclara");
	
		npc.add(ConversationStates.ATTENDING, "Lorenz",
				new QuestInStateCondition(QUEST_SLOT, "capture"),
				ConversationStates.ATTENDING,
				"Chcesz wiedzieć dlaczego on tutaj jest? On i jego wstrętni przyjaciele kopali #tunel do naszej cudownej wyspy! Dlatego został uwięziony!",
				new SetQuestAction(QUEST_SLOT, "princess"));
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("tunnel", "tunel"),
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING, "Jestem teraz wściekła i nie chce już o tym rozmawiać! Jeżeli chcesz się dowiedzieć więcej to musisz go zapytać o #tunel!",
				null);	

	}
	
	private void step4() {
	final SpeakerNPC npc = npcs.get("Lorenz");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("tunnel", "tunel"),
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING, 
				"Co chce mnie doprowadzić do szaleństwa jak wszystkie te kwiatki! Robię się głodny. Przynieś #jajo dla mnie! Daj znać, gdy zdobędziesz.",
				new SetQuestAction(QUEST_SLOT, "jajo"));	
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "capture"),
				ConversationStates.ATTENDING,
				"Proszę zapytaj Princess Esclara dlaczego tutaj jestem! Sądzę, że wypowiadając moje imię sprowokujesz ją do wyjawienia powodu",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "princess"),
				ConversationStates.ATTENDING,
				"Założę się, że Princess Esclara powiedziała, że zostałem uwięziony za #tunel ... ",
				null);
	}
	
	private void step5() {
		final SpeakerNPC npc = npcs.get("Lorenz");	
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("jajo"));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new SetQuestAction(QUEST_SLOT, "jailed"));
		reward.add(new IncreaseKarmaAction(10)); 
		
		npc.add(ConversationStates.ATTENDING, "jajo",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "jajo"),
						new PlayerHasItemWithHimCondition("jajo")),
						ConversationStates.ATTENDING, 
						"Dziękuję przyjacielu. Teraz musisz powiedzieć Princess Ylflia w Zamku Kalavan, że jestem tutaj #uwięziony. Pospiesz się!",
						new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, "jajo",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "jajo"), new NotCondition(new PlayerHasItemWithHimCondition("jajo"))),
			ConversationStates.ATTENDING,
			"Nie widzę jaja!!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "jajo"),
				ConversationStates.ATTENDING,
				"Prosiłem Ciebie o przyniesienie #jaja!",
				null);
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("jailed", "uwięziony"),
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING, "Wiem to, *Jestem* uwięziony! Potrzebuję Ciebie, abyś powiedział Princess Ylflia, że jestem tutaj!",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING,
				"Potrzebuję Ciebie, abyś poszedł do Princess Ylflia i powiedział, że jestem tutaj #uwięziony.",
				null);
	}
	
	private void step6() {
	final SpeakerNPC npc = npcs.get("Princess Ylflia");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("jailed", "Lorenz", "uwięziony"),
				new QuestInStateCondition(QUEST_SLOT, "jailed"),
				ConversationStates.ATTENDING, 
				"Och. Mój ojciec nie powinien wiedzieć o tym. Mam nadzieje, że Lorenz ma się dobrze! Dziękuję za wiadomość! Wyślij mu #pozdrowienia! Lepiej wróć do niego. Może potrzebować pomocy.",
				new SetQuestAction(QUEST_SLOT, "spoken"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("greetings", "pozdrowienia"),
				new QuestInStateCondition(QUEST_SLOT, "spoken"),
				ConversationStates.ATTENDING, "Idź i przekaż Lorenz moje #pozdrowienia.",
				null);

	}

	private void step7() {
	final SpeakerNPC npc = npcs.get("Lorenz");
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("greetings", "pozdrowienia"),
				new QuestInStateCondition(QUEST_SLOT, "spoken"),
				ConversationStates.ATTENDING, 
				"Dziękuję przyjacielu. Teraz ostatnie zadanie! Przynieś mi barbarian armor. Bez tego nie ucieknę stąd! Idź! Idź! I daj znać, gdy zdobędziesz #zbroję!",
				new SetQuestAction(QUEST_SLOT, "armor"));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "greetings"),
				ConversationStates.ATTENDING,
				"Teraz musisz porozmawiać z Princess Ylflia ... Mam nadzieję, że przesłała swoje najgorętsze #pozdrowienia...",
				null);
	}
	
	private void step8() {
		final SpeakerNPC npc = npcs.get("Lorenz");	

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("zbroja barbarzyńcy"));
		reward.add(new IncreaseXPAction(50000));
		reward.add(new EquipItemAction("sztabka złota", 20));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15)); 
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("armor", "zbroja", "zbroję"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"),
						new PlayerHasItemWithHimCondition("zbroja barbarzyńcy")),
						ConversationStates.ATTENDING, 
						"To wszystko! Teraz jestem gotowy do mojej ucieczki! Oto coś dla Ciebie. Ukradłem Princess Esclara! Żeby tylko się nie dowiedziała. Teraz zostaw mnie!",
						new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING, "armor",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"), new NotCondition(new PlayerHasItemWithHimCondition("zbroja barbarzyńcy"))),
			ConversationStates.ATTENDING,
			"Nie posiadasz zbroi barbarzyńcy przy sobie! Idź i zdobądź!",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "armor"),
				ConversationStates.ATTENDING,
				"Czekam na  #zbroję barbarzyńcy od Ciebie. Teraz jestem dość silny, aby uciec.",
				null);
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Uwięziony Barbarzyńca",
				"Lorenz jest uwięzionym barbarzyńcą na Athor Island. Dowiesz się dlaczego jest tam więziony i czy mu jakoś pomożesz?",
				true);
		step1();
		step2();
		step3();
		step4();
		step5();
		step6();
		step7();
		step8();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Odnalazłem drogę do chaty Lorenza.");
		res.add("Lorenz potrzebuje kosę, ale nie starą, aby ściąć kwiaty. Uważa je za brzydkie.");
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Lorenzowi w  wycięciu kwiatów. Za cokolwiek skazany, założę się, że na to zasłużył.");
			return res;
		} 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add("Lorenz chce abym udał się do Princess Esclara z zapytaniem: 'dlaczego on pojmany'. Muszę powiedzieć jej jego imię aby jej przypomnieć.");
		if ("capture".equals(questState)) {
			return res;
		} 
		res.add("Księżniczka powiedziała mi, że Lorenz jest skazany za kopanie tunelu! Tak więc należy mu powiedzieć, to za tunel.");
		if ("princess".equals(questState)) {
			return res;
		}
		res.add("Lorenz nagle zgłodniał i zażądał dostarczenia mu jajka. On jest naprawdę wybuchowy.");
		if ("egg".equals(questState)) {
			return res;
		}
		res.add("Teraz muszę powiedzieć Princess Ylflia, dlaczego Lorenz nie było tak długo ... Nie jestem nawet pewien, czy zna ją! Ale i tak muszę powiedzieć jej jego imię.");
		if ("jailed".equals(questState)) {
			return res;
		}
		res.add("Princess Ylflia prosił mnie, aby przekazał Lorencowi pozdrowienia.");
		if ("spoken".equals(questState)) {
			return res;
		}
		res.add("Lorenz w końcu postanowił spróbować uwolnić się. Muszę mu znaleść zbroję barbarzyńy.");
		if ("armor".equals(questState)) {
			return res;
		}
		res.add("Przyniosłem Lorenzowi zbroje! Dał mi zrabowane złoto Princesin i zarobiłem dużo doświadczenia.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}
	
	@Override
	public String getName() {
		return "JailedBarbarian";
	}
	
	// Amazon is dangerous below this level - don't hint to go there
	@Override
	public int getMinLevel() {
		return 70;
	}
	
	@Override
	public String getRegion() {
		return Region.AMAZON_ISLAND;
	}
	@Override
	public String getNPCName() {
		return "Lorenz";
	}
}
 
