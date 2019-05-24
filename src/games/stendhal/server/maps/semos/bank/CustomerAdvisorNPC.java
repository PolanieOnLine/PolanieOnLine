/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.bank;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Map;
/**
 * ZoneConfigurator configuring the NPC (former known as Dagobert) in semos bank
 */
public class CustomerAdvisorNPC implements ZoneConfigurator {

	private static final class VaultChatAction implements ChatAction {
		
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final StendhalRPZone vaultzone = (StendhalRPZone) SingletonRepository
					.getRPWorld().getRPZone("int_vault");
			String zoneName = player.getName() + "_vault";
			
			final StendhalRPZone zone = new Vault(zoneName, vaultzone, player);
			
			
			SingletonRepository.getRPWorld().addRPZone(zone);
			player.teleport(zone, 4, 5, Direction.UP, player);
			((SpeakerNPC) npc.getEntity()).setDirection(Direction.DOWN);
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Dagobert") {

	@Override
	public void createDialog() {
		addGreeting("Witam w banku w Semos! Jeśli potrzebujesz #pomocy w sprawie skrzyń powiedz mi o tym.");
		addHelp("Pójdź korytarzem po prawej, aż zobaczysz zaklęte skrzynie. Możesz w którejkolwiek z nich schować swoje rzeczy, a nikt Ci ich nie zabierze! Wiele zaklęć rzucono na nie aby były #bezpieczne, czy chcesz usłyszeć więcej o bezpieczeństwie?");
		addReply(Arrays.asList("safety", "bezpieczne"), "Gdy staniesz przy skrzyni, by poukładać w niej swe rzeczy nikt inny, ani nawet zwierzę nie może się do Ciebie zbliżyć. Magiczna aura zatrzyma każdego, kto chciałby zbliżyć się do Ciebie za pomocą magicznych biletów, ale to oznacza, że i ty nie użyjesz takiego, by wyjść z banku. Możesz odejść jedynie na piechotę. Może chcesz wiedzieć jak działa tu bezpieczny #handel?");
		addReply(Arrays.asList("trading", "handel"), "W najdalszym kącie tego banku po prawej jest wielki stół. Rzucono nań zaklęcie, aby każda transakcja odbyła się uczciwie. Oto jak z niego korzystać: Handlujący zajmują miejsca po przeciwnych stronach stołu. Kiedy już ustalicie przedmiot wymiany połóż maksymalnie po trzy rzeczy obok siebie na stole tuż przed sobą. Poczekaj aż drugi handlarz uczyni to samo. Upewnij się, że położył dokładnie to co ustaliliście i w odpowiedniej ilości. Wtedy możliwa jest #wymiana.");
		addJob("Jestem doradcą klientów i przedstawicielem Banku w Semos.");
		addOffer("Jeżeli chciałbyś przeglądnąć swoją skrzynię w odosobnieniu to mogę dać Tobie dostęp do prywatnego #skarbca. Przewodnik w środku wyjaśni Ci jak to działa.");		
		addGoodbye("Przyjemnie mi się z tobą pracowało!");
				add(ConversationStates.ANY, Arrays.asList("vault", "skarbiec", "skarbca"), new QuestCompletedCondition("armor_dagobert"), ConversationStates.IDLE, null, 
						new MultipleActions(new PlaySoundAction("keys-1", true), new VaultChatAction()));
		
		add(ConversationStates.ANY, Arrays.asList("vault", "skarbiec", "skarbca"), new QuestNotCompletedCondition("armor_dagobert"), ConversationStates.ATTENDING, "Może wyświadczyłbyś mi #przysługę, a wtedy powiem więcej o prywatnych skarbcach bankowych.", null);
		
		// remaining behaviour defined in games.stendhal.server.maps.quests.ArmorForDagobert	
	}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
			
		};
		npc.setPosition(9, 23);
		npc.setDirection(Direction.DOWN);
		npc.setDescription("Oto Dagobert. Wygląda na osobę budzącą zaufanie i niezawodną.");
		npc.setHP(95);
		npc.setEntityClass("youngnpc");
		zone.add(npc);
	}
	
}