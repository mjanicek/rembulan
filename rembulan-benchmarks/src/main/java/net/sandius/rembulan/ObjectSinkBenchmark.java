package net.sandius.rembulan;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import static net.sandius.rembulan.Util.*;

@Fork(2)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class ObjectSinkBenchmark {

	@State(Scope.Thread)
	public static class NoCacheSinkState {
		public final ObjectSink sink = new SimpleObjectSink();
	}

	@State(Scope.Thread)
	public static class PairCacheSinkState {
		public final ObjectSink sink = new PairObjectSink();
	}

	@State(Scope.Thread)
	public static class TripleCacheSinkState {
		public final ObjectSink sink = new TripleObjectSink();
	}

	public void impl_push(Blackhole bh, ObjectSink sink, int count) {
		sink.reset();
		for (int i = 0; i < count; i++) {
			sink.push(i);
		}

		assertEquals(sink.size(), count);
		bh.consume(sink.size());
		bh.consume(sink._0());
	}

	public void impl_set(Blackhole bh, ObjectSink sink, int count) {
		switch (count) {
			case 0: sink.reset(); break;
			case 1: sink.setTo(0); break;
			case 2: sink.setTo(0, 1); break;
			case 3: sink.setTo(0, 1, 2); break;
			case 4: sink.setTo(0, 1, 2, 3); break;
			case 5: sink.setTo(0, 1, 2, 3, 4); break;
			default:
				sink.setTo(0, 1, 2, 3, 4);
				for (int i = 5; i < count; i++) {
					sink.push(i);
				}
		}

		assertEquals(sink.size(), count);
		bh.consume(sink.size());
		bh.consume(sink._0());
	}

	// ------------------------------------------------------------------
	// Push

	// No cache

	@Benchmark
	public void push_0_00(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 0);
	}

	@Benchmark
	public void push_0_01(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 1);
	}

	@Benchmark
	public void push_0_02(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 2);
	}

	@Benchmark
	public void push_0_03(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 3);
	}

	@Benchmark
	public void push_0_04(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 4);
	}

	@Benchmark
	public void push_0_05(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 5);
	}

	@Benchmark
	public void push_0_10(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 10);
	}

	@Benchmark
	public void push_0_20(NoCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 20);
	}

	// Pair cache

	@Benchmark
	public void push_2_00(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 0);
	}

	@Benchmark
	public void push_2_01(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 1);
	}

	@Benchmark
	public void push_2_02(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 2);
	}

	@Benchmark
	public void push_2_03(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 3);
	}

	@Benchmark
	public void push_2_04(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 4);
	}

	@Benchmark
	public void push_2_05(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 5);
	}

	@Benchmark
	public void push_2_10(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 10);
	}

	@Benchmark
	public void push_2_20(PairCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 20);
	}

	// Triple cache

	@Benchmark
	public void push_3_00(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 0);
	}

	@Benchmark
	public void push_3_01(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 1);
	}

	@Benchmark
	public void push_3_02(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 2);
	}

	@Benchmark
	public void push_3_03(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 3);
	}

	@Benchmark
	public void push_3_04(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 4);
	}

	@Benchmark
	public void push_3_05(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 5);
	}

	@Benchmark
	public void push_3_10(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 10);
	}

	@Benchmark
	public void push_3_20(TripleCacheSinkState state, Blackhole bh) {
		impl_push(bh, state.sink, 20);
	}

	// ------------------------------------------------------------------
	// Set

	// No cache

	@Benchmark
	public void set_0_00(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 0);
	}

	@Benchmark
	public void set_0_01(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 1);
	}

	@Benchmark
	public void set_0_02(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 2);
	}

	@Benchmark
	public void set_0_03(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 3);
	}

	@Benchmark
	public void set_0_04(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 4);
	}

	@Benchmark
	public void set_0_05(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 5);
	}

	@Benchmark
	public void set_0_10(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 10);
	}

	@Benchmark
	public void set_0_20(NoCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 20);
	}

	// Pair cache

	@Benchmark
	public void set_2_00(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 0);
	}

	@Benchmark
	public void set_2_01(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 1);
	}

	@Benchmark
	public void set_2_02(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 2);
	}

	@Benchmark
	public void set_2_03(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 3);
	}

	@Benchmark
	public void set_2_04(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 4);
	}

	@Benchmark
	public void set_2_05(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 5);
	}

	@Benchmark
	public void set_2_10(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 10);
	}

	@Benchmark
	public void set_2_20(PairCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 20);
	}

	// Triple cache

	@Benchmark
	public void set_3_00(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 0);
	}

	@Benchmark
	public void set_3_01(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 1);
	}

	@Benchmark
	public void set_3_02(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 2);
	}

	@Benchmark
	public void set_3_03(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 3);
	}

	@Benchmark
	public void set_3_04(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 4);
	}

	@Benchmark
	public void set_3_05(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 5);
	}

	@Benchmark
	public void set_3_10(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 10);
	}

	@Benchmark
	public void set_3_20(TripleCacheSinkState state, Blackhole bh) {
		impl_set(bh, state.sink, 20);
	}
	
}
