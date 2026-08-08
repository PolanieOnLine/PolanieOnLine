from pathlib import Path


def replace_once(path, old, new):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected one match, found {count}")
    target.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_once(
    "src/games/stendhal/server/entity/RPEntity.java",
    '''\tprivate float getWeaponsAtk(final boolean rollDamage) {
\t\tfloat weapon = 0;

\t\tfinal List<Item> weapons = getWeapons();
\t\tfor (final Item weaponItem : weapons) {
\t\t\tweapon += rollDamage ? weaponItem.rollDamage() : weaponItem.getAverageDamage();
\t\t}

\t\t// calculate ammo when not using RATK stat
\t\tif (!Testing.COMBAT && weapons.size() > 0) {
\t\t\tif (getWeapons().get(0).isOfClass("ranged")) {
\t\t\t\tweapon += getAmmoAtk("ammunition");
\t\t\t}
\t\t\tif (getWeapons().get(0).isOfClass("wand")) {
\t\t\t\tfloat magic = getAmmoAtk("magia");
\t\t\t\tif (magic != 0) {
\t\t\t\t\tweapon += magic;
\t\t\t\t} else {
\t\t\t\t\t// Set 10% attack value from equipped wand if player doesn't use magic
\t\t\t\t\tweapon *= 0.1;
\t\t\t\t}
\t\t\t}
\t\t}

\t\treturn weapon;
\t}
''',
    '''\tprivate float getWeaponsAtk(final boolean rollDamage) {
\t\treturn getWeaponsAtk(rollDamage, null);
\t}

\tprivate float getWeaponsAtk(final boolean rollDamage,
\t\t\tfinal java.util.function.ToDoubleFunction<Item> damageMultiplier) {
\t\tfloat weapon = 0;

\t\tfinal List<Item> weapons = getWeapons();
\t\tfor (final Item weaponItem : weapons) {
\t\t\tdouble damage = rollDamage
\t\t\t\t\t? weaponItem.rollDamage() : weaponItem.getAverageDamage();
\t\t\tif (rollDamage && damageMultiplier != null) {
\t\t\t\tdamage *= damageMultiplier.applyAsDouble(weaponItem);
\t\t\t}
\t\t\tweapon += damage;
\t\t}

\t\t// calculate ammo when not using RATK stat; armor matchups modify only
\t\t// the held weapon roll, never ammunition or spell ammunition.
\t\tif (!Testing.COMBAT && weapons.size() > 0) {
\t\t\tif (getWeapons().get(0).isOfClass("ranged")) {
\t\t\t\tweapon += getAmmoAtk("ammunition");
\t\t\t}
\t\t\tif (getWeapons().get(0).isOfClass("wand")) {
\t\t\t\tfloat magic = getAmmoAtk("magia");
\t\t\t\tif (magic != 0) {
\t\t\t\t\tweapon += magic;
\t\t\t\t} else {
\t\t\t\t\t// Set 10% attack value from equipped wand if player doesn't use magic
\t\t\t\t\tweapon *= 0.1;
\t\t\t\t}
\t\t\t}
\t\t}

\t\treturn weapon;
\t}
''')

replace_once(
    "src/games/stendhal/server/entity/RPEntity.java",
    '''\t/** Retrieves melee attack using one server-side damage roll per weapon. */
\tpublic float getItemAtkForAttack() {
\t\tfinal float stableWeaponDamage = getWeaponsAtk(false);
\t\tfinal float rolledWeaponDamage = getWeaponsAtk(true);
\t\treturn Math.max(0, getItemAtk() - stableWeaponDamage + rolledWeaponDamage);
\t}
''',
    '''\t/** Retrieves melee attack using one server-side damage roll per weapon. */
\tpublic float getItemAtkForAttack() {
\t\treturn getItemAtkForAttack(null);
\t}

\t/**
\t * Retrieves melee attack while allowing each individual weapon roll to be
\t * modified independently. Non-weapon equipment remains outside the
\t * modifier.
\t *
\t * @param damageMultiplier multiplier provider for each held weapon
\t * @return rolled item attack
\t */
\tpublic float getItemAtkForAttack(
\t\t\tfinal java.util.function.ToDoubleFunction<Item> damageMultiplier) {
\t\tfinal float stableWeaponDamage = getWeaponsAtk(false);
\t\tfinal float rolledWeaponDamage = getWeaponsAtk(true, damageMultiplier);
\t\treturn Math.max(0, getItemAtk() - stableWeaponDamage + rolledWeaponDamage);
\t}
''')

replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    '''\t\tif (beaten) {
\t\t\tfinal List<Item> weapons = player.getWeapons();
\t\t\tfinal float itemAtk;
\t\t\tfinal float stableItemAtk;

\t\t\tif (Testing.COMBAT && isRanged) {
\t\t\t\titemAtk = player.getItemRatkForAttack();
\t\t\t\tstableItemAtk = player.getItemRatk();
\t\t\t} else {
\t\t\t\titemAtk = player.getItemAtkForAttack();
\t\t\t\tstableItemAtk = player.getItemAtk();
\t\t\t}

\t\t\tfinal double armorAdjustedItemAtk =
\t\t\t\t\tWeaponArmorInteractionService.adjustAttack(
\t\t\t\t\t\t\titemAtk, stableItemAtk, weapons, attackWeapon, defender);
\t\t\tdamage = player.damageDone(defender, armorAdjustedItemAtk,
\t\t\t\t\tplayer.getDamageType());
''',
    '''\t\tif (beaten) {
\t\t\tfinal List<Item> weapons = player.getWeapons();
\t\t\tfinal float itemAtk;

\t\t\tif (Testing.COMBAT && isRanged) {
\t\t\t\titemAtk = player.getItemRatkForAttack();
\t\t\t} else {
\t\t\t\titemAtk = player.getItemAtkForAttack(weapon ->
\t\t\t\t\t\tWeaponArmorInteractionService.getDamageMultiplier(
\t\t\t\t\t\t\t\tweapon, defender));
\t\t\t}

\t\t\tdamage = player.damageDone(defender, itemAtk,
\t\t\t\t\tplayer.getDamageType());
''')
