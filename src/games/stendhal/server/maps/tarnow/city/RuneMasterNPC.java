/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.city;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.player.Player;

/**
 * @author KarajuSs
 */
public class RuneMasterNPC implements ZoneConfigurator {
	private static final String QUEST_SLOT = "glyph_creator";

	private GlyphManager glyphManager;

	private void initializeGlyphs() {
		Map<String, Map<String, Integer>> glyphConfig = new LinkedHashMap<String, Map<String, Integer>>();
		glyphConfig.put("glif daru Mokoszy", createResourceMap(
			"serce olbrzyma", 100,
			"smocza krew", 40,
			"fragment glifu", 4
		));
		glyphConfig.put("glif siły", createResourceMap(
			"miecz", 1,
			"kieł smoka", 20,
			"kość dla psa", 50,
			"fragment glifu", 2
		));
		glyphConfig.put("glif Peruna", createResourceMap(
			"glif siły", 1,
			"rubin", 18,
			"pióro azazela", 12,
			"pióro lilith", 4,
			"fragment glifu", 5
		));
		glyphConfig.put("glif czaszy", createResourceMap(
			"kieł złotej kostuchy", 1,
			"pióro archanioła", 22,
			"ruda srebra", 40,
			"fragment glifu", 3
		));
		glyphConfig.put("glif tarczy", createResourceMap(
			"bryłka mithrilu", 30,
			"lodowa tarcza", 1,
			"ruda żelaza", 100,
			"fragment glifu", 2
		));
		glyphConfig.put("glif Swaroga", createResourceMap(
			"glif tarczy", 1,
			"szafir", 22,
			"pióro serafina", 16,
			"pióro cherubina", 4,
			"fragment glifu", 5
		));
		glyphConfig.put("glif Tytana", createResourceMap(
			"glif tarczy", 1,
			"glif siły", 1,
			"serce olbrzyma", 50,
			"cudowna krew", 4,
			"kolorowe kulki", 30,
			"fragment glifu", 3
		));
		glyphConfig.put("glif Strzyboga", createResourceMap(
			"piórko", 100,
			"pióro anioła", 40,
			"cudowna krew", 3,
			"bursztyn", 20,
			"ametyst", 32,
			"fragment glifu", 4
		));
		glyphConfig.put("glif Jarowita", createResourceMap(
			"magia ziemi", 1000,
			"magia płomieni", 800,
			"magia deszczu", 800,
			"magia mrozu", 600,
			"magia wiatru", 400,
			"magia mroku", 400,
			"magia światła", 300,
			"kryształ obsydianu", 11,
			"fragment glifu", 3
		));
		glyphConfig.put("glif kryzysu", createResourceMap(
			"kości do gry", 1,
			"diament", 14,
			"ząb potwora", 3,
			"sztabka platyny", 40,
			"sztabka miedzi", 32,
			"fragment glifu", 2
		));
		glyphConfig.put("glif krwi", createResourceMap(
			"wampirza krew", 36,
			"truchło nietoperza", 14,
			"truchło wampira", 22,
			"obsydian", 22,
			"fragment glifu", 3
		));

		glyphConfig.forEach((glyphName, resources) -> {
			Glyph glyph = glyphManager.createGlyph(glyphName);
			resources.forEach(glyph::addResource);
		});
	}

	private Map<String, Integer> createResourceMap(final Object... keyValuePairs) {
		Map<String, Integer> resources = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < keyValuePairs.length; i += 2) {
			String key = (String) keyValuePairs[i];
			Integer value = (Integer) keyValuePairs[i + 1];
			resources.put(key, value);
		}
		return resources;
	}

	/**
	 * Configure a zone.
	 *
	 * @param   zone		The zone to be configured.
	 * @param   attributes  Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		glyphManager = new GlyphManager();
		initializeGlyphs();

		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Zoryk Runiczny") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj, poszukiwaczu starożytnej mocy! Czy szukasz tajemnicy run?");
				addJob("Jestem strażnikiem starożytnej sztuki runicznej. Potrafię nasycać odzyskane matryce glifów nową mocą. Omar, pustynny poszukiwacz reliktów, bada miejsca, w których zachowały się takie nośniki. Możesz zapytać mnie o #listę glifów.");
				addOffer("Jeżeli przyniesiesz czysty fragment glifu, czyli nośnik odtworzony z dawnej matrycy, oraz odpowiednie składniki, mogę nadać mu nowy znak. Zapytaj o #listę glifów, aby zobaczyć, jakie znam wzory.");
				addGoodbye("Powodzenia w zdobywaniu glifów, młody poszukiwaczu mocy!");

				String allGlyphNames = glyphManager.getAllGlyphs().stream()
					.map(glyph -> "#'" + glyph.getName() + "'")
					.reduce((name1, name2) -> name1 + ", " + name2)
					.orElse("Brak glifów.");

				add(ConversationStates.ATTENDING,
					Arrays.asList("Omar", "omar", "fragment", "fragmenty", "matryca", "matryce"),
					null,
					ConversationStates.ATTENDING,
					"Omar odnajduje na pustyni pozostałości dawnych matryc glifów i potrafi oczyścić ich uszkodzone fragmenty. Ja zajmuję się tym, czego on zrobić nie może: nasycam odzyskany nośnik nową mocą i odtwarzam na nim wybrany znak.",
					null
				);

				add(ConversationStates.ATTENDING,
					Arrays.asList("lista glifów", "lista"),
					new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
					ConversationStates.ATTENDING,
					"Znam następujące wzory glifów: " + allGlyphNames + ". Każdy wymaga odzyskanego fragmentu oraz składników związanych z mocą danego znaku.",
					null
				);

				add(ConversationStates.ATTENDING,
					Arrays.asList("glyph", "glif", "glify"),
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							String glyphName = player.getQuest(QUEST_SLOT, 1);
							raiser.say("Pamiętam, że " + Grammar.genderVerb(player.getGender(), "pytałeś") + " mnie o #'" + glyphName + "'. Przygotuj odzyskany fragment oraz pozostałe składniki: " + glyphManager.getGlyph(glyphName).getFormattedResources() + ". Jeżeli zmienisz zdanie, możesz #zamienić wybrany wzór.");
						}
					}
				);

				add(ConversationStates.ATTENDING,
					Arrays.asList("another", "change", "inny", "zmień", "zamień", "zamienić"),
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							raiser.say("Dobrze. Powiedz, jaki znak mam odtworzyć. Znam następujące glify: " + allGlyphNames + ".");
							player.removeQuest(QUEST_SLOT);
						}
					}
				);

				for (Glyph glyph : glyphManager.getAllGlyphs()) {
					addDialogForGlyph(glyph);
				}
			}

			private void addDialogForGlyph(Glyph glyph) {
				final String glyphName = glyph.getName();

				add(ConversationStates.ATTENDING,
					glyphName,
					new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
					ConversationStates.SERVICE_OFFERED,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							Expression obj = sentence.getObject(0);
							if (obj != null && !obj.getNormalized().equalsIgnoreCase(glyphName)) {
								raiser.say("Nie znam takiego glifu jak " + obj.getOriginal() + ".");
							} else {
								raiser.say(Grammar.makeUpperCaseWord(glyphName) + " to " + getGlyphDescription(glyphName) + ". Aby odtworzyć ten znak na matrycy, potrzebuję następujących składników: " + glyph.getFormattedResources() + ". Jesteś " + Grammar.genderVerb(player.getGender(), "zainteresowany") + " nim?");
								player.setQuest("glyph_request", glyphName);
							}
						}
					}
				);

				add(ConversationStates.SERVICE_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new QuestInStateCondition("glyph_request", glyphName),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							String glyphName = player.getQuest("glyph_request", 0);
							Glyph selectedGlyph = glyphManager.getGlyph(glyphName);
							if (selectedGlyph == null) {
								raiser.say("Nie mogę znaleźć wybranego glifu: " + glyphName + ".");
								return;
							}

							raiser.say("Dobrze. Przygotuj odzyskany fragment oraz pozostałe składniki: " + selectedGlyph.getFormattedResources() + ".");
							player.setQuest(QUEST_SLOT, "start;" + glyphName);
							player.removeQuest("glyph_request");
						}
					}
				);

				add(ConversationStates.SERVICE_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					new QuestInStateCondition("glyph_request", glyphName),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							raiser.say("Rozumiem. Możemy wybrać inny wzór, gdy będziesz gotowy.");
							player.removeQuest("glyph_request");
						}
					}
				);

				final ChatCondition resourceConditions = new ChatCondition() {
				    @Override
				    public boolean fire(Player player, Sentence sentence, Entity npc) {
				        String glyphName = player.getQuest(QUEST_SLOT, 1);
				        Glyph selectedGlyph = glyphManager.getGlyph(glyphName);

				        if (selectedGlyph != null) {
				            List<ChatCondition> conditions = selectedGlyph.getResourceConditions();
				            return new AndCondition(conditions.toArray(new ChatCondition[0])).fire(player, sentence, npc);
				        }
				        return false;
				    }
				};

				add(ConversationStates.ATTENDING,
					glyphName,
					new AndCondition(new QuestInStateCondition(QUEST_SLOT, 1, glyphName), resourceConditions),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							String glyphName = player.getQuest(QUEST_SLOT, 1);
							Glyph selectedGlyph = glyphManager.getGlyph(glyphName);
							if (selectedGlyph == null) {
								raiser.say("Nie mogę znaleźć wybranego glifu: " + glyphName + ".");
								return;
							}

							selectedGlyph.getResources().forEach((resourceName, amount) -> {
								player.drop(resourceName, amount);
							});
							raiser.say("Gotowe. Matryca przyjęła znak " + glyphName + ". Korzystaj z jego mocy mądrze.");

							player.setQuest(QUEST_SLOT, "done");
							player.notifyWorldAboutChanges();
							player.equipOrPutOnGround(selectedGlyph.getItem());
							player.incProducedForItem(glyphName, selectedGlyph.getItem().getQuantity());
						}
					}
				);

				add(ConversationStates.ATTENDING,
					glyphName,
					new AndCondition(new QuestInStateCondition(QUEST_SLOT, 1, glyphName), new NotCondition(resourceConditions)),
					ConversationStates.ATTENDING,
					"Wróć, gdy będziesz miał przy sobie wszystkie składniki potrzebne do odtworzenia glifu " + glyphName + ". Oto lista: " + glyph.getFormattedResources() + ".",
					null
				);
			}

			private String getGlyphDescription(String glyphName) {
				switch (glyphName.toLowerCase()) {
					case "glif daru mokoszy":
						return "dar bogini płodności i ochrony, zwiększający zdrowie o 500 punktów";
					case "glif siły":
						return "glif zwiększający siłę ataku o 5 punktów";
					case "glif peruna":
						return "glif zwiększający siłę ataku o 10% posiadanego ataku";
					case "glif czaszy":
						return "glif sprawiający, że trafienia krytyczne zadają dodatkowe 25% bazowych obrażeń, zwiększając mnożnik z 2,00x do 2,25x";
					case "glif tarczy":
						return "glif zwiększający obronę o 20 punktów";
					case "glif swaroga":
						return "glif zwiększający obronę o 20% posiadanej obrony";
					case "glif tytana":
						return "glif zwiększający atak i obronę o 5 punktów oraz zdrowie o 200 punktów";
					case "glif strzyboga":
						return "glif zmniejszający wagę wolniejszych broni o 1, jeśli ich waga jest większa niż 2; waga broni nie spada poniżej 2";
					case "glif jarowita":
						return "glif redukujący wpływ wszystkich żywiołów o 15%";
					case "glif kryzysu":
						return "glif zwiększający szansę na atak krytyczny o 15%";
					case "glif krwi":
						return "glif zwiększający leczenie z kradzieży życia o 20%";
					default:
						return "potężny glif o nieopisanej mocy";
				}
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Zoryk Runiczny. Stary i mądry znawca dawnych matryc, który potrafi nasycać odzyskane fragmenty mocą glifów.");
		npc.setEntityClass("oldwizardnpc");
		npc.setGender("M");
		npc.setPosition(60, 18);
		zone.add(npc);
	}
}

class Glyph { 
	private String name;
	private TreeMap<String, Integer> resources;

	public Glyph(String name) {
		this.name = name;
		this.resources = new TreeMap<>();
	}

	public String getName() {
		return name;
	}

	public Item getItem() {
		return SingletonRepository.getEntityManager().getItem(getName());
	}

	public void addResource(String resourceName, int amount) {
		resources.put(resourceName,  resources.getOrDefault(resourceName, 0) + amount);
	}

	public Map<String, Integer> getResources() {
		return resources;
	}

	/**
	 * Generate a list of conditions for the required resources.
	 * Each condition checks if the player has a specific resource in the required quantity.
	 */
	public List<ChatCondition> getResourceConditions() {
		List<ChatCondition> conditions = new ArrayList<>();
		resources.forEach((resourceName, amount) -> {
			conditions.add(new PlayerHasItemWithHimCondition(resourceName, amount));
		});
		return conditions;
	}

	public String getFormattedResources() {
		StringBuilder resourcesList = new StringBuilder();
		resources.forEach((resource, amount) -> {
			resourcesList.append("#'" + resource + "' (" + amount + " szt.), ");
		});

		// Remove the last comma and space if the list is not empty
		if (resourcesList.length() > 0) {
			resourcesList.setLength(resourcesList.length() - 2);
		}

		return resourcesList.toString();
	}
}

class GlyphManager {
	private TreeMap<String, Glyph> glyphs;

	public GlyphManager() {
		glyphs = new TreeMap<>();
	}

	public Glyph createGlyph(String glyphName) {
		Glyph glyph = new Glyph(glyphName);
		glyphs.put(glyphName, glyph);
		return glyph;
	}

	public Glyph getGlyph(String glyphName) {
		return glyphs.get(glyphName);
	}

	public List<Glyph> getAllGlyphs() {
		return new ArrayList<>(glyphs.values());
	}
}