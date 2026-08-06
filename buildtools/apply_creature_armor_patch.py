from pathlib import Path


def replace_once(path, old, new):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected one match, found {count}")
    target.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    "import games.stendhal.server.core.engine.GameEvent;\nimport games.stendhal.server.core.engine.SingletonRepository;",
    "import games.stendhal.server.core.engine.GameEvent;\nimport games.stendhal.server.core.engine.SingletonRepository;\nimport games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;")
replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    "\t\t\tdamage = player.damageDone(defender, itemAtk, player.getDamageType());",
    "\t\t\tfinal double armorAdjustedItemAtk =\n\t\t\t\t\tWeaponArmorInteractionService.adjustAttack(\n\t\t\t\t\t\t\titemAtk, attackWeapon, defender);\n\t\t\tdamage = player.damageDone(defender, armorAdjustedItemAtk,\n\t\t\t\t\tplayer.getDamageType());")
