package rl.functionapproximation;

import rl.domain.State;

/**
 * A full tiling of daub wavelets and scaling functions up to some order
 * @author Dean
 */
public class DaubBasis extends Basis {

    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    private final int order;
    
    final int numFunctionsPerFrame;
    final int numFrames;
    final int height;
    final int width;
    
    boolean normalise;
    
    private static BasisFunction[] terms;
    private static double[][][] waveletCache;

    public DaubBasis(int numFrames, int height, int width, int baseScale, int maxScale, int order, boolean normalise) {
        this.order = order;
        this.dimensions = 2;
        this.numFunctionsPerFrame = calculateNumTerms(baseScale, maxScale, dimensions, order);
        this.numFrames = numFrames;
        this.height = height;
        this.width = width;
        this.numFeatures = numFrames*numFunctionsPerFrame;
        this.normalise = normalise;
        
        weights = new double[numFeatures];
        
        this.maxScale = maxScale;
        this.baseScale = baseScale;
        initialiseTerms();
        initialiseCache();
    }

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions, int order) {
        int numPhi = (int)Math.pow(2,baseScale) + order - 2;
        if (baseScale > maxScale) return (int)Math.pow(numPhi, dimensions) + numPhi*dimensions;
        int numPsi = (int)Math.pow(2, maxScale+1) + (maxScale-baseScale+2)*(order-2) - numPhi;
        return (int) Math.pow(numPsi,dimensions) + (int)Math.pow(numPhi, dimensions) + (numPhi+numPsi)*dimensions;
    }

    private void initialiseTerms() {
        if (terms == null) {
            terms = new BasisFunction[numFeatures];
            //System.out.println(terms.length);
            //intialise scaling functions
            int pos = 0;
            for (int f = 0; f < numFrames; f++) {
                int begin = pos;
                for (int i = 0; i < dimensions; i++) {
                    int start = pos;
                    for (int j = -order + 2; j < Math.pow(2, baseScale); j++) {
                        DaubNScalingFunction curr = new DaubNScalingFunction(baseScale, j, i, order);
                        terms[pos] = curr;
                        pos++;
                        for (int k = begin; k < start; k++) { //add all combinations in
                            terms[pos] = new CombinationFunction(curr, terms[k]);
                            pos++;
                        }
                    }
                }
                if (baseScale <= maxScale) {
                    int waveletStart = pos;
                    for (int i = 0; i < dimensions; i++) {
                        int start = pos;
                        for (int l = baseScale; l <= maxScale; l++) { //go through all scales
                            for (int j = -order + 2; j < Math.pow(2, l); j++) { // go through all translations
                                DaubNWavelet curr = new DaubNWavelet(l, j, i, order);
                                terms[pos] = curr;
                                pos++;
                                for (int k = waveletStart; k < start; k++) { //add all combinations in
                                    terms[pos] = new CombinationFunction(curr, terms[k]);
                                    pos++;
                                }
                            }
                        }
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
    
    private void initialiseCache() {
        if(waveletCache == null) {
             waveletCache = new double[numFeatures][height][width];
            //System.out.println(terms.length);
            //intialise scaling functions
            for(int i = 0; i < numFeatures; i++) {
                //System.err.println(terms[i].getBasisString()+":");
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        double ys = ((double) y)/(height-1);
                        double xs = ((double) x)/(width-1);
                        //waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{y,x}));
                        waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{ys,xs}));
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
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i];
        }
    }

    @Override
    public double getValue(State s) {
        double[] phi = computeFeatures(s);
        double Q = 0;
        // Q = Sum(wi*xi)
        
        double euclidNormSquared = 0;
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*phi[i];
            euclidNormSquared += phi[i]*phi[i];
        }
        if(normalise && euclidNormSquared > 0) Q/=euclidNormSquared;
        return Q;
    }

    @Override
    public double getValue(double[] phi) {
        double Q = 0;
        // Q = Sum(wi*xi)
        
        double euclidNormSquared = 0;
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*phi[i];
            euclidNormSquared += phi[i]*phi[i];
        }
        if(normalise && euclidNormSquared > 0) Q/=euclidNormSquared;
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
}
