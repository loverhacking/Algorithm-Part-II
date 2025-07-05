
import java.util.Arrays;

public class CircularSuffixArray {

    private final int length;
    private final int[] index;

    private static class SuffixArray implements Comparable<SuffixArray> {
        int index;
        String s;
        SuffixArray(int index, String s) {
            this.index = index;
            this.s = s;
        }

        char charAt(int i) {
            return s.charAt(index + i);
        }

        int length() {
            return s.length() / 2;
        }

        @Override
        public int compareTo(SuffixArray that) {
            if (this == that) return 0;
            for (int i = 0; i < this.length(); i++) {
                if (this.charAt(i) > that.charAt(i))      return 1;
                else if (this.charAt(i) < that.charAt(i)) return -1;
            }
            return 0;
        }
    }

    // circular suffix array of s
    public CircularSuffixArray(String s) {
        if (s ==  null) {
            throw new IllegalArgumentException();
        }
        length = s.length();
        String t = s + s;
        index = new int[length];
        createCSA(t);
    }

    private void createCSA(String t) {
        SuffixArray[] suffixes = new SuffixArray[length];
        /** Notice: beginning with Java 7, Update 6, the substring() method takes time and space
         * proportional to the length of the substring.
         * So, explicitly forming the n circular suffixes in this way
         * would take both quadratic time and space.
         * so we create nested class SuffixArray and override compareTo method
         */
        for (int i = 0; i < length; i++) {
            suffixes[i] = new SuffixArray(i, t);
        }
        Arrays.sort(suffixes);
        for (int i = 0; i < length; i++) {
            index[i] = suffixes[i].index;
        }
    }

    // length of s
    public int length() {
        return length;
    }

    // returns index of ith sorted suffix
    public int index(int i) {
        if (i < 0 || i >= length) {
            throw new IllegalArgumentException();
        }
        return index[i];
    }

    // unit testing (required)
    public static void main(String[] args) {
        String s = "ABRACADABRA!";
        CircularSuffixArray circularSuffixArray = new CircularSuffixArray(s);
        System.out.println("length of s: " + circularSuffixArray.length());
        for (int i = 0; i < circularSuffixArray.length(); i++) {
            System.out.println("The index of " + i + "th sorted suffix is: " + circularSuffixArray.index(i));
        }
    }

}
