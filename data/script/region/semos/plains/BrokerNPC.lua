--[[
 ***************************************************************************
 *                    Copyright © 2022-2023 - Arianne                      *
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


-- an accessible NPC for low levels that purchases misc. items for a lower price

local zone_name = "0_semos_plains_n"

if game:setZone(zone_name) then
	local broker = entities:createSpeakerNPC("Emeric")
	broker:setOutfit("body=0,head=0,eyes=0,hair=5,dress=2")
	broker:setOutfitColor("eyes", 0x468499)
	broker:setOutfitColor("dress", 0x065535)
	broker:setPosition(60, 92)
	broker:setIdleDirection(Direction.RIGHT)

	broker:addGreeting();
	broker:addGoodbye();
	broker:addJob("Jestem pośrednikiem, który kupuje różne przedmioty i sprzedaje do sklepów w Faimouni.")
	broker:addQuest("W niczym nie potrzebuję pomocy poza zbieraniem materiałów, " ..
		"aby zarabiać na sklepach w Faimouni.")
	broker:addHelp("Kupię od ciebie pewne rzeczy po bardzo rozsądnej cenie... dla mnie.")

	game:add(broker)
else
	logger:error("could not set zone: " .. zone_name)
end
