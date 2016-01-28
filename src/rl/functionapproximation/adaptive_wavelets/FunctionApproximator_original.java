package rl.functionapproximation.adaptive_wavelets;

import java.io.Serializable;
import java.util.ArrayList;
import rl.domain.State;

public class FunctionApproximator_original implements Serializable {

    int numFeatures;
    int numTerms;
    BasisFunction terms[];
    double weights[]; //currently not used or updated

    public FunctionApproximator_original(BasisFunction terms[], int numFeatuers) {
        this.terms = terms;
        this.numTerms = terms.length;
        this.numFeatures = numFeatures;
        initialiseWeights();
    }

    public FunctionApproximator_original(int numTerms, int numFeatures) {
        this.numTerms = numTerms;
        this.numFeatures = numFeatures;
        initialiseWeights();
    }

    public void initialiseTerms() {
        terms = new BasisFunction[numTerms];
    }

    public void initialiseWeights() {
        weights = new double[numTerms];
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public BasisFunction[] getTerms() {
        return terms;
    }

    public void setTerms(BasisFunction terms[]) {
        this.terms = terms;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double weights[]) {
        this.weights = weights;
    }

    public double getTermMagnitude(int termPos) {
        return terms[termPos].getShrink();
    }

    public double[] calculateStateValue(State s) {
        double returnArray[] = new double[numTerms];
        for (int i = 0; i < numTerms; i++) {
            returnArray[i] = terms[i].getValue(s);
        }
        return returnArray;
    }

    public double valueAt(State s) {
        double stateValue[] = calculateStateValue(s);
        return dot(stateValue, weights);
    }

    public double dot(double arr1[], double arr2[]) {
        if (arr1.length != arr2.length) {
            //System.out.println("Dot product error. Arrays not equal in length");
            return Double.NaN;
        }
        double returnVal = 0;
        for (int i = 0; i < arr1.length; i++) {
            returnVal += arr1[i] * arr2[i];
        }
        return returnVal;
    }

    public void addBasis(BasisFunction bf) {
        BasisFunction terms2[] = new BasisFunction[terms.length + 1];
        for (int i = 0; i < terms.length; i++) {
            terms2[i] = terms[i];
        }
        terms2[terms.length] = bf;
        terms = terms2;
        numTerms++;
    }

    public void removeBasis(BasisFunction bf) {
        //System.out.println("removing "+bf.getBasisString());
        BasisFunction terms2[] = new BasisFunction[terms.length - 1];
        int count = 0;
        for (int i = 0; i < terms.length; i++) {
            if (terms[i].getBasisString().compareToIgnoreCase(bf.getBasisString()) == 0) {
                continue;
            }
            terms2[count] = terms[i];
            count++;
        }
        terms = terms2;
        numTerms--;
    }

    public int getBasisIndex(BasisFunction bf) {
        int b=-1;
        for (int i = 0; i < terms.length; i++) {
            if (terms[i].getBasisString().compareToIgnoreCase(bf.getBasisString()) == 0) {
                //System.out.println(terms[i].getBasisString() +" matches "+bf.getBasisString());
                b=i;
                break;
            }
        }
        return b;
    }

}
