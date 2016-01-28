package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;


public class WaveletTensorBasis extends Basis {

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

    public WaveletTensorBasis(int numFrames, int height, int width, int baseScale, int maxScale, int order, boolean normalise) {

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
        if (maxScale < baseScale) return (int)Math.pow(numPhi, dimensions);
        if (maxScale == baseScale) return (int)Math.pow(2, dimensions) * (int)Math.pow(numPhi, dimensions);
        int tot = (int)Math.pow(numPhi, dimensions);
        for(int i=baseScale; i<=maxScale; i++) tot += ((int)Math.pow(2, dimensions)-1) * (int)Math.pow(Math.pow(2,i) + order - 2, dimensions);
        return tot;
    }
    

    public int getNumShifts(int scale) {
        return (int)Math.pow(2,scale) + order - 2;
    }
    
    public static int getNumShifts(int scale, int order) {
        return (int)Math.pow(2,scale) + order - 2;
    }
    
    public int[][] getIndexLattice(int scale) {
        int numLocations = getNumShifts(scale);
        int lattice[][] = new int[(int)Math.pow(numLocations, dimensions)][dimensions];
        for (int i = 0; i<Math.pow(numLocations, dimensions); i++) {
            int icopy = i;
            for (int j=dimensions-1; j>-1; j--) {
                lattice[i][dimensions-j-1] = icopy / (int)Math.pow(numLocations, j);
                icopy = icopy % (int)Math.pow(numLocations, j);
            }
        }
        return lattice;
    }
    
    public BasisFunction[][][] getBasicWavelets(int scale) {
        int numLocations = getNumShifts(scale);
        BasisFunction basicWavelets[][][] = new BasisFunction[2][numLocations][dimensions];
        for (int dimension=0; dimension<dimensions; dimension++) {
            for (int location=-order+2; location<(int)Math.pow(2, scale); location++) {
                basicWavelets[0][location+order-2][dimension] = new DaubNScalingFunction(scale, location, dimension, order);
                basicWavelets[1][location+order-2][dimension] = new DaubNWavelet(scale, location, dimension, order);
            }
        }
        return basicWavelets;
    }
    
    public static BasisFunction[][][] getBasicWavelets(int scale, int dimensions, int order) {
        int numLocations = getNumShifts(scale, order);
        BasisFunction basicWavelets[][][] = new BasisFunction[2][numLocations][dimensions];
        for (int dimension=0; dimension<dimensions; dimension++) {
            for (int location=-order+2; location<(int)Math.pow(2, scale); location++) {
                basicWavelets[0][location+order-2][dimension] = new DaubNScalingFunction(scale, location, dimension, order);
                basicWavelets[1][location+order-2][dimension] = new DaubNWavelet(scale, location, dimension, order);
            }
        }
        return basicWavelets;
    }
    
    // for generating wavelet cross terms at given location
    public static BasisFunction[] makeCrossTerms(int locations[], int baseScale, int dimensions, int order) {
        BasisFunction basicWavelets[][][];
        CombinationFunction b;
        int pos = 0;
        int numNewTerms = (int)Math.pow(2, dimensions) -1;
        BasisFunction newterms[] = new BasisFunction[numNewTerms];
        int local[] = findLocationIndex(locations, order);
        // get the location (as index) and scale
        basicWavelets = getBasicWavelets(baseScale, dimensions, order);
        for (int G = 1; G < Math.pow(2, dimensions); G++) {
            int Gcopy = G;
            b = new CombinationFunction(basicWavelets[Gcopy / (int) Math.pow(2, dimensions - 1)][local[0]][0], basicWavelets[(Gcopy % (int) Math.pow(2, dimensions - 1)) / (int) Math.pow(2, dimensions - 2)][local[1]][1]);
            Gcopy = Gcopy % (int) Math.pow(2, dimensions - 1);
            Gcopy = Gcopy % (int) Math.pow(2, dimensions - 2);
            for (int j = 2; j < dimensions; j++) {
                b = new CombinationFunction(b, basicWavelets[Gcopy / (int) Math.pow(2, dimensions - 1 - j)][local[j]][j]);
                Gcopy = Gcopy % (int) Math.pow(2, dimensions - 1 - j);
            }
            newterms[pos] = b;
            pos++;
        }
        return newterms;
    } 
    
    
    // for making the terms at the next scale, within scope of the current tile described by G and local
    public static BasisFunction[] makeNextScaleTerms(int G[], int locations[], int basescale, int dimensions, int order) {
        BasisFunction basicWavelets[][][];
        CombinationFunction b;
        int pos = 0;
        int scale = basescale+1;
        basicWavelets = getBasicWavelets(scale, dimensions, order);
        for (int i=0; i<locations.length; i++) {
            locations[i] = 2*locations[i] + (order-2)/2;
        }
        int local[] = findLocationIndex(locations, order);
        ArrayList l = checkLocationIndex(local, scale, order);
        int numNewTerms = l.size();
        int t[];
        BasisFunction newterms[] = new BasisFunction[numNewTerms];
        for (int i=0; i<l.size(); i++){
            t = (int[])l.get(i);
            b = new CombinationFunction(basicWavelets[G[0]][t[0]][0], basicWavelets[G[1]][t[1]][1]);
            for (int j = 2; j < dimensions; j++) {
                b = new CombinationFunction(b, basicWavelets[G[j]][t[j]][j]);
            }
            newterms[pos] = b;
            pos++;
        }
        return newterms;
    }
    
    private static ArrayList checkLocationIndex(int local[], int scale, int order) {
        int maxIndex = (int)Math.pow(2, scale) + order -2;
        int temp[];
        boolean val;
        for (int i=0; i<local.length; i++) {
            if (local[i]<-1 || local[i] >= maxIndex) return new ArrayList();
        }
        ArrayList validlocations = new ArrayList();
        for (int N=0; N<Math.pow(2, local.length); N++) {
            int Ncopy = N;
            temp = new int[local.length];
            val = true;
            for (int j = 0; j < local.length; j++) {
                temp[j] = local[j]+Ncopy / (int) Math.pow(2, local.length - 1 - j);
                if (temp[j]<0 || temp[j]>=maxIndex) {
                    val = false;
                    break;
                }
                Ncopy = Ncopy % (int) Math.pow(2, local.length - 1 - j);
            }
            if (val) validlocations.add(temp);
        }
        return validlocations;
    }
    
    private static int[] findLocationIndex(int location[], int order) {
        int localIndex[] = new int[location.length];
        for (int i = 0; i<location.length; i++) {
            localIndex[i] = location[i]+order-2;
        }
        return localIndex;
    }
    
    public static int[] getG(String basisString) {
        String s[] = basisString.split(" ");
        String t[];
        int d = s.length;
        int G[] = new int[d];
        for (int i = 0; i<d; i++) {
            t = s[i].split(",");
            G[i] = Integer.parseInt(t[0]);
        }
        return G;
    }
    
    public static boolean isScaling(String basisString) {
        int G[] = getG(basisString);
        for (int i=0; i<G.length; i++) {
            if (G[i]==1) return false;
        }
        return true;
    }
    
    public static int[] getLocals(String basisString) {
        String s[] = basisString.split(" ");
        String t[];
        int d = s.length;
        int L[] = new int[d];
        for (int i = 0; i<d; i++) {
            t = s[i].split(",");
            L[i] = Integer.parseInt(t[3]);
        }
        return L;
    }
    
    public static int getScale(String basisString) {
        String s[] = basisString.split(" ");
        String t[] = s[0].split(",");
        return Integer.parseInt(t[2]);
    }
    
    public void initialiseTerms() {
        terms = new BasisFunction[numFeatures];
        int lattice[][]; 
        BasisFunction basicWavelets[][][];
        CombinationFunction b;
        int pos = 0;
        //System.out.print(dimensions);
        //BasisFunction terms[] = new BasisFunction[WaveletTensorBasis.calculateNumTerms(baseScale, maxScale, dimensions, order)];
        // first, construct scaling tile terms
        basicWavelets = getBasicWavelets(baseScale);
        lattice = getIndexLattice(baseScale);
        for (int i=0; i<lattice.length; i++) {
            b = new CombinationFunction(basicWavelets[0][lattice[i][0]][0], basicWavelets[0][lattice[i][1]][1]);
            for (int j=2; j<dimensions; j++) {
                //System.out.print("Help");
                b = new CombinationFunction(b, basicWavelets[0][lattice[i][j]][j]);
            }
            //System.err.println(b.getBasisString());
            terms[pos] = b;
            pos++;
        }
        // then add in wavelet terms as needed
        if (baseScale<=maxScale) {
            for (int scale = baseScale; scale<maxScale+1; scale++) {
                if (scale>baseScale) {
                    basicWavelets = getBasicWavelets(scale);
                    lattice = getIndexLattice(scale);
                }
                for (int i=0; i<lattice.length; i++) {
                    for (int G=1; G<Math.pow(2,dimensions); G++) {
                        int Gcopy = G;
                        b = new CombinationFunction(basicWavelets[Gcopy / (int)Math.pow(2, dimensions-1)][lattice[i][0]][0], basicWavelets[(Gcopy % (int)Math.pow(2, dimensions-1)) / (int)Math.pow(2, dimensions-2)][lattice[i][1]][1]);
                        Gcopy = Gcopy % (int)Math.pow(2, dimensions-1);
                        Gcopy = Gcopy % (int)Math.pow(2, dimensions-2);
                        for (int j=2; j<dimensions; j++) {
                            b = new CombinationFunction(b, basicWavelets[Gcopy / (int)Math.pow(2, dimensions-1-j)][lattice[i][j]][j]);
                            Gcopy = Gcopy % (int)Math.pow(2, dimensions-1-j);
                        }
                        terms[pos] = b;
                        System.err.println(b.getBasisString());
                        pos++;
                    }
                }
            }
        }
        System.err.println("Calculated number of terms = " + numFeatures);
        System.err.println("Number of terms = " + pos);
    }
    
    private void initialiseCache() {
        if(waveletCache == null) {
             waveletCache = new double[numFeatures][height][width];
            //System.err.println(terms.length);
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
        
        //System.err.println(pos);
        System.err.println(terms.length);
        for (int i = 0; i < terms.length; i++) {
            System.err.println(terms[i].getBasisString());
        }
      
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
