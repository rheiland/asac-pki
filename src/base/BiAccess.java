package base;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import base.IAccess.BaseIOType;

public class BiAccess<S, K, V> {
	private S state;
	private Map<K, ? extends Collection<V>> data;
	private BiFunction<K, V, Integer> inputCostFunction, outputCostFunction;

	public BiAccess(S state, Map<K, ? extends Collection<V>> data, 
			BiFunction<K, V, Integer> inputCostFunction,
			BiFunction<K, V, Integer> outputCostFunction ){
		this.state = state;
		this.data = data;
		this.inputCostFunction = inputCostFunction;
		this.outputCostFunction = outputCostFunction;
	}

	/**
	 * Shortcut for construction of iterable objects using default I/O
	 * (i.e. situations where cost is 1 per data item for either workload or implementation action)
	 * @param state
	 * @param data
	 * @param defaultIOType Whether state is WorkloadState (BaseIOType.Workload) or State (BaseIOType.Implementation) 
	 */
	public BiAccess(S state, Map<K, ? extends Collection<V>> data, BaseIOType defaultIOType){
		this.state = state;
		this.data = data;
		switch (defaultIOType){
		case Implementation:
			this.inputCostFunction = defaultInputSFunc;
			this.outputCostFunction = defaultOutputSFunc;
			break;
		case Workload:
			this.inputCostFunction = defaultInputWSFunc;
			this.outputCostFunction = defaultOutputWSFunc;
			break;
		}
	}

	private final BiFunction<K, V, Integer> defaultInputSFunc = (k, v) -> {
		State state = (State) this.state;
		state.incrementAccessIO(0, 2);
		return 2;
	};

	private final BiFunction<K, V, Integer> defaultInputWSFunc = (k, v) -> {
		WorkloadState state = (WorkloadState) this.state;
		state.incrementAccessIO(0, 2);
		return 2;
	};

	private final BiFunction<K, V, Integer> defaultOutputSFunc = (k, v) -> {
		State state = (State) this.state;
		state.incrementAccessIO(2, 0);
		return 2;
	};

	private final BiFunction<K, V, Integer> defaultOutputWSFunc = (k, v) -> {
		WorkloadState state = (WorkloadState) this.state;
		state.incrementAccessIO(2, 0);
		return 2;
	};

	/**
	 * Unconditional loop over all items, custom read and write times
	 * @param action
	 * @param numReadsPerItem
	 * @param numWritesPerItem
	 */
	public void forEach(final BiConsumer<K, V> action, final int numReadsPerItem, 
			final int numWritesPerItem) {

		data.forEach( (k, vs) -> {
			vs.forEach( v -> {
				action.accept(k, v);
				for (int i = 0; i < numReadsPerItem; i++){
					inputCostFunction.apply(k, v);
				}
				for (int i = 0; i < numWritesPerItem; i++){
					outputCostFunction.apply(k, v);
				}
			});
		});
	}

	/**
	 * Unconditional loop over all items, 1 read 0 write
	 * @param action
	 * @param numReadsPerItem
	 * @param numWritesPerItem
	 */
	public void forEach(final BiConsumer<K, V> action) {
		data.forEach( (k, vs) -> {
			vs.forEach( v -> {
				action.accept(k, v);
				outputCostFunction.apply(k, v);
			});
		});
	}

	/**
	 * Conditional loop over all items, custom read and write times
	 * @param predicate
	 * @param action
	 * @param numReadsPerItem
	 * @param numWritesPerItem
	 */
	public void forEach(final BiPredicate<K, V> predicate, final BiConsumer<K, V> action, final int numReadsPerItem, 
			final int numWritesPerItem) {
		data.forEach( (k, vs) -> {
			vs.forEach( v -> {
				if (predicate.test(k, v)){
					action.accept(k, v);
					for (int i = 0; i < numReadsPerItem; i++){
						inputCostFunction.apply(k, v);
					}
					for (int i = 0; i < numWritesPerItem; i++){
						outputCostFunction.apply(k, v);
					}
				}

			});
		});
	}
	
	
	public boolean existsInK(K k){
		outputCostFunction.apply(k,  null);
		return data.keySet().contains(k);
	}
	
	/**
	 * Tests on v
	 * @param v the value to be tested
	 * @return returns true iff EXISTS e = (k_e, v_e) such that v_e = v 
	 */
	public boolean existsInV(V v){
		outputCostFunction.apply(null, v);
		return data.entrySet().parallelStream().anyMatch(
				e -> e.getValue().parallelStream().anyMatch(vOther -> vOther.equals(v))
				);
	}
	
	/**
	 * Tests the tuple
	 * @param k
	 * @param v
	 * @return returns true iff EXISTS e = (k_e, v_e) such that k_e = k AND v_e = e
	 */
	public boolean exists(K k, V v){
		outputCostFunction.apply(k, v);
		return data.entrySet().parallelStream().anyMatch( e -> 
			e.getKey().equals(k) && e.getValue().parallelStream().anyMatch(vOther -> vOther.equals(v))
				);
	}
	
	
}
