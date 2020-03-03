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


local grocer = nil

local function addNPC()
	grocer = npcHelper:createSpeakerNPC("Jimbo")
	grocer:setOutfit("dress=5,head=2,mask=1")
	grocer:setOutfitColor("skin", SkinColor.DARK)
	grocer:setOutfitColor("dress", 0x8b4513) -- saddle brown
	grocer:setOutfitColor("head", 0x228b22) -- forest green
	grocer:setOutfitColor("mask", 0xffffff) -- white (doesn't work with glasses because they are fully black)
	grocer:setPosition(25, 24)
	grocer:setIdleDirection(Direction.UP)

	grocer:addGreeting()
	grocer:addGoodbye()
	grocer:addJob("Niedawno otwarłem sklep z różnymi produktami w mieście Deniran.")
	grocer:addOffer("Proszę spójrz na tablicę, by sprawdzić przedmioty jakie mam na sprzedaż oraz ich cenę.")
	grocer:addHelp(grocer:getReply("offer"))
	grocer:addQuest("Wybacz, ale nie potrzebuję niczego w czym mógłbyś mi pomóc na tę chwilę.")

	local sellPrices = {
		{"oliwa z oliwek", 135},
		{"ocet", 135},
	}
	npcHelper:addSeller(grocer, sellPrices, false)

	-- add to shop list
	for _, item in pairs(sellPrices) do
		shops:add("denirangrocerysell", item[1], item[2])
	end

	game:add(grocer)
end

local function addSign()
	if grocer ~= nil then
		local sellSign = game:createShopSign("denirangrocerysell", "Sklep " .. grocer:getName() .. " (sprzedaje)", "Możesz kupić te rzeczy.")
		sellSign:setEntityClass("blackboard")
		sellSign:setPosition(23, 23)

		game:add(sellSign)
	end
end


local zone = "int_deniran_grocery_store"

if game:setZone(zone) then
	addNPC()
	addSign()
else
	logger:error("Could not set zone: " .. zoneName)
end