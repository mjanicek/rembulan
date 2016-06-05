package net.sandius.rembulan.test

object TableLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (TableContext) {

    about ("table.pack") {

      program ("""return table.pack(xx, yy, zz).n""") succeedsWith (3)
      program ("""return #table.pack(3, 2, 1, 0, -1)""") succeedsWith (5)

    }

    about ("table.unpack") {

      program ("""return table.unpack({1,2,3}, -2, 2)""") succeedsWith (null, null, null, 1, 2)
      program ("""return table.unpack({3,2,1}, 3, 3)""") succeedsWith (1)
      program ("""return table.unpack({3,2,1}, 0, 1)""") succeedsWith (null, 3)
      program ("""return table.unpack({3,2,1}, 0, -1)""") succeedsWith ()
      program ("""return table.unpack({3,2,1}, 10, 12)""") succeedsWith (null, null, null)

      program ("""return table.unpack({3,2,1,0,-1}, 2)""") succeedsWith (2, 1, 0, -1)
      program ("""return table.unpack({1,0,-1})""") succeedsWith (1, 0, -1)


    }

  }

}
