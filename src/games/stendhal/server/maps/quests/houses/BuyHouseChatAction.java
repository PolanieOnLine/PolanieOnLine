
package games.stendhal.server.maps.quests.houses;

import marauroa.common.game.SlotIsFullException;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.mapstuff.portal.HousePortal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

final class BuyHouseChatAction extends HouseChatAction implements ChatAction {


	private int cost;

	/**
	 * Creates a new BuyHouseChatAction.
	 * 
	 * @param cost how much does the house cost
	 * @param questSlot name of quest slot
	 */
	BuyHouseChatAction(final int cost, final String questSlot) {
		super(questSlot);
		this.cost = cost;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

		final int number = sentence.getNumeral().getAmount();
		// now check if the house they said is free
		final String itemName = Integer.toString(number);

		final HousePortal houseportal = HouseUtilities.getHousePortal(number);

		if (houseportal == null) {
			// something bad happened
			raiser.say("Przepraszam, ale nie rozumiem Ciebie. Czy mógłbyś powtórzyć numer domu?");
			raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
			return;
		}

		final String owner = houseportal.getOwner();
		if (owner.length() == 0) {
			
			// it's available, so take money
			if (player.isEquipped("money", cost)) {
				final Item key = SingletonRepository.getEntityManager().getItem(
																				"klucz do drzwi");

				final String doorId = houseportal.getDoorId();

				final int locknumber = houseportal.getLockNumber();
				((HouseKey) key).setup(doorId, locknumber, player.getName());
			
				if (player.equipToInventoryOnly(key)) {
					raiser.say("Gratulacje, a oto i klucz do " + doorId
							   + ". Upewnij się, że zmieniłeś zamki o ile zgubiłeś klucze. Czy chcesz kupić zapasowy klucz w cenie  "
							   + HouseChatAction.COST_OF_SPARE_KEY + " money?");
					
					player.drop("money", cost);
					// remember what house they own
					player.setQuest(questslot, itemName);

					// put nice things and a helpful note in the chest
					BuyHouseChatAction.fillChest(HouseUtilities.findChest(houseportal), houseportal.getDoorId());

					// set the time so that the taxman can start harassing the player
					final long time = System.currentTimeMillis();
					houseportal.setExpireTime(time);

					houseportal.setOwner(player.getName());
					raiser.setCurrentState(ConversationStates.QUESTION_1);
				} else {
					raiser.say("Przepraszam, ale nie możesz wziąć więcej kluczy!");
				}
			
			} else {
				raiser.say("Nie masz wystarczająco dużo pieniędzy, aby kupić dom");
			}
		
		} else {
			raiser.say("Przepraszam dom " + itemName
					   + " został już sprzedany. Poproś o listę #niesprzedanych domów lub podaj numer innego domu.");
			raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
		}
	}

	private static void fillChest(final StoredChest chest, String id) {
		Item item = SingletonRepository.getEntityManager().getItem("karteczka");
		item.setDescription("WITAM WŁAŚCICIELA DOMU\n"
				+ "1. Jeżeli nie zapłacisz podatku za dom to wszystkie przedmioty znajdujące się w skrzyni będą skonfiskowane.\n"
				+ "2. Wszystkie osoby, które dostaną się do domu mogą korzystać ze skrzyni.\n"
				+ "3. Pamiętaj, aby zmienić zamki jako zabezpieczenie twojego domu.\n"
				+ "4. Możesz odsprzedać dom o ile chcesz (nie zostawiaj mnie)\n");
		try {
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("napój z winogron");
			((StackableItem) item).setQuantity(2);
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("tabliczka czekolady");
			((StackableItem) item).setQuantity(2);
			chest.add(item);
		} catch (SlotIsFullException e) {
			Logger.getLogger(BuyHouseChatAction.class).info("Could not add " + item.getName() + " to chest in " + id, e);
		}
	}
}
