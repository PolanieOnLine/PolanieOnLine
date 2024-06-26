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


local zone_name = "0_fado_city"

if game:setZone(zone_name) then
	local w_name = "Emma"
	local h_name = "Ari"

	local wifeToBe = entities:create({
		type = "SpeakerNPC",
		name = w_name,
		description = "Oto " .. w_name .. ". " .. w_name .. ", która wpatruje się w " .. h_name .. ".",
		outfit = {
			layers = "body=1,head=0,eyes=1,hair=23,dress=17",
			colors = {eyes=0xdc143c, hair=0x8a2be2, dress=0x008080},
		},
		idea = "love",
		pos = {75, 56},
		idleDir = Direction.LEFT,
	})

	local husbandToBe = entities:create({
		type = "SpeakerNPC",
		name = h_name,
		description = "Oto " .. h_name .. ". " .. h_name .. ", który wpatruje się w " .. w_name .. ".",
		outfit = {
			layers = "body=0,head=0,eyes=21,hair=49,dress=53",
			colors = {eyes=0x89cff0, hair=0x0d98ba},
		},
		idea = "love",
		pos = {74, 56},
		idleDirection = Direction.RIGHT,
	})

	wifeToBe:setIgnorePlayers(true)
	wifeToBe.attackRejectedAction = function(self, attacker)
		if attacker ~= nil then
			husbandToBe:say(attacker:getName() .. ", proszę zostaw " .. self:getName() .. " w spokoju.")
		else
			husbandToBe:say("Proszę... zostaw " .. self:getName() .. " w spokoju.")
		end
	end

	-- does not respond to players if quest is not loaded
	husbandToBe:setIgnorePlayers(true)
	husbandToBe.idleAction = function(self)
		self:setIdea("love")
	end

	local treeCarving = entities:create({
		type = "Sign",
		visible = false;
		pos = {76, 54},
		text = "Czytasz: \"" .. h_name .. " ❤ " .. w_name .. "\".",
		description = "W drzewie zostało coś jest wyryte.",
	})

	game:add(wifeToBe)
	game:add(husbandToBe)
	game:add(treeCarving)
else
	logger:error("could not set zone: " .. zone_name)
end
