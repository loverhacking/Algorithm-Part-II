import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class SeamCarver {

    private static final double BOARDENERGY = 1000;

    /**
     * store energy and color for every pixel
     * notice that it's organized by height * width (i.e. row * col)
     * which is different from Color object
     */
    private int[][] color;
    private double[][] energyPixel;

    /** width of current picture */
    private int width;

    /** height of current picture */
    private int height;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }

        width = picture.width();
        height = picture.height();

        initColor(picture);
        initEnergy();
    }

    private void initColor(Picture picture) {
        color = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // for java.awt.Color, it's newly updated and use getARGB
                // but for project test, use getRGB instead to pass
                color[y][x] = picture.getARGB(x, y);
            }
        }
    }

    private void initEnergy() {
        energyPixel = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                energyPixel[y][x] = energy(x, y);
            }
        }
    }

    // current picture
    public Picture picture() {
        Picture copy = new Picture(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy.set(x, y, new Color(color[y][x]));
            }
        }
        return copy;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (!checkInRange(x, y)) {
            throw new IllegalArgumentException();
        }

        if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
            return BOARDENERGY;
        }

        double squareXGradient = calculateSquareGradient(color[y][x - 1], color[y][x + 1]);
        double squareYGradient = calculateSquareGradient(color[y - 1][x], color[y + 1][x]);

        return Math.sqrt(squareXGradient + squareYGradient);
    }

    private int calculateSquareGradient(int color1, int color2) {
        int color1Red = getRed(color1);
        int color2Red = getRed(color2);

        int color1Blue = getBlue(color1);
        int color2Blue = getBlue(color2);

        int color1Green = getGreen(color1);
        int color2Green = getGreen(color2);
        return (color1Red - color2Red) * (color1Red - color2Red)
                + (color1Blue - color2Blue) * (color1Blue - color2Blue)
                + (color1Green - color2Green) * (color1Green - color2Green);
    }

    // helper methods to get int
    private int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }
    private int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }
    private int getBlue(int rgb) {
        return (rgb) & 0xFF;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {

        /** minimum cost path ending at (row i, col j) */
        double[][] minCost = new double[height][width];

        int[] horizontalSeam = new int[width];

        for (int j = 0; j < width; j++) {
            for (int i = 0; i < height; i++) {
                minCost[i][j] = energyPixel[i][j] + minHorizontalEnergyCost(i, j, minCost);
            }
        }

        // find min value in the last column in minCost[][]
        double min = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        for (int i = 0; i < height; i++) {
            if (minCost[i][width - 1] < min) {
                min = minCost[i][width - 1];
                minIndex = i;
            }
        }
        horizontalSeam[width - 1] = minIndex;

        // trace back from the last column to form path
        int i = minIndex;
        for (int j = width - 1; j >= 1; j--) {
            i = minHorizontalCost(i, j, minCost);
            horizontalSeam[j - 1] = i;
        }
        return horizontalSeam;
    }

    /** find pathTo[i][j] given pixel (row i, col j) */
    private int minHorizontalCost(int i, int j, double[][] minCost) {

        // special case: height == 1: no need to compare
        if (height == 1) {
            return i;
        }

        if (i == 0) {
            return minCost[i][j - 1] > minCost[i + 1][j - 1] ? i + 1 : i;
        }
        if (i == height - 1) {
            return minCost[i - 1][j - 1] > minCost[i][j - 1] ? i : i - 1;
        }

        int tempMin = minCost[i - 1][j - 1] > minCost[i][j - 1] ? i : i - 1;
        return minCost[tempMin][j - 1] > minCost[i + 1][j - 1] ? i + 1 : tempMin;
    }

    /** find disTo[i][j] given pixel (row i, col j) */
    private double minHorizontalEnergyCost(int i, int j, double[][] minCost) {
        if (j == 0) {
            return 0;
        }

        // special case: height == 1: no need to compare
        if (height == 1) {
            return minCost[i][j - 1];
        }

        if (i == 0) {
            return Math.min(minCost[i][j - 1], minCost[i + 1][j - 1]);
        }
        if (i == height - 1) {
            return Math.min(minCost[i][j - 1], minCost[i - 1][j - 1]);
        }
        return Math.min(minCost[i - 1][j - 1], Math.min(minCost[i][j - 1], minCost[i + 1][j - 1]));
    }


    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {

        /** minimum cost path ending at (i, j) */
        double[][] minCost = new double[height][width];

        int[] verticalSeam = new int[height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                minCost[i][j] = energyPixel[i][j] + minVerticalEnergyCost(i, j, minCost);
            }
        }

        // find min value in last row in M[][]
        double min = Double.POSITIVE_INFINITY;
        int minIndex = 0;
        for (int j = 0; j < width; j++) {
            if (minCost[height - 1][j] < min) {
                min = minCost[height - 1][j];
                minIndex = j;
            }
        }
        verticalSeam[height - 1] = minIndex;

        // trace back from the last row to form path
        int j = minIndex;
        for (int i = height - 1; i >= 1; i--) {
            j = minVerticalCost(i, j, minCost);
            verticalSeam[i - 1] = j;
        }
        return verticalSeam;
    }

    /** find pathTo[i][j] given pixel (row i, col j) */
    private int minVerticalCost(int i, int j, double[][] minCost) {

        // special case: width == 1: no need to compare
        if (width == 1) {
            return j;
        }

        if (j == 0) {
            return minCost[i - 1][j] > minCost[i - 1][j + 1] ? j + 1 : j;
        }
        if (j == width - 1) {
            return minCost[i - 1][j - 1] > minCost[i - 1][j] ? j : j - 1;
        }

        int tempMin = minCost[i - 1][j - 1] > minCost[i - 1][j] ? j : j - 1;
        return minCost[i - 1][tempMin] > minCost[i - 1][j + 1] ? j + 1 : tempMin;

    }

    /** find disTo[i][j] given pixel (row i, col j) */
    private double minVerticalEnergyCost(int i, int j, double[][] minCost) {
        if (i == 0) {
            return 0;
        }

        // special case: width == 1: no need to compare
        if (width == 1) {
            return minCost[i - 1][j];
        }

        if (j == 0) {
            return Math.min(minCost[i - 1][j], minCost[i - 1][j + 1]);
        }
        if (j == width - 1) {
            return Math.min(minCost[i - 1][j - 1], minCost[i - 1][j]);
        }
        return Math.min(minCost[i - 1][j - 1], Math.min(minCost[i - 1][j], minCost[i - 1][j + 1]));
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        if (height <= 1) {
            throw new IllegalArgumentException();
        }
        checkSeam(seam, width);

        height = height - 1;

        int[][] newColor = new int[height][width];
        double[][] newEnergyPixel = new double[height][width];

        // update color[][]
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (j < seam[i]) {
                    newColor[j][i] = color[j][i];
                } else {
                    newColor[j][i] = color[j + 1][i];
                }
            }
        }
        color = newColor;

        // only recompute the neighbor pixel's energy of removed pixel
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (j < seam[i] - 1) {
                    newEnergyPixel[j][i] = energyPixel[j][i];
                } else if (j > seam[i]) {
                    newEnergyPixel[j][i] = energyPixel[j + 1][i];
                } else {
                    newEnergyPixel[j][i] = energy(i, j);
                }
            }

        }
        energyPixel = newEnergyPixel;
    }


    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException();
        }

        if (width <= 1) {
            throw new IllegalArgumentException();
        }

        checkSeam(seam, height);

        width = width - 1;

        int[][] newColor = new int[height][width];
        double[][] newEnergyPixel = new double[height][width];

        // update color[][]
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (i < seam[j]) {
                    newColor[j][i] = color[j][i];
                } else {
                    newColor[j][i] = color[j][i + 1];
                }
            }
        }
        color = newColor;

        // only recompute the neighbor pixel's energy of removed pixel
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (i < seam[j] - 1) {
                    newEnergyPixel[j][i] = energyPixel[j][i];
                } else if (i > seam[j]) {
                    newEnergyPixel[j][i] = energyPixel[j][i + 1];
                } else {
                    newEnergyPixel[j][i] = energy(i, j);
                }
            }
        }
        energyPixel = newEnergyPixel;
    }

    private boolean checkInRange(int x, int y) {
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }

    private void checkSeam(int[] seam, int seamLength) {

        if (seam.length != seamLength) {
            throw new IllegalArgumentException();
        }
        int m;
        if (seamLength == width) {
            m = height;
        } else {
            m = width;
        }

        for (int i = 0; i < seamLength - 1; i++) {
            if (Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException();
            }
            if (seam[i] < 0 || seam[i] >= m) {
                throw new IllegalArgumentException();
            }
        }

        if (seam[seamLength - 1] < 0 || seam[seamLength - 1] >= m) {
            throw new IllegalArgumentException();
        }
    }
}

