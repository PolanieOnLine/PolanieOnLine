package games.stendhal.server.entity.npc.shop;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.NPCShopOfferEvent;

public final class ShopOfferSender {
	private ShopOfferSender() {}

	public static void openShopWindow(final SpeakerNPC npc, final Player player) {
		if ((npc == null) || (player == null)) {
			return;
		}

		final MerchantsRegister register = SingletonRepository.getMerchantsRegister();
		final SellerBehaviour seller = register.getSeller(npc.getName());
		final BuyerBehaviour buyer = register.getBuyer(npc.getName());
		final NPCShopOfferEvent event = NPCShopOfferEvent.open(npc, player, seller, buyer);

		if (event != null) {
			player.addEvent(event);
			player.notifyWorldAboutChanges();
		}
	}

	public static void closeShopWindow(final SpeakerNPC npc, final RPEntity entity) {
		if (!(entity instanceof Player)) {
			return;
		}

		final MerchantsRegister register = SingletonRepository.getMerchantsRegister();
		if (!register.isMerchant(npc.getName())) {
			return;
		}

		final Player player = (Player) entity;
		final NPCShopOfferEvent event = NPCShopOfferEvent.close(npc.getName());
		player.addEvent(event);
		player.notifyWorldAboutChanges();
	}
}
