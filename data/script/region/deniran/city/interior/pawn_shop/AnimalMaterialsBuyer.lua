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


local buyer = nil
local buyerName = "Harley"

local function initNPC()
	buyer = entities:create({
		type = "SpeakerNPC",
		name = buyerName,
		description = "Oto " .. buyerName .. ". Zatrudniony w lombardzie.",
		outfit = {
			layers = "body=0,head=0,eyes=0,hair=11,dress=53"
		},
		pos = {17, 21},
		dir = Direction.UP
	})

	-- dialogue
	buyer:addGreeting()
	buyer:addGoodbye()
	buyer:addOffer("Sprawdź na tablicy listę przedmiotów, które skupuję.")
	buyer:addHelp("Skupuję materiały pochodzenia zwierzęcego, a mój szef tam kupuje wszelkiego rodzaju pierścionki.")
	buyer:addQuest("Nie, dziękuję. Nie potrzebuję pomocy.")
	buyer:addJob("Pracuję tutaj w lombardzie, by spróbować zaoszczędzić na własny dom. Jeśli będę miał dom, to na pewno znajdę dziewczynę.")

	game:add(buyer)
end

local function initShop()
	-- shop sign
	local sign = entities:create({
		type = "ShopSign",
		name = "buyanimalmaterials",
		title = "Barter Materiałów Pochodzenia Zwierzęcego",
		caption = buyerName .. " skupuje następujące przedmioty",
		seller = false
	})
	sign:setEntityClass("blackboard")
	sign:setPosition(19, 20)

	game:add(sign);
end


local zone = "int_deniran_pawn_shop"
if game:setZone(zone) then
	initNPC()
	if buyer ~= nil then
		initShop()
	end
else
	logger:error("Could not set zone: " .. zone)
end
