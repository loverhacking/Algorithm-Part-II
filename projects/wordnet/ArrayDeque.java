
public class ArrayDeque {

    /** the current number of elemnets in array */
    private int size;

    private int[] array;

    /** the length of current array */
    private final int length;

    /** the start pointer of array */
    private int start;

    /** the end pointer of array */
    private int end;


    /** Invariants
     * the first to be filled index is always be start.
     * the last to be filled index is always be end.
     */
    public ArrayDeque(int length) {
        array = new int[length];
        this.length = length;

        /* start fill elements in the middle */
        start = 0;
        end = 1;
        size = 0;
    }

    /** Adds an item of type int to the back of the deque */
    public void addLast(int item) {

        /* end meet the end boundary of array */
        if (end == length && size != length) {
            end = 0;
        }

        array[end] = item;

        size += 1;
        end += 1;
    }

    /** Returns true if deque is empty, false otherwise. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null */
    public int removeFirst() {

        if (size == 0) {
            return -1;
        }

        int first;

        /** decide whether the first element is in the front of array i.e. circular
         * or just the normal array case. */
        if (start == length - 1) {
            start = 0;
            first = array[start];
        } else {
            start += 1;
            first = array[start];
            array[start] = -1;
        }

        size -= 1;

        return first;
    }
}
