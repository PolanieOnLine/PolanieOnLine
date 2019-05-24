/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
// Based on HelpMrsYeti.

package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncreaseBaseHPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;


public class HelpKrasnolud extends AbstractQuest {

	private static final String QUEST_SLOT = "krasnolud";
	private static final int DELAY_IN_MINUTES = 60*48;

	private static Logger logger = Logger.getLogger(HelpKrasnolud.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void startQuest() {
		final SpeakerNPC npc = npcs.get("Krasnolud");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Potrzebuję mocnego a zarazem dość rzadkiego pancerza. Czy mógłbym mieć do ciebie prośbę byś go dla mnie zdobył?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wspaniale! Nareszcie mam zbroje o jakiej marzyłem.",
				null);


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dziękuję ci za chęci i dobre serce! Musisz udać się do Wielkoluda i powiedzieć mu, iż potrzebujesz #zbroję. On będzie wiedział, o czym mowa.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Nie! Łaski bez, ktoś inny o szlachetnym sercu mnie wesprze w tej sprawie...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
	}

	private void makeArmor() {

	final SpeakerNPC npc = npcs.get("Wielkolud");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("krasnolud", "armor", "zbroja","zbroję"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, "Ten krasnal wysłał cię po lazurową zbroję powiadasz... Ostatnio chyba ktoś przyniósł do mnie coś takiego. "
				+ " Ale jak wiesz nic nie ma za darmo, potrzebuję koniecznie #'/lazurowy hełm/' dostarcz mi go a może dostaniesz tę zbroję o ile ją gdzieś tu mam... "
				+ " Tymczasem udaj się do Gulimo w górach Ados on podobnież jest w posiadaniu takowego hełmu, o który cię proszę."
				+ " Wystarczy jak powiesz mu moje imię #/Wielkolud/, on już będzie wiedział, o co chodzi.",
				new SetQuestAction(QUEST_SLOT, "gulimo"));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("wielkolud","helmet", "hełm"),
			new NotCondition(new QuestInStateCondition(QUEST_SLOT, "helmet")),
			ConversationStates.ATTENDING,
			"Zanim ci pomogę udaj się po hełm, o który cię prosiłem do Gulimo i powiedz mu moje imię by wiedział, od kogo przybywasz..",
			null);

		npc.add(ConversationStates.ATTENDING,  Arrays.asList("wielkolud", "helmet", "zbroja", "armor"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "helmet"),
				new PlayerHasItemWithHimCondition("lazurowy hełm")),
				ConversationStates.ATTENDING, "Bardzo dobrze! Teraz chcę byś jeszcze przyniósł mi przedmioty z listy zanim dam ci #/armor/."
				+ " Potrzebuję:\n"
     		   + "#'10 skór czerwonego smoka'\n"+ "#'10 skór zielonego smoka'\n"+ "#'10 skór niebieskiego smoka'\n"+ "#'20 skór czarnego smoka'\n"+ "i #'3 złotego smoka'",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "armor"), new DropItemAction("lazurowy hełm")));

		npc.add(ConversationStates.ATTENDING,  Arrays.asList("wielkolud", "hełm", "zbroja", "armor"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "helmet"),
				new NotCondition(new PlayerHasItemWithHimCondition("lazurowy hełm"))),
				ConversationStates.ATTENDING, "Rozumiem, iż byłeś już u Gulimo. Więc gdzie masz lazurowy hełm, o który cię prosiłem?",
				null);

		final List<ChatAction> armoractions = new LinkedList<ChatAction>();
		armoractions.add(new DropItemAction("skóra czerwonego smoka",10));
		armoractions.add(new DropItemAction("skóra zielonego smoka",10));
		armoractions.add(new DropItemAction("skóra niebieskiego smoka",10));
		armoractions.add(new DropItemAction("skóra czarnego smoka",20));
		armoractions.add(new DropItemAction("skóra złotego smoka",3));
		armoractions.add(new EquipItemAction("zbroja lazurowa"));
		armoractions.add(new IncreaseXPAction(100000));
		armoractions.add(new SetQuestAction(QUEST_SLOT, "gotarmor"));

		// don't make player wait for potion - could add this in later if wanted
		npc.add(ConversationStates.ATTENDING,  Arrays.asList("wielkolud", "zbroja", "armor"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"),
								new PlayerHasItemWithHimCondition("skóra czerwonego smoka",10),
								new PlayerHasItemWithHimCondition("skóra zielonego smoka",10),
								new PlayerHasItemWithHimCondition("skóra niebieskiego smoka",10),
								new PlayerHasItemWithHimCondition("skóra czarnego smoka",20),
								new PlayerHasItemWithHimCondition("skóra złotego smoka",3)),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko, o co cię prosiłem. A oto lazurowa zbroja, pozdrów ode mnie Krasnoluda. Oby częściej przysyłał takich wojowników jak ty.",
				new MultipleActions(armoractions));

		npc.add(ConversationStates.ATTENDING,  Arrays.asList("wielkolud", "zbroja", "armor"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "armor"),
								new NotCondition(
												new AndCondition(new PlayerHasItemWithHimCondition("skóra czerwonego smoka",10),
																new PlayerHasItemWithHimCondition("skóra zielonego smoka",10),
																new PlayerHasItemWithHimCondition("skóra niebieskiego smoka",10),
																new PlayerHasItemWithHimCondition("skóra czarnego smoka",20),
																new PlayerHasItemWithHimCondition("skóra złotego smoka",3)))),
				ConversationStates.ATTENDING, "Potrzebuję 10 skór czerwonego smoka, 10 skór niebieskiego smoka, 10 skór zielonego smoka, 20 skór czarnego smoka i 3 skóry złotego smoka."
				+ " Proszę dostarcz mi wszystko z listy naraz by nie zawracać mi głowy ciągle... Powodzenia!", null);
	}

	private void makeHelmet() {
		// although the player does end up just taking an ordinary knife to salva, this step must be completed
		// (must be in quest state 'knife' when they take the knife)
	final SpeakerNPC npc = npcs.get("Gulimo");
		npc.add(ConversationStates.ATTENDING, "wielkolud",
				new QuestInStateCondition(QUEST_SLOT, "gulimo"),
				ConversationStates.ATTENDING, "Twierdzisz, że Wielkolud cię przysyła do mnie. Zapewne chodzi o Hełm. Hełm... Hełm... Lazurowy hełm gdzie ja go położyłem... Ech pamięć już nie ta, co kiedyś. "
				+ " Jest coś co potrzebuję do mojej kolekcji a mianowicie #kieł smoka."
				+ " Ja poszukam hełmu a ty mógłbyś w tym czasie przynieść 50 kłów smoka.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "kiel_smoka", 1.0));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wielkolud", "kieł", "smok"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "kiel_smoka"),
				new PlayerHasItemWithHimCondition("kieł smoka",50)),
				ConversationStates.ATTENDING, "Pięknie ci dziękuję! Niestety mam złe wieści nie znalazłem tego hełmu... Chyba go gdzieś wydałem. "
				+ " Ale poczekaj niech pomyślę... Tak wiem! Gdzieś na wyższym piętrze jest ork o imieniu Hagnurk "
				+ " udaj się do niego, handluje, bowiem on szczególnym orężem powinien mieć w swym kramie ten hełm",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "helmet", 1.0), new DropItemAction("kieł smoka",50)));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("wielkolud", "kieł", "smok"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "kiel_smoka"),
				new NotCondition(new PlayerHasItemWithHimCondition("kieł smoka",50))),
				ConversationStates.ATTENDING, "Kły smoka zdobędziesz zabijając smoki. To chyba oczywiste! Liczę, że wejdziesz w ich posiadanie, ale nie kupując od innych wojowników!",
				null);

	}

	private void bringArmor() {
	final SpeakerNPC npc = npcs.get("Krasnolud");
		final String extraTrigger = "armor";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		final List<ChatAction> tookarmoractions = new LinkedList<ChatAction>();
		tookarmoractions.add(new DropItemAction("zbroja lazurowa"));
		tookarmoractions.add(new IncreaseKarmaAction(100.0));
		tookarmoractions.add(new IncreaseXPAction(100000));
		tookarmoractions.add(new SetQuestAction(QUEST_SLOT, "dragon"));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotarmor"),
				new PlayerHasItemWithHimCondition("zbroja lazurowa")),
				ConversationStates.ATTENDING, "Dziękuję! Wygląda bardzo solidnie. Mam jeszcze jedno zadanie dla ciebie. Uwielbiam małe #smoki. Bądź tak miły i przyprowadź mi jednego.",
				new MultipleActions(tookarmoractions));

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotarmor"), new NotCondition(new PlayerHasItemWithHimCondition("zbroja lazurowa"))),
			ConversationStates.ATTENDING,
			"Gdzieżeś zapodział moją zbroję?",
			null);

		npc.add(ConversationStates.ATTENDING,
				questTrigger,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								new QuestInStateCondition(QUEST_SLOT, "kiel_smoka"),
								new QuestInStateCondition(QUEST_SLOT, "helmet")),
				ConversationStates.ATTENDING,
				"Czekam byś przyniósł zbroję. Poproś Wielkoluda o zbroję.",
				null);
	}

	private void bringDragon() {
	final SpeakerNPC npc = npcs.get("Krasnolud");

		final String extraTrigger = "smoki";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		// easy to check if they have a pet or sheep at all
		npc.add(
				ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"),
								new NotCondition(new PlayerHasPetOrSheepCondition())),
				ConversationStates.ATTENDING,
				"Smoki wykluwają się z mitycznego jaja. Możesz je zdobyć od Morgrina ze szkoły magicznej znajdującej się w magicznym mieście. "
				+ " Terry jest hodowcą smoków mieszkającym w jaskiń nieopodal na wschód od Semos on wyhoduje ci z owego jaja małego smoka.",
				null);

		// if they have any pet or sheep, then check if it's a baby dragon
		npc.add(
				ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"),
								new PlayerHasPetOrSheepCondition()),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
									 final EventRaiser npc) {
						if(!player.hasPet()){
							npc.say("Jaką miłą owieczkę prowadzisz. Ja jednak potrzebuję smoka małego. Porozmawiaj z Morgrinem ze szkoły magicznej.");
							return;
						}
						Pet pet = player.getPet();
						String petType = pet.get("type");
						if("baby_dragon".equals(petType)) {
							player.removePet(pet);
							npc.say("Przyprowadziłeś małego smoka! Będzie z niego pyszny gulasz! Gulasz z małego smoka to moja specjalność. Wróć tu za dwa dni, a otrzymasz za te #zadanie nagrodę.");
							player.addKarma(5.0);
							player.addXP(50000);
							pet.delayedDamage(pet.getHP(), "Krasnolud");
							player.setQuest(QUEST_SLOT,"reward;"+System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("Jakiego miłego masz pupila. Ja jednak potrzebuję małego smoka. Porozmawiaj z Morgrinem ze szkoły magicznej w sprawie mistycznego jaja.");
						}
					}
			});

	}

	private void getReward() {

	final SpeakerNPC npc = npcs.get("Krasnolud");

		final String extraTrigger = "reward";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(
				ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"),
								// delay is in minutes, last parameter is argument of timestamp
								new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1,DELAY_IN_MINUTES, "Witaj! Teraz szykuję sobie gulasz, nagrodą dla ciebie zajmę się dopiero jak skończę. Więc za"));


		npc.add(
				ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"),
								// delay is in minutes, last parameter is argument of timestamp
								new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Dziękuję! Nagrodą niech będzie podniesienie twojej żywotności a zarazem wytrzymałości w walce z potworami.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT,"done"), new IncreaseBaseHPAction(100), new IncreaseKarmaAction(100.0), new IncreaseXPAction(10000)));

	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pomoc Krasnoludowi",
				"Mężny Krasnolud, stary przyjaciel Władcy Smoków ma dla Ciebie zadanie. Nie spartacz go a Władca smoków być może spojrzy na Ciebie życzliwym okiem.",
				true);
		startQuest();
		makeArmor();
		makeHelmet();
		bringArmor();
		bringDragon();
		getReward();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem Krasnoluda w kuźni Zakopane.");
			res.add("Krasnolud poprosił mnie abym mu przyniósł lazurową zbroje od Wielkoluda.");
			if ("rejected".equals(questState)) {
				res.add("Nie mam ochoty bawić się w posłańca.");
				return res;
			}
			if ("start".equals(questState)) {
				return res;
			}
			res.add("Wielkolud zażądał lazurowego hełmu w zamian za zbroje. Mam udać się do Gulimo po niego i powiedzieć: wielkolud.");
			if ("gulimo".equals(questState)) {
				return res;
			}
			res.add("Gulimo żąda w zamian za hełm 100 kłów smoka.");
			if ("kiel_smoka".equals(questState)) {
				return res;
			}
			res.add("Przyniosłem Gulimo kły, niestety nie miał hełmu. Dał mi podpowiedź, że mogę go kupić u Hagnurk.");
			if ("helmet".equals(questState)) {
				return res;
			}
			res.add("Zaniosłem hełm Wielkoludowi a ten zażądał jeszcze: po 30 skór zielonego, czerwonego, niebieskiego oraz 60 czarnego i 10 złotego smoka.");
			if ("armor".equals(questState)) {
				return res;
			}
			res.add("Dostarczyłem wszystkie skóry Wielkoludowi w końcu dał mi lazurową zbroję dla Krasnoluda");
			if ("gotarmor".equals(questState)) {
				return res;
			}
			res.add("Krasnalud był wdzięczny za przyniesienie zbroi i poprosił mnie abym przyprowadził mu małego smoka.");
			if ("dragon".equals(questState)) {
				return res;
			}
			res.add("No nie ten krasnal postanowił zrobić z biednego smoka gulasz!");
			if (questState.startsWith("reward")) {
				if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
					res.add("Krasnolud po nagrodę kazał zgłosić się za tydzień.");
				} else {
					res.add("Krasnolud kazał mi wrócić za dzień, aby odebrać nagrodę, więc muszę czekać.");
				}
				return res;
			}
			res.add("Krasnolud nagrodził mnie podniesieniem bazy hp o 50 i dostalem sporo karmy oraz xp.");
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
		return "HelpKrasnolud";
	}

		@Override
	public int getMinLevel() {
		return 250;
	}
	@Override
	public String getNPCName() {
		return "Krasnolud";
	}

  @Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
