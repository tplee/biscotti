package collections;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingObject;

/**
 * A {@link SortedList} which forwards all its method calls to another {@code
 * SortedList}. Subclasses should override one or more methods to modify the
 * behavior of the backing list as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @see ForwardingObject
 * @author Zhenya Leonov
 */
public abstract class ForwardingSortedList<E> extends ForwardingList<E>
		implements SortedList<E> {

	@Override
	protected abstract SortedList<E> delegate();

	@Override
	public Comparator<? super E> comparator() {
		return delegate().comparator();
	}

	@Override
	public List<E> headList(E toElement) {
		return delegate().headList(toElement);
	}

	@Override
	public List<E> subList(E fromElement, E toElement) {
		return delegate().subList(fromElement, toElement);
	}

	@Override
	public List<E> tailList(E fromElement) {
		return delegate().tailList(fromElement);
	}

}