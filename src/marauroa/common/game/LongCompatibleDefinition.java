package marauroa.common.game;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import marauroa.common.net.InputSerializer;

/**
	* Definition wrapper that can read legacy INT encoded values for attributes promoted to LONG.
	*/
public class LongCompatibleDefinition extends Definition {
	private static Field attributesField;

	static {
		try {
			attributesField = RPClass.class.getDeclaredField("attributes");
			attributesField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			attributesField = null;
		}
	}

	private LongCompatibleDefinition(final Definition original) {
		super(original.getDefinitionClass());
		setCode(original.getCode());
		setName(original.getName());
		setType(original.getType());
		setFlags(original.getFlags());
		setCapacity((byte) original.getCapacity());
	}

	@Override
	public String deserialize(final InputSerializer serializer) throws IOException {
		if ((getType() == Type.LONG) && "xp".equals(getName())) {
			final int protocol = serializer.getProtocolVersion();
			if ((protocol > 0) && (protocol < 35)) {
				return Integer.toString(serializer.readInt());
			}
		}
		return super.deserialize(serializer);
	}

	/**
	 * Replace the XP definition on the supplied RPClass with a compatibility wrapper.
	 *
	 * @param rpclass RPClass whose XP attribute definition should be wrapped
	 */
	@SuppressWarnings("unchecked")
	public static void ensureXpCompatibility(final RPClass rpclass) {
		if (attributesField == null) {
			return;
		}

		final Definition xpDefinition = rpclass.getDefinition(Definition.DefinitionClass.ATTRIBUTE, "xp");
		if (xpDefinition == null) {
			return;
		}
		if (xpDefinition instanceof LongCompatibleDefinition) {
			return;
		}

		try {
			final LongCompatibleDefinition wrapped = new LongCompatibleDefinition(xpDefinition);
			final Map<String, Definition> attributes = (Map<String, Definition>) attributesField.get(rpclass);
			attributes.put("xp", wrapped);
		} catch (IllegalAccessException e) {
			// Leave the original definition in place if we cannot access the attributes map
		}
	}
}
