package net.sandius.rembulan;

import net.sandius.rembulan.core.ArrayRegisters;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Registers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(5)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class RegistersBenchmark {

	@State(Scope.Thread)
	public static class ObjectStackHolder {
		public final ObjectStack objectStack;
		public ObjectStackHolder() {
			objectStack = ObjectStack.newEmptyStack(250);
		}
	}

    @Benchmark
    public void bmk_0_registersFromRootView(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		Registers regs = os.rootView();

		for (int i = 0; i < 50; i++) {
			bh.consume(regs);

			for (int j = 0; j < 5; j++) {
				regs.set(j, false);
			}

			for (int j = 0; j < 5; j++) {
				bh.consume(regs.get(j));
			}

			regs = regs.from(5);
		}
    }

	@Benchmark
	public void bmk_1_registersViewFrom(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		int offset = 0;

		for (int i = 0; i < 50; i++) {
			Registers regs = os.viewFrom(offset);
			bh.consume(regs);

			for (int j = 0; j < 5; j++) {
				regs.set(j, false);
			}

			for (int j = 0; j < 5; j++) {
				bh.consume(regs.get(j));
			}

			offset += 5;
		}
	}

	@Benchmark
	public void bmk_2_directManipulation(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		int offset = 0;

		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 5; j++) {
				os.set(offset + j, false);
			}
			for (int j = 0; j < 5; j++) {
				bh.consume(os.get(offset + j));
			}
			offset += 5;
		}
	}

	@Benchmark
	public void bmk_2_registerAllocation(Blackhole bh) {
		for (int i = 0; i < 50; i++) {
			Registers regs = new ArrayRegisters(5);
			bh.consume(regs);

			for (int j = 0; j < 5; j++) {
				regs.set(j, false);
			}

			for (int j = 0; j < 5; j++) {
				bh.consume(regs.get(j));
			}
		}
	}

}
