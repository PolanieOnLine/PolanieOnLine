from pathlib import Path


def replace_once(path, old, new):
    file_path = Path(path)
    text = file_path.read_text(encoding="utf-8")
    if new in text:
        return False
    if old not in text:
        raise RuntimeError("Expected snippet not found in %s" % path)
    file_path.write_text(text.replace(old, new, 1), encoding="utf-8")
    return True


replace_once(
    "src/games/stendhal/server/entity/item/Item.java",
    "\tpublic void setSusceptibilities(Map<Nature, Double> susceptibilities) {\n"
    "\t\tthis.susceptibilities = susceptibilities;\n"
    "\t}\n\n"
    "\t/**\n"
    "\t * Add a status attack type to the item.\n",
    "\tpublic void setSusceptibilities(Map<Nature, Double> susceptibilities) {\n"
    "\t\tthis.susceptibilities = susceptibilities;\n"
    "\t}\n\n"
    "\t/**\n"
    "\t * Returns the elemental susceptibility profile configured by the item\n"
    "\t * definition. The returned map is read-only so presentation code cannot\n"
    "\t * accidentally change combat behaviour.\n"
    "\t *\n"
    "\t * @return susceptibility values keyed by damage nature\n"
    "\t */\n"
    "\tpublic Map<Nature, Double> getSusceptibilities() {\n"
    "\t\tif (susceptibilities == null) {\n"
    "\t\t\treturn Collections.emptyMap();\n"
    "\t\t}\n"
    "\t\treturn Collections.unmodifiableMap(susceptibilities);\n"
    "\t}\n\n"
    "\t/**\n"
    "\t * Add a status attack type to the item.\n")

replace_once(
    "src/games/stendhal/common/constants/ItemTooltip.java",
    "\tpublic static final String DEFENSE = \"def\";\n"
    "\tpublic static final String RANGE = \"range\";\n",
    "\tpublic static final String DEFENSE = \"def\";\n"
    "\t/** Prefix for final resistance percentages, e.g. resistance_light. */\n"
    "\tpublic static final String RESISTANCE_PREFIX = \"resistance_\";\n"
    "\tpublic static final String RANGE = \"range\";\n")

replace_once(
    "src/games/stendhal/server/entity/item/ItemTooltipService.java",
    "import java.util.HashSet;\n"
    "import java.util.Set;\n\n"
    "import games.stendhal.common.constants.GameTiming;\n"
    "import games.stendhal.common.constants.ItemTooltip;\n",
    "import java.util.HashSet;\n"
    "import java.util.Locale;\n"
    "import java.util.Set;\n\n"
    "import games.stendhal.common.constants.GameTiming;\n"
    "import games.stendhal.common.constants.ItemTooltip;\n"
    "import games.stendhal.common.constants.Nature;\n")

replace_once(
    "src/games/stendhal/server/entity/item/ItemTooltipService.java",
    "\t\tputPositiveInt(item, ItemTooltip.DEFENSE,\n"
    "\t\t\t\titem.getAttributeWithImprovement(\"def\", 0));\n"
    "\t\tputPositiveInt(item, ItemTooltip.RANGE, item.getRange());\n",
    "\t\tputPositiveInt(item, ItemTooltip.DEFENSE,\n"
    "\t\t\t\titem.getAttributeWithImprovement(\"def\", 0));\n"
    "\t\tfor (final java.util.Map.Entry<Nature, Double> entry\n"
    "\t\t\t\t: item.getSusceptibilities().entrySet()) {\n"
    "\t\t\tfinal int resistance = (int) Math.round(\n"
    "\t\t\t\t\t200.0 - (100.0 * entry.getValue().doubleValue()));\n"
    "\t\t\tput(item, ItemTooltip.RESISTANCE_PREFIX\n"
    "\t\t\t\t\t+ entry.getKey().name().toLowerCase(Locale.ROOT),\n"
    "\t\t\t\t\tInteger.toString(resistance));\n"
    "\t\t}\n"
    "\t\tputPositiveInt(item, ItemTooltip.RANGE, item.getRange());\n")

replace_once(
    "src/games/stendhal/client/gui/ItemRarityPresentation.java",
    "\t\tappendDivider(tooltip);\n"
    "\t\tappendPrimaryValue(tooltip, armour + \" pkt. pancerza\", null);\n"
    "\t\ttooltip.append(\"<table cellpadding='0' cellspacing='0'>\");\n"
    "\t\tappendTreeDetail(tooltip, false, \"Ochrona podstawowa\");\n"
    "\t\ttooltip.append(\"</table>\");\n",
    "\t\tappendDivider(tooltip);\n"
    "\t\tappendPrimaryValue(tooltip, armour + \" pkt. pancerza\", null);\n")

replace_once(
    "src/games/stendhal/client/gui/ItemRarityPresentation.java",
    "\tprivate static void appendBonuses(final StringBuilder tooltip,\n"
    "\t\t\tfinal RPObject object, final boolean weapon, final boolean armour) {\n"
    "\t\tfinal StringBuilder bonuses = new StringBuilder();\n\n"
    "\t\t/* ATK on armour and accessories is an equipment bonus, not weapon DPS. */\n",
    "\tprivate static void appendBonuses(final StringBuilder tooltip,\n"
    "\t\t\tfinal RPObject object, final boolean weapon, final boolean armour) {\n"
    "\t\tfinal StringBuilder bonuses = new StringBuilder();\n\n"
    "\t\tappendResistance(bonuses, object, \"light\", \"ŚWIATŁO\");\n"
    "\t\tappendResistance(bonuses, object, \"dark\", \"MROK\");\n"
    "\t\tappendResistance(bonuses, object, \"fire\", \"OGIEŃ\");\n"
    "\t\tappendResistance(bonuses, object, \"ice\", \"LÓD\");\n"
    "\t\tappendResistance(bonuses, object, \"earth\", \"NATURA\");\n"
    "\t\tappendResistance(bonuses, object, \"water\", \"WODA\");\n"
    "\t\tappendResistance(bonuses, object, \"cut\", \"FIZYCZNE\");\n\n"
    "\t\t/* ATK on armour and accessories is an equipment bonus, not weapon DPS. */\n")

replace_once(
    "src/games/stendhal/client/gui/ItemRarityPresentation.java",
    "\tprivate static void appendPercentageBonus(final StringBuilder bonuses,\n",
    "\tprivate static void appendResistance(final StringBuilder bonuses,\n"
    "\t\t\tfinal RPObject object, final String nature, final String label) {\n"
    "\t\tfinal String value = WeaponPerformanceCalculator.getTooltipValue(object,\n"
    "\t\t\t\tItemTooltip.RESISTANCE_PREFIX + nature);\n"
    "\t\tif (value != null) {\n"
    "\t\t\tappendBonusLine(bonuses, label + \": \" + value + \"%\");\n"
    "\t\t}\n"
    "\t}\n\n"
    "\tprivate static void appendPercentageBonus(final StringBuilder bonuses,\n")

# Update client tests for the simplified armour headline and resistance list.
path = Path("tests/games/stendhal/client/gui/ItemRarityPresentationTest.java")
text = path.read_text(encoding="utf-8")
text = text.replace(
    "\t\tputStat(object, ItemTooltip.DEFENSE, \"18\");\n"
    "\t\tputStat(object, ItemTooltip.ATTACK, \"4\");\n",
    "\t\tputStat(object, ItemTooltip.DEFENSE, \"18\");\n"
    "\t\tputStat(object, ItemTooltip.ATTACK, \"4\");\n"
    "\t\tputStat(object, ItemTooltip.RESISTANCE_PREFIX + \"light\", \"120\");\n"
    "\t\tputStat(object, ItemTooltip.RESISTANCE_PREFIX + \"dark\", \"80\");\n",
    1)
text = text.replace(
    "\t\tassertTrue(tooltip.contains(\"Ochrona podstawowa\"));\n"
    "\t\tassertTrue(tooltip.contains(\"<!--item-rarity-glow:#9b59b6:0.12-->\"));\n",
    "\t\tassertFalse(tooltip.contains(\"Ochrona podstawowa\"));\n"
    "\t\tassertTrue(tooltip.contains(\"ŚWIATŁO: 120%\"));\n"
    "\t\tassertTrue(tooltip.contains(\"MROK: 80%\"));\n"
    "\t\tassertTrue(tooltip.contains(\"<!--item-rarity-glow:#9b59b6:0.12-->\"));\n",
    1)
text = text.replace(
    "\t\tassertTrue(tooltip.contains(\"Ochrona podstawowa\"));\n",
    "\t\tassertFalse(tooltip.contains(\"Ochrona podstawowa\"));\n",
    2)
path.write_text(text, encoding="utf-8")
