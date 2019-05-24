package games.stendhal.server.maps.semos.city;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.config.annotations.TestServerOnly;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.Spell;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import marauroa.common.game.RPSlot;

/**
 * An NPC for testing purposes, that easily enables a player to play around with magic
 * 
 * @author madmetzger
 */
@TestServerOnly
public class MagicTeacherNPC implements ZoneConfigurator {
	
	/**
	 * This ChatAction prepares a player with all he needs to play around with magic
	 * 
	 * @author madmetzger
	 */
	private final class TeachMagicAction implements ChatAction {
		
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			enableSpellsFeature(player);
			boostMana(player);
			equipSpells(player);
			equipManaPotions(player);
		}
		
		private void equipManaPotions(Player player) {
			StackableItem potion = (StackableItem) SingletonRepository.getEntityManager().getItem("mana");
			potion.setQuantity(1000);
			player.equipOrPutOnGround(potion);
		}

		private void equipSpells(Player player) {
			EntityManager em = SingletonRepository.getEntityManager();
			RPSlot slot = player.getSlot("spells");
			Collection<String> spells = em.getConfiguredSpells();
			for (String spellName : spells) {
				Spell s = em.getSpell(spellName);
				slot.add(s);
			}
		}

		private void boostMana(Player player) {
			player.setBaseMana(1000);
			player.setMana(1000);
		}

		private void enableSpellsFeature(Player player) {
			player.setFeature("spells", true);
		}
		
	}

	@Override
	public void configureZone(StendhalRPZone zone,
		Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Mirlen") {

			@Override
			protected void createDialog() {
				add(ConversationStates.ATTENDING, Arrays.asList("teach", "naucz"), null, ConversationStates.SERVICE_OFFERED, "Czy chcesz się nauczyć czegoś o magii?", null);
				add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING, null, new TeachMagicAction());
			}
			
		};
		npc.addGreeting("Witaj jestem nauczycielem magii! Mogę cię #nauczyć magii.");
		npc.addJob("Moją pracą jest #uczenie ciebie o magii, którą możesz tu wypróbować.");
		npc.addHelp("Jeżeli będziesz potrzebował pomocy to zajrzyj na help #https://polskagra.net.");
		npc.addOffer("Oferuję #naukę magii. Powiedz tylko #naucz.");
		npc.addGoodbye("Niech magia będzie z tobą!");
		npc.setPosition(20, 26);
		npc.setEntityClass("blueoldwizardnpc");
		zone.add(npc);
	}

}
