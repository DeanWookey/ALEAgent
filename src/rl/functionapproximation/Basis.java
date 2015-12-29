/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.Arrays;
import rl.domain.State;

/**
 *
 * @author Craig
 */
public abstract class Basis {
    double[] weights;
    int numFeatures;
    double[] shrink;
    
    public abstract double getValue(State s);
    public abstract double[] computeFeatures(State s);
    public abstract void updateWeights(double[] deltaW);
    
    public double[] getShrink() {
        if(shrink == null) {
            shrink = new double[numFeatures];
            Arrays.fill(shrink, 1);
        }
        return shrink;
    }

    public final int getNumFunctions() {
        return this.numFeatures;
    }
    
    public final double[] getWeights() {
        return weights; //unsafe
    }
    
    public final void setWeights(double[] w) {
        weights = Arrays.copyOf(w,w.length);
    }
    
}
