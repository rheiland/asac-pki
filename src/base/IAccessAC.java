package base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is the AC Iterator for AC State (but not extension).
 *
 * This iterator uses Collection's size(), next(), hasNext(), but also
 * increments the I/O accesses whenever these methods are called.
 * @author yechen
 *
 * @param <T> type of the object to iterate over.
 */
public class IAccessAC<T> {
	protected Collection<T> object;
	protected State s;
	protected int factor;

	protected Iterator<T> it = null;

	public IAccessAC(Collection<T> obj, State s, int factor) {
		object = obj;
		this.s = s;
		this.factor = factor;
		begin();
	}

	/**
	 * Resets the iterator to the beginning.
	 */
	public void begin() {
		it = object.iterator();
	}

	/**
	 * Do we have more elements?
	 * @return true if we do.
	 */
	public boolean hasNext() {
		if(it == null) return false;
		return it.hasNext();
	}

	/**
	 * @return the next element.
	 * @throws NoSuchElementException if there's no more elements to read
	 */
	public T next() {
		T ret = it.next();
		s.incrementAccessIO(1 + factor, 0);
		return ret;
	}

	/**
	 * Does collection contain a specific object
	 * @param obj to be tested
	 * @return true iff we do.
	 */
	public boolean contains(T obj) {
		s.incrementAccessIO(1, 0);
		return object.contains(obj);
	}

	/**
	 * @return the size of the collection.
	 */
	public int size() {
		return object.size();
	}

	/**
	 * Iterate through the entire collection and return the entire list.
	 * @return
	 */
	public List<T> getList() {
		List<T> ret = new ArrayList<T>();
		while(hasNext()) {
			ret.add(next());
		}
		return ret;
	}
}
