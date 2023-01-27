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
package games.stendhal.server.maps.ados.bar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SentenceImplementation;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class BarMaidNPCTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	/**
	 * Tests for configureZone.
	 */
	@Test
	public void testConfigureZone() {

		SingletonRepository.getRPWorld();
		final BarMaidNPC barmaidConfigurator = new BarMaidNPC();

		final StendhalRPZone zone = new StendhalRPZone("testzone");
		barmaidConfigurator.configureZone(zone, null);
		assertFalse(zone.getNPCList().isEmpty());
		final NPC barMaid = zone.getNPCList().get(0);
		assertThat(barMaid.getName(), is("Siandra"));
		assertThat(barMaid.getDescription(), is("Oto piękna młoda barmanka, Siandra."));
	}

	/**
	 * Tests for hiandBye.
	 */
	@Test
	public void testHiandBye() {
		SingletonRepository.getRPWorld();
		final BarMaidNPC barmaidConfigurator = new BarMaidNPC();
		final StendhalRPZone zone = new StendhalRPZone("testzone");
		barmaidConfigurator.configureZone(zone, null);
		final SpeakerNPC barMaid = (SpeakerNPC) zone.getNPCList().get(0);
		assertThat(barMaid.getName(), is("Siandra"));
		final Engine engine = barMaid.getEngine();
		engine.setCurrentState(ConversationStates.IDLE);

		Sentence sentence = new SentenceImplementation(new Expression("hi", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat(getReply(barMaid), is("Witam!"));

		sentence = new SentenceImplementation(new Expression("bye", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.IDLE));
		assertThat(getReply(barMaid), is("Do widzenia, do widzenia!"));
	}

	/**
	 * Tests for jobOfferQuest.
	 */
	@Test
	public void testJobOfferQuest() {
		SingletonRepository.getRPWorld();
		final BarMaidNPC barmaidConfigurator = new BarMaidNPC();
		final StendhalRPZone zone = new StendhalRPZone("testzone");
		barmaidConfigurator.configureZone(zone, null);
		final SpeakerNPC barMaid = (SpeakerNPC) zone.getNPCList().get(0);
		assertThat(barMaid.getName(), is("Siandra"));

		// configure Siandra's shop
		SingletonRepository.getShopList().configureNPC("Siandra", "buyfood", false, true);

		final Engine engine = barMaid.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);

		Sentence sentence = new SentenceImplementation(new Expression("job", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat("job text", getReply(barMaid),
				is("Jestem kelnerką. Ze względu na ciężkie czasy nie mamy wystarczająco dużo jedzenia aby nakarmić naszych klientów. Czy możesz nam coś #zaoferować? Cokolwiek?"));

		sentence = new SentenceImplementation(new Expression("help", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat("help text", getReply(barMaid),
				is("Byłabym wdzięczna gdybyś mógł coś #zaoferować aby uzupełnić nasze zapasy: mięso, szynka lub ser."));

		sentence = new SentenceImplementation(new Expression("quest", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat("quest text", getReply(barMaid), is("#Zaoferowano nam już dość jedzenia, dziękuję za pomoc."));
	}

	/**
	 * Tests for buyerBehaviour.
	 */
	@Test
	public void testBuyerBehaviour() {
		SingletonRepository.getRPWorld();

		final BarMaidNPC barmaidConfigurator = new BarMaidNPC();
		final StendhalRPZone zone = new StendhalRPZone("testzone");
		barmaidConfigurator.configureZone(zone, null);
		final SpeakerNPC barMaid = (SpeakerNPC) zone.getNPCList().get(0);
		assertThat(barMaid.getName(), is("Siandra"));
		final Engine engine = barMaid.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);

		Sentence sentence = new SentenceImplementation(new Expression("offer", ExpressionType.VERB));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat("offer text", getReply(barMaid), equalTo("Skupuję ser, mięso, szpinak, szynka, mąka, oraz borowik."));

		final Expression sell = new Expression("sell", ExpressionType.VERB);

		sentence = new SentenceImplementation(sell);
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.ATTENDING));
		assertThat("offer text", getReply(barMaid), is("Powiedz mi co chcesz zrobić."));

		sentence = new SentenceImplementation(sell, new Expression("ser", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("ser jest warty jest 5. Czy chcesz sprzedać to?"));
		engine.setCurrentState(ConversationStates.ATTENDING);

		sentence = new SentenceImplementation(sell, new Expression("mięso", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("mięso jest warty jest 10. Czy chcesz sprzedać to?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		sentence = new SentenceImplementation(sell, new Expression("szpinak", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("szpinak jest warty jest 15. Czy chcesz sprzedać to?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		sentence = new SentenceImplementation(sell, new Expression("szynka", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("szynka jest warty jest 20. Czy chcesz sprzedać to?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		sentence = new SentenceImplementation(sell, new Expression("mąka", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("mąka jest warty jest 25. Czy chcesz sprzedać to?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		sentence = new SentenceImplementation(sell, new Expression("borowik", ExpressionType.OBJECT));
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("borowik jest warty jest 30. Czy chcesz sprzedać to?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		final Expression pieczarka = new Expression("borowik", ExpressionType.OBJECT);
		pieczarka.setAmount(2);
		sentence = new SentenceImplementation(sell, pieczarka);
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("2 borowicy są warty jest 60. Czy chcesz sprzedać je?"));

		engine.setCurrentState(ConversationStates.ATTENDING);
		final Expression flour = new Expression("mąka", ExpressionType.OBJECT);
		flour.setAmount(2);
		sentence = new SentenceImplementation(sell, flour);
		engine.step(PlayerTestHelper.createPlayer("bob"), sentence);
		assertThat(engine.getCurrentState(), is(ConversationStates.SELL_PRICE_OFFERED));
		assertThat("offer text", getReply(barMaid), is("2 mąki są warty jest 50. Czy chcesz sprzedać je?"));
	}

}
