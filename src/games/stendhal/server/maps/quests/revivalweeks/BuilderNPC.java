/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.dbcommand.ReadGroupQuestCommand;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.CollectingGroupQuestAdder;
import games.stendhal.server.entity.npc.behaviour.impl.CollectingGroupQuestBehaviour;
import games.stendhal.server.util.QuestUtils;
import marauroa.server.db.command.DBCommandQueue;

public class BuilderNPC implements LoadableContent, TurnListener {
	private SpeakerNPC npc = null;
	private static final String QUEST_SLOT = QuestUtils.evaluateQuestSlotName("minetown_construction_[year]");
	private ReadGroupQuestCommand command;
	private CollectingGroupQuestBehaviour behaviour;

	@Override
	public void addToWorld() {
		this.command = new ReadGroupQuestCommand(QUEST_SLOT);
		DBCommandQueue.get().enqueue(command);
		TurnNotifier.get().notifyInTurns(0, this);
	}
			
	@Override
	public void onTurnReached(int currentTurn) {
		if (command.getProgress() == null) {
			TurnNotifier.get().notifyInTurns(0, this);
			return;
		}
		Map<String, Integer> progress = command.getProgress();
		setupCollectingGroupQuest(progress);
		addNPC();
	}

	private void setupCollectingGroupQuest(Map<String, Integer> progress) {
		Map<String, Integer> required = new LinkedHashMap<>();
		Map<String, Integer> chunkSize = new HashMap<>();
		Map<String, String> hints = new HashMap<>();

		required.put("pordzewiała kosa", 1);
		chunkSize.put("pordzewiała kosa", 1);
		hints.put("pordzewiała kosa", "Jestem pewien, że Xoderos w Semos sprzeda ci starą kosę.");
		
		required.put("topór", 1);
		chunkSize.put("topór", 1);
		hints.put("topór", "Jestem pewien, że Xoderos w Semos sprzeda ci topór.");
		
		required.put("pyrlik", 1);
		chunkSize.put("pyrlik", 1);
		hints.put("pyrlik", "Jestem pewien, że Xoderos w Semos sprzeda ci pyrlik.");
		
		required.put("nożyk", 2);
		chunkSize.put("nożyk", 1);
		hints.put("nożyk", "Jestem pewien, że Xin Blanca w Semos sprzeda ci nożyk.");
		
		required.put("latarenka", 5);
		chunkSize.put("latarenka", 1);
		hints.put("latarenka", "Prawdopodobnie latarenkę możesz kupić od Jimbo w Deniran.");

		required.put("polano", 200);
		chunkSize.put("polano", 10);
		hints.put("polano", "Drewno możesz znaleźć w różnych lasach.");

		required.put("sok z chmielu", 25);
		chunkSize.put("sok z chmielu", 5);
		hints.put("sok z chmielu", "Prawdopobodnie sok z chmielu znajdziesz w każdej tawernie.");

		behaviour = new CollectingGroupQuestBehaviour(QUEST_SLOT, required, chunkSize, hints, progress);
		behaviour.setProjectName("#Mine #Town #Revival #Weeks");
	}

	private void addNPC() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_semos_mountain_n2");
		final SpeakerNPC npc = new SpeakerNPC("Klaus") {

			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Proszę, bądź ostrożny, budujemy #Mine #Town #Revival #Weeks.");
				addHelp("#Mine #Town #Revival #Weeks to coroczny festiwal.");
				addQuest("Brakuje nam zapasów i możemy nie być w stanie ukończyć budowy na czas! Co za katastrofa.");
				addJob("Jestem kierownikiem budowy odpowiedzialnym za budowę #Mine #Town #Revival #Weeks.");
				addGoodbye("Pa, wróć wkrótce.");

			}

		};

		npc.setOutfit("body=0,dress=33,head=0,mouth=0,eyes=18,mask=0,hair=27,hat=1");
		npc.setPosition(70, 118);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		npc.setDescription("Oto Klaus. Odpowiada za budowę festiwalu.");
		zone.add(npc);
		
		addQuestDialog(npc);
	}

	

	private void addQuestDialog(SpeakerNPC npc) {
		new CollectingGroupQuestAdder().add(npc, behaviour);
		npc.addReply(Arrays.asList("Mine", "Town", "Revival", "Weeks", "Mine Town",
				"Mine Town Revival", "Mine Town Revival Weeks", "Mine Town", "Revival Weeks"),
				"Podczas Tygodni Odrodzenia #świętujemy stare i w większości martwe Miasto Kopalni na północ od miasta Semos. To tradycja od wielu lat, ale w tym roku #status budowy nie wygląda dobrze.",
				null);
		npc.addReply(Arrays.asList("project", "projekt"),
				"Jestem odpowiedzialny za rozbudowę #Mine #Town #Revival #Weeks. Muszę wszystko przygotować, żeby festiwal się odbył.",
				null);

		npc.addReply(Arrays.asList("celebrate", "świętujemy"),
				"Będą gry i dużo do jedzenia i picia. Słyszałem nawet o pogoni za papierkiem.",
				null);
	}

	@Override
	public boolean removeFromWorld() {
		if (npc != null) {
			npc.getZone().remove(npc);
		}
		return true;
	}

}