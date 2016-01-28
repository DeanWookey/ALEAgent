/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

/**
 *
 * @author micha_000
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import rl.functionapproximation.adaptive_wavelets.SeriesFunction;
import java.util.ArrayList;
import java.util.Vector;
import rl.domain.State;
import rl.functionapproximation.adaptive_wavelets.SeriesFunction;

public class IFDDTiling extends FunctionApproximator {

    private final int numTiles;
    private final int dimensions;
    private ArrayList<IFDDTilingElement> conjunctionsAdded;
    private ArrayList<IFDDTilingElement> conjunctions;

    public IFDDTiling(int numTiles, int dimensions) {
        super(numTiles * dimensions, dimensions);
        this.numTiles = numTiles;
        this.dimensions = dimensions;
        conjunctionsAdded = new ArrayList();
        conjunctions = new ArrayList();
        initialiseTerms();
    }

    public SeriesFunction[][] getBasicWavelets(int scale) {
        int numLocations;
        SeriesFunction basicWavelets[][] = new SeriesFunction[1][1];
        numLocations = numTiles;
        basicWavelets = new SeriesFunction[numLocations][dimensions];
        for (int dimension = 0; dimension < dimensions; dimension++) {
            for (int location = 0; location < numTiles; location++) {
                basicWavelets[location][dimension] = new Tiling(scale, location, dimension);
            }
        }

        return basicWavelets;
    }

    public void initialiseTerms() {
        super.initialiseTerms();
        BasisFunction terms[] = getTerms();
        int lattice[][];
        IFDDTilingElement newBf;
        SeriesFunction basicWavelets[][] = getBasicWavelets(numTiles);
        SeriesFunction[] b;
        SeriesFunction bf;
        int pos = 0;
        for (int i = 0; i < basicWavelets.length; i++) {
            for (int j = 0; j < dimensions; j++) { // to change
                newBf = new IFDDTilingElement(basicWavelets[i][j], dimensions);
                //System.out.println("Starting: "+newBf.getBasisString());
                terms[pos] = newBf;
                //System.out.println(terms[pos].getBasisString());
                pos++;
                conjunctionsAdded.add(newBf);
            }
        }
        populateConjunctions();
    }

    public int[][] getIndexLattice(int scale) {
        int numLocations = numTiles;
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

    public static boolean canCombine(IFDDTilingElement a, IFDDTilingElement b) {
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
                if (!overlap(asup[i], bsup[i])) return false;
            }
        }
        return true;
    }
    
    public static boolean canCombine(IFDDTilingElement a, IFDDTilingElement b, State s) {
        boolean[] adims, bdims;
        double[][] asup, bsup;
        adims = a.getDims();
        bdims = b.getDims();
        asup = a.getSupport();
        bsup = b.getSupport();
        if (a.isSupported(s) && b.isSupported(s)) {
            //System.out.println("Checking "+a.getBasisString()+" and "+b.getBasisString());
            for (int i = 0; i < adims.length; i++) {
                if (adims[i] && bdims[i]) {
                    //System.out.println("Different Dimensions");
                    return false;
                } else {
                    if (!overlap(asup[i], bsup[i])) {
                        //System.out.println("Not overlapping");
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean overlap(double[] a, double[] b) {
        //returns true if intervals a and b overlap
        if (a[1] < b[0] || b[1] < a[0]) {
            return false;
        }
        return true;
    }

    public static IFDDTilingElement formConjunction(IFDDTilingElement a, IFDDTilingElement b) {
        IFDDTilingElement c;
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
        c = new IFDDTilingElement(cbf);
        c.addParent(a);
        c.addParent(b);
        a.addDescendents(c);
        b.addDescendents(c);
        return c;
    }

    public void populateConjunctions() {
        IFDDTilingElement conj;
        for (int i = 1; i < terms.length; i++) {
            for (int j = 0; j < i; j++) {
                if (IFDDTiling.canCombine((IFDDTilingElement) terms[i], (IFDDTilingElement) terms[j])) {
                    conj = IFDDTiling.formConjunction((IFDDTilingElement) terms[i], (IFDDTilingElement) terms[j]);
                    conjunctions.add(conj);
                }
            }
        }
    }

    public boolean notInConjunctions(IFDDTilingElement bf) {
        return !conjunctions.contains(bf);
    }

    public boolean notInAdded(IFDDTilingElement bf) {
        return !conjunctionsAdded.contains(bf);
    }

    public void addConjunctions(IFDDTilingElement bf, State s) {
        IFDDTilingElement conj;
        int conjSize = conjunctions.size();
        for (IFDDTilingElement c : conjunctionsAdded) {
            if (IFDDTiling.canCombine(c, bf, s)) {
                //System.out.println("Can combine: "+c.getBasisString()+" and "+bf.getBasisString());
                conj = IFDDTiling.formConjunction(c, bf);
                if (notInConjunctions(conj) && notInAdded(conj)) {
                    conjunctions.add(conj);
                } //else System.out.println("Already added "+ conj.getBasisString());
            }
        }
        //if (conjSize - conjunctions.size()==0) System.out.println("No conjunctions added! BF: "+bf.getBasisString());
        conjunctionsAdded.add(bf);
    }

    public int getNumDimensions() {
        return dimensions;
    }

    public ArrayList<IFDDTilingElement> getConjunctions() {
        return conjunctions;
    }

    public void removeConjunctions(IFDDTilingElement bf) {
        conjunctions.remove(bf);
    }
    
    public void modifyParents(IFDDTilingElement bf) {
        ArrayList<IFDDTilingElement> parents = bf.getParent();
        for (IFDDTilingElement parent : parents) {
            parent.addException(bf);
        }
    }

}
