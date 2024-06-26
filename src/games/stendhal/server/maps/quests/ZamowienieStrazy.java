/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemdataItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class ZamowienieStrazy extends AbstractQuest {
	private static final String QUEST_SLOT = "zamowienie_strazy";

	private static final int ILOSC_ZELAZA = 150;
	private static final int ILOSC_MIEDZI = 40;
	private static final int ILOSC_ZLOTA = 20;

	private static final String DESCRIPTION = "Oto zamówienie gwardzisty.";

	public void step_1() {
	    final SpeakerNPC npc = npcs.get("Inez");
	    final ChatAction action = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item item = SingletonRepository.getEntityManager().getItem("karteczka");
				item.setItemData(QUEST_SLOT);
				item.setDescription(DESCRIPTION);
				item.setBoundTo(player.getName());
				player.equipOrPutOnGround(item);
				player.setQuest(QUEST_SLOT, "start");
			}
		};

	    npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Dobrze, że pytasz... Ostatnio otrzymaliśmy spore zamówienie na wyposażenie straży królewskiej. Chciałbyś nam pomóc?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemdataItemWithHimCondition("karteczka", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Super. Najpierw przekaż tę karteczkę z zamówieniem do kowala #'Samsona'. On ci dokładnie określi ile będziemy potrzebować zapasów.",
			action);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new PlayerHasItemdataItemWithHimCondition("karteczka", QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Hej! Zanieś tą karteczke do kowala!",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Trudno... Może nam się to zamówienie trochę wydłuży ale jakoś sobie poradzimy...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("Samsona", "Samson", "kowal", "kowal Samson"),
			null,
			ConversationStates.ATTENDING,
			"Kowal Samson znajduje się na dworze, zaraz obok naszej kuźni.",
			null);
	}

	public void step_2() {
	    final SpeakerNPC npc = npcs.get("Samson");

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zamówienie", "zamówienia", "karteczka", "list", "straż królewska"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
				new PlayerHasItemdataItemWithHimCondition("karteczka", QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED,
			"Kolejne już zamówienie w tym miesiącu. Co teraz chcą? Ouuu... Już dawno takiego ogromnego zamówienia nie mieliśmy. Będziemy potrzebować niemałej pomocy. Pomożesz?",
			new DropItemdataItemAction("karteczka", QUEST_SLOT));

	    npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
				new NotCondition(new PlayerHasItemdataItemWithHimCondition("karteczka", QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Hmmm? O czym mówiesz?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Wspaniale. Jakbyś mógł to przynieś mi 150 żelaza, 40 rud miedzi i 20 sztabek złota. Będę tutaj na Ciebie czekał. Jak wrócisz to przypomnij mi mówiąc #'zamówienie', ok? Powodzenia!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "zapasy", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Trudno... Może nam się to zamówienie trochę wydłuży ale jakoś sobie poradzimy...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	public void step_3() {
	    final SpeakerNPC npc = npcs.get("Samson");
	    final ChatAction action = new MultipleActions(
	    	new IncreaseXPAction(15000),
	    	new SetQuestAction(QUEST_SLOT, "r_zapasow"),
		    new DropItemAction("sztabka żelaza", ILOSC_ZELAZA)
	    );

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zamówienie", "zamówienia", "karteczka", "list", "straż królewska"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "zapasy"),
				new PlayerHasItemWithHimCondition("sztabka żelaza", ILOSC_ZELAZA),
				new PlayerHasItemWithHimCondition("ruda miedzi", ILOSC_MIEDZI),
				new PlayerHasItemWithHimCondition("sztabka złota", ILOSC_ZLOTA)),
			ConversationStates.IDLE,
			"Ooo.. Świetnie, w końcu przyniosłeś żelazo. Proszę, zanieś resztę zapasów do mojego #'asystenta' i powiedz mu #'zapasy', to będzie wiedział, że nareszcie je przyniosłeś.",
			action);

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zamówienie", "zamówienia", "karteczka", "list", "straż królewska"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "zapasy"),
				new NotCondition(
					new AndCondition(new PlayerHasItemWithHimCondition("sztabka żelaza", ILOSC_ZELAZA),
						new PlayerHasItemWithHimCondition("ruda miedzi", ILOSC_MIEDZI),
						new PlayerHasItemWithHimCondition("sztabka złota", ILOSC_ZLOTA)))),
			ConversationStates.ATTENDING,
			"Chyba Ci coś już wcześniej mówiłem. Masz mi przynieść 150 żelaza, 40 rud miedzi i 20 sztabek złota.",
			null);

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("asystent kowala", "asystent", "pomocnik kowala", "pomocnik"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "r_zapasow"),
			ConversationStates.ATTENDING,
			"Mój asystent zajmuje się zamówieniami oraz rozporządza spis wszystkich zapasów jakie posiadamy. Znajdziesz go w środku kuźni.",
			null);
	}

	public void step_4() {
	    final SpeakerNPC npc = npcs.get("Inez");
	    final ChatAction action = new MultipleActions(
	    	new IncreaseXPAction(5000),
	    	new SetQuestAction(QUEST_SLOT, "czas"),
		    new DropItemAction("ruda miedzi", ILOSC_MIEDZI),
		    new DropItemAction("sztabka złota", ILOSC_ZLOTA)
	    );

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zapasy", "reszta zapasów", "zapas"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "r_zapasow"),
				new PlayerHasItemWithHimCondition("ruda miedzi", ILOSC_MIEDZI),
				new PlayerHasItemWithHimCondition("sztabka złota", ILOSC_ZLOTA)),
			ConversationStates.ATTENDING,
			"Super! To ja zacznę rozporządzać cały plan jak to ma wszystko wyglądać. Zapytaj się kowala ile mu #'czasu' zajmie wykuwanie zbroi dla straży.",
			action);

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zapasy", "reszta zapasów", "zapas"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "r_zapasow"),
				new NotCondition(
					new AndCondition(new PlayerHasItemWithHimCondition("ruda miedzi", ILOSC_MIEDZI),
						new PlayerHasItemWithHimCondition("sztabka złota", ILOSC_ZLOTA)))),
			ConversationStates.ATTENDING,
			"Chyba coś nie dosłyszałem...",
			null);

	    npc.add(ConversationStates.ATTENDING,
			"czasu",
			new QuestStateStartsWithCondition(QUEST_SLOT, "czas"),
			ConversationStates.ATTENDING,
			"Powiedz mu po prostu #'czas', a on już sam określi ile mu zajmie wykuwanie zbroi.",
			null);
	}

	public void step_5() {
	    final SpeakerNPC npc = npcs.get("Samson");

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("czas", "czasu", "time"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "czas"),
			ConversationStates.ATTENDING,
			"Chodzi Ci ile mi zajmie wykuwanie zbroi dla straży? Inez się o to pytał? To przekaż mu, że potrwa to co najmniej #'miesiąc'.",
			new SetQuestAction(QUEST_SLOT, "powrot"));
	}

	public void step_6() {
	    final SpeakerNPC npc = npcs.get("Inez");

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("miesiąc", "miesiąca", "month"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "powrot"),
			ConversationStates.ATTENDING,
			"Cały miesiąc? Dobrze.. To możesz przekazać #'Gwardziście', że wykucie całego uzbrojenia zajmie nam #'miesiąc'.",
			new SetQuestAction(QUEST_SLOT, "gwardzista"));

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("Gwardziście", "gwardziście", "gwardzista"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "gwardzista"),
			ConversationStates.ATTENDING,
			"Gwardzista znajduje się na półnac stąd.",
			null);
	}

	public void step_7() {
	    final SpeakerNPC npc = npcs.get("Gwardzista");

	    npc.add(ConversationStates.ATTENDING,
			Arrays.asList("miesiąc", "miesiąca", "month"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "gwardzista"),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Cooo?! Miesiąc?! Moja armia potrzebuje na teraz! No cóż... Proszę, w nagrodę przyjmij to, magiczny srebrny pierścień. Uchroni Cię przed mrokiem. Słyszałem również, że gdzieś w rejonach wieliczki można go ulepszyć, lecz to są jedynie plotki.");
					final Item item = SingletonRepository.getEntityManager().getItem("srebrny pierścień");
					item.setBoundTo(player.getName());
					player.equipOrPutOnGround(item);
					player.addXP(10000);
					player.addKarma(25);
					player.setQuest(QUEST_SLOT, "done");
				}
			});
	}

    @Override
    public void addToWorld() {
    	fillQuestInfo(
			"Zamówienie Straży",
			"Gwardzista złożył zamówienie u asystenta kowala na kilkadziesiąt zbroi wykonanych z dobrego materiału dla straży królewskiej.",
			false);
        step_1();
        step_2();
        step_3();
        step_4();
        step_5();
        step_6();
        step_7();
    }

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		return res;
	}

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    @Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}

    @Override
    public String getName() {
        return "Zamówienie Straży";
    }

    @Override
	public String getNPCName() {
		return "Gwardzista";
	}
}
