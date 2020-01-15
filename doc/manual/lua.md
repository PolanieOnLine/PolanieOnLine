## Stendhal Lua Scripting HowTo

*NOTE: The Lua implementation & this documentation may not be complete.*

#### Using the Logger

A global "logger" object is provided for accessing the logger instance.

Examples of usage:
```lua
logger:info("Info level message")
logger:warn("Warning level message")
logger:error("Error level message")
```

#### Creating New Object Instances

To create a new instance of a class, use the `luajava.newInstance` method:

```lua
-- Creating a new "Sign" instance
local sign = luajava.newInstance("games.stendhal.server.entity.mapstuff.sign.Sign")
```

#### Creating an NPC

- Use the global `game` object to set the current zone: `game:setZone("0_semos_city")`
- Use the global `stendhal` object to create a new NPC: `local npc = stendhal:createNPC("Lua")`

Example:
```
-- Set zone to Semos City
if game:setZone("0_semos_city") then

	-- Use helper object to create a new NPC
	local npc = npcHelper:createSpeakerNPC("Lua")
	npc:setEntityClass("littlegirlnpc")
	npc:setPosition(10, 55)
	npc:setBaseSpeed(0.1)
	local nodes = {
		{10, 55},
		{11, 55},
		{11, 56},
		{10, 56},
	}

	-- Use helper object to create NPC path
	npcHelper:setPath(npc, nodes)

	-- Dialogue
	npc:addJob("Actually, I am jobless.")
	npc:add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, nil, ConversationStates.ATTENDING, "I am sad, because I do not have a job.", nil)
	npc:addGoodbye();
	npc:setCollisionAction(CollisionAction.STOP)

	-- Some custom replies using conditions & actions
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		newCondition("PlayerNextToCondition"),
		ConversationStates.ATTENDING,
		"Czy mógłbyś się trochę cofnąć? Czuję twój oddech.",
		newAction("NPCEmoteAction", "coughs", false)
	)
	npc:add(
		ConversationStates.ATTENDING,
		"Lua",
		newCondition("NotCondition", newCondition("PlayerNextToCondition")),
		ConversationStates.ATTENDING,
		"To moje imię, nie nadużywaj go!",
		newAction("NPCEmoteAction", "giggles", false)
	)

	-- Add the NPC to the world
	game:add(npc)
end
```

Some enumerations accessible to Lua engine:
- [ConversationStates](../../src/games/stendhal/server/entity/npc/ConversationStates.java)
- [ConversationPhrases](../../src/games/stendhal/server/entity/npc/ConversationPhrases.java)
- [CollisionAction](../../src/games/stendhal/server/entity/CollisionAction.java)