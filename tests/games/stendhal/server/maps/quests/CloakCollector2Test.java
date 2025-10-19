/***************************************************************************
 *                   (C) Copyright 2003-2017 - Stendhal                    *
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class CloakCollector2Test {
	private static final String NPC = "Josephine";
	private static final String QUEST_NAME = "cloaks_collector_2";
	private static final String OLD_QUEST = "cloaks_collector";
	private static final List<String> CLOAKS = Arrays.asList("płaszcz karmazynowy",
			"płaszcz cieni", "płaszcz xenocyjski", "płaszcz elficki",
			"płaszcz chaosu", "płaszcz mainiocyjski", "złoty płaszcz",
			"czarny płaszcz smoczy");

	private static List<String> missingCloaks(final Player player) {
		String done = player.getQuest(QUEST_NAME);
		final List<String> needed = new LinkedList<String>(CLOAKS);
		final List<String> colored = new LinkedList<String>();

		if (done == null) {
			done = "";
		}

		needed.removeAll(Arrays.asList(done.split(";")));
		for (final String cloak : needed) {
			colored.add("#" + cloak);
		}

		return colored;
	}

	private static String initiallyWantedMessage(final Player player) {
		final List<String> needed = missingCloaks(player);

		return "Brakuje "
			+ Grammar.quantityplnoun(needed.size(), "płaszcz")
			+ ". To jest " + Grammar.enumerateCollection(needed)
			+ ". Znajdziesz dla mnie?";
	}

	private static String stillWantedMessage(final Player player) {
		final List<String> needed = missingCloaks(player);

		return ("Chcę " + Grammar.quantityplnoun(needed.size(), "płaszcz")
			+ ". To jest " + Grammar.enumerateCollection(needed)
			+ ". Masz jakiś?");
	}

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().remove(NPC);
	}

	@Test
	public final void missingPreviousQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final CloakCollector2 cc = new CloakCollector2();
		cc.addToWorld();
		final SpeakerNPC npc = cc.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");

		/*
		 * Josephine should have nothing to say to us, unless we have completed
		 * cloaks_collector quest. Those people would be getting the answer from
		 *  that quest.
		 */
		en.stepTest(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertEquals("Josephines answer to non cloak1 people", null, getReply(npc));
	}

	@Test
	public final void rejectQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final CloakCollector2 cc = new CloakCollector2();
		cc.addToWorld();
		final SpeakerNPC npc = cc.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		final double karma = player.getKarma();

		// CloakCollector needs to be done to start this quest
		player.setQuest(OLD_QUEST, "done");
		en.stepTest(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertEquals("Josephines first greeting",  "Witaj ponownie przybyszu! Doszły mnie słuchy o nowych płaszczach, i żałuję że poprzednio Cię o nie nie poprosiłam, ale nie przepadałam za nimi. Obawiam się, że moja #kolekcja jest niekompletna...", getReply(npc));

		en.stepTest(player, "no");
		assertEquals("Answer to refusal", "Och... nie jesteś zbyt przyjazny. Usłyszałam tak?", getReply(npc));
		assertEquals("Karma penalty at refusal", karma - 5.0, player.getKarma(), 0.01);
	}

	@Test
	public final void doQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final CloakCollector2 cc = new CloakCollector2();
		cc.addToWorld();
		final SpeakerNPC npc = cc.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		final double karma = player.getKarma();

		player.setQuest(OLD_QUEST, "done");

		en.stepTest(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertEquals("Witaj ponownie przybyszu! Doszły mnie słuchy o nowych płaszczach, i żałuję że poprzednio Cię o nie nie poprosiłam, ale nie przepadałam za nimi. Obawiam się, że moja #kolekcja jest niekompletna...", getReply(npc));

		en.stepTest(player, "collection");
		assertEquals("Answer to 'collection'",
				initiallyWantedMessage(player), getReply(npc));

		for (final String item : CLOAKS) {
			en.stepTest(player, item);
			final String expected = "Nie widziałeś przedtem? Tak więc, to jest "
				+ item
				+ ". Przepraszam, ale to mi nie pomaga! Znajdziesz je wszystkie?";
			assertEquals(expected, getReply(npc));
		}

		// does not exist
		en.stepTest(player, "pink cloak");
		assertEquals("Przepraszam, ale nie kojarzę tego. Proszę podaj nazwę innego płaszcza.", getReply(npc));

		en.stepTest(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertEquals("Wspaniale! Jestem teraz szczęśliwa! Do widzenia!", getReply(npc));

		en.stepTest(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertEquals("Witaj z powrotem! Nie masz może przy sobie jakiś #płaszczy?", getReply(npc));

		en.stepTest(player, "płaszcze");
		assertEquals(stillWantedMessage(player), getReply(npc));

		en.stepTest(player, "no");
		assertEquals("Dobrze w takim razie wróć później.", getReply(npc));

		// This is weird, but it's how the quest works at the moment
		en.stepTest(player, "no");
		assertEquals("Dobrze. Jeżeli potrzebujesz pomocy to powiedz.", getReply(npc));

		/* Josephine does not know what to do with "bye" without CloakCollector,
		   so do it manually. Jump over the greeting as it was already tested above */
		en.setCurrentState(ConversationStates.QUESTION_2);

		en.stepTest(player, "yes");
		assertEquals("Ooo! Jakie #płaszcze mi przyniosłeś?", getReply(npc));

		// Give her all but the last - Thrice to test the possible answers
		for (final String itemName : CLOAKS.subList(1, CLOAKS.size())) {
			en.stepTest(player, itemName);
			assertEquals("Och, jestem rozczarowana. Nie masz "
					+ itemName + " ze sobą.", getReply(npc));

			final Item cloak = new Item(itemName, "", "", null);
			player.getSlot("bag").add(cloak);
			en.stepTest(player, itemName);
			assertEquals("Dziękuję! Co jeszcze mi przyniosłeś?", getReply(npc));

			en.stepTest(player, itemName);
			assertEquals("Jesteś strasznie zapominalski. Już mi przyniosłeś ten płaszcz.", getReply(npc));
		}

		// check the message again now that it has changed
		en.stepTest(player, "płaszcze");
		assertEquals(stillWantedMessage(player), getReply(npc));

		// Give the last one too. Try lying first again just to be sure
		final String lastCloak = CLOAKS.get(0);
		en.stepTest(player, lastCloak);
		assertEquals("Och, jestem rozczarowana. Nie masz "
				+ lastCloak + " ze sobą.", getReply(npc));
		final Item cloak = new Item(lastCloak, "", "", null);
		player.getSlot("bag").add(cloak);
		en.stepTest(player, lastCloak);
		assertEquals("Answer to last brought cloak", "O jej! Jesteś bardzo miły. Mogę się założyć, że masz wspaniałą karmę! Słuchaj chcę Cię nagrodzić czymś specjalnym, ale jeszcze nie jest to gotowe. Mógłbyś przyjść za jakiś czas i przypomnieć mi. Nie chcę zapomnieć!", getReply(npc));

		// check the rewards
		assertEquals(karma + 105.0, player.getKarma(), 0.01);
		assertEquals(100000L, player.getXP());
		assertEquals("done;rewarded", player.getQuest(QUEST_NAME));
		assertEquals(true, player.isEquipped("buty zabójcy"));

		final Item boots = player.getFirstEquipped("buty zabójcy");
		assertEquals("player", boots.getBoundTo());
	}

	@Test
	public final void compatibility() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final CloakCollector2 cc = new CloakCollector2();
		cc.addToWorld();
		final SpeakerNPC npc = cc.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");

		player.setQuest(OLD_QUEST, "done");
		player.setQuest(QUEST_NAME, "done");

		en.stepTest(player, ConversationPhrases.GREETING_MESSAGES.get(0));
		assertEquals("Message for the compatibility hack",  "Och! Nie nagrodziłam Ciebie za ponowną pomoc! Weź te buty. Sądzę, że są wspaniałe, ale nie pasują na mnie :(", getReply(npc));
		assertEquals("done;rewarded", player.getQuest(QUEST_NAME));
		assertTrue("The player got the boots", player.isEquipped("buty zabójcy"));

		final Item boots = player.getFirstEquipped("buty zabójcy");
		assertEquals("player", boots.getBoundTo());
	}
}
