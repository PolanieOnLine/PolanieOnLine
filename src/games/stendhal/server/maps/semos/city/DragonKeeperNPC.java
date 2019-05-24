package games.stendhal.server.maps.semos.city;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DragonKeeperNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 1;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("The Dragon Keeper") {

			@Override
			protected void createDialog() {
				class DragonSellerBehaviour extends SellerBehaviour {
					DragonSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... Nie sądze, abyś mógł się zając więcej niż jednym smokie naraz.");
							return false;
						} else if (player.hasPet()) {
							say("Coż powinieneś poszukać zwierzątka, które już masz.");
							return false;
						} else {
							if (!player.drop("money", getCharge(res, player))) {
								seller.say("Nie masz wystarczająco dużo piniędzy.");
								return false;
							}
							seller.say("Daj tutaj smoka do treningu! Powinien walczyć u twojego boku i #rosnąć.");

							final BabyDragon baby_dragon = new BabyDragon(player);
							
							Entity sellerEntity = seller.getEntity();
							baby_dragon.setPosition(sellerEntity.getX(), sellerEntity.getY() + 1);

							player.setPet(baby_dragon);
							player.notifyWorldAboutChanges();

							return true;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("dragon", BUYING_PRICE);

				addGreeting();
				addJob("Walczę przeciw demonom ze smokiem u mojego boku. Znalazłby się jakiś dla ciebie.");
				addHelp("Sprzedaję smoki. Aby kupić wystarczy, że powiesz #kupię #dragon.");
				addGoodbye();
				addReply(Arrays.asList("grow", "rosnąć"),"Weź go na bitwę to zdobędzie doświadczenie i będzie rosnąć.");
				new SellerAdder().addSeller(this, new DragonSellerBehaviour(items));
			}
		};

		npc.setEntityClass("man_005_npc");
		npc.setPosition(17, 7);
		npc.initHP(85);
		npc.setDescription("The Dragon Keeper przyleciał do miasta na plecach latającego smoka.");
		zone.add(npc);

	}
}