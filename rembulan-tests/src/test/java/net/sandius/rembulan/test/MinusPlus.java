package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Closure;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.impl.AbstractFunc0;
import net.sandius.rembulan.core.impl.AbstractFunc1;
import net.sandius.rembulan.util.Ptr;

/*
function ()
    local function plusOne(x)
        return x + 1
    end

    local function unaryMinus(x)
        return -x
    end

    return unaryMinus(plusOne(0))
end
*/

/*
function <t2.lua:1,12> (9 instructions at 0x7fcec84046e0)
0 params, 5 slots, 0 upvalues, 2 locals, 1 constant, 2 functions
        1       [5]     CLOSURE         0 0     ; 0x7fcec84047f0
        2       [9]     CLOSURE         1 1     ; 0x7fcec8404640
        3       [11]    MOVE            2 1
        4       [11]    MOVE            3 0
        5       [11]    LOADK           4 -1    ; 0
        6       [11]    CALL            3 2 0
        7       [11]    TAILCALL        2 0 0
        8       [11]    RETURN          2 0
        9       [12]    RETURN          0 1
constants (1) for 0x7fcec84046e0:
        1       0
locals (2) for 0x7fcec84046e0:
        0       plusOne 2       10
        1       unaryMinus      3       10
upvalues (0) for 0x7fcec84046e0:
*/
public class MinusPlus extends AbstractFunc0 {

	public static final Long k_1 = Long.valueOf(0);

	protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
		// registers
		Object r_0, r_1, r_2, r_3, r_4;

		r_0 = null;
		r_1 = null;
		r_2 = null;
		r_3 = null;
		r_4 = null;

		// load registers
//		r_0 = objectStack.get(base + 0);
//		r_1 = objectStack.get(base + 1);
//		r_2 = objectStack.get(base + 2);
//		r_3 = objectStack.get(base + 3);
//		r_4 = objectStack.get(base + 4);

		try {
			switch (pc) {
				case 0:  // accounting block #0
					pc = 1;
					preemptionContext.withdraw(7);  // accounting the entire block already

				// Block #0: [1 .. 8], cost = 7

				case 1:  // CLOSURE 0 0
					r_0 = new p_0();

				case 2:  // CLOSURE 1 1
					r_1 = new p_1();

				case 3:  // MOVE 2 1
					r_2 = r_1;

				case 4:  // MOVE 3 0
					r_3 = r_0;

				case 5:  // LOADK 4 -1
					r_4 = k_1;

				case 6:  // CALL 3 2 0
					pc = 7;  // store next pc

					// store registers used in the call
//					objectStack.set(base + 3, r_3);  // call target
//					objectStack.set(base + 4, r_4);  // call arg #1
//					objectStack.setTop(base + 5);
//
//					Operators.call(preemptionContext, state, tail, objectStack, base + 3, base + 3, 0, 0);

				case 7:
//					r_3 = objectStack.get(base + 3);
//					r_4 = objectStack.get(base + 4);

				case 8:  // TAILCALL 2 0 0
					// TODO: is this correct?
//					objectStack.set(base + 0, r_2);
//					objectStack.set(base + 1, r_3);
//					objectStack.set(base + 2, r_4);
//					objectStack.setTop(objectStack.getTop() - 2);

					tail.set(r_2);
					return true;

				default:
					// dead code -- eliminated
					throw new IllegalStateException();
			}
		}
		catch (ControlThrowable ct) {
			// save registers to the object stack
//			objectStack.set(base + 0, r_0);
//			objectStack.set(base + 1, r_1);
//			objectStack.set(base + 2, r_2);
//			objectStack.set(base + 3, r_3);
//			objectStack.set(base + 4, r_4);

//			ct.pushCall(this, base, ret, pc, numResults, flags);

			throw ct;
		}
	}

	@Override
	public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

	/*
	function <t2.lua:3,5> (3 instructions at 0x7fcec84047f0)
	1 param, 2 slots, 0 upvalues, 1 local, 1 constant, 0 functions
	        1       [4]     ADD             1 0 -1  ; - 1
	        2       [4]     RETURN          1 2
	        3       [5]     RETURN          0 1
	constants (1) for 0x7fcec84047f0:
	        1       1
	locals (1) for 0x7fcec84047f0:
	        0       x       1       4
	upvalues (0) for 0x7fcec84047f0:
	 */
	public static class p_0 extends AbstractFunc1 {

		public static final Long k_1 = Long.valueOf(1);

		protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
			// registers
			Object r_0, r_1;

			r_0 = null;
			r_1 = null;

			// load registers
//			r_0 = objectStack.get(base + 0);
//			r_1 = objectStack.get(base + 1);

			try {
				switch (pc) {
					case 0:
						pc = 1;
						preemptionContext.withdraw(2);  // accounting the entire block already

					case 1:  // ADD 1 0 -1
						r_1 = Operators.add(r_0, k_1);

					case 2:  // RETURN 1 2
//						objectStack.set(ret + 0, r_1);
//						objectStack.setTop(ret + 1);
						return false;

					default:
						// dead code -- eliminated
						throw new IllegalStateException();
				}
			}
			catch (ControlThrowable ct) {
				// save registers to the object stack
//				objectStack.set(base + 0, r_0);
//				objectStack.set(base + 1, r_1);

//				ct.pushCall(this, base, ret, pc, numResults, flags);

				throw ct;
			}
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

	/*
	function <t2.lua:7,9> (3 instructions at 0x7fcec8404640)
	1 param, 2 slots, 0 upvalues, 1 local, 0 constants, 0 functions
	        1       [8]     UNM             1 0
	        2       [8]     RETURN          1 2
	        3       [9]     RETURN          0 1
	constants (0) for 0x7fcec8404640:
	locals (1) for 0x7fcec8404640:
	        0       x       1       4
	upvalues (0) for 0x7fcec8404640:
	 */
	public static class p_1 extends AbstractFunc1 {

		protected boolean run(PreemptionContext preemptionContext, LuaState state, Ptr<Object> tail, int base, int ret, int pc, int numResults, int flags) throws ControlThrowable {
			// registers
			Object r_0, r_1;

			r_0 = null;
			r_1 = null;

			// load registers
//			r_0 = objectStack.get(base + 0);
//			r_1 = objectStack.get(base + 1);

			try {
				switch (pc) {
					case 0:
						pc = 1;
						preemptionContext.withdraw(2);  // accounting the entire block already

					case 1:  // UNM 1 0
						r_1 = Operators.unm(r_0);

					case 2:  // RETURN 1 2
//						objectStack.set(ret + 0, r_1);
//						objectStack.setTop(ret + 1);
						return false;

					default:
						// dead code -- eliminated
						throw new IllegalStateException();
				}
			}
			catch (ControlThrowable ct) {
				// save registers to the object stack
//				objectStack.set(base + 0, r_0);
//				objectStack.set(base + 1, r_1);

//				ct.pushCall(this, base, ret, pc, numResults, flags);

				throw ct;
			}
		}

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable {
			throw new UnsupportedOperationException();
		}

	}

}
