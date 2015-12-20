package net.sandius.rembulan;

import net.sandius.rembulan.core.legacy.FixedSizeRegisters;
import net.sandius.rembulan.core.legacy.ObjectStack;
import net.sandius.rembulan.core.legacy.Registers;
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

	public static final int NESTING = 50;
	public static final int SIZE = 5;

	public static final int SHARED_STACK_SIZE = 250;

	@State(Scope.Thread)
	public static class ObjectStackHolder {
		public final ObjectStack objectStack;
		public ObjectStackHolder() {
			objectStack = ObjectStack.newEmptyStack(SHARED_STACK_SIZE);
		}
	}

    @Benchmark
    public void bmk_0_registersFromRootView(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		ObjectStack.View regs = os.rootView();

		for (int i = 0; i < NESTING; i++) {
			bh.consume(regs);

			for (int j = 0; j < SIZE; j++) {
				regs.push(j);
			}

			for (int j = 0; j < SIZE; j++) {
				bh.consume(regs.get(j));
			}

			if (i < NESTING) {
				regs = regs.from(SIZE);
			}
		}
    }

	@Benchmark
	public void bmk_1_registersViewFrom(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		int offset = 0;

		for (int i = 0; i < NESTING; i++) {
			Registers regs = os.viewFrom(offset);
			bh.consume(regs);

			for (int j = 0; j < SIZE; j++) {
				regs.push(j);
			}

			for (int j = 0; j < SIZE; j++) {
				bh.consume(regs.get(j));
			}

			offset += 5;
		}
	}

	@Benchmark
	public void bmk_2_directObjectStackManipulation(ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		bh.consume(os);

		int offset = 0;

		for (int i = 0; i < NESTING; i++) {
			os.setTop(offset);
			for (int j = 0; j < SIZE; j++) {
				os.set(offset + j, j);
			}
			os.setTop(offset + SIZE);

			for (int j = 0; j < SIZE; j++) {
				bh.consume(os.get(offset + j));
			}

			offset += 5;
		}
	}

	@Benchmark
	public void bmk_3_registerAllocation(Blackhole bh) {
		for (int i = 0; i < NESTING; i++) {
			Registers regs = new FixedSizeRegisters(SIZE);
			bh.consume(regs);

			for (int j = 0; j < SIZE; j++) {
				regs.push(j);
			}

			for (int j = 0; j < SIZE; j++) {
				bh.consume(regs.get(j));
			}
		}
	}

}
