package net.sandius.rembulan;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

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
	public void _0_00(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _0_01(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _0_02(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _0_03(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _0_04(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _0_05(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

	@Benchmark
	public void _0_10(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 10);
	}

	@Benchmark
	public void _0_20(NoCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 20);
	}

	// Pair cache

	@Benchmark
	public void _2_00(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _2_01(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _2_02(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _2_03(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _2_04(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _2_05(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

	@Benchmark
	public void _2_10(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 10);
	}

	@Benchmark
	public void _2_20(PairCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 20);
	}

	// Triple cache

	@Benchmark
	public void _3_00(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 0);
	}

	@Benchmark
	public void _3_01(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 1);
	}

	@Benchmark
	public void _3_02(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 2);
	}

	@Benchmark
	public void _3_03(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 3);
	}

	@Benchmark
	public void _3_04(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 4);
	}

	@Benchmark
	public void _3_05(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 5);
	}

	@Benchmark
	public void _3_10(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 10);
	}

	@Benchmark
	public void _3_20(TripleCacheSinkState state, Blackhole bh) {
		impl(bh, state.sink, 20);
	}

}
