package net.sandius.rembulan.core;

import java.util.Objects;

public abstract class AbstractConstants implements Constants {

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Constants)) return false;

		Constants that = (Constants) o;

		if (this.size() != that.size()) return false;

		for (int i = 0; i < this.size(); i++) {
			if (this.isNil(i) != that.isNil(i)) return false;
			if (this.isBoolean(i) != that.isBoolean(i)) return false;
			if (this.isInteger(i) != that.isInteger(i)) return false;
			if (this.isFloat(i) != that.isFloat(i)) return false;
			if (this.isString(i) != that.isString(i)) return false;

			if (this.isBoolean(i) && this.getBoolean(i) != that.getBoolean(i)) return false;
			if (this.isInteger(i) && this.getInteger(i) != that.getInteger(i)) return false;
			if (this.isFloat(i) && this.getFloat(i) != that.getFloat(i)) return false;
			if (this.isString(i) && !Objects.equals(this.getString(i), that.getString(i))) return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = 31;
		result = 31 * result + this.size();
		for (int i = 0; i < this.size(); i++) {
			result = 31 * result + (this.isNil(i) ? 0 : 1);
			result = 31 * result + (this.isBoolean(i) ? 0 : 1);
			result = 31 * result + (this.isInteger(i) ? 0 : 1);
			result = 31 * result + (this.isFloat(i) ? 0 : 1);
			result = 31 * result + (this.isString(i) ? 0 : 1);

			if (this.isBoolean(i)) result = 31 * result + (this.getBoolean(i) ? 0 : 1);
			if (this.isInteger(i)) {
				long l = this.getInteger(i);
				result = 31 * result + (int) (l ^ (l >>> 32));
			}
			if (this.isFloat(i)) {
				long l = Double.doubleToLongBits(this.getFloat(i));
				result = 31 * result + (int) (l ^ (l >>> 32));
			}
			if (this.isString(i)) {
				String s = this.getString(i);
				result = 31 * result + (s != null ? s.hashCode() : 0);
			}
		}
		return result;
	}

}
