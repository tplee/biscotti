package collections;

/**
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements held in this list
 */
public interface NavigableList<E> extends SortedList<E> {

	/**
	 * Returns a view of the portion of this list containing the <i>least</i>
	 * elements which are greater than or equal to the given element, or
	 * {@code null} if no such elements exist. This implies that if this list
	 * does not contain duplicate elements this method will return a list
	 * containing only a single element or {@code null} if no match can be made.
	 * 
	 * @param e
	 *            the element to match
	 * @return the least elements greater than or equal to {@code e}, or
	 *         {@code null} if there is no such elements
	 */
	public SortedList<E> ceiling(E e);

	/**
	 * Returns a view of the portion of this list containing the <i>greatest</i>
	 * elements which are less than or equal to the given element, or
	 * {@code null} if no such elements exist. This implies that if this list
	 * does not contain duplicate elements this method will return a list
	 * containing only a single element or {@code null} if no match can be made.
	 * 
	 * @param e
	 *            the element to match
	 * @return the least elements greater than or equal to {@code e}, or
	 *         {@code null} if there is no such elements
	 */
	public SortedList<E> floor(E e);

	/**
	 * Returns a view of the portion of this list whose elements are less than
	 * (or equal to, if {@code inclusive} is {@code true}) {@code toElement}.
	 * This implies that if the list contains duplicate elements and
	 * {@code inclusive} is {@code true}, <i>all</i> of the elements which are
	 * equal to {@code toElement} will be included in the returned list. The
	 * returned list is backed by this list, so changes in the returned list are
	 * reflected in this list.
	 * 
	 * @param toElement
	 *            high endpoint (exclusive) of the returned list
	 * @param inclusive
	 *            {@code true} if the high endpoint is to be included in the
	 *            returned view
	 * @return a view of the portion of this list whose elements are strictly
	 *         less than {@code toElement}
	 * @throws ClassCastException
	 *             if {@code toElement} is not comparable to the elements in
	 *             this list
	 * @throws NullPointerException
	 *             if {@code toElement} is {@code null} and this list does not
	 *             permit {@code null} elements
	 */
	public SortedList<E> headList(E toElement, boolean inclusive);

	/**
	 * Returns a view of the portion of this list whose elements range from
	 * {@code fromElement} to {@code toElement}. If {@code fromElement} and
	 * {@code toElement} are equal, the returned list is empty unless
	 * {@code fromInclusive} and {@code toInclusive} are both true. The returned
	 * list is backed by this list, so changes in the returned list are
	 * reflected in this list.
	 * 
	 * @param fromElement
	 *            low endpoint (inclusive) of the returned list
	 * @param fromInclusive
	 *            {@code true} if the low endpoint is to be included in the
	 *            returned view
	 * @param toElement
	 *            high endpoint (exclusive) of the returned list
	 * @param toInclusive
	 *            {@code true} if the high endpoint is to be included in the
	 *            returned view
	 * @return a view of the portion of this list whose elements range from
	 *         {@code fromElement}, inclusive, to {@code toElement}, exclusive
	 * @throws ClassCastException
	 *             if {@code fromElement} or {@code toElement} is not comparable
	 *             to the elements in this list
	 * @throws NullPointerException
	 *             if {@code fromElement} or {@code toElement} is {@code null}
	 *             and this list does not permit {@code null} elements
	 * @throws IllegalArgumentException
	 *             if {@code fromElement} is greater than {@code toElement}
	 */
	public SortedList<E> subList(E fromElement, boolean fromInclusive,
			E toElement, boolean toInclusive);

	/**
	 * Returns a view of the portion of this list whose elements are greater
	 * than (or equal to, if {@code inclusive} is {@code true})
	 * {@code fromElement}. This implies that if the list contains duplicate
	 * elements and {@code inclusive} is {@code true}, <i>all</i> of the
	 * elements which are equal to {@code fromElement}, will be included in the
	 * returned list. The returned list is backed by this list, so changes in
	 * the returned list are reflected in this list.
	 * 
	 * @param fromElement
	 *            low endpoint (inclusive) of the returned list
	 * @param inclusive
	 *            {@code true} if the low endpoint is to be included in the
	 *            returned view
	 * @return a view of the portion of this list whose elements are greater
	 *         than or equal to {@code fromElement}
	 * @throws ClassCastException
	 *             if {@code fromElement} is not comparable to the elements in
	 *             this list
	 * @throws NullPointerException
	 *             if {@code fromElement} is {@code null} and this list does not
	 *             permit {@code null} elements
	 */
	public SortedList<E> tailList(E fromElement, boolean inclusive);
}