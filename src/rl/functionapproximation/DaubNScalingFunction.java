/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;
//import org.mathIT.approximation.Wavelets;
/**
 *
 * @author Dean
 */
public class DaubNScalingFunction extends WaveletFunction {

    private final int order;
    private final double multiplier;
    
    double oldstateval = -1;
    double oldval = 0;

    public DaubNScalingFunction(int scale, int translation, int dimension, int order) {
        super(scale,translation,dimension);
        this.multiplier = Math.pow(2,scale);
        this.order = order;
    }

    @Override
    public double getValue(State s) {
        if (s.getState()[dimension] == oldstateval) {
            return oldval;
        }
        double t = multiplier*s.getState()[dimension] - translation;
        double ans = Wavelets.phi(order, t);
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
        return "Daub father (" + dimension + "," + scale + "," + translation+ ")";
    }
    
        @Override
    public boolean equals(Object o) {
        if (o instanceof DaubNScalingFunction) {
            DaubNScalingFunction d = (DaubNScalingFunction)o;
            if (this.scale == d.scale && this.translation == d.translation && this.dimension == d.dimension && this.order == d.order) {
                return true;
            }
           
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.scale;
        hash = 59 * hash + this.translation;
        hash = 59 * hash + this.dimension;
        hash = 59 * hash + this.order;
        return hash;
    }
}
