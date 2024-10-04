/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.quest.DeliverItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.houses.HouseBuyingMain;
import games.stendhal.server.maps.semos.bakery.ChefNPC;
import games.stendhal.server.util.QuestUtils;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * QUEST: Pizza Delivery
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Leander (the baker in Semos)
 * <li> NPC's all over the world (as customers)
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Leander gives you a pizza and tells you who ordered it, and how much
 * time you have to deliver.
 * <li> As a gimmick, you get a pizza delivery uniform.
 * <li> You walk to the customer and say "pizza".
 * <li> The customer takes the pizza. If you were fast enough, you get a tip.
 * <li> You put on your original clothes automatically.
 * </ul>
 * REWARD:
 * <ul>
 * <li> XP (Amount varies depending on customer. You only get half of the XP if
 * the pizza has become cold.)
 * <li> some karma if delivered on time (5)
 * <li> gold coins (As a tip, if you were fast enough; amount varies depending
 * on customer.)
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> As many as wanted, but you can't get a new task while you still have the
 * chance to do the current delivery on time.
 * </ul>
 */
public class PizzaDelivery implements QuestManuscript {
	private static final String questSlot = "pizza_delivery";

	@Override
	public DeliverItemQuestBuilder story() {
		DeliverItemQuestBuilder quest = new DeliverItemQuestBuilder();

		quest.info()
			.name("Dostawca Pizzy")
			.description("Pizzeria Leandera radzi sobie tak dobrze, że teraz rekrutuje chętnych na dostawcę.")
			.internalName(questSlot)
			.repeatableAfterMinutes(0)
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Leander");

		quest.history()
			.whenNpcWasMet("Napotkany został Leander, piekarz Semos.")
			.whenQuestWasRejected("Poprosił mnie aby pomóc z dostawą pizzy, ale nie chcę nosić pizzy.")
			.whenQuestWasAccepted("Pomogę z jego pizzą i zostanę dostawcą.")
			.whenItemWasGiven("Leander dał mi [flavor] dla [customerName].")
			.whenToldAboutCustomer("Leander powiedział mi: \"[customerDescription]\"")
			.whenInTime("Jeśli się pospieszę, może jeszcze dotrę na miejsce z gorącą pizzą.")
			.whenOutOfTime("Pizza już wystygła.")
			.whenQuestWasCompleted("Ostatnia pizza została dostarczona, którą dał mi Leander.")
			.whenQuestCanBeRepeated("Ale założę się, że Leander ma więcej zamówień.")
			.whenEarlyCompletionsShown("Udało się dostarczyć [count] ciepłych [pizza].");

		quest.offer()
			.respondToRequest("Musisz szybko dostarczyć gorącą pizzę. Jeśli będziesz wystarczająco szybko, możesz otrzymać całkiem niezły napiwek. Więc, zrobisz to?")
			.respondIfUnableToWearUniform("Przykro mi, ale nie możesz tak nosić naszego uniformu dostawcy pizzy. Jeśli się przebierzesz, możesz ponownie zapytać o #zadanie.")
			.respondToUnrepeatableRequest("Bardzo ci dziękuje za pomoc. W tej chwili nie mam żadnych innych zamówień.")
			.respondToAccept("Musisz dostarczyć [flavor] dla [customerName] w czasie [time]. Przekaż \"pizza\" [customerName], będzie wtedy wiedzieć, że jesteś ode mnie. Och, oraz proszę załóż ten strój dostawcy.")

			.respondToReject("Szkoda. Mam nadzieję, że moja córka #Sally wkrótce wróci ze swojego obozu, aby pomóc mi przy zamówieniach.")
			.remind("Nadal musisz dostarczyć pizzę dla [customerName], oraz pospiesz się!")
			.respondIfLastQuestFailed("Widzę, że nie udało Ci się dostarczyć pizzy dla [customerName] na czas. Czy tym razem dasz radę dostarczyć na czas?")
			.respondIfInventoryIsFull("Wróć, gdy znajdziesz nieco miejsca aby nieść pizze!")
			.respondToAnotherIfLostItem("Zgubiłeś [item]? Cóż, będzie cię to kosztować [charge] money. Więc chcesz kolejną?");

		quest.task()
			.itemName("pizza")
			.itemDescription("Oto [flavor].")
			.outfitUniform(new Outfit(null, Integer.valueOf(990), null, null, null, null, null, null, null))
			.chargeForLostItem(500);

		// Don't add Sally here, as it would conflict with Leander telling
		// about his daughter.
		quest.task().order()
			.customerNpc("Balduin")
			.customerDescription("Balduin to pustelnik żyjący na górze pomiędzy Semos i Ados. Nazywa się Ados Rock. Idź stąd na wschód.")
			.itemDescription("Pizza Prosciutto")
			// Tested by mort: 6:30 min, with killing some orcs.
			.minutesToDeliver(7)
			// Quite high because you can't do much senseful on top of the hill and must walk down again.
			.tipOnFastDelivery(200)
			.xpReward(300)
			.respondToFastDelivery("Dzięki! Zastanawiam się, jak udało ci się to tu tak szybko dostarczyć. Weź te [tip] sztuk złota jako napiwek. I tak nie mogę ich tu wydać!")
			.respondToSlowDelivery("Brrr. Ta [flavor] nie jest już gorąca. Cóż, w każdym razie dziękuję za wysiłek.")
			.playerMinLevel(10);

		quest.task().order()
			.customerNpc("Cyk")
			.customerDescription("Cyk jest obecnie na wakacjach na wyspie Athor. Z łatwością rozpoznacie go po niebieskich włosach. Idź na południowy wschód, aby znaleźć prom Athor.")
			.itemDescription("Pizza Hawaii")
			// You need about 6 min to Eliza, up to 12 min to wait for the
			// ferry, 5 min for the crossing, and 0.5 min from the docks to 
			// the beach, so you need a bit of luck for this one.
			.minutesToDeliver(20)
			.tipOnFastDelivery(300)
			.xpReward(500)
			.respondToFastDelivery("Wow, nigdy nie wierzyłem, że naprawdę dostarczysz to przez połowę świata! Masz, weź te [flavor] monet!")
			.respondToSlowDelivery("Zrobiło się zimne, ale czego się spodziewać, kiedy zamówię pizzę z tak odległej piekarni... W każdym razie dziękuję.")
			.playerMinLevel(20);

		quest.task().order()
			.customerNpc("Eliza")
			.customerDescription("Eliza pracuje na promie na wyspę Athor. Znajdziesz ją w dokach na południe od bagien Ados.")
			.itemDescription("Pizza del Mare")
			// minutes to deliver. Tested by mort: 6 min, ignoring slow animals and not
			// walking through the swamps.
			.minutesToDeliver(7)
			.tipOnFastDelivery(170)
			.xpReward(300)
			.respondToFastDelivery("Niesamowite! Nadal jest gorące! Tutaj, kup coś ładnego za te [tip] sztuk złota!")
			.respondToSlowDelivery("Jaka szkoda. Zrobiła się zimna. Niemniej jednak, dziękuję!")
			.playerMinLevel(20);

		quest.task().order()
			.customerNpc("Fidorea")
			.customerDescription("Fidorea mieszka w mieście Ados. Ona jest wizażystką. Będziesz musiał iść stąd na wschód.")
			.itemDescription("Pizza Napoli")
			// Tested by mort: about 6 min, outrunning all enemies.
			.minutesToDeliver(7)
			.tipOnFastDelivery(150)
			.xpReward(200)
			.respondToFastDelivery("Wielkie dzięki! Jesteś urodzonym dostawcą pizzy. Możesz przyjąć te [tip] sztuk złota jako napiwek!")
			.respondToSlowDelivery("Porażka. Zimna pizza.")
			.playerMinLevel(15);

		quest.task().order()
			.customerNpc("Haizen")
			.customerDescription("Haizen to czarodziej mieszkający w chatce niedaleko drogi do Ados. Musisz iść na północny wschód stąd.")
			.itemDescription("Pizza Diavola")
			// minutes to deliver. Tested by kymara: exactly 3 min.
			.minutesToDeliver(4)
			.tipOnFastDelivery(80)
			.xpReward(150)
			.respondToFastDelivery("Ach, moja [flavor]! I jest świeżo wyjęta z piekarnika! Weź te [tip] monety jako napiwek!")
			.respondToSlowDelivery("Mam nadzieję, że następnym razem, gdy zamówię pizzę, będzie jeszcze gorąca.")
			.playerMinLevel(10);

		quest.task().order()
			.customerNpc("Jenny")
			.customerDescription("Jenny jest właścicielką młyna na równinach na północ i trochę na wschód od Semos.")
			.itemDescription("Pizza Margherita")
			// Tested by mort: can be done in 1:15 min, with no real danger.
			.minutesToDeliver(2)
			.tipOnFastDelivery(20)
			.xpReward(50)
			.respondToFastDelivery("Ach, moja [flavor]! Bardzo miło z twojej strony! Weź [tip] monet jako napiwek!")
			.respondToSlowDelivery("Szkoda. Twoja pizzeria nie może dostarczyć gorącej pizzy, chociaż piekarnia znajduje się tuż za rogiem.")
			.playerMinLevel(2);

		quest.task().order()
			.customerNpc("Jynath")
			.customerDescription("Jynath to wiedźma mieszkająca w małej chatce na południe od zamku Or'ril. Znajdziesz ją gdy pójdziesz na południowy zachód od Semos, przez cały las, a następnie będziesz podążać ścieżką na zachód, aż zobaczysz jej chatę.")
			.itemDescription("Pizza Funghi")
			// Tested by mort: 5:30 min, leaving the slow monsters on the way behind.
			.minutesToDeliver(6)
			.tipOnFastDelivery(140)
			.xpReward(200)
			.respondToFastDelivery("Och, nie spodziewałam się ciebie tak wcześnie. Świetnie! Zwykle nie daję napiwków, ale dla Ciebie zrobię wyjątek. Oto [tip] sztuk złota.")
			.respondToSlowDelivery("Szkoda... Będę musiała użyć wyjątkowo silnego zaklęcia, aby pizza znów była gorąca.")
			.playerMinLevel(5);

		quest.task().order()
			.customerNpc("Katinka")
			.customerDescription("Katinka opiekuje się zwierzętami w schronisku Ados Wildlife Refuge. To na północny wschód stąd, w drodze do miasta Ados.")
			.itemDescription("Pizza Vegetale")
			// Tested by kymara in 3:25 min, leaving behind the orcs.
			.minutesToDeliver(4)
			.tipOnFastDelivery(100)
			.xpReward(200)
			.respondToFastDelivery("Jest! Moja [flavor]! Przyjmij proszę [tip] sztuk złota jako napiwek!")
			.respondToSlowDelivery("Ej... Nienawidzę zimnej pizzy. Chyba nakarmię tym zwierzęta.")
			.playerMinLevel(10);

		quest.task().order()
			.customerNpc("Marcus")
			.customerDescription("Marcus jest strażnikiem w więzieniu Semos. Jest na zachód stąd, za wioską Semos.")
			.itemDescription("Pizza Tonno")
			// Tested by kymara: takes longer than before due to fence in village
			.minutesToDeliver(3)
			// A bit higher than Jenny because you can't do anything
			// else in the jail and need to walk out again.
			.tipOnFastDelivery(25)
			.xpReward(100)
			.respondToFastDelivery("Ach, moja [flavor]! Oto twój napiwek: [tip] sztuk złota.")
			.respondToSlowDelivery("Nareszcie! Dlaczego to trwało tak długo?")
			.playerMinLevel(2);

		quest.task().order()
			.customerNpc("Nishiya")
			.customerDescription("Nishiya sprzedaje owce. Znajdziesz go na zachód stąd, w wiosce Semos.")
			.itemDescription("Pizza Pasta")
			// Tested by mort: easy to do in less than 1 min.
			.minutesToDeliver(1)
			.tipOnFastDelivery(10)
			.xpReward(25)
			.respondToFastDelivery("Dziękuję! Ale szybko dostarczono. Weź [tip] sztuk złota jako napiwek!")
			.respondToSlowDelivery("Szkoda. Zrobiła się zimna. Mimo wszystko dziekuję.")
			.playerMinLevel(0);

		quest.task().order()
			.customerNpc("Ouchit")
			.customerDescription("Ouchit jest handlarzem bronią. Obecnie wynajmuje pokój na piętrze w tawernie Semos, tuż za rogiem.")
			.itemDescription("Pizza Quattro Stagioni")
			// Tested by mort: can be done in 45 sec with no danger.
			.minutesToDeliver(1)
			.tipOnFastDelivery(10)
			.xpReward(25)
			.respondToFastDelivery("Dziękuję! Miło jest mieć pizzę tuż za rogiem. Weź proszę [tip] monet!")
			.respondToSlowDelivery("Powinienem był raczej odebrać to osobiście w piekarni, tak byłoby szybciej.")
			.playerMinLevel(0);

		quest.task().order()
			.customerNpc("Ramon")
			.customerDescription("Ramon pracuje jako krupier blackjacka na promie płynącym na wyspę Athor. Główny prom znajduje się na południowy wschód stąd - to długa droga!")
			.itemDescription("Pizza Bolognese")
			// You need about 6 mins to Eliza, and once you board the ferry,
			// about 15 sec to deliver. If you have bad luck, you need to
			// wait up to 12 mins for the ferry to arrive at the mainland, so
			// you need a bit of luck for this one.
			.minutesToDeliver(14)
			.tipOnFastDelivery(250)
			.xpReward(400)
			.respondToFastDelivery("Bardzo dziękuję! Wreszcie dostaję coś lepszego niż okropne jedzenie, które gotuje Laura. Weź te [tip] sztuk złota jako napiwek!")
			.respondToSlowDelivery("Szkoda. Jest zimna. A miałem nadzieję, że dostanę coś lepszego niż to jedzenie z kuchni.")
			.playerMinLevel(20);

		quest.task().order()
			.customerNpc("Tor'Koom")
			.customerDescription("Tor'Koom to ork żyjący w lochach pod miastem, Semos. Owce są jego ulubionym pożywieniem. Mieszka na 4 poziomie pod ziemią. Bądź ostrożny!")
			.itemDescription("Pizza Pecora")
			// Tested by kymara:
			// done in about 8 min, with lots of monsters getting in your way.
			.minutesToDeliver(9)
			.tipOnFastDelivery(170)
			.xpReward(300)
			.respondToFastDelivery("Pyszna [flavor]! Proszę, weź [tip] pieniędzy!")
			.respondToSlowDelivery("Wrr. Pizza zimna. Idziesz powoli jak owca.")
			.playerMinLevel(15);

		quest.task().order()
			.customerNpc("Martin Farmer")
			.customerDescription("Martin Farmer spędza wakacje w mieście Ados. Znajdziesz go na wschód stąd.")
			.itemDescription("Pizza Fiorentina")
			// Time for Fidorea was 7, so 8 should be ok for martin
			.minutesToDeliver(8)
			.tipOnFastDelivery(160)
			.xpReward(220)
			.respondToFastDelivery("Oooch, uwielbiam świeżą, gorącą pizzę, dzięki. Weź te [tip] pieniądzy...!")
			.respondToSlowDelivery("Hmpf... zimna pizza.. ok.. wezmę. Ale postaraj się następnym razem być szybciej.")
			.playerMinLevel(10);

		quest.complete()
			.respondToItemWithoutQuest("Ej! Ta pizza jest cała brudna! Znalazłeś to na ziemi?")
			.respondToItemForOtherNPC("Nie, dziękuję. Dla mnie lepsza jest [flavor].")
			.respondToMissingItem("Pizza? Gdzie?")
			.npcStatusEffect("pizza");

		return quest;
	}

	public boolean removeFromWorld() {
		boolean res = ResetSpeakerNPC.reload(new ChefNPC(), "Leander")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.rock.WeaponsCollectorNPC(), "Balduin")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.coast.FerryConveyerNPC(), "Eliza")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.city.MakeupArtistNPC(), "Fidorea")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.magician_house.WizardNPC(), "Haizen")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.semos.plains.MillerNPC(), "Jenny")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.orril.magician_house.WitchNPC(), "Jynath")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.outside.AnimalKeeperNPC(), "Katinka")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.semos.jail.GuardNPC(), "Marcus")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.semos.village.SheepSellerNPC(), "Nishiya")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.semos.tavern.BowAndArrowSellerNPC(), "Ouchit")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.semos.dungeon.SheepBuyerNPC(), "Tor'Koom")
			&& ResetSpeakerNPC.reload(new games.stendhal.server.maps.ados.wall.HolidayingManNPC(), "Martin Farmer");
		final StendhalQuestSystem quests = SingletonRepository.getStendhalQuestSystem();
		// reload other quests associated with NPCs
		quests.reloadQuestSlots(
			// Balduin
			"weapons_collector", "weapons_collector2", "ultimate_collector",
			// Fidorea
			QuestUtils.evaluateQuestSlotName("paper_chase_20[year]"),
			// Haizen
			"maze",
			// Jenny
			"kill_gnomes",
			// Jynath
			"ceryl_book",
			// Katinka
			"zoo_food",
			// Nishiya
			"sheep_growing",
			// Ouchit
			"bows_ouchit"
		);
		final NPCList npcs = SingletonRepository.getNPCList();
		// Cyk & Ramon are not loaded via zone configurators
		SpeakerNPC npc = npcs.get("Ramon");
		StendhalRPZone npczone;
		if (npc != null) {
			npczone = npc.getZone();
			if (npczone != null) {
				npczone.remove(npc);
				res = res && npcs.get("Ramon") == null;
				quests.reloadQuestSlots("blackjack");
			}
		}
		npc = npcs.get("Cyk");
		if (npc != null) {
			npczone = npc.getZone();
			if (npczone != null) {
				npczone.remove(npc);
				res = res && npcs.get("Cyk") == null;
				new HouseBuyingMain().createAthorNPC(npczone);
			}
		}
		return res;
	}
}