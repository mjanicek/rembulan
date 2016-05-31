package net.sandius.rembulan.test

object MetatableFragments extends FragmentBundle with FragmentExpectations with OneLiners {

  in (BasicContext) {
    
    val TwoLevelMetatables = fragment("two-level metatables") {
      """function mtn(tab, what)
        |  local callmt = {}
        |  callmt.__call = function() return 'Hurray' end
        |
        |  local tgt = {}
        |  setmetatable(tgt, callmt)
        |
        |  local mt = {}
        |  mt[what] = tgt
        |
        |  setmetatable(tab, mt)
        |
        |  return tab
        |end
        |
        |t = {}
        |mtn(t, '__add')
        |
        |return t + 2
      """
    }
    TwoLevelMetatables in thisContext succeedsWith ("Hurray")
  
    val SingleLevelMetamethodWithUpvalue = fragment("single level metamethod with an upvalue") {
      """function mtn(tab, what, v)
        |  local mt = {}
        |  mt[what] = function() return v end
        |
        |  setmetatable(tab, mt)
        |
        |  return tab
        |end
        |
        |t = {}
        |mtn(t, '__add', 'Hello')
        |
        |return t + 0
      """
    }
    SingleLevelMetamethodWithUpvalue in thisContext succeedsWith ("Hello")
  
    val MetamethodReturningNothing = fragment("metamethod returning nothing") {
      """function mtn(tab, what)
        |  local mt = {}
        |  mt[what] = function() end
        |  setmetatable(tab, mt)
        |  return tab
        |end
        |
        |t = {}
        |mtn(t, '__add')
        |
        |return t + 0
      """
    }
    MetamethodReturningNothing in thisContext succeedsWith (null)
  
    val CallMetamethodReturningTwoResults = fragment("metamethod for __call returning two results") {
      """function mtn(tab, what)
        |  local mt = {}
        |  mt[what] = function() return 'a', 'b' end
        |  setmetatable(tab, mt)
        |  return tab
        |end
        |
        |t = {}
        |mtn(t, '__call')
        |
        |return t()
      """
    }
    CallMetamethodReturningTwoResults in thisContext succeedsWith ("a", "b")
  
    val AddMetamethodReturningTwoResults = fragment("metamethod for __add returning two results") {
      """function mtn(tab, what)
        |  local mt = {}
        |  mt[what] = function() return 'a', 'b' end
        |  setmetatable(tab, mt)
        |  return tab
        |end
        |
        |t = {}
        |mtn(t, '__add')
        |
        |return t + 0
      """
    }
    AddMetamethodReturningTwoResults in thisContext succeedsWith ("a")

    sealed trait Associative
    object Associative {
      case object Left extends Associative {
        override def toString = "left associative"
      }
      case object Right extends Associative {
        override def toString = "right associative"
      }
    }
    
    val binaryMts = {
      import Associative._
      Seq(
        "__add"    -> ("+"  -> Left),
        "__sub"    -> ("-"  -> Left),
        "__mul"    -> ("*"  -> Left),
        "__div"    -> ("/"  -> Left),
        "__mod"    -> ("%"  -> Left),
        "__pow"    -> ("^"  -> Right),
        "__idiv"   -> ("//" -> Left),

        "__band"   -> ("&"  -> Left),
        "__bor"    -> ("|"  -> Left),
        "__bxor"   -> ("~"  -> Left),
        "__shl"    -> ("<<" -> Left),
        "__shr"    -> (">>" -> Left),

        "__concat" -> (".." -> Right)
      )
    }
  
    for ((mtn, (op, assoc)) <- binaryMts) {
      val MetamethodCalled = fragment("metamethod " + mtn + " is called for binary '" + op + "'") {
        s"""t = {}
           |local mt = {}
           |mt.$mtn = function() return 'OK' end
           |setmetatable(t, mt)
           |
           |return t $op 0
         """
      }
      MetamethodCalled in thisContext succeedsWith ("OK")
  
      val MetamethodAssociativity = fragment("metamethod " + mtn + " is " + assoc) {
        s"""t = {}
           |local mt = {}
           |mt.$mtn = function(a, b) return type(a)..' '..type(b) end
           |setmetatable(t, mt)
           |
           |return t $op 0 $op t
         """
      }
      MetamethodAssociativity in thisContext succeedsWith (assoc match {
        case Associative.Left => "string table"
        case Associative.Right => "table string"
      })
  
    }
  
    val binaryBooleanMts = Seq(
      "__lt" -> ("<",  Associative.Left, true),
      "__lt" -> (">",  Associative.Left, false),
      "__le" -> ("<=", Associative.Left, true),
      "__le" -> (">=", Associative.Left, false)
    )
  
    for ((mtn, (op, assoc, assocTestExpect)) <- binaryBooleanMts) {
  
      val MetamethodCalled = fragment("metamethod " + mtn + " is called for binary '" + op + "'") {
        s"""t = {}
           |out = nil
           |local mt = {}
           |mt.$mtn = function()
           |  out = 'OK'
           |  return true
           |end
           |setmetatable(t, mt)
           |
           |return t $op 0, out
         """
      }
      MetamethodCalled in thisContext succeedsWith (true, "OK")

      val MetamethodAssociativity = fragment("metamethod " + mtn + " (binary '" + op + "') is " + assoc) {
        s"""t = {}
           |called = 0
           |local mt = {}
           |mt.$mtn = function(a, b)
           |  called = called + 1
           |  return type(a) < type(b)
           |end
           |setmetatable(t, mt)
           |
           |return t $op 0 $op t, called
         """
      }
      MetamethodAssociativity in thisContext succeedsWith (assocTestExpect, 2)

    }
  
    // TODO: test that boolean metamethods always return a boolean
  
    // TODO: test that <= falls back to __lt if __le is not present
  
    // TODO: generalise this to *negative* test cases
  
    val EqMetamethodIsProcessedCorrectly = fragment("metamethod __eq is processed correctly") {
      s"""f = function(...)
         |  print 'f'
         |  return true
         |end
         |
         |g = function(...)
         |   print 'g'
         |   return true
         | end
         |
         |t = {}
         |u = {}
         |v = {}
         |
         |mt = {}
         |mu = {}
         |mv = {}
         |
         |mt.__eq = g
         |mu.__eq = f
         |mv.__eq = f
         |
         |setmetatable(t, mt)
         |setmetatable(u, mv)
         |setmetatable(v, mu)
         |
         |return f == g, t == u, t == v, u == v, u == t, v == t, v == u
       """
    }
    EqMetamethodIsProcessedCorrectly in thisContext succeedsWith (false, true, true, true, true, true, true)
//    EqMetamethodIsProcessedCorrectly in thisContext prints ("g\ng\nf\nf\nf\nf\n")  // FIXME

    // TODO: test associativity/order of calls for __eq
  
    val unaryMts = Seq(
      "__unm"  -> "-",
      "__bnot" -> "~",
      "__len"  -> "#"
    )
  
    for ((mtn, op) <- unaryMts) {
      val MetamethodCalled = fragment("metamethod " + mtn + " is called for unary '" + op + "'") {
        s"""t = {}
           |local mt = {}
           |mt.$mtn = function() return 'OK' end
           |setmetatable(t, mt)
           |
           |return ${op}t
         """
      }
      MetamethodCalled in thisContext succeedsWith ("OK")
    }
    
  }
  
}
