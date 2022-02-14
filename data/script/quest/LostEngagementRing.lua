--[[
 ***************************************************************************
 *                       Copyright © 2022 - Arianne                        *
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

--[[
	Stendhal quest: Lost pierścień zaręczynowy
	Steps:
	Reward:
	- karma
	- extended keyring (12 slots instead of 8)
	Notes:
	- If player loses ring, talk to Ari & say "lost" to reset quest slot.
]]

local quest_slot = "lost_engagement_ring"

local ari

local karmaAcceptReward = 15
local karmaCompleteReward = 50

local ring_infostring = "pierścionek Ariego"

-- location where ring may be on Athor island
local ring_locations = {
	{49, 30},
	{123, 3},
	{115, 122},
	{46, 106},
}

local questActiveCondition = conditions:create("QuestActiveCondition", {quest_slot})
local hasRingCondition = conditions:create("PlayerHasInfostringItemWithHimCondition", {
	"pierścień zaręczynowy",
	ring_infostring,
})

local hasKeyringCondition = function(player, sentence, npc)
	return player:hasFeature("keyring")
end

local check = {
	canStart = conditions:andC({
		conditions:create("QuestNotStartedCondition", {quest_slot}),
		conditions:create("QuestNotCompletedCondition", {quest_slot}),
		conditions:create(hasKeyringCondition),
	}),
	questActive = questActiveCondition,
	questCompleted = conditions:create("QuestCompletedCondition", {quest_slot}),
	canReward = hasRingCondition,
	cannotReward = conditions:notC(hasRingCondition),
}

local setQuestAction = function(player, sentence, npc)
	player:addKarma(karmaAcceptReward)

	-- choose random location
	local selected = random:randUniform(1, 4)

	player:setQuest(quest_slot, 0, ring_locations[selected][1])
	player:setQuest(quest_slot, 1, ring_locations[selected][2])
end

local resetQuestAction = function(player, sentence, npc)
	if player:hasQuest(quest_slot) then
		local slots = player:getQuest(quest_slot):split(";")
		if slots[1] == "have_ring" then
			-- FIXME: should have a failsafe here in case slot string is malformatted
			player:setQuest(quest_slot, slots[2] .. ";" .. slots[3])
		end
	end
end

local rewardAction = function(player, sentence, npc)
	player:addKarma(karmaCompleteReward)
	player:setFeature("keyring_ext", true)

	local slots = player:getQuest(quest_slot):split(";")
	if #slots > 2 then
		player:setQuest(quest_slot, 0, "done")
	elseif #slots > 1 then
		-- quest slot might have been reset if player reported ring "lost"
		player:setQuest(quest_slot, "done;" .. slots[1] .. ";" .. slots[2])
	else
		-- failsafe
		player:setQuest(quest_slot, "done")
	end
end

local prepareNPC = function()
	ari = entities:getNPC("Ari")
	ari:setIgnorePlayers(false)
	ari:addGoodbye()
end

local prepareRequestStep = function()

	ari:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		conditions:orC({
			{
				conditions:create("QuestNotStartedCondition", {quest_slot}),
				conditions:notC(hasKeyringCondition),
			},
			check.questCompleted,
		}),
		ConversationStates.ATTENDING,
		"Cześć!",
		nil)

	-- player does not have keyring
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		{
			conditions:create("QuestNotStartedCondition", {quest_slot}),
			conditions:notC(hasKeyringCondition),
		},
		ConversationStates.ATTENDING,
		"Myślę, że nie masz doświadczenia, aby mi pomóc. Może gdybyś"
			.. " wiedział więcej o rzemyku.",
		nil)

	-- player has keyring & can start quest
	ari:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		check.canStart,
		ConversationStates.ATTENDING,
		"Hej! Wyglądasz jak doświadczony wojownik. Może mógłbyś mi pomóc z #zadaniem.",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.canStart,
		ConversationStates.QUEST_OFFERED,
		"Straciłem mój pierścionek zaręczynowy i wstydzę się powiedzieć Emmie. Czy pomógłbyś mi?",
		nil)

	-- player has already completed quest
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.questCompleted,
		ConversationStates.ATTENDING,
		"Dziękuję, ale mam już wszystko, czego potrzebuję.",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.questActive,
		ConversationStates.ATTENDING,
		"Już pomagasz mi w poszukiwaniu mojego pierścionka zaręczynowego.",
		nil)

	ari:add(
		ConversationStates.QUEST_OFFERED,
		ConversationPhrases.NO_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Nie chcę opuszczać Emmy. Mam nadzieję, że znajdę kogoś, kto mi pomoże.",
		actions:create("DecreaseKarmaAction", {karmaAcceptReward}))

	-- player accepts quest
	ari:add(
		ConversationStates.QUEST_OFFERED,
		ConversationPhrases.YES_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Dziękuję bardzo! Zgubiłem pierścionek podczas wizyty na wyspie Athor."
			.. " Daj mi znać, kiedy go znajdziesz. I nic nie mów Emmie.",
		setQuestAction)
end

local prepareBringStep = function()
	ari:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		check.questActive,
		ConversationStates.QUESTION_1,
		"Znalazłeś mój pierścionek? Jeśli jest coś, co mogę zrobić, aby #pomóc, daj mi znać.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.HELP_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Mogłem go upuścić podczas spaceru po plaży. Jeśli tak jest,"
			.. " możesz potrzebować czegoś do kopania w piasku.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.NO_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Proszę szukaj dalej.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		check.cannotReward,
		ConversationStates.IDLE,
		"Nie masz mojego pierścionka. Proszę, szukaj dalej.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		check.canReward,
		ConversationStates.IDLE,
		"Dziękuję bardzo! W nagrodę daję ci ten rzemyk. Jest nieco większy od tego, którego już posiadasz.",
		{
			actions:create("DropInfostringItemAction", {"pierścień zaręczynowy", ring_infostring}),
			actions:create(rewardAction),
		})

	-- player reports that ring was misplaced

	ari:add(
		ConversationStates.QUESTION_1,
		"lost",
		conditions:notC(hasRingCondition),
		ConversationStates.IDLE,
		"Zgubiłeś mój zgubiony pierścionek zaręczynowy? To trochę ironiczne."
			.. " Założę się, że upuściłeś go w pobliżu miejsca, w którym go znalazłeś."
			.. " Sprawdź tam ponownie.",
		resetQuestAction)

	ari:add(
		ConversationStates.QUESTION_1,
		"lost",
		hasRingCondition,
		ConversationStates.IDLE,
		"Zgubiłeś mój zgubiony pierścionek zaręczynowy? To trochę ironiczne."
			.. " Założę się, że mi dokuczasz.",
		nil)
end


quests:create(quest_slot, "Zgubiony Pierścionek Zaręczynowy"):register(function()
	prepareNPC()
	prepareRequestStep()
	prepareBringStep()
end)