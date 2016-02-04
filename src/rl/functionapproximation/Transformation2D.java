package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author itchallengeuser
 */
public class Transformation2D {

    private final int width;
    private final int height;
    private final BasisFunction[] basisFunctions;
    private double[][][] transformCache;

    /**
     * Creates a 2d transformation object.
     *
     * @param width The width of the space as an integer.
     * @param height The height of the space as an integer.
     * @param basisFunctions An array of basis functions which operate in 2
     * dimensions over [0,1] in each dimension.
     */
    public Transformation2D(int width, int height, BasisFunction basisFunctions[]) {
        this.width = width;
        this.height = height;
        this.basisFunctions = basisFunctions;
    }

    /**
     * Transforms a 2d matrix into a 1d array using the provided basis
     * functions.
     *
     * @param originalSpace
     * @return
     */
    public double[] transform(double originalSpace[][]) {
        double[] phi = new double[basisFunctions.length];
        for (int j = 0; j < basisFunctions.length; j++) {
            double normalisation = 0;
            int count = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // x,y already scaled
                    phi[j] += originalSpace[x][y] * getBasisValue(j, x, y);
                    normalisation += Math.pow(getBasisValue(j, x, y),2);
                            count++;
                }
            }
            //if a basis function is uncorrelated to the image, its weight should be 0.
            
            if (normalisation < 0.00001) {
                phi[j] = 0;
            } else {
                phi[j] = phi[j] / normalisation;
            }
            
            
            
            //phi[j] = phi[j] /count;
        }
        return phi;
    }

    /**
     * Transforms a array of values of an unrolled 2d matrix and transforms it
     * to a new space using the provided 2d basis functions.
     *
     * @param originalSpaceUnrolled
     * @return
     */
    public double[] transform(double originalSpaceUnrolled[]) {
        double[] phi = new double[basisFunctions.length];
        for (int j = 0; j < basisFunctions.length; j++) {
            double normalisation = 0;
            int count = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int imgIndex = y * width + x;
                    // x,y already scaled in cosine pre-calculation
                    phi[j] += originalSpaceUnrolled[imgIndex] * getBasisValue(j, x, y);
                    normalisation += Math.pow(getBasisValue(j, x, y),2);
                    count++;
                }
            }
            //if a basis function is uncorrelated to the image, its weight should be 0.
            /*
            if (normalisation < 0.00001) {
                phi[j] = 0;
            } else {
                phi[j] = phi[j] / normalisation;
            }
            */
            phi[j] = phi[j]/count;
        }
        return phi;
    }

    protected double getBasisValue(int basisFunctionIndex, int x, int y) {
        if (transformCache == null) {
            transformCache = new double[basisFunctions.length][width][height];
            for (int i = 0; i < basisFunctions.length; i++) {
                for (int k = 0; k < width; k++) {
                    for (int l = 0; l < height; l++) {
                        transformCache[i][k][l] = basisFunctions[i].getValue(new State(new double[]{(double) k / (width - 1), (double) l / (height - 1)}));
                    }
                }
            }
        }
        return transformCache[basisFunctionIndex][x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BasisFunction[] getBasisFunctions() {
        return basisFunctions;
    }

}
