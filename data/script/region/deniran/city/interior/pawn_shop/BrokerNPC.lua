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


local brokerName = "Sawyer"
local broker = nil
local shopName = "deniranpawnbuy"

local function initNPC()
	broker = entities:createSpeakerNPC(brokerName)
	broker:setOutfit("body=0,head=8,hair=14,dress=5,hat=6")
	broker:setOutfitColor("body", SkinColor.DARK)
	broker:setPosition(18, 5)
	broker:setIdleDirection(Direction.LEFT)
	broker:setDescription("Oto " .. brokerName ..". Jest właścicielem oraz prowadzi lombard.")

	-- dialogue
	broker:addGreeting("Witamy w sklepie lombardowym w Deniran.")
	broker:addGoodbye()
	broker:addHelp("Jeśli chcesz coś zastawić, sprawdź moją tablicę, by zobaczyć listę tego, co skupuję.")
	broker:addJob("Jestem właścicielem tego lombardu. Jeśli chcesz coś sprzedać, sprawdź moją tablicę, by sprawdzić listę tego, co skupuję.")
	broker:addQuest("Cóż... Nie mam teraz nic, w czym mógłbyś mi pomóc.")
	broker:addOffer("Sprawdź na tablicy listę przedmiotów, które skupuję.")

	game:add(broker)
end

local function initShop()
	merchants:addBuyer(broker, merchants.shops:get(shopName), false)

	-- shop sign
	local sign = entities:createShopSign(shopName, "Lombard w Deniran", brokerName .. " skupuje następujące przedmioty", false)
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
