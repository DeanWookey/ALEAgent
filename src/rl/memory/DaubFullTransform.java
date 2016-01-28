/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.CombinationFunction;
import rl.functionapproximation.DaubNScalingFunction;
import rl.functionapproximation.DaubNWavelet;
import rl.functionapproximation.HaarScalingFunction;
import rl.functionapproximation.HaarWavelet;
import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public class DaubFullTransform extends Transform {

    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    private final int order;

    final int height;
    final int width;
    int numFeatures;

    private static BasisFunction[] terms;
    private static double[][][] waveletCache;

    public DaubFullTransform(int height, int width, int baseScale, int maxScale, int order) {
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

    public int getNumShifts(int scale) {
        return (int) Math.pow(2, scale) + order - 2;
    }

    public BasisFunction[][][] getBasicWavelets(int scale) {
        int numLocations = getNumShifts(scale);
        BasisFunction basicWavelets[][][] = new BasisFunction[2][numLocations][dimensions];
        for (int dimension = 0; dimension < dimensions; dimension++) {
            for (int location = -order + 2; location < (int) Math.pow(2, scale); location++) {
                basicWavelets[0][location + order - 2][dimension] = new DaubNScalingFunction(scale, location, dimension, order);
                basicWavelets[1][location + order - 2][dimension] = new DaubNWavelet(scale, location, dimension, order);
            }
        }
        return basicWavelets;
    }

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions, int order) {
        int numPhi = (int) Math.pow(2, baseScale) + order - 2;
        if (maxScale < baseScale) {
            return (int) Math.pow(numPhi, dimensions);
        }
        if (maxScale == baseScale) {
            return (int) Math.pow(2, dimensions) * (int) Math.pow(numPhi, dimensions);
        }
        int tot = (int) Math.pow(numPhi, dimensions);
        for (int i = baseScale; i <= maxScale; i++) {
            tot += ((int) Math.pow(2, dimensions) - 1) * (int) Math.pow(Math.pow(2, i) + order - 2, dimensions);
        }
        return tot;

    }

    private void initialiseTerms() {
        terms = new BasisFunction[numFeatures];
                //intialise scaling functions
                /*ArrayList<BasisFunction> bfBuilder = new ArrayList<BasisFunction>();
         for (int i = dimensions - 1; i >= 0; i--) { //just go backwards. gives a nice order in the array this way.
         ArrayList<BasisFunction> bfBuilder2 = new ArrayList<BasisFunction>();
         for (int j = -order + 2; j < Math.pow(2, baseScale); j++) {
         DaubNScalingFunction h = new DaubNScalingFunction(baseScale, j, i, order); //scale translation dimension
         for (BasisFunction b : bfBuilder) { //add all combinations in
         bfBuilder2.add(new CombinationFunction(h, b));
         }
         if (bfBuilder.isEmpty()) {
         bfBuilder2.add(h);
         }
         }
         bfBuilder = bfBuilder2;
         }*/

        // nts: NOT GENERAL - DO NOT COPY FOR GENERAL USE
        BasisFunction[][][] basicWavelets = getBasicWavelets(baseScale);
        int pos = 0;
        // Initial father wavelet scheme
        for (int l1 = -order + 2; l1 < (int) Math.pow(2, baseScale); l1++) {
            BasisFunction bf1 = basicWavelets[0][l1 + order - 2][0];
            for (int l2 = -order + 2; l2 < (int) Math.pow(2, baseScale); l2++) {
                terms[pos] = new CombinationFunction(bf1, basicWavelets[0][l2 + order - 2][1]);
                pos++;
            }
        }

        for (int scale = baseScale; scale <= maxScale; scale++) {
            basicWavelets = getBasicWavelets(scale);

            // father/mother 1
            for (int l1 = -order + 2; l1 < (int) Math.pow(2, scale); l1++) {
                BasisFunction bf1 = basicWavelets[0][l1 + order - 2][0];
                for (int l2 = -order + 2; l2 < (int) Math.pow(2, scale); l2++) {
                    terms[pos] = new CombinationFunction(bf1, basicWavelets[1][l2 + order - 2][1]);
                    pos++;
                }
            }
            // father/mother 2
            for (int l1 = -order + 2; l1 < (int) Math.pow(2, scale); l1++) {
                BasisFunction bf1 = basicWavelets[1][l1 + order - 2][0];
                for (int l2 = -order + 2; l2 < (int) Math.pow(2, scale); l2++) {
                    terms[pos] = new CombinationFunction(bf1, basicWavelets[0][l2 + order - 2][1]);
                    pos++;
                }
            }
            // mother/mother
            for (int l1 = -order + 2; l1 < (int) Math.pow(2, scale); l1++) {
                BasisFunction bf1 = basicWavelets[1][l1 + order - 2][0];
                for (int l2 = -order + 2; l2 < (int) Math.pow(2, scale); l2++) {
                    terms[pos] = new CombinationFunction(bf1, basicWavelets[1][l2 + order - 2][1]);
                    pos++;
                }
            }
        }

        /*System.err.println("Last position: " + pos);
         System.err.println("Number of terms: " + terms.length);
         for (int i = 0; i < terms.length; i++) {
         System.err.println(terms[i].getBasisString());
         }*/
    }

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

    private void initialiseCache() {
        if (waveletCache == null) {
            waveletCache = new double[numFeatures][height][width];
            //System.out.println(terms.length);
            //intialise scaling functions
            for (int i = 0; i < numFeatures; i++) {
                //System.err.println(terms[i].getBasisString()+":");
                //System.err.println(i);
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
