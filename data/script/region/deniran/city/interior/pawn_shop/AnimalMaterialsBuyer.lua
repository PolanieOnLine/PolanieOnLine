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


local buyer = nil
local buyerName = "Harley"
local shopName = "buyanimalmaterials"


local function initNPC()
	buyer = entities:createSpeakerNPC(buyerName)
	buyer:setOutfit("body=0,head=8,hair=11,dress=53")
	buyer:setPosition(17, 21)
	buyer:setDirection(Direction.UP)
	buyer:setDescription("Oto " .. buyerName .. ". Jest zatrudniony w lombardzie.")

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
	merchants:addBuyer(buyer, merchants.shops:get(shopName), false)

	-- shop sign
	local sign = entities:createShopSign(shopName, "Handel Materiałami Zwierzęcymi", buyerName .. " skupuje następujące przedmioty", false)
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
