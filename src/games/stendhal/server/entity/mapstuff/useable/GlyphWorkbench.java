package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.actions.GlyphUpgradeAction;

/** A two-tile crafting table for developing glyphs. */
public final class GlyphWorkbench extends UseableEntity {
    public GlyphWorkbench() {
        setEntityClass("furniture");
        put("name", "rune_workbench");
        put("state", 0);
        put("title", "Stół runiczny");
        setDescription("Oto stół runiczny. Na drewnianym blacie leżą glify, runy i narzędzia do ich obróbki. Możesz tu rozwijać moc swoich glifów.");
        setSize(2, 1);
        setResistance(100);
    }
    @Override public boolean onUsed(RPEntity user) {
        if (!(user instanceof Player) || getZone() == null || user.getZone() != getZone() || !user.nextTo(this)) return false;
        GlyphUpgradeAction.open((Player)user, this);
        return true;
    }
}
