/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2010-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement;

/**
 * categories of achievements
 *
 * @author madmetzger
 */
public enum Category {
	/** craft items */
	CRAFT,
	/** xp points */
	EXPERIENCE,
	/** def level */
	EXPERIENCE_DEF,
	/** atk level */
	EXPERIENCE_ATK,
	/** ratk level */
	EXPERIENCE_RATK,
	/** mining level */
	EXPERIENCE_MINING,
	/** fighting and killing creatures */
	FIGHTING,
	/** solving quests */
	QUEST,
	/** visiting groups of outside zones */
	OUTSIDE_ZONE,
	/** visiting groups of underground zones */
	UNDERGROUND_ZONE,
	/** visiting groups of interior zones */
	INTERIOR_ZONE,
	/** age of character */
	AGE,
	/** looting items */
	ITEM,
	/** looting items (sets) */
	ITEMSETS,
	/** upgraded items */
	ITEMUPGRADES,
	/** getting items */
	OBTAIN,
	/** buying/selling items */
	COMMERCE,
	/** helping others and being friendly */
	FRIEND,
	/** producing items */
	PRODUCTION,
	/** ados item quests */
	QUEST_ADOS_ITEMS,
	/** semos monster quest */
	QUEST_SEMOS_MONSTER,
	/** kirdneh item quest */
	QUEST_KIRDNEH_ITEM,
	/** zakopane items quest */
	QUEST_ZAKOPANE_ITEMS,
	/** gdansk jewellery quest */
	QUEST_GDANSK_JEWELLERY,
	/** kuznice monster quest */
	QUEST_KUZNICE_MONSTER,
	/** mithrilbourgh kill enemy army quest */
	QUEST_MITHRILBOURGH_ENEMY_ARMY,
	/** kill blordroughs quest */
	QUEST_KILL_BLORDROUGHS,
	/** deathmatch related */
	DEATHMATCH,
	/** pets weight **/
	WEIGHT,
	/** rebirths **/
	REBORN,
	/** złote ciupagi **/
	ZLOTE_CIUPAGI;
}
