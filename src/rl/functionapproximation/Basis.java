/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.Arrays;
import java.util.Random;
import rl.domain.State;

/**
 *
 * @author Craig
 */
public abstract class Basis implements Cloneable{
    double[] weights;
    int numFeatures;
    double[] shrink;
    
    public abstract double getValue(State s);
    public abstract double getValue(double[] phi);
    public abstract double[] computeFeatures(State s);
    public abstract void updateWeights(double[] deltaW);
    
    public double[] getShrink() {
        if(shrink == null) {
            shrink = new double[numFeatures];
            Arrays.fill(shrink, 1);
        }
        return shrink;
    }
    
    public void setShrink(double[] s) {
        shrink = s;
    }

    public final int getNumFunctions() {
        return this.numFeatures;
    }
    
    public final double[] getWeights() {
        return weights; //unsafe
    }
    
    public final void setWeights(double[] w) {
        //weights = Arrays.copyOf(w,w.length);
        System.arraycopy(w, 0, this.weights, 0, numFeatures);
    }
    
    public void randomiseWeights() {
        Random rand = new Random();
        for(int i = 0; i < numFeatures; i++) weights[i] = rand.nextDouble()*0.0001;
    }
    
    @Override
    public Object clone() {
        try {
            Basis obj = (Basis) super.clone();
            
            obj.weights = new double[numFeatures];
            System.arraycopy(this.weights, 0, obj.weights, 0, numFeatures);
            

            return obj;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
