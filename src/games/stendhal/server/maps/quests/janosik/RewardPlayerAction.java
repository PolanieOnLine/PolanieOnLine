package games.stendhal.server.maps.quests.janosik;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 *  NPC's actions when player asks for his reward.
 */
public class RewardPlayerAction implements ChatAction, IRAQuestConstants {
	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser mayor) {
		final int quantity = RAQuestHelperFunctions.calculateReward(player);
		// to avoid giving karma without job
		if(quantity==0) {
			mayor.say("Nie przegoniłeś ani jednego zbója, który zaatakował miasto więc nie zasługujesz na nagrodę.");
			return;
		}
		player.addKarma(5);
		final StackableItem moneys = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
	   	moneys.setQuantity(quantity);
	   	player.equipOrPutOnGround(moneys);
	   	mayor.say("Proszę przyjmij "+quantity+" money jako podziękowanie za twoją nieocenioną pomoc.");
	   	player.setQuest(QUEST_SLOT, "done");
	}
}
