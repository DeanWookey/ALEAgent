package rl.functionapproximation.adaptive_wavelets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import rl.domain.State;

public class QApproximator implements Serializable {

    FunctionApproximator FAs[];
    double weights[];
    LinkedList<ElegibilityTrace> traces;

    public QApproximator(FunctionApproximator FAs[]) {
        this.FAs = FAs;
        weights = new double[getNumTerms()];
        traces = new LinkedList<ElegibilityTrace>();
    }

    public ElegibilityTrace getNewTrace() {
        ElegibilityTrace t = new ElegibilityTrace(getNumTerms());
        traces.add(t);
        return t;
    }

    public void addBasis(BasisFunction bf) {
        addBasis(bf, 0.0);
    }

    public void addBasis(BasisFunction bf, double weight) {
        for (int i = 0; i < FAs.length; i++) {
            addBasis(bf, i, weight);
        }
    }

    public void addBasis(BasisFunction bf, int fa) {
        addBasis(bf, fa, 0.0);
    }

    public void addBasis(BasisFunction bf, int fa, double weight) {
        //int previousTerms = FAs[fa].getNumTerms();
        //System.out.println("Adding "+bf.getBasisString()+" in action "+fa);
        FAs[fa].addBasis(bf);
        double newWeights[] = new double[getNumTerms()];
        double newTraces[][] = new double[traces.size()][getNumTerms()];
        int pos = 0;
        int newpos = 0;
        for (int i = 0; i < FAs.length; i++) {
            for (int j = 0; j < FAs[i].getNumTerms(); j++) {
                if (i == fa && FAs[fa].getNumTerms() - 1 == j) {
                    newWeights[newpos] = weight;
                    for (int k = 0; k < traces.size(); k++) {
                        newTraces[k][newpos] = 0;
                    }
                } else {
                    newWeights[newpos] = weights[pos];
                    for (int k = 0; k < traces.size(); k++) {
                        newTraces[k][newpos] = traces.get(k).getTrace()[pos];
                    }
                    pos++;
                }
                newpos++;
            }
        }
        weights = newWeights;
        for (int i = 0; i < traces.size(); i++) {
            traces.get(i).setTrace(newTraces[i]);
        }
    }
    
    public void addBasis(BasisFunction bf, int fa, double weight, double trace) {
        //int previousTerms = FAs[fa].getNumTerms();
        //System.out.println("Adding "+bf.getBasisString()+" in action "+fa);
        FAs[fa].addBasis(bf);
        double newWeights[] = new double[getNumTerms()];
        double newTraces[][] = new double[traces.size()][getNumTerms()];
        int pos = 0;
        int newpos = 0;
        for (int i = 0; i < FAs.length; i++) {
            for (int j = 0; j < FAs[i].getNumTerms(); j++) {
                if (i == fa && FAs[fa].getNumTerms() - 1 == j) {
                    newWeights[newpos] = weight;
                    for (int k = 0; k < traces.size()-1; k++) {
                        newTraces[k][newpos] = 0;
                    }
                    newTraces[traces.size()-1][newpos] = 0;
                } else {
                    newWeights[newpos] = weights[pos];
                    for (int k = 0; k < traces.size(); k++) {
                        newTraces[k][newpos] = traces.get(k).getTrace()[pos];
                    }
                    pos++;
                }
                newpos++;
            }
        }
        weights = newWeights;
        for (int i = 0; i < traces.size(); i++) {
            traces.get(i).setTrace(newTraces[i]);
        }
    }

    /**
     * Removes a basis function from the given action. If there are multiple
     * basis functions which are identical in the function approximator, the
     * first instance will be removed.
     *
     * @param bf the basis function to remove
     * @param fa the action to remove the basis function from.
     * @throws functionapproximation.MissingBasisException
     */
    public void removeBasis(BasisFunction bf, int fa) {
        //System.out.print("Removing "+bf.getBasisString()+" from action "+ fa);
        //System.out.println(" found at "+index);
        int indexInBigArray = getBasisIndex(bf, fa);
        if (indexInBigArray == -1) System.err.println("Basis function not found!");
        //System.out.println("and at "+indexInBigArray);
        FAs[fa].removeBasis(bf);
        double newWeights[] = removeArrayElement(weights, indexInBigArray);
        double newTrace[] = removeArrayElement(traces.get(traces.size()-1).getTrace(), indexInBigArray); // should this not be indexinbigarray
        weights = newWeights;
        traces.get(traces.size()-1).setTrace(newTrace);
    }

    public int getBasisIndex(BasisFunction bf, int action) {
        int index = FAs[action].getBasisIndex(bf);  //index in its own action
        if (index == -1) {
            //System.out.println("Error, BF to remove not found");
            return -1;
        }
        int offset = 0;
        for (int i = 0; i < action; i++) {
            offset += FAs[i].getNumTerms();
        }
        return offset + index;
    }

    public double[] removeArrayElement(double arr[], int index) {
        double newArr[] = new double[arr.length - 1];
        for (int i = 0; i < index; i++) {
            newArr[i] = arr[i];
        }
        for (int i = index + 1; i < arr.length; i++) {
            newArr[i - 1] = arr[i];
        }
        return newArr;
    }

    public int getNumActions() {
        return FAs.length;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double weights[]) {
        this.weights = weights;
    }

    public double[] calculateStateValue(State s, int action) {
        double returnArray[] = new double[getNumTerms()];
        double answer[] = FAs[action].calculateStateValue(s);
        int copyPosition = getArrayStartPosition(action);
        System.arraycopy(answer, 0, returnArray, copyPosition, FAs[action].getNumTerms());
        return returnArray;
    }
    
    public int greedyAction(State s) {
        ArrayList<Integer> vals = new ArrayList();
        vals.add(0);
        double val;
        double maxVal = valueAt(s,0);
        for (int i=1; i<FAs.length; i++) {
            val = valueAt(s, i);
            if (val > maxVal) {
                vals = new ArrayList();
                vals.add(i);
                maxVal = val;
            }
            else if (val == maxVal) {
                vals.add(i);
            }
        }
        if (vals.size() > 1) {
            Random r = new Random();
            return vals.get(r.nextInt(vals.size()));
        }
        return vals.get(0);
    }
    
    public double[] calculateOptimalStateValue(State s) {
        int action = greedyAction(s);
        double returnArray[] = new double[getNumTerms()];
        double answer[] = FAs[action].calculateStateValue(s);
        int copyPosition = getArrayStartPosition(action);
        System.arraycopy(answer, 0, returnArray, copyPosition, FAs[action].getNumTerms());
        return returnArray;
    }

    public double[] calculateStateValue(State s) {
        double returnArray[] = new double[getNumTerms()];
        int pos = 0;
        for (int i = 0; i < getNumActions(); i++) {
            double answer[] = FAs[i].calculateStateValue(s);
            for (int k = 0; k < answer.length; k++) {
                returnArray[pos] = answer[k];
                pos++;
            }
        }
        return returnArray;
    }

    public double valueAt(State s, int action) {
        return dot(calculateStateValue(s, action), weights);
    }

    public int getNumTerms() {
        int numTerms = 0;
        for (int i = 0; i < FAs.length; i++) {
            numTerms += FAs[i].getNumTerms();
        }
        return numTerms;
    }

    public int getArrayStartPosition(int action) {
        int position = 0;
        for (int i = 0; i < action; i++) {
            position += FAs[i].getNumTerms();
        }
        return position;
    }

    public int getNumTerms(int action) {
        return FAs[action].getNumTerms();
    }

    public double[] getShrink() {
        double shrink[] = new double[getNumTerms()];
        
        int pos = 0;
        //System.out.println("terms "+getNumTerms());
        //for (int i = 0; i < getNumActions(); i++) System.out.println("Action "+i+" has "+ FAs[i].getNumTerms());
        for (int i = 0; i < getNumActions(); i++) {
            for (int j = 0; j < FAs[i].getNumTerms(); j++) {
                //System.out.println(i+" has " + FAs[i].getNumTerms());
                //System.out.println(pos);
                shrink[pos] = FAs[i].getTermMagnitude(j);
                pos++;
            }
        }
        return shrink;
    }

    public double dot(double arr1[], double arr2[]) {
        if (arr1.length != arr2.length) {
            System.err.println("Dot product error. Arrays not equal in length");
            return Double.NaN;
        }
        double returnVal = 0;
        for (int i = 0; i < arr1.length; i++) {
            returnVal += arr1[i] * arr2[i];
        }
        return returnVal;
    }

    public FunctionApproximator[] getFAs() {
        return FAs;
    }

    public void printFunction() {
        for (int i = 0; i < FAs.length; i++) {
            System.err.println("Action: " + i);
            int start = getArrayStartPosition(i);
            for (int j = 0; j < FAs[i].getNumTerms(); j++) {
                System.err.println(getWeights()[start] + " " + FAs[i].getTerms()[j].getBasisString());
                start++;
            }
        }
    }

    public void printArray(double arr[]) {
        String str = "";
        for (int i = 0; i < arr.length; i++) {
            str = str + arr[i] + " ";
        }
        System.err.println(str);
    }    
    
    public void save(String fileName) {
        FileOutputStream stream = null;
        {
            ObjectOutputStream oStream = null;
            try {
                stream = new FileOutputStream(fileName);
                oStream = new ObjectOutputStream(stream);
                oStream.writeObject(this);
            } catch (IOException ex) {
                Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    stream.close();
                } catch (IOException ex) {
                    Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    oStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static QApproximator load(String fileName) {
        QApproximator q = null;
        FileInputStream stream = null;

        ObjectInputStream oStream = null;
        try {

            stream = new FileInputStream(fileName);
            oStream = new ObjectInputStream(stream);
            q = (QApproximator) oStream.readObject();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                oStream.close();
            } catch (IOException ex) {
                Logger.getLogger(QApproximator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return q;
    }
}
