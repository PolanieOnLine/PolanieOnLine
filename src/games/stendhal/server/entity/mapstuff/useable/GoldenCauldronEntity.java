package games.stendhal.server.entity.mapstuff.useable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import games.stendhal.server.events.ImageEffectEvent;
import games.stendhal.server.events.SoundEvent;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class GoldenCauldronEntity extends UseableEntity {
	private static final String RPCLASS_NAME = "golden_cauldron";
	private static final String QUEST_SLOT = "ciupaga_trzy_wasy";
	private static final String RESULT_ITEM = "wywar wąsatych smoków";
	private static final String SLOT_CONTENT = "content";
	private static final int SLOT_CAPACITY = 8;
	private static final int STATE_IDLE = 0;
	private static final int STATE_ACTIVE = 1;
	private static final int RESET_SECONDS = 5;

	static {
		generateRPClass();
	}

	private static final class Ingredient {
		private final String name;
		private final int amount;

		private Ingredient(final String name, final int amount) {
			this.name = name;
			this.amount = amount;
		}
	}

	private static final List<Ingredient> INGREDIENTS = List.of(
		new Ingredient("pióro azazela", 3),
		new Ingredient("magiczna krew", 10),
		new Ingredient("smocza krew", 5),
		new Ingredient("cudowna krew", 1)
	);

	private static final class CauldronSlot extends EntitySlot {
		private final GoldenCauldronEntity cauldron;

		private CauldronSlot(final GoldenCauldronEntity owner) {
			super(SLOT_CONTENT, SLOT_CONTENT);
			cauldron = owner;
			setCapacity(SLOT_CAPACITY);
		}

		@Override
		public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
			if (!entity.nextTo(cauldron)) {
				setErrorMessage("Musisz stać tuż przy kotle, aby sięgnąć po składniki.");
				return false;
			}
			return true;
		}

		@Override
		public boolean isReachableForThrowingThingsIntoBy(final Entity entity) {
			if (!entity.nextTo(cauldron)) {
				setErrorMessage("Tylko stojąc obok kotła możesz dodawać składniki.");
				return false;
			}
			return true;
		}
	}

	private boolean open;
	private String brewer;
	private TurnListener stateReset;

	public GoldenCauldronEntity() {
	        setRPClass(RPCLASS_NAME);
	        put("type", RPCLASS_NAME);
	        setEntityClass("entity");
	        setEntitySubclass("golden_cauldron");
	        setSize(2, 2);
	        put("state", STATE_IDLE);
	        setDescription("Runiczny kocioł bulgocze miodowym światłem, gotów do mieszania smoczych esencji.");
	        setMenu("Otwórz|Użyj");

		addSlot(new CauldronSlot(this));
	}

	public static void generateRPClass() {
		if (!RPClass.hasRPClass(RPCLASS_NAME)) {
			final RPClass rpclass = new RPClass(RPCLASS_NAME);
			rpclass.isA("useable_entity");
			rpclass.addAttribute("open", Type.FLAG);
			rpclass.addAttribute("brewer", Type.STRING);
			rpclass.addRPSlot(SLOT_CONTENT, SLOT_CAPACITY);
		}
	}

	@Override
	public boolean onUsed(final RPEntity entity) {
	        if (!(entity instanceof Player)) {
	                return false;
	        }

	        final Player player = (Player) entity;

	        if (!player.nextTo(this)) {
	                player.sendPrivateText("Musisz podejść bliżej kotła.");
	                return false;
	        }

	        if (!canWorkWith(player)) {
	                return false;
	        }

	        cancelStateReset();

	        if (!open) {
	                openFor(player);
	        } else if (isCurrentBrewer(player)) {
	                close(player);
	        } else {
			player.sendPrivateText("Draconia kręci głową: kocioł właśnie pracuje dla innego adepta.");
			return false;
		}

		notifyWorldAboutChanges();
		return true;
	}

	public void close(final Player player) {
	        if (!open || !isCurrentBrewer(player)) {
	                return;
	        }
	        open = false;
	        brewer = null;
	        if (has("open")) {
	                remove("open");
	        }
	        if (has("brewer")) {
	                remove("brewer");
	        }
	        cancelStateReset();
	        put("state", STATE_IDLE);
	        notifyWorldAboutChanges();
	}

	public void mix(final Player player) {
		if (!isCurrentBrewer(player)) {
			player.sendPrivateText("Tylko osoba prowadząca rytuał może mieszać w tym kotle.");
			return;
		}
		if (!canWorkWith(player)) {
			return;
		}

		final RPSlot slot = getSlot(SLOT_CONTENT);
		final List<Item> items = new ArrayList<>();
		for (final Iterator<RPObject> it = slot.iterator(); it.hasNext();) {
			final RPObject object = it.next();
			if (object instanceof Item) {
				items.add((Item) object);
			}
		}

		final Map<String, Integer> amounts = new HashMap<>();
		for (final Item item : items) {
			final int quantity = (item instanceof StackableItem)
				? ((StackableItem) item).getQuantity()
				: 1;
			amounts.merge(item.getName(), quantity, Integer::sum);
		}

		final List<String> issues = new ArrayList<>();
		final Map<String, Integer> required = new HashMap<>();
		for (final Ingredient ingredient : INGREDIENTS) {
			final int available = amounts.getOrDefault(ingredient.name, 0);
			required.put(ingredient.name, ingredient.amount);
			if (available < ingredient.amount) {
				issues.add(ingredient.amount + "x " + ingredient.name);
			} else if (available > ingredient.amount) {
				issues.add("(za dużo) " + available + "x " + ingredient.name + ", potrzebne tylko " + ingredient.amount);
			}
		}

		for (final Map.Entry<String, Integer> entry : amounts.entrySet()) {
			boolean known = false;
			for (final Ingredient ingredient : INGREDIENTS) {
				if (ingredient.name.equals(entry.getKey())) {
					known = true;
					break;
				}
			}
			if (!known) {
				issues.add(entry.getValue() + "x " + entry.getKey() + " (nie pasują do receptury)");
			}
		}

		if (!issues.isEmpty()) {
			player.sendPrivateText("Draconia ostrzega: coś się nie zgadza. Sprawdź składniki: "
				+ String.join(", ", issues) + ".");
			return;
		}

		for (final Item item : items) {
			final Integer needEntry = required.get(item.getName());
			if (needEntry == null || needEntry <= 0) {
				continue;
			}

			int remaining = needEntry.intValue();
			if (item instanceof StackableItem) {
				final StackableItem stack = (StackableItem) item;
				final int take = Math.min(stack.getQuantity(), remaining);
				stack.sub(take);
				remaining -= take;
				if (stack.getQuantity() <= 0) {
					stack.removeFromWorld();
				} else {
					stack.notifyWorldAboutChanges();
				}
			} else {
				item.removeFromWorld();
				remaining--;
			}
			required.put(item.getName(), remaining);
		}

		boolean consumedAll = true;
		for (final Map.Entry<String, Integer> entry : required.entrySet()) {
			if (entry.getValue() != null && entry.getValue() > 0) {
				consumedAll = false;
				break;
			}
		}

		if (!consumedAll) {
			player.sendPrivateText("Kocioł prycha – część składników nie została zużyta. Spróbuj ponownie po uporządkowaniu porcji.");
			return;
		}

	        final Item result = SingletonRepository.getEntityManager().getItem(RESULT_ITEM);
	        if (result == null) {
	                player.sendPrivateText("Magia ucieka, bo wywar nie został zapisany w kronikach świata. Zgłoś to administracji.");
	                return;
	        }

	        addEvent(new SoundEvent("bubble-1", 12, 90, games.stendhal.common.constants.SoundLayer.AMBIENT_SOUND));
	        addEvent(new ImageEffectEvent("magic_brew", true));

	        player.equipOrPutOnGround(result);
	        player.sendPrivateText("Uzyskałeś wywar wąsatych smoków – zanieś go Hadrinowi do rytuału.");

	        finishBrewing();
	}

	private void openFor(final Player player) {
	        open = true;
	        brewer = player.getName();
	        put("open", "");
	        put("brewer", brewer);
	        put("state", STATE_IDLE);
	}

	private boolean canWorkWith(final Player player) {
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState == null || questState.isEmpty()) {
			player.sendPrivateText("Nie znasz przepisu Draconii. Zapytaj ją o wywar zanim zaczniesz mieszać.");
			return false;
		}
		if (!"start".equals(questState)) {
			player.sendPrivateText("Kocioł Draconii pomaga przy rytuale trzeciego wąsa, gdy jesteś na etapie zbierania składników.");
			return false;
		}
		if (player.isSubmittableEquipped(RESULT_ITEM, 1)) {
			player.sendPrivateText("Masz już porcję wywaru wąsatych smoków. Zanieś ją Hadrinowi.");
			return false;
		}
		return true;
	}

	private boolean isCurrentBrewer(final Player player) {
	        if (brewer == null) {
	                return false;
	        }
		if (!brewer.equals(player.getName())) {
			final Player active = SingletonRepository.getRuleProcessor().getPlayer(brewer);
			if (active == null) {
				brewer = null;
				open = false;
				if (has("open")) {
					remove("open");
				}
				if (has("brewer")) {
					remove("brewer");
				}
				notifyWorldAboutChanges();
			}
			return false;
		}
	        return true;
	}

	@Override
	public String getName() {
	        return "kocioł Draconii";
	}

	private void finishBrewing() {
	        cancelStateReset();
	        open = false;
	        brewer = null;
	        if (has("open")) {
	                remove("open");
	        }
	        if (has("brewer")) {
	                remove("brewer");
	        }
	        put("state", STATE_ACTIVE);
	        notifyWorldAboutChanges();

	        stateReset = new TurnListener() {
	                @Override
	                public void onTurnReached(final int currentTurn) {
	                        put("state", STATE_IDLE);
	                        notifyWorldAboutChanges();
	                        stateReset = null;
	                }

	                @Override
	                public String toString() {
	                        return "golden_cauldron_reset";
	                }
	        };
	        SingletonRepository.getTurnNotifier().notifyInSeconds(RESET_SECONDS, stateReset);
	}

	private void cancelStateReset() {
	        if (stateReset != null) {
	                SingletonRepository.getTurnNotifier().dontNotify(stateReset);
	                stateReset = null;
	        }
	}
}
