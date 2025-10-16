package games.stendhal.server.maps.quests.allotment;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.GateKey;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Builds an allotment lessor NPC for Semos.
 *
 * @author kymara, filipe
 */
public class AllotmentLessorNPC implements ZoneConfigurator {
	private static final Logger logger = Logger.getLogger(AllotmentLessorNPC.class);
	private static final String RENT_COST_ATTRIBUTE = "rent_cost";
	private int rentalCost;

	private static String QUEST_SLOT = AllotmentUtilities.QUEST_SLOT;
	private AllotmentUtilities rentHelper;

	/**
	 * Configure a zone.
	 *
	 * @param zone The zone to be configured.
	 * @param attributes Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		rentHelper = AllotmentUtilities.get();
		rentalCost = parseRentCost(attributes);
		buildNPC(zone);
	}

	private int parseRentCost(final Map<String, String> attributes) {
		if ((attributes == null) || !attributes.containsKey(RENT_COST_ATTRIBUTE)) {
			return 0;
		}
		String value = attributes.get(RENT_COST_ATTRIBUTE);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			logger.warn("Nieprawidłowa wartość opłaty za działkę: " + value, ex);
			return 0;
		}
	}

	private boolean collectRent(final Player player, final EventRaiser npc) {
		if (rentalCost <= 0) {
			return true;
		}
		if (!player.isEquipped("money", rentalCost)) {
			npc.say("Wynajem działki kosztuje " + rentalCost + " money. Wróć, gdy będziesz " + player.getGenderVerb("miał") + " tyle przy sobie.");
			return false;
		}
		player.drop("money", rentalCost);
		return true;
	}

	/**
	 * Creates the NPC and sets the quest dialog
	 *
	 * @param zone The zone to be configured.
	 */
	private void buildNPC(final StendhalRPZone zone) {
		// condition to check if there are any allotments available
		final ChatCondition hasAllotments = new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return rentHelper.getAvailableAllotments(zone.getName()).size() > 0;
			}
		};

		/**
		 * condition to check if the player already has an allotment rented
		 * note: this is used instead QuestActiveCondition because it relies on
		 * the time that the player speaks to the NPC
		 */
		final ChatCondition questActive = new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return new QuestStateGreaterThanCondition(QUEST_SLOT, 1, (int) System.currentTimeMillis()).fire(player,  sentence, npc);
			}
		};

		// create the new NPC
		final SpeakerNPC npc = new SpeakerNPC("Jef's_twin") {

			@Override
			protected void createPath() {
				/*final List<Node> nodes = new LinkedList<Node>();

				nodes.add(new Node(70, 19));
                nodes.add(new Node(86,19));
                nodes.add(new Node(86,3));
                nodes.add(new Node(87,3));
                nodes.add(new Node(87,19));
                nodes.add(new Node(106,19));
                nodes.add(new Node(106,3));
                nodes.add(new Node(107,3));
                nodes.add(new Node(107,19));
                nodes.add(new Node(69,19));

				setPath(new FixedPath(nodes, true));*/
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Szukasz kawałka ziemi pod własne plony?");
				addJob("Opiekuję się semoskimi działkami i pilnuję, by wszystkie umowy były aktualne.");
				addHelp("Mogę powiedzieć, jak wynająć działkę, odnowić #klucz albo sprawdzić, ile #czasu zostało do końca umowy.");
				addOffer("Nie prowadzę sprzedaży, ale chętnie podzielę się #informacjami o wolnych działkach i zasadach korzystania.");
				addReply("informacje", "Wolne działki czekają na kolejnych ogrodników. Mogę też przygotować zapasowy #klucz albo przypomnieć warunki najmu.");
				addReply("sklepu", "Najlepsze zbiory to te z własnej ziemi. Jeśli potrzebujesz nasion lub narzędzi, znajdziesz je w Semos, a działkę doglądaj tutaj.");
				addGoodbye("Powodzenia z uprawami.");

				// if player already has one rented ask how may help
				add(ConversationStates.ATTENDING,
					Arrays.asList("wynajmij", "działkę"),
					questActive,
					ConversationStates.QUEST_STARTED,
					"Co mogę zrobić dla ciebie? Zgubiłeś swój #klucz czy chcesz inny, czy może chcesz dowiedzieć się ile #czasu zostało ci do wygaśnięcia umowy?",
					null);

				// if allotment not rented and there are available then ask if player wants to rent
				add(ConversationStates.ATTENDING,
					Arrays.asList("wynajmij", "działkę"),
					new AndCondition(
							new NotCondition(questActive),
							hasAllotments),
					ConversationStates.QUEST_OFFERED,
					"Chcesz wynająć działkę?",
					null);

				// if allotment not rented and there are none available then tell player
				add(ConversationStates.ATTENDING,
					Arrays.asList("wynajmij", "działkę"),
					new AndCondition(
							new NotCondition(questActive),
							new NotCondition(hasAllotments)),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							long diff = rentHelper.getNextExpiryTime(zone.getName()) - System.currentTimeMillis();

							npc.say("Jest mi przykro w tym momencie nie mamy wolnych. Proszę wróć za " + TimeUtil.approxTimeUntil((int) (diff / 1000L)) + ".");
						}
				});

				// if offer rejected
				add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Okej, co mogę jeszcze dla ciebie zrobić?",
					new SetQuestAction(QUEST_SLOT, 1, "0"));

				// if accepts to rent allotment
				add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.QUESTION_1,
					null,
					new ChatAction() {
						//say which ones are available
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							List<String> allotments = rentHelper.getAvailableAllotments(zone.getName());
							String reply = Grammar.enumerateCollection(allotments);

							String priceHint = (rentalCost > 0) ? " Całkowity koszt wynosi " + rentalCost + " money." : "";
							npc.say("Którą chcesz? Popatrzmy... " + Grammar.plnoun(allotments.size(), "działkę") + " "
									+ reply + " są dostępne, chyba, że #nie chcesz tej działki." + priceHint);
						}
					});

				// to exit renting/choosing an allotment
				add(ConversationStates.ANY,
					"nie",
					null,
					ConversationStates.ATTENDING,
					"tak.",
					null);

				// do business
				add(ConversationStates.QUESTION_1,
					"",
					new TextHasNumberCondition(),
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						// does the transaction if possible
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							final int number = sentence.getNumeral().getAmount();
							final String allotmentNumber = Integer.toString(number);

							if (!rentHelper.isValidAllotment(zone.getName(), allotmentNumber)) {
								npc.say("Obawiam się, że działka nie istnieje.");
							} else {
								if (rentHelper.getAvailableAllotments(zone.getName()).contains(allotmentNumber)) {
									if (rentHelper.setExpirationTime(zone.getName(), allotmentNumber, player.getName())) {
										if (!collectRent(player, npc)) {
											rentHelper.clearRental(zone.getName(), allotmentNumber);
											return;
										}

										npc.say("Oto klucz do działki " + allotmentNumber + ". Otrzymałeś pozwolenie na używanie działki na czas "
												+ TimeUtil.approxTimeUntil((int) (AllotmentUtilities.RENTAL_TIME / 1000L)) + ".");

										if (!player.equipToInventoryOnly(rentHelper.getKey(zone.getName(), player.getName()))) {
											npc.say("Widzę, że nie masz miejsca w plecaku. Zatrzymam klucz do momentu twojego powrotu. Zapytaj się mnie o #działkę.");
										}

										new SetQuestAction(QUEST_SLOT, 1, Long.toString(AllotmentUtilities.RENTAL_TIME + System.currentTimeMillis())).fire(player, sentence, npc);
									} else {
										// error? shouldn't happen
										npc.say("Uuuu! Jest jakiś problem w papierach. Proszę daj mi trochę czasu aby naprawić ten problem.");
									}
								} else {
									npc.say("Jest mi przykro, ale ta działka jest już zajęta.");
								}
							}
						}
					});

				// if player asked about key
				add(ConversationStates.QUEST_STARTED,
					"klucz",
					null,
					ConversationStates.ATTENDING,
					"",
					// gives player a new key to give to friends to use
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							GateKey key = rentHelper.getKey(zone.getName(), player.getName());

							if (key != null) {
								if (player.equipToInventoryOnly(key)) {
									npc.say("Tu masz swój klucz, miłego sadzenia.");
								} else {
									npc.say("Nie masz miejsca w plecaku. Wróć ponownie gdy będziesz miał miejsce.");
								}
							} else {
								npc.say("Musiałeś pomylić się. Osoba o tym imieniu niczego nie wynajeła.");
							}
						}
					});

				// if player asked about remaining time
				add(ConversationStates.QUEST_STARTED,
					"czasu",
					null,
					ConversationStates.ATTENDING,
					null,
					// gets a new key
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							npc.say("Pozostało tobie jeszcze " + rentHelper.getTimeLeftPlayer(zone.getName(), player.getName()) + " czasu.");
						}
					});

			}
		};

		npc.setEntityClass("gardenernpc");
		npc.setPosition(85, 11);
		npc.initHP(100);
		npc.setDescription("Opiekun semoskich działek nadzoruje wynajem i pielęgnację ogródków.");
		zone.add(npc);
	}
}
