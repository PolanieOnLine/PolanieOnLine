from pathlib import Path


def replace_exact(path, old, new, expected=1):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != expected:
        raise RuntimeError(f"{path}: expected {expected} matches, found {count}")
    target.write_text(text.replace(old, new), encoding="utf-8")


replace_exact(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\tpublic int getArmor() {",
    "\tpublic int getArmorScore() {")
replace_exact(
    "src/games/stendhal/server/core/rule/damage/WeaponArmorInteractionService.java",
    "((Creature) defender).getArmor();",
    "((Creature) defender).getArmorScore();")
replace_exact(
    "tests/games/stendhal/server/entity/creature/CreatureArmorTest.java",
    "creature.getArmor()",
    "creature.getArmorScore()",
    expected=3)

# This branch no longer needs the one-shot patching machinery.
Path(".github/workflows/apply-creature-armor-override.yml").unlink()
Path("buildtools/apply_creature_armor_override_patch.py").unlink()
