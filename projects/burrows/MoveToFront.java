import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

import java.util.ArrayList;
import java.util.List;

public class MoveToFront {

    private static final int R = 256;

    // apply move-to-front encoding, reading from standard input and writing to standard output
    public static void encode() {
        List<Character> allAscii = new ArrayList<>(R);
        initialAscii(allAscii);
        while (!BinaryStdIn.isEmpty()) {
            char c = BinaryStdIn.readChar();
            int index = allAscii.indexOf(c);
            allAscii.remove(index);
            allAscii.add(0, c);
            BinaryStdOut.write(index, 8);
        }
        BinaryStdOut.close();
    }

    private static void initialAscii(List<Character> allAscii) {
        for (int i = 0; i < R; i++) {
            allAscii.add(i, (char) i);
        }
    }

    // apply move-to-front decoding, reading from standard input and writing to standard output
    public static void decode() {
        List<Character> allAscii = new ArrayList<>(R);
        initialAscii(allAscii);
        while (!BinaryStdIn.isEmpty()) {
            int index = BinaryStdIn.readChar();
            char c = allAscii.get(index);
            allAscii.remove(index);
            allAscii.add(0, c);
            BinaryStdOut.write(c);
        }
        BinaryStdOut.close();
    }

    // if args[0] is "-", apply move-to-front encoding
    // if args[0] is "+", apply move-to-front decoding
    public static void main(String[] args) {
        String sign = args[0];
        if (sign.equals("-")) {
            encode();
        } else if (sign.equals("+")) {
            decode();
        }
    }
}
