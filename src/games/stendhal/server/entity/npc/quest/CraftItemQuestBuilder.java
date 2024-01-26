/***************************************************************************
 *                 (C) Copyright 2023-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

public class CraftItemQuestBuilder extends QuestBuilder<CraftItemTask, CraftItemQuestOfferBuilder, CraftItemQuestCompleteBuilder, CraftItemQuestHistoryBuilder> {
	public CraftItemQuestBuilder() {
		super(new CraftItemTask());
		offer = new CraftItemQuestOfferBuilder();
		complete = new CraftItemQuestCompleteBuilder();
		history = new CraftItemQuestHistoryBuilder();
	}
}
