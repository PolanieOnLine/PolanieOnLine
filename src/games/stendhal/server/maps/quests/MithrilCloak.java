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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.TwilightMossScroll;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.mithrilcloak.MithrilCloakQuestChain;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Mithril Cloak
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Ida, a seamstress in Ados.</li>
 * <li>Imperial scientists, in kalavan basement</li>
 * <li>Mithrilbourgh wizards, in kirdneh and magic city</li>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Terry, the dragon hatcher in semos caves.</li>
 * <li>Ritati Dragontracker, odds and ends buyer in ados abandoned keep</li>
 * <li>Pdiddi, the dodgy dealer from Semos</li>
 * <li>Josephine, young woman from Fado</li>
 * <li>Pedinghaus, the mithril casting wizard in Ados</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Ida needs sewing machine fixed, with one of three items from a list</li>
 * <li>Once machine fixed and if you have done mithril shield quest, Ida offers you cloak</li>
 * <li>Kampusch tells you to how to make the fabric</li>
 * <li>Imperial scientists take silk glands and make silk thread</li>
 * <li>Kampusch fuses mithril nuggets into the silk thread</li>
 * <li>Whiggins weaves mithril thread into mithril fabric</li>
 * <li>Ida takes fabric then asks for scissors</li>
 * <li>Hogart makes the scissors which need eggshells</li>
 * <li>Terry swaps eggshells for poisons</li>
 * <li>Ida takes the scissors then asks for needles</li>
 * <li>Needles come from Ritati Dragontracker</li>
 * <li>Ida breaks a random number of needles, meaning you need to get more each time</li>
 * <li>Ida pricks her finger on the last needle and goes to twilight zone</li>
 * <li>Pdiddi sells the moss to get to twilight zone</li> 
 * <li>A creature in the twilight zone drops the elixir to heal lda</li>
 * <li>After being ill Ida asks you to take a blue striped cloak to Josephine</li>
 * <li>After taking cloak to Josephine and telling Ida she asks for mithril clasp</li>
 * <li>Pedinghaus makes mithril clasp</li>
 * <li>The clasp completes the cloak</li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li>Mithril Cloak</li>
 * <li> XP</li>
 * <li> Karma</li>
 * </ul>
 * <p>
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 * 
 * @author kymara
 */
public class MithrilCloak extends AbstractQuest {
	private static final String QUEST_SLOT = "mithril_cloak";
	
	private static Logger logger = Logger.getLogger(MithrilCloak.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Płaszcz z Mithrilu",
				"Czy jesteś zainteresowany błyszczącym z wysoką obroną płaszczem? Możesz być osobą, która przyniesie potrzebne na to przedmioty Idzie w Ados.",
				false);
		
		// login notifier to teleport away players logging into the twilight zone.
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
			   TwilightMossScroll scroll = (TwilightMossScroll) SingletonRepository.getEntityManager().getItem("mroczny mech");
				scroll.teleportBack(player);
			}

		});
		
		MithrilCloakQuestChain mithrilcloak = new MithrilCloakQuestChain();
		mithrilcloak.addToWorld();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
    	res.add("Poznałem Idę w szawalni w Ados");
        if (questState.equals("rejected")) {
			res.add("Nie jestem zainteresowany pomaganiem dla Idy.");
			return res;
		}
        res.add("Idy maszyna do szycia jest uszkodzona, a ona zwróciła się do mnie abym znalazł brakujące części.");
		if (questState.startsWith("machine")) {
			res.add("Muszę zanieść Idzie " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,1)) + ".");
			return res;
		}
		res.add("Przyniosłem części potrzebne do naprawienia maszyny Idy.");
		if (questState.equals("need_mithril_shield")) {
			res.add("Muszę zrobić na początek tarczę z mithrilu wtedy mogę iść dalej w moich poszukiwaniach płaszczu z mithrilu.");
			return res;
		}
		if (questState.equals("fixed_machine")) {
			return res;
		}
		res.add("Mój płaszcz potrzebuje tkaniny z mithrilu i Kampusch mi pomoże. On wie, jakie przedmioty trzeba.");
		if (questState.equals("need_fabric")) {
			return res;
		}
		res.add("Vincento Price jest znany z przędzenia nici jedwabnych. Udałem się do niego.");
		if (questState.startsWith("makingthread;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Zaniosłem Kampusch jedwabne nici, króre odebrałem od Vincento's studenta, Boris Karlova.");
		if (questState.equals("got_thread")) {
			return res;
		}
		res.add("Kampusch jest specjalistą w łączeniu mithrilu z jedwabnymi nićmi.");
		if (questState.startsWith("fusingthread;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Whiggins zrobi mi tkanine z mithrilu. Muszę go odnaleść.");
		if (questState.equals("got_mithril_thread")) {
			return res;
		}
		res.add("Nim Whiggins mi pomoże muszę zanieść list do Pedinghaus. Whiggins wydaje się naprawdę zmartwiony.");
		if (questState.equals("taking_letter")) {
			return res;
		}
		res.add("Wziąłem list do Pedinghaus i on go przeczytał. Mam powiedzieć Whiggins, że wszystko jest w porządku, więc mogę dostać moją tkaninę.");
		if (questState.equals("took_letter")) {
			return res;
		}
		res.add("Whiggins zabrał się za tkanie tkaniny z mithrilu na mój płaszcz!");
		if (questState.startsWith("weavingfabric;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Odebrałem tkaninę z mithrilu. Teraz muszę zanieść ją Idzie.");
		if (questState.equals("got_fabric")) {
			return res;
		}
		res.add("Ida nie potnie tkaniny zwykłymi nożyczkami. Udam się do Hogarta po specjalne nożyczki.");
		if (questState.equals("need_scissors")) {
			return res;
		}
		res.add("Hogart nie da nożyczek nim nie przyniosę żelaza, sztabki mithrilu, i magicznych skorupek.");
		if (questState.startsWith("need_eggshells;")) {
			// the quest slot knows how many eggshells were needed.
			return res;
		}
		res.add("Hogart robi magiczne nożyczki z przedmiotów, które mu przyniosłem.");
		if (questState.startsWith("makingscissors;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Zaniosłem magiczne nożyczki do Idy.");
		if (questState.equals("got_scissors")) {
			return res;
		}
		res.add("Ida potrzebuje magicznej igły do uszycia mojego płaszcza.");
		if (questState.startsWith("need_needle") || questState.startsWith("told_joke;")) {
			//  quest slot knows how many needles are still needed to take and which joke was told last
			return res;
		}
		res.add("Ida szyje mój płaszcz!");
		if (questState.startsWith("sewing;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			// number of needles still remaining is in slot 2
			// don't bother with adding info about the looping (needle breaking and sewing again)
			return res;
		}
		res.add("Ida miała straszny wypadek ukuła się w palec igłą. Ma halucynacje i muszę spróbować odwiedzić ją w strefie mroku.");
		if (questState.equals("twilight_zone")) {
			return res;
		}
		res.add("Dałem dla Idy eliksir mroku, aby przywrócić jej zdrowie. Teraz dała mi inne zadanie. Muszę iść i znaleźć prążkowany płaszcz lazurowy, który zaniosę do Josephine. W tym czasie Ida może szyć mój płaszcz.");
		if (questState.equals("taking_striped_cloak")) {
			return res;
		}
		res.add("Jospehine ucieszyła się z płaszcza. Kazała pozdrowić Idę");
		if (questState.equals("gave_striped_cloak")) {
			return res;
		}
		res.add("Mój płaszcz jest już prawie gotowy, wszystko co potrzebuję to zapięcie z mithrilu od Pedinghaus.");
		if (questState.equals("need_clasp")) {
			return res;
		}
		res.add("Pedinghaus robi mi zapięcie z mithrilu. Nie mogę się doczekać!");
		if (questState.startsWith("forgingclasp;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Mam zapinkę z mithrilu do mojego płaszcza. Wszystko co muszę zrobić, to przyjąć płaszcz od Idy.");
		if (questState.equals("got_clasp")) {
			return res;
		}
		res.add("Wreszcie mogę nosić cudowny płaszcz z mithrilu!");
		if (questState.equals("done")) {
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
		return "MithrilCloak";
	}
	
	// it's a long quest so they can always start it before they can necessarily finish all
	@Override
	public int getMinLevel() {
		return 100;
	}
	
	// Not sure about this one. it would make an achievement for all quests in ados city, quite hard
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ida";
	}
}
