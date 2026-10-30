package games.stendhal.server.events;

import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Sent only to the administrator who requested the inspection. */
public final class AdminInspectEvent extends RPEvent {
    public static void generateRPClass() {
        RPClass type=new RPClass(Events.ADMIN_INSPECT);
        for(String key:new String[]{"phase","window_id"}) type.addAttribute(key,Type.STRING,Definition.PRIVATE);
        for(String key:new String[]{"payload","request"}) type.addAttribute(key,Type.VERY_LONG_STRING,Definition.PRIVATE);
    }
    public AdminInspectEvent(String payload,String request,String window,boolean refresh) {
        super(Events.ADMIN_INSPECT);put("payload",payload);put("request",request);put("window_id",window);put("phase",refresh?"refresh":"open");
    }
}
