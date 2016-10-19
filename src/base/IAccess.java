package base;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Constructor Iterator of State Type S and Data Type D
 * @author yechen
 *
 * @param <S> Type of the State, for example: base.State, base.WorkloadState, or a subclass
 * @param <D> Type of the Data Type of the <b>individual elements</b>, for example: Map in case of
 * HashMap being the iterable or String if being ArrayList of Strings
 */
public class IAccess<S, D>{
	public enum BaseIOType {Workload, Implementation};
	private S state;
	private Iterable<D> data;
	private BiFunction<S, D, Integer> inputCostFunction, outputCostFunction;

	public IAccess(S state, Iterable<D> data, 
			BiFunction<S, D, Integer> inputCostFunction,
			BiFunction<S, D, Integer> outputCostFunction ){
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
	public IAccess(S state, Iterable<D> data, BaseIOType defaultIOType){
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

	private final BiFunction<S, D, Integer> defaultInputSFunc = (s, d) -> {
		State state = (State) s;
		state.incrementAccessIO(0, 1);
		return 1;
	};

	private final BiFunction<S, D, Integer> defaultInputWSFunc = (ws, d) -> {
		WorkloadState state = (WorkloadState) ws;
		state.incrementAccessIO(0, 1);
		return 1;
	};

	private final BiFunction<S, D, Integer> defaultOutputSFunc = (s, d) -> {
		State state = (State) s;
		state.incrementAccessIO(1, 0);
		return 1;
	};

	private final BiFunction<S, D, Integer> defaultOutputWSFunc = (ws, d) -> {
		WorkloadState state = (WorkloadState) ws;
		state.incrementAccessIO(1, 0);
		return 1;
	};


	/**
	 * Find items given predicate
	 * <br />
	 * Example of predicate:
	 * <br />
	 * Predicate pcMembers = (username) -> { return state.UA.contains(username, "pc") }
	 * @param predicate
	 * @return Returns a parallel stream of all elements where predicate is true
	 */
	public Stream<D> findItems(final Predicate<D> predicate){
		Stream<D> stream = StreamSupport.stream(data.spliterator(), true).filter(predicate);
		return stream;
	}

	/**
	 * Unconditional loop, custom read and write times
	 * @param action
	 * @param numReadsPerItem
	 * @param numWritesPerItem
	 */
	public void forEach(final Consumer<D> action, final int numReadsPerItem, 
			final int numWritesPerItem) {
		data.forEach( new Consumer<D>() {

			@Override
			public void accept(D d) {
				action.accept(d);
				for (int i = 0; i < numReadsPerItem; i++){
					inputCostFunction.apply(state, d);
				}
				for (int i = 0; i < numWritesPerItem; i++){
					outputCostFunction.apply(state, d);
				}
			}

		});
	}

	/**
	 * Conditional loop, 1 read and no write
	 * @param action
	 * @param numReadsPerItem
	 * @param numWritesPerItem
	 */
	public void forEach(final Consumer<D> action) {
		data.forEach( new Consumer<D>() {

			@Override
			public void accept(D d) {
				action.accept(d);
				outputCostFunction.apply(state, d);
			}

		});
	}

	/**
	 * Existence check. Note for hash map item is (key ,value) pair. 
	 * May need custom functions for hashmap existence check on key or value.
	 * But it is not appropriate to write it here.
	 * @param item
	 * @return Returns true iff item exists in container data
	 */
	public boolean exists(D item){
		return StreamSupport.stream(data.spliterator(), true).anyMatch(o -> o == item);
	}

	/**
	 * Forall logic on this data collection
	 * @param predicate
	 * @return true iff predicate is true for all items in data
	 */
	public boolean forAllD(Predicate<D> predicate){
		return StreamSupport.stream(data.spliterator(), true).allMatch(predicate);
	}

	/**
	 * Existance logic on this data collection
	 * @param predicate
	 * @return true iff predicate is true for at least one item in data
	 */
	public boolean existsD(Predicate<D> predicate){
		return StreamSupport.stream(data.spliterator(), true).anyMatch(predicate);
	}

	/**
	 * Get the size
	 * @return number of elements in this data collection
	 */
	public long size(){
		return StreamSupport.stream(data.spliterator(), true).count();
	}
}
