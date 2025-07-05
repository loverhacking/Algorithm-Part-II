import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class BurrowsWheeler {

    private static final int R = 256;
    // apply Burrows-Wheeler transform,
    // reading from standard input and writing to standard output
    public static void transform() {
        int first = 0;
        String s = BinaryStdIn.readString();
        CircularSuffixArray suffixArray = new CircularSuffixArray(s);
        int length = suffixArray.length();
        char[] t = new char[length];
        for (int i = 0; i < length; i++) {
            int index = suffixArray.index(i);
            if (index == 0) {
                first = i;
            }
            int num = (suffixArray.index(i) + length - 1) % length;
            t[i] = s.charAt(num);
        }
        BinaryStdOut.write(first);
        for (int i = 0; i < length; i++) {
            BinaryStdOut.write(t[i]);
        }
        BinaryStdOut.close();
    }

    // apply Burrows-Wheeler inverse transform,
    // reading from standard input and writing to standard output
    public static void inverseTransform() {
        int first = BinaryStdIn.readInt();
        char[] t = BinaryStdIn.readString().toCharArray();

        int[] next = new int[t.length];
        char[] firstCol = new char[t.length];
        constructNextArray(t, next, firstCol);

        char[] origin = new char[t.length];
        int tmp = first;
        for (int i = 0; i < t.length; i++) {
            origin[i] = firstCol[tmp];
            tmp = next[tmp];
        }
        for (int i = 0; i < origin.length; i++) {
            BinaryStdOut.write(origin[i]);
        }
        BinaryStdOut.close();
    }

    /**
     * Consider: if using the key-indexed counting method to sort the t[] array,
     * then we can get all the information with no extra memory or code.
     * Note: the next[] array establishes the mapping relationship between the first and last columns
     * since the first column is in sorted order, we can iterate over the t[] array,
     * to find t[i] element position in first column, i.e. its position in sorted order.
     * that's exactly what the count[] array is doing.
     * i.e. next[count[t[i]]] = i;
     */
    private static void constructNextArray(char[] t, int[] next, char[] firstCol) {
        int[] count = new int[R + 1];
        for (int i = 0; i < t.length; i++)
            count[t[i] + 1]++;
        for (int i = 0; i < R; i++)
            count[i + 1] += count[i];
        // The trickiest part
        for (int i = 0; i < t.length; i++) {
            next[count[t[i]]] = i;
            firstCol[count[t[i]]] = t[i];
            count[t[i]]++;
        }
    }

    // if args[0] is "-", apply Burrows-Wheeler transform
    // if args[0] is "+", apply Burrows-Wheeler inverse transform
    public static void main(String[] args) {
        String s = args[0];
        if (s.equals("-")) {
            transform();
        } else if (s.equals("+")) {
            inverseTransform();
        }
    }

}
