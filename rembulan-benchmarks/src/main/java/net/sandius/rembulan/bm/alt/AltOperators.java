package net.sandius.rembulan.bm.alt;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.RawOperators;

public abstract class AltOperators {

	private AltOperators() {
		// not to be instantiated or extended
	}

	public static boolean lt(LuaState state, Object a, Object b) {
		if (a instanceof Long) {
			long la = (Long) a;
			if (b instanceof Long) {
				long lb = (Long) b;
				return RawOperators.rawlt(la, lb);
			}
			else if (b instanceof Double) {
				double db = (Double) b;
				return RawOperators.rawlt(la, db);
			}
		}
		else if (a instanceof Double) {
			double da = (Double) a;
			if (b instanceof Long) {
				long lb = (Long) b;
				return RawOperators.rawlt(da, lb);
			}
			else if (b instanceof Double) {
				double db = (Double) b;
				return RawOperators.rawlt(da, db);
			}
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawlt((String) a, (String) b);
		}

		return Conversions.objectToBoolean(Operators.tryMetamethodCall(state, Metatables.MT_LT, a, b));
	}

}
