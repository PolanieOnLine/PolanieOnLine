from pathlib import Path


def replace_once(path, old, new):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected one match, found {count}")
    target.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\tprivate int def;\n\tprivate int hp;",
    "\tprivate int def;\n\tprivate Integer armor;\n\tprivate int hp;")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tstatusAttackProbability = 0;\n",
    "\t\t\tstatusAttackProbability = 0;\n\t\t\tarmor = null;\n")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    '\t\t\t} else if (qName.equals("def")) {\n\t\t\t\tdef = Integer.parseInt(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("hp")) {',
    '\t\t\t} else if (qName.equals("def")) {\n\t\t\t\tdef = Integer.parseInt(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("armor")) {\n\t\t\t\tarmor = Integer.valueOf(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("hp")) {')
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tcreature.setRPStats(hp, atk, ratk, def, speed);\n\t\t\tcreature.setLevel(level, xp);",
    "\t\t\tcreature.setRPStats(hp, atk, ratk, def, speed);\n\t\t\tcreature.setArmor(armor);\n\t\t\tcreature.setLevel(level, xp);")

replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t/** defense points. */\n\tprivate int def;\n\t/** experience points for killing this creature. */",
    "\t/** defense points. */\n\tprivate int def;\n\t/** Optional armor score overriding the defense fallback. */\n\tprivate Integer armor;\n\t/** experience points for killing this creature. */")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\tpublic int getDef() {\n\t\treturn def;\n\t}\n\n\tpublic double getSpeed() {",
    "\tpublic int getDef() {\n\t\treturn def;\n\t}\n\n\tpublic void setArmor(final Integer armor) {\n\t\tthis.armor = armor;\n\t}\n\n\tpublic Integer getArmor() {\n\t\treturn armor;\n\t}\n\n\tpublic double getSpeed() {")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t\tfinal Creature creature = new Creature(clazz, subclass, name, hp, atk, ratk, def,\n\t\t\t\tlevel, xp, width, height, speed, resistance, visibility, dropsItems, aiProfiles,\n\t\t\t\tcreatureSays, respawn, description);\n\t\tcreature.equip(equipsItems);",
    "\t\tfinal Creature creature = new Creature(clazz, subclass, name, hp, atk, ratk, def,\n\t\t\t\tlevel, xp, width, height, speed, resistance, visibility, dropsItems, aiProfiles,\n\t\t\t\tcreatureSays, respawn, description);\n\t\tif (armor != null) {\n\t\t\tcreature.setArmor(armor.intValue());\n\t\t}\n\t\tcreature.equip(equipsItems);")

replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\t\t} else {\n\t\t\tsetAtk(copy.getAtk());\n\t\t\tsetRatk(copy.getRatk());\n\t\t\tsetDef(copy.getDef());\n\t\t\tinitHP(copy.getBaseHP());\n\t\t}\n\t\tif (Occasion.MOREXP) {",
    "\t\t} else {\n\t\t\tsetAtk(copy.getAtk());\n\t\t\tsetRatk(copy.getRatk());\n\t\t\tsetDef(copy.getDef());\n\t\t\tinitHP(copy.getBaseHP());\n\t\t}\n\t\tif (copy.has(\"armor\")) {\n\t\t\tsetArmor(copy.getInt(\"armor\"));\n\t\t}\n\t\tif (Occasion.MOREXP) {")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\tpublic Creature getNewInstance() {\n\t\treturn new Creature(this);\n\t}\n",
    "\t/** Sets an explicit armor score independent from DEF. */\n\tpublic void setArmor(final int armor) {\n\t\tput(\"armor\", Math.max(0, Math.min(Short.MAX_VALUE, armor)));\n\t}\n\n\t/** Returns explicit armor or falls back to the current DEF value. */\n\tpublic int getArmor() {\n\t\treturn has(\"armor\") ? Math.max(0, getInt(\"armor\"))\n\t\t\t\t: Math.max(0, getDef());\n\t}\n\n\tpublic Creature getNewInstance() {\n\t\treturn new Creature(this);\n\t}\n")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    '\t\t\tnpc.addAttribute("debug", Type.VERY_LONG_STRING,\n\t\t\t\t\tDefinition.VOLATILE);\n\t\t\tnpc.addAttribute("metamorphosis", Type.STRING, Definition.VOLATILE);',
    '\t\t\tnpc.addAttribute("debug", Type.VERY_LONG_STRING,\n\t\t\t\t\tDefinition.VOLATILE);\n\t\t\tnpc.addAttribute("armor", Type.SHORT, Definition.HIDDEN);\n\t\t\tnpc.addAttribute("metamorphosis", Type.STRING, Definition.VOLATILE);')

replace_once(
    "data/conf/creatures.xsd",
    '\t\t\t<xsd:element name="def" minOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:positiveInteger" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="hp" minOccurs="1">',
    '\t\t\t<xsd:element name="def" minOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:positiveInteger" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="armor" minOccurs="0" maxOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:nonNegativeInteger" use="required" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="hp" minOccurs="1">')

replace_once(
    "src/games/stendhal/server/core/rule/damage/WeaponArmorInteractionService.java",
    "\t\tfinal int armorScore = Math.max(0, defender.getDef());",
    "\t\tfinal int armorScore = ((Creature) defender).getArmor();")
