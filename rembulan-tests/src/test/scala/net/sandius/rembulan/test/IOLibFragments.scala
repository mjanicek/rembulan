package net.sandius.rembulan.test

import net.sandius.rembulan.core.Userdata

object IOLibFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (IOContext) {

    about ("io.output") {

      program ("return io.output()") succeedsWith (classOf[Userdata])

    }

    about ("file:__tostring") {

      program ("return tostring(io.output())") succeedsWith stringStartingWith("file (0x")

    }

    about ("io.write") {

      // TODO: check what is written to the output

      program ("return io.write(1, 2)") succeedsWith (classOf[Userdata])
      program ("""return io.write({})""") failsWith "bad argument #"<<"1">>" to 'write' (string expected, got table)"

    }


  }

}
