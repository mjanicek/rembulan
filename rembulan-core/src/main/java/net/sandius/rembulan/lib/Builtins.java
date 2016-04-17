package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.util.Check;

public abstract class Builtins {

	protected void install(Table table, String key, Object value) {
		Check.notNull(table);
		if (value != null) {
			table.rawset(key, value);
		}
	}
	
	public void installInto(Table table) {
		Check.notNull(table);

		install(table, "_G", table);
		install(table, "_VERSION", __VERSION());
		
		install(table, "print", _print());
		install(table, "type", _type());

		install(table, "next", _next());
		install(table, "pairs", _pairs());
		install(table, "ipairs", _ipairs());

		install(table, "tostring", _tostring());
		install(table, "tonumber", _tonumber());

		install(table, "error", _error());
		install(table, "assert", _assert());

		install(table, "getmetatable", _getmetatable());
		install(table, "setmetatable", _setmetatable());

		install(table, "pcall", _pcall());
		install(table, "xpcall", _xpcall());

		install(table, "rawequal", _rawequal());
		install(table, "rawget", _rawget());
		install(table, "rawset", _rawset());
		install(table, "rawlen", _rawlen());

		install(table, "select", _select());

		install(table, "collectgarbage", _collectgarbage());
		install(table, "dofile", _dofile());
		install(table, "load", _load());
		install(table, "loadfile", _loadfile());
	}

	public Table init(TableFactory tableFactory) {
		Table table = tableFactory.newTable(0, 0);
		installInto(table);
		return table;
	}

	public abstract String __VERSION();


	public abstract Function _print();

	public abstract Function _type();

	public abstract Function _tostring();

	/**
	 * <code>tonumber (e [, base])</code>
	 * <p>
	 * When called with no <code>base</code>, <code>tonumber</code> tries to convert its argument
	 * to a number. If the argument is already a number or a string convertible to a number
	 * (see §3.4.2 of the Lua Reference Manual), then <code>tonumber</code> returns this number;
	 * otherwise, it returns <b>nil</b>.
	 * <p>
	 * When called with <code>base</code>, then <code>e</code> should be a string to be
	 * interpreted as an integer numeral in that base. The base may be any integer between
	 * 2 and 36, inclusive. In bases above 10, the letter 'A' (in either upper or lower case)
	 * represents 10, 'B' represents 11, and so forth, with 'Z' representing 35. If the string
	 * <code>e</code> is not a valid numeral in the given base, the function returns <b>nil<b>.
	 */
	public abstract Function _tonumber();


	public abstract Function _error();

	public abstract Function _assert();


	public abstract Function _getmetatable();

	public abstract Function _setmetatable();


	public abstract Function _next();

	public abstract Function _pairs();

	public abstract Function _ipairs();


	public abstract Function _pcall();

	public abstract Function _xpcall();


	public abstract Function _rawequal();

	public abstract Function _rawget();

	public abstract Function _rawset();

	public abstract Function _rawlen();


	public abstract Function _select();


	public abstract Function _collectgarbage();

	public abstract Function _dofile();

	public abstract Function _load();

	public abstract Function _loadfile();
	
}
