package com.palamida.util.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Queues;

/**
 * Static utility methods which operate on or return {@link Queue}s.
 * 
 * @author Zhenya Leonov
 * @see Queues
 */
public class MoreQueues {

	private MoreQueues() {
	}

	/**
	 * Creates an empty {@code ArrayDeque} with an initial capacity sufficient
	 * to hold the specified number of elements.
	 * 
	 * @param numElements
	 *            lower bound on initial capacity of the deque
	 * @return an empty {@code ArrayDeque} with an initial capacity sufficient
	 *         to hold the specified number of elements
	 */
	public static <E> ArrayDeque<E> newArrayDequeWithCapacity(final int numElements) {
		return new ArrayDeque<E>(numElements);
	}

	/**
	 * Creates an {@code ArrayDeque} containing the specified initial elements.
	 * 
	 * @param first
	 *            the first element
	 * @param rest
	 *            an array of additional elements, possibly empty
	 * @return an {@code ArrayDeque} containing the specified initial elements
	 */
	public static <E> ArrayDeque<E> newArrayDeque(final E first, final E... rest) {
		checkNotNull(first);
		checkNotNull(rest);
		final ArrayDeque<E> arrayDeque = new ArrayDeque<E>(rest.length + 1);
		arrayDeque.add(first);
		Collections.addAll(arrayDeque, rest);
		return arrayDeque;
	}

	/**
	 * Creates an {@code ArrayDeque} containing the elements returned by the
	 * specified iterator.
	 * 
	 * @param elements
	 *            the iterator whose elements are to be placed into the deque
	 * @return an {@code ArrayDeque} containing the elements returned by the
	 *         specified iterator
	 */
	public static <E> ArrayDeque<E> newArrayDeque(final Iterator<? extends E> elements) {
		checkNotNull(elements);
		final ArrayDeque<E> arrayDeque = new ArrayDeque<E>();
		Iterators.addAll(arrayDeque, elements);
		return arrayDeque;
	}

	/**
	 * Attempts to insert the given elements into the specified queue. When
	 * using a capacity-restricted queue, some or all of the elements maybe
	 * rejected.
	 * <p>
	 * This method is generally preferable to
	 * {@link Collections#addAll(Collection, Object...)
	 * Collections.addAll(Queue, E...)}, which can fail to insert an element
	 * only by throwing an exception.
	 * 
	 * @param queue
	 *            the specified queue
	 * @param elements
	 *            the elements to insert into the queue
	 * @return {@code true} if at least one elements was inserted into the
	 *         queue; {@code false} otherwise
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             inserted into this queue
	 * @throws NullPointerException
	 *             if the specified element is {@code null} and this queue does
	 *             not permit null elements
	 * @throws IllegalArgumentException
	 *             if some property of this element prevents it from being
	 *             inserted into this queue
	 */
	public static <E> boolean offerAll(Queue<? super E> queue, E... elements) {
		checkNotNull(queue);
		checkNotNull(elements);
		boolean result = false;
		for (final E e : elements)
			result |= queue.offer(e);
		return result;
	}

	/**
	 * Attempts to insert the given elements into the specified queue. When
	 * using a capacity-restricted queue, some or all of the elements maybe
	 * rejected.
	 * <p>
	 * This method is generally preferable to
	 * {@link Iterators#addAll(Collection, Iterator) Iterators.addAll(Queue,
	 * Iterator)}, which can fail to insert an element only by throwing an
	 * exception.
	 * 
	 * @param queue
	 *            the specified queue
	 * @param elements
	 *            the elements to insert into the queue
	 * @return {@code true} if at least one elements was inserted into the
	 *         queue; {@code false} otherwise
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             inserted into this queue
	 * @throws NullPointerException
	 *             if the specified element is {@code null} and this queue does
	 *             not permit null elements
	 * @throws IllegalArgumentException
	 *             if some property of this element prevents it from being
	 *             inserted into this queue
	 */
	public static <E> boolean offerAll(Queue<? super E> queue, Iterator<E> elements) {
		checkNotNull(queue);
		checkNotNull(elements);
		boolean result = false;
		while (elements.hasNext())
			result |= queue.offer(elements.next());
		return result;
	}

	/**
	 * Attempts to insert the given elements into the specified queue. When
	 * using a capacity-restricted queue, some or all of the elements maybe
	 * rejected.
	 * <p>
	 * This method is generally preferable to
	 * {@link Iterables#addAll(Collection, Iterable) Iterables.addAll(Queue,
	 * Iterable)}, which can fail to insert an element only by throwing an
	 * exception.
	 * 
	 * @param queue
	 *            the specified queue
	 * @param elements
	 *            the elements to insert into the queue
	 * @return {@code true} if at least one elements was inserted into the
	 *         queue; {@code false} otherwise
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             inserted into this queue
	 * @throws NullPointerException
	 *             if the specified element is {@code null} and this queue does
	 *             not permit null elements
	 * @throws IllegalArgumentException
	 *             if some property of this element prevents it from being
	 *             inserted into this queue
	 */
	public static <E> boolean offerAll(Queue<? super E> queue, Iterable<E> elements) {
		checkNotNull(queue);
		checkNotNull(elements);
		return offerAll(queue, elements.iterator());
	}
}
