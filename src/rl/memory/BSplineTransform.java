/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import rl.domain.State;
import rl.functionapproximation.BSplinePhi;
import rl.functionapproximation.BSplinePsi;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.CombinationFunction;

/**
 *
 * @author Craig Bester
 */
public class BSplineTransform extends Transform {

    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    private final int order;

    final int height;
    final int width;
    int numFeatures;

    private static BasisFunction[] terms;
    private static double[][][] waveletCache;

    public BSplineTransform(int height, int width, int baseScale, int maxScale, int order) {
        this.order = order;
        this.dimensions = 2;
        this.numFeatures = calculateNumTerms(baseScale, maxScale, dimensions, order);
        this.height = height;
        this.width = width;

        this.maxScale = maxScale;
        this.baseScale = baseScale;
        initialiseTerms();
        initialiseCache();

        // random weight initialisation, prevent incorrect feature correlation?
        //for(int i = 0; i < numFeatures; i++) weights[i] = random.nextDouble();
        //Arrays.fill(weights, 0);
        // Optimistic?
        //Arrays.fill(weights,0.25);
    }

    @Override
    public double[] transform(Frame fimg) {
        // avoid extra memory
        //if(phi == null) phi = new double[numFeatures];
        double[] phi = new double[numFeatures];
        double[][] img = fimg.getImage();

        for (int k = 0; k < numFeatures; k++) {
            phi[k] = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    phi[k] += img[y][x] * waveletCache[k][y][x];
                }
            }
        }

        return phi;
    }

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions, int order) {
        int numPhi = (int) Math.pow(2, baseScale) + order;
        if (maxScale < baseScale) {
            return (int) Math.pow(numPhi, dimensions);
        }
        if (maxScale == baseScale) {
            return (int) Math.pow(2, dimensions) * (int) Math.pow(numPhi, dimensions);
        }
        int tot = (int) Math.pow(numPhi, dimensions);
        for (int i = baseScale; i <= maxScale; i++) {
            tot += ((int) Math.pow(2, dimensions) - 1) * (int) Math.pow(Math.pow(2, i) + order, dimensions);
        }
        return tot;
    }

    public BasisFunction[][] getBasicPsiWavelets(int scale) {
        int numLocations = 2 * order + (int) Math.pow(2, scale);
        BasisFunction basicWavelets[][] = new BasisFunction[numLocations][dimensions];
        for (int dimension = 0; dimension < dimensions; dimension++) {
            for (int location = -2 * order; location < (int) Math.pow(2, scale); location++) {
                basicWavelets[location + 2 * order][dimension] = new BSplinePsi(scale, location, dimension, order);
            }
        }
        return basicWavelets;
    }

    public BasisFunction[][] getBasicPhiWavelets(int scale) {
        int numLocations = order + (int) Math.pow(2, scale);
        BasisFunction basicWavelets[][] = new BasisFunction[numLocations][dimensions];
        for (int dimension = 0; dimension < dimensions; dimension++) {
            for (int location = -order; location < (int) Math.pow(2, scale); location++) {
                basicWavelets[location + order][dimension] = new BSplinePhi(scale, location, dimension, order);
            }
        }
        return basicWavelets;
    }

    public int getNumShifts(int scale) {
        return (int) Math.pow(2, scale) + order;
    }

    public static int getNumShifts(int scale, int order) {
        return (int) Math.pow(2, scale) + order;
    }

    public int[][] getIndexLattice(int scale) {
        int numLocations = getNumShifts(scale);
        int lattice[][] = new int[(int) Math.pow(numLocations, dimensions)][dimensions];
        for (int i = 0; i < Math.pow(numLocations, dimensions); i++) {
            int icopy = i;
            for (int j = dimensions - 1; j > -1; j--) {
                lattice[i][dimensions - j - 1] = icopy / (int) Math.pow(numLocations, j);
                icopy = icopy % (int) Math.pow(numLocations, j);
            }
        }
        return lattice;
    }

    private void initialiseTerms() {
        if (terms == null) {
            terms = new BasisFunction[numFeatures];
            //System.err.println(terms.length);
            //intialise scaling functions
            int lattice[][];
            BasisFunction basicPhiWavelets[][], basicPsiWavelets[][];
            BasisFunction b;
            int pos = 0;
            //System.err.print(dimensions);
            //BasisFunction terms[] = new BasisFunction[WaveletTensorBasis.calculateNumTerms(baseScale, maxScale, dimensions, order)];
            // first, construct scaling tile terms
            basicPhiWavelets = getBasicPhiWavelets(baseScale);
            lattice = getIndexLattice(baseScale);
            for (int i = 0; i < lattice.length; i++) {
                b = basicPhiWavelets[lattice[i][0]][0];
                for (int j = 1; j < dimensions; j++) { // to change
                    //System.err.print("Help");
                    b = new CombinationFunction(b, basicPhiWavelets[lattice[i][j]][j]);
                }
                //System.err.println(b.getBasisString());
                terms[pos] = b;
                pos++;
            }
                // then add in wavelet terms as needed
            //        if (baseScale<=maxScale) {
            //            for (int scale = baseScale; scale<maxScale+1; scale++) {
            //                if (scale>baseScale) {
            //                    basicPhiWavelets = getBasicPhiWavelets(scale);
            //                    basicPsiWavelets = getBasicPsiWavelets(scale);
            //                    lattice = getIndexLattice(scale);
            //                }
            //                for (int i=0; i<lattice.length; i++) {
            //                    for (int G=1; G<Math.pow(2,dimensions); G++) {
            //                        int Gcopy = G;
            //                        Gcopy / (int)Math.pow(2, dimensions-1)
            //                        b = (WiFDDBasisFunction)basicWavelets[][lattice[i][0]][0];
            //                        Gcopy = Gcopy % (int)Math.pow(2, dimensions-1);
            //                        for (int j=1; j<dimensions; j++) { // to change
            //                            b = new CombinationBasis(b, (WiFDDBasisFunction)basicWavelets[Gcopy / (int)Math.pow(2, dimensions-1-j)][lattice[i][j]][j]);
            //                            Gcopy = Gcopy % (int)Math.pow(2, dimensions-1-j);
            //                        }
            //                        terms[pos] = b;
            //                        //System.err.println(b.getBasisString());
            //                        pos++;
            //                    }
            //                }
            //            }
            //        }

        }
        /*
         System.err.println(pos);
         System.err.println(terms.length);
         for (int i = 0; i < terms.length; i++) {
         System.err.println(terms[i].getBasisString());
         }
         * 
         */
    }

    private void initialiseCache() {
        if (waveletCache == null) {
            waveletCache = new double[numFeatures][height][width];
            //System.out.println(terms.length);
            //intialise scaling functions
            for (int i = 0; i < numFeatures; i++) {
                //System.err.println(terms[i].getBasisString()+":");
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double ys = ((double) y) / (height - 1);
                        double xs = ((double) x) / (width - 1);
                        //waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{y,x}));
                        waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{ys, xs}));
                        //System.err.println(y+","+x+": "+waveletCache[i][y][x]);
                    }
                }
            }
        }
        /*
         System.out.println(pos);
         System.out.println(terms.length);
         for (int i = 0; i < terms.length; i++) {
         System.out.println(terms[i].getBasisString());
         }
         * 
         */
    }
}
