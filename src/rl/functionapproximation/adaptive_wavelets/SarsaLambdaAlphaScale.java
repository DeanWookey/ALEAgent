
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import java.util.Random;
import rl.domain.State;

/**
 *
 * @author Dean
 */
public class SarsaLambdaAlphaScale {
 
    Random random;
    QApproximator q;
    ElegibilityTrace trace;
    double weights[];
    double shrink[];
    int numTerms;
    int numActions;
    
    double lambda;
    double gamma;
    double alpha;
    
    double epsilon;

    public SarsaLambdaAlphaScale(QApproximator q, int numActions, double alpha, double gamma, double lambda, double epsilon) {
        this.q = q;
        this.numTerms = q.getNumTerms();
        this.alpha = alpha;
        this.gamma = gamma;
        this.lambda = lambda;
        this.epsilon = epsilon;
        this.numActions = numActions;
        trace = q.getNewTrace();
        weights = q.getWeights();
        shrink = q.getShrink();
        random = new Random();
    }

    public void startEpisode() {
        trace.setTrace(new double[q.getNumTerms()]);
    }

    public void clearMemory() {
        double[] weights = q.getWeights();
        trace.setTrace(new double[q.getNumTerms()]);
        for (int i=0; i<weights.length; i++) {
            weights[i] = 0;
        }
        q.setWeights(weights);
        this.alpha = 1;
    }
    
    public int nextMove(State s) {
        int action;
        if ((random.nextFloat() < epsilon)) {
            action = random.nextInt(numActions);
        }
        else {
            action = q.greedyAction(s);
        }
        return action;
    }

    public double getAlpha() {
        return alpha;
    }
    
    public void agent_end(double reward) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void addSample(State s, int action, double reward, State nextState, int nextAction) {
        weights = q.getWeights();
        double trace[] = this.trace.getTrace();
        shrink = q.getShrink();
        double stateVals[] = q.calculateStateValue(s);

        int start = q.getArrayStartPosition(action);
        int end = start + q.getNumTerms(action);
        for (int i = 0; i < q.getNumTerms(); i++) {
            trace[i] = gamma * lambda * 1 * trace[i];
            if (i >= start && i < end) {
                trace[i] += stateVals[i];
            }
        }

        /* Alpha scaling include shrink */
        double bot = dot(dotDivide(trace, shrink), add(multiply(q.calculateStateValue(nextState, nextAction), gamma), multiply(q.calculateStateValue(s, action), -1)));
        
        /* Alpha scaling excluding shrink */
        //double bot = Math.abs(dot(trace, add(multiply(q.calculateStateValue(nextState, nextAction), gamma), multiply(q.calculateStateValue(s, action), -1))));
        
        /* Identical if shrink values = 1 */
        if (bot < 0) {
            alpha = Math.min(alpha, 1 / Math.abs(bot));
        }
        
        double delta = reward - q.valueAt(s, action);
        if (!nextState.isTerminal()) {
            delta += gamma * q.valueAt(nextState, nextAction);
        }
        if (java.lang.Double.isNaN(delta)) {
            System.err.println("NaN trouble");
        }


        for (int i = 0; i < q.getNumTerms(); i++) {
            weights[i] += (alpha / shrink[i]) * delta * trace[i];
        }
    }


    public double[] add(double arr1[], double arr2[]) {
        double returnArr[] = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            returnArr[i] = arr1[i] + arr2[i];
        }
        return returnArr;
    }

    public void decayAlpha(double amount) {
        alpha = alpha * amount;
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

    //pairwise multiple elements in arrays and return the resulting array
    public double[] dotMultiply(double arr1[], double arr2[]) {

        if (arr1.length != arr2.length) {
            System.err.println("Dot product error. Arrays not equal in length");
            return null;
        }
        double ret[] = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            ret[i] = arr1[i] * arr2[i];
        }
        return ret;
    }
    
        //pairwise multiple elements in arrays and return the resulting array
    public double[] dotDivide(double arr1[], double arr2[]) {

        if (arr1.length != arr2.length) {
            System.err.println("Dot product error. Arrays not equal in length");
            return null;
        }
        double ret[] = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            ret[i] = arr1[i] / arr2[i];
        }
        return ret;
    }

    public double[] multiply(double arr[], double m) {
        double returnArr[] = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            returnArr[i] = arr[i] * m;
        }
        return returnArr;
    }

    public void printArray(double arr[]) {
        String str = "";
        for (int i = 0; i < arr.length; i++) {
            str = str + arr[i] + " ";
        }
        System.err.println(str);
    }

    public String getArrayString(double arr[]) {
        String str = "";
        for (int i = 0; i < arr.length; i++) {
            str = str + arr[i] + " ";
        }
        return str;
    }
    
    public final void setEpsilon(double e) {
        epsilon = e;
    }
}
