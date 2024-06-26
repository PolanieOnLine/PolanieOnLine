/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;

public class FindDragons extends AbstractQuest {
	private static final String QUEST_SLOT = "where_dragon";

	private final HashMap<String, String> dragonHistory = new HashMap<String,String>();
	private void fillHistoryMap() {
		dragonHistory.put("Antithei",   "Antithei przebywał na Kościelisku.");
		dragonHistory.put("Felcor",     "Felcor przebywał w Ados Caves.");
		dragonHistory.put("Cinnabar",   "Cinnabar przebywał w Semos Mine.");
		dragonHistory.put("Mentis",     "Mentis przebywał w Zakopane Mines.");
		dragonHistory.put("Cornoctis",  "Cornoctis przebywał na Kalavan Forest.");
		dragonHistory.put("Ketsurui",   "Ketsurui przebywał na Orril Mountain.");
		dragonHistory.put("Adamantis",  "Adamantis przebywał na Athor Island.");
		dragonHistory.put("Cruorordis", "Cruorordis przebywał koło Warszawy.");
		dragonHistory.put("Hekate",     "Hekate przebywał na koscielisku Podzamcze.");
		dragonHistory.put("Miles",      "Miles przebywał na Desert Pyramid.");
		dragonHistory.put("Vircassis",  "Vircassis przebywał na Semos Mountain.");
		dragonHistory.put("Decida",     "Decida przebywał w Krakow Cave.");
		dragonHistory.put("Hikari",		"Hikari przebywał w Zakopane Mountain Room");
	}

	@Override
	public boolean isCompleted(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		final String npcDoneText = player.getQuest(QUEST_SLOT);
		final String[] done = npcDoneText.split(";");
		final int left = 13 - done.length;
		return left < 0;
	}

	static class DragonNPC extends SpeakerNPC {
		public DragonNPC(final String name, final int x, final int y) {
			super(name);

			setEntityClass("dragon1npc");
			setPosition(x, y);
			setCollisionAction(CollisionAction.REROUTE);
			initHP(100);

			final List<Node> nodes = new LinkedList<Node>();
			nodes.add(new Node(x, y));
			nodes.add(new Node(x - 4, y));
			nodes.add(new Node(x - 4, y - 2));
			nodes.add(new Node(x, y - 2));
			setPath(new FixedPath(nodes, true));

			// hide location from website
			hideLocation();
		}

		@Override
		protected void createPath() {
			// do nothing
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(getName()), true,
				ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT)) {
							player.setQuest(QUEST_SLOT, "");
						}

						final String npcDoneText = player.getQuest(QUEST_SLOT);
						final String[] done = npcDoneText.split(";");
						final List<String> list = Arrays.asList(done);
						final int left = 13 - list.size();

						if (list.contains(raiser.getName())) {
							if (left > -1) {
								raiser.say("Poszukaj innych smoków a zostaniesz " + player.getGenderVerb("wynagrodzony") + "!");
							} else {
								raiser.say(player.getGenderVerb("Znalazłeś") + " wszystkie smoki! Mam nadzieję, że jesteś " + player.getGenderVerb("zadowolony") + " z nagrody ☺☺☺");
							}
						} else {
							player.setQuest(QUEST_SLOT, npcDoneText + ";"
									+ raiser.getName());
							player.heal();
							player.getStatusList().removeAll(PoisonStatus.class);

							if (left > 0) {
								raiser.say("Witaj! Mnie " + player.getGenderVerb("odnalazłeś") + " ☺☺☺ Poszukaj jeszcze "
												+ (13 - list.size())
												+ " z moich braci. Żegnaj!");
								if (raiser.getZone().getName().equals("0_kościelisko_e")) {
									player.addXP(200);
								} else {
									player.addXP((13 - left + 1) * 500);
								}
							} else {
								raiser.say(player.getGenderVerb("Udowodniłeś") + ", że jesteś " + player.getGenderVerb("godny") + " tej nagrody!");

								final String[] items = { "magiczna tarcza płytowa", "tarcza chaosu", "tarcza xenocyjska" };
								final Item item = SingletonRepository.getEntityManager()
									.getItem(items[Rand.rand(items.length)]);
								item.setBoundTo(player.getName());
								player.equipOrPutOnGround(item);
								player.addXP(7000);
							}
						}
						player.notifyWorldAboutChanges();
					}
				});
			addGoodbye();
		}
	}

	@Override
	public void addToWorld() {
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		fillHistoryMap();
		fillQuestInfo(
			"Poszukiwanie Smoków",
			"Znajdź wszystkie 13 smoków.",
			false);

		StendhalRPZone zone;
		SpeakerNPC npc;

		zone = world.getZone("0_koscielisko_e");
		npc = new DragonNPC("Antithei", 68, 43);
		zone.add(npc);

		zone = world.getZone("-1_ados_caves_e");
		npc = new DragonNPC("Felcor", 75, 67);
		zone.add(npc);

		zone = world.getZone("-2_semos_mine_w2");
		npc = new DragonNPC("Cinnabar", 40, 121);
		zone.add(npc);

		zone = world.getZone("-3_zakopane_mines");
		npc = new DragonNPC("Mentis", 93, 63);
		zone.add(npc);

		zone = world.getZone("0_kalavan_forest");
		npc = new DragonNPC("Cornoctis", 9, 120);
		zone.add(npc);

		zone = world.getZone("0_orril_mountain_nw");
		npc = new DragonNPC("Ketsurui", 36, 121);
		zone.add(npc);

		zone = world.getZone("0_athor_island_e");
		npc = new DragonNPC("Adamantis", 73, 40);
		zone.add(npc);

		zone = world.getZone("0_warszawa_w");
		npc = new DragonNPC("Cruorordis", 16, 65);
		zone.add(npc);

		zone = world.getZone("1_koscielisko_podzamcze_zrc");
		npc = new DragonNPC("Hekate", 99, 7);
		zone.add(npc);

		zone = world.getZone("0_desert_pyramid_nw");
		npc = new DragonNPC("Miles", 4, 82);
		zone.add(npc);

		zone = world.getZone("0_semos_mountain_n2_e");
		npc = new DragonNPC("Vircassis", 15, 8);
		zone.add(npc);

		zone = world.getZone("-1_krakow_cave_sw");
		npc = new DragonNPC("Decida", 86, 44);
		zone.add(npc);

		zone = world.getZone("int_zakopane_mountain_room");
		npc = new DragonNPC("Hikari", 6, 7);
		zone.add(npc);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			final String npcDoneText = player.getQuest(QUEST_SLOT);
			final String[] done = npcDoneText.split(";");
			boolean first = true;
			for (final String dragon : done) {
				if (!dragon.trim().equals("")) {
					res.add(dragon.toUpperCase());
					if (first) {
						first = false;
						res.add(player.getGenderVerb("Podjąłem") + " się znalezienia wszystkich smoków.");
					}
					res.add(dragonHistory.get(dragon));
				}
			}
			if (isCompleted(player)) {
				res.add("Smoki " + player.getGenderVerb("znalazłem") + " i " + player.getGenderVerb("dostałem") + " nagrodę.");
			}
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Poszukiwanie Smoków";
	}
}
