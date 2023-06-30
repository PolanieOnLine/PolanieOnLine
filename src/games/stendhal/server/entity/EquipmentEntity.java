package games.stendhal.server.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.constants.Testing;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import marauroa.common.game.RPObject;

public abstract class EquipmentEntity extends DressedEntity {
	private static final float WEAPON_DEF_MULTIPLIER = 2.0f;
	private static final float BOOTS_DEF_MULTIPLIER = 1.0f;
	private static final float LEG_DEF_MULTIPLIER = 1.5f;
	private static final float HELMET_DEF_MULTIPLIER = 1.0f;
	private static final float CLOAK_DEF_MULTIPLIER = 1.0f;
	private static final float ARMOR_DEF_MULTIPLIER = 2.0f;
	private static final float SHIELD_DEF_MULTIPLIER = 3.0f;
	private static final float RING_DEF_MULTIPLIER = 1.0f;
	private static final float NECKLACE_DEF_MULTIPLIER = 1.0f;
	private static final float GLOVE_DEF_MULTIPLIER = 1.0f;
	private static final float BELT_DEF_MULTIPLIER = 1.0f;

	public EquipmentEntity() {
		super();
	}

	public EquipmentEntity(RPObject object) {
		super(object);
	}

	/** @return true if the entity has an item of class shield equipped. */
	public boolean hasShield() {
		return isEquippedItemClass("lhand", "shield")
				|| isEquippedItemClass("rhand", "shield");
	}

	public Item getShield() {
		final Item item = getEquippedItemClass("lhand", "shield");
		if (item != null) {
			return item;
		} else {
			return getEquippedItemClass("rhand", "shield");
		}
	}

	public boolean hasArmor() {
		return isEquippedItemClass("armor", "armor");
	}

	public Item getArmor() {
		return getEquippedItemClass("armor", "armor");
	}

	public boolean hasHelmet() {
		return isEquippedItemClass("head", "helmet");
	}

	public Item getHelmet() {
		return getEquippedItemClass("head", "helmet");
	}

	public boolean hasLegs() {
		return isEquippedItemClass("legs", "legs");
	}

	public Item getLegs() {
		return getEquippedItemClass("legs", "legs");
	}

	public boolean hasBoots() {
		return isEquippedItemClass("feet", "boots");
	}

	public Item getBoots() {
		return getEquippedItemClass("feet", "boots");
	}

	public boolean hasCloak() {
		return isEquippedItemClass("cloak", "cloak");
	}

	public Item getCloak() {
		return getEquippedItemClass("cloak", "cloak");
	}

	public boolean hasRing() {
		return isEquippedItemClass("finger", "ring");
	}

	public Item getRing() {
		return getEquippedItemClass("finger", "ring");
	}

	public boolean hasRingB() {
		return isEquippedItemClass("fingerb", "ring");
	}

	public Item getRingB() {
		return getEquippedItemClass("fingerb", "ring");
	}

	public boolean hasNecklace() {
		return isEquippedItemClass("neck", "necklace");
	}

	public Item getNecklace() {
		return getEquippedItemClass("neck", "necklace");
	}

	public boolean hasGloves() {
		return isEquippedItemClass("glove", "glove");
	}

	public Item getGloves() {
		return getEquippedItemClass("glove", "glove");
	}

	public boolean hasMoney() {
		return isEquippedItemClass("money", "money");
	}

	public Item getMoney() {
		return getEquippedItemClass("money", "money");
	}

	public boolean hasBelt() {
		return isEquippedItemClass("pas", "belts");
	}

	public Item getBelt() {
		return getEquippedItemClass("pas", "belts");
	}

	/**
	 * Gets the weapon that this entity is holding in its hands.
	 *
	 * @return The weapon, or null if this entity is not holding a weapon. If
	 *		 the entity has a weapon in each hand, returns the weapon in its
	 *		 left hand.
	 */
	public Item getWeapon() {
		final String[] weaponsClasses = { "club", "sword", "dagger", "axe", "ranged", "missile", "wand", "whip" };

		for (final String weaponClass : weaponsClasses) {
			final String[] slots = { "lhand", "rhand" };
			for (final String slot : slots) {
				final Item item = getEquippedItemClass(slot, weaponClass);
				// FIXME: should weapon always be instance of WeaponImpl?
				if (item != null) {
					return item;
				}
			}
		}

		return null;
	}

	public List<Item> getWeapons() {
		final List<Item> weapons = new ArrayList<>();
		Item weaponItem = getWeapon();
		if (weaponItem != null) {
			weapons.add(weaponItem);

			// pair weapons
			if (weaponItem.getName().endsWith(" leworęczny")) {
				// check if there is a matching right-hand weapon in
				// the other hand.
				final String rpclass = weaponItem.getItemClass();
				weaponItem = getEquippedItemClass("rhand", rpclass);
				if ((weaponItem != null)
						&& (weaponItem.getName().endsWith(" praworęczny"))) {
					weapons.add(weaponItem);
				} else {
					// You can't use a left-hand weapon without the matching
					// right-hand weapon. Hmmm... but why not?
					weapons.clear();
				}
			} else {
				// You can't hold a right-hand weapon with your left hand, for
				// ergonomic reasons ;)
				if (weaponItem.getName().endsWith(" praworęczny")) {
					weapons.clear();
				}
			}
		}

		return weapons;
	}

	/**
	 * Gets the range weapon (bow etc.) that this entity is holding in its
	 * hands.
	 *
	 * @return The range weapon, or null if this entity is not holding a range
	 *		 weapon. If the entity has a range weapon in each hand, returns
	 *		 one in its left hand.
	 */
	public Item getRangeWeapon() {
		for (final Item weapon : getWeapons()) {
			if (!weapon.isOfClass("wand") && weapon.has("range")) {
				return weapon;
			}
		}

		return null;
	}

	private Item getRangedWeaponType(String weaponType) {
		for (final Item weapon : getWeapons()) {
			if (weapon.isOfClass(weaponType)) {
				return weapon;
			}
		}

		return null;
	}

	public Item getProjectileLauncher() {
		return getRangedWeaponType("ranged");
	}

	public Item getWandWeapon() {
		return getRangedWeaponType("wand");
	}

	/**
	 * Gets the stack of ammunition (arrows or similar) that this entity is
	 * holding in its hands.
	 *
	 * @return The ammunition, or null if this entity is not holding ammunition.
	 *		 If the entity has ammunition in each hand, returns the ammunition
	 *		 in its left hand.
	 */
	public StackableItem getAmmunition(String ammoType) {
		final String[] slots = { "lhand", "rhand" };

		for (final String slot : slots) {
			final StackableItem item = (StackableItem) getEquippedItemClass(slot, ammoType);
			if (item != null) {
				return item;
			}
		}

		return null;
	}

	public StackableItem getAmmunition() {
		return getAmmunition("ammunition");
	}

	public StackableItem getMagicSpells() {
		return getAmmunition("magia");
	}

	/**
	 * Gets the stack of missiles (spears or similar) that this entity is
	 * holding in its hands, but only if it is not holding another, non-missile
	 * weapon in the other hand.
	 *
	 * You can only throw missiles while you're not holding another weapon. This
	 * restriction is a workaround because of the way attack strength is
	 * determined; otherwise, one could increase one's spear attack strength by
	 * holding an ice sword in the other hand.
	 *
	 * @return The missiles, or null if this entity is not holding missiles. If
	 *		 the entity has missiles in each hand, returns the missiles in its
	 *		 left hand.
	 */
	public StackableItem getMissileIfNotHoldingOtherWeapon() {
		StackableItem missileWeaponItem = null;
		boolean holdsOtherWeapon = false;

		for (final Item weaponItem : getWeapons()) {
			if (weaponItem.isOfClass("missile")) {
				missileWeaponItem = (StackableItem) weaponItem;
			} else {
				holdsOtherWeapon = true;
			}
		}

		if (holdsOtherWeapon) {
			return null;
		} else {
			return missileWeaponItem;
		}
	}

	/**
	 * Retrieves ATK or RATK (depending on testing.combat system property) value of equipped ammunition.
	 */
	private float getAmmoAtk(String ammoType) {
		float ammo = 0;

		final StackableItem ammoItem = getAmmunition(ammoType);
		if (ammoItem != null) {
			if (Testing.COMBAT) {
				ammo = ammoItem.getRangedAttack();
			} else {
				ammo = ammoItem.getAttack();
			}
		}

		return ammo;
	}

	private float getWeaponsAtk() {
		float weapon = 0;

		final List<Item> weapons = getWeapons();
		for (final Item weaponItem : weapons) {
			weapon += weaponItem.getAttack();
		}

		// calculate ammo when not using RATK stat
		if (!Testing.COMBAT && weapons.size() > 0) {
			if (getWeapons().get(0).isOfClass("ranged")) {
				weapon += getAmmoAtk("ammunition");
			}
			if (getWeapons().get(0).isOfClass("wand")) {
				float magic = getAmmoAtk("magia");
				if (magic != 0) {
					weapon += magic;
				} else {
					// Set 10% attack value from equipped wand if player doesn't use magic
					weapon *= 0.1;
				}
			}
		}

		return weapon;
	}

	/**
	 * Retrieves total ATK value of held weapons.
	 */
	public float getItemAtk() {
		float weapon = getWeaponsAtk();
		int glove = 0;
		int ring = 0;
		int ringb = 0;
		int shield = 0;
		int belt = 0;
		int neck = 0;
		int legs = 0;
		int helmet = 0;
		int cloak = 0;
		int boots = 0;

		if (hasGloves()) {
			glove += getGloves().getAttack();
		} if (hasRing()) {
			ring += getRing().getAttack();
		} if (hasRingB()) {
			ringb += getRingB().getAttack();
		} if (hasShield()) {
			shield += getShield().getAttack();
		} if (hasBelt()) {
			belt += getBelt().getAttack();
		} if (hasNecklace()) {
			neck += getNecklace().getAttack();
		} if (hasLegs()) {
			legs += getLegs().getAttack();
		} if (hasHelmet()) {
			helmet += getHelmet().getAttack();
		} if (hasCloak()) {
			cloak += getCloak().getAttack();
		} if (hasBoots()) {
			boots += getBoots().getAttack();
		}

		return weapon + glove + ring + ringb + shield + belt + neck + legs + helmet + cloak + boots;
	}

	/**
	 * Retrieves total range attack value of held weapon & ammunition.
	 */
	public float getItemRatk() {
		float ratk = 0;
		final List<Item> weapons = getWeapons();

		if (weapons.size() > 0) {
			final Item held = getWeapons().get(0);
			ratk += held.getRangedAttack();

			if (held.isOfClass("ranged")) {
				ratk += getAmmoAtk("ammunition");
			}
			if (held.isOfClass("wand")) {
				ratk += getAmmoAtk("magia");
			}
		}

		return ratk;
	}

	public float getItemDef() {
		int shield = 0;
		int armor = 0;
		int helmet = 0;
		int legs = 0;
		int boots = 0;
		int cloak = 0;
		int weapon = 0;
		int glove = 0;
		int necklace = 0;
		int ring = 0;
		int ringb = 0;
		int belt = 0;

		Item item;

		if (hasShield()) {
			item = getShield();
			shield = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasArmor()) {
			item = getArmor();
			armor = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasHelmet()) {
			item = getHelmet();
			helmet = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasLegs()) {
			item = getLegs();
			legs = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasBoots()) {
			item = getBoots();
			boots = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasCloak()) {
			item = getCloak();
			cloak = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasNecklace()) {
			item = getNecklace();
			necklace = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasRing()) {
			item = getRing();
			ring = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasRingB()) {
			item = getRingB();
			ringb = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasGloves()) {
			item = getGloves();
			glove = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		if (hasBelt()) {
			item = getBelt();
			belt = (int) (item.getDefense() / getItemLevelModifier(item));
		}

		final List<Item> targetWeapons = getWeapons();
		for (final Item weaponItem : targetWeapons) {
			weapon += (int) (weaponItem.getDefense() / getItemLevelModifier(weaponItem));
		}

		if (getWandWeapon() != null) {
			Item amm = getMagicSpells();
			if (amm != null) {
				weapon += amm.getDefense();
			}
		}

		return SHIELD_DEF_MULTIPLIER * shield + ARMOR_DEF_MULTIPLIER * armor
				+ CLOAK_DEF_MULTIPLIER * cloak + GLOVE_DEF_MULTIPLIER * glove
				+ HELMET_DEF_MULTIPLIER * helmet + NECKLACE_DEF_MULTIPLIER * necklace
				+ LEG_DEF_MULTIPLIER * legs + BOOTS_DEF_MULTIPLIER * boots
				+ RING_DEF_MULTIPLIER * ring + RING_DEF_MULTIPLIER * ringb
				+ BELT_DEF_MULTIPLIER * belt + WEAPON_DEF_MULTIPLIER * weapon;
	}

	/**
	 * get all items that affect a player's defensive value except the weapon
	 *
	 * @return a list of all equipped defensive items
	 */
	public List<Item> getDefenseItems() {
		List<Item> items = new LinkedList<>();
		if (hasShield()) {
			items.add(getShield());
		}
		if (hasArmor()) {
			items.add(getArmor());
		}
		if (hasHelmet()) {
			items.add(getHelmet());
		}
		if (hasLegs()) {
			items.add(getLegs());
		}

		if (hasBoots()) {
			items.add(getBoots());
		}
		if (hasCloak()) {
			items.add(getCloak());
		}

		if (hasRing()) {
			items.add(getRing());
		}
		if (hasRingB()) {
			items.add(getRingB());
		}
		if (hasNecklace()) {
			items.add(getNecklace());
		}
		if (hasGloves()) {
			items.add(getGloves());
		}
		if (hasBelt()) {
			items.add(getBelt());
		}
		return items;
	}

	public float handleEquipmentLifesteal(float sumAll) {
		float sumLifesteal = 0;
		if (hasGloves() && getGloves().has("lifesteal")) {
			sumLifesteal += sumAll * getGloves().getDouble("lifesteal");
		}
		if (hasRing() && getRing().has("lifesteal")) {
			sumLifesteal += sumAll * getRing().getDouble("lifesteal");
		} else if (hasRingB() && getRingB().has("lifesteal")) {
			sumLifesteal += sumAll * getRingB().getDouble("lifesteal");
		}
		
		return sumLifesteal;
	}

	/**
	 * Recalculates item based atk and def.
	 */
	public void updateItemAtkDef() {
		put("atk_item", ((int) getItemAtk()));
		if (Testing.COMBAT) {
			put("ratk_item", ((int) getItemRatk()));
		}
		put("def_item", ((int) getItemDef()));
		notifyWorldAboutChanges();
	}
}
