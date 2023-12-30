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

public class KillAndBringQuestBuilder extends QuestBuilder<KillAndBringTask, SimpleQuestOfferBuilder, SimpleQuestCompleteBuilder, QuestHistoryBuilder> {
	public KillAndBringQuestBuilder() {
		super(new KillAndBringTask());
		offer = new SimpleQuestOfferBuilder();
		complete = new SimpleQuestCompleteBuilder();
		history = new QuestHistoryBuilder();
	}
}
