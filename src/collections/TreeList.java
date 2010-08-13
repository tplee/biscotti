package collections;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import com.google.common.collect.Ordering;

/**
 * A {@link SortedList} implementation, based on a modified <a
 * href="http://en.wikipedia.org/wiki/Red-black_tree">red-black tree</a>.
 * Elements are ordered from <i>least</i> to <i>greatest</i> according to their
 * <i>natural ordering</i>, or by an explicit {@link Comparator} provided at
 * creation. Attempting to remove or insert {@code null} elements will fail
 * cleanly and safely leaving this list unmodified. Querying for {@code null}
 * elements is allowed. Inserting non-comparable elements will result in a
 * {@code ClassCastException}. The {@code add(int, E)}, {@code addAll(int,
 * Collection)}, and {@code set(int, E)} operations are not supported.
 * <p>
 * The iterators obtained from the {@link #iterator()} and
 * {@link #listIterator()} methods are <i>fail-fast</i>. Attempts to modify the
 * elements in this list at any time after an iterator is created, in any way
 * except through the iterator's own remove method, will result in a {@code
 * ConcurrentModificationException}. Further, the list iterator does not support
 * the {@code add(E)} and {@code set(E)} operations.
 * <p>
 * This list not <i>thread-safe</i>. If multiple threads modify this list
 * concurrently it must be synchronized externally, considering "wrapping" the
 * list using the {@code Collections.synchronizedList(List)} method.
 * <p>
 * <b>Implementation Note:</b>This implementation uses a comparator (whether or
 * not one is explicitly provided) to maintain priority order, and {@code
 * equals} when testing for element equality. The ordering imposed by the
 * comparator must be <i>consistent with equals</i> if this list is to function
 * correctly.
 * <p>
 * The underlying red-black tree provides the following worst case running time
 * (where <i>n</i> is the size of this list, and <i>m</i> is the size of the
 * specified collection):
 * <p>
 * <table border cellpadding="3" cellspacing="1">
 * <tr>
 * <th align="center">Method</th>
 * <th align="center">Running Time</th>
 * </tr>
 * <tr>
 * <td>
 * {@link #addAll(Collection)}<br>
 * {@link #containsAll(Collection) containsAll(Collection)}</br>
 * {@link #retainAll(Collection) retainAll(Collection)}</br>
 * {@link #removeAll(Collection) removeAll(Collection)}</td>
 * <td align="center"><i>O(m log n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #clear() clear()}<br>
 * {@link #indexOf(Object)}<br>
 * {@link #lastIndexOf(Object)}<br>
 * {@link #get(int)}<br>
 * {@link #remove(int)}</br></td>
 * <td align="center"><i>O(n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #add(Object) add(E)}</br> {@link #contains(Object)}</br>
 * {@link #remove(Object)}</br></td>
 * <td align="center"><i>O(log n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #isEmpty() isEmpty()}</br> {@link #size()}<br>
 * </td>
 * <td align="center"><i>O(1)</i></td>
 * </tr>
 * </table>
 * <p>
 * A list obtained from the {@link #headList(Object) headList(E)},
 * {@link #subList(int, int) subList(int, int)},
 * {@link #subList(Object, Object) subList(E, E)}, and {@link #tailList(Object)
 * tailSet(E)} methods provides identical running time to that of a standard
 * linked list. The {@code add}, {@code addAll}, and {@code set} operations are
 * not supported. The {@code remove} operations are supported.
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements maintained by this list
 */
public class TreeList<E> extends AbstractList<E> implements SortedList<E>,
		Serializable {

	private int size = 0;
	private Node max = null;
	private Node min = null;
	private Node root = null;
	private static final boolean RED = false;
	private static final boolean BLACK = true;
	transient private Comparator<? super E> comparator;
	private static final long serialVersionUID = 1L;

	private TreeList(final Comparator<? super E> comparator) {
		if (comparator != null)
			this.comparator = comparator;
		else
			this.comparator = (Comparator<? super E>) Ordering.natural();
	}

	private TreeList(final Iterable<? extends E> elements) {
		if (elements instanceof SortedList<?>)
			comparator = ((SortedList) elements).comparator();
		else if (elements instanceof SortedSet<?>)
			comparator = ((SortedSet) elements).comparator();
		else if (elements instanceof java.util.PriorityQueue<?>)
			comparator = ((java.util.PriorityQueue) elements).comparator();
		else if (elements instanceof PriorityQueue<?>)
			comparator = ((PriorityQueue) elements).comparator();
		else
			comparator = (Comparator<? super E>) Ordering.natural();
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
	public static <E> TreeList<E> create() {
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
	 * {@code Iterable}. If the specified iterable is an instance of a {@code
	 * SortedSet}, {@code java.util.PriorityQueue java.util.PriorityQueue},
	 * {@code PriorityQueue}, or this {@code TreeList}, this list will be
	 * ordered according to the same ordering. Otherwise, this list will be
	 * ordered according to the {@link Comparable natural ordering} of its
	 * elements.
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
	@SuppressWarnings("unchecked")
	public static <E> TreeList<E> create(final Iterable<? extends E> elements) {
		checkNotNull(elements);
		return new TreeList(elements);
	}

	/**
	 * {@inheritDoc} If one was not explicitly provided a <i>natural order</i>
	 * comparator is returned.
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
		Node newNode;
		if (root == null)
			min = max = root = new Node(e, null);
		else {
			int cmp;
			Node parent;
			Node t = root;
			do {
				parent = t;
				cmp = comparator.compare(e, t.element);
				if (cmp <= 0)
					t = t.left;
				else
					t = t.right;
			} while (t != null);
			newNode = new Node(e, parent);
			if (cmp <= 0)
				parent.left = newNode;
			else
				parent.right = newNode;
			fixAfterInsertion(newNode);
			if (comparator.compare(e, max.element) > 0)
				max = newNode;
			else if (comparator.compare(e, min.element) <= 0)
				min = newNode;
		}
		size++;
		modCount++;
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
	public boolean addAll(Collection<? extends E> c) {
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
			return super.indexOf(o);
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
			Node prev = null;
			Node last = null;
			int expectedModCount = modCount;

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return index < size;
			}

			@Override
			public boolean hasPrevious() {
				return index > 0;
			}

			@Override
			public E next() {
				if (index == size)
					throw new NoSuchElementException();
				checkForConcurrentModification();
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
				if (index == 0)
					throw new NoSuchElementException();
				checkForConcurrentModification();
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
				checkState(last != null);
				checkForConcurrentModification();
				if (last.left != null && last.right != null)
					next = last;
				delete(last);
				index--;
				expectedModCount = modCount;
				last = null;
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
	public List<E> headList(E toElement) {
		checkNotNull(toElement);
		Iterator<E> itor = iterator();
		int i = 0;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0)
			i++;
		return subList(0, i);
	}

	@Override
	public List<E> subList(E fromElement, E toElement) {
		checkNotNull(fromElement);
		checkNotNull(toElement);
		checkState(comparator.compare(fromElement, toElement) <= 0);
		Iterator<E> itor = iterator();
		int fromIndex = 0;
		while (itor.hasNext()
				&& comparator.compare(itor.next(), fromElement) < 0)
			fromIndex++;
		int toIndex = fromIndex + 1;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0)
			toIndex++;
		return subList(fromIndex, toIndex);
	}

	@Override
	public List<E> tailList(E fromElement) {
		checkNotNull(fromElement);
		Iterator<E> itor = iterator();
		int i = 0;
		while (itor.hasNext()
				&& comparator.compare(itor.next(), fromElement) < 0)
			i++;
		return subList(i, size);
	}

	// serializable object
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		s.defaultWriteObject();
		s.writeInt(size);
		s.writeObject(comparator);
		for (E e : this)
			s.writeObject(e);
	}

	// deserializable object
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		int size = s.readInt();
		comparator = (Comparator<? super E>) s.readObject();
		for (int i = 0; i < size; i++)
			add((E) s.readObject());
	}

	// Red-Black-Tree methods

	protected class Node {
		E element;
		Node left = null;
		Node right = null;
		Node parent;
		boolean color = BLACK;

		Node(final E element, final Node parent) {
			this.element = element;
			this.parent = parent;
		}
	}

	protected Node search(final E e) {
		Node node = root;
		while (node != null && e != null) {
			int cmp = comparator.compare(e, node.element);
			if (cmp == 0 && e.equals(node.element))
				return node;
			if (cmp <= 0)
				node = node.left;
			else
				node = node.right;
		}
		return null;
	}

	protected void delete(Node node) {
		size--;
		modCount++;
		if (max == node)
			max = predecessor(node);
		if (min == node)
			min = successor(node);
		if (node.left != null && node.right != null) {
			Node successor = successor(node);
			node.element = successor.element;
			node = successor;
		}
		Node replacement = (node.left != null ? node.left : node.right);
		if (replacement != null) {
			replacement.parent = node.parent;
			if (node.parent == null)
				root = replacement;
			else if (node == node.parent.left)
				node.parent.left = replacement;
			else
				node.parent.right = replacement;
			node.left = node.right = node.parent = null;
			if (node.color == BLACK)
				fixAfterDeletion(replacement);
		} else if (node.parent == null) {
			root = null;
		} else {
			if (node.color == BLACK)
				fixAfterDeletion(node);
			if (node.parent != null) {
				if (node == node.parent.left)
					node.parent.left = null;
				else if (node == node.parent.right)
					node.parent.right = null;
				node.parent = null;
			}
		}
	}

	private Node successor(final Node node) {
		if (node == null)
			return null;
		else if (node.right != null) {
			Node successor = node.right;
			while (successor.left != null)
				successor = successor.left;
			return successor;
		} else {
			Node parent = node.parent;
			Node child = node;
			while (parent != null && child == parent.right) {
				child = parent;
				parent = parent.parent;
			}
			return parent;
		}
	}

	protected Node predecessor(final Node node) {
		if (node == null)
			return null;
		else if (node.left != null) {
			Node predecessor = node.left;
			while (predecessor.right != null)
				predecessor = predecessor.right;
			return predecessor;
		} else {
			Node parent = node.parent;
			Node child = node;
			while (parent != null && child == parent.left) {
				child = parent;
				parent = parent.parent;
			}
			return parent;
		}
	}

	private void rotateLeft(final Node node) {
		if (node != null) {
			Node temp = node.right;
			node.right = temp.left;
			if (temp.left != null)
				temp.left.parent = node;
			temp.parent = node.parent;
			if (node.parent == null)
				root = temp;
			else if (node.parent.left == node)
				node.parent.left = temp;
			else
				node.parent.right = temp;
			temp.left = node;
			node.parent = temp;
		}
	}

	private void rotateRight(final Node node) {
		if (node != null) {
			Node temp = node.left;
			node.left = temp.right;
			if (temp.right != null)
				temp.right.parent = node;
			temp.parent = node.parent;
			if (node.parent == null)
				root = temp;
			else if (node.parent.right == node)
				node.parent.right = temp;
			else
				node.parent.left = temp;
			temp.right = node;
			node.parent = temp;
		}
	}

	private void fixAfterInsertion(Node node) {
		node.color = RED;
		while (node != null && node != root && node.parent.color == RED) {
			if (getParent(node) == getLeftChild(getParent(getParent(node)))) {
				Node y = getRightChild(getParent(getParent(node)));
				if (getColor(y) == RED) {
					setColor(getParent(node), BLACK);
					setColor(y, BLACK);
					setColor(getParent(getParent(node)), RED);
					node = getParent(getParent(node));
				} else {
					if (node == getRightChild(getParent(node))) {
						node = getParent(node);
						rotateLeft(node);
					}
					setColor(getParent(node), BLACK);
					setColor(getParent(getParent(node)), RED);
					rotateRight(getParent(getParent(node)));
				}
			} else {
				Node y = getLeftChild(getParent(getParent(node)));
				if (getColor(y) == RED) {
					setColor(getParent(node), BLACK);
					setColor(y, BLACK);
					setColor(getParent(getParent(node)), RED);
					node = getParent(getParent(node));
				} else {
					if (node == getLeftChild(getParent(node))) {
						node = getParent(node);
						rotateRight(node);
					}
					setColor(getParent(node), BLACK);
					setColor(getParent(getParent(node)), RED);
					rotateLeft(getParent(getParent(node)));
				}
			}
		}
		root.color = BLACK;
	}

	private void fixAfterDeletion(Node node) {
		while (node != root && getColor(node) == BLACK) {
			if (node == getLeftChild(getParent(node))) {
				Node sib = getRightChild(getParent(node));
				if (getColor(sib) == RED) {
					setColor(sib, BLACK);
					setColor(getParent(node), RED);
					rotateLeft(getParent(node));
					sib = getRightChild(getParent(node));
				}
				if (getColor(getLeftChild(sib)) == BLACK
						&& getColor(getRightChild(sib)) == BLACK) {
					setColor(sib, RED);
					node = getParent(node);
				} else {
					if (getColor(getRightChild(sib)) == BLACK) {
						setColor(getLeftChild(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = getRightChild(getParent(node));
					}
					setColor(sib, getColor(getParent(node)));
					setColor(getParent(node), BLACK);
					setColor(getRightChild(sib), BLACK);
					rotateLeft(getParent(node));
					node = root;
				}
			} else {
				Node sib = getLeftChild(getParent(node));
				if (getColor(sib) == RED) {
					setColor(sib, BLACK);
					setColor(getParent(node), RED);
					rotateRight(getParent(node));
					sib = getLeftChild(getParent(node));
				}
				if (getColor(getRightChild(sib)) == BLACK
						&& getColor(getLeftChild(sib)) == BLACK) {
					setColor(sib, RED);
					node = getParent(node);
				} else {
					if (getColor(getLeftChild(sib)) == BLACK) {
						setColor(getRightChild(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = getLeftChild(getParent(node));
					}
					setColor(sib, getColor(getParent(node)));
					setColor(getParent(node), BLACK);
					setColor(getLeftChild(sib), BLACK);
					rotateRight(getParent(node));
					node = root;
				}
			}
		}
		setColor(node, BLACK);
	}

	private boolean getColor(final Node p) {
		return (p == null ? BLACK : p.color);
	}

	private Node getParent(final Node p) {
		return (p == null ? null : p.parent);
	}

	private void setColor(final Node p, final boolean c) {
		if (p != null)
			p.color = c;
	}

	private Node getLeftChild(final Node p) {
		return (p == null) ? null : p.left;
	}

	private Node getRightChild(final Node p) {
		return (p == null) ? null : p.right;
	}

}