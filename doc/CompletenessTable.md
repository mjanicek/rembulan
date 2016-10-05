# Rembulan completeness table

### Missing language/runtime features

 * garbage collection, `__gc` metamethod
 * strings:
    - plain 8-bit strings? (currently, `java.lang.String` is used for strings)
    - string library locale support
 * tables:
    - weak tables
    - efficient array tables 

### Standard library

Statuses:

 * ![complete](img/complete.png) -- full compatibility: if it doesn't work, it's a bug
 * ![partial](img/partial.png) -- partial compatibility, caveats apply
 * ![stub](img/stub.png) -- a stub; does not cause an error in most cases, but has no effect
 * ![not implemented](img/not-implemented.png) -- not implemented; an error-throwing stub is present
 * ![missing](img/missing.png) -- not implemented; no stub present (will evaluate to `nil`)

| Entry | Status | Notes |
| --- | :---: | --- |
| [`assert`](http://www.lua.org/manual/5.3/manual.html#pdf-assert) | ![complete](img/complete.png) | |
| [`collectgarbage`](http://www.lua.org/manual/5.3/manual.html#pdf-collectgarbage) | ![stub](img/stub.png) | no-op, no support for `__gc` metamethods|
| [`dofile`](http://www.lua.org/manual/5.3/manual.html#pdf-dofile) | ![not implemented](img/not-implemented.png) | |
| [`error`](http://www.lua.org/manual/5.3/manual.html#pdf-error) | ![partial](img/partial.png) | error levels not supported |
| [`_G`](http://www.lua.org/manual/5.3/manual.html#pdf-_G) | ![complete](img/complete.png) | |
| [`getmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-getmetatable) | ![complete](img/complete.png) | |
| [`ipairs`](http://www.lua.org/manual/5.3/manual.html#pdf-ipairs) | ![complete](img/complete.png) | `__ipairs` metamethod not supported |
| [`load`](http://www.lua.org/manual/5.3/manual.html#pdf-load) | ![partial](img/partial.png) | binary chunks not supported |
| [`loadfile`](http://www.lua.org/manual/5.3/manual.html#pdf-loadfile) | ![not implemented](img/not-implemented.png) | |
| [`next`](http://www.lua.org/manual/5.3/manual.html#pdf-next) | ![complete](img/complete.png)| |
| [`pairs`](http://www.lua.org/manual/5.3/manual.html#pdf-pairs) | ![complete](img/complete.png) | |
| [`pcall`](http://www.lua.org/manual/5.3/manual.html#pdf-pcall) | ![complete](img/complete.png)| |
| [`print`](http://www.lua.org/manual/5.3/manual.html#pdf-print) | ![complete](img/complete.png) | |
| [`rawequal`](http://www.lua.org/manual/5.3/manual.html#pdf-rawequal) | ![complete](img/complete.png) | |
| [`rawget`](http://www.lua.org/manual/5.3/manual.html#pdf-rawget) | ![complete](img/complete.png) | |
| [`rawlen`](http://www.lua.org/manual/5.3/manual.html#pdf-rawlen) | ![complete](img/complete.png) | |
| [`rawset`](http://www.lua.org/manual/5.3/manual.html#pdf-rawset) | ![complete](img/complete.png) | |
| [`select`](http://www.lua.org/manual/5.3/manual.html#pdf-select) | ![complete](img/complete.png) | |
| [`setmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-setmetatable) | ![partial](img/partial.png) | no support for weak tables |
| [`tonumber`](http://www.lua.org/manual/5.3/manual.html#pdf-tonumber) | ![complete](img/complete.png) | |
| [`tostring`](http://www.lua.org/manual/5.3/manual.html#pdf-tostring) | ![complete](img/complete.png) | |
| [`type`](http://www.lua.org/manual/5.3/manual.html#pdf-type) | ![complete](img/complete.png) | |
| [`_VERSION`](http://www.lua.org/manual/5.3/manual.html#pdf-_VERSION) | ![complete](img/complete.png) | |
| [`xpcall`](http://www.lua.org/manual/5.3/manual.html#pdf-xpcall) | ![complete](img/complete.png) | |
| [`coroutine.create`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.create) | ![complete](img/complete.png) | |
| [`coroutine.isyieldable`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.isyieldable) | ![complete](img/complete.png) | |
| [`coroutine.resume`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.resume) | ![complete](img/complete.png) | |
| [`coroutine.running`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.running) | ![complete](img/complete.png) | |
| [`coroutine.status`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.status) | ![complete](img/complete.png) | |
| [`coroutine.wrap`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.wrap) | ![complete](img/complete.png) | |
| [`coroutine.yield`](http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.yield) | ![complete](img/complete.png) | |
| [`require`](http://www.lua.org/manual/5.3/manual.html#pdf-require) | ![stub](img/stub.png) | lookup in `package.loaded` only |
| [`package.config`](http://www.lua.org/manual/5.3/manual.html#pdf-package.config) | ![missing](img/missing.png) | |
| [`package.cpath`](http://www.lua.org/manual/5.3/manual.html#pdf-package.cpath) | ![missing](img/missing.png) | |
| [`package.loaded`](http://www.lua.org/manual/5.3/manual.html#pdf-package.loaded) | ![complete](img/complete.png) | |
| [`package.loadlib`](http://www.lua.org/manual/5.3/manual.html#pdf-package.loadlib) | ![not implemented](img/not-implemented.png) | |
| [`package.path`](http://www.lua.org/manual/5.3/manual.html#pdf-package.path) | ![missing](img/missing.png) | |
| [`package.preload`](http://www.lua.org/manual/5.3/manual.html#pdf-package.preload) | ![missing](img/missing.png) | |
| [`package.searchers`](http://www.lua.org/manual/5.3/manual.html#pdf-package.searchers) | ![missing](img/missing.png) | |
| [`package.searchpath`](http://www.lua.org/manual/5.3/manual.html#pdf-package.searchpath) | ![missing](img/missing.png) | |
| [`string.byte`](http://www.lua.org/manual/5.3/manual.html#pdf-string.byte) | ![partial](img/partial.png) | truncates chars into bytes |
| [`string.char`](http://www.lua.org/manual/5.3/manual.html#pdf-string.char) | ![complete](img/complete.png) | |
| [`string.dump`](http://www.lua.org/manual/5.3/manual.html#pdf-string.dump) | ![not implemented](img/not-implemented.png) | |
| [`string.find`](http://www.lua.org/manual/5.3/manual.html#pdf-string.find) | ![complete](img/complete.png) | |
| [`string.format`](http://www.lua.org/manual/5.3/manual.html#pdf-string.format) | ![complete](img/complete.png) | |
| [`string.gmatch`](http://www.lua.org/manual/5.3/manual.html#pdf-string.gmatch) | ![complete](img/complete.png) | |
| [`string.gsub`](http://www.lua.org/manual/5.3/manual.html#pdf-string.gsub) | ![complete](img/complete.png) | |
| [`string.len`](http://www.lua.org/manual/5.3/manual.html#pdf-string.len) | ![complete](img/complete.png) | |
| [`string.lower`](http://www.lua.org/manual/5.3/manual.html#pdf-string.lower) | ![complete](img/complete.png) | |
| [`string.match`](http://www.lua.org/manual/5.3/manual.html#pdf-string.match) | ![complete](img/complete.png) | |
| [`string.pack`](http://www.lua.org/manual/5.3/manual.html#pdf-string.pack) | ![not implemented](img/not-implemented.png) | |
| [`string.packsize`](http://www.lua.org/manual/5.3/manual.html#pdf-string.packsize) | ![not implemented](img/not-implemented.png) | |
| [`string.rep`](http://www.lua.org/manual/5.3/manual.html#pdf-string.rep) | ![complete](img/complete.png) | |
| [`string.reverse`](http://www.lua.org/manual/5.3/manual.html#pdf-string.reverse) | ![complete](img/complete.png) | |
| [`string.sub`](http://www.lua.org/manual/5.3/manual.html#pdf-string.sub) | ![complete](img/complete.png) | |
| [`string.unpack`](http://www.lua.org/manual/5.3/manual.html#pdf-string.unpack) | ![not implemented](img/not-implemented.png) | |
| [`string.upper`](http://www.lua.org/manual/5.3/manual.html#pdf-string.upper) | ![complete](img/complete.png) | |
| [`utf8.char`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.char) | ![not implemented](img/not-implemented.png) | |
| [`utf8.charpattern`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.charpattern) | ![not implemented](img/not-implemented.png) | |
| [`utf8.codes`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codes) | ![not implemented](img/not-implemented.png) | |
| [`utf8.codepoint`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.codepoint) | ![not implemented](img/not-implemented.png) | |
| [`utf8.len`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.len) | ![not implemented](img/not-implemented.png) | |
| [`utf8.offset`](http://www.lua.org/manual/5.3/manual.html#pdf-utf8.offset) | ![not implemented](img/not-implemented.png) | |
| [`table.concat`](http://www.lua.org/manual/5.3/manual.html#pdf-table.concat) | ![complete](img/complete.png) | |
| [`table.insert`](http://www.lua.org/manual/5.3/manual.html#pdf-table.insert) | ![complete](img/complete.png) | |
| [`table.move`](http://www.lua.org/manual/5.3/manual.html#pdf-table.move) | ![complete](img/complete.png) | |
| [`table.pack`](http://www.lua.org/manual/5.3/manual.html#pdf-table.pack) | ![complete](img/complete.png) | |
| [`table.remove`](http://www.lua.org/manual/5.3/manual.html#pdf-table.remove) | ![complete](img/complete.png) | |
| [`table.sort`](http://www.lua.org/manual/5.3/manual.html#pdf-table.sort) | ![complete](img/complete.png) | |
| [`table.unpack`](http://www.lua.org/manual/5.3/manual.html#pdf-table.unpack) | ![complete](img/complete.png) | |
| [`math.abs`](http://www.lua.org/manual/5.3/manual.html#pdf-math.abs) | ![complete](img/complete.png) | |
| [`math.acos`](http://www.lua.org/manual/5.3/manual.html#pdf-math.acos) | ![complete](img/complete.png) | |
| [`math.asin`](http://www.lua.org/manual/5.3/manual.html#pdf-math.asin) | ![complete](img/complete.png) | |
| [`math.atan`](http://www.lua.org/manual/5.3/manual.html#pdf-math.atan) | ![complete](img/complete.png) | |
| [`math.ceil`](http://www.lua.org/manual/5.3/manual.html#pdf-math.ceil) | ![complete](img/complete.png) | |
| [`math.cos`](http://www.lua.org/manual/5.3/manual.html#pdf-math.cos) | ![complete](img/complete.png) | |
| [`math.deg`](http://www.lua.org/manual/5.3/manual.html#pdf-math.deg) | ![complete](img/complete.png) | |
| [`math.exp`](http://www.lua.org/manual/5.3/manual.html#pdf-math.exp) | ![complete](img/complete.png) | |
| [`math.floor`](http://www.lua.org/manual/5.3/manual.html#pdf-math.floor) | ![complete](img/complete.png) | |
| [`math.fmod`](http://www.lua.org/manual/5.3/manual.html#pdf-math.fmod) | ![complete](img/complete.png) | |
| [`math.huge`](http://www.lua.org/manual/5.3/manual.html#pdf-math.huge) | ![complete](img/complete.png) | |
| [`math.log`](http://www.lua.org/manual/5.3/manual.html#pdf-math.log) | ![complete](img/complete.png) | |
| [`math.max`](http://www.lua.org/manual/5.3/manual.html#pdf-math.max) | ![complete](img/complete.png) | |
| [`math.maxinteger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.maxinteger) | ![complete](img/complete.png) | |
| [`math.mininteger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.mininteger) | ![complete](img/complete.png) | |
| [`math.modf`](http://www.lua.org/manual/5.3/manual.html#pdf-math.modf) | ![complete](img/complete.png) | |
| [`math.pi`](http://www.lua.org/manual/5.3/manual.html#pdf-math.pi) | ![complete](img/complete.png) | |
| [`math.rad`](http://www.lua.org/manual/5.3/manual.html#pdf-math.rad) | ![complete](img/complete.png) | |
| [`math.random`](http://www.lua.org/manual/5.3/manual.html#pdf-math.random) | ![complete](img/complete.png) | |
| [`math.randomseed`](http://www.lua.org/manual/5.3/manual.html#pdf-math.randomseed) | ![complete](img/complete.png) | |
| [`math.sin`](http://www.lua.org/manual/5.3/manual.html#pdf-math.sin) | ![complete](img/complete.png) | |
| [`math.sqrt`](http://www.lua.org/manual/5.3/manual.html#pdf-math.sqrt) | ![complete](img/complete.png) | |
| [`math.tan`](http://www.lua.org/manual/5.3/manual.html#pdf-math.tan) | ![complete](img/complete.png) | |
| [`math.tointeger`](http://www.lua.org/manual/5.3/manual.html#pdf-math.tointeger) | ![complete](img/complete.png) | |
| [`math.type`](http://www.lua.org/manual/5.3/manual.html#pdf-math.type) | ![complete](img/complete.png) | |
| [`math.ult`](http://www.lua.org/manual/5.3/manual.html#pdf-math.ult) | ![complete](img/complete.png) | |
| [`io.close`](http://www.lua.org/manual/5.3/manual.html#pdf-io.close) | ![complete](img/complete.png) | untested |
| [`io.flush`](http://www.lua.org/manual/5.3/manual.html#pdf-io.flush) | ![complete](img/complete.png) | untested |
| [`io.input`](http://www.lua.org/manual/5.3/manual.html#pdf-io.input) | ![partial](img/partial.png) | access default input file only, no `open` |
| [`io.lines`](http://www.lua.org/manual/5.3/manual.html#pdf-io.lines) | ![not implemented](img/not-implemented.png) | |
| [`io.open`](http://www.lua.org/manual/5.3/manual.html#pdf-io.open) | ![not implemented](img/not-implemented.png) | |
| [`io.output`](http://www.lua.org/manual/5.3/manual.html#pdf-io.output) | ![partial](img/partial.png) | access default output file only, no `open` |
| [`io.popen`](http://www.lua.org/manual/5.3/manual.html#pdf-io.popen) | ![not implemented](img/not-implemented.png) | |
| [`io.read`](http://www.lua.org/manual/5.3/manual.html#pdf-io.read) | ![partial](img/partial.png) | `file:read` not implemented |
| [`io.stdin`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stdin) | ![complete](img/complete.png) | |
| [`io.stderr`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stderr) | ![complete](img/complete.png) | |
| [`io.stdout`](http://www.lua.org/manual/5.3/manual.html#pdf-io.stdout) | ![complete](img/complete.png) | |
| [`io.tmpfile`](http://www.lua.org/manual/5.3/manual.html#pdf-io.tmpfile) | ![not implemented](img/not-implemented.png) | |
| [`io.type`](http://www.lua.org/manual/5.3/manual.html#pdf-io.type) | ![complete](img/complete.png) | |
| [`io.write`](http://www.lua.org/manual/5.3/manual.html#pdf-io.write) | ![complete](img/complete.png) | |
| [`file:close`](http://www.lua.org/manual/5.3/manual.html#pdf-file:close) | ![complete](img/complete.png) | untested |
| [`file:flush`](http://www.lua.org/manual/5.3/manual.html#pdf-file:flush) | ![complete](img/complete.png) | untested |
| [`file:lines`](http://www.lua.org/manual/5.3/manual.html#pdf-file:lines) | ![not implemented](img/not-implemented.png) | |
| [`file:read`](http://www.lua.org/manual/5.3/manual.html#pdf-file:read) | ![not implemented](img/not-implemented.png) | |
| [`file:seek`](http://www.lua.org/manual/5.3/manual.html#pdf-file:seek) | ![complete](img/complete.png) | untested |
| [`file:setvbuf`](http://www.lua.org/manual/5.3/manual.html#pdf-file:setvbuf) | ![not implemented](img/not-implemented.png) | |
| [`file:write`](http://www.lua.org/manual/5.3/manual.html#pdf-file:write) | ![complete](img/complete.png) | |
| [`os.clock`](http://www.lua.org/manual/5.3/manual.html#pdf-os.clock) | ![not implemented](img/not-implemented.png) | |
| [`os.date`](http://www.lua.org/manual/5.3/manual.html#pdf-os.date) | ![not implemented](img/not-implemented.png) | |
| [`os.difftime`](http://www.lua.org/manual/5.3/manual.html#pdf-os.difftime) | ![not implemented](img/not-implemented.png) | |
| [`os.execute`](http://www.lua.org/manual/5.3/manual.html#pdf-os.execute) | ![not implemented](img/not-implemented.png) | |
| [`os.exit`](http://www.lua.org/manual/5.3/manual.html#pdf-os.exit) | ![not implemented](img/not-implemented.png) | |
| [`os.getenv`](http://www.lua.org/manual/5.3/manual.html#pdf-os.getenv) | ![complete](img/complete.png) | |
| [`os.remove`](http://www.lua.org/manual/5.3/manual.html#pdf-os.remove) | ![not implemented](img/not-implemented.png) | |
| [`os.rename`](http://www.lua.org/manual/5.3/manual.html#pdf-os.rename) | ![not implemented](img/not-implemented.png) | |
| [`os.setlocale`](http://www.lua.org/manual/5.3/manual.html#pdf-os.setlocale) | ![not implemented](img/not-implemented.png) | |
| [`os.time`](http://www.lua.org/manual/5.3/manual.html#pdf-os.time) | ![not implemented](img/not-implemented.png) | |
| [`os.tmpname`](http://www.lua.org/manual/5.3/manual.html#pdf-os.tmpname) | ![not implemented](img/not-implemented.png) | |
| [`debug.debug`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.debug) | ![not implemented](img/not-implemented.png) | unlikely to be implemented |
| [`debug.gethook`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.gethook) | ![not implemented](img/not-implemented.png) | instruction hooks incompatible |
| [`debug.getinfo`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getinfo) | ![not implemented](img/not-implemented.png) | incompatible |
| [`debug.getlocal`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getlocal) | ![not implemented](img/not-implemented.png) | incompatible |
| [`debug.getmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getmetatable) | ![complete](img/complete.png) | |
| [`debug.getregistry`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getregistry) | ![not implemented](img/not-implemented.png) | |
| [`debug.getupvalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getupvalue) | ![complete](img/complete.png) | |
| [`debug.getuservalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.getuservalue) | ![complete](img/complete.png) | |
| [`debug.sethook`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.sethook) | ![not implemented](img/not-implemented.png) | instruction hooks incompatible |
| [`debug.setlocal`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setlocal) | ![not implemented](img/not-implemented.png) | incompatible |
| [`debug.setmetatable`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setmetatable) | ![complete](img/complete.png) | |
| [`debug.setupvalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setupvalue) | ![complete](img/complete.png) | |
| [`debug.setuservalue`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.setuservalue) | ![complete](img/complete.png) | |
| [`debug.traceback`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.traceback) | ![not implemented](img/not-implemented.png) | |
| [`debug.upvalueid`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvalueid) | ![complete](img/complete.png) | |
| [`debug.upvaluejoin`](http://www.lua.org/manual/5.3/manual.html#pdf-debug.upvaluejoin) | ![complete](img/complete.png) | |