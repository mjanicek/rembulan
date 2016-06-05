package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.MetatableProvider;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.PlainValueTypeNamer;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.core.ValueTypeNamer;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.util.Check;

public class NameMetamethodValueTypeNamer implements ValueTypeNamer {

	private final MetatableProvider metatableProvider;

	public NameMetamethodValueTypeNamer(MetatableProvider metatableProvider) {
		this.metatableProvider = Check.notNull(metatableProvider);
	}

	@Override
	public String typeNameOf(Object instance) {
		Object nameField = Metatables.getMetamethod(metatableProvider, LibUtils.MT_NAME, instance);
		if (nameField instanceof String) {
			return (String) nameField;
		}
		else {
			if (Value.isLightUserdata(instance)) {
				return LibUtils.TYPENAME_LIGHT_USERDATA;
			}
			else {
				return PlainValueTypeNamer.INSTANCE.typeNameOf(instance);
			}
		}
	}

}
