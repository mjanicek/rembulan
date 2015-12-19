package net.sandius.rembulan;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
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

	public void impl(Blackhole bh, ObjectSink sink, int count) {
		for (int i = 0; i < count; i++) {
			sink.push(i);
		}

		bh.consume(sink.size());
		bh.consume(sink._0());
		sink.reset();
	}

	// No cache

	@Benchmark
	public void _0_0_noCache_add0(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _0_1_noCache_add1(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _0_2_noCache_add2(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _0_3_noCache_add3(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _0_4_noCache_add4(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _0_5_noCache_add5(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

	// Pair cache

	@Benchmark
	public void _2_0_pairCache_add0(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _2_1_pairCache_add1(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _2_2_pairCache_add2(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _2_3_pairCache_add3(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _2_4_pairCache_add4(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _2_5_pairCache_add5(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

	// Triple cache

	@Benchmark
	public void _3_0_pairCache_add0(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _3_1_pairCache_add1(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _3_2_pairCache_add2(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _3_3_pairCache_add3(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _3_4_pairCache_add4(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _3_5_pairCache_add5(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

}
