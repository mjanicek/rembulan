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

package net.sandius.rembulan.test.fragments

import net.sandius.rembulan.test.{FragmentBundle, FragmentExpectations, OneLiners}
import net.sandius.rembulan.{LuaFormat, Table}

object TableLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (TableContext) {

    about ("table.concat") {

      program ("""return table.concat()""") failsWith "bad argument #1 to 'concat' (table expected, got no value)"
      program ("""return table.concat(nil)""") failsWith "bad argument #1 to 'concat' (table expected, got nil)"
      program ("""return table.concat({}, nil)""") succeedsWith ("")
      program ("""return table.concat({}, false)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"
      program ("""return table.concat({}, true, true)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"
      program ("""return table.concat({}, true, true, false)""") failsWith "bad argument #2 to 'concat' (string expected, got boolean)"

      in (FullContext) {
        // strings have the __index metamethod, but concat doesn't care about them
        program ("""return table.concat("hello", " ")""") failsWith "bad argument #1 to 'concat' (table expected, got string)"
      }

      program ("""return table.concat({}, "", true, false)""") failsWith "bad argument #3 to 'concat' (number expected, got boolean)"
      program ("""return table.concat({}, "", nil, false)""") failsWith "bad argument #4 to 'concat' (number expected, got boolean)"
      program ("""return table.concat({}, nil, nil, nil)""") succeedsWith ("")

      program ("""return table.concat({3, 2, 1})""") succeedsWith ("321")
      program ("""return table.concat({3, [4]=0, 2, 1})""") succeedsWith ("3210")
      program ("""return table.concat({3, x=0, 2, 1})""") succeedsWith ("321")

      program ("""return table.concat({[-1]=1, [-2]=2, [0]=0, [2]=-2, [1.0]=-1}, "", -2)""") succeedsWith ("210-1-2")
      program ("""return table.concat({[-1]=1, [-2]=2, [0]=0, [2]=-2, [1.0]=-1})""") succeedsWith ("-1-2")

      program ("""return table.concat({})""") succeedsWith ("")
      program ("""return table.concat({}, "BOO")""") succeedsWith ("")

      program ("""return table.concat({}, "", 50)""") succeedsWith ("")
      program ("""return table.concat({}, "", 0)""") failsWith "invalid value (nil) at index 0 in table for 'concat'"
      program ("""return table.concat({}, "", -1)""") failsWith "invalid value (nil) at index -1 in table for 'concat'"
      program ("""return table.concat({}, "", 1, 20)""") failsWith "invalid value (nil) at index 1 in table for 'concat'"

      program ("""return table.concat({1, 2}, "", 1, 1)""") succeedsWith ("1")
      program ("""return table.concat({1, 2, 3, 4}, "", -2, 3)""") failsWith "invalid value (nil) at index -2 in table for 'concat'"

      program ("""return table.concat({"hello", "world", 0})""") succeedsWith ("helloworld0")
      program ("""return table.concat({"hello", "world", 0}, " ")""") succeedsWith ("hello world 0")

      program ("""return table.concat({"a", 1, "b", 2, "c", 3}, nil)""") succeedsWith ("a1b2c3")
      program ("""return table.concat({"a", 1, "b"}, -0.0)""") succeedsWith ("a-0.01-0.0b")

      program ("""return table.concat({"a", 1, {}})""") failsWith "invalid value (table) at index 3 in table for 'concat'"
      program ("""return table.concat({{}, {}})""") failsWith "invalid value (table) at index 1 in table for 'concat'"

      in (FullContext) {
        program ("""return table.concat({io.stdout})""") failsWith "invalid value (userdata) at index 1 in table for 'concat'"
      }

      // concat uses the __index metamethod on the concatenated table
      program (
        """local mt = {__index = function(t, k) return k end}
          |return table.concat(setmetatable({}, mt), " ", 3, 5)
        """) succeedsWith ("3 4 5")

      // concat uses the __len metamethod on the concatenated table
      program (
        """local mt = {__len = function() return 2 end}
          |return table.concat(setmetatable({5, 4, 3, 2, 1, 0}, mt), " ")
        """) succeedsWith ("5 4")

      program ("""local mt = {__len = 10}; return table.concat(setmetatable({}, mt))""") failsWith "attempt to call a number value"
      program ("""local mt = {__len = function() return "x" end}; return table.concat(setmetatable({}, mt))""") failsWith "object length is not an integer"
      program ("""local mt = {__len = function() return "2" end}; return table.concat(setmetatable({"a", "b", "c"}, mt))""") succeedsWith "ab"
      program (
        """local mt = {__len = function() return "3.0" end}
          |return table.concat(setmetatable({"a", "b", "c"}, mt))
        """) succeedsWith "abc"

      // length is retrieved before the 2nd argument to concat
      program ("""return table.concat(setmetatable({}, {__len = function() error("BOOM") end}), "", 1, true)""") failsWith "BOOM"
      program ("""return table.concat(setmetatable({}, {__len = function() error("BOOM") end}), true, true)""") failsWith "BOOM"
      program ("""return table.concat(setmetatable({}, {__len = function() error("BOOM") end}), false)""") failsWith "BOOM"

      // concat uses the __index and __len metamethods on the concatenated table
      program (
        """local mt = {__index = function(t, k) return k end; __len = function() return 2 end}
          |return table.concat(setmetatable({}, mt), "_", -2)
        """) succeedsWith ("-2_-1_0_1_2")

      // concat does not use the __tostring metamethod of table elements
      program (
        """local mt = {__tostring = function() return "{}" end}
          |return table.concat({"a", 1, setmetatable({}, mt)})
        """) failsWith "invalid value (table) at index 3 in table for 'concat'"

      // concat does not use the __concat metamethod of table elements
      program (
        """local mt = {__concat = function() return "{}" end}
          |return table.concat({"a", 1, setmetatable({}, mt)})
        """) failsWith "invalid value (table) at index 3 in table for 'concat'"

    }

    about ("table.insert") {

      program ("table.insert()") failsWith "bad argument #1 to 'insert' (table expected, got no value)"
      program ("table.insert(nil)") failsWith "bad argument #1 to 'insert' (table expected, got nil)"
      program ("table.insert(123)") failsWith "bad argument #1 to 'insert' (table expected, got number)"

      program ("table.insert({})") failsWith "wrong number of arguments to 'insert'"
      program ("table.insert({}, false, false)") failsWith "bad argument #2 to 'insert' (number expected, got boolean)"
      program ("table.insert({}, 1, 2, 3)") failsWith "wrong number of arguments to 'insert'"
      program ("table.insert({}, 1, 2, nil)") failsWith "wrong number of arguments to 'insert'"

      program ("""local t = {}; table.insert(t, "a"); return #t, t[1]""") succeedsWith (1, "a")
      program ("""local t = {"a"}; table.insert(t, "b"); return #t, t[1], t[2]""") succeedsWith (2, "a", "b")
      program ("""local t = {"a"}; table.insert(t, 1, "b"); return #t, t[1], t[2]""") succeedsWith (2, "b", "a")
      program ("""local t = {"a"}; table.insert(t, "1", "b"); return #t, t[1], t[2]""") succeedsWith (2, "b", "a")
      program ("""local t = {"a"}; table.insert(t, "1.0", "b"); return #t, t[1], t[2]""") succeedsWith (2, "b", "a")

      program ("""table.insert({}, 0, "x")""") failsWith "bad argument #2 to 'insert' (position out of bounds)"
      program ("""table.insert({}, 0, nil)""") failsWith "bad argument #2 to 'insert' (position out of bounds)"
      program ("""table.insert({}, 10, "x")""") failsWith "bad argument #2 to 'insert' (position out of bounds)"
      program ("""table.insert({}, 10, nil)""") failsWith "bad argument #2 to 'insert' (position out of bounds)"

      // __len metamethod

      program ("""table.insert(setmetatable({}, {__len = function() error("BOOM") end}), 10)""") failsWith "BOOM"
      program ("""table.insert(setmetatable({}, {__len = function() return 1.1 end}), 10)""") failsWith "object length is not an integer"

      // length is queried before processing the rest of the arguments
      program ("""table.insert(setmetatable({}, {__len = function() error("BOOM") end}), false, false)""") failsWith "BOOM"
      program ("""table.insert(setmetatable({}, {__len = function() error("BOOM") end}), 1, 2, nil)""") failsWith "BOOM"

      program ("""local t = setmetatable({}, {__len = function() return -1 end}); table.insert(t, "x"); return #t, t[0]""") succeedsWith (-1, "x")
      program ("""local t = setmetatable({}, {__len = function() return "-1" end}); table.insert(t, "x"); return #t, t[0]""") succeedsWith ("-1", "x")
      program ("""local t = setmetatable({}, {__len = function() return "-0.0" end}); table.insert(t, "x"); return #t, t[1]""") succeedsWith ("-0.0", "x")
      program ("""table.insert(setmetatable({}, {__len = function() return -1 end}), 0, "x")""") failsWith "bad argument #2 to 'insert' (position out of bounds)"
      program ("""table.insert(setmetatable({}, {__len = function() return 2 end}), 4, "x")""") failsWith "bad argument #2 to 'insert' (position out of bounds)"
      program ("""table.insert(setmetatable({}, {__len = function() error("Boom.") end}), 0, "x")""") failsWith "Boom."

      // __index and __newindex

      program ("""local t = setmetatable({"x"}, {__index = error}); table.insert(t, 1, "y"); return #t, t[1], t[2]""") succeedsWith (2, "y", "x")

      program ("""table.insert(setmetatable({"x"}, {__newindex = function(t, k, v) error(k..v) end}), "y")""") failsWith "2y"
      program ("""table.insert(setmetatable({"x"}, {__newindex = function(t, k, v) error(k..v) end}), 1, "y")""") failsWith "2x"

      program (
        """-- meta #1
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |table.insert(t, "a")
          |table.insert(t, "b")
          |table.insert(t, 1, "c")
          |t[2] = nil
          |table.insert(t, 1, "d")
          |return ks, #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith ("[1a][2b][3b][4b]{2}[2c]", 5, "d", "c", null, "b", null)

      program (
        """-- meta #2
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |table.insert(t, "a")
          |table.insert(t, "b")
          |table.insert(t, 1, "c")
          |n = n - 1
          |rawset(t, 1, nil)
          |table.insert(t, 1, "d")
          |return ks, #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith ("[1a][2b][3b]{1}[1d]", 3, "d", null, "a", null, null)

      program (
        """-- meta #3
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |table.insert(t, "a")
          |table.insert(t, "b")
          |table.insert(t, 1, "c")
          |n = n - 1
          |rawset(t, 2, nil)
          |table.insert(t, 1, "d")
          |return ks, #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith ("[1a][2b][3b]{2}[2c]", 3, "d", "c", null, null, null)

      program (
        """-- meta #4
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |table.insert(t, "a")
          |table.insert(t, "b")
          |table.insert(t, 1, "c")
          |n = n - 1
          |rawset(t, 3, nil)
          |table.insert(t, 1, "d")
          |return ks, #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith ("[1a][2b][3b][3a]", 3, "d", "c", "a", null, null)

      program (
        """-- meta #5
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |table.insert(t, "a")
          |table.insert(t, "b")
          |table.insert(t, 1, "c")
          |n = n - 1
          |rawset(t, 4, nil)
          |table.insert(t, 1, "d")
          |return ks, #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith ("[1a][2b][3b]", 2, "d", "c", "a", null, null)

    }

    about ("table.move") {

      program ("""table.move()""") failsWith "bad argument #2 to 'move' (number expected, got no value)"
      program ("""table.move(nil)""") failsWith "bad argument #2 to 'move' (number expected, got no value)"
      program ("""table.move(nil, nil)""") failsWith "bad argument #2 to 'move' (number expected, got nil)"
      program ("""table.move(nil, "x")""") failsWith "bad argument #2 to 'move' (number expected, got string)"
      program ("""table.move(nil, 1)""") failsWith "bad argument #3 to 'move' (number expected, got no value)"
      program ("""table.move(nil, 1, 1)""") failsWith "bad argument #4 to 'move' (number expected, got no value)"
      program ("""table.move(nil, 1, 1, 1)""") failsWith "bad argument #1 to 'move' (table expected, got nil)"
      program ("""table.move({}, 1, 1, 1, "x")""") failsWith "bad argument #5 to 'move' (table expected, got string)"
      program ("""table.move("x", 1, 1, 1, "y")""") failsWith "bad argument #5 to 'move' (table expected, got string)"

      program ("""table.move({}, 1 << 63, (1 << 63) - 1, 0)""") failsWith "bad argument #3 to 'move' (too many elements to move)"
      program ("""table.move({}, 0, (1 << 63) - 1, 0)""") failsWith "bad argument #3 to 'move' (too many elements to move)"
      program ("""table.move({}, 1 << 63, -1, 0)""") failsWith "bad argument #3 to 'move' (too many elements to move)"

      program ("""return table.move({}, 1, 1, 1, nil)""") succeedsWith (classOf[Table])

      program ("""local t = {}; local u = table.move(t, 1, 1, 1); return t == u""") succeedsWith (true)

      program ("""local a = {}; return table.move({10,20,30}, 1, 0, 3, a) == a""") succeedsWith (true)

      program (
        """local t = table.move({"a", "b", "c", "d"}, 1, 3, 4)
          |return #t, t[1], t[2], t[3], t[4], t[5], t[6]
        """
      ) succeedsWith (6, "a", "b", "c", "a", "b", "c")

      program (
        """local t = table.move({"a", "b", "c", "d"}, 1, 4, 2)
          |return #t, t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith (5, "a", "a", "b", "c", "d")

      program (
        """local t = table.move({"a", "b", "c", "d", "e"}, 2, 5, 0)
          |return #t, t[0], t[1], t[2], t[3], t[4], t[5]
        """
      ) succeedsWith (5, "b", "c", "d", "e", "d", "e")

      program (
        """-- same dest, no range overlap
          |local log = ""
          |local mt = {
          |  __index = function (t, k) log = log.."["..tostring(k).."]" end,
          |  __newindex = function (t, k, v) log = log.."{"..tostring(k).."}" end
          |}
          |
          |table.move(setmetatable({}, mt), 1, 3, 4)
          |return log
        """
      ) succeedsWith ("[1]{4}[2]{5}[3]{6}")

      program (
        """-- same dest, range overlap
          |local log = ""
          |local mt = {
          |  __index = function (t, k) log = log.."["..tostring(k).."]" end,
          |  __newindex = function (t, k, v) log = log.."{"..tostring(k).."}" end
          |}
          |
          |table.move(setmetatable({}, mt), 1, 3, 2)
          |return log
        """
      ) succeedsWith ("[3]{4}[2]{3}[1]{2}")

      program (
        """-- same dest, full overlap
          |local log = ""
          |local mt = {
          |  __index = function (t, k) log = log.."["..tostring(k).."]" end,
          |  __newindex = function (t, k, v) log = log.."{"..tostring(k).."}" end
          |}
          |
          |table.move(setmetatable({}, mt), 1, 3, 1)
          |return log
        """
      ) succeedsWith ("[1]{1}[2]{2}[3]{3}")

      program (
        """-- other dest, range overlap
          |local log = ""
          |local mt = {
          |  __index = function (t, k) log = log.."["..tostring(k).."]" end,
          |  __newindex = function (t, k, v) log = log.."{"..tostring(k).."}" end
          |}
          |
          |table.move(setmetatable({}, mt), 1, 3, 2, setmetatable({}, mt))
          |return log
        """
      ) succeedsWith ("[1]{2}[2]{3}[3]{4}")

      program (
        """-- other dest, no range overlap
          |local log = ""
          |local mt = {
          |  __index = function (t, k) log = log.."["..tostring(k).."]" end,
          |  __newindex = function (t, k, v) log = log.."{"..tostring(k).."}" end
          |}
          |
          |table.move(setmetatable({}, mt), 1, 3, 4)
          |return log
        """
      ) succeedsWith ("[1]{4}[2]{5}[3]{6}")

    }

    about ("table.pack") {

      program ("""return table.pack(xx, yy, zz).n""") succeedsWith (3)
      program ("""return #table.pack(3, 2, 1, 0, -1)""") succeedsWith (5)

    }

    about ("table.remove") {

      program ("""table.remove()""") failsWith "bad argument #1 to 'remove' (table expected, got no value)"
      program ("""table.remove("")""") failsWith "bad argument #1 to 'remove' (table expected, got string)"

      program ("""return table.remove({})""") succeedsWith (null)
      program ("""return table.remove({}, 1)""") succeedsWith (null)
      program ("""return table.remove({}, 0)""") succeedsWith (null)
      program ("""return table.remove({}, nil)""") succeedsWith (null)

      program ("""return table.remove({}, nil, "extra", "args", "ignored")""") succeedsWith (null)

      program ("""return table.remove({}, "1")""") succeedsWith (null)
      program ("""return table.remove({}, "1.0")""") succeedsWith (null)
      program ("""return table.remove({}, -0.0)""") succeedsWith (null)
      program ("""return table.remove({}, "-0.0")""") succeedsWith (null)

      program ("""table.remove({}, false)""") failsWith "bad argument #2 to 'remove' (number expected, got boolean)"
      program ("""table.remove({}, "x")""") failsWith "bad argument #2 to 'remove' (number expected, got string)"

      program ("""table.remove({}, 2)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"
      program ("""table.remove({}, -1)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"

      program ("""local t = {42}; local x = table.remove(t); return x, #t, t[1]""") succeedsWith (42, 0, null)
      program ("""return table.remove({42}, 0)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"
      program ("""local t = {42}; local x = table.remove(t, 1); return x, #t, t[1]""") succeedsWith (42, 0, null)
      program ("""local t = {42}; local x = table.remove(t, 2); return x, #t, t[1]""") succeedsWith (null, 1, 42)

      program ("""local t = {"a", "b", "c", "d"}; local x = table.remove(t); return x, #t, t[1], t[2], t[3], t[4]""") succeedsWith ("d", 3, "a", "b", "c", null)
      program ("""local t = {"a", "b", "c", "d"}; local x = table.remove(t, 1); return x, t[1], t[2], t[3], t[4]""") succeedsWith ("a", "b", "c", "d", null)
      program ("""local t = {"a", "b", "c", "d"}; local x = table.remove(t, 2); return x, t[1], t[2], t[3], t[4]""") succeedsWith ("b", "a", "c", "d", null)
      program ("""local t = {"a", "b", "c", "d"}; local x = table.remove(t, 3); return x, t[1], t[2], t[3], t[4]""") succeedsWith ("c", "a", "b", "d", null)
      program ("""local t = {"a", "b", "c", "d"}; local x = table.remove(t, 4); return x, t[1], t[2], t[3], t[4]""") succeedsWith ("d", "a", "b", "c", null)

      // the __len metamethod

      // __len is always consulted when defined
      program("""local t = setmetatable({}, {__len=function() error("Boom") end}); return table.remove(t)""") failsWith "Boom"
      program("""local t = setmetatable({"a"}, {__len=function() error("Boom") end}); return table.remove(t, 0)""") failsWith "Boom"
      program("""local t = setmetatable({"a"}, {__len=function() error("Boom") end}); return table.remove(t, 1)""") failsWith "Boom"
      program("""local t = setmetatable({"a"}, {__len=function() error("Boom") end}); return table.remove(t, 2)""") failsWith "Boom"

      // no shift variants (just erase the element at pos)
      program("""local t = setmetatable({[0]="x", "a", "b"}, {__len=function() return 0 end}); return table.remove(t, 0), #t, t[0], t[1], t[2]""") succeedsWith ("x", 0, null, "a", "b")
      program("""local t = setmetatable({"a", "b", "c", "d"}, {__len=function() return 1 end}); return table.remove(t, 2), #t, t[1], t[2], t[3], t[4]""") succeedsWith ("b", 1, "a", null, "c", "d")

      // length is queried before the position is processed
      program("""local t = setmetatable({"a"}, {__len=function() error("Boom") end}); table.remove(t, false)""") failsWith "Boom"

      program("""local t = setmetatable({}, {__len=function() return -1 end}); return table.remove(t)""") succeedsWith (null)
      program("""local t = setmetatable({}, {__len=function() return -1 end}); return table.remove(t, -2)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"
      program("""local t = setmetatable({}, {__len=function() return -1 end}); return table.remove(t, -1)""") succeedsWith(null)
      program("""local t = setmetatable({}, {__len=function() return -1 end}); return table.remove(t, 0)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"
      program("""local t = setmetatable({}, {__len=function() return -1 end}); return table.remove(t, 1)""") failsWith "bad argument #2 to 'remove' (position out of bounds)"


      // __index and __newindex

      // no shift variants (just erase the element at pos); using the __index metamethod
      program("""local t = setmetatable({[0]="x", "a", "b"}, {__len=function() return 0 end; __index=rawget}); return table.remove(t, 0), #t, t[0], t[1], t[2]""") succeedsWith ("x", 0, null, "a", "b")
      program("""local t = setmetatable({"a", "b", "c", "d"}, {__len=function() return 1 end; __index=rawget}); return table.remove(t, 2), #t, t[1], t[2], t[3], t[4]""") succeedsWith ("b", 1, "a", null, "c", "d")

      program ("""local t = setmetatable({"x"}, {__index = error}); return table.remove(t)""") succeedsWith ("x")
      program ("""local t = setmetatable({nil, "x"}, {__len = function() return 2 end; __index = function(t,k) error(tostring(k)) end}); return table.remove(t, 1)""") failsWith ("1")
      program ("""local t = setmetatable({"x", nil}, {__index = function(t,k) error(tostring(k)) end}); return table.remove(t, 2)""") failsWith ("2")

      program ("""local t = setmetatable({nil, "x"}, {__len = function() return 2 end; __newindex = function(t,k,v) error(tostring(k)..tostring(v)) end}); return table.remove(t, 2)""") succeedsWith ("x")
      program ("""local t = setmetatable({nil, "x"}, {__len = function() return 2 end; __newindex = function(t,k,v) error(tostring(k)..tostring(v)) end}); return table.remove(t, 1)""") failsWith ("1x")

      program (
        """-- meta #1
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |t[1] = "a"
          |t[3] = "c"
          |t[2] = "b"
          |t[4] = "d"
          |n = n - 1
          |rawset(t, 3, nil)
          |local x = table.remove(t, 2)
          |return ks, #t, x, t[1], t[2], t[3], t[4]
        """
      ) succeedsWith ("[1a][3c][2b][4d]{3}[3nil]", 4, "b", "a", null, null, "d")

      program (
        """-- meta #2
          |local ks = ""
          |local n = 0
          |local t = setmetatable({}, {
          |  __len = function() return n end;
          |  __newindex = function(t, k, v) n = n + 1; ks = ks.."["..k..tostring(v).."]"; rawset(t, k, v) end;
          |  __index = function(t, k) ks = ks.."{"..k.."}"; return rawget(t, k) end
          |})
          |
          |t[1] = "a"
          |t[3] = "c"
          |t[2] = "b"
          |t[4] = "d"
          |n = n - 1
          |rawset(t, 3, nil)
          |local x = table.remove(t, 2)
          |local y = table.remove(t)
          |return ks, #t, x, y, t[1], t[2], t[3], t[4]
        """
      ) succeedsWith ("[1a][3c][2b][4d]{3}[3nil]", 4, "b", "d", "a", null, null, null)

    }

    about ("table.sort") {

      program ("""table.sort()""") failsWith "bad argument #1 to 'sort' (table expected, got no value)"
      program ("""table.sort(nil)""") failsWith "bad argument #1 to 'sort' (table expected, got nil)"
      program ("""table.sort("hello")""") failsWith "bad argument #1 to 'sort' (table expected, got string)"

      // comparator defined, but not called
      program ("""table.sort({}, nil)""") succeedsWith ()
      program ("""table.sort({}, 42)""") succeedsWith ()

      program ("""local t = {2, 1}; table.sort(t); return t[1], t[2]""") succeedsWith (1, 2)
      program ("""local t = {2, 1}; table.sort(t, nil); return t[1], t[2]""") succeedsWith (1, 2)

      program ("""table.sort({3, 2}, 1)""") failsWith "bad argument #2 to 'sort' (function expected, got number)"

      program ("""table.sort({true, false})""") failsWith "attempt to compare two boolean values"
      program ("""table.sort({1, false})""") failsWith "attempt to compare "<<"boolean with number"

      def doSortExplicit(vals: Seq[Any], exp: Seq[Any], comp: Option[String]): Unit = {
        val vs = vals map {
          case s: String => LuaFormat.escape(s)
          case other => other
        }
        val ctor = vs.mkString("{", ",", "}")
        val getter = ((1 to vals.size) map { "t[" + _.toString + "]" }).mkString(", ")

        val compStr = comp match {
          case Some(s) => ", " + s
          case None => ""
        }

        program("local t = " + ctor + "; table.sort(t" + compStr + "); return " + getter).succeedsWith(exp:_*)
      }

      def doSort(vals: Any*): Unit = {
        val exp = vals sortWith {
          case (a: String, b: String) => a.compareTo(b) < 0
          case (a: Number, b: Number) => net.sandius.rembulan.Ordering.NUMERIC.lt(a, b)
          case _ => false
        }
        doSortExplicit(vals, exp, None)
      }

      doSort("a", "b", "c", "d")
      doSort("d", "c", "b", "a")

      doSort((50 to 120):_*)
      doSort(30.to(4, -2):_*)
      doSort(1.1, 1.3, 1.0, 1.2, 2.0, 1.0)

//      doSortExplicit(Seq("hello", "hi", "hola"), Seq("hi", "hola", "hello"), Some("function(a, b) return #a < #b end"))

      doSort(3, 8, 5, 4, 6)

      program (
        """local t = {"one", "thirteen", "three", "four", "eleven"}
          |table.sort(t, function(a, b) return #a < #b end)
          |return t[1], t[2], t[3], t[4], t[5]
        """) succeedsWith ("one", "four", "three", "eleven", "thirteen")

      // full stops
      program (
        """-- total mt access
          |local t = {"one", "thirteen", "three", "four", "eleven"}
          |local mt = {
          |  __len = function (t) return rawlen(t) end,
          |  __index = function (t, k) return rawget(t, k) end,
          |  __newindex = function (t, k, v) return rawset(t, k, v) end
          |}
          |table.sort(setmetatable(t, mt), function(a, b) return #a < #b end)
          |return t[1], t[2], t[3], t[4], t[5]
        """) succeedsWith ("one", "four", "three", "eleven", "thirteen")

      // proxy
      program (
        """-- proxy
          |local function proxy(t)
          |  return setmetatable({}, {
          |    __len = function (_t) return #t end,
          |    __index = function (_t, k) return t[k] end,
          |    __newindex = function (_t, k, v) t[k] = v end
          |  })
          |end
          |
          |local t = {5, 3, 2, 6, 1, 4}
          |table.sort(proxy(t))
          |return t[1], t[2], t[3], t[4], t[5], t[6]
        """
      ) succeedsWith (1, 2, 3, 4, 5, 6)

      // comparator must be a function
      program ("""local f = setmetatable({}, {__call=function() return true end}); table.sort({2, 1}, f)""") failsWith "bad argument #2 to 'sort' (function expected, got table)"

      // invalid order function
      program ("""table.sort({1, 2, 3, 4}, function(a,b) return true end)""") failsWith "invalid order function for sorting"

      // PUC-Lua does not detect the invalid order function in this case, and neither do we
      program ("""table.sort({1, 2, 3, 4}, function(a,b) return false end)""") succeedsWith ()

    }

    about ("table.unpack") {

      program ("""return table.unpack()""") failsWith "attempt to get length of a nil value"
      program ("""return table.unpack(1)""") failsWith "attempt to get length of a number value"
      program ("""return table.unpack(1,2)""") failsWith "attempt to get length of a number value"
      program ("""return table.unpack(1,2,3)""") failsWith "attempt to index a number value"

      program ("""return table.unpack(1,2.3)""") failsWith "bad argument #2 to 'unpack' (number has no integer representation)"
      program ("""return table.unpack(1,"x")""") failsWith "bad argument #2 to 'unpack' (number expected, got string)"
      program ("""return table.unpack(1,2,"y")""") failsWith "bad argument #3 to 'unpack' (number expected, got string)"

      program ("""local x; return table.unpack(1,x)""") failsWith "attempt to get length of a number value"

      program ("""return table.unpack({1,2,3}, -2, 2)""") succeedsWith (null, null, null, 1, 2)
      program ("""return table.unpack({3,2,1}, 3, 3)""") succeedsWith (1)
      program ("""return table.unpack({3,2,1}, 0, 1)""") succeedsWith (null, 3)
      program ("""return table.unpack({3,2,1}, 0, -1)""") succeedsWith ()
      program ("""return table.unpack({3,2,1}, 10, 12)""") succeedsWith (null, null, null)

      program ("""return table.unpack({3,2,1,0,-1}, 2)""") succeedsWith (2, 1, 0, -1)
      program ("""return table.unpack({1,0,-1})""") succeedsWith (1, 0, -1)

      program ("""return table.unpack("nono")""") failsWith "attempt to index a string value"

      program ("""local maxi = (1 << 31) - 1; table.unpack({}, 0, maxi)""") failsWith "too many results to unpack"
      program ("""local maxi = (1 << 31) - 1; table.unpack({}, 1, maxi)""") failsWith "too many results to unpack"
      program ("""local maxI = (1 << 63) - 1; table.unpack({}, 0, maxI)""") failsWith "too many results to unpack"
      program ("""local maxI = (1 << 63) - 1; table.unpack({}, 1, maxI)""") failsWith "too many results to unpack"
      program ("""local mini, maxi = -(1 << 31), (1 << 31) - 1; table.unpack({}, mini, maxi)""") failsWith "too many results to unpack"
      program ("""local minI, maxI = 1 << 63, (1 << 63) - 1; table.unpack({}, minI, maxI)""") failsWith "too many results to unpack"

      // behaviour near math.maxinteger

      program (
        """local maxI = (1 << 63) - 1
          |local t = {[maxI - 1] = 12, [maxI] = 23}
          |return table.unpack(t, maxI - 1, maxI)
        """) succeedsWith (12, 23)

      program (
        """local maxI = (1 << 63) - 1
          |local t = setmetatable({}, {__index = function(t,k) return k end})
          |return table.unpack(t, maxI - 1, maxI)
        """) succeedsWith (Long.MaxValue - 1, Long.MaxValue)

      in (FullContext) {

        program ("""return table.unpack("hello")""") succeedsWith (null, null, null, null, null)
        program ("""return table.unpack("1","2","3")""") succeedsWith (null, null)

      }

      about ("__len metamethod") {

        program ("""return table.unpack(setmetatable({}, { __len = 3 }))""") failsWith "attempt to call a number value"

        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1, 1)""") succeedsWith (null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1)""") failsWithLuaError "boom"

        program ("""return table.unpack(setmetatable({}, { __len = function() return 2 end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "2" end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "2.0" end }), -1)""") succeedsWith (null, null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() return "boo" end }), 1)""") failsWith "object length is not an integer"

      }

      about ("__index metamethod") {

        program ("""return table.unpack(setmetatable({}, { __index = 3 }), 1, 2)""") failsWith "attempt to index a number value"

        program ("""local mt = { __index = function(t, k) local x = k or 0; return x * x end }
                   |return table.unpack(setmetatable({}, mt), -1, 3)
                 """) succeedsWith (1, 0, 1, 4, 9)

      }

      about ("__len and __index metamethods") {

        program ("""local mt = {
                   |  __index = function(t, k) local x = k or 0; return x * x end,
                   |  __len = function() return 5 end
                   |}
                   |return table.unpack(setmetatable({}, mt))
                 """) succeedsWith (1, 4, 9, 16, 25)

      }

    }

  }

}
