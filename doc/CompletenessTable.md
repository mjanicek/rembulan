# Rembulan completeness table

### Missing language/runtime features

 * garbage collection, `__gc` metamethod
 * strings:
    - plain 8-bit strings? (currently, `java.lang.String` is used for strings)
    - string library locale support
 * tables:
    - weak tables 
    - efficient length operator (currently O(n))
    - efficient iteration (currently O(n<sup>2</sup> to iterate the entire table; should be O(n)))

### Standard library

Statuses:

 * ![complete](complete.svg) -- full compatibility: if it doesn't work, it's a bug
 * ![partial](partial.svg) -- partial compatibility, caveats apply
 * ![stub](stub.svg) -- a stub; does not cause an error in most cases, but has no effect
 * ![not implemented](not-implemented.svg) -- not implemented; an error-throwing stub is present
 * ![missing](missing.svg) -- not implemented; no stub present (will evaluate to `nil`)

| Entry | Status | Notes |
| --- | :---: | --- |
| `assert` | ![complete](complete.svg) | |
| `collectgarbage` | ![stub](stub.svg) | no-op, no support for `__gc` metamethods|
| `dofile` | ![not implemented](not-implemented.svg) | |
| `error` | ![partial](partial.svg) | error levels not supported |
| `_G` | ![complete](complete.svg) | |
| `getmetatable` | ![complete](complete.svg) | |
| `ipairs` | ![complete](complete.svg) | |
| `load` | ![partial](partial.svg) | binary chunks not supported |
| `loadfile` | ![not implemented](not-implemented.svg) | |
| `next` | ![complete](complete.svg)| |
| `pairs` | ![complete](complete.svg) | |
| `pcall` | ![complete](complete.svg)| |
| `print` | ![complete](complete.svg) | |
| `rawequal` | ![complete](complete.svg) | |
| `rawget` | ![complete](complete.svg) | |
| `rawlen` | ![complete](complete.svg) | |
| `rawset` | ![complete](complete.svg) | |
| `select` | ![complete](complete.svg) | |
| `setmetatable` | ![partial](partial.svg) | no support for weak tables |
| `tonumber` | ![complete](complete.svg) | |
| `tostring` | ![complete](complete.svg) | |
| `type` | ![complete](complete.svg) | |
| `_VERSION` | ![complete](complete.svg) | |
| `xpcall` | ![complete](complete.svg) | |
| `coroutine.create` | ![complete](complete.svg) | |
| `coroutine.isyieldable` | ![complete](complete.svg) | |
| `coroutine.resume` | ![complete](complete.svg) | |
| `coroutine.running` | ![complete](complete.svg) | |
| `coroutine.status` | ![complete](complete.svg) | |
| `coroutine.wrap` | ![complete](complete.svg) | |
| `coroutine.yield` | ![complete](complete.svg) | |
| `require` | ![stub](stub.svg) | lookup in `package.loaded` only |
| `package.config` | ![missing](missing.svg) | |
| `package.cpath` | ![missing](missing.svg) | |
| `package.loaded` | ![complete](complete.svg) | |
| `package.loadlib` | ![not implemented](not-implemented.svg) | |
| `package.path` | ![missing](missing.svg) | |
| `package.preload` | ![missing](missing.svg) | |
| `package.searchers` | ![missing](missing.svg) | |
| `package.searchpath` | ![missing](missing.svg) | |
| `string.byte` | ![partial](partial.svg) | truncates chars into bytes |
| `string.char` | ![complete](complete.svg) | |
| `string.dump` | ![not implemented](not-implemented.svg) | |
| `string.find` | ![complete](complete.svg) | |
| `string.format` | ![partial](partial.svg) | no `__tostring` metamethod calls for `%s` |
| `string.gmatch` | ![complete](complete.svg) | |
| `string.gsub` | ![complete](complete.svg) | |
| `string.len` | ![complete](complete.svg) | |
| `string.lower` | ![complete](complete.svg) | |
| `string.match` | ![complete](complete.svg) | |
| `string.pack` | ![not implemented](not-implemented.svg) | |
| `string.packsize` | ![not implemented](not-implemented.svg) | |
| `string.rep` | ![complete](complete.svg) | |
| `string.reverse` | ![complete](complete.svg) | |
| `string.sub` | ![complete](complete.svg) | |
| `string.unpack` | ![not implemented](not-implemented.svg) | |
| `string.upper` | ![complete](complete.svg) | |
| `utf8.char` | ![not implemented](not-implemented.svg) | |
| `utf8.charpattern` | ![not implemented](not-implemented.svg) | |
| `utf8.codes` | ![not implemented](not-implemented.svg) | |
| `utf8.codepoint` | ![not implemented](not-implemented.svg) | |
| `utf8.len` | ![not implemented](not-implemented.svg) | |
| `utf8.offset` | ![not implemented](not-implemented.svg) | |
| `table.concat` | ![not implemented](not-implemented.svg) | |
| `table.insert` | ![not implemented](not-implemented.svg) | |
| `table.move` | ![not implemented](not-implemented.svg) | |
| `table.pack` | ![complete](complete.svg) | |
| `table.remove` | ![not implemented](not-implemented.svg) | |
| `table.sort` | ![not implemented](not-implemented.svg) | |
| `table.unpack` | ![complete](complete.svg) | |
| `math.abs` | ![complete](complete.svg) | |
| `math.acos` | ![complete](complete.svg) | |
| `math.asin` | ![complete](complete.svg) | |
| `math.atan` | ![complete](complete.svg) | |
| `math.ceil` | ![complete](complete.svg) | |
| `math.cos` | ![complete](complete.svg) | |
| `math.deg` | ![complete](complete.svg) | |
| `math.exp` | ![complete](complete.svg) | |
| `math.floor` | ![complete](complete.svg) | |
| `math.fmod` | ![complete](complete.svg) | |
| `math.huge` | ![complete](complete.svg) | |
| `math.log` | ![complete](complete.svg) | |
| `math.max` | ![complete](complete.svg) | |
| `math.maxinteger` | ![complete](complete.svg) | |
| `math.mininteger` | ![complete](complete.svg) | |
| `math.modf` | ![complete](complete.svg) | |
| `math.pi` | ![complete](complete.svg) | |
| `math.rad` | ![complete](complete.svg) | |
| `math.random` | ![complete](complete.svg) | |
| `math.randomseed` | ![complete](complete.svg) | |
| `math.sin` | ![complete](complete.svg) | |
| `math.sqrt` | ![complete](complete.svg) | |
| `math.tan` | ![complete](complete.svg) | |
| `math.tointeger` | ![complete](complete.svg) | |
| `math.type` | ![complete](complete.svg) | |
| `math.ult` | ![complete](complete.svg) | |
| `io.close` | ![complete](complete.svg) | untested |
| `io.flush` | ![complete](complete.svg) | untested |
| `io.input` | ![complete](complete.svg) | access default input file only, no `open` |
| `io.lines` | ![not implemented](not-implemented.svg) | |
| `io.open` | ![not implemented](not-implemented.svg) | |
| `io.output` | ![partial](partial.svg) | access default output file only, no `open` |
| `io.popen` | ![not implemented](not-implemented.svg) | |
| `io.read` | ![partial](partial.svg) | `file:read` not implemented |
| `io.stdin` | ![complete](complete.svg) | |
| `io.stderr` | ![complete](complete.svg) | |
| `io.stdout` | ![complete](complete.svg) | |
| `io.tmpfile` | ![not implemented](not-implemented.svg) | |
| `io.type` | ![complete](complete.svg) | |
| `io.write` | ![complete](complete.svg) | |
| `file:close` | ![complete](complete.svg) | untested |
| `file:flush` | ![complete](complete.svg) | untested |
| `file:lines` | ![not implemented](not-implemented.svg) | |
| `file:read` | ![not implemented](not-implemented.svg) | |
| `file:seek` | ![complete](complete.svg) | untested |
| `file:setvbuf` | ![not implemented](not-implemented.svg) | |
| `file:write` | ![complete](complete.svg) | |
| `os.clock` | ![not implemented](not-implemented.svg) | |
| `os.date` | ![not implemented](not-implemented.svg) | |
| `os.difftime` | ![not implemented](not-implemented.svg) | |
| `os.execute` | ![not implemented](not-implemented.svg) | |
| `os.exit` | ![not implemented](not-implemented.svg) | |
| `os.getenv` | ![not implemented](not-implemented.svg) | |
| `os.remove` | ![not implemented](not-implemented.svg) | |
| `os.rename` | ![not implemented](not-implemented.svg) | |
| `os.setlocale` | ![not implemented](not-implemented.svg) | |
| `os.time` | ![not implemented](not-implemented.svg) | |
| `os.tmpname` | ![not implemented](not-implemented.svg) | |
| `debug.debug` | ![not implemented](not-implemented.svg) | unlikely to be implemented |
| `debug.gethook` | ![not implemented](not-implemented.svg) | instruction hooks incompatible |
| `debug.getinfo` | ![not implemented](not-implemented.svg) | incompatible |
| `debug.getlocal` | ![not implemented](not-implemented.svg) | incompatible |
| `debug.getmetatable` | ![complete](complete.svg) | |
| `debug.getregistry` | ![not implemented](not-implemented.svg) | |
| `debug.getupvalue` | ![complete](complete.svg) | |
| `debug.getuservalue` | ![complete](complete.svg) | |
| `debug.sethook` | ![not implemented](not-implemented.svg) | instruction hooks incompatible |
| `debug.setlocal` | ![not implemented](not-implemented.svg) | incompatible |
| `debug.setmetatable` | ![complete](complete.svg) | |
| `debug.setupvalue` | ![complete](complete.svg) | |
| `debug.setuservalue` | ![complete](complete.svg) | |
| `debug.traceback` | ![not implemented](not-implemented.svg) | |
| `debug.upvalueid` | ![complete](complete.svg) | |
| `debug.upvaluejoin` | ![complete](complete.svg) | |