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

-- Example SpeakerNPC

logger:info("Loading Lua SpeakerNPC...")

-- Set zone to Semos City
if game:setZone("0_semos_city") then

	-- Use helper object to create a new NPC
	local npc = npcHelper:createSpeakerNPC("Lua")
	npc:setEntityClass("littlegirlnpc")
	npc:setPosition(10, 55)
	npc:setBaseSpeed(0.1)
	local nodes = {
		{10, 55},
		{11, 55},
		{11, 56},
		{10, 56},
	}

	-- Use helper object to create NPC path
	npcHelper:setPath(npc, nodes)

	-- Dialogue
	npc:addJob("Właściwie jestem bezrobotny.")
	npc:add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, nil, ConversationStates.ATTENDING, "Jest mi smutno, bo nie mam pracy.", nil)
	npc:addGoodbye();
	npc:setCollisionAction(CollisionAction.STOP)

	-- Some custom replies using conditions & actions
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		newCondition("PlayerNextToCondition"),
		ConversationStates.ATTENDING,
		"Czy mógłbyś się trochę cofnąć? Czuję twój oddech.",
		newAction("NPCEmoteAction", "coughs", false)
	)
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		newCondition("NotCondition", newCondition("PlayerNextToCondition")),
		ConversationStates.ATTENDING,
		"To moje imię, nie nadużywaj go!",
		newAction("NPCEmoteAction", "giggles", false)
	)

	-- Add the NPC to the world
	game:add(npc)

	logger:info("Lua SpeakerNPC loaded!")
else
	logger:warn("Failed to load Lua SpeakerNPC")
end