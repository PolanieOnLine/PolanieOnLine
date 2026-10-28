/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.woodcutter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.NameSearch;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.player.Player;

/** A charcoal burner working on the clearing beside the woodcutter's hut. */
public class CharcoalBurnerNPC implements ZoneConfigurator {
	private static final String PRODUCTION_SLOT = "smolislaw_burn_charcoal";
	private static final int MAX_ORDER = 1000;
	private static final Set<String> CHARCOAL_NAMES = new HashSet<>(Arrays.asList(
			"węgiel drzewny", "węgla drzewnego", "węgiel", "węgla", "charcoal"));

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Smolisław") {
			@Override
			protected void createDialog() {
				final Map<String, Integer> resources = new LinkedHashMap<>();
				resources.put("polano", 2);
				resources.put("money", 5);
				final ProducerBehaviour production = new ProducerBehaviour(PRODUCTION_SLOT,
						Arrays.asList("wypal", "wypalaj", "burn"), "węgiel drzewny", resources, 30) {
					@Override
					protected String formatProductQuantity(final int amount) {
						return Grammar.itemQuantityAccusative(amount, getProductName());
					}

					@Override
					protected String formatProductCollectionError(final int amount) {
						return formatProductQuantity(amount);
					}

					@Override
					public String getRequiredResourceNames(final int amount) {
						return Grammar.itemQuantityAccusative(amount * getRequiredResourcesPerUnit().get("polano"), "polano")
								+ " oraz " + MoneyUtils.formatPrice(amount * getRequiredResourcesPerUnit().get("money"));
					}

					@Override
					protected String getRequiredResourceNamesWithHashes(final int amount) {
						return getRequiredResourceNames(amount).replaceFirst(" ", " #");
					}

					@Override
					public ItemParserResult parse(final Sentence sentence) {
						ItemParserResult result = super.parse(sentence);
						if (!result.wasFound()) {
							final NameSearch alias = sentence.findMatchingName(CHARCOAL_NAMES);
							if (alias.found()) {
								result = new ItemParserResult(true, getProductName(), alias.getAmount(), null);
							}
						}
						// Reject before ProducerAdder changes oversized requests to one item.
						if (result.wasFound() && (result.getAmount() < 1 || result.getAmount() > MAX_ORDER)) {
							return new ItemParserResult(false, result.getChosenItemName(), result.getAmount(), null);
						}
						return result;
					}

					@Override
					public String getErrormessage(final ItemParserResult result, final String activity) {
						if (result.getAmount() < 1 || result.getAmount() > MAX_ORDER) {
							return "Przyjmuję zlecenia od 1 do " + MAX_ORDER + " sztuk węgla drzewnego naraz.";
						}
						return super.getErrormessage(result, activity);
					}
				};
				production.setUnitsPerTime(MAX_ORDER);
				new ProducerAdder().addProducer(this, production,
						"Witaj na mojej polanie! Uważaj na dym z #mielerza. Jeżeli potrzebujesz węgla drzewnego, powiedz #wypal.");
				addJob("Jestem węglarzem. Powoli wypalam drewno w #mielerzu, żeby otrzymać węgiel drzewny.");
				addOffer("Za każde 2 polana i 5 monet wypalę jedną sztukę węgla drzewnego. Przyjmuję do 1000 sztuk w jednym zleceniu. Wypalanie jednej sztuki trwa 30 sekund.");
				addHelp("Powiedz na przykład #'wypal 100 węgla drzewnego'. Przygotuję zlecenie do potwierdzenia, a później odbierzesz je słowem #odbierz albo przy następnym powitaniu. Nie musisz czekać tutaj ani pozostawać w grze.");
				addReply(Arrays.asList("mielerz", "mielerza"),
						"Pod tą warstwą ziemi leżą ciasno ułożone polana. Pilnuję dopływu powietrza, żeby drewno nie spłonęło na popiół.");
				addReply(Arrays.asList("Drwal", "drwal", "drewno", "polano", "polana"),
						"Drwal mieszka w pobliskiej chacie. U niego nauczysz się ścinać drzewa oznaczone do wyrębu. Gotowe polana przynieś do mnie.");
				final List<String> collection = Arrays.asList("odbierz", "odbiór", "odbior", "collect");
				getEngine().add(ConversationStates.ATTENDING, collection, new QuestActiveCondition(PRODUCTION_SLOT),
						false, ConversationStates.ATTENDING, null, new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								production.giveProduct(raiser, player);
							}
						});
				getEngine().add(ConversationStates.ATTENDING, collection, new QuestNotActiveCondition(PRODUCTION_SLOT),
						false, ConversationStates.ATTENDING, "Nie mam dla ciebie zleconego węgla. Jeśli chcesz złożyć zamówienie, powiedz #wypal.", null);
				addGoodbye("Do zobaczenia! Oby wiatr nie zaniósł ci dymu prosto w oczy.");
			}
		};
		npc.setDescription("Oto Smolisław, węglarz pilnujący mielerza na leśnej polanie.");
		npc.setEntityClass("charcoalburnernpc");
		npc.setGender("M");
		npc.setPosition(57, 58);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
