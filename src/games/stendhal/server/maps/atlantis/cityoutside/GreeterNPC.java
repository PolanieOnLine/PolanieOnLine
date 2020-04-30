/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.atlantis.cityoutside;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;


public class GreeterNPC implements ZoneConfigurator {

	private static final StendhalRPWorld world = SingletonRepository.getRPWorld();

	private SpeakerNPC greeter;

	private static List<Pair<String, String>> atlantisZones = null;
	private static List<String> atlantisPeople = null;
	private static List<String> atlantisCreatures = null;

	private Integer fee = null;

	private static enum InquiryType {
		ZONE,
		PERSON,
		CREATURE;
	};

	private static Map<String, String> zoneReplies = new HashMap<String, String>() {{
		put("Atlantyda",
				"\"Atlantyda\" o nazwa naszego świata. Składa się z miasta, " +
				"w którym teraz jesteśmy, oraz okolicznych równin i gór.");
		put("Miasto Atlantyda",
				"To jest nasze piękne miasto. Od stuleci jesteśmy " +
				"chronieni i żyjemy w pokoju.");
	}};

	private static Map<String, String> peopleReplies = new HashMap<String, String>() {{
		put("Ryla", "Kto ja? Nie mam wiele do powiedzenia o sobie.");
		put("Zelan", "Często szuka rogów jednorożca. Możesz zapytać go, czy potrzebuje pomocy.");
		put("Mirielle", "Prowadzi sklep z eliksirami na północny wschód stąd.");
	}};

	private static Map<String, String> creatureReplies = new HashMap<String, String>() {{
		put(Arrays.asList("mały pegaz", "małych pegazów"),
				"Biedne stworzonka. Cudzoziemcy przybywają tutaj, aby je kłusować za specjalne upuszczane pierścienie. " +
				"Ich populacja zmalała w ostatnich latach.");
		put("pegaz kościsty",
				"Bardzo chronią swoje potomstwo. Nie zdziw się, jeśli znajdziesz parę pilnującą grupy #'małych pegazów'.");
		put("mamut włochaty",
				"Te potwory istnieją już od początku czasów. Bądź ostrożny, jeśli spotkasz ich stado. Są bardzo silne.");
	}};


	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		initNPC(zone);
		initHealerBehaviour();
	}

	private void initNPC(final StendhalRPZone zone) {
		greeter = new SpeakerNPC("Ryla");
		greeter.setEntityClass("atlantisfemale08npc");
		greeter.setDescription("Oto Ryla. Młoda na twarzy oraz wielowiekowe doświadczenie w oczach.");

		greeter.addGreeting();
		greeter.addGoodbye();
		greeter.addHelp(
				"To jest Atlantyda. Mieszkamy tu w pokoju od wieków."
				+ " Jeśli jesteś ranny, będę mogła cię #uleczyć, lub jeśli chcesz dowiedzieć"
				+ " się więcej o tym miejscu, jest takich kilka #tematów, w których jestem szkolona.");
		greeter.addJob("Moim zadaniem jest pomaganie nowym odwiedzającym w #poznawaniu naszej ziemi. Mogę także #uleczyć rany.");
		greeter.addReply(
				Arrays.asList("topic", "learn", "temat", "tematów", "poznawaniu", "poznawać"),
				"Co chciałbyś wiedzieć? Mogę opowiedzieć o tutejszych #obszarach, #stworzeniach lub #mieszkańcach Atlantydy.");
		greeter.addQuest("Nie mam dla ciebie nic, ale być może niektórzy inni ludzie na Atlantydzie będą potrzebować twojej pomocy.");

		greeter.add(ConversationStates.ATTENDING,
				Arrays.asList("zone", "area", "obszar", "obszarach", "obszary", "strefy"),
				null,
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						// make sure zone list is ready
						buildAtlantisZoneList();

						final StringBuilder reply = new StringBuilder("Obszary Atlantydy są");
						final int zoneCount = atlantisZones.size();
						int idx = 0;
						for (final Pair<String, String> zonePair: atlantisZones) {
							final String zoneName = zonePair.first();

							reply.append(" #'" + zoneName + "'");

							if (idx < zoneCount - 2) {
								reply.append(",");
							} else if (idx == zoneCount - 2) {
								if (zoneCount > 2) {
									reply.append(",");
								}
								reply.append(" oraz");
							}
							idx++;
						}
						reply.append(". O którym obszarze chciałbyś dowiedzieć się więcej?");

						greeter.say(reply.toString());
					}
				});

		greeter.add(ConversationStates.ATTENDING,
				Arrays.asList("inhabitant", "people", "npc", "mieszkańcach", "mieszkańcy", "ludzi", "ludziach", "obywatele"),
				null,
				ConversationStates.QUESTION_2,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						// make sure NPC list is ready
						buildAtlantisPeopleList();

						final StringBuilder reply = new StringBuilder("Mieszkańcy Atlantydy są pokojowi. " +
								"W ten sposób nasza cywilizacja przetrwała tak długo. Nasi obywatele to");
						final int npcCount = atlantisPeople.size() - 1; // don't include Ryla
						int idx = 0;
						final String selfName = greeter.getName();
						for (final String npcName: atlantisPeople) {
							if (!npcName.equals(selfName)) {
								reply.append(" #'" + npcName + "'");

								if (idx < npcCount - 1) {
									reply.append(",");
								}
								idx++;
							}
						}

						if (npcCount > 1) {
							reply.append(",");
						}

						reply.append(" oraz ja, #'" + selfName + "'. O której osobie chciałbyś dowiedzieć się więcej?");

						greeter.say(reply.toString());
					}
				});

		greeter.add(ConversationStates.ATTENDING,
				Arrays.asList("creature", "enemy", "beast", "potworach", "potwory", "stworzeniach", "stworzenia", "bestiach", "bestie"),
				null,
				ConversationStates.QUESTION_3,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						// make sure creature list is ready
						buildAtlantisCreatureList();

						final StringBuilder reply = new StringBuilder("Wpółistniejące stworzenia w Atlantydzie są to");
						final int creatureCount = atlantisCreatures.size();
						int idx = 0;
						for (final String creatureName: atlantisCreatures) {
							reply.append(" #'" + creatureName + "'");

							if (idx < creatureCount - 2) {
								reply.append(",");
							} else if (idx == creatureCount - 2) {
								if (creatureCount > 2) {
									reply.append(",");
								}
								reply.append(" oraz");
							}
							idx++;
						}
						reply.append(". O jakim stworzeniu chciałbyś dowiedzieć się więcej?");

						greeter.say(reply.toString());
					}
				});

		greeter.add(ConversationStates.QUESTION_1,
				"",
				null,
				ConversationStates.QUESTION_1,
				null,
				createReply(InquiryType.ZONE));

		greeter.add(ConversationStates.QUESTION_2,
				"",
				null,
				ConversationStates.QUESTION_2,
				null,
				createReply(InquiryType.PERSON));

		greeter.add(ConversationStates.QUESTION_3,
				"",
				null,
				ConversationStates.QUESTION_3,
				null,
				createReply(InquiryType.CREATURE));


		greeter.setPosition(65, 75);
		zone.add(greeter);
	}

	private void initHealerBehaviour() {
		final ChatAction calculateCostAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (player.getHP() == player.getBaseHP()) {
					greeter.say("Przepraszam, ale wygląda na to, że nie potrzebujesz uzdrowienia. Jak jeszcze mogę ci #pomóc?");
					return;
				}

				final StringBuilder healMessage = new StringBuilder();
				if (player.isBadBoy()) {
					healMessage.append("Hmmmm, widzę, że się źle zachowywałeś. Tutaj, na Atlantydzie, nie podchodzimy uprzejmie do zbirów, więc zapłacisz dodatkowo za uzdrowienie. To będzie kosztować ");
					calculateFee(player, true);
				} else {
					healMessage.append("Uzdrowienie kogoś z twojego wzrostu będzie kosztowało ");
					calculateFee(player, false);
				}

				healMessage.append(fee + ". Jesteś gotów zapłacić?");

				greeter.say(healMessage.toString());
				greeter.setCurrentState(ConversationStates.HEAL_OFFERED);
			}
		};

		final ChatAction healAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				player.drop("money", fee);
				greeter.addEvent(new SoundEvent(SoundID.HEAL, SoundLayer.CREATURE_NOISE));
				player.setHP(player.getBaseHP());

				// reset fee after healing
				fee = null;

				greeter.say("Zostaliście uzdrowieni. Jak jeszcze mogę ci #pomóc?");
			}
		};

		new HealerAdder().addHealer(greeter, calculateCostAction, healAction);
	}

	private void calculateFee(final Player player, final boolean badBoy) {
		final int diff = player.getBaseHP() - player.getHP();
		final int level = player.getLevel();

		fee = level * 4 + diff * 2;

		if (badBoy) {
			fee = fee * 3;
		}
	}

	private ChatAction createReply(final InquiryType type) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final String inquiry = sentence.getTrimmedText().toLowerCase();

				if (ConversationPhrases.GOODBYE_MESSAGES.contains(inquiry)) {
					greeter.endConversation();
					return;
				}

				if (Arrays.asList("no", "none", "nothing", "no one", "nie", "żaden", "nic", "nikt").contains(inquiry)) {
					greeter.setCurrentState(ConversationStates.ATTENDING);
					greeter.say("Okej, w czym jeszcze mogę ci #pomóc?");
					return;
				}

				Map<String, String> replyList;
				String subjectType;
				String noun = "coś";

				if (type.equals(InquiryType.ZONE)) {
					replyList = zoneReplies;
					subjectType = "obszar";
				} else if (type.equals(InquiryType.PERSON)) {
					replyList = peopleReplies;
					subjectType = "osoba";
					noun = "ktoś";
				} else {
					replyList = creatureReplies;
					subjectType = "stworzenie";
				}

				String reply = null;
				for (final String key: replyList.keySet()) {
					if (inquiry.equals(key.toLowerCase())) {
						reply = replyList.get(key);
						break;
					}
				}

				if (reply == null) {
					greeter.say("Przepraszam, nie mam na ten temat " + subjectType + " żadnych informacji. Zapytaj mnie o " + noun + " jeszcze.");
					return;
				}

				if (type.equals(InquiryType.PERSON)) {
					reply += " Kto";
				} else {
					reply += " Co";
				}

				reply += " jeszcze chciałbyś wiedzieć?";
				greeter.say(reply);
			}
		};
	}

	private String formatZoneName(final StendhalRPZone zone) {
		String zoneName = zone.getHumanReadableName().replace("Deniran ", "").split(",")[0].replace("Mtn", "Mountain");

		if (zoneName.equals("Atlantis")) {
			zoneName = "Miasto Atlantydy";
		}

		return zoneName;
	}

	private void buildAtlantisZoneList() {
		if (atlantisZones == null) {
			atlantisZones = new ArrayList<>();

			final List<Pair<String, String>> mountains = new ArrayList<>();

			for (final StendhalRPZone z: world.getAllZonesFromRegion(Region.DENIRAN.toLowerCase(), true, false, true)) {
				final String zoneName = z.getName();

				if (zoneName.contains("atlantis")) {
					// format zone name for human readability
					final String formattedName = formatZoneName(z);

					if (formattedName.contains("Mountain")) {
						// store mountains for later adding
						mountains.add(new Pair<String, String>(formattedName, zoneName));
					} else {
						atlantisZones.add(new Pair<String, String>(formattedName, zoneName));
					}
				}
			}

			final Comparator<Pair<String, String>> sorter = new Comparator<Pair<String, String>>() {
				@Override
				public int compare(final Pair<String, String> p1, final Pair<String, String> p2) {
					return p1.first().toLowerCase().compareTo(p2.first().toLowerCase());
				}
			};

			Collections.sort(atlantisZones, sorter);
			Collections.sort(mountains, sorter);
			atlantisZones.addAll(mountains);
		}
	}

	private void buildAtlantisPeopleList() {
		if (atlantisPeople == null) {

			atlantisPeople = new ArrayList<>();

			final NPCList npcList = SingletonRepository.getNPCList();
			for (final String npcName: npcList.getNPCs()) {
				final SpeakerNPC npc = npcList.get(npcName);

				// exclude teleporting NPCs
				if (npc.isTeleporter()) {
					continue;
				}

				final String zoneName = npc.getZone().getName();
				// only include NPC inside city
				if (zoneName.equals("-7_deniran_atlantis") || (zoneName.startsWith("int_") && zoneName.contains("atlantis"))) {
					// use 'npc.getName()' instead of 'npcName' to get proper titleization
					atlantisPeople.add(npc.getName());
				}
			}

			Collections.sort(atlantisPeople, String.CASE_INSENSITIVE_ORDER);
		}
	}

	private void buildAtlantisCreatureList() {
		if (atlantisCreatures == null) {
			buildAtlantisZoneList();

			atlantisCreatures = new ArrayList<>();

			for (final Pair<String, String> zonePair: atlantisZones) {
				final String zoneID = zonePair.second();
				final StendhalRPZone rpZone = world.getZone(zoneID);
				if (rpZone != null) {
					for (final CreatureRespawnPoint spawner: rpZone.getRespawnPointList()) {
						final String creatureName = spawner.getPrototypeCreature().getName();
						if (!atlantisCreatures.contains(creatureName)) {
							atlantisCreatures.add(creatureName);
						}
					}
				}
			}

			Collections.sort(atlantisCreatures, String.CASE_INSENSITIVE_ORDER);
		}
	}
}
