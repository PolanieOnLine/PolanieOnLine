/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.antivenom_ring.ApothecaryStage;
import games.stendhal.server.maps.quests.antivenom_ring.ZoologistStage;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Runs actual quest dialogue registration and checks replies through the conversation engine. */
public class PolishQuestReplyTest {
	private final Map<String, SpeakerNPC> previousNPCs = new LinkedHashMap<>();
	private Player player;

	@BeforeClass public static void prepareWorld() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void preparePlayer() { player = PlayerTestHelper.createPlayer("alias-tester"); }
	@After public void restoreNPCs() {
		NPCList npcs = SingletonRepository.getNPCList();
		for (Map.Entry<String, SpeakerNPC> entry : previousNPCs.entrySet()) {
			npcs.remove(entry.getKey());
			if (entry.getValue() != null) npcs.add(entry.getValue());
		}
	}

	private SpeakerNPC npc(String name) {
		NPCList npcs = SingletonRepository.getNPCList();
		previousNPCs.put(name, npcs.remove(name));
		SpeakerNPC npc = new SpeakerNPC(name);
		npcs.add(npc);
		return npc;
	}

	private void register(Object quest, String methodName) throws Exception {
		Method method = quest.getClass().getDeclaredMethod(methodName);
		method.setAccessible(true);
		method.invoke(quest);
	}

	private void replies(SpeakerNPC npc, String expected, String... phrases) {
		for (String phrase : phrases) {
			npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
			assertTrue(npc.getName() + ": " + phrase, npc.getEngine().step(player, phrase));
			String reply = getReply(npc);
			assertNotNull(npc.getName() + ": " + phrase, reply);
			assertTrue(phrase + " returned: " + reply, reply.contains(expected));
			assertEquals(ConversationStates.ATTENDING, npc.getEngine().getCurrentState());
		}
	}

	@Test public void monogenesUnderstandsThePolishChestAndMapLinks() {
		SpeakerNPC npc = npc("Monogenes");
		new MeetMonogenes().addToWorld();
		replies(npc, "publicznej skrzyni", "publiczna skrzynia", "publicznej skrzyni", "publiczną skrzynię", "skrzynia", "skrzyni", "public chest");
		replies(npc, "Zaznaczyłem", "mapę", "mapy", "mapie");
	}

	@Test public void fryderykUnderstandsPlaceNamesInPolishCases() {
		SpeakerNPC npc = npc("Fryderyk");
		new MeetFryderyk().addToWorld();
		replies(npc, "Mapa miasta Zakopane", "mapę", "mapie");
		replies(npc, "żółty napis", "banku");
		replies(npc, "czerwonym krzyżem", "szpitala", "szpitalu");
		replies(npc, "Gazdę Wojtka", "burmistrza Zakopanego", "burmistrzu Zakopanego");
		replies(npc, "mały domek", "domu gier");
		replies(npc, "Herold", "boiska", "boisku");
		replies(npc, "kowala", "kuźni", "kuźnię");
		replies(npc, "ścieżkę", "kościeliska", "kościelisku");
		replies(npc, "torów", "krakowa", "krakowie");
	}

	@Test public void vulcanusUnderstandsTheHighlightedPolishInstruction() throws Exception {
		SpeakerNPC npc = npc("Vulcanus");
		register(new StuffForVulcanus(), "step_1");
		replies(npc, "podanej kolejności", "dokładnie", "kolejność", "kolejności", "exact");
	}

	@Test public void sergeantUnderstandsDruzynaInsteadOfTheOldTypo() throws Exception {
		SpeakerNPC npc = npc("Sergeant James");
		register(new KanmararnSoldiers(), "prepareSergeant");
		replies(npc, "Było nas pięciu", "drużyna", "drużyny", "drużynę", "drużynie", "group");
		replies(npc, "Wielki skarb", "skarb", "skarbu", "skarby");
		replies(npc, "silnymi przeciwnikami", "krasnal", "krasnale", "krasnalach");
		replies(npc, "legendarnym królestwem", "królestwo");
	}

	@Test public void zynnUnderstandsTopicsAndLinksTheCorrectNalworName() {
		SpeakerNPC npc = npc("Zynn Iwuhos");
		new MeetZynn().addToWorld();
		replies(npc, "dwie znaczące potęgi", "historia", "historię");
		replies(npc, "różnych #miejscach", "geografia", "geografię");
		replies(npc, "#Nalwor", "miejsce", "miejsca", "miejsc");
		replies(npc, "starożytne elfickie miasto", "Nalwor");
		replies(npc, "zestawy", "nazewnictwo", "nazewnictwa");
	}

	@Test public void ortivUnderstandsInflectedIngredients() throws Exception {
		SpeakerNPC npc = npc("Ortiv Milquetoast");
		register(new MixtureForOrtiv(), "prepareRequestingStep");
		replies(npc, "Jabłka", "jabłka", "jabłek");
		replies(npc, "młodej kobiecie", "butelki", "butelkę", "butelek");
		replies(npc, "Muchomory są trujące", "muchomora", "muchomory", "muchomorów");
		replies(npc, "zioła", "aranduli", "arandulę");
		replies(npc, "biały", "skrzydlicy", "skrzydlicę", "skrzydlice");
		replies(npc, "wyspy Athor", "kokudy", "kokudę");
		replies(npc, "słodkie", "lukrecji", "lukrecję");
		replies(npc, "szklanką", "napoju z winogron", "napoje z winogron");
		replies(npc, "wampirami", "czosnku");
	}

	@Test public void apothecaryUnderstandsRingAndIngredientCases() throws Exception {
		SpeakerNPC npc = npc("test-apothecary");
		new ApothecaryStage(npc.getName(), "antivenom_ring").addToWorld();
		for (int i = 0; i < 25; i++) {
			replies(npc, "trujące potwory", "pierścienia leczniczego", "pierścienie lecznicze", "pierścieniu leczniczym", "medicinal ring");
			replies(npc, "wzmocnić", "pierścienia antyjadowego", "pierścienie antyjadowe", "pierścieniu antyjadowym", "antivenom ring");
			replies(npc, "wiele rodzai", "pierścień", "pierścienie", "ring");
		}
		replies(npc, "sanktuarium", "jadu", "jadu kobry");
		replies(npc, "ziół", "mandragorę");
		replies(npc, "przekąską", "mufinki", "mufinkę", "mufinek");
	}

	@Test public void zoologistUnderstandsVenomousAnimalForms() throws Exception {
		SpeakerNPC npc = npc("test-zoologist");
		new ZoologistStage(npc.getName(), "antivenom_ring").addToWorld();
		replies(npc, "ekstrakcji", "jadowity", "jadowite", "jadowite zwierzęta", "venomous");
	}

	@Test public void mithrilFabricHintsRecognizeTheActualMaterialNames() throws Exception {
		SpeakerNPC scientist = npc("Vincento Price");
		SpeakerNPC spinner = npc("Kampusch");
		Class<?> infoClass = Class.forName("games.stendhal.server.maps.quests.mithrilcloak.MithrilCloakQuestInfo");
		Constructor<?> infoConstructor = infoClass.getDeclaredConstructor();
		infoConstructor.setAccessible(true);
		Class<?> fabricClass = Class.forName("games.stendhal.server.maps.quests.mithrilcloak.MakingFabric");
		Constructor<?> constructor = fabricClass.getDeclaredConstructor(infoClass);
		constructor.setAccessible(true);
		Object fabric = constructor.newInstance(infoConstructor.newInstance());
		register(fabric, "makeThreadStep");
		register(fabric, "makeMithrilThreadStep");
		replies(scientist, "Utkam", "przędzy", "przędzę");
		replies(scientist, "królowa pająków", "gruczoł przędzy", "gruczołu przędzy", "gruczoły przędzy");
		replies(spinner, "aniołki", "balonika", "baloniki", "baloników");
		replies(spinner, "40 szpulek", "przędza jedwabna", "przędzę jedwabną");
		replies(spinner, "Możesz sam", "bryłka mithrilu", "bryłkę mithrilu", "bryłki mithrilu");
	}
}
