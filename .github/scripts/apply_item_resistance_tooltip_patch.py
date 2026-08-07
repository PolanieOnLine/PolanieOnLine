from pathlib import Path

# Remove the redundant defensive subtitle if an older checkout still has it.
path = Path("src/games/stendhal/client/gui/ItemRarityPresentation.java")
text = path.read_text(encoding="utf-8")
old = (
    "\t\tappendDivider(tooltip);\n"
    "\t\tappendPrimaryValue(tooltip, armour + \" pkt. pancerza\", null);\n"
    "\t\ttooltip.append(\"<table cellpadding='0' cellspacing='0'>\");\n"
    "\t\tappendTreeDetail(tooltip, false, \"Ochrona podstawowa\");\n"
    "\t\ttooltip.append(\"</table>\");\n"
)
new = (
    "\t\tappendDivider(tooltip);\n"
    "\t\tappendPrimaryValue(tooltip, armour + \" pkt. pancerza\", null);\n"
)
if old in text:
    text = text.replace(old, new, 1)
elif "Ochrona podstawowa" in text:
    raise RuntimeError("Unexpected armour markup around Ochrona podstawowa")
path.write_text(text, encoding="utf-8")

# The old tree-tail assertion belonged to the removed subtitle. Verify the
# actual resistance list markers instead.
path = Path("tests/games/stendhal/client/gui/ItemRarityPresentationTest.java")
text = path.read_text(encoding="utf-8")
old = (
    "\t\tassertTrue(tooltip.contains(\"<!--item-rarity-glow:#9b59b6:0.12-->\"));\n"
    "\t\tassertTrue(tooltip.contains(\"&#9492;&#9472;&#9670;\"));\n"
    "\t\tassertTrue(tooltip.contains(\"+4 ataku\"));\n"
)
new = (
    "\t\tassertTrue(tooltip.contains(\"<!--item-rarity-glow:#9b59b6:0.12-->\"));\n"
    "\t\tassertTrue(tooltip.contains(\"&#9670; ŚWIATŁO: 120%\"));\n"
    "\t\tassertTrue(tooltip.contains(\"&#9670; MROK: 80%\"));\n"
    "\t\tassertTrue(tooltip.contains(\"+4 ataku\"));\n"
)
if old in text:
    text = text.replace(old, new, 1)
elif "&#9492;&#9472;&#9670;" in text and "ŚWIATŁO: 120%" in text:
    raise RuntimeError("Unexpected armour tree assertion")
path.write_text(text, encoding="utf-8")
