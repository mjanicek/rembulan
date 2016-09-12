# Rembulan completeness table

### Missing language/runtime features

 * garbage collection, `__gc` metamethod
 * strings:
    - plain 8-bit strings? (currently, `java.lang.String` is used for strings)
    - string library locale support
 * tables:
    - weak tables
    - efficient array tables 
    - efficient length operator (currently O(n))
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
| `assert` | ![complete](complete.png) | |
| `collectgarbage` | ![stub](stub.png) | no-op, no support for `__gc` metamethods|
| `dofile` | ![not implemented](not-implemented.png) | |
| `error` | ![partial](partial.png) | error levels not supported |
| `_G` | ![complete](complete.png) | |
| `getmetatable` | ![complete](complete.png) | |
| `ipairs` | ![complete](complete.png) | |
| `load` | ![partial](partial.png) | binary chunks not supported |
| `loadfile` | ![not implemented](not-implemented.png) | |
| `next` | ![complete](complete.png)| |
| `pairs` | ![complete](complete.png) | |
| `pcall` | ![complete](complete.png)| |
| `print` | ![complete](complete.png) | |
| `rawequal` | ![complete](complete.png) | |
| `rawget` | ![complete](complete.png) | |
| `rawlen` | ![complete](complete.png) | |
| `rawset` | ![complete](complete.png) | |
| `select` | ![complete](complete.png) | |
| `setmetatable` | ![partial](partial.png) | no support for weak tables |
| `tonumber` | ![complete](complete.png) | |
| `tostring` | ![complete](complete.png) | |
| `type` | ![complete](complete.png) | |
| `_VERSION` | ![complete](complete.png) | |
| `xpcall` | ![complete](complete.png) | |
| `coroutine.create` | ![complete](complete.png) | |
| `coroutine.isyieldable` | ![complete](complete.png) | |
| `coroutine.resume` | ![complete](complete.png) | |
| `coroutine.running` | ![complete](complete.png) | |
| `coroutine.status` | ![complete](complete.png) | |
| `coroutine.wrap` | ![complete](complete.png) | |
| `coroutine.yield` | ![complete](complete.png) | |
| `require` | ![stub](stub.png) | lookup in `package.loaded` only |
| `package.config` | ![missing](missing.png) | |
| `package.cpath` | ![missing](missing.png) | |
| `package.loaded` | ![complete](complete.png) | |
| `package.loadlib` | ![not implemented](not-implemented.png) | |
| `package.path` | ![missing](missing.png) | |
| `package.preload` | ![missing](missing.png) | |
| `package.searchers` | ![missing](missing.png) | |
| `package.searchpath` | ![missing](missing.png) | |
| `string.byte` | ![partial](partial.png) | truncates chars into bytes |
| `string.char` | ![complete](complete.png) | |
| `string.dump` | ![not implemented](not-implemented.png) | |
| `string.find` | ![complete](complete.png) | |
| `string.format` | ![partial](partial.png) | no `__tostring` metamethod calls for `%s` |
| `string.gmatch` | ![complete](complete.png) | |
| `string.gsub` | ![complete](complete.png) | |
| `string.len` | ![complete](complete.png) | |
| `string.lower` | ![complete](complete.png) | |
| `string.match` | ![complete](complete.png) | |
| `string.pack` | ![not implemented](not-implemented.png) | |
| `string.packsize` | ![not implemented](not-implemented.png) | |
| `string.rep` | ![complete](complete.png) | |
| `string.reverse` | ![complete](complete.png) | |
| `string.sub` | ![complete](complete.png) | |
| `string.unpack` | ![not implemented](not-implemented.png) | |
| `string.upper` | ![complete](complete.png) | |
| `utf8.char` | ![not implemented](not-implemented.png) | |
| `utf8.charpattern` | ![not implemented](not-implemented.png) | |
| `utf8.codes` | ![not implemented](not-implemented.png) | |
| `utf8.codepoint` | ![not implemented](not-implemented.png) | |
| `utf8.len` | ![not implemented](not-implemented.png) | |
| `utf8.offset` | ![not implemented](not-implemented.png) | |
| `table.concat` | ![complete](complete.png) | |
| `table.insert` | ![complete](complete.png) | |
| `table.move` | ![not implemented](not-implemented.png) | |
| `table.pack` | ![complete](complete.png) | |
| `table.remove` | ![complete](complete.png) | |
| `table.sort` | ![not implemented](not-implemented.png) | |
| `table.unpack` | ![complete](complete.png) | |
| `math.abs` | ![complete](complete.png) | |
| `math.acos` | ![complete](complete.png) | |
| `math.asin` | ![complete](complete.png) | |
| `math.atan` | ![complete](complete.png) | |
| `math.ceil` | ![complete](complete.png) | |
| `math.cos` | ![complete](complete.png) | |
| `math.deg` | ![complete](complete.png) | |
| `math.exp` | ![complete](complete.png) | |
| `math.floor` | ![complete](complete.png) | |
| `math.fmod` | ![complete](complete.png) | |
| `math.huge` | ![complete](complete.png) | |
| `math.log` | ![complete](complete.png) | |
| `math.max` | ![complete](complete.png) | |
| `math.maxinteger` | ![complete](complete.png) | |
| `math.mininteger` | ![complete](complete.png) | |
| `math.modf` | ![complete](complete.png) | |
| `math.pi` | ![complete](complete.png) | |
| `math.rad` | ![complete](complete.png) | |
| `math.random` | ![complete](complete.png) | |
| `math.randomseed` | ![complete](complete.png) | |
| `math.sin` | ![complete](complete.png) | |
| `math.sqrt` | ![complete](complete.png) | |
| `math.tan` | ![complete](complete.png) | |
| `math.tointeger` | ![complete](complete.png) | |
| `math.type` | ![complete](complete.png) | |
| `math.ult` | ![complete](complete.png) | |
| `io.close` | ![complete](complete.png) | untested |
| `io.flush` | ![complete](complete.png) | untested |
| `io.input` | ![partial](partial.png) | access default input file only, no `open` |
| `io.lines` | ![not implemented](not-implemented.png) | |
| `io.open` | ![not implemented](not-implemented.png) | |
| `io.output` | ![partial](partial.png) | access default output file only, no `open` |
| `io.popen` | ![not implemented](not-implemented.png) | |
| `io.read` | ![partial](partial.png) | `file:read` not implemented |
| `io.stdin` | ![complete](complete.png) | |
| `io.stderr` | ![complete](complete.png) | |
| `io.stdout` | ![complete](complete.png) | |
| `io.tmpfile` | ![not implemented](not-implemented.png) | |
| `io.type` | ![complete](complete.png) | |
| `io.write` | ![complete](complete.png) | |
| `file:close` | ![complete](complete.png) | untested |
| `file:flush` | ![complete](complete.png) | untested |
| `file:lines` | ![not implemented](not-implemented.png) | |
| `file:read` | ![not implemented](not-implemented.png) | |
| `file:seek` | ![complete](complete.png) | untested |
| `file:setvbuf` | ![not implemented](not-implemented.png) | |
| `file:write` | ![complete](complete.png) | |
| `os.clock` | ![not implemented](not-implemented.png) | |
| `os.date` | ![not implemented](not-implemented.png) | |
| `os.difftime` | ![not implemented](not-implemented.png) | |
| `os.execute` | ![not implemented](not-implemented.png) | |
| `os.exit` | ![not implemented](not-implemented.png) | |
| `os.getenv` | ![complete](complete.png) | |
| `os.remove` | ![not implemented](not-implemented.png) | |
| `os.rename` | ![not implemented](not-implemented.png) | |
| `os.setlocale` | ![not implemented](not-implemented.png) | |
| `os.time` | ![not implemented](not-implemented.png) | |
| `os.tmpname` | ![not implemented](not-implemented.png) | |
| `debug.debug` | ![not implemented](not-implemented.png) | unlikely to be implemented |
| `debug.gethook` | ![not implemented](not-implemented.png) | instruction hooks incompatible |
| `debug.getinfo` | ![not implemented](not-implemented.png) | incompatible |
| `debug.getlocal` | ![not implemented](not-implemented.png) | incompatible |
| `debug.getmetatable` | ![complete](complete.png) | |
| `debug.getregistry` | ![not implemented](not-implemented.png) | |
| `debug.getupvalue` | ![complete](complete.png) | |
| `debug.getuservalue` | ![complete](complete.png) | |
| `debug.sethook` | ![not implemented](not-implemented.png) | instruction hooks incompatible |
| `debug.setlocal` | ![not implemented](not-implemented.png) | incompatible |
| `debug.setmetatable` | ![complete](complete.png) | |
| `debug.setupvalue` | ![complete](complete.png) | |
| `debug.setuservalue` | ![complete](complete.png) | |
| `debug.traceback` | ![not implemented](not-implemented.png) | |
| `debug.upvalueid` | ![complete](complete.png) | |
| `debug.upvaluejoin` | ![complete](complete.png) | |