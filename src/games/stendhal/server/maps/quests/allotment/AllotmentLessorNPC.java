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
 
/**
 * Builds an allotment lessor NPC for Semos. 
 *
 * @author kymara, filipe
 */
public class AllotmentLessorNPC implements ZoneConfigurator {
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
		buildNPC(zone);
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
				// TODO: this was copy pasted change as needed
				addGreeting("Cześć!");
				addJob("Hm, nie mam pojęcia o czym mówisz. Czekam na moją mamę, aż wróci ze #sklepu.");
				addHelp("Posiadam #informacje o tym bazarze tu obok.");
				addOffer("Niczego nie sprzedaję. Czekam na moją mamę. Ale mogę ci zdradzić #informacje, jeżeli jesteś ciekawy.");
				// quest: FindJefsMom , quest sentence given there
				addReply("informacje", "Doszły mnie słuchy, iż nie długo będzie więcej sprzedawców. Wtedy bazar ożyje, jak na razie jest tam pusto i nie ma ruchu.");
				addReply("sklepu", "Musiała iść po za miasto. Na tym bazarze obok, jedynym sprzedawcą jest kwiaciarka.Krążą #informacje, że na bazar ma...");
				addGoodbye("Do zobaczenia.");

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
							
							npc.say("Jest mi przykro w tym momęcie nie mamy wolnych. Proszę wróć za " + TimeUtil.approxTimeUntil((int) (diff / 1000L)) + ".");
						}					
				});				
				
				// if offer rejected
				add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Okey, co mogę jeszcze dla ciebie zrobić?",
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
							
							npc.say("Którą chcesz? Popatrzmy... " + Grammar.plnoun(allotments.size(), "działkę") + " " 
									+ reply + " są dostępne, chyba, że #nie chesz tej działki.");
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
							
							//TODO: get payment
							if (!rentHelper.isValidAllotment(zone.getName(), allotmentNumber)) {
								npc.say("Obawiam się, że działka nie istnieje.");
							} else {
								if (rentHelper.getAvailableAllotments(zone.getName()).contains(allotmentNumber)) {
									if(rentHelper.setExpirationTime(zone.getName(), allotmentNumber, player.getName())) {
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
 
		//TODO: also copy-pasted change as needed
		npc.setEntityClass("kid6npc");
		npc.setPosition(85, 11);
		npc.initHP(100);
		npc.setDescription("Oto klon Jefa. Wygląda jakby na kogoś czekał.");
		zone.add(npc);
	}
}