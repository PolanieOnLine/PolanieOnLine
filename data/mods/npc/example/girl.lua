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
	local npc = entities:createSpeakerNPC("Lua")
	npc:setEntityClass("littlegirlnpc")
	npc:setPosition(10, 55)
	npc:setBaseSpeed(0.1)
	npc:setCollisionAction(CollisionAction.STOP)

	local nodes = {
		{10, 55},
		{11, 55},
		{11, 56},
		{10, 56},
	}

	-- Use helper object to create NPC path
	npc:setPath(nodes, true)

	-- Dialogue
	npc:addJob("Właściwie jestem bezrobotna.")
	npc:addGoodbye();

	-- Some custom replies using conditions & actions
	npc:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		nil,
		ConversationStates.ATTENDING,
		"Jest mi smutno, bo nie mam pracy.",
		nil
	)
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		conditions:create("PlayerNextToCondition"),
		ConversationStates.ATTENDING,
		"Czy mógłbyś się trochę cofnąć? Czuję twój oddech.",
		actions:create("NPCEmoteAction", {"coughs", false})
	)
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		conditions:notCondition(conditions:create("PlayerNextToCondition")),
		ConversationStates.ATTENDING,
		"To moje imię, nie nadużywaj go!",
		actions:create("NPCEmoteAction", {"giggles", false})
	)

	-- Set up a sign for Lua
	local sign = entities:createSign()
	sign:setEntityClass("signpost")
	sign:setPosition(12, 55)
	sign:setText("Poznaj Lua'e!")

	-- Add the entities to the world
	game:add(npc)
	game:add(sign)

	logger:info("Lua SpeakerNPC loaded!")
else
	logger:warn("Failed to load Lua SpeakerNPC")
end
