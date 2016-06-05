package net.sandius.rembulan.test

object TableLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (TableContext) {

    about ("table.pack") {

      program ("""return table.pack(xx, yy, zz).n""") succeedsWith (3)
      program ("""return #table.pack(3, 2, 1, 0, -1)""") succeedsWith (5)

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

      in (FullContext) {

        program ("""return table.unpack("hello")""") succeedsWith (null, null, null, null, null)
        program ("""return table.unpack("1","2","3")""") succeedsWith (null, null)

      }

      about ("__len metamethod") {

        program ("""return table.unpack(setmetatable({}, { __len = 3 }))""") failsWith "attempt to call a number value"

        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1, 1)""") succeedsWith (null, null, null)
        program ("""return table.unpack(setmetatable({}, { __len = function() error("boom") end }), -1)""") failsWithLuaError "boom"

        program ("""return table.unpack(setmetatable({}, { __len = function() return 2 end }), -1)""") succeedsWith (null, null, null, null)
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
