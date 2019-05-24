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
package games.stendhal.server.maps.ados.bank;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds the Ados bank npc.
 *
 * @author Vanessa Julius
 */
public class BankNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Rachel") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witamy w Banku Ados!");
				addJob("Jestem doradcą klienta w Banku Ados.");
				addHelp("Nasze pokoje posiadają 4 #skrzynie naszego banku i 4 skrzynie partnerskiego #Semos. Są w pełni dostępne dla ciebie.");
				addReply(Arrays.asList("chests", "skrzynie"), "Możesz znaleść skrzynie w dwóch osobnych pokojach. Dwie skrzynie naszego banku są w tym pokoju po lewej, a 2 Semos są po prawej.");
				addReply("Semos", "Naszy główny parter jest w mieście Semos. Może już spotkałeś mojego szefa #Dagobert. Jest moim mentorem.");
				addReply("Dagobert", "Może wyjaśnić sporo rzeczy o systemie bankowym, ale może ja wyjaśnię #więcej tobie o ile chcesz.");
				addReply(Arrays.asList("more", "więcej"), "Odwiedź któryś z naszych dwóch pokoi, aby skorzystać z naszych magicznych skrzyń. Możesz przechowywać swoje przedmioty w każdym z nich i nikt po za tobą nie będzie miał do nich dostępu. Rzucono sporo czarów w miejscu skrzyń, aby zapewnić #bezpieczeństwo.");
				addReply(Arrays.asList("safety", "bezpieczeństwo"), "Gdy stoisz przy skrzyni, aby poukładać przedmioty to inne osoby lub zwierzęta nie będą mogły do ciebie podejść. Magiczna aura powstrzumuje inncyh od użycia zwoju umożliwiającego podejście blisko ciebie. Będziesz musiał odejść ,aby mogli zbliżyć się do skrzyni. Ostatecznie pozwól mi opowiedzieć bezpiecznym #handlu.");
				addReply(Arrays.asList("trading", "handlu"),"Aby rozpocząć handel z innym wojownikiem naciśnij na nim prawy przycisk i wybierz 'Handluj'. Jeśli chce z tobą pohandlować to pojawi się okienko gdzie będziesz mógł przeciągnąć przedmioty do zaoferowania i zobaczysz co jest tobie oferowane. Oboje naciśnijcie na Zaoferuj, a potem musicie obaj zaakceptować ofertę, aby sfinalizować transakcję.");
				addQuest("Przykro mi, ale w tej chwili nie mam dla ciebie pracy.");
 				addGoodbye("Dziękujemy za odwiedzenie naszego banku!");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};
		
		npc.setDescription("Oto Rachel, która wygląda na mądrą kobietę. Pracuje w banku Ados.");
		npc.setEntityClass("adosbankassistantnpc");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(9, 4);
		npc.initHP(100);
		zone.add(npc);
		
	}
}
