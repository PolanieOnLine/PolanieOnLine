from pathlib import Path

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
