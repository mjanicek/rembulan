/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.MetatableProvider;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.PlainValueTypeNamer;
import net.sandius.rembulan.core.Values;
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
			if (Values.isLightUserdata(instance)) {
				return LibUtils.TYPENAME_LIGHT_USERDATA;
			}
			else {
				return PlainValueTypeNamer.INSTANCE.typeNameOf(instance);
			}
		}
	}

}
