--[[
 ***************************************************************************
 *                       Copyright © 2021 - Arianne                        *
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


-- some non-interactive NPCs to wander around Deniran

local zones = {
	["0_deniran_city"] = {
		{
			image = "youngnpc",
			path = {
				nodes = {
					{17,33},  {32,33},  {32,56},  {16,56},  {16,52},  {16,56},
					{32,56},  {32,75},  {38,75},  {38,76},  {44,76},  {44,77},
					{93,77},  {93,75}, {103,75}, {103,65},  {96,65},  {96,60},
				},
				retrace = true,
				speed = 0.4,
				on_collide = CollisionAction.STOP,
			},
		},
	},
	["0_deniran_city_e"] = {
		{
			image = "womanonstoolnpc",
			desc = "Oto kobieta siedząca na ławce.",
			pos = {x=53, y=55},
			facedir = Direction.DOWN,
		},
	},
	["0_deniran_city_e2"] = {
		{
			outfit = {
				layers = "body=0,head=0,eyes=6,hair=46,dress=53,mask=1,hat=7",
			},
			path = {
				nodes = {
					{73,47}, {73,34}, {47,34}, {47,47},
				},
			},
		},
	},
	["0_deniran_city_s"] = {},
	["0_deniran_city_s_e2"] = {},
	["0_deniran_city_se"] = {},
	["0_deniran_city_sw"] = {},
	["0_deniran_city_w"] = {},
}

for zone_name, entities_table in pairs(zones) do
	if not game:setZone(zone_name) then
		logger:error("could not set zone: " .. zone_name)
	else
		for _, data in ipairs(entities_table) do
			local citizen = entities:createSilentNPC()

			if data.desc == nil then
				data.desc = "Oto tubylec miasta Deniran."
			end
			citizen:setDescription(data.desc)

			if type(data.outfit) == "table" and data.outfit.layers ~= nil then
				citizen:setOutfit(data.outfit.layers)
				if type(data.outfit.colors) == "table" then
					for k, v in pairs(data.outfit.colors) do
						citizen:setOutfitColor(k, v)
					end
				end
			elseif data.image ~= nil then
				citizen:setEntityClass(data.image)
			end

			if type(data.path) == "table" and type(data.path.nodes) == "table" then
				citizen:setPathAndPosition(data.path.nodes, true)
				if data.path.retrace then
					citizen:setRetracePath()
				end
				if data.path.speed ~= nil then
					citizen:setBaseSpeed(data.path.speed)
				end

				citizen:setCollisionAction(data.path.on_collide or CollisionAction.REVERSE)
			elseif type(data.pos) == "table" then
				citizen:setPosition(data.pos.x, data.pos.y)
			end

			if data.facedir ~= nil then
				citizen:setDirection(data.facedir)
			end

			game:add(citizen)
		end
	end
end