from pathlib import Path
import re


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
    "\tprivate int def;\n\tprivate int armor;\n\tprivate int hp;")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tstatusAttackProbability = 0;\n",
    "\t\t\tstatusAttackProbability = 0;\n\t\t\tarmor = 0;\n")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    '\t\t\t} else if (qName.equals("def")) {\n\t\t\t\tdef = Integer.parseInt(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("hp")) {',
    '\t\t\t} else if (qName.equals("def")) {\n\t\t\t\tdef = Integer.parseInt(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("armor")) {\n\t\t\t\tarmor = Integer.parseInt(attrs.getValue("value"));\n\t\t\t} else if (qName.equals("hp")) {')
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tcreature.setRPStats(hp, atk, ratk, def, speed);\n\t\t\tcreature.setLevel(level, xp);",
    "\t\t\tcreature.setRPStats(hp, atk, ratk, def, speed);\n\t\t\tcreature.setArmor(armor);\n\t\t\tcreature.setLevel(level, xp);")

replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t/** defense points. */\n\tprivate int def;\n\t/** experience points for killing this creature. */",
    "\t/** defense points. */\n\tprivate int def;\n\t/** Physical armor used for weapon-class matchups. */\n\tprivate int armor;\n\t/** experience points for killing this creature. */")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\tpublic int getDef() {\n\t\treturn def;\n\t}\n\n\tpublic double getSpeed() {",
    "\tpublic int getDef() {\n\t\treturn def;\n\t}\n\n\tpublic void setArmor(final int armor) {\n\t\tthis.armor = Math.max(0, armor);\n\t}\n\n\tpublic int getArmor() {\n\t\treturn armor;\n\t}\n\n\tpublic double getSpeed() {")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t\tfinal Creature creature = new Creature(clazz, subclass, name, hp, atk, ratk, def,\n\t\t\t\tlevel, xp, width, height, speed, resistance, visibility, dropsItems, aiProfiles,\n\t\t\t\tcreatureSays, respawn, description);\n\t\tcreature.equip(equipsItems);",
    "\t\tfinal Creature creature = new Creature(clazz, subclass, name, hp, atk, ratk, def,\n\t\t\t\tlevel, xp, width, height, speed, resistance, visibility, dropsItems, aiProfiles,\n\t\t\t\tcreatureSays, respawn, description);\n\t\tcreature.setArmor(armor);\n\t\tcreature.equip(equipsItems);")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    '\t\tos.append("      <def value=\\\"" + def + "\\\"/>\\n");\n\t\tos.append("      <hp value=\\\"" + hp + "\\\"/>\\n");',
    '\t\tos.append("      <def value=\\\"" + def + "\\\"/>\\n");\n\t\tif (armor > 0) {\n\t\t\tos.append("      <armor value=\\\"" + armor + "\\\"/>\\n");\n\t\t}\n\t\tos.append("      <hp value=\\\"" + hp + "\\\"/>\\n");')

replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\t\tthis.statusAttackers = copy.statusAttackers;\n\t\tthis.noises = copy.noises;\n",
    "\t\tthis.statusAttackers = copy.statusAttackers;\n\t\tthis.noises = copy.noises;\n\t\tsetArmor(copy.getArmor());\n")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\tpublic Creature getNewInstance() {\n\t\treturn new Creature(this);\n\t}\n",
    "\tpublic void setArmor(final int armor) {\n\t\tif (armor > 0) {\n\t\t\tput(\"armor\", Math.min(Short.MAX_VALUE, armor));\n\t\t} else if (has(\"armor\")) {\n\t\t\tremove(\"armor\");\n\t\t}\n\t}\n\n\tpublic int getArmor() {\n\t\treturn has(\"armor\") ? Math.max(0, getInt(\"armor\")) : 0;\n\t}\n\n\tpublic Creature getNewInstance() {\n\t\treturn new Creature(this);\n\t}\n")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    '\t\t\tnpc.addAttribute("debug", Type.VERY_LONG_STRING,\n\t\t\t\t\tDefinition.VOLATILE);\n\t\t\tnpc.addAttribute("metamorphosis", Type.STRING, Definition.VOLATILE);',
    '\t\t\tnpc.addAttribute("debug", Type.VERY_LONG_STRING,\n\t\t\t\t\tDefinition.VOLATILE);\n\t\t\tnpc.addAttribute("armor", Type.SHORT, Definition.HIDDEN);\n\t\t\tnpc.addAttribute("metamorphosis", Type.STRING, Definition.VOLATILE);')

replace_once(
    "data/conf/creatures.xsd",
    '\t\t\t<xsd:element name="def" minOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:positiveInteger" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="hp" minOccurs="1">',
    '\t\t\t<xsd:element name="def" minOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:positiveInteger" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="armor" minOccurs="0" maxOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:nonNegativeInteger" use="required" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>\n\t\t\t<xsd:element name="hp" minOccurs="1">')

replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    "import games.stendhal.server.core.engine.GameEvent;\nimport games.stendhal.server.core.engine.SingletonRepository;",
    "import games.stendhal.server.core.engine.GameEvent;\nimport games.stendhal.server.core.engine.SingletonRepository;\nimport games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;")
replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    "\t\t\tdamage = player.damageDone(defender, itemAtk, player.getDamageType());",
    "\t\t\tfinal double armorAdjustedItemAtk =\n\t\t\t\t\tWeaponArmorInteractionService.adjustAttack(\n\t\t\t\t\t\t\titemAtk, attackWeapon, defender);\n\t\t\tdamage = player.damageDone(defender, armorAdjustedItemAtk,\n\t\t\t\t\tplayer.getDamageType());")

orc_path = Path("data/conf/creatures/stendhal/orc.xml")
orc_xml = orc_path.read_text(encoding="utf-8")
for creature_name, armor_value in [
        ("ork", 12),
        ("ork włócznik", 20),
        ("ork łowca", 50),
        ("ork wojownik", 50),
        ("szef orków", 100)]:
    pattern = r'(<creature name="' + re.escape(creature_name) + r'">.*?<def value="\d+"/>)'
    replacement = r'\1\n\t\t\t<armor value="' + str(armor_value) + r'"/>'
    orc_xml, count = re.subn(pattern, replacement, orc_xml,
                             count=1, flags=re.DOTALL)
    if count != 1:
        raise RuntimeError(f"Could not add armor to {creature_name}")
orc_path.write_text(orc_xml, encoding="utf-8")
