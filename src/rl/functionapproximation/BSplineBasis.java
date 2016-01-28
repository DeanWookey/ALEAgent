package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

public class BSplineBasis extends Basis {

    private final int baseScale;
    private final int dimensions;
    private final int maxScale;
    private final int order;

    final int numFunctionsPerFrame;
    final int numFrames;
    final int height;
    final int width;

    boolean normalise;

    private static BasisFunction[] terms;
    private static double[][][] waveletCache;

    public BSplineBasis(int numFrames, int height, int width, int baseScale, int maxScale, int order, boolean normalise) {
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
        initialiseCache();
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

    public void initialiseTerms() {
        if (terms == null) {
            terms = new BasisFunction[numFeatures];
            //System.out.println(terms.length);
            //intialise scaling functions
            int lattice[][];
            BasisFunction basicPhiWavelets[][], basicPsiWavelets[][];
            BasisFunction b;
            int pos = 0;
            //System.out.print(dimensions);
            //BasisFunction terms[] = new BasisFunction[WaveletTensorBasis.calculateNumTerms(baseScale, maxScale, dimensions, order)];
            // first, construct scaling tile terms
            basicPhiWavelets = getBasicPhiWavelets(baseScale);
            lattice = getIndexLattice(baseScale);
            for (int f = 0; f < numFrames; f++) {
                for (int i = 0; i < lattice.length; i++) {
                    b = basicPhiWavelets[lattice[i][0]][0];
                    for (int j = 1; j < dimensions; j++) { // to change
                        //System.out.print("Help");
                        b = new CombinationFunction(b, basicPhiWavelets[lattice[i][j]][j]);
                    }
                    //System.out.println(b.getBasisString());
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
                //                        //System.out.println(b.getBasisString());
                //                        pos++;
                //                    }
                //                }
                //            }
                //        }
            }

        }
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

    @Override
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i];
        }
    }

}
