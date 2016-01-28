/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public class AdaptiveBasis extends FunctionApproximator {
    // hard code to bsplines for now

    private final int baseScale;
    private final int dimensions;
    private final int order;
    private String type;
    private boolean wifdd = false; // if true, wavelets are separated in dimension
    private boolean widdd = false; // if true, only one wavelet is used
    private SmartIndex conjunctionsAdded;
    private SmartIndex conjunctions;
    private int time;

    public AdaptiveBasis(boolean wifdd, boolean widdd, String type, int baseScale, int dimensions, int order) {
        super(calculateNumTerms(wifdd, widdd, type, baseScale, dimensions, order), dimensions);
        System.err.println("Number of terms: "+calculateNumTerms(wifdd, widdd, type, baseScale, dimensions, order));
        this.baseScale = baseScale;
        this.wifdd = wifdd;
        this.widdd = widdd;
        this.dimensions = dimensions;
        this.order = order;
        this.type = type;
        conjunctionsAdded = new SmartIndex(dimensions, baseScale, findSupport(type, order));
        conjunctions = new SmartIndex(dimensions, baseScale, findSupport(type, order));
        time = 0;
        initialiseTerms();
    }

    public static int findSupport(String type, int order) {
        if (type.equalsIgnoreCase("daubechies")) {
            return order - 1;
        }
        if (type.equalsIgnoreCase("bspline")) {
            return order + 1;
        }
        if (type.equalsIgnoreCase("tiling")) {
            return 1;
        }
        return 0;
    }

    public void incTime() {
        time++;
    }

    public int getTime() {
        return time;
    }

    public void resetErrors() {
        for (int i = 0; i < terms.length; i++) {
            ((AdaptiveBasisElement) terms[i]).resetError();
        }
    }

    public static int getBaseScale(String type, int order) {
        // finds the basescale needed, assuming widdd
        if (type.equalsIgnoreCase("daubechies")) {
            return -3;
        }
        if (type.equalsIgnoreCase("bspline")) {
            return 0;
        }
        if (type.equalsIgnoreCase("tiling")) {
            return 1;
        }
        return 0;
    }

    public static int calculateNumTerms(boolean wifdd, boolean widdd, String type, int baseScale, int dimensions, int order) {
        int tot = 0;
        //System.out.println(tails);
        if (wifdd) {
            // number of wavelets determined by basescale, dimensions separate
            if (type.equalsIgnoreCase("daubechies")) {
                tot = (dimensions * ((int) Math.ceil(Math.pow(2, baseScale) + order - 2)));
            }
            if (type.equalsIgnoreCase("bspline")) {
                tot = (dimensions * ((int) Math.ceil(Math.pow(2, baseScale) + order)));
            }
            if (type.equalsIgnoreCase("tiling")) {
                tot = dimensions * baseScale;
            }

        }
        if (!wifdd) {
            if (type.equalsIgnoreCase("daubechies")) {
                tot = (int) Math.pow(Math.ceil(Math.pow(2, baseScale)) + order - 2, dimensions);
            }
            if (type.equalsIgnoreCase("bspline")) {
                tot = (int) Math.pow(Math.ceil(Math.pow(2, baseScale)) + order, dimensions);
            }
            if (type.equalsIgnoreCase("tiling")) {
                tot = (int) Math.pow(baseScale, dimensions);
            }
        }
        //System.out.println(tot+" terms");
        return tot;
    }

    public SeriesFunction[][] getBasicWavelets(int scale) {
        int numLocations;
        SeriesFunction basicWavelets[][] = new SeriesFunction[1][1];
        if (type.equalsIgnoreCase("daubechies")) {
            numLocations = (int) Math.ceil(Math.pow(2, baseScale)) + order - 2;
            basicWavelets = new SeriesFunction[numLocations][dimensions];
            for (int dimension = 0; dimension < dimensions; dimension++) {
                for (int location = -order + 2; location < (int) Math.pow(2, scale); location++) {
                    basicWavelets[location + order - 2][dimension] = new DaubNScalingFunction(scale, location, dimension, order);
                }
            }
        }
        if (type.equalsIgnoreCase("bspline")) {
            numLocations = (int) Math.ceil(Math.pow(2, baseScale)) + order;
            basicWavelets = new SeriesFunction[numLocations][dimensions];
            for (int dimension = 0; dimension < dimensions; dimension++) {
                for (int location = -order; location < (int) Math.pow(2, scale); location++) {
                    basicWavelets[location + order][dimension] = new BSplinePhi(scale, location, dimension, order);
                }
            }
        }
        if (type.equalsIgnoreCase("tiling")) {
            numLocations = baseScale;
            basicWavelets = new SeriesFunction[numLocations][dimensions];
            for (int dimension = 0; dimension < dimensions; dimension++) {
                for (int location = 0; location < baseScale; location++) {
                    basicWavelets[location][dimension] = new Tiling(scale, location, dimension);
                }
            }
        }

        return basicWavelets;
    }

    public void initialiseTerms() {
        super.initialiseTerms();
        BasisFunction terms[] = getTerms();
        int lattice[][];
        //System.out.println(tails);
        //System.out.println(terms.length);
        AdaptiveBasisElement newBf;
        SeriesFunction basicWavelets[][] = getBasicWavelets(baseScale);
        SeriesFunction[] b;
        SeriesFunction bf;
        int pos = 0;
        if (wifdd) {
            // keep all dimensions separate
            for (int i = 0; i < basicWavelets.length; i++) {
                for (int j = 0; j < dimensions; j++) { // to change
                    newBf = new AdaptiveBasisElement(basicWavelets[i][j], dimensions);
                    //System.out.println("Starting: "+newBf.getBasisString());
                    terms[pos] = newBf;
                    //System.out.println(terms[pos].getBasisString());
                    pos++;
                    conjunctionsAdded.add(newBf);
                }
            }
        } else {
            //not adaptive or just widdd
            lattice = getIndexLattice(baseScale);
            //System.out.println("Lattice is "+ lattice.length+" by "+lattice[0].length);

            for (int i = 0; i < lattice.length; i++) {
                b = new SeriesFunction[dimensions];
                for (int j = 0; j < dimensions; j++) { // to change
                    //System.out.print("Help");
                    b[j] = basicWavelets[lattice[i][j]][j];
                }
                terms[pos] = new AdaptiveBasisElement(b);
                //System.out.println(terms[pos].getBasisString());
                pos++;
            }
        }
        if (wifdd) {
            populateConjunctions();
        }
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

    public int getNumShifts(int scale) {
        if (type.equalsIgnoreCase("daubechies")) {
            return (int) Math.pow(2, baseScale) + order - 2;
        }
        if (type.equalsIgnoreCase("bspline")) {
            return (int) Math.pow(2, baseScale) + order;
        }
        if (type.equalsIgnoreCase("tiling")) {
            return baseScale;
        }
        return 0;
    }

    public static boolean canCombine(AdaptiveBasisElement a, AdaptiveBasisElement b) {
        boolean[] adims, bdims;
        double[][] asup, bsup;
        adims = a.getDims();
        bdims = b.getDims();
        asup = a.getSupport();
        bsup = b.getSupport();
        for (int i = 0; i < adims.length; i++) {
            if (adims[i] && bdims[i]) {
                return false;
            } else {
                if (!overlap(asup[i], bsup[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean overlap(double[] a, double[] b) {
        //returns true if intervals a and b overlap
        if (a[1] < b[0] || b[1] < a[0]) {
            return false;
        }
        return true;
    }

    public static AdaptiveBasisElement formConjunction(AdaptiveBasisElement a, AdaptiveBasisElement b) {
        AdaptiveBasisElement c;
        SeriesFunction[] cbf;
        boolean[] adims, bdims;
        SeriesFunction[] abf = a.getBFs();
        SeriesFunction[] bbf = b.getBFs();
        adims = a.getDims();
        bdims = b.getDims();
        cbf = new SeriesFunction[abf.length];
        for (int i = 0; i < adims.length; i++) {
            if (adims[i]) {
                cbf[i] = abf[i];
            }
            if (bdims[i]) {
                cbf[i] = bbf[i];
            }
        }
        c = new AdaptiveBasisElement(cbf);
        c.addParent(a);
        c.addParent(b);
        a.addDescendents(c);
        b.addDescendents(c);
        return c;
    }

    public void populateConjunctions() {
        AdaptiveBasisElement conj;
        for (int i = 1; i < terms.length; i++) {
            for (int j = 0; j < i; j++) {
                if (AdaptiveBasis.canCombine((AdaptiveBasisElement) terms[i], (AdaptiveBasisElement) terms[j])) {
                    conj = AdaptiveBasis.formConjunction((AdaptiveBasisElement) terms[i], (AdaptiveBasisElement) terms[j]);
                    conjunctions.add(conj);
                }
            }
        }
    }

    public boolean notInConjunctions(AdaptiveBasisElement bf) {
        return !conjunctions.present(bf);
    }

    public boolean notInAdded(AdaptiveBasisElement bf) {
        return !conjunctionsAdded.present(bf);
    }

    public void addConjunctions(AdaptiveBasisElement bf, State s) {
        AdaptiveBasisElement conj;
        //int size = conjunctions.size();
        ArrayList<AdaptiveBasisElement> actConj = conjunctionsAdded.activated(s);
        for (AdaptiveBasisElement c : actConj) {
            if (AdaptiveBasis.canCombine(c, bf)) {
                conj = AdaptiveBasis.formConjunction(c, bf);
                //if (!notInConjunctions(conj)) System.out.println(conj.getBasisString()+" in conjunctions already");
                //if (!notInAdded(conj)) System.out.println(conj.getBasisString()+" added already");
                if (notInConjunctions(conj) && notInAdded(conj)) {
                    conjunctions.add(conj);
                }
            }
        }
        conjunctionsAdded.add(bf);
    }

//    public void addSplits(AdaptiveBasisElement bf) {
//        AdaptiveBasisElement[][] allSplits = AdaptiveBasis.findSplits(bf);
//        for (int j = 0; j < allSplits.length; j++) {
//            for (int k = 0; k < allSplits[j].length; k++) {
//                if (notInSplits(allSplits[j][k])) {
//                    splits.add(allSplits[j][k]);
//                }
//            }
//        }
//    }
//
//    public void removeSplits(AdaptiveBasisElement bf) {
//        AdaptiveBasisElement split;
//        for (int i = splits.size() - 1; i >= 0; i--) {
//            split = (AdaptiveBasisElement) splits.get(i);
//            if (split.getSplitParents().equals(bf)) {
//                splits.remove(i);
//            }
//        }
//    }
    public int getNumDimensions() {
        return dimensions;
    }

//    public void populateSplits() {
//        AdaptiveBasisElement[][] allSplits;
//        for (int i = 0; i < terms.length; i++) {
//            allSplits = AdaptiveBasis.findSplits((AdaptiveBasisElement) terms[i]);
//            for (int j = 0; j < allSplits.length; j++) {
//                for (int k = 0; k < allSplits[j].length; k++) {
//                    splits.add(allSplits[j][k]);
//                }
//            }
//        }
//    }
    public static AdaptiveBasisElement[][] findSplits(AdaptiveBasisElement bf) {
        AdaptiveBasisElement[][] children;
        boolean[] dims = bf.getDims();
        int numdims = 0;
        for (int i = 0; i < dims.length; i++) {
            if (dims[i]) {
                numdims++;
            }
        }
        children = new AdaptiveBasisElement[numdims][];
        //System.out.print(numdims+", ");
        for (int i = 0; i < dims.length; i++) {
            if (dims[i]) {
                //System.out.print(numdims+" ");
                children[numdims - 1] = bf.getChildren(i);
                numdims--;
            }
        }
        return children;
    }

//    public ArrayList<AdaptiveBasisElement> getSplits() {
//        return splits;
//    }
    public ArrayList<AdaptiveBasisElement> getConjunctions() {
        return conjunctions.getAll();
    }

//    public void resetRelevance() {
//        for (int i=0; i<conjunctions.size();i++) {
//            ((AdaptiveBasisElement)conjunctions.get(i)).setRelevance(0);
//        }
//        for (int i=0; i<splits.size(); i++) {
//            ((AdaptiveBasisElement)splits.get(i)).setRelevance(0);
//        }
//    }
    ArrayList<AdaptiveBasisElement> getSupportedConjunctions(State s) {
        return conjunctions.activated(s);
    }

    public void removeConjunctions(AdaptiveBasisElement bf) {
        conjunctions.remove(bf);
    }
}
