# Rembulan completeness table

### Missing language/runtime features

 * garbage collection, `__gc` metamethod
 * strings:
    - plain 8-bit strings? (currently, `java.lang.String` is used for strings)
    - string library locale support
 * tables:
    - weak tables
    - efficient array tables 
    - efficient iteration (currently O(n<sup>2</sup>) to iterate the entire table; should be O(n)))

### Standard library

Statuses:

 * ![complete](complete.png) -- full compatibility: if it doesn't work, it's a bug
 * ![partial](partial.png) -- partial compatibility, caveats apply
 * ![stub](stub.png) -- a stub; does not cause an error in most cases, but has no effect
 * ![not implemented](not-implemented.png) -- not implemented; an error-throwing stub is present
 * ![missing](missing.png) -- not implemented; no stub present (will evaluate to `nil`)

| Entry | Status | Notes |
| --- | :---: | --- |
| [`assert`](http://www.lua.org/manual/5.3/manual.html#pdf-assert) | ![complete](complete.png) | |
| [`collectgarbage`](http://www.lua.org/manual/5.3/manual.html#pdf-collectgarbage) | ![stub](stub.png) | no-op, no support for `__gc` metamethods|
| [`dofile`](http://www.lua.org/manual/5.3/manual.html#pdf-dofile) | ![not implemented](not-implemented.png) | |
| [`error`](http://www.lua.org/manual/5.3/manual.html#pdf-error) | ![partial](partial.png) | error levels not supported |
| [`_G`](http://www.lua.org/manual/5.3/manual.html#pdf-_G) | ![complete](complete.png) | |
| [`getmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-getmetatable) | ![complete](complete.png) | |
| [`ipairs`](http://www.lua.org/manual/5.3/manual.html#pdf-ipairs) | ![complete](complete.png) | |
| [`load`](http://www.lua.org/manual/5.3/manual.html#pdf-load) | ![partial](partial.png) | binary chunks not supported |
| [`loadfile`](http://www.lua.org/manual/5.3/manual.html#pdf-loadfile) | ![not implemented](not-implemented.png) | |
| [`next`](http://www.lua.org/manual/5.3/manual.html#pdf-next) | ![complete](complete.png)| |
| [`pairs`](http://www.lua.org/manual/5.3/manual.html#pdf-pairs) | ![complete](complete.png) | |
| [`pcall`](http://www.lua.org/manual/5.3/manual.html#pdf-pcall) | ![complete](complete.png)| |
| [`print`](http://www.lua.org/manual/5.3/manual.html#pdf-print) | ![complete](complete.png) | |
| [`rawequal`](http://www.lua.org/manual/5.3/manual.html#pdf-rawequal) | ![complete](complete.png) | |
| [`rawget`](http://www.lua.org/manual/5.3/manual.html#pdf-rawget) | ![complete](complete.png) | |
| [`rawlen`](http://www.lua.org/manual/5.3/manual.html#pdf-rawlen) | ![complete](complete.png) | |
| [`rawset`](http://www.lua.org/manual/5.3/manual.html#pdf-rawset) | ![complete](complete.png) | |
| [`select`](http://www.lua.org/manual/5.3/manual.html#pdf-select) | ![complete](complete.png) | |
| [`setmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-setmetatable) | ![partial](partial.png) | no support for weak tables |
| [`tonumber`](http://www.lua.org/manual/5.3/manual.html#pdf-tonumber) | ![complete](complete.png) | |
| [`tostring`](http://www.lua.org/manual/5.3/manual.html#pdf-tostring) | ![complete](complete.png) | |
| [`type`](http://www.lua.org/manual/5.3/manual.html#pdf-type) | ![complete](complete.png) | |
| [`_VERSION`](http://www.lua.org/manual/5.3/manual.html#pdf-_VERSION) | ![complete](complete.png) | |
| [`xpcall`](http://www.lua.org/manual/5.3/manual.html#pdf-xpcall) | ![complete](complete.png) | |
| [`coroutine.create`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.create) | ![complete](complete.png) | |
| [`coroutine.isyieldable`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.isyieldable) | ![complete](complete.png) | |
| [`coroutine.resume`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.resume) | ![complete](complete.png) | |
| [`coroutine.running`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.running) | ![complete](complete.png) | |
| [`coroutine.status`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.status) | ![complete](complete.png) | |
| [`coroutine.wrap`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.wrap) | ![complete](complete.png) | |
| [`coroutine.yield`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.yield) | ![complete](complete.png) | |
| [`require`](http://www.lua.org/manual/5.3/manual.html#pdf-require) | ![stub](stub.png) | lookup in `package.loaded` only |
| [`package.config`](http://www.lua.org/manual/5.3/manual.html#pdf-package.config) | ![missing](missing.png) | |
| [`package.cpath`](http://www.lua.org/manual/5.3/manual.html#pdf-package.cpath) | ![missing](missing.png) | |
| [`package.loaded`](http://www.lua.org/manual/5.3/manual.html#pdf-package.loaded) | ![complete](complete.png) | |
| [`package.loadlib`](http://www.lua.org/manual/5.3/manual.html#pdf-package.loadlib) | ![not implemented](not-implemented.png) | |
| [`package.path`](http://www.lua.org/manual/5.3/manual.html#pdf-package.path) | ![missing](missing.png) | |
| [`package.preload`](http://www.lua.org/manual/5.3/manual.html#pdf-package.preload) | ![missing](missing.png) | |
| [`package.searchers`](http://www.lua.org/manual/5.3/manual.html#pdf-package.searchers) | ![missing](missing.png) | |
| [`package.searchpath`](http://www.lua.org/manual/5.3/manual.html#pdf-package.searchpath) | ![missing](missing.png) | |
| [`string.byte`](http://www.lua.org/manual/5.3/manual.html#pdf-string.byte) | ![partial](partial.png) | truncates chars into bytes |
| [`string.char`](http://www.lua.org/manual/5.3/manual.html#pdf-string.char) | ![complete](complete.png) | |
| [`string.dump`](http://www.lua.org/manual/5.3/manual.html#pdf-string.dump) | ![not implemented](not-implemented.png) | |
| [`string.find`](http://www.lua.org/manual/5.3/manual.html#pdf-string.find) | ![complete](complete.png) | |
| [`string.format`](http://www.lua.org/manual/5.3/manual.html#pdf-string.format) | ![complete](complete.png) | |
| [`string.gmatch`](http://www.lua.org/manual/5.3/manual.html#pdf-string.gmatch) | ![complete](complete.png) | |
| [`string.gsub`](http://www.lua.org/manual/5.3/manual.html#pdf-string.gsub) | ![complete](complete.png) | |
| [`string.len`](http://www.lua.org/manual/5.3/manual.html#pdf-string.len) | ![complete](complete.png) | |
| [`string.lower`](http://www.lua.org/manual/5.3/manual.html#pdf-string.lower) | ![complete](complete.png) | |
| [`string.match`](http://www.lua.org/manual/5.3/manual.html#pdf-string.match) | ![complete](complete.png) | |
| [`string.pack`](http://www.lua.org/manual/5.3/manual.html#pdf-string.pack) | ![not implemented](not-implemented.png) | |
| [`string.packsize`](http://www.lua.org/manual/5.3/manual.html#pdf-string.packsize) | ![not implemented](not-implemented.png) | |
| [`string.rep`](http://www.lua.org/manual/5.3/manual.html#pdf-string.rep) | ![complete](complete.png) | |
| [`string.reverse`](http://www.lua.org/manual/5.3/manual.html#pdf-string.reverse) | ![complete](complete.png) | |
| [`string.sub`](http://www.lua.org/manual/5.3/manual.html#pdf-string.sub) | ![complete](complete.png) | |
| [`string.unpack`](http://www.lua.org/manual/5.3/manual.html#pdf-string.unpack) | ![not implemented](not-implemented.png) | |
| [`string.upper`](http://www.lua.org/manual/5.3/manual.html#pdf-string.upper) | ![complete](complete.png) | |
| [`utf8.char`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.char) | ![not implemented](not-implemented.png) | |
| [`utf8.charpattern`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.charpattern) | ![not implemented](not-implemented.png) | |
| [`utf8.codes`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codes) | ![not implemented](not-implemented.png) | |
| [`utf8.codepoint`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codepoint) | ![not implemented](not-implemented.png) | |
| [`utf8.len`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.len) | ![not implemented](not-implemented.png) | |
| [`utf8.offset`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.offset) | ![not implemented](not-implemented.png) | |
| [`table.concat`](http://www.lua.org/manual/5.3/manual.html#pdf-table.concat) | ![complete](complete.png) | |
| [`table.insert`](http://www.lua.org/manual/5.3/manual.html#pdf-table.insert) | ![complete](complete.png) | |
| [`table.move`](http://www.lua.org/manual/5.3/manual.html#pdf-table.move) | ![complete](complete.png) | |
| [`table.pack`](http://www.lua.org/manual/5.3/manual.html#pdf-table.pack) | ![complete](complete.png) | |
| [`table.remove`](http://www.lua.org/manual/5.3/manual.html#pdf-table.remove) | ![complete](complete.png) | |
| [`table.sort`](http://www.lua.org/manual/5.3/manual.html#pdf-table.sort) | ![complete](complete.png) | |
| [`table.unpack`](http://www.lua.org/manual/5.3/manual.html#pdf-table.unpack) | ![complete](complete.png) | |
| [`math.abs`](http://www.lua.org/manual/5.3/manual.html#pdf-math.abs) | ![complete](complete.png) | |
| [`math.acos`](http://www.lua.org/manual/5.3/manual.html#pdf-math.acos) | ![complete](complete.png) | |
| [`math.asin`](http://www.lua.org/manual/5.3/manual.html#pdf-math.asin) | ![complete](complete.png) | |
| [`math.atan`](http://www.lua.org/manual/5.3/manual.html#pdf-math.atan) | ![complete](complete.png) | |
| [`math.ceil`](http://www.lua.org/manual/5.3/manual.html#pdf-math.ceil) | ![complete](complete.png) | |
| [`math.cos`](http://www.lua.org/manual/5.3/manual.html#pdf-math.cos) | ![complete](complete.png) | |
| [`math.deg`](http://www.lua.org/manual/5.3/manual.html#pdf-math.deg) | ![complete](complete.png) | |
| [`math.exp`](http://www.lua.org/manual/5.3/manual.html#pdf-math.exp) | ![complete](complete.png) | |
| [`math.floor`](http://www.lua.org/manual/5.3/manual.html#pdf-math.floor) | ![complete](complete.png) | |
| [`math.fmod`](http://www.lua.org/manual/5.3/manual.html#pdf-math.fmod) | ![complete](complete.png) | |
| [`math.huge`](http://www.lua.org/manual/5.3/manual.html#pdf-math.huge) | ![complete](complete.png) | |
| [`math.log`](http://www.lua.org/manual/5.3/manual.html#pdf-math.log) | ![complete](complete.png) | |
| [`math.max`](http://www.lua.org/manual/5.3/manual.html#pdf-math.max) | ![complete](complete.png) | |
| [`math.maxinteger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.maxinteger) | ![complete](complete.png) | |
| [`math.mininteger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.mininteger) | ![complete](complete.png) | |
| [`math.modf`](http://www.lua.org/manual/5.3/manual.html#pdf-math.modf) | ![complete](complete.png) | |
| [`math.pi`](http://www.lua.org/manual/5.3/manual.html#pdf-math.pi) | ![complete](complete.png) | |
| [`math.rad`](http://www.lua.org/manual/5.3/manual.html#pdf-math.rad) | ![complete](complete.png) | |
| [`math.random`](http://www.lua.org/manual/5.3/manual.html#pdf-math.random) | ![complete](complete.png) | |
| [`math.randomseed`](http://www.lua.org/manual/5.3/manual.html#pdf-math.randomseed) | ![complete](complete.png) | |
| [`math.sin`](http://www.lua.org/manual/5.3/manual.html#pdf-math.sin) | ![complete](complete.png) | |
| [`math.sqrt`](http://www.lua.org/manual/5.3/manual.html#pdf-math.sqrt) | ![complete](complete.png) | |
| [`math.tan`](http://www.lua.org/manual/5.3/manual.html#pdf-math.tan) | ![complete](complete.png) | |
| [`math.tointeger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.tointeger) | ![complete](complete.png) | |
| [`math.type`](http://www.lua.org/manual/5.3/manual.html#pdf-math.type) | ![complete](complete.png) | |
| [`math.ult`](http://www.lua.org/manual/5.3/manual.html#pdf-math.ult) | ![complete](complete.png) | |
| [`io.close`](http://www.lua.org/manual/5.3/manual.html#pdf-io.close) | ![complete](complete.png) | untested |
| [`io.flush`](http://www.lua.org/manual/5.3/manual.html#pdf-io.flush) | ![complete](complete.png) | untested |
| [`io.input`](http://www.lua.org/manual/5.3/manual.html#pdf-io.input) | ![partial](partial.png) | access default input file only, no `open` |
| [`io.lines`](http://www.lua.org/manual/5.3/manual.html#pdf-io.lines) | ![not implemented](not-implemented.png) | |
| [`io.open`](http://www.lua.org/manual/5.3/manual.html#pdf-io.open) | ![not implemented](not-implemented.png) | |
| [`io.output`](http://www.lua.org/manual/5.3/manual.html#pdf-io.output) | ![partial](partial.png) | access default output file only, no `open` |
| [`io.popen`](http://www.lua.org/manual/5.3/manual.html#pdf-io.popen) | ![not implemented](not-implemented.png) | |
| [`io.read`](http://www.lua.org/manual/5.3/manual.html#pdf-io.read) | ![partial](partial.png) | `file:read` not implemented |
| [`io.stdin`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stdin) | ![complete](complete.png) | |
| [`io.stderr`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stderr) | ![complete](complete.png) | |
| [`io.stdout`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stdout) | ![complete](complete.png) | |
| [`io.tmpfile`](http://www.lua.org/manual/5.3/manual.html#pdf-io.tmpfile) | ![not implemented](not-implemented.png) | |
| [`io.type`](http://www.lua.org/manual/5.3/manual.html#pdf-io.type) | ![complete](complete.png) | |
| [`io.write`](http://www.lua.org/manual/5.3/manual.html#pdf-io.write) | ![complete](complete.png) | |
| [`file:close`](http://www.lua.org/manual/5.3/manual.html#pdf-file:close) | ![complete](complete.png) | untested |
| [`file:flush`](http://www.lua.org/manual/5.3/manual.html#pdf-file:flush) | ![complete](complete.png) | untested |
| [`file:lines`](http://www.lua.org/manual/5.3/manual.html#pdf-file:lines) | ![not implemented](not-implemented.png) | |
| [`file:read`](http://www.lua.org/manual/5.3/manual.html#pdf-file:read) | ![not implemented](not-implemented.png) | |
| [`file:seek`](http://www.lua.org/manual/5.3/manual.html#pdf-file:seek) | ![complete](complete.png) | untested |
| [`file:setvbuf`](http://www.lua.org/manual/5.3/manual.html#pdf-file:setvbuf) | ![not implemented](not-implemented.png) | |
| [`file:write`](http://www.lua.org/manual/5.3/manual.html#pdf-file:write) | ![complete](complete.png) | |
| [`os.clock`](http://www.lua.org/manual/5.3/manual.html#pdf-os.clock) | ![not implemented](not-implemented.png) | |
| [`os.date`](http://www.lua.org/manual/5.3/manual.html#pdf-os.date) | ![not implemented](not-implemented.png) | |
| [`os.difftime`](http://www.lua.org/manual/5.3/manual.html#pdf-os.difftime) | ![not implemented](not-implemented.png) | |
| [`os.execute`](http://www.lua.org/manual/5.3/manual.html#pdf-os.execute) | ![not implemented](not-implemented.png) | |
| [`os.exit`](http://www.lua.org/manual/5.3/manual.html#pdf-os.exit) | ![not implemented](not-implemented.png) | |
| [`os.getenv`](http://www.lua.org/manual/5.3/manual.html#pdf-os.getenv) | ![complete](complete.png) | |
| [`os.remove`](http://www.lua.org/manual/5.3/manual.html#pdf-os.remove) | ![not implemented](not-implemented.png) | |
| [`os.rename`](http://www.lua.org/manual/5.3/manual.html#pdf-os.rename) | ![not implemented](not-implemented.png) | |
| [`os.setlocale`](http://www.lua.org/manual/5.3/manual.html#pdf-os.setlocale) | ![not implemented](not-implemented.png) | |
| [`os.time`](http://www.lua.org/manual/5.3/manual.html#pdf-os.time) | ![not implemented](not-implemented.png) | |
| [`os.tmpname`](http://www.lua.org/manual/5.3/manual.html#pdf-os.tmpname) | ![not implemented](not-implemented.png) | |
| [`debug.debug`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.debug) | ![not implemented](not-implemented.png) | unlikely to be implemented |
| [`debug.gethook`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.gethook) | ![not implemented](not-implemented.png) | instruction hooks incompatible |
| [`debug.getinfo`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getinfo) | ![not implemented](not-implemented.png) | incompatible |
| [`debug.getlocal`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getlocal) | ![not implemented](not-implemented.png) | incompatible |
| [`debug.getmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getmetatable) | ![complete](complete.png) | |
| [`debug.getregistry`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getregistry) | ![not implemented](not-implemented.png) | |
| [`debug.getupvalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getupvalue) | ![complete](complete.png) | |
| [`debug.getuservalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getuservalue) | ![complete](complete.png) | |
| [`debug.sethook`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.sethook) | ![not implemented](not-implemented.png) | instruction hooks incompatible |
| [`debug.setlocal`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setlocal) | ![not implemented](not-implemented.png) | incompatible |
| [`debug.setmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setmetatable) | ![complete](complete.png) | |
| [`debug.setupvalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setupvalue) | ![complete](complete.png) | |
| [`debug.setuservalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setuservalue) | ![complete](complete.png) | |
| [`debug.traceback`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.traceback) | ![not implemented](not-implemented.png) | |
| [`debug.upvalueid`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvalueid) | ![complete](complete.png) | |
| [`debug.upvaluejoin`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvaluejoin) | ![complete](complete.png) | |