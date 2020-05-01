--[[
 ***************************************************************************
 *                       Copyright © 2020 - Arianne                        *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************
]]


local zoneName = "-7_deniran_atlantis"

if game:setZone(zoneName) then
	local zelan = entities:createSpeakerNPC("Zelan")

	-- NPC appearance & behavior
	zelan:setEntityClass("atlantismale01npc")
	zelan:setCollisionAction(CollisionAction.STOP)

	-- NPC location & path
	local nodes = {
		{63, 66},
		{75, 66},
	}
	zelan:setPathAndPosition(nodes, true)

	-- NPC dialog
	zelan:addGreeting()
	zelan:addGoodbye()

	-- add Zelan to the world
	game:add(zelan)


	-- quest
	local quest = quests.simple:create("unicorn_horns_for_zelan", "Rogi jednorożca dla Zelana", "Zelan")
	quest:setDescription("Zelan potrzebuje pomocy przy zbieraniu rogów jednorożca.")

	quest:setReply(quests.simple.ID_REQUEST,
		"Cześć! Potrzebuję rogów jednorożca, żeby zrobić kilka sztyletów."
		.. " Jest to naprawdę niebezpieczne w lasach otaczających Atlantydę. Jeśli jesteś odważny,"
		.. " przydałaby mi się pomoc w zbieraniu rogów jednorożca. Pomożesz mi?")
	quest:setReply(quests.simple.ID_ACCEPT,
		"Świetnie! Uważaj, tam jest dużo ogromnych stworów, a te centaury są naprawdę okropne.")
	quest:setReply(quests.simple.ID_REWARD, "Wielkie dzięki!")
	quest:setReply(quests.simple.ID_REJECT, "W porządku, znajdę kogoś, kto mi pomoże.")

	quest:setItemToCollect("róg jednorożca", 10)
	quest:setXPReward(50000)
	quest:setKarmaReward(5.0)
	quest:addItemReward("zupa", 3)
	quest:addItemReward("money", 20000)
	quest:setRegion(Region.ATLANTIS)

	quests:register(quest)
else
	logger:error("Could not set zone: " .. zoneName)
end
