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


-- disabled on main server
if not properties:enabled("stendhal.testserver") then
	do return end
end

--[[
	Stendhal quest: Lost Engagement Ring
	Steps:
	Reward:
	- karma
	- extended keyring (12 slots instead of 8)
]]

local quest_slot = "lost_engagement_ring"

local ari

local karmaAcceptReward = 15
local karmaCompleteReward = 50

local ring_infostring = "pierścionek Ariego"

local questActiveCondition = conditions:create("QuestActiveCondition", {quest_slot})
local hasRingCondition = conditions:create("PlayerHasInfostringItemWithHimCondition", {
	"pierścień zaręczynowy",
	ring_infostring,
})

local check = {
	canStart = conditions:andC({
		conditions:create("QuestNotStartedCondition", {quest_slot}),
		conditions:create("QuestNotCompletedCondition", {quest_slot}),
		conditions:create(function(player, sentence, npc)
			return player:getFeature("keyring") ~= nil
		end),
	}),
	questActive = questActiveCondition,
	questCompleted = conditions:create("QuestCompletedCondition", {quest_slot}),
	canReward = hasRingCondition,
	cannotReward = conditions:notC(hasRingCondition),
}

local rewardAction = function(player, sentence, npc)
	npc:say("Dziękuję bardzo! W nagrodę daję ci ten rzemyk. Jest nieco większy niż ten, który już posiadasz.")

	player:addKarma(karmaCompleteReward)
	player:setFeature("keyring_ext", true)

	player:setQuest(quest_slot, "done")
end

local prepareNPC = function()
	ari = entities:getNPC("Ari")
	ari:setIgnorePlayers(false)
	ari:addGreeting("Cześć!")
	ari:addGoodbye()
end

local prepareRequestStep = function()
	-- TODO: add reply to "quest" when player does not have keyring

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.canStart,
		ConversationStates.QUEST_OFFERED,
		"Zgubiłem pierścionek zaręczynowy i wstydzę się powiedzieć Emmie. Czy pomógłbyś mi?",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.questCompleted,
		ConversationStates.ATTENDING,
		"Nie potrzebuję więcej pomocy.",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		check.questActive,
		ConversationStates.ATTENDING,
		"Już pomagasz mi znaleźć pierścionek zaręczynowy.",
		nil)

	ari:add(
		ConversationStates.QUEST_OFFERED,
		ConversationPhrases.NO_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Nie chcę opuszczać Emmy side. Mam nadzieję, że znajdę kogoś, kto mi pomoże.",
		actions:create("DecreaseKarmaAction", {karmaAcceptReward}))

	ari:add(
		ConversationStates.QUEST_OFFERED,
		ConversationPhrases.YES_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Dziękuję bardzo! Daj mi znać, kiedy znajdziesz mój pierścionek. I nie mów nic Emmie.",
		actions:create("SetQuestAndModifyKarmaAction", {quest_slot, "start", karmaAcceptReward}))
end

local prepareBringStep = function()
	ari:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		check.questActive,
		ConversationStates.QUESTION_1,
		"Znalazłeś mój pierścionek?",
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
		nil,
		{
			actions:create(rewardAction),
			actions:create("DropInfostringItemAction", {"pierścień zaręczynowy", ring_infostring}),
		})
end


quests:create(quest_slot, "Zgubiony Pierścionek Zaręczynowy"):register(function()
	prepareNPC()
	prepareRequestStep()
	prepareBringStep()
end)