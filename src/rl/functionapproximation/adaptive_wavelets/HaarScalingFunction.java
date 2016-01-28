/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author Dean
 */
public class HaarScalingFunction implements WiFDDBasisFunction {

    private final int scale;
    private final int translation;
    private final int dimension;
    private final double multiplier;
    private double activation;
    private int arrayIndex;

    public HaarScalingFunction(int scale, int translation, int dimension) {
        this.scale = scale;
        this.multiplier = Math.pow(2, scale);
        this.translation = translation;
        this.dimension = dimension;
    }

    @Override
    public double getValue(State s) {
        double t = multiplier * s.getState()[dimension] - translation;
        if (t >= 0 && t < 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public double getShrink() {
        return 1;
    }

    @Override
    public String getBasisString() {
        return "(" + dimension + "," + scale + "," + translation + ")";
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
        if (o instanceof HaarScalingFunction) {
            HaarScalingFunction d = (HaarScalingFunction)o;
            if (this.scale == d.scale && this.translation == d.translation && this.dimension == d.dimension) {
                return true;
            }
           
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.scale;
        hash = 67 * hash + this.translation;
        hash = 67 * hash + this.dimension;
        return hash;
    }
}
