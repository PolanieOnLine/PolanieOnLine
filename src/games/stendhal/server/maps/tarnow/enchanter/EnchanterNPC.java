/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.tarnow.enchanter;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.actions.AffixRerollAction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

/** Przemir's craft is changing equipment affixes, separate from Zoryk's glyphs. */
public final class EnchanterNPC implements ZoneConfigurator {
	public static final String NAME = "Przemir Zaklinacz";
	public static final String ZONE = "int_tarnow_enchanter_workshop";
	public static final int X = 8;
	public static final int Y = 5;

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC(NAME) {
			@Override
			protected void createDialog() {
				addGreeting("Witaj w mojej pracowni. Szukasz innej mocy dla swojego wyposażenia? Użyj stołu przede mną albo zapytaj o #przemianę.");
				addJob("Jestem zaklinaczem. Odkrywam moc ukrytą w broni, pancerzach i ozdobach. Kamienie przemiany pozwalają mi nadać jednej z ich właściwości nową postać.");
				addHelp("Przynieś wyposażenie z magicznymi właściwościami. Użyj stołu albo powiedz #przemiana, a obejrzymy, co można zmienić. Pierwsza próba wymaga kamieni przemiany, a kolejne sztabek, łupów z potworów i monet. O szczegóły zapytaj, mówiąc #zasady.");
				addOffer("Mogę przeprowadzić #przemianę jednej właściwości twojego wyposażenia. Pierwsza próba kosztuje jeden kamień przemiany dla zwykłego afiksu lub trzy dla legendarnego. Przy kolejnych próbach potrzebuję surowców i monet. Koszt zależy od rzadkości przedmiotu i rodzaju afiksu. Przed próbą możesz poznać #zasady.");
				addGoodbye("Niech twój oręż dobrze ci służy.");
				add(ConversationStates.ATTENDING, Arrays.asList("przemiana", "przemianę", "przemiane", "przemianą", "zmiana afiksu", "afiks", "afiksy", "affix", "reroll", "transformation", "transformación"),
						null, ConversationStates.ATTENDING, null,
						(player, sentence, raiser) -> AffixRerollAction.open(player, this));
				add(ConversationStates.ATTENDING, Arrays.asList("zasady", "zasadach", "rules", "reglas"),
						null, ConversationStates.ATTENDING,
						"Najpierw wskażesz przedmiot i właściwość, którą chcesz zmienić. Pierwsza próba wymaga kamieni przemiany. Możesz przyjąć nową moc albo zachować dotychczasową, ale kamienie przepadną w obu przypadkach. Od tej chwili tylko ta jedna właściwość będzie mogła podlegać dalszym przemianom. Za kolejne próby zapłacisz sztabkami, łupami z potworów i monetami. Im rzadszy przedmiot, tym cenniejszych składników potrzebuję. Legendarny afiks wymaga ich trzy razy więcej niż zwykły. Składniki i monety zużyję także wtedy, gdy zachowasz dawną moc.", null);
				add(ConversationStates.ATTENDING, Arrays.asList("kamienie", "kamień", "kamien", "stones", "piedras"),
						null, ConversationStates.ATTENDING,
						"Kamienie przemiany bywają nagrodą podczas szczególnych wydarzeń w naszej krainie. Zwykły afiks odblokuję za jeden kamień, a legendarny za trzy. Nawet jeśli zachowasz dawną moc, kamienie zostaną zużyte. Później możesz dalej zmieniać tę samą właściwość za surowce i monety, bez kolejnych kamieni.", null);
				add(ConversationStates.ATTENDING, Arrays.asList("glif", "glify", "glyph", "glyphs", "glifos"),
						null, ConversationStates.ATTENDING,
						"O glifach najlepiej porozmawiać z Zorykiem Runicznym. Znajdziesz go przy północnej świątyni w Tarnowie, obok stołu runicznego.", null);
			}
		};
		npc.setDescription("Oto Przemir Zaklinacz. Ogląda oręż pod światło, jakby szukał w nim czegoś ukrytego.");
		npc.setEntityClass("wizardnpc");
		npc.setGender("M");
		npc.setPosition(X, Y);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
