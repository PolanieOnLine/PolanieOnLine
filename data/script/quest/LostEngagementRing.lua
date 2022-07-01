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

local minLevel = 50
local karmaAcceptReward = 15
local karmaCompleteReward = 50

local ring_infostring = "pierścionek Ariego"

-- location where ring may be on Athor island
local ring_locations = {
	{49, 30},
	{123, 3},
	{115, 122},
	{49, 106},
}

local questNotStartedCondition = conditions:create("QuestNotStartedCondition", {quest_slot})
local questCompletedCondition = conditions:create("QuestCompletedCondition", {quest_slot})
local questActiveCondition = conditions:create("QuestActiveCondition", {quest_slot})
local hasRingCondition = conditions:create("PlayerHasInfostringItemWithHimCondition", {
	"pierścień zaręczynowy",
	ring_infostring,
})

local hasKeyringCondition = conditions:create(function(player, sentence, npc)
	return player:hasFeature("keyring")
end)
local minLevelCondition = conditions:create("LevelGreaterThanCondition", {minLevel - 1})

--[[ FIXME: conditions:create is struggling with constructors that take varargs
local visitedAthorCondition = conditions:create("PlayerVisitedZonesCondition", {
	"0_athor_island",
})
]]
local visitedAthorCondition = luajava.newInstance(
	"games.stendhal.server.entity.npc.condition.PlayerVisitedZonesCondition",
	{"0_athor_island"})

local canStartCondition = conditions:andC({
	questNotStartedCondition,
	conditions:notC(questCompletedCondition),
	hasKeyringCondition,
	visitedAthorCondition,
	minLevelCondition,
})

local chooseRingLocation = function()
	return ring_locations[random:randUniform(1, 4)]
end

local setQuestAction = function(player, sentence, npc)
	player:addKarma(karmaAcceptReward)

	-- choose random location
	local selected = chooseRingLocation()
	player:setQuest(quest_slot, 0, selected[1])
	player:setQuest(quest_slot, 1, selected[2])
end

local resetQuestAction = function(player, sentence, npc)
	if player:hasQuest(quest_slot) then
		local slots = player:getQuest(quest_slot):split(";")
		local x = slots[1]
		local y = slots[2]

		if not string.isNumber(x) then
			x = slots[2]
			y = slots[3]
		end

		if not string.isNumber(x) or not string.isNumber(y) then
			logger:warn(quest_slot .. " quest: malformatted quest slot, setting"
				.. " new ring coordinates")

			local selected = chooseRingLocation()
			x = selected[1]
			y = selected[2]
		end

		player:setQuest(quest_slot, x .. ";" .. y)
	end
end

local rewardAction = function(player, sentence, npc)
	player:addKarma(karmaCompleteReward)
	player:setFeature("keyring", "6 3")

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
				questNotStartedCondition,
				conditions:orC({
					conditions:notC(hasKeyringCondition),
					conditions:notC(visitedAthorCondition),
					conditions:notC(minLevelCondition),
				}),
			},
			questCompletedCondition,
		}),
		ConversationStates.ATTENDING,
		"Cześć!",
		nil)
		
	-- player doesn't meet minimum level requirement
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		{
			questNotStartedCondition,
			conditions:notC(minLevelCondition),
		},
		ConversationStates.ATTENDING,
		"Myślę, że nie masz doświadczenia, aby mi pomóc. Wróć, gdy staniesz się silniejszy.",
		nil)

	-- player does not have keyring
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		{
			questNotStartedCondition,
			minLevelCondition,
			conditions:notC(hasKeyringCondition),
		},
		ConversationStates.ATTENDING,
		"Myślę, że nie masz doświadczenia, aby mi pomóc. Może gdybyś"
			.. " wiedział więcej o rzemyku.",
		nil)

	-- player has not visited Athor
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		{
			questNotStartedCondition,
			minLevelCondition,
			hasKeyringCondition,
			conditions:notC(visitedAthorCondition),
		},
		ConversationStates.ATTENDING,
		"Myślę, że nie masz doświadczenia, aby mi pomóc. Może gdybyś"
			.. " był bardziej zaznajomiony z wyspą Athor.",
		nil)

	-- player has keyring & can start quest
	ari:add(
		ConversationStates.IDLE,
		ConversationPhrases.GREETING_MESSAGES,
		canStartCondition,
		ConversationStates.ATTENDING,
		"Hej! Wyglądasz jak doświadczony wojownik. Może mógłbyś mi pomóc z #zadaniem.",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		canStartCondition,
		ConversationStates.QUEST_OFFERED,
		"Straciłem mój pierścionek zaręczynowy i wstydzę się powiedzieć Emmie. Czy pomógłbyś mi?",
		nil)

	-- player has already completed quest
	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		questCompletedCondition,
		ConversationStates.ATTENDING,
		"Dziękuję, ale mam już wszystko, czego potrzebuję.",
		nil)

	ari:add(
		ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		questActiveCondition,
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
		questActiveCondition,
		ConversationStates.QUESTION_1,
		"Znalazłeś mój pierścionek? Jeśli jest coś, co mogę zrobić, aby #pomóc, daj mi znać.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.HELP_MESSAGES,
		nil,
		ConversationStates.IDLE,
		"Mogłem go upuścić podczas spaceru po plaży. Jeśli tak jest,"
			.. " możesz potrzebować czegoś do kopania w piasku lub coś, co może wykryć metal."
			.. " Może lombard w Deniran ma narzędzie, którego możesz użyć.",
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
		conditions:notC(hasRingCondition),
		ConversationStates.IDLE,
		"Nie masz mojego pierścionka. Proszę, szukaj dalej.",
		nil)

	ari:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		hasRingCondition,
		ConversationStates.IDLE,
		"Dziękuję bardzo! W nagrodę daję ci ten rzemyk. Jest nieco większy od tego, którego już posiadasz.",
		{
			actions:create("DropInfostringItemAction", {"pierścień zaręczynowy", ring_infostring}),
			actions:create(rewardAction),
		})

	-- player reports that ring was misplaced

	ari:add(
		ConversationStates.QUESTION_1,
		{ "lost", "zgubiony", "stracony" },
		conditions:notC(hasRingCondition),
		ConversationStates.IDLE,
		"Zgubiłeś mój zgubiony pierścionek zaręczynowy? To trochę ironiczne."
			.. " Założę się, że upuściłeś go w pobliżu miejsca, w którym go znalazłeś."
			.. " Sprawdź tam ponownie.",
		resetQuestAction)

	ari:add(
		ConversationStates.QUESTION_1,
		{ "lost", "zgubiony", "stracony" },
		hasRingCondition,
		ConversationStates.IDLE,
		"Zgubiłeś mój zgubiony pierścionek zaręczynowy? To trochę ironiczne."
			.. " Założę się, że mi dokuczasz.",
		nil)
end


-- set up metal detector lender

-- we use a quest slot so player cannot borrow multiple metal detectors
local lender_slot = "sawyer_metal_detector"
local loanActive = conditions:create("QuestActiveCondition", {lender_slot})
local lend_phrases = {"loan", "lend", "metal detector", "borrow", "pożyczyć", "wypożyczyć", "wykrywacz"}
--local return_phrases = {"return", "zwrócić, "zwrot"}

-- items that Sawyer will take in exchange for his metal detector
local collateral = {
	-- sword
	"czarny miecz",
	"sztylet chaosu",
	"ognisty miecz demonów",
	"złota klinga",
	"złota klinga orków",
	"piekielny sztylet",
	--"miecz nieśmiertelnych",
	"miecz cesarski",
	"nihonto",
	"miecz orków",
	"sztylet królewski",
	"sztylet dusz",
	"miecz xenocyjski",

	-- axe
	"czarna halabarda",
	"czarna kosa",
	"topór chaosu",
	"topór Durina",
	--"magiczny topór obosieczny",

	-- club
	"lodowy młot bojowy",
	"młot wulkanów",

	-- ranged
	"łuk z mithrilu",

	-- armor
	"czarna zbroja",
	"lodowa zbroja",
	--"magiczna zbroja płytowa",
	--"zbroja mainiocyjska",
	"zbroja z mithrilu",
	"zbroja monarchistyczna",

	-- boots
	--"czarne buty",
	--"magiczne buty płytowe",
	"buty z mithrilu",
	"buty monarchistyczne",

	-- cloak
	--"czarny płaszcz",
	"płaszcz licha",
	--"magiczny płaszcz",
	"płaszcz z mithrilu",
	"płaszcz monarchistyczny",
	"płaszcz wampirzy",

	-- helmet
	"czarny hełm",
	"hełm libertyński",
	"magiczny hełm kolczy",
	"hełm z mithrilu",
	"hełm monarchistyczny",

	-- legs
	--"czarne spodnie",
	--"spodnie nabijane klejnotami",
	--"magiczne spodnie płytowe",
	"spodnie z mithrilu",
	"spodnie monarchistyczne",

	-- ring
	"pierścień szmaragdowy",
	"wzmocniony pierścień imperialny",
	"pierścień imperialny",
	"pierścień skorupy żółwia",

	-- shield
	"czarna tarcza",
	"lodowa tarcza",
	--"magiczna tarcza płytowa",
	"tarcza z mithrilu",
	"tarcza monarchistyczna",
	--"tarcza xenocyjska",
}

local loanMetalDetector = function(player, lender, detector, offer, bound_to, info_s)
	-- player walked away before receiving metal detector
	if lender:getAttending() == nil then
		lender:say("Gdzie poszedłeś? No cóż, chyba po prostu odłożę to z powrotem na swoje miejsce.")
		sawyersShelf:returnMetalDetector(detector)
		lender:setCurrentState(ConversationStates.IDLE)
		return
	end

	-- problem with metal detector item
	if detector == nil then
		lender:say("O o! Wygląda na to, że mój wykrywacz metalu jest zepsuty. Przepraszam kolego. Może mógłbyś"
			.. " skontaktować się z #/support i poprosić kogoś o naprawienie tego dla mnie.")
		lender:setCurrentState(ConversationStates.ATTENDING)
		return
	end

	local traded = player:getFirstEquipped(offer)

	if traded == nil then
		lender:say("Ha! Chcesz mnie oszukać?")
		lender:setCurrentState(ConversationStates.ATTENDING)
		return
	end

	-- handle items with infostring & bound items
	local bound_to = traded:getBoundTo() or ""
	local info_s = traded:getInfoString() or ""

	local slot_state = offer .. ";" .. bound_to .. ";" .. info_s

	detector:setInfoString("Sawyer;" .. slot_state)
	detector:setBoundTo(player:getName())
	detector:setUndroppableOnDeath(true)

	player:setQuest(lender_slot, slot_state)
	player:drop(traded)
	player:equipOrPutOnGround(detector)

	lender:say("Ok, proszę bardzo. Mój wykrywacz metali za twój " .. offer
		.. ". Uważaj na to. Jeśli się zgubi, nie będziesz mógł go"
		.. " #zwrócić, a ja zachowam twój przedmiot " .. offer .. ". W czymś"
		.. " jeszcze mógłbym Ci pomóc?")
	lender:setCurrentState(ConversationStates.ATTENDING)
end

local handleLendRequest = function(player, sentence, lender)
	local offer = sentence:getTrimmedText()

	-- FIXME: using table.contains function breaks ChatAction.fire method created with actions:create

	-- player ends conversation
	--if not table.contains(arrays:toTable(ConversationPhrases.GOODBYE_MESSAGES), offer:lower()) then
	for _, msg in ipairs(arrays:toTable(ConversationPhrases.GOODBYE_MESSAGES)) do
		if offer:lower() == msg then
			lender:getEntity():endConversation()
			return
		end
	end

	-- player decides not to trade
	-- FIXME: arrays:toTable does not parse ConversationPhrases.NO_MESSAGES correctly
	for _, msg in ipairs({"no", "nope", "nothing", "none", "nie"}) do
		if offer:lower() == msg then
			lender:say("W porządku. W czym jeszcze mogę Ci pomóc?")
			return
		end
	end

	-- Sawyer will not accept offered item
	--if not table.contains(collateral, offer) then
	local accepted = false
	for _, acceptable in ipairs(collateral) do
		if offer == acceptable then
			accepted = true
			break
		end
	end

	if not accepted then
		lender:say("Hmmm, nie jestem tym zainteresowany. Co jeszcze masz?")
		lender:setCurrentState(ConversationStates.QUESTION_1)
		return;
	end

	-- player isn't carrying the item
	if not player:isEquipped(offer) then
		lender:say("Nie masz nawet jednego z nich. Daj spokój, co masz?")
		lender:setCurrentState(ConversationStates.QUESTION_1)
		return;
	end

	local detector = sawyersShelf:getMetalDetector()

	-- metal detector from shelf was already loaned out
	if detector == nil then
		lender:say("Hmmmm. I guess I forgot that I already loaned it out...")
		lender:say("!me is thinking.")
		lender:setCurrentState(ConversationStates.BUSY)

		game:runAfter(10, function()
			lender:say("Oh! That's right, I keep a spare just in case.")
			detector = sawyersShelf:getSpareMetalDetector()

			game:runAfter(5, function()
				loanMetalDetector(player, lender, detector, offer,
					bound_to, info_s)
			end)
		end)

		return
	end

	-- metal detector from shelf
	loanMetalDetector(player, lender, detector, offer, bound_to, info_s)
end

local handleReturnRequest = function(player, sentence, lender)
	if not player:isEquipped("wykrywacz metali") then
		lender:say("Nie masz nawet wykrywacza metalu.")
		return
	end

	local detector = player:getFirstEquipped("wykrywacz metali")
	local detector_info = (detector:getInfoString() or ""):split(";")

	-- Sawyer doesn't recognize the metal detector
	if #detector_info == 0 or detector_info[1] ~= "Sawyer" then
		lender:say("To nie jest moje. Zabierz to z powrotem. Chcę MÓJ wykrywacz metali.")
		return
	end

	local item_name = detector_info[2]
	local bound_to = detector_info[3]
	local info_s = detector_info[4]

	local item = entities:getItem(item_name)

	-- problem with item
	if item == nil then
		lender:say("O o! Wygląda na to, że zepsułem twój przedmiot " .. item_name .. "."
			.. " Przepraszam kolego. Może mógłbyś skontaktować się z #/support i poprosić kogoś"
			.. " o naprawienie tego dla mnie.")
		return
	end

	if bound_to ~= "" then
		item:setBoundTo(bound_to)
	end

	if info_s ~= "" then
		item:setInfoString(info_s)
	end

	player:drop(detector)
	player:equipOrPutOnGround(item)
	player:setQuest(lender_slot, nil)

	sawyersShelf:returnMetalDetector(detector)
	lender:say("Dobra. Oto twój przedmiot " .. item_name .. ". Wygląda jak nowy."
		.. " Czy mogę jeszcze w czymś pomóc?")
end

local prepareMetalDetectorLender = function()
	local lender = entities:getNPC("Sawyer")
	if lender == nil then
		logger:error("Cannot set up metal detector lender Sawyer for Lost Engagement Ring quest")
		return
	end

	-- hint to players that they can borrow metal detector
	lender:addOffer("Mam też parę rzeczy, których nie mam nic przeciwko, aby #wypożyczyć.")

	lender:add(
		ConversationStates.ATTENDING,
		lend_phrases,
		conditions:notC(loanActive),
		ConversationStates.QUESTION_1,
		"Więc chcesz pożyczyć mój wykrywacz metalu? Cóż, nie pożyczam rzeczy"
			.. " bez jakiejś formy zabezpieczenia. Co chciałbyś zostawić?",
		nil)

	lender:add(
		ConversationStates.ATTENDING,
		lend_phrases,
		loanActive,
		ConversationStates.ATTENDING,
		"Już pożyczyłem ci mój wykrywacz metalu. Możesz go #zwrócić, kiedy tylko chcesz.",
		nil)

	lender:add(
		ConversationStates.QUESTION_1,
		"",
		conditions:notC(loanActive),
		ConversationStates.ATTENDING,
		nil,
		handleLendRequest)

	lender:add(
		ConversationStates.ATTENDING,
		"return",
		conditions:notC(loanActive),
		ConversationStates.ATTENDING,
		"Co zwrócić? Niczego ci nie pożyczałem.",
		nil)

	lender:add(
		ConversationStates.ATTENDING,
		"return",
		loanActive,
		ConversationStates.QUESTION_1,
		"Chcesz zwrócić mój wykrywacz metalu?",
		nil)

	lender:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.NO_MESSAGES,
		loanActive,
		ConversationStates.ATTENDING,
		"Dobra. W czym jeszcze mogę Ci pomóc?",
		nil)

	lender:add(
		ConversationStates.QUESTION_1,
		ConversationPhrases.YES_MESSAGES,
		loanActive,
		ConversationStates.ATTENDING,
		nil,
		handleReturnRequest)
end


local quest = quests:create(quest_slot, "Zgubiony Pierścionek Zaręczynowy",
	"Para, która ma wziąć ślub w mieście Fado, potrzebuje pomocy.")
quest:setMinLevel(minLevel)
quest:setRegion(Region.FADO_CITY)
quest:setNPCName("Ari")

quest:setHistoryFunction(function(player)
	local history = {}

	local state = player:getQuest(quest_slot, 0)
	if state == "done" then
		table.insert(history, "Znalazłem pierścionek Ariego. Teraz jest gotowy do małżeństwa z Emmą.")
	elseif state == "found_ring" then
		if player:isEquippedWithInfostring("pierścień zaręczynowy", ring_infostring) then
			table.insert(history, "Znalazłem pierścień Ariego i powinienem mu go przynieść.")
		else
			table.insert(history, "Znalazłem pierścionek Ariego, ale chyba go zgubiłem."
				.. " Powinienem zgłosić Ariemu, że <em>zgubiłem</em> go.")
		end
	else
		table.insert(history, "Ari stracił pierścionek zaręczynowy podczas wakacji na wyspie Athor.")
		if player:isEquipped("wykrywacz metali") then
			table.insert(history, "Może uda mi się go znaleźć tym wykrywaczem metalu.")
		elseif player:isEquipped("łopata") then
			table.insert(history, "Może uda mi się go znaleźć tą łopatą.")
		else
			table.insert(history, "Potrzebuję narzędzia, żeby to znaleźć.")
		end
	end

	return history
end)

quest:register(function()
	prepareNPC()
	prepareRequestStep()
	prepareBringStep()
	prepareMetalDetectorLender()
end)
