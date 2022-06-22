/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.tarnow.city;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;

/**
 * @author KarajuSs
 */
public class MythNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();
	private static final int currentFee = 1000;
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Klechdarz") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Chcesz może posłuchać o pewnej #ciekawostce? Oczywiście pobiorę niewielką #opłatę.");
				addJob("Praca? W ramach pracy mogę opowiedzieć różne #historyjki.");
				addHelp("Nie potrzebuję pomocy.");
				addOffer("Mogę sprzedać magiczny zwój, który z powrotem przeniesie cię tutaj.");
				addReply(Arrays.asList("facts","fact","ciekawostka","ciekawostki","ciekawostce","historyjka","historyjki","opłatę","cost"),
						"Za ciekawostkę będzie cię kosztowało 1,000 money! Chcesz ją usłyszeć?");
				addGoodbye("Jak będziesz chciał znów coś posłuchać to wróć.");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("tarnowscrollseller")), false);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(hasFeeCondition),
					ConversationStates.IDLE,
					"Nie masz tyle pieniędzy! Wróć jak będziesz miał, musisz koniecznie to usłyszczeć!",
					null
				);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.YES_MESSAGES,
					hasFeeCondition,
					ConversationStates.ATTENDING,
					null,
                	new MultipleActions(
                		new DropItemAction("money", currentFee),
                    	new SayTextAction("!me patrzy się na drzewo i myśli... i myśli... i myśli."),
                    	new SayTextAction("No dobrze, chyba mam coś dla ciebie ciekawego..."),
                    	new SayTextAction("!me drapie poważnie się po brodzie."),
                    	new SayTextAction(facts)
                	)
			 	);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.NO_MESSAGES,
					ConversationStates.IDLE,
					null,
					new MultipleActions(
	                    new SayTextAction("!me spojrzał na ciebie."),
	                    new SayTextAction("Yhym hmmmm... y.. hmm.."),
	                    new SayTextAction("!me drapie poważnie się po brodzie."),
	                    new SayTextAction("Mądra decyzja! Jak bym był Tobą to sam nie zapłaciłbym! Bądź zdrów!")
	                )
				);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			public void onRejectedAttackStart(RPEntity attacker) {
            	say("!me krzyczy i mówi.");
				say("Jak możesz tak do starszych ludzi! Więcej szacunku młody człowieku!");
				if (attacker.getHP() < 10) {
					say("Tym razem cię nie zranię, ponieważ nie chcę mieć ciebie na sumieniu...");
				} else {
					attacker.onDamaged(attacker, 10);
				}
			}

			@Override
			public void say(final String text) {
				say(text, false);
			}
		};

		npc.setDescription("Oto stary klechdarz. Może opowie nam jakąś historyjkę.");
		npc.setEntityClass("npcstaryklecha");
		npc.setGender("M");
		npc.setPosition(38, 76);
		zone.add(npc);
	}

	final ChatCondition hasFeeCondition = new ChatCondition() {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			return player.isEquipped("money", currentFee);
		}
	};

	String end = " Chcesz usłyszeć coś nowego?";
	String[] facts = { "Czy wiedziałeś, że wyposażenie z mithrilu po maksymalnym ulepszeniu dają dodatkowy atak? Nie? To już wiesz!" + end,
			"Kowal Tworzymir z Warszawy potrawi zwiększyć zasięg wszystkim broniom zasięgowym!" + end,
			"Niebieski pazurek jest najrzadszym pazurkiem w krainie, dlaczego? Nikt nie zna odpowiedzi!" + end,
			"Istnieje pewny pierścień w krainie, który potrafi ciebie zniknąć!" + end,
			"Zbroja z mithrilu... ona naprawdę istnieje!" + end,
			"Administratorzy was podglądują!" + end,
			"Najrzadszą rzeczą w krainie są... rękawice wampirze!" + end,
			"Książe w Warszawie zaczął mobilizować swoją armię, aby odbić zamek!" + end,
			"Kowal Tworzymir posiada wygórowaną cenę za ulepszenie mithrilu!" + end,
			"Wiedziałeś, że najczęstrzym miastem, w którym ludzie się rozwodzą to Ados? Ciekawe dlaczego..." + end,
			"Lubisz pieski? Stary Baca na kościelisku handluje owczarakami." + end,
			"Lovena z Deniran potrawi odczytać ze swojej magicznej kuli kto i ile pokonał potworów! Musisz koniecznie ją odwiedzić!" + end,
			"Kiedyś w całej krainie mówiono tylko i wyłącznie po... angielsku!" + end,
			"Młody chłopiec, który przebywa w smoczej krainie wraz ze smokami może dla ciebie wykonać ładny wisiorek ze smoczych pazurków." + end,
			"Smok, który zwie się Yerena potrafi cofnąć czas... i to pięć razy! Za każdym razem nasze doświadczenie wraz ze zdrowiem się poprawia!" + end,
			"Jak mnie zaatakujesz to ci oddam. Nie radziłbym!" + end };
}
