package games.stendhal.server.script;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*

import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;

import games.stendhal.server.entity.spell.Spell;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
*/

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.Spell;

/**
 * Counts the number of unused sprites on the world.
 *
 * @author yoriy
 */
public class CountUnusedSprites extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		Collection<DefaultCreature> allCreatures = SingletonRepository.getEntityManager().getDefaultCreatures();
		Collection<DefaultItem> allItems = SingletonRepository.getEntityManager().getDefaultItems();
		Map<String, Spell> spellsByName = new HashMap<String, Spell>();
		for (String spellName : SingletonRepository.getEntityManager().getConfiguredSpells()) {
			Spell spell = SingletonRepository.getEntityManager().getSpell(spellName);
			if (spell != null) {
				spellsByName.put(spellName, spell);
			}
		}
		final StringBuilder sb=new StringBuilder();

        /* items */
		final File dirItemSprites = new File("data/sprites/items");
		String[] itemclasses = dirItemSprites.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		});

		for(final String f : itemclasses) {
			final File dirF = new File("data/sprites/items/"+f);
			String[] subclasses = dirF.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				};
			});

			if(subclasses == null) continue;
			for(final String g: subclasses) {
				// have both class and subclass, check if we have such item in Stendhal world
				String realsubclass = g.substring(0, g.length()-4);
				boolean found = false;
				for(final DefaultItem h : allItems) {
                   if(h.getItemSubclass().equals(realsubclass)) {
                	   found = true;
                   }
				}
				if(found == false) {
					sb.append("found unused item: ("+ f + "/" + realsubclass +")\n");
				};
			}
		};

		/* mosters */
		final File dirMonsterSprites = new File("data/sprites/monsters");
		String[] monsterclasses = dirMonsterSprites.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory();
			}
		});

		for(final String f : monsterclasses) {
			final File dirF = new File("data/sprites/monsters/"+f);
			String[] subclasses = dirF.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				};
			});

			if(subclasses == null) continue;
			for(final String g: subclasses) {
				// have both class and subclass, check if we have such item in Stendhal world
				String realsubclass = g.substring(0, g.length()-4);
				boolean found = false;
				for(final DefaultCreature h : allCreatures) {
		   if(h.getCreatureSubclass().equals(realsubclass)) {
		       found = true;
		   }
				}
				if(found == false) {
					sb.append("found unused creature: ("+ f + "/" + realsubclass +")\n");
				};
			}
		};

		final File dirSpellSprites = new File("data/sprites/spells");
		String[] spellnatures = dirSpellSprites.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			};
		});

		Set<String> spellsWithSprites = new HashSet<String>();
		if (spellnatures != null) {
			for (final String nature : spellnatures) {
				final File dirNature = new File(dirSpellSprites, nature);
				String[] sprites = dirNature.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".png");
					};
				});

				if (sprites == null) {
					continue;
				}

				for (final String sprite : sprites) {
					String spellName = sprite.substring(0, sprite.length() - 4);
					if (!spellsWithSprites.add(spellName)) {
						sb.append("found duplicate spell sprite: ("+ nature + "/" + spellName +")\n");
						continue;
					}
					Spell spell = spellsByName.get(spellName);
					if (spell == null) {
						sb.append("found unused spell: ("+ nature + "/" + spellName +")\n");
						continue;
					}

					if (!spell.getNature().toString().equalsIgnoreCase(nature)) {
						sb.append("found spell sprite in wrong nature: ("+ nature + "/" + spellName +")\n");
					}
				}
			}
		}

		for (Map.Entry<String, Spell> entry : spellsByName.entrySet()) {
			String expectedNature = entry.getValue().getNature().toString().toLowerCase(Locale.ENGLISH);
			File spriteFile = new File(dirSpellSprites, expectedNature + "/" + entry.getKey() + ".png");
			if (!spriteFile.exists()) {
				sb.append("missing spell sprite: ("+ expectedNature + "/" + entry.getKey() +")\n");
			}
		}

		admin.sendPrivateText("list of pictures: " + sb.toString());
	}
}
