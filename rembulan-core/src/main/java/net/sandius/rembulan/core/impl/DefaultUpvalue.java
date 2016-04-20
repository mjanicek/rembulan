package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.core.UpvalueFactory;

public class DefaultUpvalue extends Upvalue {

	public static final UpvalueFactory FACTORY_INSTANCE = new UpvalueFactory() {
		@Override
		public Upvalue newUpvalue(Object initialValue) {
			return new DefaultUpvalue(initialValue);
		}
	};

	private Object value;

	public DefaultUpvalue(Object initialValue) {
		this.value = initialValue;
	}

	@Override
	public Object get() {
		return value;
	}

	@Override
	public void set(Object value) {
		this.value = value;
	}

}
