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


local zone = "int_tarnow_weaponshop"

local ringsmith = nil
local questSlot = "goslawa_forge_ring"
local waitTime = MathHelper.MINUTES_IN_ONE_HOUR * 6
local ring = "wzmocniony pierścień zdrowia"

local fee = 480000

local requirements = {
	{"pierścień zdrowia", 1},
	{"róg jednorożca", 25},
	{"róg demona", 25},
	{"bryłka złota", 50},
	{"duży smoczy eliksir", 75},
	{"money", fee}, -- fee needs to be the last item in this list
}

local function getItemListString(includeFee)
	sb = string.builder()

	local reqCount = #requirements
	if not includeFee then
		reqCount = reqCount - 1
	end

	for idx = 1, reqCount do
		if idx > 1 then
			sb:append(", ")

			if idx == reqCount then
				sb:append("oraz ")
			end
		end

		local itemName = requirements[idx][1]
		local quant = requirements[idx][2]
		itemName = grammar:plnoun(quant, itemName)
		if itemName ~= "money" then
			itemName = "#'" .. itemName .. "'"
		end

		sb:append(tostring(quant) .. " " .. itemName)
	end

	return sb:toString()
end

if game:setZone(zone) then
	ringsmith = entities:createSpeakerNPC("Gosława")
	ringsmith:setOutfit("body=1,head=0,eyes=2,dress=52,hair=45,hat=6")
	ringsmith:setOutfitColor("eyes", 0x1f6521)
	ringsmith:setOutfitColor("hair", Color.RED)

	-- path
	local nodes = {
		{10, 5},
		{13, 5},
	}
	ringsmith:setPathAndPosition(nodes, true)

	-- dialogue
	ringsmith:addGreeting()
	ringsmith:addGoodbye()
	ringsmith:addQuest("Nie mam dla ciebie zadania w tym momencie.")
	ringsmith:addJob("Mogę dla ciebie #wzmocnić specjalny przedmiot.")
	local helpReply = "Mogę #wzmocnić użyteczny #pierścień jeśli przyniesiesz parę materiałów."
	ringsmith:addHelp(helpReply)
	ringsmith:addOffer(helpReply)

	local hasItemsCondition = {}
	local startAction = {
		actions:create("SetQuestToTimeStampAction", {questSlot}),
		actions:create("SayTimeRemainingAction", {questSlot, waitTime, "Okej, Biorę się za pracę. Proszę wróć za ",
			"Przypomnij mi mówiąc #pierścień."}),
		actions:create("PlaySoundAction", {"coins-1"}),
	}

	for _, item in ipairs(requirements) do
		table.insert(hasItemsCondition, conditions:create("PlayerHasItemWithHimCondition", {item[1], item[2], item[3], item[4]}))
		table.insert(startAction, actions:create("DropItemAction", {item[1], item[2], item[3], item[4]}))
	end

	local rewardAction = {
		actions:create("EquipItemAction", {ring}),
		actions:clearQuest(questSlot),
		actions:create("IncreaseItemExchangeAction", {"produce", ring})
	}

	local forgePhrases = {"forge", "ring", "wzmocnienie", "wzmocnić", "pierścień"}

	ringsmith:add(ConversationStates.ATTENDING,
		forgePhrases,
		{
			conditions:create("QuestNotActiveCondition", {questSlot}),
			conditions:notCondition(hasItemsCondition),
		},
		ConversationStates.ATTENDING,
		"Jeśli przynieś mi " .. getItemListString(true) .. " to będę mogła zrobić dla ciebie coś specjalnego.",
		nil)

	ringsmith:add(ConversationStates.ATTENDING,
		forgePhrases,
		{
			conditions:create("QuestNotActiveCondition", {questSlot}),
			hasItemsCondition,
		},
		ConversationStates.QUESTION_1,
		"Widzę, że już masz " .. getItemListString(false) .. ". Chcesz abym wzmocniła dla ciebie specjalny pierścień?" ..
			" Koszt będzie wynosić " .. tostring(fee) .. " money.",
		nil)

	ringsmith:add(ConversationStates.QUESTION_1,
		ConversationPhrases.NO_MESSAGES,
		nil,
		ConversationStates.ATTENDING,
		"Okej.",
		nil)

	ringsmith:add(ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		conditions:notCondition(hasItemsCondition),
		ConversationStates.ATTENDING,
		"Chyba coś zgubiłeś po drodze.",
		nil)

	ringsmith:add(ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		hasItemsCondition,
		ConversationStates.IDLE,
		nil,
		startAction)

	ringsmith:add(ConversationStates.ATTENDING,
		forgePhrases,
		{
			conditions:create("QuestActiveCondition", {questSlot}),
			conditions:notCondition(conditions:create("TimePassedCondition", {questSlot, waitTime})),
		},
		ConversationStates.ATTENDING,
		nil,
		actions:create("SayTimeRemainingAction", {questSlot, waitTime, "Twój pierścień nie jest jeszcze gotowy. Proszę, wróć za"}))

	ringsmith:add(ConversationStates.ATTENDING,
		forgePhrases,
		{
			conditions:create("QuestActiveCondition", {questSlot}),
			conditions:create("TimePassedCondition", {questSlot, waitTime}),
		},
		ConversationStates.ATTENDING,
		"Dobrze, że przypominasz. Oto twój nowy " .. ring .. "!",
		rewardAction)

	game:add(ringsmith)
else
	logger:error("Could not set zone: " .. zone)
end
