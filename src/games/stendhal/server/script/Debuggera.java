/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.core.scripting.ScriptingNPC;
import games.stendhal.server.core.scripting.ScriptingSandbox;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

/**
 * code for abstract/int_admin_playground which creates a NPC to help testers.
 *
 * @author hendrik
 */
public class Debuggera extends ScriptImpl {

	// boolean debuggeraEnabled;

//	private static final class DebuggeraEnablerAction implements ChatAction {
//		boolean enabled;
//
//		public DebuggeraEnablerAction(final boolean enable) {
//			this.enabled = enable;
//		}
//
//		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
//			// TODO debuggeraEnabled = enabled;
//			if (enabled) {
//				raiser.say("Dziękuję.");
//			} else {
//				raiser.say("Dobrze nie będę rozmawiała z obcymi");
//			}
//		}
//	}

	private static final class QuestsAction implements ChatAction {
		ScriptingSandbox sandbox;

		public QuestsAction(final ScriptingSandbox sandbox) {
			this.sandbox = sandbox;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			// list quest
			final StringBuilder sb = new StringBuilder("Stan twoich zadań:");
			final List<String> quests = player.getQuests();
			for (final String quest : quests) {
				sb.append("\r\n" + quest + " = " + player.getQuest(quest));
			}

			// change quest
			String quest = sentence.getOriginalText();
			if (quest != null) {
				int pos = quest.indexOf("=");
				if (pos > -1) {
					final String value = quest.substring(pos + 1);
					quest = quest.substring(0, pos);
					sb.append("\r\n\r\nSet \"" + quest + "\" to \"" + value + "\"");
					sandbox.addGameEvent(player.getName(), "alter_quest",
							Arrays.asList(player.getName(), quest, value));
					player.setQuest(quest.trim(), value.trim());
				}
			}
			raiser.say(sb.toString());
		}
	}

	private static final class TeleportNPCAction implements ChatAction {
		ScriptingSandbox sandbox;

		public TeleportNPCAction(final ScriptingSandbox sandbox) {
			this.sandbox = sandbox;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			SingletonRepository.getTurnNotifier().notifyInTurns(0,
					new TeleportScriptAction(player, (SpeakerNPC) raiser.getEntity(), sandbox));
		}
	}

	static class TeleportScriptAction implements TurnListener {
		private final ScriptingSandbox sandbox;

		private final Player player;

		private final SpeakerNPC engine;

		// private Sentence sentence;
		//
		// private int destIdx = 0;

		private int counter;

		private int inversedSpeed = 3;

		private int textCounter;

		private boolean beamed;

		// syntax-error: private final String[] MAGIC_PHRASE = {"Across the
		// land,", "Across the sea.", "Friends forever,", "We will always be."};

		public TeleportScriptAction(final Player player, final SpeakerNPC engine, final ScriptingSandbox sandbox) {
			this.player = player;
			this.engine = engine;
			// this.sentence = sentence;
			this.sandbox = sandbox;
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			boolean keepRunning = true;
			counter++;
			if (beamed) {
				// slow down
				if (counter % inversedSpeed == 0) {
					Direction direction = player.getDirection();
					direction = Direction.build((direction.get()) % 4 + 1);
					player.setDirection(direction);
					sandbox.modify(player);
					if (direction == Direction.DOWN) {
						inversedSpeed++;
						if (inversedSpeed == 3) {
							keepRunning = false;
						}
					}
				}
			} else {
				// speed up
				if (counter % inversedSpeed == 0) {
					Direction direction = player.getDirection();
					direction = Direction.build((direction.get()) % 4 + 1);
					player.setDirection(direction);
					sandbox.modify(player);
					if (direction == Direction.DOWN) {
						switch (textCounter) {
						case 0:
							engine.say("Przez ląd");
							inversedSpeed--;
							break;
						case 1:
							engine.say("Przez morze.");
							inversedSpeed--;
							break;
						case 2:
							engine.say("Przyjaciele na zawsze");
							break;
						case 3:
							engine.say("Zawsze będziemy.");
							break;
						default:
							// Teleport to a near by spot

							final StendhalRPZone zone = sandbox.getZone(player);
							final int x = player.getX();
							final int y = player.getY();
							final int[][] tele_offsets = { { 7, 7 }, { 7, -7 },
									{ -7, 7 }, { -7, -7 } };
							final Random random = new Random();

							for (int i = 0; i < 3; i++) {
								final int r = random.nextInt(tele_offsets.length);
								if (player.teleport(zone, x
										+ tele_offsets[r][0], y
										+ tele_offsets[r][1], null, null)) {
									break;
								}
							}

							inversedSpeed = 1;
							beamed = true;
							break;
						}
						textCounter++;
					}
				}
			}
			if (keepRunning) {
				SingletonRepository.getTurnNotifier().notifyInTurns(0, this);
			}
		}
	}

	public class SightseeingAction implements ChatAction, TurnListener {

		private Player player;

		private final List<String> zones;

		private int counter;

		public SightseeingAction(final StendhalRPWorld world) {
			// this.sandbox = sandbox;

			zones = new ArrayList<String>();

			for (final IRPZone irpZone : world) {
				final StendhalRPZone zone = (StendhalRPZone) irpZone;
				zones.add(zone.getName());
			}
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			this.player = player;
			counter = 0;
			player.sendPrivateText("Zaczynajmy");
			SingletonRepository.getTurnNotifier().notifyInTurns(10, this);
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			try {
				final String zoneName = zones.get(counter);
				final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);

				final int[][] tele_xy = { { 5, 5 }, { 50, 50 }, { 20, 20 },
						{ 100, 100 }, { 100, 5 } };
				boolean foundSpot = false;

				for (int i = 0; i < tele_xy.length; i++) {
					if (player.teleport(zone, tele_xy[i][0], tele_xy[i][1],
							null, null)) {
						player.sendPrivateText("Witaj w " + zoneName);
						foundSpot = true;
						break;
					}
				}

				if (!foundSpot) {
					player.sendPrivateText("Nie znalazłem wolnego miejsca w "
							+ zoneName);
				}
			} catch (final Exception e) {
				Logger.getLogger(SightseeingAction.class).error(e, e);
			}

			counter++;

			if (counter < zones.size() - 1) {
				SingletonRepository.getTurnNotifier().notifyInTurns(10, this);
			}
		}
	}

	@Override
	public void load(final Player admin, final List<String> args, final ScriptingSandbox sandbox) {
		super.load(admin, args, sandbox);

		// Create NPC
		final ScriptingNPC npc = new ScriptingNPC("Debuggera");
		npc.setEntityClass("girlnpc");

		// Place NPC in int_admin_playground on server start
		final String myZone = "int_admin_playground";
		sandbox.setZone(myZone);
		int x = 4;
		int y = 11;

		// If this script is executed by an admin, Debuggera will be placed next
		// to him/her.
		if (admin != null) {
			sandbox.setZone(sandbox.getZone(admin));
			x = admin.getX() + 1;
			y = admin.getY();
		}

		// Set zone and position
		npc.setPosition(x, y);
		npc.setDirection(Direction.DOWN);
		sandbox.add(npc);

		//
		npc.add(ConversationStates.IDLE, Arrays.asList("hi", "hello",
				"greetings", "hola", "cześć", "witaj", "witam",
				"hej", "dzień dobry", "dobry wieczór"), null, ConversationStates.IDLE,
				"Moja mama powiedziała, że nie powinnam rozmawiać z obcymi.", null);
		npc.behave("bye", "Dowidzenia.");

		// Greating and admins may enable or disable her
		npc.add(ConversationStates.IDLE, Arrays.asList("hi", "hello",
				"greetings", "hola", "cześć", "witaj", "witam",
				"hej", "dzień dobry", "dobry wieczór"), new AdminCondition(),
				ConversationStates.ATTENDING,
				"Witaj administratorze. Czy sądzisz, że jestem #szalona?", null);

//		npc.add(ConversationStates.IDLE, [ "hi","hello","greetings","hola" ],
//				new AdminCondition(), ConversationStates.QUESTION_1,
//				"Czy mogę rozmawiać z obcymi?", null);
//		npc.add(ConversationStates.QUESTION_1, SpeakerNPC.YES_MESSAGES, new AdminCondition(),
//				ConversationStates.ATTENDING, null, new DebuggeraEnablerAction(true));
//		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, new AdminCondition(),
//				ConversationStates.ATTENDING, null, new DebuggeraEnablerAction(false));

		npc.behave(Arrays.asList("szalona", "insane", "crazy", "mad"),
				"Dlaczego jesteś taki skromny? NIE JESTEM SZALONA. Moja mama powiedziała, że jestem #szczególnym dzieckiem.");
		npc.behave(
				Arrays.asList("szczególnym", "special", "special child"),
				"Mogę zobaczyć inne światy w moich snach. Są czymś więcej niż snami. Tam ludzie siedzą przy maszynach zwanymi komputerami. To są dziwni ludzie. Nie mogą używać telepatii bez czegoś zwanego inter-network, ale Ci ludzie i maszyny są jakoś połączeni z całym światem. Jeżeli się skoncentruję to mogę #zmienić parę rzeczy w naszym świecie.");
		// npc.behave("verschmelzung", "\r\nYou have one hand,\r\nI have the
		// other.\r\nPut them together,\r\nWe have each other.");
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("susi"),
				null,
				ConversationStates.ATTENDING,
				"Tak to moja bliźniacza siostra. Ludzie uważają ją za normalną ponieważ ukrywa swoje specjalne zdolności.",
				null);

		// change
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zmienić", "change", "change"), new QuestInStateCondition(
						"debuggera", "friends"), ConversationStates.ATTENDING,
				"Mogę Cię teleportować.", null);
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zmienić", "change", "change"),
				new QuestNotInStateCondition("debuggera", "friends"),
				ConversationStates.ATTENDING,
				"Czy chcesz zostać moim #przyjacielem?", null);

		// friends
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("przyjacielem", "friend", "friends"), new QuestInStateCondition(
						"debuggera", "friends"), ConversationStates.ATTENDING,
				"Jesteśmy przyjaciółmi.", null);
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("przyjacielem", "friend", "friends"),
				new QuestNotInStateCondition("debuggera", "friends"),
				ConversationStates.INFORMATION_1,
				"Proszę powtórz za mną:\r\n                        \"A circle is round,\"",
				null);
		npc.add(ConversationStates.INFORMATION_1, Arrays.asList(
				"Kółko jest okrągłe", "Kółko jest okrągłe"), null,
				ConversationStates.INFORMATION_2, "\"i nie ma końca.\"", null);
		npc.add(ConversationStates.INFORMATION_2, Arrays.asList(
				"i nie ma końca.", "i nie ma końca."), null,
				ConversationStates.INFORMATION_3, "\"To tak długo,\"", null);
		npc.add(ConversationStates.INFORMATION_3, Arrays.asList(
				"To tak długo,", "To tak długo", "To tak długo,",
				"To tak długo"), null, ConversationStates.INFORMATION_4,
				"\"będę twoim przyjacielem.\"", null);
		npc.add(ConversationStates.INFORMATION_4, Arrays.asList(
				"będę twoim przyjacielem.", "będę twoim przyjacielem"), null,
				ConversationStates.ATTENDING, "Wspaniale. Jesteśmy teraz przyjaciółmi.",
				new SetQuestAction("debuggera", "friends"));

		// quests
		npc.add(ConversationStates.ATTENDING, "quest", new AdminCondition(),
				ConversationStates.ATTENDING, null, new QuestsAction(sandbox));

		// teleport
		npc.add(ConversationStates.ATTENDING, Arrays.asList("teleport",
				"teleportme"), new AdminCondition(), ConversationStates.IDLE,
				null, new TeleportNPCAction(sandbox));

		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		npc.add(ConversationStates.ATTENDING, Arrays.asList("sightseeing",
				"memory", "memoryhole"), new AdminCondition(),
				ConversationStates.IDLE, null, new SightseeingAction(world));
	}
	/*
	 * Make new friends, but keep the old. One is silver, And the other gold,
	 *
	 * You help me, And I'll help you. And together, We will see it through.
	 *
	 * The sky is blue, The Earth Earth is green. I can help, To keep it clean.
	 */

}
