/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;
//import org.mathIT.approximation.Wavelets;
/**
 *
 * @author mitch
 */
public class DaubNWavelet implements WiFDDBasisFunction{
    
    private final int scale;
    private final int translation;
    private final int dimension;
    private final int order;
    private final double multiplier; 
    
    double oldstateval = -1;
    double oldval = 0;
    private double activation;
    private int arrayIndex;
    public DaubNWavelet(int scale, int translation, int dimension, int order) {
        this.scale = scale;
        this.multiplier = Math.pow(2,scale);
        this.translation = translation;
        this.dimension = dimension;
        this.order = order;
    }
    @Override
    public double getValue(State s) {
        if (s.getState()[dimension] == oldstateval) {
            return oldval;
        }
        double t = multiplier*s.getState()[dimension] - translation;
        double ans = Wavelets.psi(order, t);
        oldstateval = s.getState()[dimension];
        oldval = ans;
        return ans;
    }

    @Override
    public double getShrink() {
        return 1;
    }

    @Override
    public String getBasisString() {
        return "(d=" + dimension + ",s=" + scale + ",t=" + translation+ ")";
    }
        @Override
    public ArrayList<Integer> getDimensions() {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(dimension);
        return temp;
    }
        @Override
    public double getActivation() {
        return activation;
    }

    @Override
    public void setActivation(double activation) {
        this.activation = activation;
    }

    @Override
    public void decreaseActivation(double amount) {
        this.activation = this.activation - amount;
        this.activation = Math.max(0, activation);
    }
       @Override
    public int getArrayIndex() {
        return this.arrayIndex;
    }

    @Override
    public void setArrayIndex(int index) {
        this.arrayIndex = index;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof DaubNWavelet) {
            DaubNWavelet d = (DaubNWavelet)o;
            if (this.scale == d.scale && this.translation == d.translation && this.dimension == d.dimension && this.order == d.order) {
                return true;
            }
           
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.scale;
        hash = 41 * hash + this.translation;
        hash = 41 * hash + this.dimension;
        hash = 41 * hash + this.order;
        return hash;
    }
    
}
