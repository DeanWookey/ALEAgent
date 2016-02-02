/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class DaubFullBasis extends TransformBasis {

    final int maxScale;
    final int baseScale;
    final int dimensions;
    final int order;

    final int numFunctionsPerFrame;
    final int numFrames;
    final int height;
    final int width;

    boolean normalise;

    private static BasisFunction[] terms;

    public DaubFullBasis(int numFrames, int height, int width, int baseScale, int maxScale, int order, boolean normalise) {
        super(numFrames, width, height);
        this.order = order;
        this.dimensions = 2;
        this.numFunctionsPerFrame = calculateNumTerms(baseScale, maxScale, dimensions, order);
        this.numFrames = numFrames;
        this.height = height;
        this.width = width;
        this.numFeatures = numFrames * numFunctionsPerFrame;
        this.normalise = normalise;

        weights = new double[numFeatures];

        this.maxScale = maxScale;
        this.baseScale = baseScale;
        initialiseTerms();
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
        if (terms == null) {
            terms = new BasisFunction[numFeatures];
            int pos = 0;
            for (int f = 0; f < numFrames; f++) {
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
            }

        }
    }

    /*
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
    }
    */

    /*
    @Override
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i];
        }
    }
    */
/*
    @Override
    public double getValue(State s) {
        double[] phi = computeFeatures(s);
        double Q = 0;
        // Q = Sum(wi*xi)

        double euclidNormSquared = 0;
        for (int i = 0; i < numFeatures; i++) {
            Q += weights[i] * phi[i];
            euclidNormSquared += phi[i] * phi[i];
        }
        if (normalise && euclidNormSquared > 0) {
            Q /= euclidNormSquared;
        }
        return Q;
    }
    */

    /*
    @Override
    public double getValue(double[] phi) {
        double Q = 0;
        // Q = Sum(wi*xi)

        double euclidNormSquared = 0;
        for (int i = 0; i < numFeatures; i++) {
            Q += weights[i] * phi[i];
            euclidNormSquared += phi[i] * phi[i];
        }
        if (normalise && euclidNormSquared > 0) {
            Q /= euclidNormSquared;
        }
        return Q;
    }
    */

    /*
    @Override
    public double[] computeFeatures(State s) {
        // avoid extra memory
        //if(phi == null) phi = new double[numFeatures];
        double[] phi = new double[numFeatures];
        double[] vars = s.getState();
        if (vars.length == numFeatures) { // unsafe but useful
            return vars;
        } else {
            int index, imgindex;
            for (int f = 0; f < numFrames; f++) {
                for (int k = 0; k < numFunctionsPerFrame; k++) {
                    index = f * numFunctionsPerFrame + k;
                    // have to reset old value when overwriting
                    phi[index] = 0;

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            imgindex = f * height * width + y * width + x;
                            // x,y already scaled in cosine pre-calculation
                            //phi[index] += vars[imgindex] * terms[index].getValue(new State(new double[]{y,x}));
                            phi[index] += vars[imgindex] * waveletCache[index][y][x];
                        }
                    }
                }
            }
        }

        return phi;
    }
    */

    @Override
    public BasisFunction[] getBasisFunctions() {
        return terms;
    }

    @Override
    public int getNumFeatures() {
        return terms.length;
    }
}
