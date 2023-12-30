--[[
 ***************************************************************************
 *                    Copyright © 2020-2023 - Arianne                      *
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


local brokerName = "Sawyer"
local broker = nil

local function initNPC()
	broker = entities:create({
		type = "SpeakerNPC",
		name = brokerName,
		description = "Oto " .. brokerName ..". Wygląda na właściciela lombardu, który aktualnie prowadzi.",
		outfit = {
			layers = "body=0,head=0,eyes=5,hair=14,dress=5,hat=6",
			colors = {
				body = SkinColor.DARK,
				eyes = 0x0000ff
			}
		},
		pos = {18, 5},
		idleDir = Direction.LEFT
	})

	-- dialogue
	broker:addGreeting("Witamy w sklepie lombardowym w Deniran.")
	broker:addGoodbye()
	broker:addHelp("Jeśli chcesz coś zastawić, sprawdź moją tablicę, by zobaczyć listę tego, co skupuję.")
	broker:addJob("Jestem właścicielem tego lombardu. Jeśli chcesz coś sprzedać, sprawdź moją tablicę, by sprawdzić listę tego, co skupuję.")
	broker:addQuest("Cóż... Nie mam teraz nic, w czym mógłbyś mi pomóc.")
	broker:addOffer("Sprawdź na tablicy listę przedmiotów, które skupuję.")

	broker:add(
		ConversationStates.ATTENDING,
		{ "golden blade", "złota klinga" },
		nil,
		ConversationStates.ATTENDING,
		nil,
		function(player, sentence, raiser)
			broker:say("To nie przypadek kolego. Ta rzecz nie opuszcza mojego wzroku.")
			broker:say("!me surowo patrzy na ciebie w górę i w dół.")
		end)

	game:add(broker)
end

local function initShop()
	-- shop sign
	local sign = entities:create({
		type = "ShopSign",
		name = "deniranpawnbuy",
		title = "Lombard Deniran",
		caption = brokerName .. " skupuje następujące przedmioty",
		seller = false
	})
	sign:setEntityClass("blackboard")
	sign:setPosition(18, 8)

	game:add(sign);
end


local zone = "int_deniran_pawn_shop"

if game:setZone(zone) then
	initNPC()
	if broker ~= nil then
		initShop()
	end
else
	logger:error("Could not set zone: " .. zone)
end
