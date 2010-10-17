package com.googlecode.biscotti.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;
import static com.google.common.base.Preconditions.checkState;
import static com.googlecode.biscotti.base.Preconditions2.checkElementPosition;
import static com.googlecode.biscotti.collect.TreeList.Color.BLACK;
import static com.googlecode.biscotti.collect.TreeList.Color.RED;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import com.google.common.collect.Ordering;

/**
 * A {@link SortedList} implementation, based on a modified <a
 * href="http://en.wikipedia.org/wiki/Red-black_tree">red-black tree</a>.
 * Elements are ordered from <i>least</i> to <i>greatest</i> according to their
 * <i>natural ordering</i>, or by an explicit {@link Comparator} provided at
 * creation. Attempting to remove or insert {@code null} elements is prohibited.
 * Querying for {@code null} elements is allowed. Inserting non-comparable
 * elements will result in a {@code ClassCastException}. The {@code add(int, E)}
 * , {@code addAll(int, Collection)}, and {@code set(int, E)} operations are not
 * supported.
 * <p>
 * The iterators obtained from the {@link #iterator()} and
 * {@link #listIterator()} methods are <i>fail-fast</i>. Attempts to modify the
 * elements in this list at any time after an iterator is created, in any way
 * except through the iterator's own remove method, will result in a {@code
 * ConcurrentModificationException}. Further, the list iterator does not support
 * the {@code add(E)} and {@code set(E)} operations.
 * <p>
 * This list is not <i>thread-safe</i>. If multiple threads modify this list
 * concurrently it must be synchronized externally, considering "wrapping" the
 * list using the {@link Collections3#synchronize(SortedList)} method.
 * <p>
 * <b>Implementation Note:</b> The the ordering maintained by this list must be
 * <i>consistent with equals</i> if this it is to function correctly. This is so
 * because this implementation uses a comparator (whether or not one is
 * explicitly provided) to perform all element comparisons. Two elements which
 * are deemed equal by the comparator's {@code compare(E, E)} method are, from
 * the standpoint of this list, equal.
 * <p>
 * The underlying red-black tree provides the following worst case running time
 * for this list and its views (where <i>n</i> is the size of this list,
 * <i>k</i> is the highest number of duplicate elements of each other, and
 * <i>m</i> is the size of the specified collection):
 * <p>
 * <table border cellpadding="3" cellspacing="1">
 *   <tr>
 *     <th align="center">Method</th>
 *     <th align="center">Running Time</th>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #addAll(Collection) addAll(Collection)}</br>
 *       {@link #containsAll(Collection) containsAll(Collection)}</br>
 *       {@link #retainAll(Collection) retainAll(Collection)}</br>
 *       {@link #removeAll(Collection) removeAll(Collection)}
 *     </td>
 *     <td align="center"><i>O(m(lg(n - k) + k))</i></td>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #clear() clear()}</br>
 *       {@link #indexOf(Object)}</br>
 *       {@link #lastIndexOf(Object)}</br>
 *       {@link #get(int)}</br>
 *       {@link #remove(int)}</br>
 *     </td>
 *     <td align="center"><i>O(n)</i></td>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #add(Object) add(E)}</br>
 *       {@link #contains(Object)}</br>
 *       {@link #remove(Object)}</br>
 *     </td>
 *     <td align="center"><i>O(lg(n - k) + k)</i></td>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #isEmpty() isEmpty()}</br>
 *       {@link #size()}</br>
 *     </td>
 *     <td align="center"><i>O(1)</i></td>
 *   </tr>
 * </table>
 * 
 * @author Zhenya Leonov
 * @param <E>
 * the type of elements maintained by this list
 */
public class TreeList<E> extends AbstractList<E> implements SortedList<E> {

	private int size = 0;
	private final Node nil = new Node();
	private Node max = nil;
	private Node min = nil;
	private Node root = nil;
	private Comparator<? super E> comparator;

	private TreeList(final Comparator<? super E> comparator) {
		if (comparator != null)
			this.comparator = comparator;
		else
			this.comparator = (Comparator<? super E>) Ordering.natural();
	}

	private TreeList(final Iterable<? extends E> elements) {
		Comparator<? super E> comparator = null;
		if (elements instanceof SortedSet<?>)
			comparator = ((SortedSet) elements).comparator();
		else if (elements instanceof java.util.PriorityQueue<?>)
			comparator = ((java.util.PriorityQueue) elements).comparator();
		else if (elements instanceof SortedCollection<?>)
			comparator = ((SortedCollection) elements).comparator();
		if (comparator == null)
			this.comparator = (Comparator<? super E>) Ordering.natural();
		else
			this.comparator = comparator;
		for (E element : elements)
			add(element);
	}

	/**
	 * Creates a new {@code TreeList} that orders its elements according to
	 * their natural ordering.
	 * 
	 * @return a new {@code TreeList} that orders its elements according to
	 *         their natural ordering
	 */
	public static <E extends Comparable<? super E>> TreeList<E> create() {
		return new TreeList<E>((Comparator<? super E>) null);
	}

	/**
	 * Creates a new {@code TreeList} that orders its elements according to the
	 * specified comparator.
	 * 
	 * @param comparator
	 *            the comparator that will be used to order this priority list
	 * @return a new {@code TreeList} that orders its elements according to
	 *         {@code comparator}
	 */
	public static <E> TreeList<E> create(final Comparator<? super E> comparator) {
		checkNotNull(comparator);
		return new TreeList<E>(comparator);
	}

	/**
	 * Creates a new {@code TreeList} containing the elements of the specified
	 * {@code Iterable}. If the specified iterable is an instance of of
	 * {@link SortedSet}, {@link java.util.PriorityQueue
	 * java.util.PriorityQueue}, or {@code SortedCollection}, this list will be
	 * ordered according to the same ordering. Otherwise, this list will be
	 * ordered according to the <i>natural ordering</i> of its elements.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the list
	 * @return a new {@code TreeList} containing the elements of the specified
	 *         iterable
	 * @throws ClassCastException
	 *             if elements of the specified iterable cannot be compared to
	 *             one another according to this list's ordering
	 * @throws NullPointerException
	 *             if any of the elements of the specified iterable or the
	 *             iterable itself is {@code null}
	 */
	public static <E> TreeList<E> create(final Iterable<? extends E> elements) {
		checkNotNull(elements);
		return new TreeList<E>(elements);
	}

	/**
	 * {@inheritDoc} If one was not explicitly provided a <i>natural order</i>
	 * comparator is returned.
	 * 
	 * @return the comparator used to order this queue
	 */
	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	/**
	 * Inserts the specified element into this list in sorted order.
	 */
	@Override
	public boolean add(E e) {
		checkNotNull(e);
		size++;
		modCount++;
		Node x = root;
		Node parent = nil;
		Node newNode = new Node(e);
		while (x != nil) {
			parent = x;
			if (comparator.compare(newNode.element, x.element) < 0)
				x = x.left;
			else
				x = x.right;
		}
		newNode.parent = parent;
		if (parent == nil)
			root = newNode;
		else if (comparator.compare(newNode.element, parent.element) < 0)
			parent.left = newNode;
		else
			parent.right = newNode;
		fixAfterInsertion(newNode);
		if (max == nil || comparator.compare(e, max.element) >= 0)
			max = newNode;
		if (min == nil || comparator.compare(e, min.element) < 0)
			min = newNode;
		return true;
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		return o != null && search((E) o) != null;
	}

	@Override
	public E get(int index) {
		checkElementIndex(index, size);
		Iterator<E> itor = iterator();
		for (int i = 0; i < index; i++)
			itor.next();
		return itor.next();
	}

	@Override
	public int indexOf(Object o) {
		if (o != null)
			super.indexOf(o);
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o != null) {
			ListIterator<E> itor = listIterator();
			while (itor.hasNext())
				if (itor.next().equals(o)) {
					while (itor.hasNext() && itor.next().equals(o))
						;
					return itor.previousIndex();
				}
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support the {@code add(E)} and {@code
	 * set(E)} operations.
	 */
	@Override
	public ListIterator<E> listIterator() {
		return new ListIterator<E>() {
			int index = 0;
			Node next = min;
			Node prev = nil;
			Node last = nil;
			int expectedModCount = modCount;

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public boolean hasPrevious() {
				return index > 0;
			}

			@Override
			public E next() {
				checkForConcurrentModification();
				if (index == size())
					throw new NoSuchElementException();
				Node node = prev = next;
				index++;
				next = successor(node);
				last = node;
				return node.element;
			}

			@Override
			public int nextIndex() {
				return index;
			}

			@Override
			public E previous() {
				checkForConcurrentModification();
				if (index == 0)
					throw new NoSuchElementException();
				Node node = next = prev;
				index--;
				prev = predecessor(node);
				last = node;
				return node.element;
			}

			@Override
			public int previousIndex() {
				return index - 1;
			}

			@Override
			public void remove() {
				checkForConcurrentModification();
				checkState(last != nil);
				if (last.left != nil && last.right != nil)
					next = last;
				delete(last);
				index--;
				expectedModCount = modCount;
				last = nil;
			}

			@Override
			public void set(E e) {
				throw new UnsupportedOperationException();
			}

			private void checkForConcurrentModification() {
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support the {@code add(E)} and {@code
	 * set(E)} operations.
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		checkPositionIndex(index, size);
		ListIterator<E> listIterator = listIterator();
		for (int i = 0; i < index; i++)
			listIterator.next();
		return listIterator;
	}

	@Override
	public boolean remove(Object o) {
		checkNotNull(o);
		Node node = search((E) o);
		if (node == null)
			return false;
		delete(node);
		return true;
	}

	@Override
	public E remove(int index) {
		checkElementIndex(index, size);
		ListIterator<E> li = listIterator(index);
		E e = li.next();
		li.remove();
		return e;
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public TreeList<E> headList(E toElement) {
		checkNotNull(toElement);
		Iterator<E> itor = iterator();
		int toIndex = 0;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0)
			toIndex++;
		return new SubList(this, 0, toIndex);
	}

	@Override
	public TreeList<E> subList(int fromIndex, int toIndex) {
		checkPositionIndexes(fromIndex, toIndex, size());
		return new SubList(this, fromIndex, toIndex);
	}

	@Override
	public TreeList<E> subList(E fromElement, E toElement) {
		checkNotNull(fromElement);
		checkNotNull(toElement);
		checkArgument(comparator.compare(fromElement, toElement) <= 0);
		Iterator<E> itor = iterator();
		int fromIndex = 0;
		while (itor.hasNext()
				&& comparator.compare(itor.next(), fromElement) < 0)
			fromIndex++;
		int toIndex = fromIndex + 1;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0)
			toIndex++;
		return new SubList(this, fromIndex, toIndex);
	}

	@Override
	public TreeList<E> tailList(E fromElement) {
		checkNotNull(fromElement);
		Iterator<E> itor = iterator();
		int fromIndex = 0;
		while (itor.hasNext()
				&& comparator.compare(itor.next(), fromElement) < 0)
			fromIndex++;
		return new SubList(this, fromIndex, size);
	}

	private class SubList extends TreeList<E> {
		private TreeList<E> l;
		private int offset;
		private int size;

		private void checkForConcurrentModification() {
			if (modCount != l.modCount)
				throw new ConcurrentModificationException();
		}

		public SubList(TreeList<E> l, int fromIndex, int toIndex) {
			super(l.comparator);
			this.l = l;
			offset = fromIndex;
			modCount = l.modCount;
			size = toIndex - fromIndex;
			min = l.min;
			int i = 0;
			for (; i < fromIndex; i++)
				min = successor(min);
			max = min;
			for (; i < toIndex - 1; i++)
				max = successor(max);
		}

		@Override
		public boolean add(E e) {
			checkElementPosition(e, min.element, max.element,
					comparator);
			l.add(e);
			modCount = l.modCount;
			size++;
			if (comparator.compare(max.element, e) == 0)
				max = successor(max);
			return true;
		}

		@Override
		public void add(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object o) {
			checkForConcurrentModification();
			return o != null && search((E) o) != null;
		}

		@Override
		public E get(int index) {
			checkElementIndex(index, size);
			checkForConcurrentModification();
			return l.get(index + offset);
		}

		@Override
		public ListIterator<E> listIterator() {
			return listIterator(0);
		}

		@Override		
		public ListIterator<E> listIterator(final int index) {
				checkForConcurrentModification();
				checkPositionIndex(index, size);
			return new ListIterator<E>() {
				private ListIterator<E> i = l.listIterator(index + offset);

				@Override
				public boolean hasNext() {
					return nextIndex() < size;
				}

				@Override
				public E next() {
					if (hasNext())
						return i.next();
					else
						throw new NoSuchElementException();
				}

				@Override
				public boolean hasPrevious() {
					return previousIndex() >= 0;
				}

				@Override
				public E previous() {
					if (hasPrevious())
						return i.previous();
					else
						throw new NoSuchElementException();
				}

				@Override
				public int nextIndex() {
					return i.nextIndex() - offset;
				}

				@Override
				public int previousIndex() {
					return i.previousIndex() - offset;
				}

				@Override
				public void remove() {
					i.remove();
					modCount = l.modCount;
					size--;
				}

				@Override
				public void set(E e) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void add(E e) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			checkForConcurrentModification();
			checkNotNull(o);
			Node node = search((E) o);
			if (node == null)
				return false;
			if (node == max)
				max = predecessor(max);
			if (node == min)
				min = successor(min);
			l.delete(node);
			modCount = l.modCount;
			size--;
			return true;
		}

		@Override
		public E remove(int index) {
			checkForConcurrentModification();
			checkElementIndex(index, size);
			if (index == 0)
				min = successor(min);
			if (index == size - 1)
				max = predecessor(max);
			E e = l.remove(index + offset);
			modCount = l.modCount;
			size--;
			return e;
		}

		@Override
		public E set(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			checkForConcurrentModification();
			return size;
		}

		@Override
		Node search(final E e) {
			int i = comparator.compare(e, min.element);
			int j = comparator.compare(e, max.element);
			if (i < 0 || j > 0)
				return null;
			if (i == 0)
				return min;
			else if (j == 0)
				return max;
			else
				return l.search(e);
		}
	}

	// Red-Black-Tree

	static enum Color {
		BLACK, RED;
	}

	private class Node {
		private E element = null;
		private Node parent, left, right;
		private Color color = BLACK;

		private Node() {
			parent = left = right = this;
		}

		private Node(final E element) {
			this.element = element;
			parent = left = right = nil;
		}
	}

	Node search(final E e) {
		Node n = root;
		while (n != nil) {
			int cmp = comparator.compare(e, n.element);
			if (cmp == 0)
				return n;
			if (cmp < 0)
				n = n.left;
			else
				n = n.right;
		}
		return null;
	}

	void delete(Node z) {
		size--;
		modCount++;
		Node x, y;
		if (min == z)
			min = successor(z);
		if(z.left == nil || z.right == nil)
			y = z;
		else
			y = successor(z);
		if(y.left != nil)
			x = y.left;
		else
			x = y.right;
		x.parent = y.parent;
		if (y.parent == nil)
			root = x;
		else if (y == y.parent.left)
			y.parent.left = x;
		else
			y.parent.right = x;
		if (y != z)
			z.element = y.element;
		if (y.color == Color.BLACK)
			fixAfterDeletion(x);
	}

	private Node successor(Node x) {
		if (x == nil)
			return nil;
		if (x.right != nil) {
			Node y = x.right;
			while (y.left != nil)
				y = y.left;
			return y;
		}
		Node y = x.parent;
		while (y != nil && x == y.right) {
			x = y;
			y = y.parent;
		}
		return y;
	}

	private Node predecessor(Node x) {
		if (x == nil)
			return nil;
		if (x.left != nil) {
			Node y = x.left;
			while (y.right != nil)
				y = y.right;
			return y;
		}
		Node y = x.parent;
		while (y != nil && x == y.left) {
			x = y;
			y = y.left;
		}
		return y;
	}

	private void leftRotate(final Node x) {
		if (x != nil) {
			Node n = x.right;
			x.right = n.left;
			if (n.left != nil)
				n.left.parent = x;
			n.parent = x.parent;
			if (x.parent == nil)
				root = n;
			else if (x.parent.left == x)
				x.parent.left = n;
			else
				x.parent.right = n;
			n.left = x;
			x.parent = n;
		}
	}

	private void rightRotate(final Node x) {
		if (x != nil) {
			Node n = x.left;
			x.left = n.right;
			if (n.right != nil)
				n.right.parent = x;
			n.parent = x.parent;
			if (x.parent == nil)
				root = n;
			else if (x.parent.right == x)
				x.parent.right = n;
			else
				x.parent.left = n;
			n.right = x;
			x.parent = n;
		}
	}

	private void fixAfterInsertion(Node z) {
		z.color = RED;
		while (z.parent.color == RED) {
			if (z.parent == z.parent.parent.left) {
				Node y = z.parent.parent.right;
				if (y.color == RED) {
					z.parent.color = BLACK;
					y.color = BLACK;
					z.parent.parent.color = RED;
					z = z.parent.parent;
				} else {
					if (z == z.parent.right) {
						z = z.parent;
						leftRotate(z);
					}
					z.parent.color = BLACK;
					z.parent.parent.color = RED;
					rightRotate(z.parent.parent);
				}
			} else {
				Node y = z.parent.parent.left;
				if (y.color == RED) {
					z.parent.color = BLACK;
					y.color = BLACK;
					z.parent.parent.color = RED;
					z = z.parent.parent;
				} else {
					if (z == z.parent.left) {
						z = z.parent;
						rightRotate(z);
					}
					z.parent.color = BLACK;
					z.parent.parent.color = RED;
					leftRotate(z.parent.parent);
				}
			}
		}
		root.color = BLACK;
	}

	private void fixAfterDeletion(Node x) {
		while (x != root && x.color == BLACK) {
			if (x == x.parent.left) {
				Node w = x.parent.right;
				if (w.color == RED) {
					w.color = BLACK;
					x.parent.color = RED;
					leftRotate(x.parent);
					w = x.parent.right;
				}
				if (w.left.color == BLACK && w.right.color == BLACK) {
					w.color = RED;
					x = x.parent;
				} else {
					if (w.right.color == BLACK) {
						w.left.color = BLACK;
						w.color = RED;
						rightRotate(w);
						w = x.parent.right;
					}
					w.color = x.parent.color;
					x.parent.color = BLACK;
					x.right.color = BLACK;
					leftRotate(x.parent);
					x = root;
				}
			} else {
				Node w = x.parent.left;
				if (w.color == RED) {
					w.color = BLACK;
					x.parent.color = RED;
					rightRotate(x.parent);
					w = x.parent.left;
				}
				if (w.left.color == BLACK && w.right.color == BLACK) {
					w.color = RED;
					x = x.parent;
				} else {
					if (w.left.color == BLACK) {
						w.right.color = BLACK;
						w.color = RED;
						leftRotate(w);
						w = x.parent.left;
					}
					w.color = x.parent.color;
					x.parent.color = BLACK;
					w.left.color = BLACK;
					rightRotate(x.parent);
					x = root;
				}
			}
		}
		x.color = BLACK;
	}
}