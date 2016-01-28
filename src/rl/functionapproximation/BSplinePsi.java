/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public class BSplinePsi extends BasisFunction {
    private final int scale;
    private final int translation;
    private final int dimension;
    private final double multiplier;
    private final int order;
    
    double oldstateval = -1;
    double oldval = 0;
    private double activation;
    private int arrayIndex;
    
    
    public BSplinePsi(int scale, int translation, int dimension, int order) {
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
        double ans = BSpline.Psi(t, order);
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
        return ("Psi (1," + dimension + "," + scale + "," + translation+")");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.scale;
        hash = 59 * hash + this.translation;
        hash = 59 * hash + this.dimension;
        return hash;
    }
}