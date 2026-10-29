package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.actions.ProductionAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/** The charcoal mound itself is the interaction target, just like a workbench. */
public final class ProductionWorksite extends UseableEntity {
    private final SpeakerNPC producer;

    public ProductionWorksite(SpeakerNPC producer) {
        this.producer = producer;
        setEntityClass("furniture");
        put("name", "charcoal_mound");
        put("state", 0);
        put("title", "Mielerz");
        setDescription("Oto mielerz Smolisława. Użyj go, aby zlecić wypalanie węgla drzewnego lub odebrać gotowe zamówienie.");
        setSize(4, 3);
        // The map already blocks the mound's base; the smoke remains walkable.
        setResistance(0);
    }

    public SpeakerNPC getProducer() { return producer; }

    @Override public boolean onUsed(RPEntity user) {
        if (!(user instanceof Player) || getZone() == null || user.getZone() != getZone()
                || producer.getZone() != getZone() || !user.nextTo(this)) return false;
        ProductionAction.open((Player) user, this);
        return true;
    }
}
